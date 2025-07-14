package Hopper4et.apollocarpetadditions;

import carpet.CarpetExtension;
import carpet.CarpetServer;


import carpet.api.settings.SettingsManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.ModInitializer;
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
    public void onGameStarted()
    {
        CarpetServer.settingsManager.parseSettingsClass(ApolloCarpetAdditionsSettings.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang)
    {
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
