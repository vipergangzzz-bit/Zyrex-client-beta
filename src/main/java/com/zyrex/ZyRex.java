package com.zyrex;

import com.zyrex.client.clickgui.ClickGUI;
import com.zyrex.client.gui.HomeScreen;
import com.zyrex.client.gui.LoginScreen;
import com.zyrex.config.Config;
import com.zyrex.module.Module;
import com.zyrex.module.ModuleManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = ZyRex.MODID, version = ZyRex.VERSION)
public class ZyRex {
    public static final String MODID = "zyrex";
    public static final String VERSION = "1.0";

    public static final ModuleManager moduleManager = new ModuleManager();
    private final ClickGUI clickGUI = new ClickGUI(moduleManager);
    private final LoginScreen loginScreen;
    private final HomeScreen homeScreen;
    private boolean wasRShiftDown;
    private final Map<Integer, Boolean> keyStates = new HashMap<Integer, Boolean>();
    private int saveCounter;
    private boolean loggedIn;
    public boolean showMainMenu;
    public boolean hasOverpowered;

    public ZyRex() {
        loginScreen = new LoginScreen(this);
        homeScreen = new HomeScreen(this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        for (Module m : moduleManager.modules) {
            MinecraftForge.EVENT_BUS.register(m);
        }
        Config.load(moduleManager);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        System.out.println("[ZyRex] GuiOpenEvent: " + (event.gui != null ? event.gui.getClass().getName() : "null"));
        if (event.gui instanceof GuiMainMenu) {
            if (showMainMenu) {
                showMainMenu = false;
                return;
            }
            event.gui = loggedIn ? homeScreen : loginScreen;
        } else if (!loggedIn && event.gui == null) {
            event.gui = loginScreen;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        boolean rShiftDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (rShiftDown && !wasRShiftDown) {
            if (mc.currentScreen instanceof ClickGUI
                    || mc.currentScreen instanceof HomeScreen) {
                mc.displayGuiScreen(null);
            } else if (mc.currentScreen == null) {
                if (!loggedIn) {
                    mc.displayGuiScreen(loginScreen);
                } else {
                    clickGUI.setShowOverPowered(hasOverpowered);
                    clickGUI.setPreviousScreen(null);
                    mc.displayGuiScreen(clickGUI);
                }
            }
        }
        wasRShiftDown = rShiftDown;

        if (mc.currentScreen == null) {
            for (Module m : moduleManager.modules) {
                int k = m.getKey();
                if (k == -1) continue;
                boolean down = Keyboard.isKeyDown(k);
                Boolean was = keyStates.get(k);
                if (down && (was == null || !was)) {
                    m.toggle();
                }
                keyStates.put(k, down);
            }
        }

        saveCounter++;
        if (saveCounter >= 300) {
            saveCounter = 0;
            Config.save(moduleManager);
        }
    }

    public void onLogin(String username, boolean dev, boolean overpowered) {
        this.loggedIn = true;
        this.hasOverpowered = overpowered;
        homeScreen.setUsername(username);
        Minecraft.getMinecraft().displayGuiScreen(homeScreen);
    }

    public ClickGUI getClickGUI() {
        return clickGUI;
    }
}
