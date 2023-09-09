package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;

import java.util.List;
import java.util.Objects;

public class AutoPot extends Module {
    protected final Setting<Integer> speedDelay = register(new NumberSetting<>("SpeedDelay", 2500, 1, 5000));
    protected final Setting<Float> enemyRange = register(new NumberSetting<>("EnemyRange", 6.0f, 0.1f, 10.0f));
    protected final Setting<Float> noEnemyP = register(new NumberSetting<>("NoEnemyP", 80f, -90f, 90f));
    protected final Setting<Integer> healthPotThreshold = register(new NumberSetting<>("HealthThreshold", 15, 1, 19));
    protected final Setting<Integer> healthDelay = register(new NumberSetting<>("HealthDelay", 50, 1, 2000));

    private int lastSlot = -1;
    private long lastThrowTime = 0;

    public AutoPot() {
        super("AutoPot", Category.Combat);
        setData(new AutoPotData(this));
        this.listeners.add(new LambdaListener<>(TickEvent.class, this::onTick));
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
            return "Health: " + (int) mc.player.getHealth();
        } else {
            int remainingTime = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getDuration();
            return "\u00A7bSpeed: " + (remainingTime / 20) + "s";
        }
    }

    private void onTick(TickEvent event) {
        if (!mc.player.isPotionActive(MobEffects.SPEED)) {
            handleSpeedPotion();
        }

        handleHealthPotion();
    }

    private void handleSpeedPotion() {
        if (System.currentTimeMillis() - lastThrowTime < speedDelay.getValue()) {
            return;
        }

        if (isEnemyInRange(enemyRange.getValue())) {
            int slot = findSpeedSplashPotion();
            if (slot != -1) {
                throwPotion(slot);
                lastThrowTime = System.currentTimeMillis();
            }
        } else if (isBelowMovementThreshold()) {
            throwPotionWithPitch(noEnemyP.getValue());
        }
    }

    private void handleHealthPotion() {
        int currentHealth = (int) mc.player.getHealth();
        if (currentHealth <= healthPotThreshold.getValue() && System.currentTimeMillis() - lastThrowTime >= healthDelay.getValue()) {
            int healthSlot = findHealthSplashPotion();
            if (healthSlot != -1) {
                throwPotion(healthSlot);
                lastThrowTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isEnemyInRange(float range) {
        return mc.world.playerEntities.stream()
                .anyMatch(player -> player != mc.player && mc.player.getDistance(player) <= range);
    }

    private boolean isBelowMovementThreshold() {
        double movementThreshold = mc.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
        double playerMotion = mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ;
        return playerMotion <= movementThreshold * movementThreshold;
    }

    private int findSpeedSplashPotion() {
        for (int i = 0; i < 9; i++) {
            ItemStack stackInSlot = mc.player.inventory.getStackInSlot(i);

            if (stackInSlot.getItem() instanceof ItemSplashPotion) {
                List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stackInSlot);

                if (effects.stream().anyMatch(effect -> effect.getPotion() == MobEffects.SPEED)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findHealthSplashPotion() {
        for (int i = 0; i < 9; i++) {
            ItemStack stackInSlot = mc.player.inventory.getStackInSlot(i);

            if (stackInSlot.getItem() instanceof ItemSplashPotion) {
                List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stackInSlot);

                if (effects.stream().anyMatch(effect -> effect.getPotion() == MobEffects.INSTANT_HEALTH)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void throwPotion(int slot) {
        lastSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = slot;
        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
        mc.player.inventory.currentItem = lastSlot;
    }

    private void throwPotionWithPitch(float pitch) {
        int slot = findSpeedSplashPotion();
        if (slot != -1) {
            throwPotion(slot);
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, pitch, mc.player.onGround));
        }
    }
}