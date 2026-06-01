package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.List;

public class StaffDetectorModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Name", "Hypixel", "Custom");
    private final List<String> staffNames = new ArrayList<String>();

    public StaffDetectorModule() {
        super("Staff Detector", Category.Player);
        addSetting(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        staffNames.clear();
        for (Object o : mc.theWorld.playerEntities) {
            EntityPlayer player = (EntityPlayer) o;
            if (player == mc.thePlayer) continue;
            if (isStaff(player)) staffNames.add(player.getName());
        }
    }

    private boolean isStaff(EntityPlayer player) {
        String m = mode.getValue();
        if (m.equals("Name")) {
            String name = player.getName().toLowerCase();
            if (name.contains("mod") || name.contains("staff") || name.contains("admin")
                || name.contains("helper") || name.contains("builder")) return true;
            if (player.getDisplayName() != null) {
                String display = player.getDisplayName().getUnformattedText();
                if (display.contains("[") && (display.contains("MOD") || display.contains("ADMIN")
                    || display.contains("HELPER") || display.contains("BUILDER"))) return true;
            }
        }
        if (m.equals("Hypixel")) {
            if (player.getDisplayName() != null) {
                String display = player.getDisplayName().getUnformattedText();
                if (display.contains("[MOD]") || display.contains("[ADMIN]")
                    || display.contains("[HELPER]") || display.contains("[YOUTUBE]")
                    || display.contains("[BUILDER]")) return true;
            }
        }
        return false;
    }

    public List<String> getStaffNames() { return staffNames; }
}
