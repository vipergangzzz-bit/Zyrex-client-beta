package com.zyrex.module.impl;

import com.zyrex.module.Category;
import com.zyrex.module.Module;
import com.zyrex.setting.ModeSetting;
import com.zyrex.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAuraModule extends Module {
    private final NumberSetting range = new NumberSetting("Range", 4.2, 1, 6, 0.1);
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation Speed", 10, 1, 30, 1);
    private final ModeSetting priority = new ModeSetting("Priority", "Distance", "Health", "Angle");
    private final ModeSetting sortMode = new ModeSetting("Sort Mode", "Single", "Switch");
    private final ModeSetting rotationMode = new ModeSetting("Rotation Mode", "Normal", "Silent", "None");
    private final ModeSetting targetMode = new ModeSetting("Target Mode", "Players", "Mobs", "Animals", "All");
    private final NumberSetting cps = new NumberSetting("CPS", 12, 1, 20, 1);
    private final NumberSetting randomness = new NumberSetting("Randomness", 0, 0, 4, 1);
    private final NumberSetting fovRange = new NumberSetting("FOV Range", 360, 30, 360, 15);
    private final ModeSetting wallMode = new ModeSetting("Wall Mode", "Normal", "Strict");
    private final NumberSetting wallRange = new NumberSetting("Wall Range", 3.0, 1, 6, 0.1);

    private EntityLivingBase target;
    private int attackCooldown;
    private float currentYaw, currentPitch;
    private boolean rotating;

    public KillAuraModule() {
        super("Kill Aura", Category.Combat);
        addSetting(range);
        addSetting(rotationSpeed);
        addSetting(priority);
        addSetting(sortMode);
        addSetting(rotationMode);
        addSetting(targetMode);
        addSetting(cps);
        addSetting(randomness);
        addSetting(fovRange);
        addSetting(wallMode);
        addSetting(wallRange);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (attackCooldown > 0) attackCooldown--;

        target = findTarget();
        if (target == null) {
            rotating = false;
            return;
        }

        float[] rotations = getRotations(target);
        if (rotationMode.getValue().equals("Normal")) {
            mc.thePlayer.rotationYaw = rotations[0];
            mc.thePlayer.rotationPitch = rotations[1];
        } else if (rotationMode.getValue().equals("Silent")) {
            smoothRotate(rotations[0], rotations[1]);
            rotating = true;
        }

        if (attackCooldown <= 0 && canAttack()) {
            attack(target);
            attackCooldown = (int) (20 / cps.getValue());
            if (randomness.getValue() > 0)
                attackCooldown += (int) (Math.random() * randomness.getValue());
        }
    }

    private boolean canAttack() {
        if (mc.thePlayer.hurtTime > 0) return false;
        EntityLivingBase current = target;
        if (current == null) return false;
        double dist = mc.thePlayer.getDistanceToEntity(current);
        boolean wallCheck = wallMode.getValue().equals("Strict");
        if (wallCheck && !mc.thePlayer.canEntityBeSeen(current) && dist > wallRange.getValue())
            return false;
        return dist <= range.getValue();
    }

    private EntityLivingBase findTarget() {
        List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase entity = (EntityLivingBase) o;
            if (entity == mc.thePlayer || !entity.isEntityAlive()) continue;
            if (entity.isInvisible()) continue;
            if (AntiBotModule.isBot(entity)) continue;
            String tm = targetMode.getValue();
            boolean isValid = false;
            if (tm.equals("Players") || tm.equals("All")) {
                if (entity instanceof EntityPlayer && !entity.getName().equals(mc.thePlayer.getName()))
                    isValid = true;
            }
            if ((tm.equals("Mobs") || tm.equals("All")) && entity instanceof EntityMob)
                isValid = true;
            if ((tm.equals("Animals") || tm.equals("All")) && entity instanceof EntityAnimal && !(entity instanceof EntityMob))
                isValid = true;
            if (!isValid) continue;

            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist > range.getValue()) continue;
            if (!isInFOV(entity)) continue;

            entities.add(entity);
        }

        if (entities.isEmpty()) return null;

        String prio = priority.getValue();
        String sort = sortMode.getValue();
        if (sort.equals("Single")) {
            if (prio.equals("Distance"))
                entities.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));
            else if (prio.equals("Health"))
                entities.sort(Comparator.comparingDouble(e -> ((EntityLivingBase) e).getHealth()));
            else if (prio.equals("Angle"))
                entities.sort(Comparator.comparingDouble(e -> getAngleDifference(e)));
            return entities.get(0);
        }

        if (sort.equals("Switch")) {
            long time = System.currentTimeMillis() / 1000;
            int index = (int) (time % entities.size());
            return entities.get(Math.abs(index));
        }
        return null;
    }

    private boolean isInFOV(Entity entity) {
        float fov = (float)(fovRange.getValue());
        if (fov >= 360) return true;
        float[] rotations = getRotations(entity);
        float yawDiff = MathHelper.wrapAngleTo180_float(rotations[0] - mc.thePlayer.rotationYaw);
        return Math.abs(yawDiff) <= fov;
    }

    private float getAngleDifference(Entity entity) {
        float[] rotations = getRotations(entity);
        return Math.abs(MathHelper.wrapAngleTo180_float(rotations[0] - mc.thePlayer.rotationYaw));
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

    private void smoothRotate(float targetYaw, float targetPitch) {
        float speed = (float)(rotationSpeed.getValue());
        currentYaw += MathHelper.wrapAngleTo180_float(targetYaw - currentYaw) * speed / 10;
        currentPitch += MathHelper.wrapAngleTo180_float(targetPitch - currentPitch) * speed / 10;
    }

    private void attack(Entity entity) {
        mc.thePlayer.swingItem();
        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
    }

    @Override
    protected void onDisable() {
        rotating = false;
        target = null;
    }
}
