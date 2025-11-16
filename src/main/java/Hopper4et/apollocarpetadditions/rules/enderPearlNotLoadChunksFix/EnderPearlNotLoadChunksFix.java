package Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix;

import Hopper4et.apollocarpetadditions.utils.ChunkUtils;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EnderPearlNotLoadChunksFix {

    private static final double FAST_SPEED = 16d;
    private static final Set<EnderPearlEntity> fastEnderPearls = new HashSet<>();

    public static void enderPearlTick(EnderPearlEntity enderPearl, CallbackInfo ci) {
        Vec3d velocity = enderPearl.getVelocity();
        if (!(Math.abs(velocity.x) > FAST_SPEED || Math.abs(velocity.z) > FAST_SPEED)) {
            fastEnderPearls.remove(enderPearl);
            return;
        }
        fastEnderPearls.add(enderPearl);
        loadChunks(enderPearl);
        if (!pathIsLoaded((ServerWorld) enderPearl.getWorld(), enderPearl.getPos(), enderPearl.getPos().add(velocity))) {
            ci.cancel();
        }
    }

    private static boolean pathIsLoaded(ServerWorld world, Vec3d start, Vec3d end) {
        if (!ChunkUtils.isChunkEntityLoaded(world, ChunkUtils.getChunkPos(end))) return false;
        return ChunkUtils.raycastChunkSelection(
                start,
                end,
                (chunkSectionPos) -> {
                    if (!world.isInBuildLimit(chunkSectionPos.getMinPos())) return true;
                    CompletableFuture<OptionalChunk<Chunk>> future = world.getChunkManager().getChunkFutureSyncOnMainThread(
                            chunkSectionPos.getX(),
                            chunkSectionPos.getZ(),
                            ChunkStatus.FULL,
                            true
                    );
                    return future.join().isPresent();
                }
        );
    }

    private static void loadChunks(EnderPearlEntity enderPearl) {
        if (!(enderPearl.getWorld() instanceof ServerWorld serverWorld)) return;
        Vec3d pos = enderPearl.getPos();
        ChunkUtils.loadEnderPearlChunk(serverWorld, ChunkUtils.getChunkPos(pos));
        ChunkUtils.loadEnderPearlChunk(serverWorld, ChunkUtils.getChunkPos(pos.add(enderPearl.getVelocity())));
    }

    public static void tick() {
        fastEnderPearls.forEach(EnderPearlNotLoadChunksFix::loadChunks);
    }

    public static void removeEnderPearl(EnderPearlEntity enderPearl) {
        fastEnderPearls.remove(enderPearl);
    }

    public static void removeAllFastEnderPearls() {
        fastEnderPearls.clear();
    }
}
