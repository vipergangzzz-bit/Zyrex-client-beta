package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PhaseModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "AAC", "Vanilla", "Hypixel");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 3.0, 0.1);
    private final NumberSetting depth = new NumberSetting("Depth", 1, 1, 5, 1);

    public PhaseModule() {
        super("Phase", Category.Movement);
        addSetting(mode);
        addSetting(speed);
        addSetting(depth);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        String m = mode.getValue();
        float s = (float)(speed.getValue());
        int d = (int)(depth.getValue());

        if (m.equals("AAC")) {
            if (mc.thePlayer.isCollidedHorizontally) {
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                mc.thePlayer.setPosition(mc.thePlayer.posX - Math.sin(yaw) * s * 0.05, mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(yaw) * s * 0.05);
            }
        } else if (m.equals("Vanilla")) {
            if (mc.thePlayer.isCollidedHorizontally) {
                mc.thePlayer.noClip = true;
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                mc.thePlayer.motionX = -Math.sin(yaw) * s * 0.2;
                mc.thePlayer.motionZ = Math.cos(yaw) * s * 0.2;
            }
        } else if (m.equals("Hypixel")) {
            if (mc.thePlayer.isCollidedHorizontally) {
                for (int i = 0; i < d; i++) {
                    double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                    double x = mc.thePlayer.posX - Math.sin(yaw) * s * 0.5;
                    double z = mc.thePlayer.posZ + Math.cos(yaw) * s * 0.5;
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, mc.thePlayer.posY, z, true));
                }
            }
        }
    }
}
