package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MoveFixModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Silent", "Strict");

    public MoveFixModule() {
        super("Move Fix", Category.Movement);
        addSetting(mode);
    }

    public float[] fixRotation(float yaw, float pitch) {
        if (!isEnabled()) return new float[]{yaw, pitch};
        float[] fixed = new float[2];

        if (mode.getValue().equals("Silent")) {
            float diff = yaw - mc.thePlayer.rotationYaw;
            fixed[0] = mc.thePlayer.rotationYaw + diff;
            fixed[1] = mc.thePlayer.rotationPitch;
            if (pitch > 90 || pitch < -90) pitch = mc.thePlayer.rotationPitch;
        } else {
            fixed[0] = mc.thePlayer.rotationYaw;
            fixed[1] = mc.thePlayer.rotationPitch;
        }
        return fixed;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) { }
}
