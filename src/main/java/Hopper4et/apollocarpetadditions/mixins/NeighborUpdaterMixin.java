package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsServer;
import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;



@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin {
    @Redirect(
            method = "tryNeighborUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;")
    )
    private static CrashReport tryNeighborUpdate(Throwable cause, String title) {
        if (ApolloCarpetAdditionsSettings.blockUpdateSuppressionLagFix)
            return ApolloCarpetAdditionsServer.STATIC_BLOCK_UPDATE_SUPPRESSION_CRASH_REPORT;
        else
            return CrashReport.create(cause, "Exception while updating neighbours");
    }
}