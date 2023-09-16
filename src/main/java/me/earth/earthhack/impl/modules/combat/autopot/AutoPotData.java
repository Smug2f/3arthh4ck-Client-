package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.data.DefaultData;

final class AutoPotData extends DefaultData<AutoPot>
{
    public AutoPotData (AutoPot module)
    {
        super(module);
        register(module.speed, "Throw a speed potion if on");
        register(module.speedDelay, "How long to throw another speed potion at each throw  after the speed effect ran out");
        register(module.enemyRange, "will throw your pot at NoEnnemyP if no player are found around x blocks");
        register(module.noEnemyP, "NoEnnemy Pitch:will throw your pot at the desired pitch if there are no player and immobile");
        register(module.heal, "Throw an health potion to help you recover health if on");
        register(module.healthPotThreshold, "Health:throw an heal potion if player has less than x hearths");
        register(module.healthDelay, "How long to throw another heal potion after each throw");
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
