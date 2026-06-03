package com.zyrex.client.gui;

import com.zyrex.ZyRex;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class LoginScreen extends GuiScreen {

    private static final int ACCENT       = 0xFF9B4DCA;
    private static final int ACCENT_DIM   = 0xFF6B2D9A;
    private static final int ACCENT_GLOW  = 0x409B4DCA;
    private static final int BG           = 0xFF050505;
    private static final int TEXT         = 0xFFEEEEEE;
    private static final int MUTED        = 0xFF888888;
    private static final int DIM          = 0xFF555555;
    private static final int INPUT_BG     = 0xFF0A0A0A;
    private static final int INPUT_FOCUS  = 0xFF9B4DCA;

    private static final int FW = 300;
    private static final int FH = 36;

    private static final String SUPABASE_URL = "https://xdgdsvjsutqakagvcaci.supabase.co";
    private static final String ANON_KEY = "sb_publishable_kfU1tZo27CmEp0wAG-hy7w_U9jWEbjr";

    private final ZyRex mod;
    private final Random random = new Random();

    private GuiTextField usernameField;
    private GuiTextField passwordField;
    private String statusText = "";
    private int statusColor = ACCENT;
    private boolean loggingIn;
    private volatile Object loginResult;
    private Thread loginThread;
    private int tick;

    private float[] starField;
    private static final int STAR_COUNT = 120;

    private String savedUser = "";
    private String savedPass = "";
    private boolean savedUserFocus = true;

    public LoginScreen(ZyRex mod) {
        this.mod = mod;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        starField = new float[STAR_COUNT * 3];
        for (int i = 0; i < STAR_COUNT * 3; i++) {
            starField[i] = random.nextFloat();
        }

        int fx = (width - FW) / 2;

        usernameField = new GuiTextField(0, mc.fontRendererObj, fx, height / 2 - 18, FW, FH);
        usernameField.setMaxStringLength(24);
        usernameField.setEnableBackgroundDrawing(false);
        usernameField.setTextColor(TEXT);
        usernameField.setText(savedUser);
        usernameField.setFocused(savedUserFocus);

        passwordField = new GuiTextField(0, mc.fontRendererObj, fx, height / 2 + 44, FW, FH);
        passwordField.setMaxStringLength(24);
        passwordField.setEnableBackgroundDrawing(false);
        passwordField.setTextColor(TEXT);
        passwordField.setText(savedPass);
        passwordField.setFocused(!savedUserFocus);

        loggingIn = false;
        statusText = "";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick++;

        drawBackground(0);
        Gui.drawRect(0, 0, width, height, BG);

        drawStars();

        int cx = width / 2;

        float pulse = (float) (Math.sin(tick * 0.05) * 0.12 + 0.88);
        String title = "ZyRex";
        int tw = mc.fontRendererObj.getStringWidth(title);
        int tx = cx - tw / 2;
        for (int i = 0; i < 8; i++) {
            int g = (int) (16 * pulse * (1.0 - i / 8.0));
            Gui.drawRect(tx - i * 2, height / 2 - 100 + i, tx + tw + i * 2, height / 2 - 99 + i,
                    (g << 24) | 0x9B4DCA);
        }
        mc.fontRendererObj.drawString(title, tx, height / 2 - 100, ACCENT, false);

        String sub = "your gateway";
        int sw = mc.fontRendererObj.getStringWidth(sub);
        mc.fontRendererObj.drawString(sub, cx - sw / 2, height / 2 - 76, 0xFF555555, false);

        Gui.drawRect(cx - 130, height / 2 - 64, cx + 130, height / 2 - 63, 0xFF181818);

        int fx = usernameField.xPosition;
        int fyU = height / 2 - 18;
        int fyP = height / 2 + 44;

        int userLabelColor = (usernameField.isFocused() ? ACCENT : MUTED);
        mc.fontRendererObj.drawString("USERNAME", cx - mc.fontRendererObj.getStringWidth("USERNAME") / 2, fyU - 18, userLabelColor, false);

        drawField(fx, fyU, FW, FH, usernameField.isFocused(),
                mouseX >= fx && mouseX <= fx + FW && mouseY >= fyU && mouseY <= fyU + FH);
        usernameField.drawTextBox();

        int passLabelColor = (passwordField.isFocused() ? ACCENT : MUTED);
        mc.fontRendererObj.drawString("PASSWORD", cx - mc.fontRendererObj.getStringWidth("PASSWORD") / 2, fyP - 18, passLabelColor, false);

        drawField(fx, fyP, FW, FH, passwordField.isFocused(),
                mouseX >= fx && mouseX <= fx + FW && mouseY >= fyP && mouseY <= fyP + FH);
        drawMaskedPassword();

        if (!statusText.isEmpty()) {
            mc.fontRendererObj.drawString(statusText,
                    cx - mc.fontRendererObj.getStringWidth(statusText) / 2,
                    height / 2 + 100, statusColor, false);
        }

        drawLoginButton(mouseX, mouseY);

        String ver = "ZyRex v" + ZyRex.VERSION;
        mc.fontRendererObj.drawString(ver,
                cx - mc.fontRendererObj.getStringWidth(ver) / 2,
                height - 22, 0xFF222222, false);

        if (loginResult != null) {
            String result = loginResult.toString();
            if (result.equals("no")) {
                loggingIn = false;
                loginResult = null;
                loginThread = null;
                statusText = "Invalid credentials";
                statusColor = 0xFFFF5555;
                passwordField.setText("");
                passwordField.setFocused(true);
            } else {
                loggingIn = false;
                loginResult = null;
                String[] parts = result.split(",");
                boolean dev = parts.length > 0 && parts[0].equals("yes");
                boolean op = parts.length > 1 && parts[1].equals("yes");
                mod.onLogin(usernameField.getText(), dev, op);
            }
        }
    }

    private void drawStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            int idx = i * 3;
            float x = starField[idx] * width;
            float y = starField[idx + 1] * height;
            float s = starField[idx + 2] * 1.5f + 0.5f;
            float bright = (float) (Math.sin(tick * 0.01 + i) * 0.3 + 0.7);
            int a = (int) (bright * 180);
            Gui.drawRect((int) x, (int) y, (int) (x + s), (int) (y + s), (a << 24) | 0xFFFFFF);
        }
    }

    private void drawField(int x, int y, int w, int h, boolean focused, boolean hovered) {
        Gui.drawRect(x, y, x + w, y + h, INPUT_BG);
        int lineColor = focused ? INPUT_FOCUS : (hovered ? ACCENT : 0xFF222222);
        int lineH = focused ? 2 : 1;
        Gui.drawRect(x, y + h - lineH, x + w, y + h, lineColor);
        if (focused) {
            Gui.drawRect(x, y + h, x + w, y + h + 3, ACCENT_GLOW);
        }
    }

    private void drawMaskedPassword() {
        String raw = passwordField.getText();
        if (raw.isEmpty()) return;
        String masked = new String(new char[raw.length()]).replace('\0', '*');
        int tx = passwordField.xPosition + 4;
        int ty = passwordField.yPosition + (passwordField.height - 8) / 2;
        mc.fontRendererObj.drawString(masked, tx, ty, TEXT);

        if (passwordField.isFocused() && (tick / 6) % 2 == 0) {
            int cursorX = tx + mc.fontRendererObj.getStringWidth(masked);
            Gui.drawRect(cursorX, ty, cursorX + 1, ty + 8, TEXT);
        }
    }

    private void drawLoginButton(int mouseX, int mouseY) {
        int bw = FW, bh = 40;
        int bx = (width - bw) / 2;
        int by = height / 2 + 130;
        boolean hovered = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;

        float glow = (float) (Math.sin(tick * 0.08) * 0.15 + 0.85);
        int pulseAlpha = (int) (30 * glow);

        if (!loggingIn && hovered) {
            Gui.drawRect(bx - 2, by - 2, bx + bw + 2, by + bh + 2, (pulseAlpha << 24) | 0x9B4DCA);
        }

        int topColor = loggingIn ? ACCENT_DIM : (hovered ? ACCENT : 0xFF7B3DAA);
        int botColor = loggingIn ? ACCENT_DIM : (hovered ? 0xFF6B2D9A : 0xFF5B2D8A);
        DrawUtil.drawVerticalGradient(bx, by, bx + bw, by + bh, topColor, botColor);
        Gui.drawRect(bx, by + bh - 1, bx + bw, by + bh, 0x40000000);

        String label = loggingIn ? "AUTHENTICATING\u2026" : "LOGIN";
        if (!loggingIn && hovered) label = "LOGIN \u25B6";
        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, bx + (bw - lw) / 2, by + (bh - mc.fontRendererObj.FONT_HEIGHT) / 2,
                loggingIn ? DIM : TEXT, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (loggingIn) return;

        usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        passwordField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            boolean focusUser = mouseX >= usernameField.xPosition && mouseX <= usernameField.xPosition + usernameField.width
                    && mouseY >= height / 2 - 18 && mouseY <= height / 2 - 18 + usernameField.height;
            boolean focusPass = mouseX >= passwordField.xPosition && mouseX <= passwordField.xPosition + passwordField.width
                    && mouseY >= height / 2 + 44 && mouseY <= height / 2 + 44 + passwordField.height;
            usernameField.setFocused(focusUser);
            passwordField.setFocused(focusPass);
        }

        int bx = (width - FW) / 2;
        int by = height / 2 + 130;
        if (mouseX >= bx && mouseX <= bx + FW && mouseY >= by && mouseY <= by + 40) {
            attemptLogin();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (loggingIn) return;

        if (keyCode == Keyboard.KEY_ESCAPE) return;

        if (keyCode == Keyboard.KEY_RETURN) {
            attemptLogin();
            return;
        }

        if (keyCode == Keyboard.KEY_TAB) {
            boolean u = usernameField.isFocused();
            usernameField.setFocused(!u);
            passwordField.setFocused(u);
            return;
        }

        usernameField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty()) {
            statusText = "Enter your username";
            statusColor = 0xFFFF5555;
            usernameField.setFocused(true);
            return;
        }

        if (pass.isEmpty()) {
            statusText = "Enter your password";
            statusColor = 0xFFFF5555;
            passwordField.setFocused(true);
            return;
        }

        loggingIn = true;
        loginResult = null;
        statusText = "Authenticating\u2026";
        statusColor = ACCENT;

        final String fUser = user;
        final String fPass = pass;

        loginThread = new Thread(() -> {
            try {
                loginResult = checkCredentials(fUser, fPass);
            } catch (Exception e) {
                System.out.println("[ZyRex] Supabase error: " + e);
                loginResult = "no";
            }
        });
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private String checkCredentials(String user, String pass) throws IOException {
        URL url = new URL(SUPABASE_URL + "/rest/v1/rpc/check_creds");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + ANON_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        String body = "{\"p_user\":\"" + escapeJson(user) + "\",\"p_pass\":\"" + escapeJson(pass) + "\"}";
        OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream());
        w.write(body);
        w.close();

        int code = conn.getResponseCode();
        if (code == 200) {
            BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = r.readLine();
            r.close();
            if (line != null) {
                line = line.trim();
                if (line.startsWith("\"") && line.endsWith("\""))
                    line = line.substring(1, line.length() - 1);
                return line;
            }
            return "no";
        }
        return "no";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (usernameField != null) {
            savedUser = usernameField.getText();
            savedPass = passwordField.getText();
            savedUserFocus = usernameField.isFocused();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
