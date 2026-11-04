package dev.voidex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.voidex.hud.HudModule;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VoidexConfig {
    public static int accent = 0x8B5CF6;
    public static boolean customTitle = true;
    public static float zoomFactor = 0.25f;
    public static final List<HudModule> modules = new ArrayList<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private VoidexConfig() {}

    public static class Mod {
        public boolean enabled = true, background = true, shadow = true;
        public int x, y, color = 0xFFFFFF;
        public float scale = 1f;
    }

    public static class Data {
        public int accent = VoidexConfig.accent;
        public boolean customTitle = true;
        public float zoomFactor = 0.25f;
        public Map<String, Mod> modules = new LinkedHashMap<>();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("voidex.json");
    }

    private static void add(HudModule m) {
        m.x = 6;
        m.y = 6 + modules.size() * 22;
        modules.add(m);
    }

    private static void registerDefaults() {
        modules.clear();
        add(new HudModule("fps", "FPS", mc -> mc.getCurrentFps() + " FPS"));
        add(new HudModule("cps", "CPS", mc -> dev.voidex.hud.CpsTracker.left() + " | " + dev.voidex.hud.CpsTracker.right() + " CPS"));
        add(new HudModule("coords", "Coordinates", mc -> mc.player == null ? "XYZ" :
                String.format("XYZ: %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())));
        add(new HudModule("facing", "Direction", mc -> {
            if (mc.player == null) return "-";
            String s = mc.player.getHorizontalFacing().asString();
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }));
        add(new HudModule("ping", "Ping", mc -> {
            if (mc.player == null || mc.getNetworkHandler() == null) return "-- ms";
            var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return (e == null ? 0 : e.getLatency()) + " ms";
        }));
        add(new HudModule("clock", "Clock", mc -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
    }

    public static void load() {
        registerDefaults();
        Path f = file();
        if (!Files.exists(f)) { save(); return; }
        try (Reader r = Files.newBufferedReader(f)) {
            Data d = GSON.fromJson(r, Data.class);
            if (d == null) return;
            accent = d.accent;
            customTitle = d.customTitle;
            zoomFactor = d.zoomFactor;
            if (d.modules == null) return;
            for (HudModule m : modules) {
                Mod s = d.modules.get(m.id);
                if (s == null) continue;
                m.enabled = s.enabled; m.background = s.background; m.shadow = s.shadow;
                m.x = s.x; m.y = s.y; m.color = s.color; m.scale = s.scale;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        Data d = new Data();
        d.accent = accent; d.customTitle = customTitle; d.zoomFactor = zoomFactor;
        for (HudModule m : modules) {
            Mod s = new Mod();
            s.enabled = m.enabled; s.background = m.background; s.shadow = m.shadow;
            s.x = m.x; s.y = m.y; s.color = m.color; s.scale = m.scale;
            d.modules.put(m.id, s);
        }
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(d, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
