package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InfiniteMineModule extends Module {
    private final NumberSetting range = new NumberSetting("Range", 50, 5, 250, 5);
    private final ModeSetting target = new ModeSetting("Target", "Crosshair", "Bed & Wool");

    public InfiniteMineModule() {
        super("Infinite Mine", Category.OverPowered);
        addSetting(range);
        addSetting(target);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (target.getValue().equals("Crosshair")) {
            tickCrosshair();
        } else {
            tickScan();
        }
    }

    private void tickCrosshair() {
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) return;

        MovingObjectPosition mop = raycast((float)(range.getValue()));
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        BlockPos pos = mop.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (block == Blocks.air || block == Blocks.bedrock) return;

        breakBlock(pos, mop.sideHit);
    }

    private void tickScan() {
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) return;

        int r = (int)(range.getValue());
        BlockPos playerPos = mc.thePlayer.getPosition();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (block == Blocks.air || block == Blocks.bedrock) continue;
                    if (!isBedOrWool(block)) continue;

                    breakBlock(pos, EnumFacing.UP);
                }
            }
        }
    }

    private boolean isBedOrWool(Block block) {
        String name = Block.blockRegistry.getNameForObject(block).toString().toLowerCase();
        return name.contains("bed") || name.contains("wool");
    }

    private void breakBlock(BlockPos pos, EnumFacing side) {
        double px = mc.thePlayer.posX, py = mc.thePlayer.posY, pz = mc.thePlayer.posZ;
        Vec3 center = new Vec3(pos).addVector(0.5, 0.5, 0.5);

        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
            center.xCoord, center.yCoord, center.zCoord, true));
        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
            C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side));
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
            C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, side));
        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(px, py, pz, true));
    }

    private MovingObjectPosition raycast(float distance) {
        Vec3 pos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 look = mc.thePlayer.getLook(1.0f);
        Vec3 end = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
        return mc.theWorld.rayTraceBlocks(pos, end, false, false, true);
    }
}
