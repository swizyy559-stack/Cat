package com.cat.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

/** Стеклянное меню модулей (ClickGUI). */
public class CatScreen extends Screen {
    private static final int W = 260, ROW = 24, HEAD = 34;
    private final long openedAt = Util.getMeasuringTimeMs();

    public CatScreen() { super(Text.literal("Cat")); }

    @Override public boolean shouldPause() { return false; }

    private float fade() {
        return Math.min(1f, (Util.getMeasuringTimeMs() - openedAt) / 200f);
    }

    private static int argb(float alpha, int rgb) {
        return ((int) (Math.max(0, Math.min(1, alpha)) * 255) << 24) | (rgb & 0xFFFFFF);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, argb(0.35f * fade(), 0x000000));
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        float f = fade();
        int h = HEAD + Module.ALL.size() * ROW + 8;
        int x = (width - W) / 2;
        int y = (height - h) / 2 + (int) ((1 - f) * 12);

        // стекло
        ctx.fill(x, y, x + W, y + h, argb(0.72f * f, 0x2A211A));
        ctx.drawBorder(x, y, W, h, argb(0.8f * f, 0xE8A15A));
        ctx.drawTextWithShadow(textRenderer, "CAT", x + 10, y + 10, argb(f, 0xFFB86B));
        ctx.drawTextWithShadow(textRenderer, "Modules", x + 40, y + 10, argb(f, 0xBFB3A6));

        int ry = y + HEAD;
        for (Module m : Module.ALL) {
            boolean hover = mx >= x + 6 && mx <= x + W - 6 && my >= ry && my < ry + ROW - 2;
            ctx.fill(x + 6, ry, x + W - 6, ry + ROW - 2, argb((hover ? 0.35f : 0.2f) * f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, m.name, x + 12, ry + 3, argb(f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, m.desc, x + 12, ry + 13, argb(f, 0x9A8F84));
            // переключатель
            int px = x + W - 40, py = ry + 5;
            ctx.fill(px, py, px + 26, py + 12, argb(f, m.enabled ? 0xE8A15A : 0x4A413A));
            int knob = m.enabled ? px + 15 : px + 1;
            ctx.fill(knob, py + 1, knob + 10, py + 11, argb(f, 0xFFFFFF));
            ry += ROW;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int h = HEAD + Module.ALL.size() * ROW + 8;
        int x = (width - W) / 2, y = (height - h) / 2;
        int ry = y + HEAD;
        for (Module m : Module.ALL) {
            if (button == 0 && mx >= x + 6 && mx <= x + W - 6 && my >= ry && my < ry + ROW - 2) {
                m.toggle();
                return true;
            }
            ry += ROW;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
