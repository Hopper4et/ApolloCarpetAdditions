package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.commands.PlayerCommand;
import carpet.utils.Messenger;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionProviderCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerCommand.class)
public abstract class CarpetPlayerCommandMixin {

    @Inject(method = "cantSpawn", at = @At("RETURN"), remap = false, cancellable = true)
    private static void cantSpawn(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Boolean> cir) {
        if (
                cir.getReturnValue() ||
                ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode == ApolloCarpetAdditionsSettings.GameModeOptions.NONE ||
                context.getSource().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
        ) {
            return;
        }

        GameType gameMode;

        try {
            gameMode = GameModeArgument.getGameMode(context, "gamemode") ;
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            return;
        }

        if (
                (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GameModeOptions.SURVIVAL ||
                        gameMode.getId() != 0) &&
                        (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GameModeOptions.SPECTATOR ||
                                gameMode.getId() != 3) &&
                        (ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GameModeOptions.SURVIVAL_SPECTATOR ||
                                (gameMode.getId() != 0 && gameMode.getId() != 3)) &&
                        ApolloCarpetAdditionsSettings.playerCommandNonOperatorSpawnInGamemode != ApolloCarpetAdditionsSettings.GameModeOptions.ALL
        ) {
            Messenger.m(context.getSource(), "r You don't have permission to spawn players in " + gameMode.getName());
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;hasPermission(Lnet/minecraft/server/permissions/PermissionCheck;)Lnet/minecraft/server/permissions/PermissionProviderCheck;"))
    private static PermissionProviderCheck<CommandSourceStack> a(PermissionCheck permission){
        return Commands.hasPermission(Commands.LEVEL_ALL);
    }

}
