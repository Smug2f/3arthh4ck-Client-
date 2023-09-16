package me.earth.earthhack.impl.modules.movement.headhitter;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.util.client.SimpleData;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class HeadHitter extends Module {
    protected final Setting<Integer> jumpDelay = register(new NumberSetting<>("JPS", 6, 1, 10));
    private int jumpCooldown = 0;

    public HeadHitter() {
        super("AutoHeadHitter", Category.Movement);
        setData(new SimpleData(this, "Jump if there is a solid block above the player, jps = jumps per second."));
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    protected void onUpdate() {
        boolean blockAbove = isBlockAbove();

        if (blockAbove && mc.player != null && mc.gameSettings.keyBindForward.isKeyDown()) {
            if (jumpCooldown <= 0) {
                mc.player.jump();
                jumpCooldown = (int)(20.0 / jumpDelay.getValue()); // Set the jump cooldown based on JPS
            }
        }

        if (jumpCooldown > 0) {
            jumpCooldown--;
        }
    }

    private boolean isBlockAbove() {
        BlockPos blockPos = mc.player.getPosition().add(0, 2, 0);
        return mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR &&
                mc.world.getBlockState(blockPos).getBlock() != Blocks.LAVA &&
                mc.world.getBlockState(blockPos).getBlock() != Blocks.WATER;
    }

    @Override
    public String getDisplayInfo() {
        boolean blockAbove = isBlockAbove();

        if (blockAbove) {
            return "\u00A7cJPS: " + jumpDelay.getValue();
        } else {
            return "\u00A7aReady";
        }
    }
}
