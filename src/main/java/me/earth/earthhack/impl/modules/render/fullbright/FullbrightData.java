package me.earth.earthhack.impl.modules.render.fullbright;

import me.earth.earthhack.api.module.data.DefaultData;

final class FullbrightData extends DefaultData<Fullbright>
{
    public FullbrightData(Fullbright module)
    {
        super(module);
        register(module.mode, "-Gamma make the mc gamma 1000\n" +
                "-Potion use night vision\n" +
                "-Auto if light level above 7 then use normal minecraft light");
    }

    @Override
    public int getColor()
    {
        return 0xffffffff;
    }

    @Override
    public String getDescription()
    {
        return "Brighten your world.";
    }
}
