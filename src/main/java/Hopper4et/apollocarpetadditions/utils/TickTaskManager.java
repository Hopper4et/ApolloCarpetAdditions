package Hopper4et.apollocarpetadditions.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class TickTaskManager {

    private static final Set<TickTask> tickTasks = new HashSet<>();

    public static final class TickTask {
        private final Runnable action;
        private int ticks;

        public TickTask(Runnable action, int ticks) {
            this.action = action;
            this.ticks = ticks;
        }

        private int decrementTicks() {
            return --ticks;
        }

        public void refresh(int ticks) {
            this.ticks = Math.max(this.ticks, ticks);
        }
    }

    public static TickTask createTask(Runnable action, int ticks) {
        TickTask tickTask = new TickTask(action, ticks);
        tickTasks.add(tickTask);
        return tickTask;
    }

    public static void tick() {
        final Iterator<TickTask> iterator = tickTasks.iterator();
        while (iterator.hasNext()) {
            TickTask tickTask = iterator.next();
            if (tickTask.decrementTicks() <= 0) {
                tickTask.action.run();
                iterator.remove();
            }
        }
    }
}
