package com.example.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class ChatInteractionListener {

    public static void register() {
        // 1. Обычный чат игроков (Одиночка / Ванильные серверы)
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!ClickGuiScreen.PLAYER_MENU.isEnabled()) return true;
            if (sender == null) return true;

            String playerName = sender.name();
            if (playerName == null || playerName.isEmpty()) return true;

            // Делаем ник кликабельным для ОТКРЫТИЯ МЕНЮ
            Component clickableName = Component.literal(playerName)
                .withStyle(Style.EMPTY
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent.RunCommand("/moderutil openplayer " + playerName))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("§eОткрыть меню для §f" + playerName)))
                );

            Component newMessage = Component.literal("<")
                .append(clickableName)
                .append(Component.literal("> ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(message.getString()).withStyle(ChatFormatting.WHITE));

            Minecraft.getInstance().gui.getChat().addMessage(newMessage);
            return false;
        });

        // 2. Системные сообщения (Сервера с плагинами)
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (!ClickGuiScreen.PLAYER_MENU.isEnabled() || overlay) return message;

            String rawText = message.getString();
            if (rawText.isEmpty()) return message;

            Minecraft mc = Minecraft.getInstance();
            List<String> onlinePlayers = new ArrayList<>();
            if (mc.getConnection() != null) {
                for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                    String name = info.getProfile().name();
                    if (name != null) onlinePlayers.add(name.toLowerCase());
                }
            }

            // Разбиваем на слова, сохраняя пробелы
            String[] words = rawText.split("(?<=\\s)|(?=\\s)");
            MutableComponent result = Component.literal("");
            boolean changed = false;

            for (String word : words) {
                String trimmed = word.trim();
                if (trimmed.isEmpty()) {
                    result.append(Component.literal(word));
                    continue;
                }

                // Очищаем от знаков препинания (запятые и т.д.)
                String cleanWord = trimmed.replaceAll("[^a-zA-Z0-9_а-яА-Я]", "");
                boolean isOnlinePlayer = onlinePlayers.contains(cleanWord.toLowerCase());

                if (!cleanWord.isEmpty()) {
                    // ТЕПЕРЬ ЛЮБОЕ СЛОВО ОТКРЫВАЕТ МЕНЮ
                    Component clickableWord = Component.literal(word)
                        .withStyle(Style.EMPTY
                            .withColor(isOnlinePlayer ? ChatFormatting.GREEN : ChatFormatting.WHITE) // Игроки зеленые, остальные белые
                            .withClickEvent(new ClickEvent.RunCommand("/moderutil openplayer " + cleanWord))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("§eОткрыть меню для §f" + cleanWord)))
                        );
                    result.append(clickableWord);
                    changed = true;
                } else {
                    result.append(Component.literal(word));
                }
            }

            return changed ? result : message;
        });
    }
}