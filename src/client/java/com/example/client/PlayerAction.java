package com.example.client;

public class PlayerAction {
    private final String name;
    private final String command; // Команда без ника, например "kick %s"

    public PlayerAction(String name, String command) {
        this.name = name;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    // %s будет заменен на ник игрока
    public String getCommand(String playerName) {
        return command.replace("%s", playerName);
    }
}