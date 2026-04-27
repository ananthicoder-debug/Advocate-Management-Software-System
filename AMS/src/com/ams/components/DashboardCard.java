package com.ams.components;

import com.ams.util.AMSTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Gradient Dashboard Card with icon, title, count
 */
public class DashboardCard extends JPanel {

    private static final long serialVersionUID = 1L;

    private String title;
    private String value;
    private String icon;
    
    private Color  color1;
    private Color  color2;
    @SuppressWarnings("unused")
    private Runnable onClick;

    public DashboardCard(String title, String value, String icon, Color c1, Color c2, Runnable onClick) {
        this.title   = title;
        this.value   = value;
        this.icon    = icon;
        this.color1  = c1;
        this.color2  = c2;
        this.onClick = onClick;
        setOpaque(false);
        setPreferredSize(new Dimension(200, 130));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e)  { repaint(); }
        });
    }

    public void setValue(String val) { this.value = val; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRoundRect(4, 6, getWidth()-4, getHeight()-4, AMSTheme.CARD_RADIUS*2, AMSTheme.CARD_RADIUS*2);

        // Gradient fill
        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-6, AMSTheme.CARD_RADIUS*2, AMSTheme.CARD_RADIUS*2);

        // Semi-circle decoration
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillOval(getWidth()-80, -30, 130, 130);

        // Icon (large char)
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        g2.setColor(new Color(255, 255, 255, 180));
        g2.drawString(icon, 14, 48);

        // Value
        g2.setFont(AMSTheme.FONT_CARD_VALUE);
        g2.setColor(AMSTheme.TEXT_LIGHT);
        g2.drawString(value, 14, 88);

        // Title
        g2.setFont(AMSTheme.FONT_BODY);
        g2.setColor(new Color(255, 255, 255, 210));
        g2.drawString(title, 14, getHeight() - 14);

        g2.dispose();
    }
}
