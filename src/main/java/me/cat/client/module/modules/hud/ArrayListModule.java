package me.cat.client.module.modules.hud;

import me.cat.client.CatClient;
import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.ColorSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListModule extends Module {
    public ArrayListModule() {
        super("module.arrayList", Category.HUD);
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

        List<Module> enabled = CatClient.moduleManager.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int y = 4;
        for (Module m : enabled) {
            String name = m.getName();
            int w = mc.textRenderer.getWidth(name);
            int x = mc.getWindow().getScaledWidth() - w - 4;
            ctx.drawTextWithShadow(mc.textRenderer, name, x, y, color);
            y += mc.textRenderer.fontHeight + 2;
        }
    }
}
