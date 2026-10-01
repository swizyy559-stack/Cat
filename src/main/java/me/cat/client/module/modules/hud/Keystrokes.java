package me.cat.client.module.modules.hud;

import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

public class Keystrokes extends Module {
    public Keystrokes() {
        super("module.keystrokes", Category.HUD);
        addSetting(new ColorSetting("setting.color", 0xFFFFFFFF));
    }

    @Override
    public void onRender2D(DrawContext ctx, float tickDelta) {
        if (!isEnabled()) return;
        int color = getSettings().stream()
                .filter(s -> s instanceof ColorSetting)
                .findFirst()
                .map(s -> ((ColorSetting) s).getValue())
                .orElse(0xFFFFFFFF);

        int startX = 6;
        int startY = mc.getWindow().getScaledHeight() - 74;

        drawKey(ctx, "W", startX + 20, startY,
                InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey("key.keyboard.w").getCode()), color);
        drawKey(ctx, "A", startX, startY + 20,
                InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey("key.keyboard.a").getCode()), color);
        drawKey(ctx, "S", startX + 20, startY + 20,
                InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey("key.keyboard.s").getCode()), color);
        drawKey(ctx, "D", startX + 40, startY + 20,
                InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey("key.keyboard.d").getCode()), color);
        drawKey(ctx, "LMB", startX, startY + 40, mc.options.attackKey.isPressed(), color);
        drawKey(ctx, "RMB", startX + 34, startY + 40, mc.options.useKey.isPressed(), color);
    }

    private void drawKey(DrawContext ctx, String text, int x, int y, boolean pressed, int color) {
        int bg = pressed ? 0xFFAAAAAA : 0x99000000;
        int w = text.equals("LMB") || text.equals("RMB") ? 32 : 18;
        int h = 18;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + (w - mc.textRenderer.getWidth(text)) / 2, y + 5, color);
    }
}
