package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NicknameScreen extends Screen {
    private EditBox nameBox;
    private final Screen parent;

    public NicknameScreen(Screen parent) {
        super(Component.literal("Смена ника"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;

        nameBox = new EditBox(this.font, centerX - 100, this.height / 2 - 20, 200, 20, Component.literal("Ник"));
        nameBox.setMaxLength(16);
        nameBox.setValue(ConfigManager.customNickname != null ? ConfigManager.customNickname : Minecraft.getInstance().getUser().getName());
        this.addRenderableWidget(nameBox);

        this.addRenderableWidget(Button.builder(
                Component.literal("Сохранить"),
                (btn) -> {
                    String newName = nameBox.getValue().trim();
                    if (!newName.isEmpty() && !newName.equals(Minecraft.getInstance().getUser().getName())) {
                        ConfigManager.customNickname = newName;
                    } else {
                        ConfigManager.customNickname = null;
                    }
                    ConfigManager.save();
                    Minecraft.getInstance().setScreen(this.parent);
                }
        ).bounds(centerX - 100, this.height / 2 + 10, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Назад"),
                (btn) -> Minecraft.getInstance().setScreen(this.parent)
        ).bounds(centerX - 100, this.height / 2 + 40, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
        graphics.drawString(this.font, "Введите желаемый ник:", this.width / 2 - 80, this.height / 2 - 45, 0xFFFFFF, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}