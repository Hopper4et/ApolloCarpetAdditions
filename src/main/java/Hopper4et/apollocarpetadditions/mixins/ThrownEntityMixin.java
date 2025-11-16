package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix.EnderPearlNotLoadChunksFix;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin extends Entity {

    public ThrownEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract void tickInitialBubbleColumnCollision();

    @Shadow
    protected abstract void applyDrag();


    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {

        if (!ApolloCarpetAdditionsSettings.enderPearlNotLoadChunksFix) return;
        if (!((ThrownEntity) (Object) this instanceof EnderPearlEntity enderPearl)) return;
        if (this.getWorld().isClient) return;

        //get new velocity
        Vec3d velocity = this.getVelocity();
        this.tickInitialBubbleColumnCollision();
        this.applyGravity();
        this.applyDrag();
        EnderPearlNotLoadChunksFix.enderPearlTick(enderPearl, ci);
        this.setVelocity(velocity);
    }
}