package com.cat.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

/** Стеклянное меню Cat: размытый фон, боковая панель категорий, карточки модулей. */
public class CatScreen extends Screen {
    private static final int W = 380, H = 190, SIDE = 96;
    private static final int CARD_W = 126, CARD_H = 44, GAP = 8;
    private static final int BG = 0x1E1712, ACC = 0xE8A15A;
    private static String category = "Visuals";
    private final long openedAt = Util.getMeasuringTimeMs();

    public CatScreen() { super(Text.literal("Cat")); }

    @Override public boolean shouldPause() { return false; }

    private float fade() { return Math.min(1f, (Util.getMeasuringTimeMs() - openedAt) / 220f); }

    private static int argb(float alpha, int rgb) {
        int a = Math.max(4, (int) (Math.max(0f, Math.min(1f, alpha)) * 255));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpColor(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static void roundRect(DrawContext ctx, int x1, int y1, int x2, int y2, int r, int color) {
        r = Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        for (int dy = 0; dy < r; dy++) {
            double d = r - dy - 0.5;
            int dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - d * d)));
            ctx.fill(x1 + dx, y1 + dy, x2 - dx, y1 + dy + 1, color);
            ctx.fill(x1 + dx, y2 - dy - 1, x2 - dx, y2 - dy, color);
        }
        ctx.fill(x1, y1 + r, x2, y2 - r, color);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int px() { return (width - W) / 2; }
    private int py() { return (height - H) / 2; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.renderBackground(ctx, mx, my, delta); // размытие фона + затемнение
        float f = fade();
        int x = px(), y = py() + (int) ((1 - f) * 10);

        // стеклянная панель с рамкой
        roundRect(ctx, x - 1, y - 1, x + W + 1, y + H + 1, 9, argb(0.55f * f, ACC));
        roundRect(ctx, x, y, x + W, y + H, 8, argb(0.82f * f, BG));

        // боковая панель
        roundRect(ctx, x + 6, y + 6, x + SIDE, y + H - 6, 6, argb(0.5f * f, 0x000000));
        ctx.drawTextWithShadow(textRenderer, "CAT", x + 16, y + 16, argb(f, 0xFFB86B));
        ctx.drawTextWithShadow(textRenderer, "v1.0", x + 40, y + 16, argb(f, 0x8A7F74));

        int cy = y + 40;
        for (String c : Module.CATEGORIES) {
            boolean sel = c.equals(category);
            boolean hov = inside(mx, my, x + 10, cy, SIDE - 14, 20);
            if (sel) roundRect(ctx, x + 10, cy, x + SIDE - 4, cy + 20, 5, argb(0.95f * f, ACC));
            else if (hov) roundRect(ctx, x + 10, cy, x + SIDE - 4, cy + 20, 5, argb(0.25f * f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, c, x + 18, cy + 6, argb(f, sel ? BG : 0xFFFFFF));
            cy += 24;
        }
        ctx.drawTextWithShadow(textRenderer, "RShift - закрыть", x + 12, y + H - 18, argb(f, 0x8A7F74));

        // заголовок и карточки
        ctx.drawTextWithShadow(textRenderer, category, x + SIDE + 14, y + 14, argb(f, 0xFFFFFF));
        int cx0 = x + SIDE + 14, cy0 = y + 34, i = 0;
        for (Module m : Module.in(category)) {
            int cx = cx0 + (i % 2) * (CARD_W + GAP);
            int cyy = cy0 + (i / 2) * (CARD_H + GAP);
            boolean hov = inside(mx, my, cx, cyy, CARD_W, CARD_H);
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.3f;

            roundRect(ctx, cx, cyy, cx + CARD_W, cyy + CARD_H, 6, argb((hov ? 0.28f : 0.16f) * f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, m.name, cx + 8, cyy + 8, argb(f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(m.desc, CARD_W - 16),
                    cx + 8, cyy + 24, argb(f, 0xA89C90));

            int tx = cx + CARD_W - 30, ty = cyy + 7;
            roundRect(ctx, tx, ty, tx + 22, ty + 11, 5, argb(f, lerpColor(0x4A413A, ACC, m.anim)));
            int kx = tx + 1 + (int) (m.anim * 11);
            roundRect(ctx, kx, ty + 1, kx + 9, ty + 10, 4, argb(f, 0xFFFFFF));
            i++;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int x = px(), y = py();
            int cy = y + 40;
            for (String c : Module.CATEGORIES) {
                if (inside(mx, my, x + 10, cy, SIDE - 14, 20)) { category = c; return true; }
                cy += 24;
            }
            int cx0 = x + SIDE + 14, cy0 = y + 34, i = 0;
            for (Module m : Module.in(category)) {
                int cx = cx0 + (i % 2) * (CARD_W + GAP);
                int cyy = cy0 + (i / 2) * (CARD_H + GAP);
                if (inside(mx, my, cx, cyy, CARD_W, CARD_H)) { m.toggle(); return true; }
                i++;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
