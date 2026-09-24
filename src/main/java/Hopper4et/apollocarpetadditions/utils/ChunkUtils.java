package Hopper4et.apollocarpetadditions.utils;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class ChunkUtils {

    //TODO TicketType изменился там чето теперь нельзя сделать его
/*    private static final int ENDER_PEARL_TICKS = 40;
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
*/
    public static ChunkPos getChunkPos(Vec3 pos) {
        return new ChunkPos(MathUtil.floor(pos.x) >> 4, MathUtil.floor(pos.z) >> 4);
    }

    public static boolean isChunkEntityLoaded(ServerLevel level, ChunkPos chunkPos) {
        return level.getChunk(chunkPos.x(), chunkPos.z(), ChunkStatus.FULL, false) instanceof LevelChunk;
    }

    //uses predicate chunkHitFactory for every chunk selection on raycast path and returns false if any predicate returns false
    public static boolean raycastChunkSelection(Vec3 start, Vec3 end, Predicate<SectionPos> chunkHitFactory) {
        double endX = MathUtil.lerp(-1.0E-7, end.x, start.x);
        double endY = MathUtil.lerp(-1.0E-7, end.y, start.y);
        double endZ = MathUtil.lerp(-1.0E-7, end.z, start.z);
        double startX = MathUtil.lerp(-1.0E-7, start.x, end.x);
        double startY = MathUtil.lerp(-1.0E-7, start.y, end.y);
        double startZ = MathUtil.lerp(-1.0E-7, start.z, end.z);
        int currentChunkSelectionX = MathUtil.getSectionCoordFloored(startX);
        int currentChunkSelectionY = MathUtil.getSectionCoordFloored(startY);
        int currentChunkSelectionZ = MathUtil.getSectionCoordFloored(startZ);
        SectionPos currentChunkSection = SectionPos.of(currentChunkSelectionX, currentChunkSelectionY, currentChunkSelectionZ);

        if (!(boolean) chunkHitFactory.test(currentChunkSection)) return false;

        double distanceX = endX - startX;
        double distanceY = endY - startY;
        double distanceZ = endZ - startZ;
        int signOfDistanceX = MathUtil.sign(distanceX);
        int signOfDistanceY = MathUtil.sign(distanceY);
        int signOfDistanceZ = MathUtil.sign(distanceZ);
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

            currentChunkSection = SectionPos.of(currentChunkSelectionX, currentChunkSelectionY, currentChunkSelectionZ);
            if (!(boolean) chunkHitFactory.test(currentChunkSection)) return false;
        }

        return true;
    }

    private static double partOfChunk(double value) {
        return (value - (MathUtil.lfloor(value) >> 4 << 4)) / 16;
    }
}
