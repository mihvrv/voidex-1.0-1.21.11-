package dev.voidex.ui;

import net.minecraft.client.gui.DrawContext;

public final class UI {
    private UI() {}

    public static int opaque(int rgb) { return rgb | 0xFF000000; }

    public static int alpha(int rgb, int a) { return (a << 24) | (rgb & 0xFFFFFF); }

    public static int tint(int rgb, float k) {
        int r = (int) (((rgb >> 16) & 255) * k);
        int g = (int) (((rgb >> 8) & 255) * k);
        int b = (int) ((rgb & 255) * k);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Filled rounded rectangle (radius r, in GUI pixels). */
    public static void round(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { c.fill(x, y, x + w, y + h, col); return; }
        c.fill(x, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.floor(Math.sqrt(r * (double) r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        round(c, x, y, w, h, 3, col);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }
}
