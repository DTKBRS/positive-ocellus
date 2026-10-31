package com.positiveocellus;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;

/** All decoding happens off the client thread. Only prepared clips can consume game audio. */
final class VoicePlayer implements AutoCloseable
{
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "positive-ocellus-audio"); t.setDaemon(true); return t;
    });
    private final Map<String, Clip> clips = new HashMap<>();
    private final Set<String> reserved = new HashSet<>();
    private boolean closed;
    private long generation;

    int load(VoicePack pack, Path directory) throws IOException
    {
        Map<String, Clip> prepared = new HashMap<>();
        long remainingBytes = 32L * 1024 * 1024;
        for (VoiceLine l : pack.lines)
        {
            Clip clip = null;
            try
            {
                Path custom = directory.resolve(l.wav);
                if (Files.exists(custom) && Files.size(custom) > 4_000_000) continue;
                InputStream source = Files.exists(custom) ? Files.newInputStream(custom)
                    : VoicePlayer.class.getResourceAsStream("/com/positiveocellus/audio/" + l.wav);
                if (source == null) continue;
                try (InputStream raw = source; AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(raw)))
                {
                    AudioFormat f = in.getFormat();
                    if (!AudioFormat.Encoding.PCM_SIGNED.equals(f.getEncoding()) || f.getSampleSizeInBits() != 16
                        || f.getChannels() > 2 || f.getFrameRate() > 48000 || in.getFrameLength() < 0
                        || in.getFrameLength() > f.getFrameRate() * 20) continue;
                    long bytes = in.getFrameLength() * f.getFrameSize();
                    if (bytes > remainingBytes) continue;
                    remainingBytes -= bytes;
                    clip = AudioSystem.getClip(); clip.open(in); prepared.put(l.key, clip);
                }
            }
            catch (Exception e) { if (clip != null) clip.close(); /* Keep original audible. */ }
        }
        Map<String, Clip> previous;
        synchronized (this)
        {
            if (closed) { prepared.values().forEach(Clip::close); return 0; }
            generation++;
            previous = new HashMap<>(clips);
            clips.clear(); clips.putAll(prepared); reserved.clear();
        }
        previous.values().forEach(Clip::close);
        return prepared.size();
    }

    synchronized boolean play(String key, int volume, int delay)
    {
        Clip clip = clips.get(key);
        if (closed || volume <= 0 || clip == null || !clip.isOpen() || clip.isRunning() || reserved.contains(key)) return false;
        long expected = generation;
        reserved.add(key);
        executor.schedule(() -> {
            synchronized (VoicePlayer.this)
            {
                if (closed || generation != expected) return;
                try
                {
                    clip.setFramePosition(0);
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
                    {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float db = (float) (20 * Math.log10(Math.min(100, volume) / 100.0));
                        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
                    }
                    clip.start();
                }
                finally { reserved.remove(key); }
            }
        }, Math.max(0, Math.min(delay, 3000)) * 20L, TimeUnit.MILLISECONDS);
        return true;
    }

    synchronized void stop()
    {
        generation++;
        reserved.clear();
        clips.values().forEach(Clip::stop);
    }

    @Override public synchronized void close()
    {
        closed = true; stop(); executor.shutdownNow();
        clips.values().forEach(Clip::close); clips.clear();
    }
}
