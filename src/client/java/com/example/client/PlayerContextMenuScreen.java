package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PlayerContextMenuScreen extends Screen {
    private final String targetPlayer;
    private final Screen parent;

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 260;
    private int panelX;
    private int panelY;

    public PlayerContextMenuScreen(String targetPlayer, Screen parent) {
        super(Component.literal("Действия: " + targetPlayer));
        this.targetPlayer = targetPlayer;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - PANEL_HEIGHT) / 2;

        int startY = panelY + 70;
        int buttonWidth = 180;
        int buttonX = panelX + (PANEL_WIDTH - buttonWidth) / 2;

        for (int i = 0; i < ActionManager.ACTIONS.size(); i++) {
            PlayerAction action = ActionManager.ACTIONS.get(i);
            int y = startY + i * 25;

            this.addRenderableWidget(Button.builder(
                Component.literal(action.getName()),
                (btn) -> executeAction(action.getCommand(targetPlayer))
            ).bounds(buttonX, y, buttonWidth, 20).build());
        }

        int settingsY = startY + ActionManager.ACTIONS.size() * 25 + 15;

        this.addRenderableWidget(Button.builder(
            Component.literal("§eНастроить кнопки"),
            (btn) -> Minecraft.getInstance().setScreen(new EditActionsScreen(this))
        ).bounds(buttonX, settingsY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Закрыть"),
            (btn) -> Minecraft.getInstance().setScreen(this.parent)
        ).bounds(buttonX, settingsY + 25, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("§c[X]"),
            (btn) -> Minecraft.getInstance().setScreen(this.parent)
        ).bounds(panelX + PANEL_WIDTH - 25, panelY + 5, 20, 20).build());
    }

    private void executeAction(String rawCommand) {
        if (Minecraft.getInstance().player == null || rawCommand == null || rawCommand.trim().isEmpty()) {
            return;
        }

        // Убираем лишние пробелы по краям
        String command = rawCommand.trim();

        // Если игрок случайно написал слэш в настройках, убираем его
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        // Отправляем команду на сервер (Minecraft сам подставит слэш)
        Minecraft.getInstance().player.connection.sendCommand(command);

        // Закрываем меню
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF1E1E1E);
        drawBorder(graphics, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF555555, 1);

        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 30, 0xFF2D2D2D);
        graphics.fill(panelX, panelY + 30, panelX + PANEL_WIDTH, panelY + 31, 0xFF555555);

        graphics.drawString(this.font, "Действия с игроком", panelX + 8, panelY + 11, 0xFFFFFFFF, false);

        graphics.drawString(this.font, targetPlayer, panelX + 12, panelY + 42, 0xFFFFAA00, false);
        graphics.drawString(this.font, "Выберите действие:", panelX + 12, panelY + 56, 0xFFAAAAAA, false);

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color, int thickness) {
        graphics.fill(x, y, x + width, y + thickness, color);
        graphics.fill(x, y + height - thickness, x + width, y + height, color);
        graphics.fill(x, y, x + thickness, y + height, color);
        graphics.fill(x + width - thickness, y, x + width, y + height, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}