package com.example.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ActionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("moderutil_actions.json");
    
    public static final List<PlayerAction> ACTIONS = new ArrayList<>();

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            // Если файла нет, создаем дефолтные кнопки
            ACTIONS.add(new PlayerAction("Кик", "kick %s"));
            ACTIONS.add(new PlayerAction("Бан", "ban %s"));
            ACTIONS.add(new PlayerAction("Тп к игроку", "tp %s"));
            ACTIONS.add(new PlayerAction("Мут (1 час)", "mute %s 1h"));
            save();
            return;
        }

        try {
            String content = Files.readString(CONFIG_PATH);
            List<PlayerAction> loaded = GSON.fromJson(content, new TypeToken<List<PlayerAction>>(){}.getType());
            if (loaded != null) {
                ACTIONS.clear();
                ACTIONS.addAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(ACTIONS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(String name, String command) {
        ACTIONS.add(new PlayerAction(name, command));
        save();
    }

    public static void remove(int index) {
        if (index >= 0 && index < ACTIONS.size()) {
            ACTIONS.remove(index);
            save();
        }
    }
}