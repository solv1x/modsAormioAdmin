package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class CheckCommand {

    public static void onStart(String playerName) {
        if (!ClickGuiScreen.CHECK_COMMAND.isEnabled()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§c[Check] §fМодуль 'Check Command' выключен в ClickGUI!"), false
            );
            return;
        }

        ModeratorState.setActivePlayer(playerName);
        CheckTimer.start(); // <--- ЗАПУСКАЕМ ТАЙМЕР

        String message = "@" + playerName + " Привет, ты вызван на проверку читов. " +
                "Скидывай свой ID AnyDesk. Скажу сразу, лив с проверки = бан. " +
                "Отказ от проверки - тоже моментально баню. " +
                "ВАЖНО: Discord не принимаем!";

        Minecraft.getInstance().player.connection.sendChat(message);

        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );

        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§a[Check] §fПроверка начата для " + playerName), false
        );
    }

    public static void onStop() {
        if (!ModeratorState.isActive()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§c[Check] §fСейчас нет активной проверки."), false
            );
            return;
        }
        String playerName = ModeratorState.getActivePlayer();
        ModeratorState.clear();
        CheckTimer.stop(); // <--- ОСТАНАВЛИВАЕМ ТАЙМЕР
        
        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§a[Check] §fПроверка для " + playerName + " остановлена."), false
        );
    }

    public static void onConfirm() {
        if (!ModeratorState.isActive()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§c[Check] §fСейчас нет активной проверки."), false
            );
            return;
        }
        String playerName = ModeratorState.getActivePlayer();
        ModeratorState.clear();
        CheckTimer.stop(); // <--- ОСТАНАВЛИВАЕМ ТАЙМЕР
        
        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§a[Check] §fПроверка для " + playerName + " подтверждена."), false
        );
    }
}