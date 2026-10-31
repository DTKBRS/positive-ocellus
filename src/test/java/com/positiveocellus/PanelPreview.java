package com.positiveocellus;

import com.google.gson.Gson;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Render the real Swing editor without starting a game client or requiring an account. */
public final class PanelPreview {
    public static void main(String[] args) throws Exception {
        VoicePack pack;
        try (Reader r = new InputStreamReader(PanelPreview.class.getResourceAsStream("/com/positiveocellus/voice-pack.json"), StandardCharsets.UTF_8)) {
            pack = new Gson().fromJson(r, VoicePack.class);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                VoicePanel panel = new VoicePanel(new PositiveOcellusPlugin());
                panel.loaded(pack, 19);
                panel.setSize(225, 1150);
                layout(panel);
                BufferedImage image = new BufferedImage(225, 1150, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics(); panel.printAll(graphics); graphics.dispose();
                File out = new File("build/reports/voice-editor.png"); out.getParentFile().mkdirs(); ImageIO.write(image, "png", out);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
    private static void layout(Container c) { c.doLayout(); for (Component child : c.getComponents()) if (child instanceof Container) layout((Container) child); }
}
