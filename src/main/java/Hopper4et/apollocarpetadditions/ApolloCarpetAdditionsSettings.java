package Hopper4et.apollocarpetadditions;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;

public class ApolloCarpetAdditionsSettings {

    private static final String MOD = "apollo";

    public enum GamemodeOptions {
        NONE, SURVIVAL, SPECTATOR, SURVIVAL_SPECTATOR, ALL
    }

    //bug fixes

    @Rule(categories = { MOD, BUGFIX })
    public static boolean pigCannonUnstuck = false;

    @Rule(categories = { MOD, BUGFIX })
    public static boolean lazyLinkingPre1_21Render = false;

    @Rule(categories = { MOD, OPTIMIZATION })
    public static boolean blockUpdateSuppressionLagFix = false;

    @Rule(categories = { MOD, BUGFIX, EXPERIMENTAL })
    public static boolean fakePlayerChunkLoadingPriority = false;

    //other rules

    @Rule(categories = { MOD, SURVIVAL })
    public static boolean instamineDeepslateWithNetheritePickaxe = false;

    @Rule(categories = { MOD, SURVIVAL })
    public static boolean endGatewaysLoadChunks = true;

    //command rules

    @Rule(categories = { MOD, COMMAND, SURVIVAL })
    public static GamemodeOptions playerCommandNonOperatorSpawnInGamemode = GamemodeOptions.NONE;

    @Rule(categories = { MOD, COMMAND })
    public static String allowEditOtherPlayersMacros = "ops";

    //commands

    @Rule(categories = { MOD, COMMAND })
    public static String commandMacro = "ops";
}