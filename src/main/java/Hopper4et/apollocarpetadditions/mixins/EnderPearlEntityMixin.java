package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix.EnderPearlNotLoadChunksFix;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin extends ThrowableItemProjectile {

    public EnderPearlEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onRemoval", at = @At(value = "HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if (ApolloCarpetAdditionsSettings.enderPearlNotLoadChunksFix) {
            EnderPearlNotLoadChunksFix.removeEnderPearl((ThrownEnderpearl) (Object) this);
        }
    }
}
