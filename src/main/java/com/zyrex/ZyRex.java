package com.zyrex;

import com.zyrex.client.clickgui.ClickGUI;
import com.zyrex.config.Config;
import com.zyrex.module.Module;
import com.zyrex.module.ModuleManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
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
    private boolean wasRShiftDown;
    private final Map<Integer, Boolean> keyStates = new HashMap<Integer, Boolean>();
    private int saveCounter;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        for (Module m : moduleManager.modules) {
            MinecraftForge.EVENT_BUS.register(m);
        }
        Config.load(moduleManager);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        // ClickGUI toggle
        boolean rShiftDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (rShiftDown && !wasRShiftDown) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen instanceof ClickGUI) {
                mc.displayGuiScreen(null);
            } else {
                mc.displayGuiScreen(clickGUI);
            }
        }
        wasRShiftDown = rShiftDown;

        // module keybinds (ignore if in any GUI)
        if (Minecraft.getMinecraft().currentScreen == null) {
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

        // auto-save every 300 ticks (15 seconds)
        saveCounter++;
        if (saveCounter >= 300) {
            saveCounter = 0;
            Config.save(moduleManager);
        }
    }
}
