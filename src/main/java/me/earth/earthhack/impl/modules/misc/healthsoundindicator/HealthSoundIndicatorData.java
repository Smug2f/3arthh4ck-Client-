package me.earth.earthhack.impl.modules.misc.healthsoundindicator;

import me.earth.earthhack.api.module.data.DefaultData;

final class HealthSoundIndicatorData extends DefaultData<HealthSoundIndicator>
{
    public HealthSoundIndicatorData (HealthSoundIndicator module)
    {
        super(module);
        register(module.health, "When lower than x health it will send a msg");
        register(module.delay, "The amount of time to wait to send another message");
        register(module.friendDelay, "The amount of time to wait to send another message if tellfriends is enabled");
        register(module.broadCast, "broadCast:On = everyone BroadCast:off = only you");
        register(module.tellFriends, "Will tell your friends about your life choice (if you have)");
        register(module.showHealth, "Will tell your health.");
        register(module.showCoords, "Will also tell your coords.");
        register(module.showDimension, "Tell in wich dimension you're in.");
    }

    @Override
    public int getColor()
    {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription()
    {
        return "Will say your coords or play a sound when low on health (WIP)";

    }

}
