package Hopper4et.apollocarpetadditions.models;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.utils.CommandHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static Hopper4et.apollocarpetadditions.commands.macro.MacroCommand.MACROS_DIR;
import static com.mojang.text2speech.Narrator.LOGGER;

public record Macro(String name, UUID ownerUUID, String ownerNickName, List<DelayCommand> commands) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Macro(String name, @Nullable UUID ownerUUID, @Nullable String ownerNickName, @Nullable List<DelayCommand> commands) {
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.ownerNickName = ownerNickName;
        this.commands = commands;
    }

    public Macro(String name, List<DelayCommand> commands) {
        this(name, null, null, commands);
    }

    public boolean isOwner(ServerCommandSource source) {
        return source.isExecutedByPlayer() && Objects.requireNonNull(source.getPlayer()).getUuid().equals(ownerUUID);
    }

    public boolean canEdit(ServerCommandSource source) {
        return CommandHelper.canUseCommand(source, ApolloCarpetAdditionsSettings.allowEditOtherPlayersMacros)
                || (source.isExecutedByPlayer() && Objects.requireNonNull(source.getPlayer()).getUuid().equals(ownerUUID));
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(macroPathByName(this.name))) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save macro", e);
        }
    }

    public static Macro getFromFile(String name) {
        Path path = macroPathByName(name);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                return GSON.fromJson(reader, Macro.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load macro: ", e);
            }
        }
        return null;
    }

    public static List<String> listMacros() {
        List<String> macroNames = new ArrayList<>();
        try {
            if (!Files.exists(MACROS_DIR)) {
                Files.createDirectories(MACROS_DIR);
            }
            try (Stream<Path> folderLister = Files.list(MACROS_DIR)) {
                folderLister
                        .filter(f -> f.toString().endsWith(".json"))
                        .forEach(f -> macroNames.add(f.getFileName().toString().replaceFirst("\\.json$", "").toLowerCase(Locale.ROOT)));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to get macros list: ", e);
        }
        return macroNames;
    }

    public static List<String> listOwnerMacros(ServerCommandSource source) {
        return listMacros().stream().filter(name -> {
            Macro macro = getFromFile(name);
            return macro != null && macro.isOwner(source);
        }).toList();
    }

    public static List<String> listCanEditMacros(ServerCommandSource source) {
        return listMacros().stream().filter(name -> {
            Macro macro = getFromFile(name);
            return macro != null && macro.canEdit(source);
        }).toList();
    }

    public static Path macroPathByName(String name) {
        return MACROS_DIR.resolve(name + ".json");
    }

}
