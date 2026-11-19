package com.example.client;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;

public class ChatSenderListener {
    public static void register() {
        // Перехват команд
        ClientSendMessageEvents.ALLOW_COMMAND.register((command) -> {
            // Если кликнули по нику в чате (команда от ClickEvent)
            if (command.startsWith("moderutil openplayer ")) {
                String playerName = command.substring("moderutil openplayer ".length()).trim();
                Minecraft.getInstance().setScreen(new PlayerContextMenuScreen(playerName, Minecraft.getInstance().screen));
                return false; // Блокируем отправку на сервер
            }

            // Твоя логика для /check
            if (command.startsWith("check")) {
                java.util.concurrent.CompletableFuture.delayedExecutor(3, java.util.concurrent.TimeUnit.SECONDS).execute(() -> {
                    Minecraft.getInstance().execute(() -> {
                        if (command.startsWith("check start ")) {
                            String playerName = command.substring("check start ".length()).trim();
                            CheckCommand.onStart(playerName);
                        } else if (command.equals("check stop")) {
                            CheckCommand.onStop();
                        } else if (command.equals("check confirm")) {
                            CheckCommand.onConfirm();
                        }
                    });
                });
                return true;
            }
            return true;
        });

        // Логика @ника при активной проверке
        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            if (ModeratorState.isActive()) {
                String player = ModeratorState.getActivePlayer();
                if (!message.startsWith("@" + player)) {
                    Minecraft.getInstance().player.connection.sendChat("@" + player + " " + message);
                    return false;
                }
            }
            return true;
        });
    }
}