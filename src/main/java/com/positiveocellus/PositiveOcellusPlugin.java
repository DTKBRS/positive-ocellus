package com.positiveocellus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.NPC;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.*;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(name="Positive Ocellus", description="Replace Ocellus's overhead dialogue with encouraging text", tags={"tfa", "ocellus", "dialogue", "text"})
public class PositiveOcellusPlugin extends Plugin {
    @Inject private ClientToolbar toolbar;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path directory = RuneLite.RUNELITE_DIR.toPath().resolve("positive-ocellus");
    private volatile TextPack active;
    private volatile boolean running;
    private boolean replacing;
    private ExecutorService io;
    private TextPanel panel;
    private NavigationButton navigation;

    @Override protected void startUp() {
        running = true;
        io = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "positive-ocellus-settings"); t.setDaemon(true); return t;
        });
        panel = new TextPanel(this);
        navigation = NavigationButton.builder().tooltip("Positive Ocellus")
            .icon(ImageUtil.loadImageResource(getClass(), "/com/positiveocellus/icon.png")).panel(panel).build();
        toolbar.addNavigation(navigation); reload();
    }

    @Override protected void shutDown() {
        running = false; active = null;
        toolbar.removeNavigation(navigation); io.shutdownNow();
    }

    void reload() {
        ExecutorService worker = io;
        TextPanel target = panel;
        worker.submit(() -> {
            try {
                Path file = directory.resolve("text-pack.json");
                Files.createDirectories(directory);
                if (!Files.exists(file))
                    try (InputStream in = getClass().getResourceAsStream("/com/positiveocellus/text-pack.json")) { Files.copy(in, file); }
                TextPack pack = TextPack.read(file, gson);
                if (!running || io != worker) return;
                active = pack;
                TextPack editable = gson.fromJson(gson.toJson(pack), TextPack.class);
                SwingUtilities.invokeLater(() -> { if (running && panel == target) target.loaded(editable); });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> target.status("Load failed: " + e.getMessage()));
            }
        });
    }

    void save(TextPack pack) {
        TextPack snapshot = gson.fromJson(gson.toJson(pack), TextPack.class);
        snapshot.validate();
        ExecutorService worker = io;
        TextPanel target = panel;
        worker.submit(() -> {
            try {
                snapshot.save(directory.resolve("text-pack.json"), gson);
                if (!running || io != worker) return;
                active = snapshot;
                SwingUtilities.invokeLater(() -> target.status("Saved. Changes apply to the next Ocellus line."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> target.status("Save failed: " + e.getMessage()));
            }
        });
    }

    @Subscribe public void onOverheadTextChanged(OverheadTextChanged event) {
        TextPack pack = active;
        if (replacing || pack == null || !(event.getActor() instanceof NPC)
            || !"Ocellus".equalsIgnoreCase(event.getActor().getName())) return;
        String text = pack.replacement(event.getOverheadText());
        if (text == null || text.equals(event.getOverheadText())) return;
        replacing = true;
        try { event.getActor().setOverheadText(text); }
        finally { replacing = false; }
    }
}
