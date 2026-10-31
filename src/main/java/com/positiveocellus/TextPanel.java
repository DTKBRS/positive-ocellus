package com.positiveocellus;

import java.awt.*;
import javax.swing.*;
import net.runelite.client.ui.PluginPanel;

final class TextPanel extends PluginPanel {
    private final PositiveOcellusPlugin plugin;
    private final JComboBox<String> selector = new JComboBox<>();
    private final JTextField cue = new JTextField();
    private final JTextField trigger = new JTextField();
    private final JTextArea positive = new JTextArea(3, 16);
    private final JCheckBox exact = new JCheckBox("Match the entire line");
    private final JCheckBox enabled = new JCheckBox("Enable this replacement");
    private final JTextArea status = new JTextArea(3,16);
    private TextPack pack;
    private int selected = -1;
    private boolean loading;

    TextPanel(PositiveOcellusPlugin plugin) {
        this.plugin=plugin;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel title=new JLabel("POSITIVE OCELLUS");
        title.setForeground(new Color(219,188,108)); title.setFont(title.getFont().deriveFont(Font.BOLD,16f));
        title.setAlignmentX(LEFT_ALIGNMENT); add(title);
        note("Encouraging text above his head.");
        field("Dialogue line",selector,28);
        field("Cue description",cue,28);
        field("Original words to match",trigger,28);
        exact.setAlignmentX(LEFT_ALIGNMENT); add(exact);
        positive.setLineWrap(true); positive.setWrapStyleWord(true);
        field("Positive overhead text",new JScrollPane(positive),68);
        enabled.setAlignmentX(LEFT_ALIGNMENT); add(enabled);
        button("Save changes",()->{try{commit();if(pack!=null){pack.validate();plugin.save(pack);status("Saving...");}}catch(Exception e){status(e.getMessage());}});
        button("Reload saved text",plugin::reload);
        button("Add dialogue line",this::addLine);
        note("Up to 80 characters per replacement. Case and punctuation are ignored when matching. Audio stays unchanged.");
        status.setEditable(false); status.setLineWrap(true); status.setWrapStyleWord(true);
        status.setAlignmentX(LEFT_ALIGNMENT);status.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));add(status);
        selector.addActionListener(e->{if(!loading){commit();showLine(selector.getSelectedIndex());}});
    }
    private void field(String label,JComponent input,int height){JLabel l=new JLabel(label);l.setForeground(Color.LIGHT_GRAY);l.setAlignmentX(LEFT_ALIGNMENT);add(l);input.setAlignmentX(LEFT_ALIGNMENT);input.setMaximumSize(new Dimension(Integer.MAX_VALUE,height));add(input);add(Box.createVerticalStrut(6));}
    private void note(String text){JTextArea t=new JTextArea(text);t.setEditable(false);t.setOpaque(false);t.setForeground(Color.LIGHT_GRAY);t.setLineWrap(true);t.setWrapStyleWord(true);t.setAlignmentX(LEFT_ALIGNMENT);t.setMaximumSize(new Dimension(Integer.MAX_VALUE,((text.length()+28)/29)*17));add(t);add(Box.createVerticalStrut(8));}
    private void button(String text,Runnable action){JButton b=new JButton(text);b.setAlignmentX(LEFT_ALIGNMENT);b.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));b.addActionListener(e->action.run());add(b);add(Box.createVerticalStrut(6));}
    void status(String text){status.setText(text);}
    void loaded(TextPack value){loading=true;pack=value;selected=-1;selector.removeAllItems();for(TextLine l:pack.lines)selector.addItem(l.key);loading=false;showLine(pack.lines.isEmpty()?-1:0);status(pack.lines.size()+" text replacements ready.");}
    private void commit(){if(selected<0)return;TextLine l=pack.lines.get(selected);l.cue=cue.getText();l.trigger=trigger.getText();l.exactMatch=exact.isSelected();l.positive=positive.getText();l.enabled=enabled.isSelected();}
    private void showLine(int i){selected=i;if(i<0)return;TextLine l=pack.lines.get(i);cue.setText(l.cue);trigger.setText(l.trigger);exact.setSelected(l.exactMatch);positive.setText(l.positive);enabled.setSelected(l.enabled);}
    private void addLine(){if(pack==null)return;commit();String key=JOptionPane.showInputDialog(this,"Unique key (lowercase letters, numbers, - or _)");if(key==null)return;if(!key.matches("[a-z0-9_-]+")||pack.lines.stream().anyMatch(l->key.equals(l.key))){status("Invalid or duplicate key");return;}TextLine l=new TextLine();l.key=key;l.cue="New cue";l.trigger=key;l.positive="You've got this!";pack.lines.add(l);loading=true;selector.addItem(key);selector.setSelectedIndex(pack.lines.size()-1);loading=false;showLine(pack.lines.size()-1);}
}
