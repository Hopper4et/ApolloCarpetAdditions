package Hopper4et.apollocarpetadditions.rules.fakePlayerChunkLoadingPriority;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import Hopper4et.apollocarpetadditions.mixins.ChunkMapAccessor;
import Hopper4et.apollocarpetadditions.mixins.ChunkHolderAccessor;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.TicketStorage;
import org.jspecify.annotations.Nullable;

/**
 * Chunk loading for Carpet fake players in one dimension (one per {@link net.minecraft.server.level.DistanceManager}).
 * <p>
 * Vanilla adds the view distance loading tickets of all players through one throttler: at most 4 chunks are in flight,
 * a slot is freed only when its chunk becomes entity ticking, and the queue is ordered by the distance to the nearest
 * player, real or fake. The worldgen and light queues behind it are ordered by ticket level, which is 31 for every
 * player chunk. So a bot waits behind whatever real players are generating, and its far chunks can starve while a real
 * player keeps flying into new terrain.
 * <p>
 * Here bots get a throttler of their own with the same 4 slots, filled nearest to a bot first with
 * {@link FakePlayerTicket}, and every chunk bot tickets keep in memory is queued for worldgen and light before real
 * players' chunks. The vanilla tickets stay as they are, so the loaded chunks and their levels end up the same as in
 * vanilla; only the order and the speed change.
 * <p>
 * The throttling matters: vanilla code on the main thread treats a chunk as present as soon as its ticket level says
 * FULL ({@code ServerChunkCache.hasChunk}) and then waits for it synchronously (collisions, fluids). Ticketing a whole
 * view distance at once and putting it ahead of everything made such a wait for a real player's chunk last until the
 * whole bot area was generated, freezing the server for seconds.
 */
public final class FakePlayerChunkLoader {
    /** Same level as vanilla player loading tickets. */
    private static final int TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    /** How far past the view distance a level 31 ticket still keeps chunks in memory as partially generated border. */
    private static final int BORDER = ChunkLevel.MAX_LEVEL - TICKET_LEVEL;
    /** Vanilla never queues a player chunk above priority 31, boosted chunks use 0..30. */
    private static final int MAX_BOOSTED_PRIORITY = TICKET_LEVEL - 1;
    /** Same as the vanilla player ticket throttler, but these slots are for bots only. */
    private static final int MAX_CHUNKS_IN_FLIGHT = 4;
    private static final int NONE = -1;
    private static final Ticket TICKET_TO_REMOVE = new Ticket(FakePlayerTicket.TYPE, TICKET_LEVEL);

    private final TicketStorage ticketStorage;
    private final Long2IntOpenHashMap fakePlayersPerChunk = new Long2IntOpenHashMap();
    /** Distance to the nearest bot for every chunk kept in memory by bot tickets, as of the last applied update. */
    private final Long2IntOpenHashMap boostedChunks = new Long2IntOpenHashMap();
    /** Chunks within view distance of a bot that still wait for a ticket, by their distance to the bot. */
    private final Long2IntOpenHashMap waitingChunks = new Long2IntOpenHashMap();
    private final LongLinkedOpenHashSet[] waitingByDistance = new LongLinkedOpenHashSet[ChunkMap.MAX_VIEW_DISTANCE + 1];
    private final LongOpenHashSet ticketedChunks = new LongOpenHashSet();
    /** Ticketed chunks that are not entity ticking yet, each holds a throttler slot. */
    private final LongOpenHashSet chunksInFlight = new LongOpenHashSet();
    /** Null while the rule is off: nothing is tracked then and vanilla runs untouched. */
    private @Nullable DistanceTracker tracker;
    private int viewDistance;

    public FakePlayerChunkLoader(TicketStorage ticketStorage) {
        this.ticketStorage = ticketStorage;
        this.boostedChunks.defaultReturnValue(NONE);
        this.waitingChunks.defaultReturnValue(NONE);
        for (int distance = 0; distance < this.waitingByDistance.length; distance++) {
            this.waitingByDistance[distance] = new LongLinkedOpenHashSet();
        }
    }

    public void addFakePlayer(long chunk) {
        if (this.fakePlayersPerChunk.addTo(chunk, 1) == 0 && this.tracker != null) {
            this.tracker.update(chunk, 0, true);
        }
    }

