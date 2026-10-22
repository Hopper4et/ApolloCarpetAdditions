package Hopper4et.apollocarpetadditions.commands.macro;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import carpet.utils.CommandHelper;
import carpet.utils.Messenger;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import org.jspecify.annotations.Nullable;


import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static Hopper4et.apollocarpetadditions.commands.macro.Macro.*;
import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MacroCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = literal("macro")
                .requires((player) -> CommandHelper.canUseCommand(player, ApolloCarpetAdditionsSettings.commandMacro))
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(MacroCommand::create)
                        )
                )
                .then(registerMacroSelector(
                        literal("delete"),
                        context -> delete(
                                context,
                                context.getSource().getTextName()
                        ),
                        context -> delete(
                                context,
                                MACRO_PUBLIC_OWNER
                        ),
                        context -> delete(
                                context,
                                StringArgumentType.getString(context, "owner")
                        )
                ))
                .then(registerMacroSelector(
                        literal("info"),
                        context -> info(
                                context,
                                context.getSource().getTextName()
                        ),
                        context -> info(
                                context,
                                MACRO_PUBLIC_OWNER
                        ),
                        context -> info(
                                context,
                                StringArgumentType.getString(context, "owner")
                        )
                ))
                .then(literal("list")
                        .then(literal("mine")
                                .executes(
                                        context -> list(
                                                context,
                                                context.getSource().getTextName()
                                        )
                                )
                        )
                        .then(literal("public")
                                .executes(
                                        context -> list(
                                                context,
                                                MACRO_PUBLIC_OWNER
                                        )
                                )
                        )
                        .then(literal("private")
                                .then(argument("owner", StringArgumentType.word())
                                        .suggests(MacroCommand::suggestOwner)
                                        .executes(
                                                context -> list(
                                                        context,
                                                        StringArgumentType.getString(context, "owner")
                                                )
                                        )
                                )
                        )
                )
                .then(literal("edit")
                        .then(literal("mine")
                                .then(editCommandTree())
                        )
                        .then(literal("public")
                                .then(editCommandTree())
                        )
                        .then(literal("private")
                                .then(argument("owner", StringArgumentType.word())
                                        .suggests(MacroCommand::suggestOwner)
                                        .then(editCommandTree())
                                )
                        )
                )
                .then(literal("run")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(MacroCommand::suggestMine)
                                .executes(
                                        context -> run(
                                                context,
                                                context.getSource().getTextName(),
                                                null
                                        )
                                )
                                .then(argument("variables", StringArgumentType.greedyString())
                                        .executes(
                                                context -> run(
                                                        context,
                                                        context.getSource().getTextName(),
                                                        StringArgumentType.getString(context, "variables")
                                                )
                                        )
                                )
                        )
                        .then(literal("public")
                                .then(argument("name", StringArgumentType.word())
                                        .suggests(MacroCommand::suggestPublic)
                                        .executes(
                                                context -> run(
                                                        context,
                                                        MACRO_PUBLIC_OWNER,
                                                        null
                                                )
                                        )
                                        .then(argument("variables", StringArgumentType.greedyString())
                                                .executes(
                                                        context -> run(
                                                                context,
                                                                MACRO_PUBLIC_OWNER,
                                                                StringArgumentType.getString(context, "variables")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("private")
                                .then(argument("owner", StringArgumentType.word())
                                        .suggests(MacroCommand::suggestOwner)
                                        .then(argument("name", StringArgumentType.word())
                                                .suggests(MacroCommand::suggestPrivate)
                                                .executes(
                                                        context -> run(
                                                                context,
                                                                StringArgumentType.getString(context, "owner"),
                                                                StringArgumentType.getString(context, "variables")
                                                        )
                                                )
                                                .then(argument("variables", StringArgumentType.greedyString())
                                                        .executes(
                                                                context -> run(
                                                                        context,
                                                                        StringArgumentType.getString(context, "owner"),
                                                                        StringArgumentType.getString(context, "variables")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("stop")
                        .executes(MacroCommand::stop)
                );

        dispatcher.register(command);
    }


    private static <T extends ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, ?> registerMacroSelector(
            T parent,
            Command<CommandSourceStack> mineCmd,
            Command<CommandSourceStack> publicCmd,
            Command<CommandSourceStack> privateCmd) {

         return parent.then(literal("mine")
                         .then(argument("name", StringArgumentType.word())
                                 .suggests(MacroCommand::suggestMine)
                                 .executes(mineCmd)
                         )
                 )
                 .then(literal("public")
                         .then(argument("name", StringArgumentType.word())
                                 .suggests(MacroCommand::suggestPublic)
                                 .executes(publicCmd)
                         )
                 )
                .then(literal("private")
                        .then(argument("owner", StringArgumentType.word())
                                .suggests(MacroCommand::suggestOwner)
                                .then(argument("name", StringArgumentType.word())
                                        .suggests(MacroCommand::suggestPrivate)
                                        .executes(privateCmd)
                                )
                        )
                );

    }

    private static ArgumentBuilder<CommandSourceStack, ?> editCommandTree() {
        return Commands.argument("name", StringArgumentType.word())
                .suggests(MacroCommand::suggestMacroForEditTree)
                .then(literal("add")
                        .then(argument("delay", TimeArgument.time())
                                .then(argument("command", StringArgumentType.greedyString())
                                        .executes(MacroCommand::editAdd)
                                )
                        )
                )
                .then(literal("replace")
                        .then(argument("command number", IntegerArgumentType.integer(1))
                                .suggests(MacroCommand::suggestCommandNumber)
                                .then(argument("delay", TimeArgument.time())
                                        .then(argument("command", StringArgumentType.greedyString())
                                                .executes(MacroCommand::editReplace)
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("command number", IntegerArgumentType.integer(1))
                                .suggests(MacroCommand::suggestCommandNumber)
                                .executes(MacroCommand::editRemove)
                        )
                )
                .then(literal("rename")
                        .then(argument("new name", StringArgumentType.word())
                                .executes(MacroCommand::editRename)
                        )
                )
                .then(literal("transfer")
                        .then(literal("public")
                                .executes(MacroCommand::editVisibilityPublic)
                        )
                        .then(argument("new owner", StringArgumentType.word())
                                .suggests(MacroCommand::suggestOwner)
                                .executes(MacroCommand::editVisibilityPrivate)
                        )
                );

    }

    private static CompletableFuture<Suggestions> suggestMacroForEditTree(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listMacros(getOwnerFromEditCommand(context)).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCommandNumber(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String macroName = StringArgumentType.getString(context, "name");
        String owner = getOwnerFromEditCommand(context);
        Macro macro = new Macro(macroName, owner, true);

        int numberOfCommands = macro.actions.size();
        for (int i = 1; i <= numberOfCommands; i++) builder.suggest(i);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestMine(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listMacros(context.getSource().getTextName()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPublic(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listMacros(MACRO_PUBLIC_OWNER).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPrivate(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listMacros(StringArgumentType.getString(context, "owner")).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestOwner(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Macro.listOwners().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int create(CommandContext<CommandSourceStack> context){
        String macroName = StringArgumentType.getString(context, "name").toLowerCase();
        CommandSourceStack source = context.getSource();

        if (Files.exists(macroPath(macroName, source.getTextName()))) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  is already exist"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, source.getTextName().toLowerCase(), false);
        macro.save();

        source.sendSuccess(() -> Messenger.c("w Macro ", "wu " + macroName, "w  created"), false);
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> context, String owner) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendFailure(Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"));
            return 0;
        }
        if(Macro.cantEdit(source, owner)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to delete this macro"), false);
            return 0;
        }

        try {
            Files.delete(macroPath(macroName, owner));
        } catch (IOException e) {
            LOGGER.error("Failed to delete macro", e);
            return 0;
        }

        source.sendSuccess(() -> Messenger.c("w Macro ", "wu " + macroName, "w  has been deleted"), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context, String owner) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        StringBuilder builder = new StringBuilder();
        for(Macro.MacroAction action : macro.actions) {
            builder.append("\nDelay: ").append(action.delay).append(" ticks, command: ").append(action.command);
        }
        String commands = builder.toString();

        if (macro.owner.equals(Macro.MACRO_PUBLIC_OWNER)) {
            source.sendSuccess(() -> Messenger.c(
                            "w Name: ", "wu " + macroName,
                            "w , Visibility: ", "wu Public",
                            "w , Commands: " + commands),
                    false
            );
        } else {
            source.sendSuccess(() -> Messenger.c(
                            "w Name: ", "wu " + macroName,
                            "w , Visibility: ", "wu Private",
                            "w , Owner: ", "wu " + macro.owner,
                            "w , Commands: " + commands),
                    false
            );
        }

        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context, String owner) {
        List<String> macroNames = Macro.listMacros(owner);
        context.getSource().sendSuccess(() -> Messenger.c("w List of macros: " + macroNames), false);
        return 1;
    }

    private static String getOwnerFromEditCommand(CommandContext<CommandSourceStack> context) {
        var nodes = context.getNodes();
        if (nodes.size() > 2) {
            String visibilityType = nodes.get(2).getNode().getName();
            switch (visibilityType) {
                case "mine":
                    return context.getSource().getTextName();
                case "public":
                    return Macro.MACRO_PUBLIC_OWNER;
                case "private":
                    return StringArgumentType.getString(context, "owner");
            }
        }
        throw new IllegalStateException("Unable to determine macro owner");
    }

    private static int editAdd(CommandContext<CommandSourceStack> context) {
        String macroName = StringArgumentType.getString(context, "name");
        int delay = IntegerArgumentType.getInteger(context, "delay");
        String command = StringArgumentType.getString(context, "command");
        CommandSourceStack source = context.getSource();
        String owner = getOwnerFromEditCommand(context);

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        if (Macro.cantEdit(source, owner)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        macro.actions.add(new Macro.MacroAction(delay, command));
        macro.save();
        source.sendSuccess(() -> Messenger.c("w Command was added to macro ", "wu " + macroName), false);
        return 1;
    }

    private static int editReplace(CommandContext<CommandSourceStack> context) {
        int commandNumber = IntegerArgumentType.getInteger(context, "command number");
        String macroName = StringArgumentType.getString(context, "name");
        int delay = IntegerArgumentType.getInteger(context, "delay");
        String command = StringArgumentType.getString(context, "command");
        CommandSourceStack source = context.getSource();
        String owner = getOwnerFromEditCommand(context);

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        if (Macro.cantEdit(source, owner)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        int numberOfCommands = macro.actions.size();
        if (numberOfCommands < commandNumber) {
            source.sendSuccess(() -> Messenger.c("r This macro only has " + numberOfCommands + " commands"), false);
            return 0;
        }

        macro.actions.set(commandNumber - 1, new Macro.MacroAction(delay, command));
        macro.save();
        source.sendSuccess(() -> Messenger.c("w Command was changed in macro ", "wu " + macroName), false);
        return 1;
    }

    private static int editRemove(CommandContext<CommandSourceStack> context) {
        int commandNumber = IntegerArgumentType.getInteger(context, "command number");
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();
        String owner = getOwnerFromEditCommand(context);

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        if (Macro.cantEdit(source, owner)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        int numberOfCommands = macro.actions.size();
        if (numberOfCommands < commandNumber) {
            source.sendSuccess(() -> Messenger.c("r This macro only has " + numberOfCommands + " commands"), false);
            return 0;
        }

        macro.actions.remove(commandNumber - 1);
        macro.save();
        source.sendSuccess(() -> Messenger.c("w Command was removed in macro ", "wu " + macroName), false);
        return 1;
    }

    private static int editVisibilityPublic(CommandContext<CommandSourceStack> context) {
        return editVisibility(context, MACRO_PUBLIC_OWNER);
    }

    private static int editVisibilityPrivate(CommandContext<CommandSourceStack> context) {
        return editVisibility(context, StringArgumentType.getString(context, "new owner").toLowerCase());
    }

    private static int editVisibility(CommandContext<CommandSourceStack> context, String newOwner) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();
        String owner = getOwnerFromEditCommand(context);

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        if (Macro.cantEdit(source, owner)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        if (!Files.exists(MACROS_DIR.resolve(newOwner))) {
            source.sendSuccess(() -> Messenger.c("r New owner does not exists"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        if (macro.owner.equals(newOwner)) {
            source.sendSuccess(() -> Messenger.c("r Macro already has this owner"), false);
            return 0;
        }

        if (Macro.doesMacroExist(macroName, newOwner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  is already exist"), false);
            return 0;
        }

        macro.owner = newOwner;
        macro.save();

        try {
            Files.delete(macroPath(macroName, owner));
        } catch (IOException e) {
            LOGGER.error("Failed to delete macro", e);
            return 0;
        }

        source.sendSuccess(() -> Messenger.c("w Macro owner is set to ", "wu " + newOwner), false);
        return 1;
    }

    private static int editRename(CommandContext<CommandSourceStack> context) {
        String macroName = StringArgumentType.getString(context, "name");
        String newMacroName = StringArgumentType.getString(context, "new name").toLowerCase();
        CommandSourceStack source = context.getSource();
        String owner = getOwnerFromEditCommand(context);

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        if (Macro.cantEdit(source, owner)) {
            source.sendSuccess(() -> Messenger.c("r You don't have permission to edit this macro"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        if (macro.name.equals(newMacroName)) {
            source.sendSuccess(() -> Messenger.c("r Macro already has this name"), false);
            return 0;
        }

        if (Macro.doesMacroExist(newMacroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + newMacroName, "r  is already exist"), false);
            return 0;
        }

        macro.name = newMacroName;
        macro.save();

        try {
            Files.delete(macroPath(macroName, owner));
        } catch (IOException e) {
            LOGGER.error("Failed to delete macro", e);
            return 0;
        }

        source.sendSuccess(() -> Messenger.c("w Macro name is set to ", "wu " + newMacroName), false);
        return 1;
    }

    private static int run(CommandContext<CommandSourceStack> context, String owner, @Nullable String variables) {
        String macroName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!Macro.doesMacroExist(macroName, owner)) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  does not exist"), false);
            return 0;
        }

        Macro macro = new Macro(macroName, owner, true);

        if (macro.actions.isEmpty()) {
            source.sendSuccess(() -> Messenger.c("r Macro ", "ru " + macroName, "r  has no commands"), false);
            return 0;
        }

        source.sendSuccess(() -> Messenger.c("w Macro ", "wu " + macroName, "w  launched"), true);
        MacroRunner.addMacro(macro, source, variables);
        return 1;
    }


    private static int stop(CommandContext<CommandSourceStack> context){
        return 1;
    }
}


