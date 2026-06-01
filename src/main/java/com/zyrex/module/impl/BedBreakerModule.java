package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.NumberSetting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BedBreakerModule extends Module {
    private final NumberSetting range = new NumberSetting("Range", 50, 10, 250, 5);

    private List<BlockPos> beds = new ArrayList<BlockPos>();
    private BlockPos protectedBed;
    private Set<BlockPos> attempted = new HashSet<BlockPos>();
    private Set<BlockPos> retrying = new HashSet<BlockPos>();
    private Set<BlockPos> blacklisted = new HashSet<BlockPos>();

    public BedBreakerModule() {
        super("Bed Breaker", Category.OverPowered);
        addSetting(range);
    }

    @Override
    protected void onEnable() {
        beds.clear();
        protectedBed = null;
        attempted.clear();
        retrying.clear();
        blacklisted.clear();
        scanBeds();
    }

    @Override
    protected void onDisable() {
        beds.clear();
        protectedBed = null;
        attempted.clear();
        retrying.clear();
        blacklisted.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (beds.isEmpty()) return;

        for (BlockPos pos : retrying) {
            if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed) {
                blacklisted.add(pos);
            }
        }
        retrying.clear();

        for (BlockPos pos : attempted) {
            if (blacklisted.contains(pos)) continue;
            if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed) {
                retrying.add(pos);
            }
        }
        attempted.clear();

        double px = mc.thePlayer.posX, py = mc.thePlayer.posY, pz = mc.thePlayer.posZ;

        for (BlockPos pos : beds) {
            if (pos.equals(protectedBed) || blacklisted.contains(pos)) continue;
            if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.bed) continue;

            Vec3 center = new Vec3(pos).addVector(0.5, 0.5, 0.5);
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                center.xCoord, center.yCoord, center.zCoord, true));
            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));

            attempted.add(pos);
        }

        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(px, py, pz, true));
    }

    private void scanBeds() {
        int r = (int)(range.getValue());
        BlockPos playerPos = mc.thePlayer.getPosition();

        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (block == Blocks.bed) {
                        beds.add(pos);
                        double dist = mc.thePlayer.getDistanceSq(pos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = pos;
                        }
                    }
                }
            }
        }

        protectedBed = closest;
    }
}
