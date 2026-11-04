package dev.voidex.ui;

import dev.voidex.VoidexConfig;
import dev.voidex.hud.HudModule;
import dev.voidex.ui.Controls.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoidexMenuScreen extends Screen {
    private static final int PW = 500, PH = 300;
    private static final int CARD_W = 152, CARD_H = 60, GAP = 8;
    private static final int[] COLORS = {0xFFFFFF, 0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x8B5CF6, 0xFF79C6};
    private static final int[] ACCENTS = {0x8B5CF6, 0x3B82F6, 0x10B981, 0xF43F5E, 0xF59E0B, 0xEC4899, 0x06B6D4};

    private final Screen parent;
    private final List<Ctl> ctls = new ArrayList<>();
    private Ctl dragging;

    private int tab = 0;          // 0 = Modules, 1 = Settings
    private int sub = -1;         // index of the module being configured, -1 = card grid
    private final StringBuilder query = new StringBuilder();
    private boolean searchFocused = false;

    private int px, py;
    private float s = 1f;

    public VoidexMenuScreen(Screen parent) {
        super(Text.literal("Voidex"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Shrink the whole menu so it always fits the window.
        s = Math.min(1f, Math.min((width - 16f) / PW, (height - 16f) / PH));
        if (s < 0.3f) s = 0.3f;
        float vw = width / s, vh = height / s;
        px = (int) ((vw - PW) / 2f);
        py = (int) ((vh - PH) / 2f);
        rebuild();
    }

    private static String describe(HudModule m) {
        switch (m.id) {
            case "fps": return "Frames per second";
            case "cps": return "Clicks per second";
            case "coords": return "Your XYZ position";
            case "facing": return "Direction you face";
            case "ping": return "Server latency";
            case "clock": return "Current time";
            default: return "HUD element";
        }
    }

    // ---- layout helpers ----
    private int tabX(int i) { return px + 100 + i * 78; }
    private boolean inTab(int i, int x, int y) { return x >= tabX(i) && x < tabX(i) + 72 && y >= py + 9 && y < py + 31; }
    private boolean showSearch() { return tab == 0 && sub < 0; }
    private int searchX() { return px + PW - 12 - 170; }
    private boolean inSearch(int x, int y) { return showSearch() && x >= searchX() && x < searchX() + 170 && y >= py + 9 && y < py + 31; }

    private List<Integer> visible() {
        List<Integer> out = new ArrayList<>();
        String q = query.toString().trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < VoidexConfig.modules.size(); i++) {
            HudModule m = VoidexConfig.modules.get(i);
            if (q.isEmpty() || m.name.toLowerCase(Locale.ROOT).contains(q) || describe(m).toLowerCase(Locale.ROOT).contains(q)) out.add(i);
        }
        return out;
    }

    private int cardX(int slot) { return px + 12 + (slot % 3) * (CARD_W + GAP); }
    private int cardY(int slot) { return py + 50 + (slot / 3) * (CARD_H + GAP); }
    private boolean inToggle(int slot, int x, int y) {
        int tx = cardX(slot) + CARD_W - 10 - 28, ty = cardY(slot) + CARD_H - 10 - 13;
        return x >= tx - 2 && x < tx + 30 && y >= ty - 2 && y < ty + 15;
    }

    private void rebuild() {
        ctls.clear();
        int rx = px + 12, rw = PW - 24, y;
        if (tab == 0 && sub >= 0) {
            final HudModule m = VoidexConfig.modules.get(sub);
            ctls.add(new Btn(px + 12, py + 46, 64, 20, () -> "< Back", () -> { sub = -1; rebuild(); }));
            y = py + 78;
            ctls.add(new Toggle(rx, y, rw, "Enabled", () -> m.enabled, v -> m.enabled = v)); y += 25;
            ctls.add(new Toggle(rx, y, rw, "Background", () -> m.background, v -> m.background = v)); y += 25;
            ctls.add(new Toggle(rx, y, rw, "Text shadow", () -> m.shadow, v -> m.shadow = v)); y += 25;
            ctls.add(new Slider(rx, y, rw, "Scale", 0.5f, 2.0f, () -> m.scale, v -> m.scale = v)); y += 25;
            ctls.add(new Swatches(rx, y, rw, "Text color", COLORS, () -> m.color, v -> m.color = v));
        } else if (tab == 1) {
            y = py + 50;
            ctls.add(new Swatches(rx, y, rw, "Accent color", ACCENTS, () -> VoidexConfig.accent, v -> VoidexConfig.accent = v)); y += 25;
            ctls.add(new Slider(rx, y, rw, "Zoom level", 0.1f, 0.8f, () -> VoidexConfig.zoomFactor, v -> VoidexConfig.zoomFactor = v)); y += 25;
            ctls.add(new Toggle(rx, y, rw, "Custom title screen", () -> VoidexConfig.customTitle, v -> VoidexConfig.customTitle = v));
        }
        int by = py + PH - 34;
        ctls.add(new Btn(px + 12, by, 130, 22, () -> "Edit HUD Layout", () -> client.setScreen(new HudEditorScreen(this))));
        ctls.add(new Btn(px + PW - 112, by, 100, 22, () -> "Done", this::close));
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xB0000000);
    }

    @Override
    public void render(DrawContext c, int mx0, int my0, float delta) {
        renderBackground(c, mx0, my0, delta);
        int mx = (int) (mx0 / s), my = (int) (my0 / s);
        int acc = UI.opaque(VoidexConfig.accent);

        c.getMatrices().pushMatrix();
        c.getMatrices().scale(s, s);

        // panel
        UI.round(c, px - 1, py - 1, PW + 2, PH + 2, 8, UI.alpha(VoidexConfig.accent, 0x55));
        UI.round(c, px, py, PW, PH, 7, 0xF20B0B0E);

        // header: logo, tabs, search
        UI.round(c, px + 14, py + 12, 3, 14, 1, acc);
        c.drawText(textRenderer, Text.literal("VOIDEX").formatted(Formatting.BOLD), px + 23, py + 15, 0xFFFFFFFF, false);
        String[] tabs = {"Modules", "Settings"};
        for (int i = 0; i < 2; i++) {
            boolean sel = tab == i;
            boolean hov = inTab(i, mx, my);
            if (sel) UI.round(c, tabX(i), py + 9, 72, 22, 5, UI.alpha(VoidexConfig.accent, 0x55));
            else if (hov) UI.round(c, tabX(i), py + 9, 72, 22, 5, 0x18FFFFFF);
            int tw = textRenderer.getWidth(tabs[i]);
            c.drawText(textRenderer, tabs[i], tabX(i) + (72 - tw) / 2, py + 16, sel ? 0xFFFFFFFF : 0xFF9A9AA6, false);
        }
        if (showSearch()) {
            int sx = searchX();
            UI.round(c, sx - 1, py + 8, 172, 24, 6, searchFocused ? acc : 0xFF24242C);
            UI.round(c, sx, py + 9, 170, 22, 5, 0xFF121218);
            String shown = query.toString();
            if (shown.isEmpty() && !searchFocused) {
                c.drawText(textRenderer, "Search modules...", sx + 9, py + 16, 0xFF6A6A76, false);
            } else {
                while (textRenderer.getWidth(shown) > 150 && !shown.isEmpty()) shown = shown.substring(1);
                c.drawText(textRenderer, shown, sx + 9, py + 16, 0xFFFFFFFF, false);
                if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
                    c.fill(sx + 9 + textRenderer.getWidth(shown), py + 14, sx + 10 + textRenderer.getWidth(shown), py + 26, 0xFFFFFFFF);
                }
            }
        }
        c.fill(px + 10, py + 38, px + PW - 10, py + 39, 0x1CFFFFFF);

        // content
        if (tab == 0 && sub < 0) {
            List<Integer> vis = visible();
            if (vis.isEmpty()) {
                String none = "No modules match your search";
                c.drawText(textRenderer, none, px + (PW - textRenderer.getWidth(none)) / 2, py + 120, 0xFF6A6A76, false);
            }
            for (int slot = 0; slot < vis.size(); slot++) {
                HudModule m = VoidexConfig.modules.get(vis.get(slot));
                int cx = cardX(slot), cy = cardY(slot);
                boolean hov = mx >= cx && mx < cx + CARD_W && my >= cy && my < cy + CARD_H;
                UI.round(c, cx, cy, CARD_W, CARD_H, 6, hov ? 0xFF1C1C25 : 0xFF14141B);
                if (m.enabled) UI.round(c, cx, cy + 10, 3, CARD_H - 20, 1, acc);
                c.drawText(textRenderer, m.name, cx + 12, cy + 10, 0xFFFFFFFF, false);
                c.drawText(textRenderer, describe(m), cx + 12, cy + 23, 0xFF7A7A86, false);
                c.drawText(textRenderer, "Configure", cx + 12, cy + CARD_H - 19, hov ? acc : 0xFF55555F, false);
                int tx = cx + CARD_W - 10 - 28, ty = cy + CARD_H - 10 - 13;
                UI.round(c, tx, ty, 28, 13, 6, m.enabled ? acc : 0xFF30303A);
                UI.round(c, m.enabled ? tx + 28 - 11 : tx + 2, ty + 2, 9, 9, 4, 0xFFFFFFFF);
            }
        } else if (tab == 0) {
            HudModule m = VoidexConfig.modules.get(sub);
            c.drawText(textRenderer, Text.literal(m.name).formatted(Formatting.BOLD), px + 86, py + 48, 0xFFFFFFFF, false);
            c.drawText(textRenderer, describe(m), px + 86, py + 60, 0xFF7A7A86, false);
        } else {
            c.drawText(textRenderer, "GENERAL", px + 14, py + 40 + 1, 0xFF6A6A76, false);
        }

        for (Ctl ctl : ctls) ctl.draw(c, mx, my);

        c.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int x = (int) (click.x() / s), y = (int) (click.y() / s);

        searchFocused = inSearch(x, y);
        for (int i = 0; i < 2; i++) {
            if (inTab(i, x, y)) { tab = i; sub = -1; searchFocused = false; rebuild(); return true; }
        }
        if (searchFocused) return true;

        for (Ctl ctl : ctls) {
            if (ctl.hit(x, y)) { ctl.click(x, y); dragging = ctl; return true; }
        }
        if (tab == 0 && sub < 0) {
            List<Integer> vis = visible();
            for (int slot = 0; slot < vis.size(); slot++) {
                int cx = cardX(slot), cy = cardY(slot);
                if (x >= cx && x < cx + CARD_W && y >= cy && y < cy + CARD_H) {
                    HudModule m = VoidexConfig.modules.get(vis.get(slot));
                    if (inToggle(slot, x, y)) m.enabled = !m.enabled;
                    else { sub = vis.get(slot); rebuild(); }
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragging != null) { dragging.drag((int) (click.x() / s), (int) (click.y() / s)); return true; }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.isValidChar() && query.length() < 24) {
            query.appendCodePoint(input.codepoint());
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (searchFocused) {
            int key = input.key();
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (query.length() > 0) query.deleteCharAt(query.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        VoidexConfig.save();
        client.setScreen(parent);
    }
}
