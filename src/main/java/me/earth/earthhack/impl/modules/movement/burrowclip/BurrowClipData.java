package me.earth.earthhack.impl.modules.movement.burrowclip;

import me.earth.earthhack.api.module.data.DefaultData;

final class BurrowClipData extends DefaultData<BurrowClip>
{
    public BurrowClipData(BurrowClip module)
    {
        super(module);
        register(module.mode, "" +
                "-burrowclip clip into blocks " +
                "-sandclip prevent being tped by sand" +
                "- soon");
        register(module.disable, "Disable the module after clipping");
        register(module.hole, "Only clip if you are in an hole");
        register(module.burrow, "Only work if you are burrowed");
    }

    @Override
    public int getColor()
    {
        return 0xffffffff;
    }

    @Override
    public String getDescription()
    {
        return "Clips into blocks";
    }

}
