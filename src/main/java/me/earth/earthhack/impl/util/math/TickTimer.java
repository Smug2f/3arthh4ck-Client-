package me.earth.earthhack.impl.util.math;

public class TickTimer {
    private long lastTime;
    private final int ticks;

    public TickTimer(int ticks) {
        this.ticks = ticks;
        reset();
    }

    public boolean hasReached() {
        long currentTime = System.currentTimeMillis();
        int elapsedTicks = (int) ((currentTime - lastTime) / 50L); // Convert to ticks
        return elapsedTicks >= ticks;
    }

    public void reset() {
        lastTime = System.currentTimeMillis();
    }
}
