package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class ItemMixin {
    @Redirect(
            method = "getPlayerPOVHitResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;blockInteractionRange()D")
    )
    private static double changeInteractionRange(Player player) {
        if(ApolloCarpetAdditionsSettings.playerBucketInteractionRange == -1){
            return player.blockInteractionRange();
        } else {
            return ApolloCarpetAdditionsSettings.playerBucketInteractionRange;
        }
    }
}
