package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Objects;

public class AutoPot extends Module {
    protected final Setting<Integer> speedDelay = register(new NumberSetting<>("SpeedDelay", 2500, 1, 5000));
    protected final Setting<Float> EnnemyRange = register(new NumberSetting<>("EnnemyRange", 6.0f, 0.1f, 10.0f));
    protected final Setting<Float> NoEnnemyP = register(new NumberSetting<>("NoEnnemy P", 80f, -90f, 90f));
    private int lastSlot = -1;
    private long lastThrowTime = 0;

    public AutoPot() {
        super("AutoPot", Category.Combat);
        setData(new AutoPotData(this));
        this.listeners.add(new LambdaListener<>(TickEvent.class, e -> {
            if (!mc.player.isPotionActive(MobEffects.SPEED)) {
                if (System.currentTimeMillis() - lastThrowTime >= speedDelay.getValue()) {
                    if (isEnemyInRange(EnnemyRange.getValue())) {
                        int slot = InventoryUtil.findHotbarItem(Items.SPLASH_POTION);
                        if (slot != -1) {
                            lastSlot = mc.player.inventory.currentItem;
                            mc.player.inventory.currentItem = slot;
                            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                            mc.player.inventory.currentItem = lastSlot;
                            lastThrowTime = System.currentTimeMillis();
                        }
                    } else if (mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ <= mc.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() * mc.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) {
                        throwPotionWithPitch(NoEnnemyP.getValue());
                    }
                }
            }
        }));
    }

    @Override
    protected void onEnable() {
        if (mc.world == null || mc.player == null) {
            toggle();
        }
    }

    @Override
    public String getDisplayInfo() {
        if (!mc.player.isPotionActive(MobEffects.SPEED)) {
            int currentHealth = (int) mc.player.getHealth();
            return "Health: " + currentHealth;
        } else {
            int remainingTime = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getDuration();
            return "Speed: " + (remainingTime / 20) + "s";
        }
    }

    private boolean isEnemyInRange(float range) {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != mc.player && mc.player.getDistance(player) <= range) {
                return true;
            }
        }
        return false;
    }

    private void throwPotionWithPitch(float pitch) {
        int slot = InventoryUtil.findHotbarItem(Items.SPLASH_POTION);
        if (slot != -1) {
            lastSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = slot;
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
            mc.player.inventory.currentItem = lastSlot;
            lastThrowTime = System.currentTimeMillis();
            float pitchRadians = (float) Math.toRadians(pitch);
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, pitchRadians, mc.player.onGround));
        }
    }
}
