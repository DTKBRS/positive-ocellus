package com.positiveocellus;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextPackTest {
    static TextPack defaults() throws Exception {
        return TextPack.bundled(new Gson());
    }
    @Test public void normalizesCaseMarkupAndEllipses() throws Exception {
        assertEquals("You are worthy.",defaults().replacement("<col=ff0000>UNWORTHY…</col>"));
        assertEquals("You are getting stronger.",defaults().replacement("You... fail..."));
    }
    @Test public void matchesMultiWordTriggerAcrossPunctuation() throws Exception {
        assertEquals("This is your moment!",defaults().replacement("Your time... comes..."));
    }
    @Test public void doesNotMatchInsideAnotherWord() throws Exception {
        assertNull(defaults().replacement("Running laps"));
        assertNull(defaults().replacement("Weekend"));
    }
    @Test public void endCueMustBeTheWholeLine() throws Exception {
        assertEquals("Let's finish strong!",defaults().replacement("End..."));
        assertEquals("You can overcome this.",defaults().replacement("You can't... escape... the end..."));
        assertNull(defaults().replacement("A different end"));
    }
    @Test public void disabledCuePreservesOriginal() throws Exception {
        TextPack p=defaults();p.lines.get(1).enabled=false;assertNull(p.replacement("Unworthy..."));
    }
    @Test public void unknownAndNullTextRemainUntouched() throws Exception {
        assertNull(defaults().replacement("A brand new callout"));assertNull(defaults().replacement(null));
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
    @Test public void bundledPackPreservesInsolenceCue() throws Exception {
        TextPack p=defaults();assertEquals(19,p.lines.size());
        assertNull(p.replacement("Insolence..."));
        assertEquals(18,p.lines.stream().filter(l->l.enabled).count());
    }
}
