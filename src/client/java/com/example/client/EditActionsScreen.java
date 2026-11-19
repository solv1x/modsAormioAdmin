package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditActionsScreen extends Screen {
    private final Screen parent;
    private EditBox nameBox;
    private EditBox commandBox;

    public EditActionsScreen(Screen parent) {
        super(Component.literal("Настройка действий"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;

        // Поле для названия кнопки
        nameBox = new EditBox(this.font, centerX - 100, 40, 200, 20, Component.literal("Название"));
        nameBox.setMaxLength(32);
        this.addRenderableWidget(nameBox);

        // Поле для команды
        commandBox = new EditBox(this.font, centerX - 100, 65, 200, 20, Component.literal("Команда (ник = %s)"));
        commandBox.setMaxLength(256);
        this.addRenderableWidget(commandBox);

        // Кнопка "Добавить"
        this.addRenderableWidget(Button.builder(
            Component.literal("Добавить кнопку"),
            (btn) -> {
                if (!nameBox.getValue().isEmpty() && !commandBox.getValue().isEmpty()) {
                    ActionManager.add(nameBox.getValue(), commandBox.getValue());
                    this.rebuild(); // Перестраиваем экран
                }
            }
        ).bounds(centerX - 100, 90, 200, 20).build());

        // Список кнопок для удаления
        int listY = 130;
        for (int i = 0; i < ActionManager.ACTIONS.size(); i++) {
            final int index = i;
            PlayerAction action = ActionManager.ACTIONS.get(i);
            this.addRenderableWidget(Button.builder(
                Component.literal("§c[X] §f" + action.getName()),
                (btn) -> {
                    ActionManager.remove(index);
                    this.rebuild();
                }
            ).bounds(centerX - 100, listY + i * 25, 200, 20).build());
        }

        // Кнопка "Назад"
        this.addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            (btn) -> Minecraft.getInstance().setScreen(parent)
        ).bounds(centerX - 100, this.height - 40, 200, 20).build());
    }

    private void rebuild() {
        String name = nameBox.getValue();
        String cmd = commandBox.getValue();
        this.clearWidgets();
        this.init();
        nameBox.setValue(name);
        commandBox.setValue(cmd);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
        graphics.drawString(this.font, "Настройка кнопок", this.width / 2 - 40, 15, 0xFFFFFFFF, false);
        graphics.drawString(this.font, "Нажми на [X], чтобы удалить", this.width / 2 - 70, 120, 0xFFAAAAAA, false);
        super.render(graphics, mouseX, mouseY, delta);
    }
}