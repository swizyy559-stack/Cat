package me.cat.client.module.modules.visuals;

import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.BooleanSetting;
import me.cat.client.module.settings.ColorSetting;
import me.cat.client.module.settings.NumberSetting;

public class JumpCircles extends Module {
    public JumpCircles() {
        super("module.jumpCircles", Category.VISUALS);
        addSetting(new BooleanSetting("setting.enabled", true));
        addSetting(new ColorSetting("setting.color", 0xFFFFAA00));
        addSetting(new NumberSetting("setting.opacity", 0.7, 0.0, 1.0, 0.05));
        addSetting(new NumberSetting("setting.radius", 1.0, 0.1, 3.0, 0.1));
    }

    @Override
    public void onRender3D(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        // TODO: рендер кольца при прыжке
    }
}
