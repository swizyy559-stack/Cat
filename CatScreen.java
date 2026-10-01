package com.cat.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Меню Cat: категории слева, модули в середине, настройки справа. */
public class CatScreen extends Screen {
    private static final int W = 440, H = 262, SIDE = 92;
    private static final int LIST_X = SIDE + 10, LIST_W = 150, ROW_H = 24, ROW_GAP = 3;
    private static final int SET_X = LIST_X + LIST_W + 10, SET_W = W - SET_X - 10, SROW = 28;
    private static final int BG = 0x1E1712;

    private static String category = "Visuals";
    private static Module selected;
    private Setting dragging;
    private final long openedAt = Util.getMeasuringTimeMs();

    public CatScreen() { super(Text.literal("Cat")); }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void removed() {
        Config.save();
        super.removed();
    }

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
        if (r < 0) r = 0;
        for (int dy = 0; dy < r; dy++) {
            double d = r - dy - 0.5;
            int dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - d * d)));
            ctx.fill(x1 + dx, y1 + dy, x2 - dx, y1 + dy + 1, color);
            ctx.fill(x1 + dx, y2 - dy - 1, x2 - dx, y2 - dy, color);
        }
        ctx.fill(x1, y1 + r, x2, y2 - r, color);
    }

    private static void drawSwitch(DrawContext ctx, int x, int y, float on, float f, int acc) {
        roundRect(ctx, x, y, x + 22, y + 11, 5, argb(f, lerpColor(0x4A413A, acc, on)));
        int kx = x + 1 + (int) (on * 11);
        roundRect(ctx, kx, y + 1, kx + 9, y + 10, 4, argb(f, 0xFFFFFF));
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int px() { return (width - W) / 2; }
    private int py() { return (height - H) / 2; }

    private void ensureSelected() {
        if (selected == null || !selected.category.equals(category)) {
            selected = Module.in(category).get(0);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.renderBackground(ctx, mx, my, delta); // размытие фона
        ensureSelected();
        float f = fade();
        int acc = Colors.accent();
        int x = px(), y = py() + (int) ((1 - f) * 10);

        roundRect(ctx, x - 1, y - 1, x + W + 1, y + H + 1, 9, argb(0.55f * f, acc));
        roundRect(ctx, x, y, x + W, y + H, 8, argb((float) Module.THEME_ALPHA.v * f, BG));

        // боковая панель
        roundRect(ctx, x + 6, y + 6, x + SIDE, y + H - 6, 6, argb(0.5f * f, 0x000000));
        ctx.drawTextWithShadow(textRenderer, "CAT", x + 16, y + 16, argb(f, acc));
        ctx.drawTextWithShadow(textRenderer, "v2.0", x + 40, y + 16, argb(f, 0x8A7F74));

        int cy = y + 40;
        for (String c : Module.CATEGORIES) {
            boolean sel = c.equals(category);
            boolean hov = inside(mx, my, x + 10, cy, SIDE - 14, 20);
            if (sel) roundRect(ctx, x + 10, cy, x + SIDE - 4, cy + 20, 5, argb(0.95f * f, acc));
            else if (hov) roundRect(ctx, x + 10, cy, x + SIDE - 4, cy + 20, 5, argb(0.25f * f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, c, x + 16, cy + 6, argb(f, sel ? BG : 0xFFFFFF));
            cy += 24;
        }
        ctx.drawTextWithShadow(textRenderer, "RShift - закрыть", x + 10, y + H - 18, argb(f, 0x8A7F74));

        // список модулей
        ctx.drawTextWithShadow(textRenderer, category, x + LIST_X, y + 14, argb(f, 0xFFFFFF));
        List<Module> mods = Module.in(category);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int rx = x + LIST_X, ry = y + 34 + i * (ROW_H + ROW_GAP);
            boolean sel = m == selected;
            boolean hov = inside(mx, my, rx, ry, LIST_W, ROW_H);
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.3f;
            if (sel) roundRect(ctx, rx, ry, rx + LIST_W, ry + ROW_H, 6, argb(0.3f * f, acc));
            else roundRect(ctx, rx, ry, rx + LIST_W, ry + ROW_H, 6, argb((hov ? 0.22f : 0.12f) * f, 0xFFFFFF));
            ctx.drawTextWithShadow(textRenderer, m.name, rx + 8, ry + 8, argb(f, 0xFFFFFF));
            drawSwitch(ctx, rx + LIST_W - 30, ry + 6, m.anim, f, acc);
        }

        // настройки выбранного модуля
        int sx = x + SET_X, sy = y + 34;
        roundRect(ctx, sx - 4, y + 30, sx + SET_W, y + H - 10, 6, argb(0.12f * f, 0xFFFFFF));
        ctx.drawTextWithShadow(textRenderer, selected.name, sx + 2, y + 14, argb(f, acc));
        ctx.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(selected.desc, SET_W - 8),
                sx + 2, sy + 2, argb(f, 0x8A7F74));
        sy += 16;
        if (selected.settings.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "Нет настроек", sx + 2, sy + 4, argb(f, 0xA89C90));
        }
        for (int j = 0; j < selected.settings.size(); j++) {
            Setting s = selected.settings.get(j);
            int rowY = sy + j * SROW;
            ctx.drawTextWithShadow(textRenderer, s.name, sx + 2, rowY + 2, argb(f, 0xFFFFFF));
            if (s.type == Setting.Type.BOOL) {
                drawSwitch(ctx, sx + SET_W - 34, rowY + 1, s.b ? 1f : 0f, f, acc);
            } else if (s.type == Setting.Type.MODE) {
                String t = "< " + s.display() + " >";
                ctx.drawTextWithShadow(textRenderer, t, sx + SET_W - textRenderer.getWidth(t) - 8, rowY + 2, argb(f, acc));
            } else {
                String t = s.display();
                ctx.drawTextWithShadow(textRenderer, t, sx + SET_W - textRenderer.getWidth(t) - 8, rowY + 2, argb(f, acc));
                int x1 = sx + 4, x2 = sx + SET_W - 8, ty = rowY + 15;
                roundRect(ctx, x1, ty, x2, ty + 4, 2, argb(0.3f * f, 0xFFFFFF));
                int fw = (int) ((x2 - x1) * s.fraction());
                roundRect(ctx, x1, ty, x1 + Math.max(4, fw), ty + 4, 2, argb(f, acc));
                roundRect(ctx, x1 + fw - 3, ty - 2, x1 + fw + 3, ty + 6, 3, argb(f, 0xFFFFFF));
            }
        }
    }

    private void updateSlider(double mx) {
        if (dragging == null) return;
        int sx = px() + SET_X;
        dragging.setFraction((mx - (sx + 4)) / (double) (SET_W - 12));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = px(), y = py();
        ensureSelected();

        if (button == 0) {
            int cy = y + 40;
            for (String c : Module.CATEGORIES) {
                if (inside(mx, my, x + 10, cy, SIDE - 14, 20)) {
                    category = c;
                    ensureSelected();
                    return true;
                }
                cy += 24;
            }
        }

        List<Module> mods = Module.in(category);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int rx = x + LIST_X, ry = y + 34 + i * (ROW_H + ROW_GAP);
            if (inside(mx, my, rx, ry, LIST_W, ROW_H)) {
                selected = m;
                if (button == 1 || (button == 0 && mx >= rx + LIST_W - 34)) m.toggle();
                return true;
            }
        }

        int sx = x + SET_X, sy = y + 34 + 16;
        for (int j = 0; j < selected.settings.size(); j++) {
            Setting s = selected.settings.get(j);
            int rowY = sy + j * SROW;
            if (s.type == Setting.Type.SLIDER) {
                if (button == 0 && inside(mx, my, sx, rowY + 10, SET_W - 4, 16)) {
                    dragging = s;
                    updateSlider(mx);
                    return true;
                }
            } else if (inside(mx, my, sx, rowY, SET_W - 4, 16)) {
                if (s.type == Setting.Type.BOOL) s.b = !s.b;
                else s.cycle(button == 1 ? -1 : 1);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null && button == 0) {
            updateSlider(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
