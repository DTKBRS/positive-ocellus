package com.positiveocellus;

import net.runelite.client.config.*;

@ConfigGroup("positiveocellus")
public interface PositiveOcellusConfig extends Config
{
    @ConfigItem(keyName="volume", name="Voice volume", description="Replacement volume multiplier; respects game sound volume. Preview uses this volume directly.", position=0)
    @Range(min=0,max=100)
    default int volume() { return 70; }

    @ConfigItem(keyName="capture", name="Capture sound IDs", description="Show sounds while Ocellus is nearby. Candidates need manual verification.", position=1)
    default boolean capture() { return false; }
}
