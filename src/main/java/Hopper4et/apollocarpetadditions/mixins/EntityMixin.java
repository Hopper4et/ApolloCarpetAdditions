package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.CarpetSettings;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    //todo probably added by mojang
    /*@Inject(method = "queueBlockCollisionCheck", at = @At("HEAD"), cancellable = true)
    private void queueBlockCollisionCheck(Vec3d vec3, Vec3d vec32, CallbackInfo ci) {
        if (ApolloCarpetAdditionsSettings.portalNoClipFix && (Entity)(Object)this instanceof PlayerEntity playerEntity && (playerEntity.isSpectator() || (CarpetSettings.creativeNoClip && playerEntity.getAbilities().flying)))
            ci.cancel();
    }*/
}
