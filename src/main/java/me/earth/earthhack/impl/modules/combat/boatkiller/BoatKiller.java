package me.earth.earthhack.impl.modules.combat.boatkiller;

import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.util.helpers.disabling.DisablingModule;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketVehicleMove;

/**
 * Ported code from log4j.
 * @author Smug2 .
 */

public class BoatKiller extends DisablingModule {

    public BoatKiller() {
        super("BoatKiller", Category.Combat);
        setData(new BoatKillerData(this));
    }

    protected final Setting<Integer> loop = register(new NumberSetting<>("Loop", 4, 1, 20));
    protected final Setting<Float> y = register(new NumberSetting<>("Y1", -0.05f, -10f, 10f));
    protected final Setting<Float> y2 = register(new NumberSetting<>("Y2", 3f, -300f, 300f));

    @Override
    protected void onEnable() {
        super.onEnable();
        int loopCountMax = loop.getValue();
        for (int loopCount = 0; loopCount < loopCountMax; loopCount++) {
            if (mc.player.getRidingEntity() instanceof EntityBoat) {
                EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();
                Vec3d originalPos = boat.getPositionVector();
                boat.setPosition(boat.posX, boat.posY + y.getValue(), boat.posZ);
                CPacketVehicleMove groundPacket = new CPacketVehicleMove(boat);
                boat.setPosition(boat.posX, boat.posY + y2.getValue(), boat.posZ);
                CPacketVehicleMove skyPacket = new CPacketVehicleMove(boat);
                boat.setPosition(originalPos.x, originalPos.y, originalPos.z);
                for (int i = 0; i < 100; i++) {
                    mc.player.connection.sendPacket(skyPacket);
                    mc.player.connection.sendPacket(groundPacket);
                }
                mc.player.connection.sendPacket(new CPacketVehicleMove(boat));
            }
        }
        disable();
    }
}