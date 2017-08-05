package be.maximvdw.spigotbuyercheck.schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SchedulerManager
 *
 * @author Maxim Van de Wynckel
 * @date 08-May-16
 */
public class SchedulerManager {
    private static ScheduledExecutorService scheduler;
    private static ScheduledExecutorService asyncScheduler;

    /**
     * Create a new task
     *
     * @param runnable task
     * @param amount   amount of time units
     * @param timeUnit time unit
     */
    public static void createTask(Runnable runnable, long amount, TimeUnit timeUnit) {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        scheduler.scheduleAtFixedRate(runnable, 0, amount, timeUnit);
    }


    /**
     * Create a new async task
     *
     * @param runnable task
     * @param amount   amount of time units
     * @param timeUnit time unit
     */
    public static void createAsyncTask(Runnable runnable, long amount, TimeUnit timeUnit) {
        if (asyncScheduler == null) {
            asyncScheduler = Executors.newScheduledThreadPool(5);
        }
        asyncScheduler.scheduleAtFixedRate(runnable, 0, amount, timeUnit);
    }
}
