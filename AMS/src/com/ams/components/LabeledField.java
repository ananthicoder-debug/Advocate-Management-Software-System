package com.ams.components;

import com.ams.util.AMSTheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Rounded text field with label above
 */
public class LabeledField extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField field;
    private JLabel     label;

    public LabeledField(String labelText, int columns) {
        setLayout(new BorderLayout(0, 5));
        setOpaque(false);

        label = new JLabel(labelText);
        label.setFont(AMSTheme.FONT_BOLD);
        label.setForeground(AMSTheme.TEXT_SECONDARY);

        field = new JTextField(columns) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setBackground(AMSTheme.BG_INPUT);
        field.setFont(AMSTheme.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD0D9F0), 1, true),
            new EmptyBorder(6, 12, 6, 12)));
        field.setPreferredSize(new Dimension(0, AMSTheme.FIELD_H));
        field.setOpaque(false);

        add(label, BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
    }

    public JTextField getField()   { return field; }
    public String     getText()    { return field.getText().trim(); }
    public void       setText(String t) { field.setText(t); }
    public void       setEditable(boolean b) { field.setEditable(b); }
}
