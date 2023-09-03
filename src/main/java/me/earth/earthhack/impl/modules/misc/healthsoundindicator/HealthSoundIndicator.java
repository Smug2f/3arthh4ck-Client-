package me.earth.earthhack.impl.modules.misc.healthsoundindicator;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.text.ChatIDs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;

import java.util.Objects;


/**
 * HealthSoundIndicator module by Smug2.
 * Feel free to look into.
 * @author Smug2 and Ai_24
 */
public class HealthSoundIndicator extends Module {

    // Fields
    private long lastMessageTime = 0;

    // Settings
    protected final Setting<Integer> health =
            register(new NumberSetting<>("Health", 8, 1, 19));
    protected final Setting<Integer> delay =
            register(new NumberSetting<>("Delay", 10000, 1, 30000));
    protected final Setting<Boolean> broadCast =
            register(new BooleanSetting("BroadCast", false));
    protected final Setting<Boolean> tellFriends =
            register(new BooleanSetting("Tell-Friends", false));
    protected final Setting<Boolean> showHealth =
            register(new BooleanSetting("Show-Health", true));
    protected final Setting<Boolean> showCoords =
            register(new BooleanSetting("Show-Coords", true));

    public HealthSoundIndicator() {
        super("HealthIndicator", Category.Misc);
        setData(new HealthSoundIndicatorData(this));
        this.listeners.add(new LambdaListener<>(TickEvent.class, e -> {
            if (e.isSafe()) {
                int currentHealth = (int) mc.player.getHealth();
                String coords = "!";

                if (showCoords.getValue()) {
                    coords = String.format(" at coordinates: X %.1f, Y %.1f, Z %.1f!",
                            mc.player.posX,
                            mc.player.posY,
                            mc.player.posZ);
                }

                if (showHealth.getValue() && showCoords.getValue() && currentHealth <= health.getValue() && System.currentTimeMillis() - lastMessageTime >= delay.getValue()) {
                    sendMessage("I got " + currentHealth + coords);
                } else {
                    if (!showHealth.getValue() && currentHealth <= health.getValue() && System.currentTimeMillis() - lastMessageTime >= delay.getValue()) {
                        if (showCoords.getValue()) {
                            sendMessage("I got low health" + coords);
                        } else {
                            sendMessage("I got low health.");
                        }
                        lastMessageTime = System.currentTimeMillis();
                    } else if (showHealth.getValue() && currentHealth <= health.getValue() && System.currentTimeMillis() - lastMessageTime >= delay.getValue()) {
                        sendMessage("My current health is " + currentHealth + ".");
                        lastMessageTime = System.currentTimeMillis();
                    }
                    if (tellFriends.getValue()) {
                        String ownName = mc.player.getName();
                        for (String friend : Managers.FRIENDS.getPlayers()) {
                            if (!Objects.equals(friend, ownName)) {
                                for (EntityPlayer loadedPlayer : mc.world.playerEntities) {
                                    if (Objects.equals(friend, loadedPlayer.getName())) {
                                        mc.player.sendChatMessage("/msg "
                                                + friend
                                                + " I got low health "
                                                + coords);


                            }
                        }
                    }

                }

                        }
                    }
                }
            }));
        }
    @Override
    protected void onEnable() {
        if (mc.world == null || mc.player == null)
            toggle();
    }
    @Override
    public String getDisplayInfo() {
        int currentHealth = (int) mc.player.getHealth();
        TextFormatting color = currentHealth <= health.getValue() ? TextFormatting.RED : TextFormatting.GREEN;
        return "Health: " + color + currentHealth;
    }

    private void sendMessage(String message) {
        if (broadCast.getValue())
            mc.player.sendChatMessage(message);
        else
            Managers.CHAT.sendDeleteMessage(message, this.getName(), ChatIDs.MODULE);
    }
}
