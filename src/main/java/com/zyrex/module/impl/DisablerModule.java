package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DisablerModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Transaction", "Packet", "Hypixel", "Verus");

    private int ticks;

    public DisablerModule() {
        super("Disabler", Category.Exploit);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        ticks++;
        String m = mode.getValue();

        if (m.equals("Packet")) {
            if (ticks % 5 == 0) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            }
        } else if (m.equals("Hypixel")) {
            if (ticks % 3 == 0) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-10, mc.thePlayer.posZ, true));
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX, mc.thePlayer.posY - 1.0E-10, mc.thePlayer.posZ, true));
            }
        } else if (m.equals("Transaction")) {
            if (ticks % 20 == 0) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0FPacketConfirmTransaction(0, (short) 0, false));
            }
        } else if (m.equals("Verus")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-10, mc.thePlayer.posZ, false));
        }
    }
}
