package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ScaffoldModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Expand", "GodBridge", "Insane");
    private final ModeSetting tower = new ModeSetting("Tower", "On", "Off");
    private final NumberSetting expand = new NumberSetting("Expand", 1, 0, 5, 1);

    private int slot = -1;

    public ScaffoldModule() {
        super("Scaffold", Category.Movement);
        addSetting(mode);
        addSetting(tower);
        addSetting(expand);
    }

    @Override
    protected void onEnable() {
        slot = findBlockSlot();
    }

    @Override
    protected void onDisable() {
        if (slot != -1) mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        slot = -1;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        slot = findBlockSlot();
        if (slot == -1) return;

        if (tower.getValue().equals("On") && mc.gameSettings.keyBindJump.isKeyDown())
            mc.thePlayer.motionY = 0.42;

        clutch();

        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));

        int exp = mode.getValue().equals("Expand") ? (int)(expand.getValue()) : 0;
        BlockPos playerPos = new BlockPos(mc.thePlayer).down();

        for (int i = 0; i <= exp; i++) {
            BlockPos bp = playerPos;
            if (i > 0) {
                bp = bp.add(
                    (int) Math.round(MathHelper.sin(mc.thePlayer.rotationYaw * (float) Math.PI / 180) * i),
                    0,
                    (int) Math.round(-MathHelper.cos(mc.thePlayer.rotationYaw * (float) Math.PI / 180) * i)
                );
            }
            placeAt(bp);
        }
    }

    private void placeAt(BlockPos pos) {
        if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) return;
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (!(mc.theWorld.getBlockState(neighbor).getBlock() instanceof BlockAir)) {
                swing();
                place(neighbor, facing.getOpposite());
                return;
            }
        }
    }

    private void swing() {
        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
    }

    private void place(BlockPos neighbor, EnumFacing side) {
        mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(
            neighbor, side.getIndex(),
            mc.thePlayer.inventory.getStackInSlot(slot),
            0.5f, 0.5f, 0.5f));
    }

    private void clutch() {
        if (!mc.thePlayer.onGround && mc.thePlayer.motionY < -0.5) {
            BlockPos under = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
            if (mc.theWorld.getBlockState(under).getBlock() instanceof BlockAir) {
                int clutchSlot = findBlockSlot();
                if (clutchSlot == -1) return;
                int prevSlot = slot;
                slot = clutchSlot;
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
                EnumFacing side = EnumFacing.UP;
                BlockPos neighbor = under.offset(side);
                for (EnumFacing f : EnumFacing.values()) {
                    BlockPos check = under.offset(f);
                    Block b = mc.theWorld.getBlockState(check).getBlock();
                    if (b != Blocks.air && b.isFullBlock()) {
                        neighbor = check;
                        side = f.getOpposite();
                        break;
                    }
                }
                swing();
                place(neighbor, side);
                slot = prevSlot;
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
            }
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) return i;
        }
        return -1;
    }
}
