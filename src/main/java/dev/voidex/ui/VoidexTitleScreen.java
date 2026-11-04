package dev.voidex.ui;

import dev.voidex.VoidexConfig;
import dev.voidex.ui.Controls.Btn;
import dev.voidex.ui.Controls.Ctl;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class VoidexTitleScreen extends Screen {
    private final List<Ctl> ctls = new ArrayList<>();
    private float s = 1f;
    private float vw, vh;

    public VoidexTitleScreen() {
        super(Text.literal("Voidex"));
    }

    @Override
    protected void init() {
        // Scale the layout down if the window is small so nothing gets cut off.
        s = Math.min(1f, Math.min(width / 240f, height / 250f));
        if (s < 0.3f) s = 0.3f;
        vw = width / s;
        vh = height / s;

        ctls.clear();
        int bw = 200, bh = 24, x = (int) ((vw - bw) / 2f), y = (int) (vh / 2f - 8);
        ctls.add(new Btn(x, y, bw, bh, () -> "Singleplayer", () -> client.setScreen(new SelectWorldScreen(this))));
        y += bh + 6;
        ctls.add(new Btn(x, y, bw, bh, () -> "Multiplayer", () -> client.setScreen(new MultiplayerScreen(this))));
        y += bh + 6;
        ctls.add(new Btn(x, y, bw, bh, () -> "Voidex Settings", () -> client.setScreen(new VoidexMenuScreen(this))));
        y += bh + 6;
        ctls.add(new Btn(x, y, 97, bh, () -> "Options", () -> client.setScreen(new OptionsScreen(this, client.options))));
        ctls.add(new Btn(x + 103, y, 97, bh, () -> "Quit", () -> client.scheduleStop()));
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        c.fillGradient(0, 0, width, height, 0xFF09090F, UI.tint(VoidexConfig.accent, 0.28f));
    }

    @Override
    public void render(DrawContext c, int mx0, int my0, float delta) {
        renderBackground(c, mx0, my0, delta);
        int mx = (int) (mx0 / s), my = (int) (my0 / s);
        float t = (System.currentTimeMillis() % 100000L) / 1000f;

        c.getMatrices().pushMatrix();
        c.getMatrices().scale(s, s);

        c.getMatrices().pushMatrix();
        c.getMatrices().translate(vw / 2f, vh / 2f - 90f);
        c.getMatrices().scale(5f, 5f);
        c.drawText(textRenderer, "VOIDEX", -textRenderer.getWidth("VOIDEX") / 2, 0, 0xFFFFFFFF, false);
        c.getMatrices().popMatrix();

        int lw = (int) (60 + 10 * Math.sin(t * 2));
        int ly = (int) (vh / 2f - 38);
        UI.round(c, (int) (vw / 2f) - lw, ly, lw * 2, 2, 1, UI.opaque(VoidexConfig.accent));

        for (Ctl ctl : ctls) ctl.draw(c, mx, my);
        c.drawText(textRenderer, "Voidex 1.0", 6, (int) vh - 12, 0xFF6A6A76, false);

        c.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            int mx = (int) (click.x() / s), my = (int) (click.y() / s);
            for (Ctl ctl : ctls) {
                if (ctl.hit(mx, my)) { ctl.click(mx, my); return true; }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
