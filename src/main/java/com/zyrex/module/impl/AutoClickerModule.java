package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoClickerModule extends Module {
    private final NumberSetting cps = new NumberSetting("CPS", 12, 1, 30, 1);
    private final ModeSetting mode = new ModeSetting("Mode", "Single", "Multi");
    private final ModeSetting clickStyle = new ModeSetting("Click Style", "Hold", "Toggle");
    private final NumberSetting randomization = new NumberSetting("Randomization", 0, 0, 5, 1);

    private int cooldown;
    private boolean clicking;

    public AutoClickerModule() {
        super("Auto Clicker", Category.Combat);
        addSetting(cps);
        addSetting(mode);
        addSetting(clickStyle);
        addSetting(randomization);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        boolean holding = mc.gameSettings.keyBindAttack.isKeyDown();

        if (clickStyle.getValue().equals("Toggle")) {
            if (holding && mc.thePlayer.ticksExisted % 10 == 0) clicking = !clicking;
        } else {
            clicking = holding;
        }

        if (!clicking) {
            cooldown = 0;
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
            mc.playerController.attackEntity(mc.thePlayer, mc.objectMouseOver.entityHit);
            mc.thePlayer.swingItem();
        } else {
            mc.thePlayer.swingItem();
            mc.playerController.clickBlock(
                mc.objectMouseOver != null ? mc.objectMouseOver.getBlockPos() : null,
                mc.objectMouseOver != null ? mc.objectMouseOver.sideHit : null
            );
        }

        double rand = randomization.getValue() > 0 ? (Math.random() * randomization.getValue()) : 0;
        cooldown = Math.max(0, (int) (20 / cps.getValue() - rand));
    }
}
