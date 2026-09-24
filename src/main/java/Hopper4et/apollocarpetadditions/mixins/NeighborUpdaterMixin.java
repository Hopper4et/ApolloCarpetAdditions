package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsServer;
import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.CrashReport;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;



@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin {
    @Redirect(
            method = "executeUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/CrashReport;forThrowable(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/CrashReport;")
    )
    private static CrashReport tryNeighborUpdate(Throwable t, String title) {
        if (ApolloCarpetAdditionsSettings.blockUpdateSuppressionLagFix)
            return ApolloCarpetAdditionsServer.STATIC_BLOCK_UPDATE_SUPPRESSION_CRASH_REPORT;
        else
            return CrashReport.forThrowable(t, "Exception while updating neighbours");
    }
}