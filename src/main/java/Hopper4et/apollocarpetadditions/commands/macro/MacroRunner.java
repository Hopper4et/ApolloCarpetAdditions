package Hopper4et.apollocarpetadditions.commands.macro;

import Hopper4et.apollocarpetadditions.commands.macro.Macro.MacroAction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroRunner {

    private static final List<MacroTask> tasks = new ArrayList<>();

    private static class MacroTask {
        final List<MacroAction> macroActionList;
        CommandSourceStack source;
        int index = 0, tickCounter;
        String variables;

        private MacroTask(List<MacroAction> macroActionList, CommandSourceStack source, String variables) {
            this.macroActionList = macroActionList;
            this.source = source;
            this.variables = variables;
            this.tickCounter = macroActionList.getFirst().delay;
        }
    }

    public static void addMacro(Macro macro, CommandSourceStack source, @Nullable String variables) {
        removeMacro(source);
        MacroTask task = new MacroTask(macro.actions, source, variables);
        if (task.tickCounter == 0) {
            runCommand(task, source.getServer());
        }
        if (task.index < task.macroActionList.size()) tasks.add(task);
    }

    public static void removeMacro(CommandSourceStack source) {
        tasks.removeIf(task -> Objects.equals(task.source.getEntity(), source.getEntity()));
    }

    public static void tick(MinecraftServer server) {
        List<MacroTask> tasksCopy = new ArrayList<>(tasks);
        for (MacroTask task : tasksCopy) {
            if (task.tickCounter <= 0) runCommand(task, server);
            task.tickCounter--;
        }
    }

    private static void runCommand(MacroTask task, MinecraftServer server) {
        //stops if entity died
        Entity entity = task.source.getEntity();
        if (entity != null) {
            if (entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
                tasks.remove(task);
                return;
            }
            if (entity instanceof ServerPlayer player) {
                task.source = player.createCommandSourceStack();
            } else {
                task.source = entity.createCommandSourceStackForNameResolution((ServerLevel) entity.level());
            }
        }

        server.getCommands().performPrefixedCommand(task.source, parseCommand(task.macroActionList.get(task.index).command, task.variables));

        task.index++;
        if (task.index >= task.macroActionList.size()) {
            tasks.remove(task);
            return;
        }
        task.tickCounter = task.macroActionList.get(task.index).delay;
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
