package com.zyrex.module;

import com.zyrex.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final Category category;
    private final List<Setting> settings = new ArrayList<Setting>();
    private boolean enabled;
    private int key = -1;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public List<Setting> getSettings() { return settings; }
    public int getKey() { return key; }

    public void setKey(int key) { this.key = key; }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }

    protected void onEnable() {}
    protected void onDisable() {}

    protected void addSetting(Setting setting) { settings.add(setting); }

    public String getKeyDisplay() {
        if (key == -1) return "NONE";
        return Keyboard.getKeyName(key);
    }
}
