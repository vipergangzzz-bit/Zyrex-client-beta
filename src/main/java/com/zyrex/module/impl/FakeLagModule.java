package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakeLagModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "All", "Movement", "Filter");
    private final NumberSetting lagTicks = new NumberSetting("Lag Ticks", 10, 2, 40, 1);

    private final ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<Packet>();
    private int ticks;

    public FakeLagModule() {
        super("Fake Lag", Category.Exploit);
        addSetting(mode);
        addSetting(lagTicks);
    }

    @Override
    protected void onDisable() {
        flushPackets();
    }

    public boolean shouldLagPacket(Packet<?> packet) {
        if (!isEnabled()) return false;

        String m = mode.getValue();
        if (m.equals("All")) return true;

        if (m.equals("Movement") && packet instanceof C03PacketPlayer) return true;

        if (m.equals("Filter")) {
            if (packet instanceof C03PacketPlayer || packet instanceof C0FPacketConfirmTransaction
                || packet instanceof C0BPacketEntityAction) return true;
        }
        return false;
    }

    public void queuePacket(Packet<?> packet) {
        packetQueue.add(packet);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        ticks++;
        if (ticks >= lagTicks.getValue()) {
            flushPackets();
            ticks = 0;
        }
    }

    private void flushPackets() {
        Packet packet;
        while ((packet = packetQueue.poll()) != null) {
            mc.thePlayer.sendQueue.addToSendQueue(packet);
        }
    }
}
