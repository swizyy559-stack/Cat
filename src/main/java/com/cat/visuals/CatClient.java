package com.cat.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CatClient implements ClientModInitializer {
    private static final Random RNG = new Random();
    private static final int ACCENT = 0xFFE8A15A;

    private static KeyBinding guiKey;
    private static boolean wasOnGround = true;
    private static final List<double[]> rings = new ArrayList<>(); // x, y, z, age
    private static Entity target;
    private static long targetUntil;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.catvisuals.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.catvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(CatClient::tick);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity) {
                target = entity;
                targetUntil = Util.getMeasuringTimeMs() + 3000;
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(CatClient::renderHud);
    }

    // ---------- логика эффектов ----------
    private static void tick(MinecraftClient mc) {
        while (guiKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new CatScreen());
        }
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;

        // JumpCircles: запускаем кольцо в момент прыжка
        boolean onGround = p.isOnGround();
        if (Module.JUMPCIRCLES.enabled && wasOnGround && !onGround && p.getVelocity().y > 0.1) {
            rings.add(new double[]{p.getX(), p.getY() + 0.03, p.getZ(), 0});
        }
        wasOnGround = onGround;

        // расширяющееся радужное кольцо
        for (var it = rings.iterator(); it.hasNext(); ) {
            double[] r = it.next();
            double radius = 0.3 + r[3] * 0.14;
            int points = 30;
            for (int i = 0; i < points; i++) {
                double a = i * (Math.PI * 2 / points);
                float hue = (float) ((i / (double) points + r[3] * 0.03) % 1.0);
                spawnDust(mc, rgb(hue), 0.7f,
                        r[0] + Math.cos(a) * radius, r[1], r[2] + Math.sin(a) * radius);
            }
            r[3]++;
            if (r[3] > 12) it.remove();
        }

        // Trails: оранжевые частицы за ногами при движении
        double speed = Math.hypot(p.getX() - p.prevX, p.getZ() - p.prevZ);
        if (Module.TRAILS.enabled && speed > 0.05) {
            for (int i = 0; i < 3; i++) {
                spawnDust(mc, 0xFF9A2E, 0.8f,
                        p.getX() + (RNG.nextDouble() - 0.5) * 0.5,
                        p.getY() + RNG.nextDouble() * 1.6,
                        p.getZ() + (RNG.nextDouble() - 0.5) * 0.5);
            }
        }

        // TargetRing: кольцо ползёт вверх-вниз по цели
        if (Module.TARGETRING.enabled && target != null) {
            if (target.isRemoved() || Util.getMeasuringTimeMs() > targetUntil) {
                target = null;
            } else {
                double t = Util.getMeasuringTimeMs() / 400.0;
                double y = target.getY() + (Math.sin(t) + 1) / 2 * target.getHeight();
                double radius = target.getWidth() * 0.9 + 0.2;
                for (int i = 0; i < 16; i++) {
                    double a = t * 2 + i * (Math.PI * 2 / 16);
                    spawnDust(mc, 0xFF5A3C, 0.8f,
                            target.getX() + Math.cos(a) * radius, y, target.getZ() + Math.sin(a) * radius);
                }
            }
        }
    }

    private static void spawnDust(MinecraftClient mc, int rgb, float scale, double x, double y, double z) {
        mc.world.addParticle(new DustParticleEffect(rgb, scale), x, y, z, 0, 0, 0);
    }

    private static int rgb(float hue) {
        return Color.HSBtoRGB(hue, 0.65f, 1f) & 0xFFFFFF;
    }

    // ---------- HUD ----------
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
            key(ctx, tr, "W", bx + s + g, by, s, mc.options.forwardKey.isPressed());
            key(ctx, tr, "A", bx, by + s + g, s, mc.options.leftKey.isPressed());
            key(ctx, tr, "S", bx + s + g, by + s + g, s, mc.options.backKey.isPressed());
            key(ctx, tr, "D", bx + 2 * (s + g), by + s + g, s, mc.options.rightKey.isPressed());
            keyWide(ctx, tr, "LMB", bx, by + 2 * (s + g), 31, s, mc.options.attackKey.isPressed());
            keyWide(ctx, tr, "RMB", bx + 33, by + 2 * (s + g), 31, s, mc.options.useKey.isPressed());
        }
    }

    private static void key(DrawContext ctx, TextRenderer tr, String l, int x, int y, int s, boolean down) {
        keyWide(ctx, tr, l, x, y, s, s, down);
    }

    private static void keyWide(DrawContext ctx, TextRenderer tr, String l, int x, int y, int w, int h, boolean down) {
        ctx.fill(x, y, x + w, y + h, down ? 0xCCE8A15A : 0x99201812);
        ctx.drawTextWithShadow(tr, l, x + (w - tr.getWidth(l)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }
}
