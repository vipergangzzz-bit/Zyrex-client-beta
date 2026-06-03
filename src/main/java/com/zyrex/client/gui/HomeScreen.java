package com.zyrex.client.gui;

import com.zyrex.ZyRex;
import java.util.Random;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;

public class HomeScreen extends GuiScreen {

    private static final int ACCENT       = 0xFF9B4DCA;
    private static final int BG           = 0xFF050505;
    private static final int TEXT         = 0xFFEEEEEE;
    private static final int MUTED        = 0xFF888888;
    private static final int BUTTON_BG    = 0xFF0A0A0A;

    private static final int BW = 280;
    private static final int BH = 42;
    private static final int IAS_W = 80;

    private final ZyRex mod;
    private final Random random = new Random();
    private String username;
    private int tick;

    private float[] starField;
    private static final int STAR_COUNT = 100;

    public HomeScreen(ZyRex mod) {
        this.mod = mod;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void initGui() {
        starField = new float[STAR_COUNT * 3];
        for (int i = 0; i < STAR_COUNT * 3; i++) {
            starField[i] = random.nextFloat();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick++;

        drawBackground(0);
        Gui.drawRect(0, 0, width, height, BG);

        for (int i = 0; i < STAR_COUNT; i++) {
            int idx = i * 3;
            float x = starField[idx] * width;
            float y = starField[idx + 1] * height;
            float s = starField[idx + 2] * 1.5f + 0.5f;
            float bright = (float) (Math.sin(tick * 0.01 + i) * 0.3 + 0.7);
            int a = (int) (bright * 160);
            Gui.drawRect((int) x, (int) y, (int) (x + s), (int) (y + s), (a << 24) | 0xFFFFFF);
        }

        int cx = width / 2;

        float pulse = (float) (Math.sin(tick * 0.05) * 0.12 + 0.88);
        String title = "ZyRex";
        int tw = mc.fontRendererObj.getStringWidth(title);
        int tx = cx - tw / 2;
        for (int i = 0; i < 8; i++) {
            int g = (int) (16 * pulse * (1.0 - i / 8.0));
            Gui.drawRect(tx - i * 2, 30 + i, tx + tw + i * 2, 31 + i,
                    (g << 24) | 0x9B4DCA);
        }
        mc.fontRendererObj.drawString(title, tx, 30, ACCENT, false);

        String welcome = "Welcome, " + username + ".";
        mc.fontRendererObj.drawString(welcome, cx - mc.fontRendererObj.getStringWidth(welcome) / 2, 58, MUTED, false);

        Gui.drawRect(cx - 140, 74, cx + 140, 75, 0xFF181818);

        int bx = (width - BW) / 2;
        int cgw = BW - 8 - IAS_W;

        drawButton("Singleplayer", bx, 110, true, mouseX, mouseY);
        drawButton("Multiplayer", bx, 162, true, mouseX, mouseY);
        drawButton("Open ClickGUI \u25B6", bx, 214, true, cgw, mouseX, mouseY);
        drawSmallButton("IAS", bx + cgw + 4, 214, IAS_W, mouseX, mouseY);

        if (mc.theWorld != null) {
            drawButton("Back to Game", bx, 266, false, mouseX, mouseY);
        }

        String ver = "ZyRex v" + ZyRex.VERSION;
        mc.fontRendererObj.drawString(ver,
                cx - mc.fontRendererObj.getStringWidth(ver) / 2,
                height - 22, 0xFF222222, false);

        String debug = "[Debug Menu]";
        mc.fontRendererObj.drawString(debug, 4, height - 12, 0xFF444444, false);
    }

    private void drawButton(String label, int x, int y, boolean primary, int mx, int my) {
        drawButton(label, x, y, primary, BW, mx, my);
    }

    private void drawButton(String label, int x, int y, boolean primary, int w, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + BH;

        if (primary) {
            float pulse = (float) (Math.sin(tick * 0.08) * 0.12 + 0.88);
            int glowAlpha = (int) (25 * pulse);
            if (hovered) {
                Gui.drawRect(x - 2, y - 2, x + w + 2, y + BH + 2, (glowAlpha << 24) | 0x9B4DCA);
            }
            int topColor = hovered ? ACCENT : 0xFF7B3DAA;
            int botColor = hovered ? 0xFF6B2D9A : 0xFF5B2D8A;
            DrawUtil.drawVerticalGradient(x, y, x + w, y + BH, topColor, botColor);
            Gui.drawRect(x, y + BH - 1, x + w, y + BH, 0x40000000);
        } else {
            Gui.drawRect(x, y, x + w, y + BH, hovered ? 0xFF181818 : BUTTON_BG);
            Gui.drawRect(x, y, x + w, y + 1, hovered ? ACCENT : 0xFF222222);
        }

        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, x + (w - lw) / 2, y + (BH - mc.fontRendererObj.FONT_HEIGHT) / 2,
                primary ? TEXT : (hovered ? TEXT : MUTED), false);
    }

    private void drawSmallButton(String label, int x, int y, int w, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + BH;

        Gui.drawRect(x, y, x + w, y + BH, hovered ? 0xFF2A2A2A : 0xFF111111);
        Gui.drawRect(x, y, x + w, y + 1, hovered ? ACCENT : 0xFF333333);

        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, x + (w - lw) / 2, y + (BH - mc.fontRendererObj.FONT_HEIGHT) / 2,
                hovered ? TEXT : MUTED, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int bx = (width - BW) / 2;
        int cgw = BW - 8 - IAS_W;

        if (mouseX >= bx && mouseX <= bx + BW && mouseY >= 110 && mouseY <= 110 + BH) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
            return;
        }

        if (mouseX >= bx && mouseX <= bx + BW && mouseY >= 162 && mouseY <= 162 + BH) {
            mc.displayGuiScreen(new GuiMultiplayer(this));
            return;
        }

        if (mouseX >= bx && mouseX <= bx + cgw && mouseY >= 214 && mouseY <= 214 + BH) {
            mod.getClickGUI().setShowOverPowered(mod.hasOverpowered);
            mod.getClickGUI().setPreviousScreen(this);
            mc.displayGuiScreen(mod.getClickGUI());
            return;
        }

        int iasX = bx + cgw + 4;
        if (mouseX >= iasX && mouseX <= iasX + IAS_W && mouseY >= 214 && mouseY <= 214 + BH) {
            try {
                Class<?> cls = Class.forName("the_fireplace.ias.gui.AccountListScreen");
                java.lang.reflect.Constructor<?> ctor = cls.getConstructor(GuiScreen.class);
                mc.displayGuiScreen((GuiScreen) ctor.newInstance(this));
            } catch (Exception e) {
                System.out.println("[ZyRex] IAS open failed: " + e);
            }
            return;
        }

        if (mc.theWorld != null
                && mouseX >= bx && mouseX <= bx + BW && mouseY >= 266 && mouseY <= 266 + BH) {
            mc.displayGuiScreen(null);
            return;
        }

        if (mouseX >= 4 && mouseX <= 4 + mc.fontRendererObj.getStringWidth("[Debug Menu]")
                && mouseY >= height - 12 && mouseY <= height - 12 + mc.fontRendererObj.FONT_HEIGHT) {
            mod.showMainMenu = true;
            mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) return;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
