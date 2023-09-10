package me.earth.earthhack.impl.modules.movement.headhitter;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.util.client.SimpleData;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class HeadHitter extends Module {
    protected final Setting<Integer> jumpDelay =
            register(new NumberSetting<>("JPS", 6, 1, 10));

    private int ticksUntilNextJump = 0;

    public HeadHitter() {
        super("AutoHeadHitter", Category.Movement);
        SimpleData data = new SimpleData(
                this, "Jump if there is a solid block above the player, jps = jump per second.");
        this.setData(data);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ticksUntilNextJump = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isEnabled()) return; // Check if the module is enabled

        Minecraft mc = Minecraft.getMinecraft();

        if (ticksUntilNextJump > 0) {
            ticksUntilNextJump--;
        }

        if (ticksUntilNextJump == 0 && mc.player != null && mc.gameSettings.keyBindForward.isKeyDown()) {
            Vec3d currentPosition = mc.player.getPositionVector();
            BlockPos blockPos = new BlockPos(currentPosition.x, currentPosition.y + 2.0, currentPosition.z);

            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR && mc.world.getBlockState(blockPos).getBlock() != Blocks.LAVA && mc.world.getBlockState(blockPos).getBlock() != Blocks.WATER) {
                mc.player.jump();
                ticksUntilNextJump = 20 / jumpDelay.getValue();
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.getEntityBoundingBox().maxY + 2.0, mc.player.posZ);

        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR
                && mc.world.getBlockState(blockPos).getBlock() != Blocks.LAVA
                && mc.world.getBlockState(blockPos).getBlock() != Blocks.WATER) {
            return "\u00A7cJPS: " + jumpDelay.getValue();
        } else {
            return "\u00A7aReady";
        }
    }
}