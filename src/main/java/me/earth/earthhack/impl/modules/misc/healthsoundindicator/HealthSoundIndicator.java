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
    protected final Setting<Integer> friendDelay =
            register(new NumberSetting<>("FriendMessageDelay", 15000, 1, 30000));
    protected final Setting<Boolean> broadCast =
            register(new BooleanSetting("BroadCast", false));
    protected final Setting<Boolean> tellFriends =
            register(new BooleanSetting("Tell-Friends", false));
    protected final Setting<Boolean> showHealth =
            register(new BooleanSetting("Show-Health", true));
    protected final Setting<Boolean> showCoords =
            register(new BooleanSetting("Show-Coords", true));
    protected final Setting<Boolean> showDimension =
            register(new BooleanSetting("Show-Dimension", true));

    public HealthSoundIndicator() {
        super("HealthIndicator", Category.Misc);
        setData(new HealthSoundIndicatorData(this));
        this.listeners.add(new LambdaListener<>(TickEvent.class, e -> {
            if (e.isSafe()) {
                String message = createMessage();
                if (!message.isEmpty() && System.currentTimeMillis() - lastMessageTime >= delay.getValue()) {
                    sendMessage(message);
                    lastMessageTime = System.currentTimeMillis();
                }

                if (tellFriends.getValue()) {
                    String ownName = mc.player.getName();
                    for (String friend : Managers.FRIENDS.getPlayers()) {
                        if (!Objects.equals(friend, ownName)) {
                            for (EntityPlayer loadedPlayer : mc.world.playerEntities) {
                                if (Objects.equals(friend, loadedPlayer.getName()) &&
                                        System.currentTimeMillis() - lastMessageTime >= friendDelay.getValue()) {
                                    mc.player.sendChatMessage("/msg " + friend + " " + message);
                                    lastMessageTime = System.currentTimeMillis();
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
        if (mc.world == null || mc.player == null) {
            toggle();
        }
    }

    @Override
    public String getDisplayInfo() {
        int currentHealth = (int) mc.player.getHealth();
        TextFormatting color = currentHealth <= health.getValue() ? TextFormatting.RED : TextFormatting.GREEN;
        return "Health: " + color + currentHealth;
    }

    private void sendMessage(String message) {
        if (broadCast.getValue()) {
            mc.player.sendChatMessage(message);
        } else {
            Managers.CHAT.sendDeleteMessage(message, this.getName(), ChatIDs.MODULE);
        }
    }

    private String createMessage() {
        int currentHealth = (int) mc.player.getHealth();
        String coords = showCoords.getValue()
                ? String.format(" at coordinates: X %.1f, Y %.1f, Z %.1f!",
                mc.player.posX, mc.player.posY, mc.player.posZ)
                : "";

        String dimension = "";
        if (showDimension.getValue()) {
            int playerDimension = mc.player.dimension;
            if (playerDimension == -1) {
                dimension = " in the Nether";
            } else if (playerDimension == 0) {
                dimension = " in the Overworld";
            } else if (playerDimension == 1) {
                dimension = " in the End";
            } else {
                dimension = " in Dimension " + playerDimension;
            }
        }

        String message = "";
        if (showHealth.getValue() && currentHealth <= health.getValue()) {
            message = "My current health is " + currentHealth + coords + dimension;
        } else if (!showHealth.getValue() && currentHealth <= health.getValue()) {
            message = "I got low health" + coords + dimension;
        }
        return message;
    }
}
