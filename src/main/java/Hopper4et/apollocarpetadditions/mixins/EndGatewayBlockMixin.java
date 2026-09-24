package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(EndGatewayBlock.class)
public abstract class EndGatewayBlockMixin {
    @Inject(method = "getPortalDestination",
            at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void createTeleportTarget(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos, CallbackInfoReturnable<TeleportTransition> cir, @Local(name = "teleportPosition") Vec3 teleportPosition) {
        TeleportTransition.PostTeleportTransition postDimensionTransition = ApolloCarpetAdditionsSettings.endGatewaysLoadChunks ? TeleportTransition.PLACE_PORTAL_TICKET : TeleportTransition.DO_NOTHING;
        cir.setReturnValue(entity instanceof ThrownEnderpearl ?
                new TeleportTransition(currentLevel, teleportPosition, Vec3.ZERO, 0.0F, 0.0F, Set.of(), postDimensionTransition) :
                new TeleportTransition(currentLevel, teleportPosition, Vec3.ZERO, 0.0F, 0.0F, Relative.union(Relative.DELTA, Relative.ROTATION), postDimensionTransition));
    }
}
