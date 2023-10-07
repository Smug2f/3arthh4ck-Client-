package me.earth.earthhack.impl.modules.movement.burrowclip;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.misc.UpdateEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.network.NetworkUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Objects;

public class BurrowClip extends Module {

    protected final Setting<BurrowClipMode> mode =
            register(new EnumSetting<>("Mode", BurrowClipMode.burrowclip));

    protected final Setting<Boolean> disable =
            register(new BooleanSetting("Disable", false));
    protected final Setting<Boolean> burrowed =
            register(new BooleanSetting("Burrowed", false));

    protected final Setting<Boolean> hole =
            register(new BooleanSetting("Hole", false));

    protected final Setting<Integer> updates =
            register(new NumberSetting<>("Updates", 10, 1, 30));

    public BlockPos pos;
    int disableTime = 0;

    public BurrowClip() {
        super("Clip+", Category.Movement);
        this.listeners.add(new LambdaListener<>(UpdateEvent.class, e -> {
            if (mc.world == null || mc.player == null) {
                return;
            }

            boolean shouldBurrowClip = true;

            if (burrowed.getValue()) {
                // Check if the player is burrowed at y=0 or y=1
                BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                BlockPos y0 = playerPos.down();
                BlockPos y1 = playerPos;

                boolean isY0Solid = isBlockLavaOrWater(y0) && isBlockSolid(y0);
                boolean isY1Solid = isBlockLavaOrWater(y1) && isBlockSolid(y1);

                if (!(isY0Solid || isY1Solid)) {
                    shouldBurrowClip = false; // Don't proceed if both y0 and y1 are not solid (and not lava or water)
                }
            }

            if (hole.getValue()) {
                BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                boolean isFeetSurroundedByObsidianOrBedrock = isObsidianOrBedrockAroundFeet(playerPos);

                if (!isFeetSurroundedByObsidianOrBedrock) {
                    shouldBurrowClip = false; // Don't proceed if feet are not surrounded by obsidian or bedrock
                }
            }

            if (shouldBurrowClip) {
                if (Objects.requireNonNull(mode.getValue()) == BurrowClipMode.burrowclip) {
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX - 1.5400, mc.player.posY, mc.player.posZ, mc.player.onGround));
                }

                disableTime++;
                if (disable.getValue()) {
                    if (disableTime >= updates.getValue()) {
                        disable();
                    }
                }
            }
        }));
    }

    private boolean isBlockLavaOrWater(BlockPos pos) {
        return !mc.world.getBlockState(pos).getMaterial().isLiquid();
    }

    private boolean isBlockSolid(BlockPos pos) {
        return !mc.world.getBlockState(pos).getMaterial().isLiquid() && !mc.world.isAirBlock(pos);
    }
    private boolean isObsidianOrBedrockAroundFeet(BlockPos playerPos) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                BlockPos blockPos = playerPos.add(xOffset, -1, zOffset);
                if (!isBlockObsidianOrBedrock(blockPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isBlockObsidianOrBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == net.minecraft.init.Blocks.OBSIDIAN
                || mc.world.getBlockState(pos).getBlock() == net.minecraft.init.Blocks.BEDROCK;
    }

    @Override
    protected void onDisable() {
        disableTime = 0;
    }

    @Override
    public String getDisplayInfo() {
        StringBuilder displayInfo = new StringBuilder();

        if (updates.getValue() != null) {
            displayInfo.append("Updates: ").append(updates.getValue());
        }

        return displayInfo.toString();
    }
}
