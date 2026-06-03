package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InfiniteReachModule extends Module {
    private final NumberSetting range = new NumberSetting("Range", 50, 10, 250, 5);
    private final NumberSetting offset = new NumberSetting("Offset", 0.5, 0.1, 3.0, 0.1);
    private final NumberSetting cooldownTicks = new NumberSetting("Cooldown", 10, 0, 40, 1);
    private final ModeSetting trigger = new ModeSetting("Trigger", "Auto", "Click");

    private int cooldown;

    public InfiniteReachModule() {
        super("Infinite Reach", Category.OverPowered);
        addSetting(range);
        addSetting(offset);
        addSetting(cooldownTicks);
        addSetting(trigger);
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

        if (trigger.getValue().equals("Click") && !mc.gameSettings.keyBindAttack.isKeyDown()) return;

        List<EntityLivingBase> targets = raycastTargets((float)(range.getValue()));
        if (targets.isEmpty()) return;

        double px = mc.thePlayer.posX, py = mc.thePlayer.posY, pz = mc.thePlayer.posZ;

        for (EntityLivingBase target : targets) {
            double dx = target.posX - px;
            double dz = target.posZ - pz;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.01) dist = 0.01;
            double o = offset.getValue();
            double nx = target.posX - (dx / dist) * o;
            double nz = target.posZ - (dz / dist) * o;

            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                nx, target.posY, nz, true));
            mc.thePlayer.swingItem();
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
        }

        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(px, py, pz, true));

        cooldown = (int)(cooldownTicks.getValue());
    }

    private List<EntityLivingBase> raycastTargets(float distance) {
        Vec3 pos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 look = mc.thePlayer.getLook(1.0f);
        Vec3 end = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);

        List<Entity> areaEntities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer,
            mc.thePlayer.getEntityBoundingBox()
                .addCoord(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance)
                .expand(2.0, 2.0, 2.0));

        List<EntityLivingBase> targets = new ArrayList<EntityLivingBase>();

        for (Entity entity : areaEntities) {
            if (!entity.canBeCollidedWith()) continue;
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;
            if (living == mc.thePlayer || !living.isEntityAlive()) continue;
            if (AntiBotModule.isBot(living)) continue;

            float border = entity.getCollisionBorderSize();
            AxisAlignedBB box = entity.getEntityBoundingBox().expand(border, border, border);
            MovingObjectPosition mop = box.calculateIntercept(pos, end);

            if (mop != null) {
                targets.add(living);
            }
        }

        Collections.sort(targets, new Comparator<EntityLivingBase>() {
            public int compare(EntityLivingBase a, EntityLivingBase b) {
                return Double.compare(a.getDistanceToEntity(mc.thePlayer), b.getDistanceToEntity(mc.thePlayer));
            }
        });

        return targets;
    }

}
