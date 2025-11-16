package Hopper4et.apollocarpetadditions;

import Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix.EnderPearlNotLoadChunksFix;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import static carpet.api.settings.RuleCategory.*;

public class ApolloCarpetAdditionsSettings {

    private static final String MOD = "apollo";
    private static final String SPECTATOR = "spectator";

    public enum GamemodeOptions {
        NONE, SURVIVAL, SPECTATOR, SURVIVAL_SPECTATOR, ALL
    }

    //portals

    @Rule(categories = { MOD, SURVIVAL })
    public static boolean endGatewaysLoadChunks = true;

    @Rule(categories = { MOD, CREATIVE, SPECTATOR })
    public static boolean spectatorCanUsePortals = false;

    //bug fixes

    @Rule(categories = { MOD, BUGFIX })
    public static boolean pigCannonUnstuck = false;

    @Rule(categories = { MOD, BUGFIX })
    public static boolean lazyLinkingPre1_21Render = false;

    @Rule(categories = { MOD, BUGFIX, SPECTATOR })
    public static boolean portalNoClipFix = false;

    @Rule(categories = { MOD, BUGFIX, SURVIVAL }, validators = EnderPearlChunkLoadingFixValidator.class)
    public static boolean enderPearlNotLoadChunksFix = false;

    //other rules

    @Rule(categories = { MOD, SURVIVAL })
    public static boolean instamineDeepslateWithNetheritePickaxe = false;

    //command rules

    @Rule(categories = { MOD, COMMAND, SURVIVAL })
    public static GamemodeOptions playerCommandNonOperatorSpawnInGamemode = GamemodeOptions.NONE;

    @Rule(categories = { MOD, COMMAND })
    public static String allowEditOtherPlayersMacros = "ops";

    //commands

    @Rule(categories = { MOD, COMMAND })
    public static String commandMacro = "ops";

    private static class EnderPearlChunkLoadingFixValidator extends Validator<Boolean> {
        @Override
        public Boolean validate(@Nullable ServerCommandSource source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
            if (!newValue) EnderPearlNotLoadChunksFix.removeAllFastEnderPearls();
            return newValue;
        }
    }
}