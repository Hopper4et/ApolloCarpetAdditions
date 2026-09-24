package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerEntityMixin {
    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"), cancellable = true
    )
    private void getBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (ApolloCarpetAdditionsSettings.instamineDeepslateWithNetheritePickaxe && block.getBlock().equals(Blocks.DEEPSLATE) && cir.getReturnValue() >= 49.0F) {
            cir.setReturnValue(90.0F);
        }
    }
}
