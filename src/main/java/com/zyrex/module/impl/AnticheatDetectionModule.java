package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AnticheatDetectionModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog", "NCP", "AAC", "All");

    private boolean flagged;
    private int flagTicks;
    private String lastFlag;

    public AnticheatDetectionModule() {
        super("Anti Cheat Detection", Category.Player);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        String m = mode.getValue();
        boolean any = m.equals("All");

        if (any || m.equals("Watchdog")) {
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.hurtTime < 5) {
                if (Math.abs(mc.thePlayer.motionY) > 0.4) {
                    flag("Watchdog Velocity");
                }
            }
        }

        if (flagTicks > 0) flagTicks--;
        else flagged = false;
    }

    private void flag(String type) {
        flagged = true;
        lastFlag = type;
        flagTicks = 100;
    }

    public boolean isFlagged() { return flagged; }
    public String getLastFlag() { return lastFlag; }
}
