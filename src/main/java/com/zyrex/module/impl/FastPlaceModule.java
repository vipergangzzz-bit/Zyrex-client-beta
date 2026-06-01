package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FastPlaceModule extends Module {
    private final NumberSetting delay = new NumberSetting("Delay", 0, 0, 4, 1);

    public FastPlaceModule() {
        super("Fast Place", Category.Movement);
        addSetting(delay);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.thePlayer == null) return;
        try {
            int val = ReflectionHelper.getPrivateValue(Minecraft.class, mc, "rightClickDelayTimer", "field_71452_i");
            ReflectionHelper.setPrivateValue(Minecraft.class, mc, Math.min(val, (int)(delay.getValue())), "rightClickDelayTimer", "field_71452_i");
        } catch (Exception e) { }
    }
}
