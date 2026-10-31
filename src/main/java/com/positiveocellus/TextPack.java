package com.positiveocellus;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;

public final class TextPack {
    public int schemaVersion = 1;
    public List<TextLine> lines = new ArrayList<>();

    static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
            .replaceAll("<[^>]*>", "").replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** Ordered matching: a disabled specific cue must not fall through to a generic one. */
    public String replacement(String overhead) {
        if (overhead == null) return null;
        String normalized = normalize(overhead);
        for (TextLine line : lines) {
            String trigger = normalize(line.trigger);
            boolean match = line.exactMatch ? normalized.equals(trigger)
                : (" " + normalized + " ").contains(" " + trigger + " ");
            if (match) return line.enabled ? line.positive : null;
        }
        return null;
    }

    public void validate() {
        if (schemaVersion != 1 || lines == null || lines.size() > 256)
            throw new IllegalArgumentException("Unsupported text pack or too many lines");
        Set<String> keys = new HashSet<>(), triggers = new HashSet<>();
        for (TextLine l : lines) {
            if (l == null || l.key == null || !l.key.matches("[a-z0-9_-]+") || !keys.add(l.key))
                throw new IllegalArgumentException("Each line needs a unique lowercase key");
            if (l.trigger == null || l.trigger.length() > 120 || normalize(l.trigger).isEmpty()
                || !triggers.add(normalize(l.trigger)))
                throw new IllegalArgumentException("Each line needs a unique nonempty trigger");
            if (l.positive == null || l.positive.trim().isEmpty() || l.positive.length() > 80
                || l.positive.chars().anyMatch(c -> c == '<' || c == '>' || Character.isISOControl(c)))
                throw new IllegalArgumentException("Replacement must be 1–80 characters, without markup or line breaks");
        }
    }

    public static TextPack read(Path file, Gson gson) throws IOException {
        if (Files.size(file) > 1024 * 1024) throw new IOException("Text pack exceeds 1 MB");
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            TextPack p = gson.fromJson(r, TextPack.class);
            if (p == null) throw new IOException("Empty text pack");
            p.validate(); return p;
        }
    }

    public void save(Path file, Gson gson) throws IOException {
        validate(); Files.createDirectories(file.getParent());
        Path tmp = Files.createTempFile(file.getParent(), "text-", ".json");
        try {
            Files.write(tmp, gson.toJson(this).getBytes(StandardCharsets.UTF_8));
            try { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(tmp); }
    }
}
