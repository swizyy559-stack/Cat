package com.cat.visuals;

import java.util.List;

public class Module {
    public final String name;
    public final String desc;
    public boolean enabled;

    public Module(String name, String desc, boolean enabled) {
        this.name = name;
        this.desc = desc;
        this.enabled = enabled;
    }

    public void toggle() { enabled = !enabled; }

    public static final Module WATERMARK   = new Module("Watermark",   "Cat | ник | FPS | время", true);
    public static final Module HOTKEYS     = new Module("Hotkeys",     "Панель с биндами", true);
    public static final Module ARRAYLIST   = new Module("ArrayList",   "Список включённых модулей", true);
    public static final Module KEYSTROKES  = new Module("Keystrokes",  "WASD / ЛКМ / ПКМ", true);
    public static final Module JUMPCIRCLES = new Module("JumpCircles", "Круг при прыжке", true);
    public static final Module TRAILS      = new Module("Trails",      "Шлейф за игроком", true);
    public static final Module TARGETRING  = new Module("TargetRing",  "Кольцо вокруг цели", true);

    public static final List<Module> ALL = List.of(
            WATERMARK, HOTKEYS, ARRAYLIST, KEYSTROKES, JUMPCIRCLES, TRAILS, TARGETRING);
}
