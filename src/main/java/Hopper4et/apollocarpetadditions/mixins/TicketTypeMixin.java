package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.rules.fakePlayerChunkLoadingPriority.FakePlayerTicket;
import net.minecraft.server.level.TicketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TicketType.class)
public abstract class TicketTypeMixin {
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void apolloCarpetAdditions$registerFakePlayerTicket(CallbackInfo ci) {
		FakePlayerTicket.register();
	}
}
