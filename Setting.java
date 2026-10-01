package com.cat.visuals;

public class Setting {
    public enum Type { BOOL, SLIDER, MODE }

    public final String name;
    public final Type type;
    public boolean b;
    public double v, min, max, step;
    public String[] modes;
    public int idx;

    private Setting(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public static Setting bool(String name, boolean def) {
        Setting s = new Setting(name, Type.BOOL);
        s.b = def;
        return s;
    }

    public static Setting slider(String name, double min, double max, double step, double def) {
        Setting s = new Setting(name, Type.SLIDER);
        s.min = min; s.max = max; s.step = step; s.v = def;
        return s;
    }

    public static Setting mode(String name, int def, String... modes) {
        Setting s = new Setting(name, Type.MODE);
        s.modes = modes;
        s.idx = def;
        return s;
    }

    public void setFraction(double f) {
        f = Math.max(0, Math.min(1, f));
        double raw = min + (max - min) * f;
        v = Math.max(min, Math.min(max, Math.round(raw / step) * step));
    }

    public double fraction() { return (v - min) / (max - min); }

    public void cycle(int dir) { idx = (idx + dir + modes.length) % modes.length; }

    public String display() {
        if (type == Type.BOOL) return b ? "ON" : "OFF";
        if (type == Type.MODE) return modes[idx];
        return step >= 1 ? String.valueOf((int) Math.round(v)) : String.format("%.1f", v);
    }

    public String save() {
        if (type == Type.BOOL) return Boolean.toString(b);
        if (type == Type.MODE) return Integer.toString(idx);
        return Double.toString(v);
    }

    public void load(String s) {
        try {
            if (type == Type.BOOL) b = Boolean.parseBoolean(s);
            else if (type == Type.MODE) idx = Math.max(0, Math.min(modes.length - 1, Integer.parseInt(s)));
            else v = Math.max(min, Math.min(max, Double.parseDouble(s)));
        } catch (NumberFormatException ignored) {
        }
    }
}
