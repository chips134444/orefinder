package com.example.orefinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OreFinderClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static KeyBinding menuKey;

    private static final List<Found> emeralds = new ArrayList<>();
    private static final List<Found> coals = new ArrayList<>();
    private int tickCounter = 0;

    private record Found(BlockPos pos, double dist) {}

    @Override
    public void onInitializeClient() {
        OreFinderConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.orefinder.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.orefinder"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.orefinder.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.orefinder"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                OreFinderConfig.enabled = !OreFinderConfig.enabled;
                OreFinderConfig.save();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Ore Finder: " +
                            (OreFinderConfig.enabled ? "ON" : "OFF")), true);
                }
            }
            while (menuKey.wasPressed()) {
                client.setScreen(new OreFinderScreen(null));
            }

            if (!OreFinderConfig.enabled || client.world == null || client.player == null) {
                emeralds.clear();
                coals.clear();
                return;
            }
            if (++tickCounter >= 40) { // rescan every 2 seconds
                tickCounter = 0;
                scan(client.world, client.player.getBlockPos());
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            if (!OreFinderConfig.enabled) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;

            int x = 6, y = 6;
            ctx.drawTextWithShadow(mc.textRenderer, "Ore Finder (G toggle, H menu)", x, y, 0xFFFFFF);
            y += 12;
            if (OreFinderConfig.findEmerald) y = drawList(ctx, mc, "Deepslate Emerald", emeralds, x, y, 0x55FF55);
            if (OreFinderConfig.findCoal) y = drawList(ctx, mc, "Deepslate Coal", coals, x, y, 0xAAAAAA);
        });
    }

    private static int drawList(net.minecraft.client.gui.DrawContext ctx, MinecraftClient mc,
                                String label, List<Found> list, int x, int y, int color) {
        if (list.isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, label + ": none nearby", x, y, color);
            return y + 11;
        }
        ctx.drawTextWithShadow(mc.textRenderer, label + ":", x, y, color);
        y += 11;
        for (Found f : list) {
            String s = String.format("  %d, %d, %d  (%.0fm)", f.pos.getX(), f.pos.getY(), f.pos.getZ(), f.dist);
            ctx.drawTextWithShadow(mc.textRenderer, s, x, y, color);
            y += 10;
        }
        return y + 2;
    }

    private void scan(ClientWorld world, BlockPos center) {
        emeralds.clear();
        coals.clear();

        int r = OreFinderConfig.range;
        int minY = world.getBottomY();
        int maxY = Math.min(world.getTopYInclusive(), 8); // deepslate lives at/below ~y=8
        Vec3d c = center.toCenterPos();
        BlockPos.Mutable p = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    p.set(center.getX() + dx, y, center.getZ() + dz);
                    Block b = world.getBlockState(p).getBlock();
                    if (b == Blocks.DEEPSLATE_EMERALD_ORE && OreFinderConfig.findEmerald) {
                        emeralds.add(new Found(p.toImmutable(), Math.sqrt(p.getSquaredDistance(c))));
                    } else if (b == Blocks.DEEPSLATE_COAL_ORE && OreFinderConfig.findCoal) {
                        coals.add(new Found(p.toImmutable(), Math.sqrt(p.getSquaredDistance(c))));
                    }
                }
            }
        }
        emeralds.sort(Comparator.comparingDouble(Found::dist));
        coals.sort(Comparator.comparingDouble(Found::dist));
        trim(emeralds);
        trim(coals);
    }

    private static void trim(List<Found> l) {
        while (l.size() > 5) l.remove(l.size() - 1);
    }
}
