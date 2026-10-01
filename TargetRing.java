package me.cat.client.module.modules.visuals;

import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.BooleanSetting;
import me.cat.client.module.settings.ColorSetting;
import me.cat.client.module.settings.NumberSetting;

public class TargetRing extends Module {
    public TargetRing() {
        super("module.targetRing", Category.VISUALS);
        addSetting(new BooleanSetting("setting.enabled", true));
        addSetting(new ColorSetting("setting.color", 0xFFFF0000));
        addSetting(new NumberSetting("setting.opacity", 0.8, 0.0, 1.0, 0.05));
        addSetting(new NumberSetting("setting.radius", 1.2, 0.1, 3.0, 0.1));
    }
}
