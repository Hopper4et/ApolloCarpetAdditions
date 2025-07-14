package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntitySpawnS2CPacket.class)
public class EntitySpawnS2CPacketMixin {
    @ModifyArgs(method = "<init>(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/network/EntityTrackerEntry;I)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;<init>(ILjava/util/UUID;DDDFFLnet/minecraft/entity/EntityType;ILnet/minecraft/util/math/Vec3d;D)V"))
    private static void injected(Args args, @Local(argsOnly = true) Entity entity) {
        if (ApolloCarpetAdditionsSettings.lazyLinkingPre1_21Render) {
            args.set(2, entity.getX());
            args.set(3, entity.getY());
            args.set(4, entity.getZ());
        }
    }
}
