package dev.voidex.ui;

import dev.voidex.VoidexConfig;
import dev.voidex.hud.HudModule;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule drag;
    private int ox, oy;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x66000000);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        renderBackground(c, mx, my, delta);
        String hint = "Drag modules to move them  -  Esc to save & go back";
        c.drawText(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, 10, 0xFFCFCFD6, false);

        for (HudModule m : VoidexConfig.modules) {
            if (!m.enabled) continue;
            m.render(c, client);
            if (m == drag || m.contains(client, mx, my)) {
                UI.outline(c, m.x - 1, m.y - 1, m.getWidth(client) + 2, m.getHeight(client) + 2,
                        UI.opaque(VoidexConfig.accent));
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() == 0) {
            for (int i = VoidexConfig.modules.size() - 1; i >= 0; i--) {
                HudModule m = VoidexConfig.modules.get(i);
                if (m.enabled && m.contains(client, mx, my)) {
                    drag = m;
                    ox = (int) mx - m.x;
                    oy = (int) my - m.y;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        double mx = click.x(), my = click.y();
        if (drag != null) {
            drag.x = Math.max(0, Math.min(width - drag.getWidth(client), (int) mx - ox));
            drag.y = Math.max(0, Math.min(height - drag.getHeight(client), (int) my - oy));
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        drag = null;
        return super.mouseReleased(click);
    }

    @Override
    public void close() {
        VoidexConfig.save();
        client.setScreen(parent);
    }
}
