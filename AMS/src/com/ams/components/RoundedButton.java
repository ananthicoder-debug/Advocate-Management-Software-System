package com.ams.components;

import com.ams.util.AMSTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Reusable rounded button with hover/press animations
 */
public class RoundedButton extends JButton {

    private static final long serialVersionUID = 1L;

    private Color bgColor;
    private Color hoverColor;
    private Color pressColor;
    private Color currentBg;
    private int radius;

    public RoundedButton(String text, Color bg, Color hover) {
        super(text);
        this.bgColor    = bg;
        this.hoverColor = hover;
        this.pressColor = bg.darker();
        this.currentBg  = bg;
        this.radius     = AMSTheme.BTN_RADIUS;
        setup();
    }

    public RoundedButton(String text) {
        this(text, AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
    }

    private void setup() {
        setForeground(AMSTheme.TEXT_LIGHT);
        setFont(AMSTheme.FONT_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(160, AMSTheme.FIELD_H));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { currentBg = hoverColor; repaint(); }
            @Override public void mouseExited(MouseEvent e)   { currentBg = bgColor;    repaint(); }
            @Override public void mousePressed(MouseEvent e)  { currentBg = pressColor; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { currentBg = hoverColor; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(currentBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius*2, radius*2);
        super.paintComponent(g);
        g2.dispose();
    }
}