    public void removeFakePlayer(long chunk) {
        int count = this.fakePlayersPerChunk.get(chunk);
        if (count > 1) {
            this.fakePlayersPerChunk.put(chunk, count - 1);
        } else if (count == 1) {
            this.fakePlayersPerChunk.remove(chunk);
            if (this.tracker != null) {
                this.tracker.update(chunk, Integer.MAX_VALUE, false);
            }
        }
    }

    /** Called with the server view distance, the same one vanilla uses for player loading tickets. */
    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    /** Runs at the start of every {@code DistanceManager.runAllUpdates}, before vanilla propagates ticket levels. */
    public void runUpdates(ChunkMap chunkMap) {
        if (!ApolloCarpetAdditionsSettings.fakePlayerChunkLoadingPriority) {
            if (this.tracker != null) {
                this.tracker = null;
                this.releaseAll(chunkMap);
            }
            return;
        }

        if (this.tracker == null || this.tracker.viewDistance != this.viewDistance) {
            this.rebuildTracker();
        }

        DistanceTracker tracker = this.tracker;
        tracker.runAllUpdates();
        if (!tracker.changed.isEmpty()) {
            LongIterator iterator = tracker.changed.iterator();
            while (iterator.hasNext()) {
                long chunk = iterator.nextLong();
                this.apply(chunkMap, chunk, tracker.distance(chunk));
            }
            tracker.changed.clear();
        }

        this.releaseLoadedChunks(chunkMap);
        this.ticketWaitingChunks(chunkMap);
    }

    /**
     * Queue priority for a chunk whose ticket level just changed, used by both the worldgen and the light queues.
     * Lower runs first.
     */
    public int queuePriority(long chunk, int ticketLevel) {
        if (this.tracker == null || !ChunkLevel.isLoaded(ticketLevel)) {
            return ticketLevel;
        }

        int distance = this.boostedChunks.get(chunk);
        return distance == NONE ? ticketLevel : Math.min(ticketLevel, Math.min(distance, MAX_BOOSTED_PRIORITY));
    }

    private void rebuildTracker() {
        DistanceTracker tracker = new DistanceTracker(this.viewDistance, this.fakePlayersPerChunk);
        LongIterator sources = this.fakePlayersPerChunk.keySet().iterator();
        while (sources.hasNext()) {
            tracker.update(sources.nextLong(), 0, true);
        }

        tracker.runAllUpdates();
        // Chunks the old range reached but the new one does not still have to lose their ticket and boost
        tracker.changed.addAll(this.ticketedChunks);
        tracker.changed.addAll(this.waitingChunks.keySet());
        tracker.changed.addAll(this.boostedChunks.keySet());
        this.tracker = tracker;
    }

    private void apply(ChunkMap chunkMap, long chunk, int distance) {
        boolean wanted = distance <= this.viewDistance;
        if (this.ticketedChunks.contains(chunk)) {
            if (!wanted) {
                this.ticketedChunks.remove(chunk);
                this.chunksInFlight.remove(chunk);
                this.ticketStorage.removeTicket(chunk, TICKET_TO_REMOVE);
            }
        } else {
            int waitingAt = this.waitingChunks.get(chunk);
            int waitAt = wanted ? distance : NONE;
            if (waitingAt != waitAt) {
                if (waitingAt != NONE) {
                    this.waitingByDistance[waitingAt].remove(chunk);
                    this.waitingChunks.remove(chunk);
                }
                if (waitAt != NONE) {
                    this.waitingByDistance[waitAt].add(chunk);
                    this.waitingChunks.put(chunk, waitAt);
                }
            }
        }

        boolean boosted = distance <= this.viewDistance + BORDER;
        boolean wasBoosted = boosted ? this.boostedChunks.put(chunk, distance) != NONE : this.boostedChunks.remove(chunk) != NONE;
        // Moving inside the boosted area keeps the priority the chunk was queued with: every bot chunk is still
        // ahead of real players, and the new chunks at the front of a moving bot are queued by their own distance.
        if (boosted != wasBoosted) {
            requeue(chunkMap, chunk);
        }
    }

    /** Frees the slot of every ticketed chunk that reached entity ticking, like vanilla does with its throttler. */
    private void releaseLoadedChunks(ChunkMap chunkMap) {
        if (this.chunksInFlight.isEmpty()) {
            return;
        }

        LongIterator iterator = this.chunksInFlight.iterator();
        while (iterator.hasNext()) {
            ChunkHolder holder = chunkMap.getUpdatingChunkIfPresent(iterator.nextLong());
            // Until vanilla applies the new ticket level the holder still has the completed "unloaded" future
            if (holder != null && holder.getTicketLevel() <= TICKET_LEVEL && holder.getEntityTickingChunkFuture().isDone()) {
                iterator.remove();
            }
        }
    }

