package com.positiveocellus;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextPackTest {
    static TextPack defaults() throws Exception {
        try(Reader r=new InputStreamReader(TextPackTest.class.getResourceAsStream("/com/positiveocellus/text-pack.json"),StandardCharsets.UTF_8)) {
            TextPack p=new Gson().fromJson(r,TextPack.class);p.validate();return p;
        }
    }
    @Test public void normalizesCaseMarkupAndEllipses() throws Exception {
        assertEquals("You are worthy.",defaults().replacement("<col=ff0000>UNWORTHY…</col>"));
        assertEquals("You are getting stronger.",defaults().replacement("You... fail..."));
    }
    @Test public void matchesMultiWordTriggerAcrossPunctuation() throws Exception {
        assertEquals("Your moment is here.",defaults().replacement("Your time... comes..."));
    }
    @Test public void doesNotMatchInsideAnotherWord() throws Exception {
        assertNull(defaults().replacement("Running laps"));
        assertNull(defaults().replacement("Weekend"));
    }
    @Test public void endCueMustBeTheWholeLine() throws Exception {
        assertEquals("Finish strong.",defaults().replacement("End..."));
        assertEquals("You can overcome this.",defaults().replacement("You can't... escape... the end..."));
        assertNull(defaults().replacement("A different end"));
    }
    @Test public void disabledCuePreservesOriginal() throws Exception {
        TextPack p=defaults();p.lines.get(1).enabled=false;assertNull(p.replacement("Unworthy..."));
    }
    @Test public void unknownAndNullTextRemainUntouched() throws Exception {
        assertNull(defaults().replacement("A brand new callout"));assertNull(defaults().replacement(null));
    }
    @Test public void roundTripPreservesCustomText() throws Exception {
        Path d=Files.createTempDirectory("ocellus-text-test"), f=d.resolve("text-pack.json");
        try {TextPack p=defaults();p.lines.get(1).positive="Nice work!";p.save(f,new Gson());assertEquals("Nice work!",TextPack.read(f,new Gson()).replacement("Unworthy..."));}
        finally {Files.deleteIfExists(f);Files.delete(d);}
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsEmptyTriggers() throws Exception {
        TextPack p=defaults();p.lines.get(0).trigger="...";p.validate();
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsMarkupInReplacement() throws Exception {
        TextPack p=defaults();p.lines.get(0).positive="<img=1>";p.validate();
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsDuplicateTriggers() throws Exception {
        TextPack p=defaults();p.lines.get(1).trigger=p.lines.get(0).trigger;p.validate();
    }
    @Test public void bundledPackHasNineteenEnabledLines() throws Exception {
        TextPack p=defaults();assertEquals(19,p.lines.size());assertTrue(p.lines.stream().allMatch(l->l.enabled));
    }
}
