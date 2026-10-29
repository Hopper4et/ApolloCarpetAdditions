package Hopper4et.apollocarpetadditions;

import Hopper4et.apollocarpetadditions.mixins.TimeArgumentAccessor;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import static carpet.api.settings.RuleCategory.*;

public class ApolloCarpetAdditionsSettings {

    private static final String MOD = "apollo";

    public enum GameModeOptions {
        NONE, SURVIVAL, SPECTATOR, SURVIVAL_SPECTATOR, ALL
    }

    public enum SpectatorGameModeChangeOptions {
        FALSE, BOTH, TO, FROM
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

    @Rule(categories = { MOD, COMMAND })
    public static SpectatorGameModeChangeOptions spawnParticlesWhenSpectatorGameModeChanged = SpectatorGameModeChangeOptions.FALSE;

    //command rules

    @Rule(categories = { MOD, COMMAND, SURVIVAL })
    public static GameModeOptions playerCommandNonOperatorSpawnInGamemode = GameModeOptions.NONE;

    @Rule(categories = { MOD, COMMAND })
    public static String allowEditOtherPlayersMacros = "ops";

    @Rule(categories = { MOD, COMMAND }, validators = newTimeArgumentValuesValidator.class)
    public static boolean newTimeArgumentValues = false;

    //commands

    @Rule(categories = { MOD, COMMAND })
    public static String commandMacro = "ops";

    private static class newTimeArgumentValuesValidator extends Validator<Boolean> {

        @Override
        public Boolean validate(@Nullable CommandSourceStack commandSourceStack, CarpetRule<Boolean> carpetRule, Boolean newValue, String s) {
            if (newValue) {
                TimeArgumentAccessor.getTimeArgumentUnits().put("m", 1200);
                TimeArgumentAccessor.getTimeArgumentUnits().put("h", 72000);
            } else {
                TimeArgumentAccessor.getTimeArgumentUnits().remove("m", 1200);
                TimeArgumentAccessor.getTimeArgumentUnits().remove("h", 72000);
            }
            return newValue;
        }
    }
}
