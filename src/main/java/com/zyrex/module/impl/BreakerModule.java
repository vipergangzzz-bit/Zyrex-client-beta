package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BreakerModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Chests", "Beds", "Crystals", "All");

    public BreakerModule() {
        super("Breaker", Category.Player);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        String m = mode.getValue();

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = new BlockPos(mc.thePlayer).add(x, y, z);
                    net.minecraft.block.Block block = mc.theWorld.getBlockState(pos).getBlock();
                    boolean shouldBreak = false;

                    if (m.equals("Chests") || m.equals("All")) {
                        if (block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.ender_chest)
                            shouldBreak = true;
                    }
                    if (m.equals("Beds") || m.equals("All")) {
                        if (block == Blocks.bed) shouldBreak = true;
                    }
                    if (m.equals("Crystals") || m.equals("All")) {
                    }

                    if (shouldBreak) {
                        mc.thePlayer.swingItem();
                        mc.playerController.clickBlock(pos, mc.objectMouseOver != null ? mc.objectMouseOver.sideHit : null);
                    }
                }
            }
        }
    }
}
