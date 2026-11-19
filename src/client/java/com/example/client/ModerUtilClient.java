package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class ModerUtilClient implements ClientModInitializer {

    public static KeyMapping openGuiKey;
    public static KeyMapping openPlayerMenuKey;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ActionManager.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("moderutil", "custom_category")
        );

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.moderutil.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, category
        ));

        openPlayerMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.moderutil.open_player_menu", InputConstants.Type.KEYSYM, InputConstants.KEY_LALT, category
        ));

        // Регистрируем клиентскую команду для открытия меню
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("moderutil")
                .then(ClientCommandManager.literal("openplayer")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            Minecraft.getInstance().setScreen(new PlayerContextMenuScreen(playerName, Minecraft.getInstance().screen));
                            return 1;
                        })
                    )
                )
            );
        });

        ChatListener.register();
        ChatSenderListener.register();
        ChatInteractionListener.register();
        HudRenderer.register();

        // Кнопка в главном меню
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                int buttonWidth = 200;
                int buttonHeight = 20;
                int x = scaledWidth / 2 - buttonWidth / 2;
                int y = scaledHeight / 4 + 48 + 24 * 4; 
                Screens.getButtons(screen).add(Button.builder(
                        Component.literal("Сменить ник"),
                        (btn) -> client.setScreen(new NicknameScreen(screen))
                ).bounds(x, y, buttonWidth, buttonHeight).build());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }

            while (openPlayerMenuKey.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    if (client.hitResult instanceof EntityHitResult entityHit) {
                        if (entityHit.getEntity() instanceof Player targetPlayer) {
                            String playerName = targetPlayer.getName().getString();
                            client.setScreen(new PlayerContextMenuScreen(playerName, null));
                        }
                    }
                }
            }

            if (client.player != null) {
                if (ClickGuiScreen.FULLBRIGHT.isEnabled()) {
                    client.player.addEffect(new MobEffectInstance(
                            MobEffects.NIGHT_VISION, 20, 0, false, false, false
                    ));
                } else {
                    client.player.removeEffect(MobEffects.NIGHT_VISION);
                }
            }
        });
    }
}