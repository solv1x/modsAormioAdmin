package com.example.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("moderutil.json");

    public static String customNickname = null;

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("fullbright", ClickGuiScreen.FULLBRIGHT.isEnabled());
        json.addProperty("check_command", ClickGuiScreen.CHECK_COMMAND.isEnabled());
        json.addProperty("player_menu", ClickGuiScreen.PLAYER_MENU.isEnabled());

        if (customNickname != null && !customNickname.isEmpty()) {
            json.addProperty("custom_nickname", customNickname);
        }

        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) return;

        try {
            String content = Files.readString(CONFIG_PATH);
            JsonObject json = GSON.fromJson(content, JsonObject.class);

            if (json.has("fullbright")) {
                ClickGuiScreen.FULLBRIGHT.setEnabled(json.get("fullbright").getAsBoolean());
            }
            if (json.has("check_command")) {
                ClickGuiScreen.CHECK_COMMAND.setEnabled(json.get("check_command").getAsBoolean());
            }
            if (json.has("player_menu")) {
                ClickGuiScreen.PLAYER_MENU.setEnabled(json.get("player_menu").getAsBoolean());
            }
            if (json.has("custom_nickname")) {
                customNickname = json.get("custom_nickname").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}