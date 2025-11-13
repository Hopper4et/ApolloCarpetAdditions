package Hopper4et.apollocarpetadditions;

import carpet.api.settings.Rule;

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

    //commands

    @Rule(categories = { MOD, COMMAND })
    public static String commandMacro = "ops";

    @Rule(categories = { MOD, COMMAND })
    public static String allowEditOtherPlayersMacros = "ops";

    //other

    @Rule(categories = { MOD, COMMAND, SURVIVAL })
    public static GamemodeOptions playerCommandNonOperatorSpawnInGamemode = GamemodeOptions.NONE;

}