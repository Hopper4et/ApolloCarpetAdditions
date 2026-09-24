package Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix;

import Hopper4et.apollocarpetadditions.utils.ChunkUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EnderPearlNotLoadChunksFix {

    private static final double FAST_SPEED = 16d;
    private static final Set<ThrownEnderpearl> fastEnderPearls = new HashSet<>();

    public static void enderPearlTick(ThrownEnderpearl enderPearl, CallbackInfo ci) {
        Vec3 velocity = enderPearl.getDeltaMovement();
        if (!(Math.abs(velocity.x) > FAST_SPEED || Math.abs(velocity.z) > FAST_SPEED)) {
            fastEnderPearls.remove(enderPearl);
            return;
        }
        fastEnderPearls.add(enderPearl);
        loadChunks(enderPearl);
        if (!pathIsLoaded((ServerLevel) enderPearl.level(), enderPearl.position(), enderPearl.position().add(velocity))) {
            ci.cancel();
        }
    }

    private static boolean pathIsLoaded(ServerLevel world, Vec3 start, Vec3 end) {
        if (!ChunkUtils.isChunkEntityLoaded(world, ChunkUtils.getChunkPos(end))) return false;
        return ChunkUtils.raycastChunkSelection(
                start,
                end,
                (chunkSectionPos) -> {
                    if (!world.isInsideBuildHeight(chunkSectionPos.minBlockY())) return true;
                    CompletableFuture<net.minecraft.server.level.ChunkResult<ChunkAccess>> future = world.getChunkSource().getChunkFuture(
                            chunkSectionPos.getX(),
                            chunkSectionPos.getZ(),
                            ChunkStatus.FULL,
                            true
                    );
                    return future.join().isSuccess();
                }
        );
    }

    private static void loadChunks(ThrownEnderpearl enderPearl) {
        if (!(enderPearl.level() instanceof ServerLevel serverWorld)) return;
        Vec3 pos = enderPearl.position();
        //todo loadEnderPearlChunk
        //ChunkUtils.loadEnderPearlChunk(serverWorld, ChunkUtils.getChunkPos(pos));
        //ChunkUtils.loadEnderPearlChunk(serverWorld, ChunkUtils.getChunkPos(pos.add(enderPearl.getVelocity())));
    }

    public static void tick() {
        fastEnderPearls.forEach(EnderPearlNotLoadChunksFix::loadChunks);
    }

    public static void removeEnderPearl(ThrownEnderpearl enderPearl) {
        fastEnderPearls.remove(enderPearl);
    }

    public static void removeAllFastEnderPearls() {
        fastEnderPearls.clear();
    }
}
