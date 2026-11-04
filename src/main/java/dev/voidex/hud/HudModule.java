package dev.voidex.hud;

import dev.voidex.VoidexConfig;
import dev.voidex.ui.UI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Function;

public class HudModule {
    public static final int PAD = 4;
    private static final int FONT_H = 9;

    public final String id, name;
    private final Function<MinecraftClient, String> text;

    public boolean enabled = true, background = true, shadow = true;
    public int x, y, color = 0xFFFFFF;
    public float scale = 1f;

    public HudModule(String id, String name, Function<MinecraftClient, String> text) {
        this.id = id;
        this.name = name;
        this.text = text;
    }

    public int getWidth(MinecraftClient mc) {
        return Math.round((mc.textRenderer.getWidth(text.apply(mc)) + PAD * 2) * scale);
    }

    public int getHeight(MinecraftClient mc) {
        return Math.round((FONT_H + PAD * 2) * scale);
    }

    public boolean contains(MinecraftClient mc, double mx, double my) {
        return mx >= x && my >= y && mx < x + getWidth(mc) && my < y + getHeight(mc);
    }

    public void render(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        String t = text.apply(mc);
        int w = tr.getWidth(t) + PAD * 2;
        int h = FONT_H + PAD * 2;

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate((float) x, (float) y);
        ctx.getMatrices().scale(scale, scale);
        if (background) {
            UI.rrect(ctx, 0, 0, w, h, 0x90000000);
            ctx.fill(1, h - 1, w - 1, h, UI.opaque(VoidexConfig.accent));
        }
        ctx.drawText(tr, t, PAD, PAD, UI.opaque(color), shadow);
        ctx.getMatrices().popMatrix();
    }
}
