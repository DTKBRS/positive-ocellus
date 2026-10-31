package com.positiveocellus;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
public class PositiveOcellusLauncher {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(PositiveOcellusPlugin.class);
        RuneLite.main(args);
    }
}
