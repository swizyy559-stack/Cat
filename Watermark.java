package me.cat.client.module.modules.hud;

import me.cat.client.CatClient;
import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.ColorSetting;
import net.minecraft.client.gui.DrawContext;

public class Watermark extends Module {
    public Watermark() {
        super("module.watermark", Category.HUD);
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
        ctx.drawTextWithShadow(mc.textRenderer, CatClient.NAME + " v" + CatClient.VERSION, 4, 4, color);
    }
}
