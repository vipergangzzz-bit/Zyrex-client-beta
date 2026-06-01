package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TriggerBotModule extends Module {
    private final NumberSetting cps = new NumberSetting("CPS", 12, 1, 20, 1);
    private final ModeSetting mode = new ModeSetting("Mode", "Hold", "Toggle");
    private final NumberSetting range = new NumberSetting("Range", 4.2, 1, 6, 0.1);

    private int cooldown;

    public TriggerBotModule() {
        super("Trigger Bot", Category.Combat);
        addSetting(cps);
        addSetting(mode);
        addSetting(range);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mode.getValue().equals("Hold") && !mc.gameSettings.keyBindAttack.isKeyDown()) return;

        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) mc.objectMouseOver.entityHit;
            if (!target.isEntityAlive()) return;
            if (mc.thePlayer.getDistanceToEntity(target) > range.getValue()) return;

            if (cooldown > 0) { cooldown--; return; }
            mc.playerController.attackEntity(mc.thePlayer, target);
            mc.thePlayer.swingItem();
            cooldown = (int) (20 / cps.getValue());
        }
    }
}
