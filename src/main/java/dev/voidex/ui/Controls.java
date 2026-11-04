package dev.voidex.ui;

import dev.voidex.VoidexConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Controls {
    private Controls() {}

    private static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }
    private static int accent() { return UI.opaque(VoidexConfig.accent); }

    private static void row(DrawContext c, int x, int y, int w, int h, boolean hover) {
        UI.round(c, x, y, w, h, 4, hover ? 0x2CFFFFFF : 0x16FFFFFF);
    }

    public static abstract class Ctl {
        public final int x, y, w, h;

        protected Ctl(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        public boolean hit(int mx, int my) { return mx >= x && my >= y && mx < x + w && my < y + h; }
        public abstract void draw(DrawContext c, int mx, int my);
        public void click(int mx, int my) {}
        public void drag(int mx, int my) {}
    }

    public static final class Toggle extends Ctl {
        private final String label;
        private final BooleanSupplier get;
        private final Consumer<Boolean> set;

        public Toggle(int x, int y, int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
            super(x, y, w, 21);
            this.label = label; this.get = get; this.set = set;
        }

        @Override public void draw(DrawContext c, int mx, int my) {
            boolean on = get.getAsBoolean();
            row(c, x, y, w, h, hit(mx, my));
            c.drawText(tr(), label, x + 9, y + (h - 8) / 2, 0xFFE6E6EA, false);
            int pw = 28, ph = 13, bx = x + w - pw - 9, by = y + (h - ph) / 2;
            UI.round(c, bx, by, pw, ph, 6, on ? accent() : 0xFF30303A);
            int kx = on ? bx + pw - ph + 2 : bx + 2;
            UI.round(c, kx, by + 2, ph - 4, ph - 4, 4, 0xFFFFFFFF);
        }

        @Override public void click(int mx, int my) { set.accept(!get.getAsBoolean()); }
    }

    public static final class Slider extends Ctl {
        private final String label;
        private final float min, max;
        private final Supplier<Float> get;
        private final Consumer<Float> set;

        public Slider(int x, int y, int w, String label, float min, float max, Supplier<Float> get, Consumer<Float> set) {
            super(x, y, w, 21);
            this.label = label; this.min = min; this.max = max; this.get = get; this.set = set;
        }

        private int tx() { return x + w - 120; }
        private int tw() { return 106; }

        @Override public void draw(DrawContext c, int mx, int my) {
            row(c, x, y, w, h, hit(mx, my));
            c.drawText(tr(), label, x + 9, y + (h - 8) / 2, 0xFFE6E6EA, false);
            float v = get.get();
            float t = (v - min) / (max - min);
            int ty = y + h / 2 - 2;
            UI.round(c, tx(), ty, tw(), 4, 2, 0xFF30303A);
            int fill = Math.max(4, (int) (tw() * t));
            UI.round(c, tx(), ty, fill, 4, 2, accent());
            UI.round(c, tx() + fill - 4, y + h / 2 - 4, 8, 8, 4, 0xFFFFFFFF);
            String val = String.format(Locale.ROOT, "%.2f", v);
            c.drawText(tr(), val, tx() - 8 - tr().getWidth(val), y + (h - 8) / 2, 0xFF9A9AA6, false);
        }

        @Override public void click(int mx, int my) { drag(mx, my); }

        @Override public void drag(int mx, int my) {
            float t = Math.max(0f, Math.min(1f, (mx - tx()) / (float) tw()));
            set.accept(min + t * (max - min));
        }
    }

    public static final class Swatches extends Ctl {
        private final String label;
        private final int[] colors;
        private final IntSupplier get;
        private final IntConsumer set;

        public Swatches(int x, int y, int w, String label, int[] colors, IntSupplier get, IntConsumer set) {
            super(x, y, w, 21);
            this.label = label; this.colors = colors; this.get = get; this.set = set;
        }

        private int sx() { return x + w - colors.length * 16 - 5; }

        @Override public void draw(DrawContext c, int mx, int my) {
            row(c, x, y, w, h, false);
            c.drawText(tr(), label, x + 9, y + (h - 8) / 2, 0xFFE6E6EA, false);
            for (int i = 0; i < colors.length; i++) {
                int bx = sx() + i * 16, by = y + 4;
                if ((get.getAsInt() & 0xFFFFFF) == colors[i]) UI.round(c, bx - 2, by - 2, 16, 16, 5, 0xFFFFFFFF);
                UI.round(c, bx, by, 12, 12, 3, UI.opaque(colors[i]));
            }
        }

        @Override public void click(int mx, int my) {
            int i = (mx - sx()) / 16;
            if (mx >= sx() && i >= 0 && i < colors.length) set.accept(colors[i]);
        }
    }

    public static final class Btn extends Ctl {
        private final Supplier<String> label;
        private final Runnable action;

        public Btn(int x, int y, int w, int h, Supplier<String> label, Runnable action) {
            super(x, y, w, h);
            this.label = label; this.action = action;
        }

        @Override public void draw(DrawContext c, int mx, int my) {
            boolean hov = hit(mx, my);
            UI.round(c, x, y, w, h, 5, hov ? accent() : UI.alpha(VoidexConfig.accent, 0x88));
            String s = label.get();
            c.drawText(tr(), s, x + (w - tr().getWidth(s)) / 2, y + (h - 8) / 2, 0xFFFFFFFF, false);
        }

        @Override public void click(int mx, int my) { action.run(); }
    }
}
