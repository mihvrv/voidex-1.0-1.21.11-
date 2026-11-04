package dev.voidex;

public final class Zoom {
    public static float factor = 1f;

    private Zoom() {}

    public static void update(boolean held) {
        float target = held ? VoidexConfig.zoomFactor : 1f;
        factor += (target - factor) * 0.35f;
        if (Math.abs(target - factor) < 0.002f) factor = target;
    }
}
