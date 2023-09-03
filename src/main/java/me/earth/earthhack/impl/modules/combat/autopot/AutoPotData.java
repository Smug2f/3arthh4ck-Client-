package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.data.DefaultData;

final class AutoPotData extends DefaultData<AutoPot>
{
    public AutoPotData (AutoPot module)
    {
        super(module);
        register(module.speedDelay, "How long to wait after each throw  after the speed effect ran out");
        register(module.EnnemyRange, "will throw your pot up if no player are found around x blocks");
        register(module.NoEnnemyP, "NoEnnemy Pitch:will throw your pot at the desired pitch if there are no player and immobile");
    }

    @Override
    public int getColor()
    {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription()
    {
        return "Throw potions to help you in pvp";

    }

}
