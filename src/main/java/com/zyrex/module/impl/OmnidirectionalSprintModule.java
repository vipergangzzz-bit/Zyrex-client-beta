package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class OmnidirectionalSprintModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Silent");

    public OmnidirectionalSprintModule() {
        super("Omni Sprint", Category.Movement);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mode.getValue().equals("Normal")) {
            if (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0) {
                if (!mc.thePlayer.isUsingItem() && !mc.thePlayer.isSneaking()) {
                    mc.thePlayer.setSprinting(true);
                }
            }
        } else if (mode.getValue().equals("Silent")) {
            float forward = mc.thePlayer.moveForward;
            float strafe = mc.thePlayer.moveStrafing;
            if (forward == 0 && strafe == 0) return;
            if (mc.thePlayer.isUsingItem() || mc.thePlayer.isSneaking()) return;
            mc.thePlayer.setSprinting(true);
        }
    }
}