    private void ticketWaitingChunks(ChunkMap chunkMap) {
        int distance = 0;
        while (this.chunksInFlight.size() < MAX_CHUNKS_IN_FLIGHT && !this.waitingChunks.isEmpty()) {
            while (this.waitingByDistance[distance].isEmpty()) {
                distance++;
            }

            long chunk = this.waitingByDistance[distance].removeFirstLong();
            this.waitingChunks.remove(chunk);
            this.ticketedChunks.add(chunk);
            // Chunks real players already keep entity ticking only get the ticket, they have nothing left to load
            if (!isEntityTicking(chunkMap, chunk)) {
                this.chunksInFlight.add(chunk);
            }
            this.ticketStorage.addTicket(chunk, new Ticket(FakePlayerTicket.TYPE, TICKET_LEVEL));
        }
    }

    private void releaseAll(ChunkMap chunkMap) {
        LongIterator ticketed = this.ticketedChunks.iterator();
        while (ticketed.hasNext()) {
            this.ticketStorage.removeTicket(ticketed.nextLong(), TICKET_TO_REMOVE);
        }
        this.ticketedChunks.clear();
        this.chunksInFlight.clear();
        this.waitingChunks.clear();
        for (LongLinkedOpenHashSet waiting : this.waitingByDistance) {
            waiting.clear();
        }

        long[] boosted = this.boostedChunks.keySet().toLongArray();
        this.boostedChunks.clear();
        for (long chunk : boosted) {
            requeue(chunkMap, chunk);
        }
    }

    private static boolean isEntityTicking(ChunkMap chunkMap, long chunk) {
        ChunkHolder holder = chunkMap.getUpdatingChunkIfPresent(chunk);
        return holder != null && holder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).isSuccess();
    }

    /** Moves the chunk's pending worldgen and light tasks to the priority {@link #queuePriority} now gives it. */
    private static void requeue(ChunkMap chunkMap, long chunk) {
        ChunkHolder holder = chunkMap.getUpdatingChunkIfPresent(chunk);
        if (holder == null || !ChunkLevel.isLoaded(holder.getTicketLevel())) {
            return;
        }

        // Same call vanilla makes when the ticket level changes, the priority is swapped in by ChunkMapMixin
        ((ChunkMapAccessor) chunkMap).apolloCarpetAdditions$onLevelChange(
                holder.getPos(), holder::getQueueLevel, holder.getTicketLevel(), ((ChunkHolderAccessor) holder)::setQueueLevelAccessor
        );
    }

    /** Chebyshev distance to the nearest chunk with a bot, like vanilla's player distance trackers. */
    private static final class DistanceTracker extends ChunkTracker {
        private final int viewDistance;
        private final int maxDistance;
        private final Long2IntOpenHashMap sources;
        private final Long2ByteOpenHashMap distances = new Long2ByteOpenHashMap();
        /** Chunks whose distance changed since the last apply. Levels change in steps during propagation, so only the final value is applied. */
        private final LongOpenHashSet changed = new LongOpenHashSet();

        private DistanceTracker(int viewDistance, Long2IntOpenHashMap sources) {
            super(viewDistance + BORDER + 2, 16, 256);
            this.viewDistance = viewDistance;
            this.maxDistance = viewDistance + BORDER;
            this.sources = sources;
            this.distances.defaultReturnValue((byte) (this.maxDistance + 2));
        }

        private int distance(long chunk) {
            int distance = this.distances.get(chunk);
            return distance <= this.maxDistance ? distance : Integer.MAX_VALUE;
        }

        private void runAllUpdates() {
            this.runUpdates(Integer.MAX_VALUE);
        }

        @Override
        protected int getLevelFromSource(long to) {
            return this.sources.get(to) > 0 ? 0 : Integer.MAX_VALUE;
        }

        @Override
        protected int getLevel(long node) {
            return this.distances.get(node);
        }

        @Override
        protected void setLevel(long node, int level) {
            if (level > this.maxDistance) {
                this.distances.remove(node);
            } else {
                this.distances.put(node, (byte) level);
            }
            this.changed.add(node);
        }
    }
}