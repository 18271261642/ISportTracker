package com.isport.tracker.fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtils {
    private ExecutorService service;

    private ThreadPoolUtils() {
        int num = Runtime.getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(num * 2);
    }

    private static ThreadPoolUtils manager = new ThreadPoolUtils();

    public static ThreadPoolUtils getInstance() {
        return manager;
    }

    public void addTask(Runnable runnable) {
        if (!service.isShutdown()) {
            service.execute(runnable);
        }
    }

    public void onTerminate() {
        if (service != null) {
            service.shutdown();

        }
    }
}
