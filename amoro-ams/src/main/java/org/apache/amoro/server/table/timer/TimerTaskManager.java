package org.apache.amoro.server.table.timer;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.table.executor.AsyncTableExecutors;
import org.apache.amoro.server.table.executor.OptimizingExpiringExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class TimerTaskManager {

    private OptimizingExpiringExecutor optimizingExpiringExecutor;

    private static final TimerTaskManager instance = new TimerTaskManager();

    private ScheduledExecutorService executorService;
    private Map<String, Runnable> taskMap;

    private static final int delay = 60 * 1000 ;

    public void setUp(Configurations conf){
            this.optimizingExpiringExecutor = new OptimizingExpiringExecutor(
            conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_KEEP_DAYS),
            conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL_HOURS));
    }


    private TimerTaskManager() {
        this.executorService = Executors.newScheduledThreadPool(1);
        this.taskMap = new ConcurrentHashMap<>();
    }
    public static TimerTaskManager getInstance() {
        return instance;
    }

    public void addTask(String taskId, Runnable task,long period) {
        if (taskMap.containsKey(taskId)) {
            cancelTask(taskId);
        }
        executorService.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
        taskMap.put(taskId, task);
    }


    public void cancelTask(String taskId) {
        Runnable task = taskMap.get(taskId);
        if (task != null) {
            taskMap.remove(taskId);
        }
    }


    public void stopAllTasks() {
        executorService.shutdown();
        taskMap.clear();
    }

    public void startTimer() {
        this.addTask("optimizingExpiringExecutor",()-> optimizingExpiringExecutor.execute(),optimizingExpiringExecutor.getInterval());
    }
}
