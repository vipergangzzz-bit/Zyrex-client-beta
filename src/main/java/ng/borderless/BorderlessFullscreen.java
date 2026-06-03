package ng.skyiswinni.borderless;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;

import java.awt.*;

@Mod(modid = BorderlessFullscreen.MODID, version = BorderlessFullscreen.VERSION, acceptedMinecraftVersions = "*")
public class BorderlessFullscreen {

    public static final String MODID = "borderlessfullscreen";
    public static final String VERSION = "1.0";

    private static boolean fakeFullscreen = false;

    private static int prevX;
    private static int prevY;
    private static int prevWidth;
    private static int prevHeight;
    private static DisplayMode prevDisplayMode;

    private static final WinDef.HWND HWND_TOPMOST =
            new WinDef.HWND(Pointer.createConstant(-1));

    private static final WinDef.HWND HWND_NOTOPMOST =
            new WinDef.HWND(Pointer.createConstant(-2));

    private static final int GWL_STYLE = -16;
    private static final int WS_OVERLAPPEDWINDOW = 0x00CF0000;

    @SuppressWarnings("unused")
    public static boolean apply() {

        Minecraft minecraft = Minecraft.getMinecraft();

        boolean grabbed = Mouse.isGrabbed();
        if (grabbed) Mouse.setGrabbed(false);

        try {
            if (!fakeFullscreen) {

                prevX = Display.getX();
                prevY = Display.getY();
                prevWidth = Display.getWidth();
                prevHeight = Display.getHeight();
                prevDisplayMode = Display.getDisplayMode();

                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

                Display.setFullscreen(false);

                int width = screen.width;
                int height = screen.height + 1;

                Display.setDisplayMode(new DisplayMode(width, height));
                Display.setLocation(0, 0);

                WinDef.HWND hwnd = getHWND();

                int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_STYLE);
                style &= ~WS_OVERLAPPEDWINDOW;

                User32.INSTANCE.SetWindowLong(hwnd, GWL_STYLE, style);

                User32.INSTANCE.SetWindowPos(
                        hwnd,
                        HWND_TOPMOST,
                        0,
                        0,
                        width,
                        height,
                        0
                );

                minecraft.displayWidth = width;
                minecraft.displayHeight = height;

                WinDef.HWND shellHwnd = User32.INSTANCE.FindWindow("Shell_TrayWnd", null);
                User32.INSTANCE.SetForegroundWindow(shellHwnd);
                Thread.sleep(50);
                User32.INSTANCE.SetForegroundWindow(hwnd);

                fakeFullscreen = true;

            } else {

                Display.setFullscreen(false);

                Display.setDisplayMode(prevDisplayMode);
                Display.setLocation(prevX, prevY);

                WinDef.HWND hwnd = getHWND();

                int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_STYLE);
                style |= WS_OVERLAPPEDWINDOW;

                User32.INSTANCE.SetWindowLong(hwnd, GWL_STYLE, style);

                User32.INSTANCE.SetWindowPos(
                        hwnd,
                        HWND_NOTOPMOST,
                        prevX,
                        prevY,
                        prevWidth,
                        prevHeight,
                        0
                );

                minecraft.displayWidth = prevWidth;
                minecraft.displayHeight = prevHeight;

                fakeFullscreen = false;
            }

            minecraft.resize(minecraft.displayWidth, minecraft.displayHeight);

            minecraft.updateDisplay();

            Mouse.setCursorPosition(
                    minecraft.displayWidth / 2,
                    minecraft.displayHeight / 2
            );

            if (grabbed) Mouse.setGrabbed(true);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static WinDef.HWND getHWND() {
        try {

            Class<?> displayClass = Class.forName("org.lwjgl.opengl.Display");
            java.lang.reflect.Field displayField = displayClass.getDeclaredField("display_impl");
            displayField.setAccessible(true);

            Object displayImpl = displayField.get(null);

            java.lang.reflect.Method hwndMethod =
                    displayImpl.getClass().getDeclaredMethod("getHwnd");

            hwndMethod.setAccessible(true);

            long hwnd = (long) hwndMethod.invoke(displayImpl);

            return new WinDef.HWND(Pointer.createConstant(hwnd));

        } catch (Exception e) {
            throw new RuntimeException("Failed to get HWND from LWJGL", e);
        }
    }
}