package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FlightModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Creative", "Glide", "Hypixel", "Collision");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
    private final NumberSetting glideSpeed = new NumberSetting("Glide Speed", 0.05, 0.01, 0.5, 0.01);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", 1.0, 0.1, 5.0, 0.1);

    private int ticks;

    public FlightModule() {
        super("Flight", Category.Movement);
        addSetting(mode);
        addSetting(speed);
        addSetting(glideSpeed);
        addSetting(verticalSpeed);
    }

    @Override
    protected void onEnable() {
        ticks = 0;
    }

    @Override
    protected void onDisable() {
        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.noClip = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        ticks++;
        String m = mode.getValue();
        float s = (float)(speed.getValue()) * 2.0f;
        float vs = (float)(verticalSpeed.getValue()) * 2.0f;

        if (m.equals("Vanilla")) {
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.capabilities.isFlying = true;
            mc.thePlayer.capabilities.setFlySpeed(s * 0.05f);
            if (mc.gameSettings.keyBindJump.isKeyDown()) mc.thePlayer.motionY = vs * 0.3;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) mc.thePlayer.motionY = -vs * 0.3;
        } else if (m.equals("Creative")) {
            mc.thePlayer.capabilities.isFlying = true;
            mc.thePlayer.capabilities.setFlySpeed(s * 0.05f);
        } else if (m.equals("Glide")) {
            if (mc.thePlayer.motionY < 0)
                mc.thePlayer.motionY = -(float)(glideSpeed.getValue()) * 2.0f;
            if (mc.gameSettings.keyBindJump.isKeyDown()) mc.thePlayer.motionY = vs * 0.3;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) mc.thePlayer.motionY = -vs * 0.3;
        } else if (m.equals("Hypixel")) {
            mc.thePlayer.capabilities.isFlying = true;
            mc.thePlayer.capabilities.setFlySpeed(s * 0.05f);
            if (mc.thePlayer.ticksExisted % 5 == 0)
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
        } else if (m.equals("Collision")) {
            mc.thePlayer.noClip = true;
            mc.thePlayer.motionY = 0;
            if (mc.gameSettings.keyBindJump.isKeyDown()) mc.thePlayer.motionY = vs * 0.3;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) mc.thePlayer.motionY = -vs * 0.3;
        }
    }
}
