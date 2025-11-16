package Hopper4et.apollocarpetadditions;

import Hopper4et.apollocarpetadditions.commands.MacroRunner;
import Hopper4et.apollocarpetadditions.commands.macro.MacroCommand;
import Hopper4et.apollocarpetadditions.rules.enderPearlNotLoadChunksFix.EnderPearlNotLoadChunksFix;
import Hopper4et.apollocarpetadditions.utils.TickTaskManager;
import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class ApolloCarpetAdditionsServer implements CarpetExtension, ModInitializer {

    @Override
    public void onInitialize() {

    }

    static {
        CarpetServer.manageExtension(new ApolloCarpetAdditionsServer());
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(ApolloCarpetAdditionsSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, final CommandRegistryAccess commandBuildContext) {
        MacroCommand.register(dispatcher);
    }

    @Override
    public void onTick(MinecraftServer server) {
        TickTaskManager.tick();
        MacroRunner.tick(server);
        EnderPearlNotLoadChunksFix.tick();
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        InputStream langFile = ApolloCarpetAdditionsServer.class.getClassLoader().getResourceAsStream("assets/apollocarpetadditions/lang/%s.json".formatted(lang));
        if (langFile == null) {
            // we don't have that language
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
