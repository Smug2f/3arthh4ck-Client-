package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.misc.UpdateEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.helpers.disabling.DisablingModule;
import me.earth.earthhack.impl.util.math.TickTimer;
import net.minecraft.block.Block;
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

public class AutoPot extends DisablingModule {
    protected final Setting<Boolean> speed =
            register(new BooleanSetting("Speed", true));
    protected final Setting<Integer> speedDelay =
            register(new NumberSetting<>("SpeedDelay", 6, 0, 30));
    protected final Setting<Float> enemyRange =
            register(new NumberSetting<>("EnemyRange", 6.0f, 0.1f, 10.0f));
    protected final Setting<Float> noEnemyP =
            register(new NumberSetting<>("NoEnemyP", 80f, -90f, 90f));
    protected final Setting<Boolean> heal =
            register(new BooleanSetting("Heal", true));
    protected final Setting<Integer> healthPotThreshold =
            register(new NumberSetting<>("HealthThreshold", 15, 1, 19));
    protected final Setting<Integer> healthDelay =
            register(new NumberSetting<>("HealthDelay", 1, 0, 30));
    protected final Setting<Integer> healSlot =
            register(new NumberSetting<>("HealSlot", 8, 1, 9));


    private final TickTimer speedDelayTimer;
    private final TickTimer healthDelayTimer;

    public AutoPot() {
        super("AutoPot", Category.Combat);
        setData(new AutoPotData(this));
        this.listeners.add(new LambdaListener<>(UpdateEvent.class, e -> {
            if (mc.player != null && mc.world != null) {
                handleSpeedPotion();
                handleHealthPotion();
            } else {
                toggle();
            }
        }));

        speedDelayTimer = new TickTimer(speedDelay.getValue());
        healthDelayTimer = new TickTimer(healthDelay.getValue());
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

    private void handleSpeedPotion() {
        if (!speed.getValue() || mc.player.isPotionActive(MobEffects.SPEED)) {
            return;
        }

        if (!isEnemyInRange(enemyRange.getValue()) || hasSolidBlocksBelowPlayer()) {
            float pitch = noEnemyP.getValue();
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0f, pitch, mc.player.onGround));
            int slot = findSpeedSplashPotion();
            if (slot != -1 && speedDelayTimer.hasReached()) {
                throwPotion(slot);
                speedDelayTimer.reset();
            }
        }
    }

    private void handleHealthPotion() {
        int currentHealth = (int) mc.player.getHealth();
        if (!heal.getValue() || currentHealth > healthPotThreshold.getValue() || hasSolidBlocksBelowPlayer()) {
            return;
        }

        int healthSlot = findHealthSplashPotion();
        if (healthSlot != -1 && healthDelayTimer.hasReached()) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0f, 90f, mc.player.onGround));
            movePotionToHotbar(healthSlot);
            throwPotion(healSlot.getValue() - 1);
            healthDelayTimer.reset();
        }
    }

    private final Object potionLock = new Object();

    private void movePotionToHotbar(int potionSlot) {
        if (potionSlot < 0 || potionSlot >= 9) {
            return;
        }

        int hotbarSlot = healSlot.getValue() - 1;

        if (potionSlot == hotbarSlot) {
            return;
        }

        synchronized (potionLock) {
            ItemStack potionStack = mc.player.inventory.getStackInSlot(potionSlot);

            if (potionStack.getItem() instanceof ItemSplashPotion) {
                ItemStack hotbarStack = mc.player.inventory.getStackInSlot(hotbarSlot);

                if (hotbarStack.isEmpty()) {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
                    mc.player.inventory.setInventorySlotContents(potionSlot, ItemStack.EMPTY);
                } else {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
                    mc.player.inventory.setInventorySlotContents(potionSlot, hotbarStack);
                }
            }
        }
    }

    private boolean isEnemyInRange(float range) {
        return mc.world.playerEntities.stream()
                .anyMatch(player -> player != mc.player && mc.player.getDistance(player) <= range);
    }

    private boolean hasSolidBlocksBelowPlayer() {
        int playerX = MathHelper.floor(mc.player.posX);
        int playerZ = MathHelper.floor(mc.player.posZ);
        int playerY = MathHelper.floor(mc.player.getEntityBoundingBox().minY);

        for (int y = playerY - 1; y >= playerY - 2; y--) {
            Block block = mc.world.getBlockState(new BlockPos(playerX, y, playerZ)).getBlock();
            if (block != Blocks.AIR && block != Blocks.WATER && block != Blocks.LAVA && block != Blocks.PORTAL && block != Blocks.END_PORTAL) {
                return false;
            }
        }
        return true;
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
        int lastSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = slot;
        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
        mc.player.inventory.currentItem = lastSlot;
    }
}
