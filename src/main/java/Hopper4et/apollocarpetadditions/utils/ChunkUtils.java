package Hopper4et.apollocarpetadditions.utils;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ChunkUtils {
    private static final int ENDER_PEARL_TICKS = 40;
    private static final ChunkTicketType<ChunkPos> ENDER_PEARL_PATH_TICKET = ChunkTicketType.create("ender_pearl_path", Comparator.comparingLong(ChunkPos::toLong));
    private static final Map<ChunkPos, TickTaskManager.TickTask> enderPearlLoadedChunks = new HashMap<>();

    public static void loadEnderPearlChunk(ServerWorld world, ChunkPos chunkPos) {
        if (enderPearlLoadedChunks.containsKey(chunkPos)) {
            enderPearlLoadedChunks.get(chunkPos).refresh(ENDER_PEARL_TICKS);
        } else {
            world.getChunkManager().addTicket(ENDER_PEARL_PATH_TICKET, chunkPos, 2, chunkPos);
            enderPearlLoadedChunks.put(chunkPos, TickTaskManager.createTask(() -> unloadEnderPearlChunk(world, chunkPos), ENDER_PEARL_TICKS));
        }
    }

    public static void unloadEnderPearlChunk(ServerWorld world, ChunkPos chunkPos) {
        world.getChunkManager().removeTicket(ENDER_PEARL_PATH_TICKET, chunkPos, 2, chunkPos);
        enderPearlLoadedChunks.remove(chunkPos);
    }

    public static ChunkPos getChunkPos(Vec3d pos) {
        return new ChunkPos(MathHelper.floor(pos.x) >> 4, MathHelper.floor(pos.z) >> 4);
    }

    public static boolean isChunkEntityLoaded(ServerWorld world, ChunkPos chunkPos) {
        return world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false) instanceof WorldChunk;
    }

    //uses predicate chunkHitFactory for every chunk selection on raycast path and returns false if any predicate returns false
    public static boolean raycastChunkSelection(Vec3d start, Vec3d end, Predicate<ChunkSectionPos> chunkHitFactory) {
        double endX = MathHelper.lerp(-1.0E-7, end.x, start.x);
        double endY = MathHelper.lerp(-1.0E-7, end.y, start.y);
        double endZ = MathHelper.lerp(-1.0E-7, end.z, start.z);
        double startX = MathHelper.lerp(-1.0E-7, start.x, end.x);
        double startY = MathHelper.lerp(-1.0E-7, start.y, end.y);
        double startZ = MathHelper.lerp(-1.0E-7, start.z, end.z);
        int currentChunkSelectionX = ChunkSectionPos.getSectionCoordFloored(startX);
        int currentChunkSelectionY = ChunkSectionPos.getSectionCoordFloored(startY);
        int currentChunkSelectionZ = ChunkSectionPos.getSectionCoordFloored(startZ);
        ChunkSectionPos currentChunkSection = ChunkSectionPos.from(currentChunkSelectionX, currentChunkSelectionY, currentChunkSelectionZ);

        if (!(boolean) chunkHitFactory.test(currentChunkSection)) return false;

        double distanceX = endX - startX;
        double distanceY = endY - startY;
        double distanceZ = endZ - startZ;
        int signOfDistanceX = MathHelper.sign(distanceX);
        int signOfDistanceY = MathHelper.sign(distanceY);
        int signOfDistanceZ = MathHelper.sign(distanceZ);
        double progressStepX = signOfDistanceX == 0 ? Double.MAX_VALUE : signOfDistanceX * 16 / distanceX;
        double progressStepY = signOfDistanceY == 0 ? Double.MAX_VALUE : signOfDistanceY * 16 / distanceY;
        double progressStepZ = signOfDistanceZ == 0 ? Double.MAX_VALUE : signOfDistanceZ * 16 / distanceZ;
        double progressX = progressStepX * (signOfDistanceX > 0 ? 1.0 - partOfChunk(startX) : partOfChunk(startX));
        double progressY = progressStepY * (signOfDistanceY > 0 ? 1.0 - partOfChunk(startY) : partOfChunk(startY));
        double progressZ = progressStepZ * (signOfDistanceZ > 0 ? 1.0 - partOfChunk(startZ) : partOfChunk(startZ));

        while (progressX <= 1.0 || progressY <= 1.0 || progressZ <= 1.0) {
            if (progressX < progressY) {
                if (progressX < progressZ) {
                    currentChunkSelectionX += signOfDistanceX;
                    progressX += progressStepX;
                } else {
                    currentChunkSelectionZ += signOfDistanceZ;
                    progressZ += progressStepZ;
                }
            } else if (progressY < progressZ) {
                currentChunkSelectionY += signOfDistanceY;
                progressY += progressStepY;
            } else {
                currentChunkSelectionZ += signOfDistanceZ;
                progressZ += progressStepZ;
            }

            currentChunkSection = ChunkSectionPos.from(currentChunkSelectionX, currentChunkSelectionY, currentChunkSelectionZ);
            if (!(boolean) chunkHitFactory.test(currentChunkSection)) return false;
        }

        return true;
    }

    private static double partOfChunk(double value) {
        return (value - (MathHelper.lfloor(value) >> 4 << 4)) / 16;
    }
}
