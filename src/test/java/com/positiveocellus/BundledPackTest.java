package com.positiveocellus;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BundledPackTest {
    @Test public void everyCatalogCueHasPlayablePcmAudio() throws Exception {
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream("/com/positiveocellus/voice-pack.json"), StandardCharsets.UTF_8)) {
            VoicePack pack = new Gson().fromJson(r, VoicePack.class);
            pack.validate(); assertEquals(19, pack.lines.size());
            for (VoiceLine line : pack.lines) {
                assertEquals("No unverified IDs may ship as active mappings", -1, line.soundId);
                assertNotNull(line.positive);
                try (InputStream raw = getClass().getResourceAsStream("/com/positiveocellus/audio/" + line.wav);
                     AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                    assertEquals(AudioFormat.Encoding.PCM_SIGNED, audio.getFormat().getEncoding());
                    assertEquals(16, audio.getFormat().getSampleSizeInBits());
                    assertTrue(audio.getFrameLength() > 0);
                    assertTrue(audio.getFrameLength() / audio.getFormat().getFrameRate() <= 20);
                }
            }
        }
    }
    @Test public void sidebarIconFitsHubDimensions() throws Exception {
        java.awt.image.BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/com/positiveocellus/icon.png"));
        assertTrue(icon.getWidth() <= 48); assertTrue(icon.getHeight() <= 72);
    }
}
