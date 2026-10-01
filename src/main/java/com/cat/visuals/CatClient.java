package com.cat.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CatClient implements ClientModInitializer {
    private static final int ACCENT = 0xFFE8A15A;
    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.catvisuals.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.catvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new CatScreen());
            }
            WorldFx.tick(mc);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity) WorldFx.setTarget(entity);
            return ActionResult.PASS;
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(WorldFx::render);
        HudRenderCallback.EVENT.register(CatClient::renderHud);
    }

    private static int rgb(float hue) {
        return Color.HSBtoRGB(hue, 0.65f, 1f) & 0xFFFFFF;
    }

    private static void renderHud(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();

        if (Module.WATERMARK.enabled) {
            String rest = " | " + mc.player.getName().getString() + " | " + mc.getCurrentFps() + " fps | "
                    + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            int w = tr.getWidth("Cat") + tr.getWidth(rest) + 10;
            ctx.fill(6, 6, 6 + w, 20, 0x99201812);
            ctx.fill(6, 6, 8, 20, ACCENT);
            ctx.drawTextWithShadow(tr, "Cat", 12, 9, ACCENT);
            ctx.drawTextWithShadow(tr, rest, 12 + tr.getWidth("Cat"), 9, 0xFFFFFFFF);
        }

        if (Module.HOTKEYS.enabled) {
            int y = 26;
            ctx.fill(6, y, 96, y + 28, 0x99201812);
            ctx.drawTextWithShadow(tr, "Hotkeys", 12, y + 3, ACCENT);
            ctx.drawTextWithShadow(tr, "ClickGUI  [RSHIFT]", 12, y + 15, 0xFFCFC5BA);
        }

        if (Module.ARRAYLIST.enabled) {
            List<Module> on = new ArrayList<>();
            for (Module m : Module.ALL) if (m.enabled) on.add(m);
            on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());
            int y = 6, i = 0;
            long t = Util.getMeasuringTimeMs();
            for (Module m : on) {
                int w = tr.getWidth(m.name);
                int x = sw - w - 8;
                float hue = ((t / 3000f) + i * 0.06f) % 1f;
                ctx.fill(x - 3, y - 1, sw - 3, y + 10, 0x88201812);
                ctx.fill(sw - 5, y - 1, sw - 3, y + 10, 0xFF000000 | rgb(hue));
                ctx.drawTextWithShadow(tr, m.name, x, y + 1, 0xFF000000 | rgb(hue));
                y += 11;
                i++;
            }
        }

        if (Module.KEYSTROKES.enabled) {
            int bx = 10, by = sh / 2 - 30, s = 20, g = 2;
            key(ctx, tr, "W", bx + s + g, by, s, s, mc.options.forwardKey.isPressed());
            key(ctx, tr, "A", bx, by + s + g, s, s, mc.options.leftKey.isPressed());
            key(ctx, tr, "S", bx + s + g, by + s + g, s, s, mc.options.backKey.isPressed());
            key(ctx, tr, "D", bx + 2 * (s + g), by + s + g, s, s, mc.options.rightKey.isPressed());
            key(ctx, tr, "LMB", bx, by + 2 * (s + g), 31, s, mc.options.attackKey.isPressed());
            key(ctx, tr, "RMB", bx + 33, by + 2 * (s + g), 31, s, mc.options.useKey.isPressed());
        }
    }

    private static void key(DrawContext ctx, TextRenderer tr, String l, int x, int y, int w, int h, boolean down) {
        ctx.fill(x, y, x + w, y + h, down ? 0xCCE8A15A : 0x99201812);
        ctx.drawTextWithShadow(tr, l, x + (w - tr.getWidth(l)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }
}
