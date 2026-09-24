package Hopper4et.apollocarpetadditions.commands;

import Hopper4et.apollocarpetadditions.models.DelayCommand;
import Hopper4et.apollocarpetadditions.models.Macro;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroRunner {

    private static final List<MacroTask> tasks = new ArrayList<>();

    private static class MacroTask {
        final List<DelayCommand> delayCommandList;
        final CommandSourceStack source;
        int index = 0, tickCounter;
        String variables;

        private MacroTask(List<DelayCommand> delayCommandList, CommandSourceStack source, String variables) {
            this.delayCommandList = delayCommandList;
            this.source = source;
            this.variables = variables;
            this.tickCounter = delayCommandList.getFirst().delay();
        }
    }

    public static void addMacro(Macro macro, CommandSourceStack source, @Nullable String variables) {
        String sourceName = source.getTextName();
        tasks.removeIf(task -> Objects.equals(task.source.getTextName(), sourceName));
        MacroTask task = new MacroTask(macro.commands(), source, variables);
        if (task.tickCounter == 0) runCommand(task, source.getServer());
        if (task.index < task.delayCommandList.size()) tasks.add(task);
    }

    public static void removeMacro(String name) {
        tasks.removeIf(task -> Objects.equals(task.source.getTextName(), name));
    }

    public static List<String> getMacroList() {
        List<String> list = new ArrayList<>();
        for (MacroTask task : tasks) {
            list.add(task.source.getTextName());
        }
        return list;
    }

    public static void tick(MinecraftServer server) {
        List<MacroTask> tasksCopy = new ArrayList<>(tasks);
        for (MacroTask task : tasksCopy) {
            if (task.tickCounter <= 0) runCommand(task, server);
            task.tickCounter--;
        }
    }

    private static void runCommand(MacroTask task, MinecraftServer server) {
        //stops if player log out
        CommandSourceStack source = task.source;
        if (source.isPlayer() && source.getPlayer() != null && server.getPlayerList().getPlayer(source.getPlayer().getUUID()) == null) {
            tasks.remove(task);
            return;
        }
        //run command
        server.getCommands().performPrefixedCommand(task.source, parseCommand(task.delayCommandList.get(task.index).command(), task.variables));

        task.index++;
        if (task.index >= task.delayCommandList.size()) {
            tasks.remove(task);
            return;
        }
        task.tickCounter = task.delayCommandList.get(task.index).delay();
        if (task.tickCounter == 0) runCommand(task, server);
    }

    private static String parseCommand(String command, String variables){
        //player Alex spawn at 10 10 10  $Player=Alex $x=10 $y=10 $z=10
        //player $Player spawn at $x $y $z

        if (variables == null || variables.trim().isEmpty()) {
            return command;
        }
        String result = command;
        //жесть воще регех
        Pattern pattern = Pattern.compile("(\\$[a-zA-Z0-9_]+)=(.*?)(?=\\s+\\$[a-zA-Z0-9_]+=|$)");
        Matcher matcher = pattern.matcher(variables);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();
            result = result.replace(key, value);
        }

        return result;
    }
}
