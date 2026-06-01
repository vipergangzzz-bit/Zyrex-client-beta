package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CritModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Jump", "Mini Jump");
    private final NumberSetting chance = new NumberSetting("Chance", 100, 1, 100, 1);
    private final ModeSetting onlyGround = new ModeSetting("Only Ground", "On", "Off");

    private int ticksSinceAttack;
    private boolean attacked;
    private boolean injected;

    public CritModule() {
        super("Crit", Category.Combat);
        addSetting(mode);
        addSetting(chance);
        addSetting(onlyGround);
    }

    @Override
    protected void onEnable() {
        injectHandler();
    }

    @Override
    protected void onDisable() {
        attacked = false;
        ticksSinceAttack = 0;
        removeHandler();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (attacked) ticksSinceAttack++;
        if (ticksSinceAttack > 3) {
            attacked = false;
            ticksSinceAttack = 0;
        }

        if (!attacked) return;

        if (onlyGround.getValue().equals("On") && !mc.thePlayer.onGround) return;
        if (Math.random() * 100 > chance.getValue()) return;

        String m = mode.getValue();
        if (m.equals("Jump")) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                mc.thePlayer.motionY = 0.42;
            }
            attacked = false;
            ticksSinceAttack = 0;
        } else if (m.equals("Mini Jump")) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.11;
                mc.thePlayer.onGround = false;
            }
            attacked = false;
            ticksSinceAttack = 0;
        }
    }

    private void injectHandler() {
        if (injected) return;
        if (mc.getNetHandler() == null) return;

        ChannelPipeline pipeline = mc.getNetHandler().getNetworkManager().channel().pipeline();
        if (pipeline == null || pipeline.get("crit_handler") != null) return;

        pipeline.addBefore("packet_handler", "crit_handler", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof C02PacketUseEntity) {
                    C02PacketUseEntity packet = (C02PacketUseEntity) msg;
                    if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                        if (!mode.getValue().equals("Packet")) {
                            attacked = true;
                            ticksSinceAttack = 0;
                            super.write(ctx, msg, promise);
                            return;
                        }
                        if (onlyGround.getValue().equals("On") && !mc.thePlayer.onGround) {
                            super.write(ctx, msg, promise);
                            return;
                        }
                        if (Math.random() * 100 > chance.getValue()) {
                            super.write(ctx, msg, promise);
                            return;
                        }
                        double x = mc.thePlayer.posX;
                        double y = mc.thePlayer.posY;
                        double z = mc.thePlayer.posZ;
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.0625, z, false));
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 1.1E-5, z, false));
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                    }
                }
                super.write(ctx, msg, promise);
            }
        });

        injected = true;
    }

    private void removeHandler() {
        if (!injected || mc.getNetHandler() == null) return;

        ChannelPipeline pipeline = mc.getNetHandler().getNetworkManager().channel().pipeline();
        if (pipeline == null) return;

        try {
            pipeline.remove("crit_handler");
        } catch (Exception e) { }

        injected = false;
    }
}
