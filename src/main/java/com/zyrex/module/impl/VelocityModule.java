package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class VelocityModule extends Module {
    private boolean injected;

    public VelocityModule() {
        super("Velocity", Category.Combat);
    }

    @Override
    protected void onDisable() {
        removeHandler();
        injected = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) {
            if (injected) removeHandler();
            return;
        }
        if (!injected && mc.getNetHandler() != null) {
            injectHandler();
        }
    }

    private void injectHandler() {
        if (injected) return;
        if (mc.getNetHandler() == null) return;

        ChannelPipeline pipeline = mc.getNetHandler().getNetworkManager().channel().pipeline();
        if (pipeline == null || pipeline.get("velocity_handler") != null) return;

        pipeline.addBefore("packet_handler", "velocity_handler", new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof S12PacketEntityVelocity) {
                    if (((S12PacketEntityVelocity) msg).getEntityID() == mc.thePlayer.getEntityId()) return;
                } else if (msg instanceof S27PacketExplosion) {
                    return;
                }
                super.channelRead(ctx, msg);
            }
        });

        injected = true;
    }

    private void removeHandler() {
        if (!injected || mc.getNetHandler() == null) return;

        ChannelPipeline pipeline = mc.getNetHandler().getNetworkManager().channel().pipeline();
        if (pipeline == null) return;

        try {
            pipeline.remove("velocity_handler");
        } catch (Exception e) { }
    }
}
