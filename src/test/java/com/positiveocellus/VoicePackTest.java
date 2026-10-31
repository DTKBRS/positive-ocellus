package com.positiveocellus;

import com.google.gson.Gson;
import java.nio.file.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class VoicePackTest
{
    private VoicePack pack() {
        VoicePack p = new VoicePack();
        VoiceLine l = new VoiceLine(); l.key="encourage"; l.wav="encourage.wav"; l.soundId=12;
        p.lines.add(l); return p;
    }
    @Test public void roundTripPreservesMapping() throws Exception {
        Path file=Files.createTempDirectory("ocellus-test").resolve("pack.json");
        VoicePack p=pack(); p.save(file,new Gson());
        VoicePack result=VoicePack.read(file,new Gson());
        assertEquals(12,result.lines.get(0).soundId);
        assertEquals("encourage.wav",result.lines.get(0).wav);
        Files.delete(file); Files.delete(file.getParent());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsPathTraversal() {
        VoicePack p=pack(); p.lines.get(0).wav="../outside.wav"; p.validate();
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsDuplicateActiveId() {
        VoicePack p=pack(); VoiceLine l=new VoiceLine(); l.key="other"; l.wav="other.wav"; l.soundId=12; p.lines.add(l); p.validate();
    }
    @Test public void allowsSameIdOnDifferentChannelsAndUnmappedLines() {
        VoicePack p=pack(); VoiceLine l=new VoiceLine(); l.key="area"; l.wav="area.wav"; l.soundId=12; l.channel="area"; p.lines.add(l); p.validate();
        l.soundId=-1; p.lines.get(0).soundId=-1; p.validate();
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownVersion() { VoicePack p=pack(); p.schemaVersion=2; p.validate(); }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidChannel() { VoicePack p=pack(); p.lines.get(0).channel="music"; p.validate(); }
    @Test public void missingAudioNeverConsumesOriginal() {
        try (VoicePlayer player=new VoicePlayer()) { assertFalse(player.play("absent",70,0)); }
    }
    @Test public void stoppedPlayerCannotScheduleAudio() {
        VoicePlayer player=new VoicePlayer(); player.close(); assertFalse(player.play("absent",70,0));
    }
}
