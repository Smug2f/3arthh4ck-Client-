package me.earth.earthhack.impl.modules.misc.healthsoundindicator;

import me.earth.earthhack.api.module.data.DefaultData;

final class HealthSoundIndicatorData extends DefaultData<HealthSoundIndicator>
{
    public HealthSoundIndicatorData (HealthSoundIndicator module)
    {
        super(module);
        register(module.health, "At wich number of hearths the sound will trigger");
    }

    @Override
    public int getColor()
    {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription()
    {
        return "Will play a sound when you run low on heart";

    }

}
