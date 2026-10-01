package com.cat.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class CatClient implements ClientModInitializer {
    private static final Random RNG = new Random();
    private static final Deque<Long> CLICKS = new ArrayDeque<>();

    private static KeyBinding guiKey, zoomKey;
    private static boolean lastAttack, zooming, moving;
    private static int savedFov;
    private static float pulse;
    private static double speedBps;

    @Override
    public void onInitializeClient() {
        Config.load();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.catvisuals.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.catvisuals"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.catvisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.catvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new CatScreen());
            }
            WorldFx.tick(mc);
            FpsBoost.tick(mc);
            zoomTick(mc);
            infoTick(mc);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            FpsBoost.restore(mc);
            if (zooming && mc.options != null) mc.options.getFov().setValue(savedFov);
            Config.save();
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity) {
                WorldFx.setTarget(entity);
                if (Module.HITEFFECTS.enabled) {
                    ParticleEffect pe;
                    switch (Module.HIT_TYPE.idx) {
                        case 1: pe = ParticleTypes.CRIT; break;
                        case 2: pe = ParticleTypes.HEART; break;
                        case 3: pe = ParticleTypes.FLAME; break;
                        case 4: pe = ParticleTypes.END_ROD; break;
                        default: pe = ParticleTypes.ENCHANTED_HIT;
                    }
                    int count = (int) Module.HIT_COUNT.v;
                    for (int i = 0; i < count; i++) {
                        world.addParticle(pe,
                                entity.getX() + (RNG.nextDouble() - 0.5) * entity.getWidth(),
                                entity.getBodyY(0.5) + (RNG.nextDouble() - 0.5),
                                entity.getZ() + (RNG.nextDouble() - 0.5) * entity.getWidth(),
                                (RNG.nextDouble() - 0.5) * 0.4,
                                RNG.nextDouble() * 0.3,
                                (RNG.nextDouble() - 0.5) * 0.4);
                    }
                }
            }
            return ActionResult.PASS;
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(WorldFx::render);
        HudRenderCallback.EVENT.register(CatClient::renderHud);
    }

    // ---------- тики ----------
    private static void zoomTick(MinecraftClient mc) {
        if (mc.options == null) return;
        boolean want = Module.ZOOM.enabled && zoomKey.isPressed() && mc.currentScreen == null;
        if (want && !zooming) {
            savedFov = mc.options.getFov().getValue();
            zooming = true;
        }
        if (want) {
            mc.options.getFov().setValue((int) Module.ZOOM_FOV.v);
        } else if (zooming) {
            mc.options.getFov().setValue(savedFov);
            zooming = false;
        }
    }

    private static void infoTick(MinecraftClient mc) {
        long now = Util.getMeasuringTimeMs();
        boolean atk = mc.options.attackKey.isPressed();
        if (atk && !lastAttack && mc.currentScreen == null) {
            CLICKS.addLast(now);
            pulse = 5f;
        }
        lastAttack = atk;
        while (!CLICKS.isEmpty() && now - CLICKS.peekFirst() > 1000) CLICKS.pollFirst();
        pulse *= 0.8f;
        if (pulse < 0.1f) pulse = 0f;

        if (mc.player != null) {
            double sp = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
            speedBps = sp * 20.0;
            moving = sp > 0.05;
        }
    }

    // ---------- HUD ----------
    private static void renderHud(DrawContext ctx, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int acc = Colors.accentA();

        if (Module.WATERMARK.enabled) {
            String ping = "";
            if (Module.WM_PING.b && mc.getNetworkHandler() != null && !mc.isInSingleplayer()) {
                PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (e != null) ping = " | " + e.getLatency() + " ms";
            }
            String rest = " | " + mc.player.getName().getString() + " | " + mc.getCurrentFps() + " fps" + ping
                    + " | " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            int w = tr.getWidth("Cat") + tr.getWidth(rest) + 10;
            ctx.fill(6, 6, 6 + w, 20, 0x99201812);
            ctx.fill(6, 6, 8, 20, acc);
            ctx.drawTextWithShadow(tr, "Cat", 12, 9, acc);
            ctx.drawTextWithShadow(tr, rest, 12 + tr.getWidth("Cat"), 9, 0xFFFFFFFF);
        }

        int infoY = 26;
        if (Module.HOTKEYS.enabled) {
            ctx.fill(6, infoY, 110, infoY + 28, 0x99201812);
            ctx.drawTextWithShadow(tr, "Hotkeys", 12, infoY + 3, acc);
            ctx.drawTextWithShadow(tr, "ClickGUI [RSHIFT]  Zoom [C]", 12, infoY + 15, 0xFFCFC5BA);
            infoY += 32;
        }

        List<String> lines = new ArrayList<>();
        if (Module.COORDS.enabled) {
            String line = String.format("XYZ %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (Module.CO_DIR.b) {
                String d = mc.player.getHorizontalFacing().asString();
                line += "  " + d.substring(0, 1).toUpperCase() + d.substring(1);
            }
            lines.add(line);
        }
        if (Module.CPS.enabled) lines.add("CPS " + CLICKS.size());
        if (Module.SPEED.enabled) lines.add(String.format("Speed %.1f b/s", speedBps));
        if (!lines.isEmpty()) {
            int w = 0;
            for (String l : lines) w = Math.max(w, tr.getWidth(l));
            ctx.fill(6, infoY, 6 + w + 12, infoY + lines.size() * 11 + 4, 0x99201812);
            ctx.fill(6, infoY, 8, infoY + lines.size() * 11 + 4, acc);
            int ly = infoY + 3;
            for (String l : lines) {
                ctx.drawTextWithShadow(tr, l, 12, ly, 0xFFFFFFFF);
                ly += 11;
            }
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
                int col = Module.AL_RAINBOW.b
                        ? (0xFF000000 | Colors.hsb((t / 3000f) + i * 0.06f))
                        : acc;
                if (Module.AL_BG.b) ctx.fill(x - 3, y - 1, sw - 3, y + 10, 0x88201812);
                ctx.fill(sw - 5, y - 1, sw - 3, y + 10, col);
                ctx.drawTextWithShadow(tr, m.name, x, y + 1, col);
                y += 11;
                i++;
            }
        }

        if (Module.KEYSTROKES.enabled) {
            int bx = 10, by = sh / 2 - 30, s = 20, g = 2;
            key(ctx, tr, "W", bx + s + g, by, s, s, mc.options.forwardKey.isPressed(), acc);
            key(ctx, tr, "A", bx, by + s + g, s, s, mc.options.leftKey.isPressed(), acc);
            key(ctx, tr, "S", bx + s + g, by + s + g, s, s, mc.options.backKey.isPressed(), acc);
            key(ctx, tr, "D", bx + 2 * (s + g), by + s + g, s, s, mc.options.rightKey.isPressed(), acc);
            if (Module.KS_MOUSE.b) {
                key(ctx, tr, "LMB", bx, by + 2 * (s + g), 31, s, mc.options.attackKey.isPressed(), acc);
                key(ctx, tr, "RMB", bx + 33, by + 2 * (s + g), 31, s, mc.options.useKey.isPressed(), acc);
            }
        }

        if (Module.ARMORHUD.enabled) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int ax = sw - 24, ay = sh - 100;
            for (EquipmentSlot slot : slots) {
                ItemStack st = mc.player.getEquippedStack(slot);
                if (!st.isEmpty()) {
                    ctx.drawItem(st, ax, ay);
                    if (Module.AR_DUR.b) ctx.drawStackOverlay(tr, st, ax, ay);
                }
                ay += 20;
            }
        }

        if (Module.CROSSHAIRFX.enabled && mc.currentScreen == null) {
            renderCrosshair(ctx, sw / 2, sh / 2);
        }
    }

    private static void renderCrosshair(DrawContext ctx, int cx, int cy) {
        float hue = (Util.getMeasuringTimeMs() % 4000) / 4000f;
        int col = 0xFF000000 | Colors.pick(Module.CROSS_COLOR, hue);
        int size = (int) Module.CROSS_SIZE.v;
        int gap = 6 + (Module.CROSS_DYN.b ? (int) (pulse + (moving ? 3 : 0)) : 0);
        if (Module.CROSS_STYLE.idx == 0) {
            ctx.fill(cx - 1, cy - gap - size, cx + 1, cy - gap, col);
            ctx.fill(cx - 1, cy + gap, cx + 1, cy + gap + size, col);
            ctx.fill(cx - gap - size, cy - 1, cx - gap, cy + 1, col);
            ctx.fill(cx + gap, cy - 1, cx + gap + size, cy + 1, col);
        } else {
            int r = gap + size;
            for (int i = 0; i < 40; i++) {
                double a = i * Math.PI * 2 / 40;
                int x = cx + (int) Math.round(Math.cos(a) * r);
                int y = cy + (int) Math.round(Math.sin(a) * r);
                ctx.fill(x, y, x + 1, y + 1, col);
            }
        }
    }

    private static void key(DrawContext ctx, TextRenderer tr, String l, int x, int y, int w, int h, boolean down, int acc) {
        ctx.fill(x, y, x + w, y + h, down ? (0xCC000000 | (acc & 0xFFFFFF)) : 0x99201812);
        ctx.drawTextWithShadow(tr, l, x + (w - tr.getWidth(l)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }
}
