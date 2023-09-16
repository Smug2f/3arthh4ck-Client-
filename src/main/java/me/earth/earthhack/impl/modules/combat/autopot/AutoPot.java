package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;

public class AutoPot extends Module {
    protected final Setting<Boolean> speed = register(new BooleanSetting("Speed", true));
    protected final Setting<Integer> speedDelay = register(new NumberSetting<>("SpeedDelay", 2500, 1, 5000));
    protected final Setting<Float> enemyRange = register(new NumberSetting<>("EnemyRange", 6.0f, 0.1f, 10.0f));
    protected final Setting<Float> noEnemyP = register(new NumberSetting<>("NoEnemyP", 80f, -90f, 90f));
    protected final Setting<Boolean> heal = register(new BooleanSetting("Heal", true));
    protected final Setting<Integer> healthPotThreshold = register(new NumberSetting<>("HealthThreshold", 15, 1, 19));
    protected final Setting<Integer> healthDelay = register(new NumberSetting<>("HealthDelay", 50, 1, 2000));

    private int lastSlot = -1;
    private long lastSpeedThrowTime = 0;
    private long lastHealthThrowTime = 0;

    public AutoPot() {
        super("AutoPot", Category.Combat);
        setData(new AutoPotData(this));
        this.listeners.add(new LambdaListener<>(TickEvent.class, this::onTick));
    }

    @Override
    protected void onEnable() {
        if (mc != null && mc.world != null && mc.player != null) {
            super.onEnable(); // Call the parent onEnable method to properly enable the module
        } else {
            toggle(); // Disable the module if any of the required variables is null
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
        if (speed.getValue() && System.currentTimeMillis() - lastSpeedThrowTime >= speedDelay.getValue()) {
            int slot = findSpeedSplashPotion();
            if (slot != -1) {
                float pitch;
                if (!hasSolidBlocksBelowPlayer()) {
                    pitch = -90f; // Always throw at -90 pitch if not on solid ground
                } else if (!isEnemyInRange(enemyRange.getValue()) || isBelowMovementThreshold()) {
                    pitch = noEnemyP.getValue(); // Both conditions met, throw at Noenemy pitch
                } else {
                    pitch = 90f; // Either one or none conditions are met, throw at 90 pitch (onground)
                }

                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, pitch, mc.player.onGround));
                throwPotion(slot);
                lastSpeedThrowTime = System.currentTimeMillis();
            }
        }
    }

    private void handleHealthPotion() {
        int currentHealth = (int) mc.player.getHealth();
        if (heal.getValue() && currentHealth <= healthPotThreshold.getValue() && System.currentTimeMillis() - lastHealthThrowTime >= healthDelay.getValue() && hasSolidBlocksBelowPlayer()) {
            int healthSlot = findHealthSplashPotion();
            if (healthSlot != -1) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90f, mc.player.onGround)); // Always throw at 90 pitch for heal
                throwPotion(healthSlot);
                lastHealthThrowTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isEnemyInRange(float range) {
        return mc.world.playerEntities.stream()
                .anyMatch(player -> player != mc.player && mc.player.getDistance(player) <= range);
    }

    private boolean isBelowMovementThreshold() {
        double movementThreshold = mc.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
        double horizontalMotion = mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ;
        return horizontalMotion <= movementThreshold * movementThreshold;
    }

    private boolean hasSolidBlocksBelowPlayer() {
        int playerX = MathHelper.floor(mc.player.posX);
        int playerZ = MathHelper.floor(mc.player.posZ);

        int playerY = MathHelper.floor(mc.player.getEntityBoundingBox().minY);

        // Check for solid blocks at y-1 and y-2
        for (int y = playerY - 1; y >= playerY - 2; y--) {
            Block block = mc.world.getBlockState(new BlockPos(playerX, y, playerZ)).getBlock();
            if (block != Blocks.AIR && block != Blocks.WATER && block != Blocks.LAVA) {
                return true;
            }
        }
        return false;
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
}
