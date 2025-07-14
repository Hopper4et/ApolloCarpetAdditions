package Hopper4et.apollocarpetadditions;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;

public class ApolloCarpetAdditionsSettings {
    private static final String MOD = "apollo";

    public enum GamemodesOptions {
        NONE, SURVIVAL, SPECTATOR, SURVIVAL_SPECTATOR, ALL;
    }

    @Rule(categories = { MOD, CREATIVE })
    public static boolean spectatorCanUsePortals = false;

    @Rule(categories = { MOD, SURVIVAL })
    public static boolean endGatewaysLoadChunks = true;

    @Rule(categories = { MOD, BUGFIX })
    public static boolean pigCannonUnstuck = false;

    @Rule(categories = {MOD, COMMAND, SURVIVAL})
    public static GamemodesOptions playerCommandNonOperatorSpawnInGamemode = GamemodesOptions.NONE;

    @Rule(categories = {MOD, BUGFIX})
    public static boolean lazyLinkingPre1_21Render = false;

}