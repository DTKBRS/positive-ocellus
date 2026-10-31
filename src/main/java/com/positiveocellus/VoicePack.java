package com.positiveocellus;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class VoicePack
{
    public int schemaVersion = 1;
    public List<VoiceLine> lines = new ArrayList<>();

    public void validate()
    {
        if (schemaVersion != 1 || lines == null || lines.size() > 256)
            throw new IllegalArgumentException("Unsupported pack version or too many lines");
        Set<String> keys = new HashSet<>(), ids = new HashSet<>();
        for (VoiceLine l : lines)
        {
            if (l == null || l.key == null || !l.key.matches("[a-z0-9_-]+") || !keys.add(l.key))
                throw new IllegalArgumentException("Every line needs a unique lowercase key");
            if (l.soundId < -1 || !("sound".equals(l.channel) || "area".equals(l.channel)))
                throw new IllegalArgumentException("Invalid sound ID or channel for " + l.key);
            if (l.enabled && l.soundId >= 0 && !ids.add(l.channel + ":" + l.soundId))
                throw new IllegalArgumentException("Duplicate active sound mapping: " + l.soundId);
            if (l.wav == null || !l.wav.matches("[a-zA-Z0-9_-]+\\.wav"))
                throw new IllegalArgumentException("WAV must be a plain filename: " + l.key);
        }
    }

    public static VoicePack read(Path file, Gson gson) throws IOException
    {
        if (Files.size(file) > 1024 * 1024) throw new IOException("Pack exceeds 1 MB");
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            VoicePack p = gson.fromJson(r, VoicePack.class);
            if (p == null) throw new IOException("Empty pack");
            p.validate();
            return p;
        }
    }

    public void save(Path file, Gson gson) throws IOException
    {
        validate();
        Files.createDirectories(file.getParent());
        Path tmp = Files.createTempFile(file.getParent(), "pack-", ".json");
        try
        {
            Files.write(tmp, gson.toJson(this).getBytes(StandardCharsets.UTF_8));
            try { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
        }
        finally { Files.deleteIfExists(tmp); }
    }
}
