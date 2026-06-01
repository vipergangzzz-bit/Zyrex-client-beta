package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SpeedModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Bunny Hop", "Ground");
    private final NumberSetting speed = new NumberSetting("Speed", 2.0, 0.1, 10.0, 0.1);
    private final NumberSetting timerSpeed = new NumberSetting("Timer", 1.0, 0.5, 3.0, 0.1);

    private double moveSpeed;

    public SpeedModule() {
        super("Speed", Category.Movement);
        addSetting(mode);
        addSetting(speed);
        addSetting(timerSpeed);
    }

    @Override
    protected void onEnable() {
        moveSpeed = 0.2873;
        setTimerSpeed((float)(timerSpeed.getValue()));
    }

    @Override
    protected void onDisable() {
        setTimerSpeed(1.0f);
    }

    private void setTimerSpeed(float speed) {
        try {
            java.lang.reflect.Field timerField = mc.getClass().getDeclaredField("timer");
            timerField.setAccessible(true);
            Object timer = timerField.get(mc);
            java.lang.reflect.Field speedField = timer.getClass().getDeclaredField("timerSpeed");
            speedField.setAccessible(true);
            speedField.setFloat(timer, speed);
        } catch (Exception e) { }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        setTimerSpeed((float)(timerSpeed.getValue()));

        if (mc.thePlayer.moveForward == 0 && mc.thePlayer.moveStrafing == 0) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            return;
        }

        if (mode.getValue().equals("Bunny Hop")) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                moveSpeed = getBaseSpeed() * speed.getValue();
            } else if (mc.thePlayer.motionY < -0.1) {
                moveSpeed = getBaseSpeed() * speed.getValue() * 0.8;
            }
            setMotion(moveSpeed);
        } else {
            if (mc.thePlayer.onGround) {
                setMotion(getBaseSpeed() * speed.getValue());
            }
        }
    }

    private double getBaseSpeed() {
        double base = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amp = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            base *= 1.0 + 0.2 * (amp + 1);
        }
        return base;
    }

    private void setMotion(double speed) {
        float forward = mc.thePlayer.moveForward;
        float strafe = mc.thePlayer.moveStrafing;
        float yaw = mc.thePlayer.rotationYaw;

        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        mc.thePlayer.motionX = (strafe * cos - forward * sin) * speed;
        mc.thePlayer.motionZ = (forward * cos + strafe * sin) * speed;
    }
}
