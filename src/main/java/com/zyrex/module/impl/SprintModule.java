package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SprintModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Legit", "Omni", "Blatant");

    public SprintModule() {
        super("Sprint", Category.Movement);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        String m = mode.getValue();
        if (m.equals("Legit")) {
            if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isUsingItem()) {
                mc.thePlayer.setSprinting(true);
            }
        } else if (m.equals("Omni")) {
            if ((mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0) && !mc.thePlayer.isUsingItem()) {
                mc.thePlayer.setSprinting(true);
            }
        } else if (m.equals("Blatant")) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
