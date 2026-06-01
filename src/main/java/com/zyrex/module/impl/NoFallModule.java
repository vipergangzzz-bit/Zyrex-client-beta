package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoFallModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Edit", "Hypixel");

    public NoFallModule() {
        super("No Fall", Category.Movement);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mc.thePlayer.fallDistance > 3) {
            String m = mode.getValue();
            if (m.equals("Packet")) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            } else if (m.equals("Edit")) {
                mc.thePlayer.fallDistance = 0;
                mc.thePlayer.motionY = 0;
            } else if (m.equals("Hypixel")) {
                if (mc.thePlayer.ticksExisted % 3 == 0)
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ, true));
            }
        }
    }
}
