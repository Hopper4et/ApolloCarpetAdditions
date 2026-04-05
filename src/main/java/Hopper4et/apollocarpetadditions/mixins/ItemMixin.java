package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class ItemMixin {
    @Redirect(
            method = "raycast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getBlockInteractionRange()D")
    )
    private static double changeInteractionRange(PlayerEntity player) {
        if(ApolloCarpetAdditionsSettings.playerBucketInteractionRange == -1){
            return player.getBlockInteractionRange();
        } else {
            return ApolloCarpetAdditionsSettings.playerBucketInteractionRange;
        }
    }
}
