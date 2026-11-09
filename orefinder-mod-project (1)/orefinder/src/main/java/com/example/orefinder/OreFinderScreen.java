package com.example.orefinder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class OreFinderScreen extends Screen {
    private final Screen parent;

    public OreFinderScreen(Screen parent) {
        super(Text.literal("Deepslate Ore Finder"));
        this.parent = parent;
    }

    private static Text onOff(String name, boolean v) {
        return Text.literal(name + ": " + (v ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        int cx = width / 2 - 100;
        int y = height / 4;

        addDrawableChild(ButtonWidget.builder(onOff("Ore Finder", OreFinderConfig.enabled), b -> {
            OreFinderConfig.enabled = !OreFinderConfig.enabled;
            b.setMessage(onOff("Ore Finder", OreFinderConfig.enabled));
        }).dimensions(cx, y, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(onOff("Deepslate Emerald", OreFinderConfig.findEmerald), b -> {
            OreFinderConfig.findEmerald = !OreFinderConfig.findEmerald;
            b.setMessage(onOff("Deepslate Emerald", OreFinderConfig.findEmerald));
        }).dimensions(cx, y + 26, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(onOff("Deepslate Coal", OreFinderConfig.findCoal), b -> {
            OreFinderConfig.findCoal = !OreFinderConfig.findCoal;
            b.setMessage(onOff("Deepslate Coal", OreFinderConfig.findCoal));
        }).dimensions(cx, y + 52, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Scan range: " + OreFinderConfig.range), b -> {
            OreFinderConfig.range = OreFinderConfig.range >= 32 ? 8 : OreFinderConfig.range + 8;
            b.setMessage(Text.literal("Scan range: " + OreFinderConfig.range));
        }).dimensions(cx, y + 78, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx, y + 110, 200, 20).build());
    }

    @Override
    public void close() {
        OreFinderConfig.save();
        if (client != null) client.setScreen(parent);
    }
}
