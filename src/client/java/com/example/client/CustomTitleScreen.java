package com.example.client;

import com.example.client.mixin.UserMixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

public class CustomTitleScreen extends Screen {

    public CustomTitleScreen() {
        super(Component.literal("Модераторский клиент"));
    }


@Override
protected void init() {
    int centerX = this.width / 2 - 100;
    int y = this.height / 4 + 40;

    this.addRenderableWidget(Button.builder(
            Component.literal("Одиночная игра"),
            btn -> this.minecraft.setScreen(new SelectWorldScreen(this))
    ).bounds(centerX, y, 200, 20).build());

    this.addRenderableWidget(Button.builder(
            Component.literal("Сетевая игра"),
            btn -> this.minecraft.setScreen(new JoinMultiplayerScreen(this))
    ).bounds(centerX, y + 24, 200, 20).build());

    this.addRenderableWidget(Button.builder(
            Component.literal("Сменить ник"),
            btn -> this.minecraft.setScreen(new NicknameScreen(this))
    ).bounds(centerX, y + 48, 200, 20).build());

    this.addRenderableWidget(Button.builder(
            Component.literal("ModerUtil GUI"),
            btn -> this.minecraft.setScreen(new ClickGuiScreen())
    ).bounds(centerX, y + 72, 200, 20).build());

    this.addRenderableWidget(Button.builder(
            Component.literal("Настройки"),
            btn -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
    ).bounds(centerX, y + 96, 200, 20).build());

    this.addRenderableWidget(Button.builder(
            Component.literal("Выход"),
            btn -> this.minecraft.stop()
    ).bounds(centerX, y + 120, 200, 20).build());
}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    graphics.fill(0, 0, this.width, this.height, 0xFF14141E);

    graphics.fill(0, 0, this.width, 40, 0x80000000);
        graphics.drawCenteredString(this.font, "Модераторский Клиент", this.width / 2, 15, 0xFFFFFFFF);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawString(this.font, "ModerUtil 1.0", 4, this.height - 14, 0xFFAAAAAA, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
