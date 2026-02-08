package com.radiushelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class RHConfigStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("radius_helper.json");
    private static RHConfig config = new RHConfig();

    private RHConfigStorage() {
    }

    public static RHConfig get() {
        return config;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            RHConfig loaded = GSON.fromJson(reader, RHConfig.class);
            if (loaded != null) {
                if (!loaded.showSelection) {
                    loaded.showSelectionBox = false;
                }
                loaded.showSelection = true;
                if (loaded.selectionColor == 0) {
                    loaded.selectionColor = 0x5AA6FF;
                }
                if (loaded.privateColor == 0) {
                    loaded.privateColor = 0x28FFFF;
                }
                if (loaded.tntColor == 0) {
                    loaded.tntColor = 0xFF7A2F;
                }
                loaded.clamp();
                config = loaded;
            }
        } catch (IOException ignored) {
            config = new RHConfig();
        }
    }

    public static void save() {
        config.clamp();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException ignored) {
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException ignored) {
            // Ignore config save errors.
        }
    }
}
