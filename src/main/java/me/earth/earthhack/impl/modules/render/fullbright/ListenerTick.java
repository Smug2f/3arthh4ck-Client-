package me.earth.earthhack.impl.modules.render.fullbright;

import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

final class ListenerTick extends ModuleListener<Fullbright, TickEvent> {
    public ListenerTick(Fullbright module) {
        super(module, TickEvent.class);
    }

    @Override
    public void invoke(TickEvent event) {
        if (event.isSafe()) {
            switch (module.mode.getValue()) {
                case Gamma:
                    mc.gameSettings.gammaSetting = 1000.0f;
                    mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
                    break;
                case Potion:
                    mc.gameSettings.gammaSetting = 1.0f;
                    mc.player.addPotionEffect(
                            new PotionEffect(MobEffects.NIGHT_VISION, 1215, 0));
                    break;
                case Auto:
                    BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    int lightLevel = mc.world.getLightFor(EnumSkyBlock.BLOCK, playerPos);
                    if (lightLevel > 7) {
                        mc.gameSettings.gammaSetting = 1.0f;
                    } else {
                        mc.gameSettings.gammaSetting = 1000.0f;
                    }
                    break;
            }
        }
    }
}
