package com.example.client;

public class ModeratorState {
    private static String activePlayer = null;

    public static void setActivePlayer(String name) {
        activePlayer = name;
    }

    public static String getActivePlayer() {
        return activePlayer;
    }

    public static void clear() {
        activePlayer = null;
    }

    public static boolean isActive() {
        return activePlayer != null;
    }
}