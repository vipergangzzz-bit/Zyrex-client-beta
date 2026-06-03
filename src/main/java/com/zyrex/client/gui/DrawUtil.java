package com.zyrex.client.gui;

import net.minecraft.client.gui.Gui;

public class DrawUtil {

    public static void drawVerticalGradient(int x1, int y1, int x2, int y2, int colorA, int colorB) {
        int a1 = (colorA >> 24) & 0xFF, r1 = (colorA >> 16) & 0xFF, g1 = (colorA >> 8) & 0xFF, b1 = colorA & 0xFF;
        int a2 = (colorB >> 24) & 0xFF, r2 = (colorB >> 16) & 0xFF, g2 = (colorB >> 8) & 0xFF, b2 = colorB & 0xFF;
        for (int y = y1; y < y2; y++) {
            float t = (float) (y - y1) / (y2 - y1);
            int a = (int) (a1 + (a2 - a1) * t);
            int r = (int) (r1 + (r2 - r1) * t);
            int g = (int) (g1 + (g2 - g1) * t);
            int b = (int) (b1 + (b2 - b1) * t);
            Gui.drawRect(x1, y, x2, y + 1, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

}
