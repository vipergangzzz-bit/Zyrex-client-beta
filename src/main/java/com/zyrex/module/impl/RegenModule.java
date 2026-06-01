package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RegenModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Packet", "Hypixel");
    private final NumberSetting packets = new NumberSetting("Packets", 20, 1, 100, 1);
    private final NumberSetting health = new NumberSetting("Health", 8, 1, 20, 1);

    private boolean wasDamaged;
    private int damageTimer;

    public RegenModule() {
        super("Regen", Category.Player);
        addSetting(mode);
        addSetting(packets);
        addSetting(health);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mc.thePlayer.hurtTime > 0) {
            wasDamaged = true;
        }

        if (wasDamaged) {
            damageTimer++;
            if (damageTimer > 20) {
                wasDamaged = false;
                damageTimer = 0;
                return;
            }

            if (mc.thePlayer.getHealth() > health.getValue()) return;
            if (!mc.thePlayer.onGround) return;

            String m = mode.getValue();
            int p = (int)(packets.getValue());

            if (m.equals("Packet")) {
                for (int i = 0; i < p; i++) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                }
            } else if (m.equals("Hypixel")) {
                for (int i = 0; i < p / 2; i++) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                }
            }
        }
    }
}
