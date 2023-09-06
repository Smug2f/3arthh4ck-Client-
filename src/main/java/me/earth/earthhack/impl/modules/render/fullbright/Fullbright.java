package me.earth.earthhack.impl.modules.render.fullbright;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.impl.modules.render.fullbright.mode.BrightMode;
import net.minecraft.init.MobEffects;

public class Fullbright extends Module
{
    protected final Setting<BrightMode> mode =
            register(new EnumSetting<>("Mode", BrightMode.Gamma));
    public Fullbright()
    {
        super("Fullbright", Category.Render);
        this.listeners.add(new ListenerTick(this));
        this.setData(new FullbrightData(this));
    }

    @Override
    protected void onDisable()
    {
        if (mc.player != null && mode.getValue() == BrightMode.Potion)
        {
            mc.gameSettings.gammaSetting = 1.0F;
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
    }

}
