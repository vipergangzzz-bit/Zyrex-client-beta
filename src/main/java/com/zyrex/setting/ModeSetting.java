package com.zyrex.setting;

public class ModeSetting extends Setting {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        this.modes = modes;
    }

    public String getValue() { return modes[index]; }

    public void setValue(String value) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) { index = i; return; }
        }
    }

    @Override
    public String getDisplayValue() { return getValue(); }

    @Override
    public void onClick(int button) { index = (index + 1) % modes.length; }
}
