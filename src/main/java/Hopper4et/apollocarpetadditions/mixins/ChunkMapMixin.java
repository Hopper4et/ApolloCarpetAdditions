package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.rules.fakePlayerChunkLoadingPriority.FakePlayerChunkLoaderHolder;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
	@Shadow
	public abstract DistanceManager getDistanceManager();

	// Both the worldgen and the light queue take their priority from here
	@ModifyVariable(method = "onLevelChange", at = @At("HEAD"), argsOnly = true)
	private int apolloCarpetAdditions$fakePlayerQueuePriority(int newLevel, @Local(argsOnly = true) ChunkPos pos) {
		return ((FakePlayerChunkLoaderHolder) this.getDistanceManager()).apolloCarpetAdditions$getFakePlayerChunkLoader().queuePriority(pos.pack(), newLevel);
	}
}
