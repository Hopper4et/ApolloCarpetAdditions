package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientboundAddEntityPacket.class)
public class EntitySpawnS2CPacketMixin {
    @ModifyArgs(method = "<init>(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerEntity;I)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;<init>(ILjava/util/UUID;DDDFFLnet/minecraft/world/entity/EntityType;ILnet/minecraft/world/phys/Vec3;D)V"))
    private static void injected(Args args, @Local(argsOnly = true, ordinal = 0) Entity entity) {
        if (ApolloCarpetAdditionsSettings.lazyLinkingPre1_21Render) {
            args.set(2, entity.getX());
            args.set(3, entity.getY());
            args.set(4, entity.getZ());
        }
    }
}
