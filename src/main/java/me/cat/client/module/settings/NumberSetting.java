package me.cat.client.module.settings;

public class NumberSetting extends Setting<Double> {
    private final double min, max, step;

    public NumberSetting(String nameKey, Double value, double min, double max, double step) {
        super(nameKey, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }
}
