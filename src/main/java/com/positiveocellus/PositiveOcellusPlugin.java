package com.positiveocellus;

import com.google.gson.Gson;
import net.runelite.api.NPC;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name="Positive Ocellus", description="Replace Ocellus's overhead dialogue with encouraging text", tags={"tfa", "ocellus", "dialogue", "text"})
public class PositiveOcellusPlugin extends Plugin {
    private volatile TextPack active;
    private boolean replacing;

    @Override protected void startUp() throws Exception {
        active = TextPack.bundled(new Gson());
    }

    @Override protected void shutDown() {
        active = null;
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
