package me.cat.client.module.modules.visuals;

import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.BooleanSetting;
import me.cat.client.module.settings.ColorSetting;
import me.cat.client.module.settings.NumberSetting;

public class Trails extends Module {
    public Trails() {
        super("module.trails", Category.VISUALS);
        addSetting(new BooleanSetting("setting.enabled", true));
        addSetting(new ColorSetting("setting.color", 0xFF00FFFF));
        addSetting(new NumberSetting("setting.opacity", 0.5, 0.0, 1.0, 0.05));
        addSetting(new NumberSetting("setting.thickness", 2.0, 0.5, 5.0, 0.1));
    }
}
