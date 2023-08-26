package me.earth.earthhack.impl.modules.misc.healthsoundindicator;

import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.util.helpers.disabling.DisablingModule;

/**
 * Feel free to look into.
 * @author Smug2 .
 */
public class HealthSoundIndicator extends DisablingModule {

    /**
     * Setting to control the health threshold for playing the sound.
     */
    protected final Setting<Integer> health = register(new NumberSetting<>("Health", 8, 1, 19));

    public HealthSoundIndicator() {
        super("HealthSoundIndicator", Category.Misc);
        setData(new HealthSoundIndicatorData(this));
    }

    @Override
    protected void onEnable() {
    }
}
