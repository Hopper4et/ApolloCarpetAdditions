package Hopper4et.apollocarpetadditions;

import Hopper4et.apollocarpetadditions.commands.MacroRunner;
import Hopper4et.apollocarpetadditions.commands.ClientCommandsServer;
import Hopper4et.apollocarpetadditions.commands.macro.MacroCommand;
import Hopper4et.apollocarpetadditions.utils.TickTaskManager;
import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.CrashReport;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class ApolloCarpetAdditionsServer implements CarpetExtension, ModInitializer {

    @Override
    public void onInitialize() {
        ClientCommandsServer.initialize();
    }
    public static final CrashReport STATIC_BLOCK_UPDATE_SUPPRESSION_CRASH_REPORT = CrashReport.forThrowable(
            new RuntimeException("Neighbor update failed"), "Exception while updating neighbours"
    );

    static {
        CarpetServer.manageExtension(new ApolloCarpetAdditionsServer());
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(ApolloCarpetAdditionsSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        MacroCommand.register(dispatcher);
    }

    @Override
    public void onTick(MinecraftServer server) {
        TickTaskManager.tick();
        MacroRunner.tick(server);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        InputStream langFile = ApolloCarpetAdditionsServer.class.getClassLoader().getResourceAsStream("assets/apollocarpetadditions/lang/%s.json".formatted(lang));
        if (langFile == null) {
            return Collections.emptyMap();
        }
        String jsonData;
        try {
            jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonData, new TypeToken<Map<String, String>>() {}.getType());
    }
}
