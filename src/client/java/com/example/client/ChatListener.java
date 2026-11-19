package com.example.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ChatListener {
    private static String lastCheckedPlayer = null;

    public static void setLastCheckedPlayer(String name) {
        lastCheckedPlayer = name;
    }

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (lastCheckedPlayer == null) return true;

            String text = message.getString();
            if (text.contains(lastCheckedPlayer)) {
                // 1. Звук при ответе
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                );

                // 2. Подсветка (блокируем оригинал, добавляем цветной)
                Component colored = Component.literal("[Проверка] ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(text).withStyle(ChatFormatting.YELLOW));
                Minecraft.getInstance().gui.getChat().addMessage(colored);

                return false; // Оригинал не показываем
            }
            return true;
        });
    }
}