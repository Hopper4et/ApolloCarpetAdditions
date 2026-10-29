package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Inject(method = "setGameModeForPlayer", at = @At("TAIL"))
    private void onGameModeChanged(GameType gameModeForPlayer, GameType previousGameModeForPlayer, CallbackInfo ci) {
        if (gameModeForPlayer == null || gameModeForPlayer == previousGameModeForPlayer) return;

        boolean from = ApolloCarpetAdditionsSettings.spawnParticlesWhenSpectatorGameModeChanged == ApolloCarpetAdditionsSettings.SpectatorGameModeChangeOptions.FROM;
        boolean to = ApolloCarpetAdditionsSettings.spawnParticlesWhenSpectatorGameModeChanged == ApolloCarpetAdditionsSettings.SpectatorGameModeChangeOptions.TO;
        boolean both = ApolloCarpetAdditionsSettings.spawnParticlesWhenSpectatorGameModeChanged == ApolloCarpetAdditionsSettings.SpectatorGameModeChangeOptions.BOTH;
        if (
                ((from || both) && previousGameModeForPlayer == GameType.SPECTATOR)
                        || ((to || both) && gameModeForPlayer == GameType.SPECTATOR)
        ) {
            level.sendParticles(
                    ParticleTypes.SPIT,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    50,
                    0.2, 0.5, 0.2,
                    0.0
            );
        }
    }
}
