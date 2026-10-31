package com.positiveocellus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(name="Positive Ocellus", description="Encouraging, editable Ocellus voice replacements", tags={"tfa", "sound", "voice"})
public class PositiveOcellusPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private PositiveOcellusConfig config;
    @Inject private ClientToolbar toolbar;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path directory = RuneLite.RUNELITE_DIR.toPath().resolve("positive-ocellus");
    private volatile Map<String, VoiceLine> mappings = Collections.emptyMap();
    private volatile VoicePlayer player;
    private ExecutorService io;
    private VoicePanel panel;
    private NavigationButton navigation;
    private boolean nearby;
    private String lastOverhead = "";
    private final Set<String> captured = new HashSet<>();

    @Provides PositiveOcellusConfig provideConfig(ConfigManager manager) { return manager.getConfig(PositiveOcellusConfig.class); }

    @Override protected void startUp()
    {
        player = new VoicePlayer();
        io = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "positive-ocellus-files"); t.setDaemon(true); return t; });
        panel = new VoicePanel(this);
        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/com/positiveocellus/icon.png");
        navigation = NavigationButton.builder().tooltip("Positive Ocellus").icon(icon).panel(panel).build();
        toolbar.addNavigation(navigation);
        reload();
    }

    @Override protected void shutDown()
    {
        mappings = Collections.emptyMap(); nearby = false; captured.clear();
        toolbar.removeNavigation(navigation);
        io.shutdownNow(); player.close();
    }

    void reload()
    {
        VoicePlayer target = player;
        VoicePanel targetPanel = panel;
        io.submit(() -> {
            try
            {
                Files.createDirectories(directory);
                Path file = directory.resolve("voice-pack.json");
                if (!Files.exists(file))
                    try (InputStream in = getClass().getResourceAsStream("/com/positiveocellus/voice-pack.json")) { Files.copy(in, file); }
                VoicePack pack = VoicePack.read(file, gson);
                mappings = Collections.emptyMap();
                int loaded = target.load(pack, directory);
                Map<String, VoiceLine> next = new HashMap<>();
                for (VoiceLine line : pack.lines)
                    if (line.enabled && line.soundId >= 0) next.put(line.channel + ":" + line.soundId, line);
                if (Thread.currentThread().isInterrupted() || target != player) return;
                mappings = Collections.unmodifiableMap(next);
                SwingUtilities.invokeLater(() -> targetPanel.loaded(pack, loaded));
            }
            catch (Exception e) { SwingUtilities.invokeLater(() -> targetPanel.status("Load failed: " + e.getMessage())); }
        });
    }

    void save(VoicePack pack)
    {
        VoicePack snapshot = gson.fromJson(gson.toJson(pack), VoicePack.class);
        io.submit(() -> {
            try { snapshot.save(directory.resolve("voice-pack.json"), gson); reload(); }
            catch (Exception e) { SwingUtilities.invokeLater(() -> panel.status("Save failed: " + e.getMessage())); }
        });
    }

    void preview(String key) { if (!player.play(key, config.volume(), 0)) panel.status("Audio unavailable or already playing. Save/reload after changes."); }
    Path directory() { return directory; }

    @Subscribe public void onGameTick(GameTick event)
    {
        boolean wasNearby = nearby;
        nearby = client.getNpcs().stream().anyMatch(n -> "Ocellus".equalsIgnoreCase(n.getName()));
        if (wasNearby && !nearby) { player.stop(); lastOverhead = ""; captured.clear(); }
    }

    @Subscribe public void onGameStateChanged(GameStateChanged e)
    {
        if (e.getGameState() != GameState.LOGGED_IN) { nearby = false; player.stop(); captured.clear(); lastOverhead = ""; }
    }

    @Subscribe public void onOverheadTextChanged(OverheadTextChanged e)
    {
        if (!(e.getActor() instanceof NPC) || !"Ocellus".equalsIgnoreCase(e.getActor().getName())) return;
        lastOverhead = e.getOverheadText();
        if (config.capture()) report("OVERHEAD " + lastOverhead);
    }

    private boolean replace(String channel, int id, int delay, Actor source)
    {
        if (!nearby) return false;
        if (config.capture() && captured.size() < 500 && captured.add(channel + ":" + id + ":" + lastOverhead))
            report(channel + " ID=" + id + " source=" + (source == null ? "unknown" : source.getName()) + " | recent overhead: " + lastOverhead);
        VoiceLine line = mappings.get(channel + ":" + id);
        if (line == null) return false;
        int gameVolume = "area".equals(channel) ? client.getPreferences().getAreaSoundEffectVolume() : client.getPreferences().getSoundEffectVolume();
        int volume = Math.round(config.volume() * gameVolume / 127f);
        return player.play(line.key, volume, delay);
    }
    private void report(String text)
    {
        String message = LocalTime.now().withNano(0) + " " + text;
        SwingUtilities.invokeLater(() -> panel.capture(message));
    }
    @Subscribe public void onSoundEffectPlayed(SoundEffectPlayed e)
    {
        if (!e.isConsumed() && replace("sound", e.getSoundId(), e.getDelay(), e.getSource())) e.consume();
    }
    @Subscribe public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed e)
    {
        if (!e.isConsumed() && replace("area", e.getSoundId(), e.getDelay(), e.getSource())) e.consume();
    }
}
