package me.earth.earthhack.impl.modules.combat.boatkiller;

import me.earth.earthhack.api.module.data.DefaultData;

final class BoatKillerData extends DefaultData<BoatKiller>
{
    public BoatKillerData (BoatKiller module)
    {
        super(module);
        register(module.loop, "How many time the code will loop");
        register(module.y, "The first y , need to be lower than Y2 and required since you can't phase with only 1Y");
        register(module.y2, "The second y , set it to higer value than the the Y1 one.");
    }

    @Override
    public int getColor()
    {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription()
    {
        return "Let you do some hacker thing while in boat:\n" +
                "-Set Loop to 2, Y to 5.05, and Y2 to 180 for boat killer (tp above nether roof)\n"+
                "-Set Loop to 4, Y to 0.05, and Y2 to 3 for boat pop (Try to instakill who is in the boat)\n"+
                "-Set Loop to 2, Y to -5.05, and Y2 to -120(-180) for boat phase (tp under bedrock)";
    }

}
