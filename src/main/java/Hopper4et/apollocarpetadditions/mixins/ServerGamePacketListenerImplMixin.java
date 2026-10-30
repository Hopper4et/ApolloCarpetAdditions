package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Inject(method = "forceSendPlayerSupportBlocks", at = @At("HEAD"),cancellable = true)
    // silence Logger, A polloTech is talking
    private void silence(CallbackInfo ci){
        if (ApolloCarpetAdditionsSettings.StopSendPlayerSupportBlocks) {
            ci.cancel();
        }
    }
}
