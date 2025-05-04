package com.xxl.mq.core.thread;

import com.xxl.tool.core.DateTool;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * registry thread
 *
 * Created by xuxueli on 16/8/28.
 */
public class RegistryThread {
    private volatile boolean running = false;
    private Thread registryThread = null;

    public void start() {
        running = true;

        // registry
        registryThread = new Thread(() -> {
            while (!running) {
                try {
                    // todo, registry
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(DateTool.formatDateTime(new Date()) + " registry beat...");
                } catch (InterruptedException ignored) {
                }
            }
        });
        registryThread.start();
    }

    public void stop() {
        running = false;
        registryThread.interrupt();
    }
}