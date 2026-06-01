package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.Collection;

public class AntiBotModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Hypixel", "Minemen", "Custom", "Advanced");
    private final java.util.List<Entity> botEntities = new ArrayList<Entity>();
    private Collection<String> tabNames = new ArrayList<String>();

    private static AntiBotModule instance;

    public AntiBotModule() {
        super("Anti Bot", Category.Combat);
        addSetting(mode);
        instance = this;
    }

    public static boolean isBot(Entity entity) {
        if (instance == null || !instance.isEnabled()) return false;
        return instance.botEntities.contains(entity);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        tabNames = new ArrayList<String>();
        if (mc.getNetHandler() != null) {
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                tabNames.add(info.getGameProfile().getName());
            }
        }

        botEntities.clear();
        for (Object o : mc.theWorld.loadedEntityList) {
            Entity e = (Entity) o;
            if (e == mc.thePlayer) continue;
            if (isBotCheck(e)) botEntities.add(e);
        }
    }

    private boolean isBotCheck(Entity entity) {
        // not in tab list = bot
        if (!tabNames.contains(entity.getName())) return true;

        // no armor = bot (bedrock/cheater bots usually have no armor)
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            boolean hasArmor = false;
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack != null) { hasArmor = true; break; }
            }
            if (!hasArmor) return true;
        }

        String m = mode.getValue();
        if (m.equals("Hypixel")) {
            if (entity.getName().contains("[NPC]") || entity.getName().startsWith("§")) return true;
            if (entity.getUniqueID() != null && entity.getUniqueID().toString().startsWith("00000000-0000-0000-0000-000000000000")) return true;
        }
        if (m.equals("Minemen")) {
            if (entity.ticksExisted < 10) return true;
            if (entity.getName().contains(" ")) return true;
        }
        if (m.equals("Advanced")) {
            if (entity.isInvisible()) return true;
            if (entity.getName().equals(mc.thePlayer.getName()) && entity != mc.thePlayer) return true;
        }
        return false;
    }

    public boolean isBotEntity(Entity entity) {
        return botEntities.contains(entity);
    }
}
