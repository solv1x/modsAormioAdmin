package com.example.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public class HudRenderer {

    public static void register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("moderutil", "check_timer"),
            (graphics, tickCounter) -> render(graphics)
        );
    }

    private static void render(GuiGraphics graphics) {
        if (!CheckTimer.isRunning()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long seconds = CheckTimer.getElapsedSeconds();
        String timeText = String.format("%02d:%02d", seconds / 60, seconds % 60);
        String player = ModeratorState.getActivePlayer();
        if (player == null) player = "?";

        String text = "Проверка: " + player + " | " + timeText;
        int x = 10;
        int y = 10;
        int width = mc.font.width(text) + 8;

        // Фон (полупрозрачный черный)
        graphics.fill(x - 4, y - 4, x + width, y + 12, 0x80000000);
        // Текст (БЕЛЫЙ, непрозрачный — 0xFFFFFFFF)
        graphics.drawString(mc.font, text, x, y, 0xFFFFFFFF, false);
    }
}