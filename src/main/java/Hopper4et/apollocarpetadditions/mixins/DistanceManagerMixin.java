package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.rules.fakePlayerChunkLoadingPriority.FakePlayerChunkLoader;
import Hopper4et.apollocarpetadditions.rules.fakePlayerChunkLoadingPriority.FakePlayerChunkLoaderHolder;
import Hopper4et.apollocarpetadditions.utils.FakePlayers;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements FakePlayerChunkLoaderHolder {
	@Unique
	private FakePlayerChunkLoader apollochunks$fakePlayerChunkLoader;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void apolloCarpetAdditions$init(TicketStorage ticketStorage, Executor executor, Executor mainThreadExecutor, CallbackInfo ci) {
		this.apollochunks$fakePlayerChunkLoader = new FakePlayerChunkLoader(ticketStorage);
	}

	@Override
	public FakePlayerChunkLoader apolloCarpetAdditions$getFakePlayerChunkLoader() {
		return this.apollochunks$fakePlayerChunkLoader;
	}

	// Only players that load chunks get here: ChunkMap skips spectators and, with Carpet, creative players by its rules
	@Inject(method = "addPlayer", at = @At("TAIL"))
	private void apolloCarpetAdditions$addFakePlayer(SectionPos pos, ServerPlayer player, CallbackInfo ci) {
		if (FakePlayers.isFake(player)) {
			this.apollochunks$fakePlayerChunkLoader.addFakePlayer(pos.chunk().pack());
		}
	}

	@Inject(method = "removePlayer", at = @At("TAIL"))
	private void apolloCarpetAdditions$removeFakePlayer(SectionPos pos, ServerPlayer player, CallbackInfo ci) {
		if (FakePlayers.isFake(player)) {
			this.apollochunks$fakePlayerChunkLoader.removeFakePlayer(pos.chunk().pack());
		}
	}

	@Inject(method = "updatePlayerTickets", at = @At("TAIL"))
	private void apolloCarpetAdditions$updateViewDistance(int viewDistance, CallbackInfo ci) {
		this.apollochunks$fakePlayerChunkLoader.setViewDistance(viewDistance);
	}

	// Before vanilla propagates ticket levels, so bot tickets placed here load in the same pass
	@Inject(method = "runAllUpdates", at = @At("HEAD"))
	private void apolloCarpetAdditions$runFakePlayerUpdates(ChunkMap scheduler, CallbackInfoReturnable<Boolean> cir) {
		this.apollochunks$fakePlayerChunkLoader.runUpdates(scheduler);
	}
}
