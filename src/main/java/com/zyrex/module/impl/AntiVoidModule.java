package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AntiVoidModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Motion", "Hypixel");
    private final NumberSetting fallDistance = new NumberSetting("Fall Distance", 5, 1, 20, 1);
    private final NumberSetting delay = new NumberSetting("Delay", 0, 0, 10, 1);

    private int ticks;

    public AntiVoidModule() {
        super("Anti Void", Category.Movement);
        addSetting(mode);
        addSetting(fallDistance);
        addSetting(delay);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        ticks++;

        if (mc.thePlayer.fallDistance < fallDistance.getValue()) return;
        if (ticks <= delay.getValue()) return;

        String m = mode.getValue();
        if (m.equals("Packet")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX, mc.thePlayer.posY + 5, mc.thePlayer.posZ, false));
        } else if (m.equals("Motion")) {
            mc.thePlayer.motionY = 0.5;
        } else if (m.equals("Hypixel")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            mc.thePlayer.motionY = 0.3;
        }
    }
}
