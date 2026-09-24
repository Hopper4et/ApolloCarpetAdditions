package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix.EnderPearlNotLoadChunksFix;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrowableProjectile.class)
public abstract class ThrownEntityMixin extends Entity {

    public ThrownEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    protected abstract void handleFirstTickBubbleColumn();

    @Shadow
    protected abstract void applyInertia();


    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {

        if (!ApolloCarpetAdditionsSettings.enderPearlNotLoadChunksFix) return;
        if (!((ThrowableProjectile) (Object) this instanceof ThrownEnderpearl enderPearl)) return;
        if (this.level().isClientSide()) return;

        //get new velocity
        Vec3 velocity = this.getDeltaMovement();
        this.handleFirstTickBubbleColumn();
        this.applyGravity();
        this.applyInertia();
        EnderPearlNotLoadChunksFix.enderPearlTick(enderPearl, ci);
        this.setDeltaMovement(velocity);
    }
}