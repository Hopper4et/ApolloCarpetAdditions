package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.commands.PlayerCommand;
import carpet.utils.Messenger;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerCommand.class)
public abstract class CarpetPlayerCommandMixin {

    @Inject(method = "cantSpawn", at = @At("RETURN"), remap = false, cancellable = true)
    private static void cantSpawn(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Boolean> cir) {
        if (
                cir.getReturnValue() ||
                ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode == ApolloCarpetAdditionsSettings.GamemodeOptions.NONE ||
                context.getSource().hasPermissionLevel(2)
        ) {
            return;
        }

        GameMode gameMode;

        try {
            gameMode = GameModeArgumentType.getGameMode(context, "gamemode") ;
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            return;
        }

        if (
                (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodeOptions.SURVIVAL ||
                        gameMode.getId() != 0) &&
                        (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodeOptions.SPECTATOR ||
                                gameMode.getId() != 3) &&
                        (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodeOptions.SURVIVAL_SPECTATOR ||
                                (gameMode.getId() != 0 && gameMode.getId() != 3)) &&
                        ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodeOptions.ALL
        ) {
            Messenger.m(context.getSource(), "r You don't have permission to spawn players in " + gameMode.getName());
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(method = "lambda$register$30", index = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
    private static int hasPermissionLevel1(int level){
        return ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode ==
                ApolloCarpetAdditionsSettings.GamemodeOptions.NONE ? 2 : 0;
    }

    @ModifyArg(method = "lambda$register$29", index = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
    private static int hasPermissionLevel2(int level){
        return ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode ==
                ApolloCarpetAdditionsSettings.GamemodeOptions.NONE ? 2 : 0;
    }

}
