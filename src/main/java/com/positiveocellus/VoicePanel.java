package com.positiveocellus;

import java.awt.*;
import java.nio.file.*;
import javax.swing.*;
import net.runelite.client.ui.PluginPanel;

final class VoicePanel extends PluginPanel
{
    private final PositiveOcellusPlugin plugin;
    private final JComboBox<String> selector = new JComboBox<>();
    private final JTextField original = new JTextField();
    private final JTextField positive = new JTextField();
    private final JSpinner soundId = new JSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
    private final JComboBox<String> channel = new JComboBox<>(new String[]{"sound", "area"});
    private final JTextField wav = new JTextField();
    private final JCheckBox enabled = new JCheckBox("Enable this line");
    private final JTextArea status = new JTextArea();
    private final JTextArea capture = new JTextArea(12, 16);
    private VoicePack pack;
    private int selected = -1;
    private boolean loading;

    VoicePanel(PositiveOcellusPlugin plugin)
    {
        this.plugin = plugin;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("POSITIVE OCELLUS");
        heading.setForeground(new Color(219, 188, 108));
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        heading.setAlignmentX(LEFT_ALIGNMENT);
        add(heading);
        note("A little encouragement in the Archive.");
        field("Voice line", selector);
        field("Original cue / description", original);
        field("Positive script / recording note", positive);
        field("Sound ID (-1 = unverified)", soundId);
        field("Event channel", channel);
        field("WAV filename", wav);
        enabled.setAlignmentX(LEFT_ALIGNMENT);
        add(enabled);
        button("Preview saved audio", () -> { if (selected >= 0) plugin.preview(pack.lines.get(selected).key); });
        button("Save and reload", () -> { try { commit(); pack.validate(); plugin.save(pack); status("Saving..."); } catch (Exception e) { status(e.getMessage()); } });
        button("Reload from disk", plugin::reload);
        button("Import WAV for this line", this::importWav);
        button("Add voice line", this::addLine);
        note("Changing the script does not synthesize speech. Import a matching 16-bit PCM WAV, up to 20 seconds.");
        status.setEditable(false); status.setLineWrap(true); status.setWrapStyleWord(true); status.setRows(5); status.setAlignmentX(LEFT_ALIGNMENT); status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); add(status);
        note("Capture candidates (enable in settings)");
        capture.setEditable(false); capture.setLineWrap(true); capture.setWrapStyleWord(true);
        JScrollPane captureScroll = new JScrollPane(capture);
        captureScroll.setAlignmentX(LEFT_ALIGNMENT);
        captureScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        add(captureScroll);
        button("Clear capture", () -> capture.setText(""));
        note("Copy capture text to match IDs manually. Nearby sounds are candidates, not verified voice lines.");
        selector.addActionListener(e -> { if (!loading) { commit(); showLine(selector.getSelectedIndex()); } });
    }

    private void field(String label, JComponent input) { JLabel title = new JLabel(label); title.setForeground(Color.LIGHT_GRAY); title.setAlignmentX(LEFT_ALIGNMENT); add(title); input.setAlignmentX(LEFT_ALIGNMENT); input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28)); add(input); add(Box.createVerticalStrut(5)); }
    private void note(String message) { JTextArea t = new JTextArea(message); t.setEditable(false); t.setLineWrap(true); t.setWrapStyleWord(true); t.setOpaque(false); t.setForeground(Color.LIGHT_GRAY); t.setAlignmentX(LEFT_ALIGNMENT); t.setRows((message.length() + 28) / 29); t.setMaximumSize(new Dimension(Integer.MAX_VALUE, t.getRows() * 17)); add(t); add(Box.createVerticalStrut(8)); }
    private void button(String text, Runnable action) { JButton b = new JButton(text); b.setAlignmentX(LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28)); b.addActionListener(e -> action.run()); add(b); add(Box.createVerticalStrut(5)); }
    void status(String text) { status.setText(text); }
    void capture(String text) { if (capture.getText().length() > 50000) capture.setText(""); capture.append(text + "\n"); }

    void loaded(VoicePack value, int loaded)
    {
        loading = true; pack = value; selected = -1; selector.removeAllItems();
        for (VoiceLine line : value.lines) selector.addItem(line.key);
        loading = false; showLine(value.lines.isEmpty() ? -1 : 0);
        long verified = value.lines.stream().filter(l -> l.soundId >= 0 && l.enabled).count();
        status(loaded + " audio clips ready. " + verified + " mapped lines. Data: " + plugin.directory());
    }
    private void commit()
    {
        if (selected < 0) return;
        VoiceLine l = pack.lines.get(selected);
        l.original = original.getText(); l.positive = positive.getText();
        l.soundId = (Integer) soundId.getValue(); l.channel = (String) channel.getSelectedItem();
        l.wav = wav.getText().trim(); l.enabled = enabled.isSelected();
    }
    private void showLine(int index)
    {
        selected = index; if (index < 0) return;
        VoiceLine l = pack.lines.get(index);
        original.setText(l.original); positive.setText(l.positive); soundId.setValue(l.soundId);
        channel.setSelectedItem(l.channel); wav.setText(l.wav); enabled.setSelected(l.enabled);
    }
    private void addLine()
    {
        if (pack == null) return;
        commit();
        String key = JOptionPane.showInputDialog(this, "Unique key (lowercase letters, numbers, - or _)");
        if (key == null) return;
        if (!key.matches("[a-z0-9_-]+") || pack.lines.stream().anyMatch(l -> key.equals(l.key))) { status("Invalid or duplicate key"); return; }
        VoiceLine l = new VoiceLine(); l.key = key; l.original = "New cue"; l.positive = "You've got this!"; l.wav = key + ".wav";
        pack.lines.add(l); loading = true; selector.addItem(key); selector.setSelectedIndex(pack.lines.size()-1); loading = false; showLine(pack.lines.size()-1);
    }
    private void importWav()
    {
        if (selected < 0) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path source = chooser.getSelectedFile().toPath();
        VoiceLine importing = pack.lines.get(selected);
        String name = importing.key + ".wav";
        Path target = plugin.directory().resolve(name);
        if (Files.exists(target) && JOptionPane.showConfirmDialog(this, "Replace existing " + name + "?", "Replace audio", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                if (Files.size(source) > 4_000_000) throw new IllegalArgumentException("WAV exceeds 4 MB");
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); return null;
            }
            protected void done() { try { get(); importing.wav = name; if (selected >= 0 && pack.lines.get(selected) == importing) wav.setText(name); status("Imported. Save and reload to validate and preview."); } catch (Exception e) { status("Import failed: " + e.getMessage()); } }
        }.execute();
    }
}
