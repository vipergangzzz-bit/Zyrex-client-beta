package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class FreeLookModule extends Module {
    private static boolean freeLooking;
    private static float cameraYaw;
    private static float cameraPitch;
    private float originalYaw, originalPitch;
    private boolean wasPressed;

    public FreeLookModule() {
        super("FreeLook", Category.Player);
    }

    public static boolean isFreeLooking() { return freeLooking; }
    public static float getCameraYaw() { return cameraYaw; }
    public static float getCameraPitch() { return cameraPitch; }

    @Override
    protected void onEnable() {
        if (mc.thePlayer != null) {
            originalYaw = mc.thePlayer.rotationYaw;
            originalPitch = mc.thePlayer.rotationPitch;
            cameraYaw = mc.thePlayer.rotationYaw;
            cameraPitch = mc.thePlayer.rotationPitch;
        }
    }

    @Override
    protected void onDisable() {
        freeLooking = false;
        if (mc.thePlayer != null && wasPressed) {
            mc.thePlayer.rotationYaw = cameraYaw;
            mc.thePlayer.rotationPitch = cameraPitch;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        boolean pressed = Mouse.isButtonDown(2);

        if (pressed && !wasPressed) {
            freeLooking = !freeLooking;
            if (freeLooking) {
                cameraYaw = mc.thePlayer.rotationYaw;
                cameraPitch = mc.thePlayer.rotationPitch;
            } else {
                mc.thePlayer.rotationYaw = cameraYaw;
                mc.thePlayer.rotationPitch = cameraPitch;
            }
        }

        if (freeLooking) {
            cameraYaw += Mouse.getDX() * 0.3f;
            cameraPitch -= Mouse.getDY() * 0.3f;
            cameraPitch = Math.max(-90, Math.min(90, cameraPitch));
        }

        wasPressed = pressed;
    }
}
