package me.cat.client.module.modules.hud;

import me.cat.client.CatClient;
import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

public class HotkeysHUD extends Module {
    public HotkeysHUD() {
        super("module.hotkeys", Category.HUD);
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

        int x = 4;
        int y = 80;
        for (Module m : CatClient.moduleManager.getModules()) {
            if (m.getKey() != -1) {
                String name = m.getName() + " [" + InputUtil.fromKeyCode(m.getKey(), -1).getLocalizedText().getString() + "]";
                ctx.drawTextWithShadow(mc.textRenderer, name, x, y, color);
                y += mc.textRenderer.fontHeight + 2;
            }
        }
    }
}
