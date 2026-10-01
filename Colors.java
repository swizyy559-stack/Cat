package com.cat.visuals;

import java.awt.Color;

public final class Colors {
    private Colors() {}

    public static boolean rainbow(Setting s) { return s.idx == 0; }

    public static int fixed(Setting s) {
        switch (s.idx) {
            case 1: return 0xFF8A4C;
            case 2: return 0x4CD7FF;
            case 3: return 0xFF6AD5;
            default: return 0xFFFFFF;
        }
    }

    public static int hsb(float hue) {
        float h = ((hue % 1f) + 1f) % 1f;
        return Color.HSBtoRGB(h, 0.6f, 1f) & 0xFFFFFF;
    }

    public static int pick(Setting s, float hue) { return rainbow(s) ? hsb(hue) : fixed(s); }

    public static int accent() {
        int[] a = {0xE8A15A, 0x5AA8E8, 0xE85AB4, 0x5AE88A, 0xA05AE8};
        return a[Module.THEME_COLOR.idx];
    }

    public static int accentA() { return 0xFF000000 | accent(); }
}
