package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TeleportAuraModule extends Module {
    private final NumberSetting range = new NumberSetting("Range", 15, 1, 250, 0.5);
    private final NumberSetting cps = new NumberSetting("CPS", 10, 1, 20, 1);
    private final ModeSetting priority = new ModeSetting("Priority", "Distance", "Health", "Angle");
    private final ModeSetting rotationMode = new ModeSetting("Rotation", "Normal", "Silent", "None");

    private int cooldown;

    public TeleportAuraModule() {
        super("Teleport Aura", Category.Combat);
        addSetting(range);
        addSetting(cps);
        addSetting(priority);
        addSetting(rotationMode);
    }

    @Override
    protected void onDisable() {
        cooldown = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (cooldown > 0) { cooldown--; return; }

        EntityLivingBase target = findTarget();
        if (target == null) return;

        double prevX = mc.thePlayer.posX;
        double prevY = mc.thePlayer.posY;
        double prevZ = mc.thePlayer.posZ;

        float[] rots = getRotations(target);
        String rot = rotationMode.getValue();
        if (rot.equals("Normal")) {
            mc.thePlayer.rotationYaw = rots[0];
            mc.thePlayer.rotationPitch = rots[1];
        }

        double tpY = target.posY;
        tpY = Math.max(tpY, mc.theWorld.getHorizon());

        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
            target.posX, target.posY, target.posZ, true));

        mc.thePlayer.swingItem();
        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(prevX, prevY, prevZ, true));

        cooldown = (int) (20 / cps.getValue());
    }

    private EntityLivingBase findTarget() {
        List<EntityLivingBase> list = new ArrayList<EntityLivingBase>();
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (e == mc.thePlayer || !e.isEntityAlive()) continue;
            if (e.isInvisible()) continue;
            if (AntiBotModule.isBot(e)) continue;
            if (!(e instanceof EntityPlayer)) continue;
            if (mc.thePlayer.getDistanceToEntity(e) > range.getValue()) continue;
            list.add(e);
        }
        if (list.isEmpty()) return null;
        String p = priority.getValue();
        if (p.equals("Distance"))
            list.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));
        else if (p.equals("Health"))
            list.sort(Comparator.comparingDouble(e -> ((EntityLivingBase) e).getHealth()));
        else if (p.equals("Angle"))
            list.sort(Comparator.comparingDouble(e -> getAngleDifference(e)));
        return list.get(0);
    }

    private float getAngleDifference(Entity e) {
        float[] rots = getRotations(e);
        return Math.abs(MathHelper.wrapAngleTo180_float(rots[0] - mc.thePlayer.rotationYaw));
    }

    private float[] getRotations(Entity entity) {
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffY = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180 / Math.PI) - 90;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180 / Math.PI);
        return new float[]{MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch)};
    }
}
