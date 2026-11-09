package com.example.orefinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OreFinderConfig {
    public static boolean enabled = true;
    public static boolean findEmerald = true;
    public static boolean findCoal = true;
    public static int range = 24; // horizontal scan radius in blocks

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("orefinder.json");

    private static class Data {
        boolean enabled = true;
        boolean findEmerald = true;
        boolean findCoal = true;
        int range = 24;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Data d = GSON.fromJson(Files.readString(FILE), Data.class);
                if (d != null) {
                    enabled = d.enabled;
                    findEmerald = d.findEmerald;
                    findCoal = d.findCoal;
                    range = d.range;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        Data d = new Data();
        d.enabled = enabled;
        d.findEmerald = findEmerald;
        d.findCoal = findCoal;
        d.range = range;
        try {
            Files.writeString(FILE, GSON.toJson(d));
        } catch (IOException ignored) {
        }
    }
}
