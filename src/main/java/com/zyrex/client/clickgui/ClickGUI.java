package com.zyrex.client.clickgui;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.module.ModuleManager;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import com.zyrex.setting.Setting;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends GuiScreen {
    private final ModuleManager moduleManager;
    private static Category selectedCategory = Category.Combat;
    private Module expandedModule;
    private boolean binding;
    private Setting draggedSetting;
    private int animTick;

    private int sidebarW, mainX, mainW, contentY, rowH = 26;
    private int settingsW = 180;

    // palette
    private static final int BG = 0xF0080808;
    private static final int SIDEBAR = 0xF00A0A0A;
    private static final int PANEL = 0xF0111111;
    private static final int CARD = 0xE8141414;
    private static final int CARD_HOVER = 0xFF1A1A1A;
    private static final int ACCENT = 0xFF9B4DCA;
    private static final int ACCENT_GLOW = 0x209B4DCA;
    private static final int BORDER = 0x402A2A2A;
    private static final int TEXT = 0xFFEEEEEE;
    private static final int MUTED = 0xFF777777;
    private static final int SUBTLE = 0xFF444444;
    private static final int TRACK_BG = 0xFF222222;
    private static final int KNOB = 0xFFDDDDDD;
    private static final int BADGE = 0xFF1A1A1A;

    // category icons
    private static final String[] CAT_ICONS = {"\u2694", "\u26A1", "\uD83D\uDEE1", "\uD83D\uDD11"};
    // ⚔ ☠ for combat, ⚡ for movement, 🛡 for player, 🔑 for exploit

    public ClickGUI(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public void initGui() {
        sidebarW = (int) (width * 0.09);
        sidebarW = Math.max(sidebarW, 80);
        mainX = sidebarW + 1;
        mainW = (int) Math.min(380, width * 0.28);
        contentY = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        animTick++;

        Gui.drawRect(0, 0, width, height, BG);

        // sidebar with border glow
        Gui.drawRect(0, 0, sidebarW, height, SIDEBAR);
        drawGradRect(sidebarW, 0, sidebarW + 1, height, ACCENT, (ACCENT & 0x00FFFFFF) | 0x30000000);

        // logo area
        mc.fontRendererObj.drawString("ZyRex", 14, 14, ACCENT, false);
        mc.fontRendererObj.drawString("v1.0", 14, 26, SUBTLE, false);

        // separator under logo
        Gui.drawRect(12, 38, sidebarW - 12, 39, BORDER);

        // categories
        int catY = 50;
        int catH = 34;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            boolean active = cat == selectedCategory;
            boolean hovered = mouseX >= 2 && mouseX <= sidebarW && mouseY >= catY && mouseY <= catY + catH;

            if (active) {
                Gui.drawRect(2, catY, sidebarW - 2, catY + catH, ACCENT_GLOW);
                Gui.drawRect(2, catY, 4, catY + catH, ACCENT);
            } else if (hovered) {
                Gui.drawRect(2, catY, sidebarW - 2, catY + catH, 0x08FFFFFF);
            }

            String icon = i < CAT_ICONS.length ? CAT_ICONS[i] : "";
            String name = cat.displayName();
            int iconX = 14;
            int textX = iconX + (icon.isEmpty() ? 0 : 10);

            if (!icon.isEmpty())
                mc.fontRendererObj.drawString(icon, iconX, catY + 9, active ? ACCENT : MUTED, false);

            mc.fontRendererObj.drawString(name, textX, catY + 9, active ? TEXT : MUTED, false);

            catY += catH;
        }

        // main panel
        int panelH = height;
        Gui.drawRect(mainX, 0, mainX + mainW, panelH, PANEL);
        Gui.drawRect(mainX + mainW, 0, mainX + mainW + 1, panelH, BORDER);

        // header
        int headerH = 38;
        Gui.drawRect(mainX + 1, 1, mainX + mainW - 1, headerH, 0xFF0D0D0D);
        Gui.drawRect(mainX + 12, headerH - 1, mainX + mainW - 12, headerH, BORDER);

        String headerTitle = selectedCategory.displayName();
        List<Module> modList = moduleManager.getModulesByCategory(selectedCategory);
        String headerCount = modList.size() + " modules";
        mc.fontRendererObj.drawString(headerTitle, mainX + 14, 12, TEXT, false);
        mc.fontRendererObj.drawString(headerCount, mainX + 14, 24, SUBTLE, false);

        // modules
        int modY = headerH + 4;
        int modPad = 6;
        int modGap = 3;

        for (Module m : modList) {
            boolean hovered = mouseX >= mainX + 4 && mouseX <= mainX + mainW - 4
                    && mouseY >= modY && mouseY <= modY + rowH;

            // card background
            Gui.drawRect(mainX + modPad, modY, mainX + mainW - modPad, modY + rowH,
                    hovered ? CARD_HOVER : CARD);

            // subtle left accent if enabled
            if (m.isEnabled())
                Gui.drawRect(mainX + modPad, modY, mainX + modPad + 2, modY + rowH, ACCENT);

            // keybind badge
            int nameX = mainX + modPad + 10;
            if (m.getKey() != -1) {
                String keyStr = m.getKeyDisplay();
                int kw = mc.fontRendererObj.getStringWidth(keyStr) + 6;
                Gui.drawRect(nameX - 2, modY + 4, nameX + kw - 2, modY + 16, 0xFF181818);
                Gui.drawRect(nameX - 2, modY + 4, nameX + kw - 2, modY + 16, BORDER);
                mc.fontRendererObj.drawString(keyStr, nameX, modY + 6, SUBTLE, false);
                nameX += kw + 4;
            }

            // module name
            mc.fontRendererObj.drawString(m.getName(), nameX, modY + 6, hovered ? TEXT : 0xFFCCCCCC, false);

            // toggle switch (pill)
            int toggleX = mainX + mainW - modPad - 32;
            int toggleY = modY + 6;
            int toggleW = 26;
            int toggleH = 13;

            int trackColor = m.isEnabled() ? ACCENT : 0xFF2A2A2A;
            drawPill(toggleX, toggleY, toggleW, toggleH, trackColor);

            if (m.isEnabled()) {
                drawPillKnob(toggleX + toggleW - toggleH, toggleY, toggleH, KNOB);
            } else {
                drawPillKnob(toggleX, toggleY, toggleH, 0xFF666666);
            }

            modY += rowH + modGap;
        }

        // settings panel
        if (expandedModule != null) {
            int setX = mainX + mainW + 2;
            int setY = 0;
            int setW = settingsW;
            int setH = height;

            Gui.drawRect(setX, setY, setX + setW, setY + setH, PANEL);
            Gui.drawRect(setX, setY, setX + 1, setY + setH, BORDER);

            // settings header
            Gui.drawRect(setX + 1, 1, setX + setW - 1, headerH, 0xFF0D0D0D);
            Gui.drawRect(setX + 12, headerH - 1, setX + setW - 12, headerH, BORDER);

            String setTitle = expandedModule.getName();
            if (binding) setTitle = "\u2190 Press a key...";
            mc.fontRendererObj.drawString(setTitle, setX + 14, 12, binding ? ACCENT : TEXT, false);
            mc.fontRendererObj.drawString(expandedModule.getSettings().size() + " settings", setX + 14, 24, SUBTLE, false);

            int setRowY = headerH + 8;
            for (Setting s : expandedModule.getSettings()) {
                int sRowH = s instanceof NumberSetting ? 38 : 26;
                boolean sHovered = mouseX >= setX + 4 && mouseX <= setX + setW - 4
                        && mouseY >= setRowY && mouseY <= setRowY + sRowH;

                Gui.drawRect(setX + 6, setRowY, setX + setW - 6, setRowY + sRowH, sHovered ? 0x10FFFFFF : 0x00000000);

                mc.fontRendererObj.drawString(s.getName(), setX + 12, setRowY + 4, TEXT, false);

                if (s instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) s;
                    float pct = (float) ((ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                    int sliderX = setX + 12;
                    int sliderY = setRowY + 20;
                    int sliderW = 90;
                    int sliderH = 3;

                    Gui.drawRect(sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, TRACK_BG);
                    int fillEnd = sliderX + (int) (pct * sliderW);
                    if (fillEnd > sliderX)
                        drawGradRect(sliderX, sliderY, fillEnd, sliderY + sliderH, ACCENT,
                                new Color(139, 67, 186, 180).getRGB());

                    // knob
                    int knobX = fillEnd;
                    Gui.drawRect(knobX - 2, sliderY - 2, knobX + 3, sliderY + sliderH + 2, KNOB);

                    String valStr = ns.getDisplayValue();
                    mc.fontRendererObj.drawString(valStr, sliderX + sliderW + 8, sliderY - 2, TEXT, false);

                } else if (s instanceof ModeSetting) {
                    int mx = setX + setW - 12 - mc.fontRendererObj.getStringWidth(s.getDisplayValue());
                    mc.fontRendererObj.drawString("\u25C0 " + s.getDisplayValue() + " \u25B6",
                            setX + 12, setRowY + 14, ACCENT, false);
                } else {
                    mc.fontRendererObj.drawString(s.getDisplayValue(), setX + 12, setRowY + 14, MUTED, false);
                }

                setRowY += sRowH + 3;
            }
        }
    }

    private void drawPill(int x, int y, int w, int h, int color) {
        Gui.drawRect(x + 2, y, x + w - 2, y + h, color);
        Gui.drawRect(x, y + 1, x + 2, y + h - 1, color);
        Gui.drawRect(x + w - 2, y + 1, x + w, y + h - 1, color);
    }

    private void drawPillKnob(int x, int y, int size, int color) {
        Gui.drawRect(x + 1, y + 1, x + size - 1, y + size - 1, color);
    }

    private void drawGradRect(int x1, int y1, int x2, int y2, int colorA, int colorB) {
        for (int y = y1; y < y2; y++) {
            float ratio = (float) (y - y1) / (y2 - y1);
            int r = lerp((colorA >> 16) & 0xFF, (colorB >> 16) & 0xFF, ratio);
            int g = lerp((colorA >> 8) & 0xFF, (colorB >> 8) & 0xFF, ratio);
            int b = lerp(colorA & 0xFF, colorB & 0xFF, ratio);
            int a = lerp((colorA >> 24) & 0xFF, (colorB >> 24) & 0xFF, ratio);
            Gui.drawRect(x1, y, x2, y + 1, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private int lerp(int a, int b, float t) {
        return (int) (a + (b - a) * t);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // category click
        int catY = 50;
        int catH = 34;
        Category[] cats = Category.values();
        for (Category cat : cats) {
            if (mouseX >= 2 && mouseX <= sidebarW && mouseY >= catY && mouseY <= catY + catH) {
                selectedCategory = cat;
                expandedModule = null;
                binding = false;
                return;
            }
            catY += catH;
        }

        // module click
        int headerH = 38;
        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int modY = headerH + 4;
        int modPad = 6;
        int modGap = 3;

        for (Module m : modules) {
            if (mouseX >= mainX + 4 && mouseX <= mainX + mainW - 4
                    && mouseY >= modY && mouseY <= modY + rowH) {
                if (mouseButton == 0) {
                    m.toggle();
                } else if (mouseButton == 1) {
                    expandedModule = expandedModule == m ? null : m;
                    binding = false;
                } else if (mouseButton == 2) {
                    expandedModule = m;
                    binding = true;
                }
                return;
            }
            modY += rowH + modGap;
        }

        // settings clicks
        if (expandedModule != null) {
            int setX = mainX + mainW + 2;
            int setRowY = headerH + 8;
            for (Setting s : expandedModule.getSettings()) {
                int sRowH = s instanceof NumberSetting ? 38 : 26;
                if (mouseX >= setX + 4 && mouseX <= setX + settingsW - 4
                        && mouseY >= setRowY && mouseY <= setRowY + sRowH) {
                    if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        int sliderX = setX + 12;
                        int sliderW = 90;
                        int sliderY = setRowY + 20;
                        if (mouseY >= sliderY - 3 && mouseY <= sliderY + 6
                                && mouseX >= sliderX && mouseX <= sliderX + sliderW) {
                            draggedSetting = ns;
                            updateSliderValue(ns, mouseX, sliderX, sliderW);
                            return;
                        }
                    } else if (s instanceof ModeSetting) {
                        s.onClick(mouseButton);
                        return;
                    } else {
                        s.onClick(mouseButton);
                        return;
                    }
                }
                setRowY += sRowH + 3;
            }
        }

        expandedModule = null;
        binding = false;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggedSetting = null;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggedSetting instanceof NumberSetting && expandedModule != null) {
            int setX = mainX + mainW + 2;
            int sliderX = setX + 12;
            int sliderW = 90;
            updateSliderValue((NumberSetting) draggedSetting, mouseX, sliderX, sliderW);
        }
    }

    private void updateSliderValue(NumberSetting ns, int mouseX, int sliderX, int sliderW) {
        double pct = (double) (mouseX - sliderX) / sliderW;
        pct = Math.max(0, Math.min(1, pct));
        double range = ns.getMax() - ns.getMin();
        double rawValue = ns.getMin() + pct * range;
        double stepped = Math.round(rawValue / ns.getStep()) * ns.getStep();
        ns.setValue(stepped);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (binding) {
            if (expandedModule != null) {
                expandedModule.setKey(keyCode);
                binding = false;
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        com.zyrex.config.Config.save(moduleManager);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
