package Hopper4et.apollocarpetadditions.commands.macro;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.utils.CommandHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Macro {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path MACROS_DIR = FabricLoader.getInstance().getConfigDir().resolve("ApolloCarpetAdditions/macros");
    private static final Type MACRO_LIST_TYPE = new TypeToken<List<MacroAction>>() {}.getType();
    public static final String MACRO_PUBLIC_OWNER = "public";

    public String name;
    public String owner;
    public List<MacroAction> actions = new ArrayList<>();

    public Macro(String name, String owner, boolean getActions) {
        this.name = name;
        this.owner = owner;
        if (getActions) this.getActionsFromFile();
    }

    public static class MacroAction {
        public int delay;
        public String command;

        public MacroAction(int delay, String command) {
            this.delay = delay;
            this.command = command;
        }
    }

    public static Path macroPath(String name, String owner) {
        return MACROS_DIR.resolve(owner).resolve(name + ".json");
    }

    public void save() {
        Path path = macroPath(this.name, this.owner);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this.actions, MACRO_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save macro", e);
        }
    }

    public void getActionsFromFile() {
        Path path = macroPath(this.name, this.owner);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                this.actions = GSON.fromJson(reader, MACRO_LIST_TYPE);
            } catch (IOException e) {
                LOGGER.error("Failed to load macro: ", e);
            }
        }
    }

    public static boolean doesMacroExist(String name, String owner) {
        Path path = macroPath(name, owner);
        return Files.exists(path);
    }

    public static boolean cantEdit(CommandSourceStack source, String macroOwner)  {
        return !CommandHelper.canUseCommand(source, ApolloCarpetAdditionsSettings.allowEditOtherPlayersMacros)
                && (!source.getTextName().equals(macroOwner));
    }

    public static List<String> listMacros(String owner) {
        List<String> macroNames = new ArrayList<>();
        Path path = MACROS_DIR.resolve(owner);
        if (Files.exists(path)) {
            try (Stream<Path> folderLister = Files.list(path)) {
                folderLister
                        .filter(f -> f.toString().endsWith(".json"))
                        .forEach(f -> macroNames.add(f.getFileName().toString().replaceFirst("\\.json$", "").toLowerCase(Locale.ROOT)));
            } catch (IOException e) {
                LOGGER.error("Failed to get macros list: ", e);
            }
        }
        return macroNames;
    }

    public static List<String> listOwners() {
        if (Files.exists(MACROS_DIR)) {
            try (Stream<Path> stream = Files.list(MACROS_DIR)) {
                return stream
                        .filter(Files::isDirectory)
                        .map(p -> p.getFileName().toString())
                        .filter(name -> !name.equals(MACRO_PUBLIC_OWNER))
                        .toList();
            } catch (IOException e) {
                LOGGER.error("Failed to get macros list: ", e);
            }
        }
        return new ArrayList<>();
    }
}
