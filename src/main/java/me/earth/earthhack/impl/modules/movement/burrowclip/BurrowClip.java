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
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BurrowClip extends Module {

    protected final Setting<BurrowClipMode> mode =
            register(new EnumSetting<>("Mode", BurrowClipMode.sandclip));

    protected final Setting<Integer> delay =
            register(new NumberSetting<>("Delay", 5, 1, 10));

    protected final Setting<Boolean> disable =
            register(new BooleanSetting("Disable", false));

    protected final Setting<Boolean> hole =
            register(new BooleanSetting("Hole", true));

    protected final Setting<Boolean> burrow =
            register(new BooleanSetting("Burrow", true));

    protected final Setting<Integer> updates =
            register(new NumberSetting<>("Updates", 10, 1, 30));

    public BlockPos pos;
    int disableTime = 0;

    public BurrowClip() {
        super("Clip+", Category.Movement);
        this.setData(new BurrowClipData(this));
        this.listeners.add(new LambdaListener<>(UpdateEvent.class, e -> {
            if (mc.world == null || mc.player == null) {
                return;
            }

            if (hole.getValue()) {
                BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
                int validBlocks = 0;

                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    for (int zOffset = -1; zOffset <= 1; zOffset++) {
                        BlockPos blockPos = playerPos.add(xOffset, 0, zOffset);
                        if (isValidBlock(blockPos)) {
                            validBlocks++;
                        }
                    }
                }

                if (validBlocks < 4) {
                    return; // Not enough surrounding blocks, do nothing
                }
            }

            if (burrow.getValue() && mode.getValue() == BurrowClipMode.sandclip) {
                BlockPos playerFeet = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                Block blockAtFeet = mc.world.getBlockState(playerFeet).getBlock();

                if (blockAtFeet != Blocks.GRAVEL && blockAtFeet != Blocks.SAND && blockAtFeet != Blocks.CONCRETE_POWDER) {
                    return; // No falling blocks, do nothing
                }
            }

            switch (mode.getValue()) {
                case burrowclip:
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.0042123, mc.player.posZ, mc.player.onGround));
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.02141, mc.player.posZ, mc.player.onGround));
                    NetworkUtil.send(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 0.097421, mc.player.posZ, 500, 500, mc.player.onGround));
                    break;

                case sandclip:
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX - 0.5400, mc.player.posY, mc.player.posZ, mc.player.onGround));
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX - 0.8400, mc.player.posY, mc.player.posZ, mc.player.onGround));
                    break;
            }

            disableTime++;
            if (disable.getValue()) {
                if (disableTime >= updates.getValue()) {
                    disable();
                }
            }
        }));
    }

    private boolean isValidBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block != Blocks.AIR && block != Blocks.LAVA && block != Blocks.WATER;
    }

    @Override
    protected void onDisable() {
        disableTime = 0;
    }

    @Override
    public String getDisplayInfo() {
        StringBuilder displayInfo = new StringBuilder();

        if (mode.getValue() == BurrowClipMode.sandclip) {
            BlockPos playerFeet = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
            Block blockAtFeet = mc.world.getBlockState(playerFeet).getBlock();

            if (blockAtFeet == Blocks.GRAVEL || blockAtFeet == Blocks.SAND || blockAtFeet == Blocks.CONCRETE_POWDER) {
                displayInfo.append("\u00A7cFalling block found");
            } else {
                displayInfo.append("\u00A72Safe");
            }
        }
        if (mode.getValue() == BurrowClipMode.burrowclip) {
            displayInfo.append(mode.getValue().toString());
        }

        return displayInfo.toString();
    }
}