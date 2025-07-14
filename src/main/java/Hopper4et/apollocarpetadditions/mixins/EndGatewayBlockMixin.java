package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(EndGatewayBlock.class)
public abstract class EndGatewayBlockMixin {
    @Inject(method = "createTeleportTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/TeleportTarget;",
            at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTarget> cir, @Local Vec3d vec3d) {
        TeleportTarget.PostDimensionTransition postDimensionTransition =
                ApolloCarpetAdditionsSettings.endGatewaysLoadChunks ? TeleportTarget.ADD_PORTAL_CHUNK_TICKET : TeleportTarget.NO_OP;
        cir.setReturnValue(entity instanceof EnderPearlEntity ?
                new TeleportTarget(world, vec3d, Vec3d.ZERO, 0.0F, 0.0F, Set.of(), postDimensionTransition) :
                new TeleportTarget(world, vec3d, Vec3d.ZERO, 0.0F, 0.0F, PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT), postDimensionTransition));
    }
}
