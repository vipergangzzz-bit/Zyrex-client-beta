package com.zyrex.setting;

public class NumberSetting extends Setting {
    private double value;
    private final double min, max, step;

    public NumberSetting(String name, double value, double min, double max, double step) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getValue() { return value; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public void setValue(double value) { this.value = Math.max(min, Math.min(max, value)); }

    @Override
    public String getDisplayValue() {
        if (step == (int) step) return String.valueOf((int) value);
        return String.format("%.1f", value);
    }

    @Override
    public void onClick(int button) {
        if (button == 0) value = Math.min(max, value + step);
        else if (button == 1) value = Math.max(min, value - step);
    }
}
