package Hopper4et.apollocarpetadditions.commands;

import Hopper4et.apollocarpetadditions.models.DelayCommand;
import Hopper4et.apollocarpetadditions.models.Macro;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MacroRunner {

    private static final List<MacroTask> tasks = new ArrayList<>();

    private static class MacroTask {
        final List<DelayCommand> delayCommandList;
        final ServerCommandSource source;
        int index = 0, tickCounter;

        private MacroTask(List<DelayCommand> delayCommandList, ServerCommandSource source) {
            this.delayCommandList = delayCommandList;
            this.source = source;
            tickCounter = delayCommandList.getFirst().delay();
        }
    }

    public static void addMacro(Macro macro, ServerCommandSource source) {
        String sourceName = source.getName();
        tasks.removeIf(task -> Objects.equals(task.source.getName(), sourceName));
        MacroTask task = new MacroTask(macro.commands(), source);
        if (task.tickCounter == 0) runCommand(task, source.getServer());
        if (task.index < task.delayCommandList.size()) tasks.add(task);
    }

    public static void removeMacro(String name) {
        tasks.removeIf(task -> Objects.equals(task.source.getName(), name));
    }

    public static List<String> getMacroList() {
        List<String> list = new ArrayList<>();
        for (MacroTask task : tasks) {
            list.add(task.source.getName());
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
        ServerCommandSource source = task.source;
        if (source.isExecutedByPlayer() && source.getPlayer() != null && server.getPlayerManager().getPlayer(source.getPlayer().getUuid()) == null) {
            tasks.remove(task);
            return;
        }
        //run command
        server.getCommandManager().executeWithPrefix(task.source, task.delayCommandList.get(task.index).command());

        task.index++;
        if (task.index >= task.delayCommandList.size()) {
            tasks.remove(task);
            return;
        }
        task.tickCounter = task.delayCommandList.get(task.index).delay();
        if (task.tickCounter == 0) runCommand(task, server);
    }
}
