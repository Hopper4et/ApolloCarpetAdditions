package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.commands.PlayerCommand;
import carpet.utils.Messenger;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerCommand.class)
public abstract class CarpetPlayerCommandMixin {

    @Inject(method = "cantSpawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void cantSpawn(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Boolean> cir) {
        GameMode gameMode;
        try {
            gameMode = GameModeArgumentType.getGameMode(context, "gamemode") ;
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            return;
        }
        if (
                (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodesOptions.SURVIVAL ||
                        gameMode.getId() != 0) &&
                        (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodesOptions.SPECTATOR ||
                                gameMode.getId() != 3) &&
                        (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodesOptions.SURVIVAL_SPECTATOR ||
                                (gameMode.getId() != 0 && gameMode.getId() != 3)) &&
                        ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GamemodesOptions.ALL) {
            Messenger.m(context.getSource(), "r You don't have permission to spawn players in " + gameMode.getName());
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(method = "lambda$register$30", index = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
    private static int hasPermissionLevel1(int level){
        return ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode ==
                ApolloCarpetAdditionsSettings.GamemodesOptions.NONE ? 2 : 0;
    }

    @ModifyArg(method = "lambda$register$29", index = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
    private static int hasPermissionLevel2(int level){
        return ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode ==
                ApolloCarpetAdditionsSettings.GamemodesOptions.NONE ? 2 : 0;
    }

}
