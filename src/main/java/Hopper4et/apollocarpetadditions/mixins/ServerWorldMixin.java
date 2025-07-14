package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "tickPassenger", at = @At(value = "RETURN"))
    private void tickPassenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if (!ApolloCarpetAdditionsSettings.pigCannonUnstuck) return;
        if (passenger instanceof ServerPlayerEntity player) {
            player.getServerWorld().getChunkManager().updatePosition(player);
        }
    }
}
