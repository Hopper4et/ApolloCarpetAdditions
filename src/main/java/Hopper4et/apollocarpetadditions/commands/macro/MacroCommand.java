package Hopper4et.apollocarpetadditions.commands.macro;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import Hopper4et.apollocarpetadditions.commands.MacroRunner;
import Hopper4et.apollocarpetadditions.models.DelayCommand;
import Hopper4et.apollocarpetadditions.models.Macro;
import carpet.utils.CommandHelper;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TimeArgument;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static Hopper4et.apollocarpetadditions.models.Macro.macroPathByName;
import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MacroCommand {

    public static final Path MACROS_DIR = FabricLoader.getInstance().getConfigDir().resolve("ApolloCarpetAdditions/macros");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = literal("macro")
                .requires((player) -> CommandHelper.canUseCommand(player, ApolloCarpetAdditionsSettings.commandMacro))
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(MacroCommand::create)))
                .then(literal("delete")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(MacroCommand::suggestCanEditMacros)
                                .executes(MacroCommand::delete)))
                .then(literal("info")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(MacroCommand::suggestAllMacros)
                                .executes(MacroCommand::info)))
                .then(literal("list")
                        .executes(MacroCommand::list))
                .then(literal("edit")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(MacroCommand::suggestCanEditMacros)
                                .then(literal("add")
                                        .then(argument("delay", TimeArgument.time())
                                                .then(argument("command", StringArgumentType.greedyString())
                                                        .executes(MacroCommand::add))))
                                .then(literal("replace")
                                        .then(argument("command number", IntegerArgumentType.integer(1))
                                                .suggests(MacroCommand::suggestCommandNumber)
                                                .then(argument("delay", TimeArgument.time())
                                                        .then(argument("command", StringArgumentType.greedyString())
                                                                .executes(MacroCommand::replace)))))
                                .then(literal("remove")
                                        .then(argument("command number", IntegerArgumentType.integer(1))
                                                .suggests(MacroCommand::suggestCommandNumber)
                                                .executes(MacroCommand::remove)))))
                .then(literal("run")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(MacroCommand::suggestAllMacros)
                                .executes(MacroCommand::runWithoutVariables)
                                .then(argument("variables", StringArgumentType.greedyString())
                                        .executes(MacroCommand::runWithVariables))))
                .then(literal("myrun")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(MacroCommand::suggestOwnerMacros)
                                .executes(MacroCommand::runWithoutVariables)
                                .then(argument("variables", StringArgumentType.greedyString())
                                        .executes(MacroCommand::runWithVariables))))
                .then(literal("stop")
                        .then(argument("nickname", StringArgumentType.word())
                                .suggests(MacroCommand::suggestRunningMacros)
                                .executes(MacroCommand::stop)));
        dispatcher.register(command);
    }

    private static CompletableFuture<Suggestions> suggestCommandNumber(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String macroName = StringArgumentType.getString(context, "name");
        Macro macro = Macro.getFromFile(macroName);
        if (macro != null) {
            int numberOfCommands = macro.commands().size();
            for (int i = 1; i <= numberOfCommands; i++) builder.suggest(i);
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestAllMacros(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listMacros().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestOwnerMacros(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listOwnerMacros(context.getSource()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCanEditMacros(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listCanEditMacros(context.getSource()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestRunningMacros(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MacroRunner.getMacroList().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int create(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (Files.exists(macroPathByName(name))) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + name, "r  is already exist"), false);
            return 0;
        }

        Macro macro;
        if (source.isPlayer()) {
            macro = new Macro(name, Objects.requireNonNull(source.getPlayer()).getUUID(), source.getPlayer().getName().getString(), new ArrayList<>());
        } else {
            macro = new Macro(name, new ArrayList<>());
        }
        macro.save();

        source.sendSuccess(() -> Messenger.c("w Macro ", "wu " + name, "w  created"), false);
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> context) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        Macro macro = Macro.getFromFile(macroName);
        if (macro == null) {
            source.sendFailure(Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"));
            return 0;
        }
        if(!macro.canEdit(source)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to delete this macro"), false);
            return 0;
        }

        try {
            Files.delete(macroPathByName(macroName));
        } catch (IOException e) {
            LOGGER.error("Failed to delete macro", e);
            return 0;
        }

        source.sendSuccess(() -> Messenger.c("w Macro ", "wu " + macroName, "w  has been deleted"), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();
        Macro macro = Macro.getFromFile(macroName);
        if (macro == null) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        String ownerNickName = Objects.requireNonNullElse(macro.ownerNickName(), "Server");

        StringBuilder builder = new StringBuilder();
        for(DelayCommand s : macro.commands()) {
            builder.append("\nDelay: ").append(s.delay()).append(" ticks, command: ").append(s.command());
        }
        String commands = builder.toString();

        source.sendSuccess(() -> Messenger.c("w Name: ", "wu " + macroName, "w , Owner: ", "wu " + ownerNickName, "w , Commands: " + commands), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<String> ownerMacros = Macro.listOwnerMacros(source);
        List<String> otherMacros = Macro.listMacros();
        otherMacros.removeAll(ownerMacros);
        source.sendSuccess(() -> Messenger.c("w List of your macros: " + ownerMacros), false);
        source.sendSuccess(() -> Messenger.c("w List of other macros: " + otherMacros), false);
        return 1;
    }

    private static int add (CommandContext<CommandSourceStack> context) {
        String macroName = StringArgumentType.getString(context, "name");
        int delay = IntegerArgumentType.getInteger(context, "delay");
        String command = StringArgumentType.getString(context, "command");
        CommandSourceStack source = context.getSource();

        Macro macro = Macro.getFromFile(macroName);
        if (macro == null) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }
        if (!macro.canEdit(source)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        macro.commands().add(new DelayCommand(delay, command));
        macro.save();
        source.sendSuccess(() -> Messenger.c("w Command was added to macro ", "wu " + macroName), false);
        return 1;
    }

    private static int replace (CommandContext<CommandSourceStack> context) {
        int commandNumber = IntegerArgumentType.getInteger(context, "command number");
        String macroName = StringArgumentType.getString(context, "name");
        int delay = IntegerArgumentType.getInteger(context, "delay");
        String command = StringArgumentType.getString(context, "command");
        CommandSourceStack source = context.getSource();

        Macro macro = Macro.getFromFile(macroName);
        if (macro == null) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }
        if (!macro.canEdit(source)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        int numberOfCommands = macro.commands().size();
        if (numberOfCommands < commandNumber) {
            source.sendSuccess(() -> Messenger.c("r This macro only has " + numberOfCommands + " commands"), false);
            return 0;
        }

        macro.commands().set(commandNumber - 1, new DelayCommand(delay, command));
        macro.save();
        source.sendSuccess(() -> Messenger.c("w Command was changed in macro ", "wu " + macroName), false);
        return 1;
    }

    private static int remove (CommandContext<CommandSourceStack> context) {
        int commandNumber = IntegerArgumentType.getInteger(context, "command number");
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        Macro macro = Macro.getFromFile(macroName);
        if (macro == null) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }
        if (!macro.canEdit(source)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        int numberOfCommands = macro.commands().size();
        if (numberOfCommands < commandNumber) {
            source.sendSuccess(() -> Messenger.c("r This macro only has " + numberOfCommands + " commands"), false);
            return 0;
        }

        macro.commands().remove(commandNumber - 1);
        macro.save();
        source.sendSuccess(() -> Messenger.c("w Command was removed in macro ", "wu " + macroName), false);
        return 1;
    }

    private static int runWithoutVariables(CommandContext<CommandSourceStack> context){
        return run(context, null);
    }

    private static int runWithVariables(CommandContext<CommandSourceStack> context){
        String variables = StringArgumentType.getString(context, "variables");
        if(variables.isEmpty()){
            return run(context, null);
        }else return run(context, variables);
    }

    private static int run (CommandContext<CommandSourceStack> context, @Nullable String variables) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        Macro macro = Macro.getFromFile(macroName);
        if (macro == null) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }
        if (macro.commands().isEmpty()) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  has no commands"), false);
            return 0;
        }

        source.sendSuccess(() -> Messenger.c("w Macro ", "wu " + macroName, "w  launched"), true);
        MacroRunner.addMacro(macro, source, variables);

        return 1;
    }

    private static int stop (CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "nickname");
        CommandSourceStack source = context.getSource();
        if (!CommandHelper.canUseCommand(source, ApolloCarpetAdditionsSettings.allowEditOtherPlayersMacros) && !Objects.equals(source.getTextName(), name)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to stop this macro"), false);
            return 0;
        }

        for (String runningMacro : MacroRunner.getMacroList()) {
            if (Objects.equals(runningMacro, name)) {
                source.sendSuccess(() -> Messenger.c("r This macro stopped"), false);
                MacroRunner.removeMacro(name);
                return 1;
            }
        }

        source.sendSuccess(() -> Messenger.c("r This macro is not running"), false);
        return 0;
    }
}


