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
            System.out.println("Task with ID " + taskId + " already exists. Replacing the old task.");
            cancelTask(taskId); // 先取消旧任务
        }
        // 使用线程池执行定时任务
        executorService.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
        taskMap.put(taskId, task);
        System.out.println("Task with ID " + taskId + " added.");
    }


    public void cancelTask(String taskId) {
        Runnable task = taskMap.get(taskId);
        if (task != null) {
            // 取消任务
            // 注意：这里我们没有直接取消任务，因为 ScheduledExecutorService 并不直接支持取消某个任务
            // 但我们可以在管理中移除它（通常我们会通过任务标识符管理任务）
            taskMap.remove(taskId);  // 从任务列表中移除
        }
    }

    /**
     * 更新一个已经存在的定时任务
     * @param taskId 任务标识
     * @param newTask 要执行的新 Runnable 任务
     * @param period 任务执行的间隔时间
     */
    public void updateTask(String taskId, Runnable newTask, long period) {
        cancelTask(taskId);  // 先取消旧任务
        addTask(taskId, newTask, period);  // 添加新任务
    }

    public Map<String, Runnable> getAllTasks() {
        return taskMap;
    }

    public void stopAllTasks() {
        executorService.shutdown();
        taskMap.clear();
        System.out.println("All tasks have been stopped.");
    }

    public void startTimer() {
        this.addTask("optimizingExpiringExecutor",()-> optimizingExpiringExecutor.execute(),optimizingExpiringExecutor.getInterval());
    }
}
