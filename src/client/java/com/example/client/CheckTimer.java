package com.example.client;

public class CheckTimer {
    private static long startTime = 0;
    private static boolean running = false;

    public static void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }

    public static void stop() {
        running = false;
    }

    public static boolean isRunning() {
        return running;
    }

    public static long getElapsedSeconds() {
        if (!running) return 0;
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}