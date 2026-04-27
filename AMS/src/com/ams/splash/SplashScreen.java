package com.ams.splash;

import com.ams.login.LoginFrame;
import com.ams.util.AMSTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Welcome / Splash Screen — Advocate Management System
 * Bright, animated, click-to-proceed design.
 */
public class SplashScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private int alpha = 0;           // fade-in state
    private float angle = 0f;        // rotating ring angle
    private Timer fadeTimer;
    private Timer spinTimer;
    private boolean ready = false;

    public SplashScreen() {
        setUndecorated(true);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        SplashPanel panel = new SplashPanel();
        setContentPane(panel);

        // Click-to-continue
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (ready) launchLogin();
            }
            @Override public void mouseEntered(MouseEvent e) { if (ready) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override public void mouseExited(MouseEvent e)  { setCursor(Cursor.getDefaultCursor()); }
        });

        // Fade-in timer
        fadeTimer = new Timer(18, ev -> {
            alpha = Math.min(alpha + 5, 255);
            repaint();
            if (alpha >= 255) {
                ((Timer) ev.getSource()).stop();
                ready = true;
            }
        });

        // Spinning ring timer
        spinTimer = new Timer(25, ev -> {
            angle += 1.5f;
            if (angle >= 360) angle = 0;
            repaint();
        });

        fadeTimer.start();
        spinTimer.start();
    }

    private void launchLogin() {
        spinTimer.stop();
        setVisible(false);
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // ─── Inner panel ─────────────────────────────────────────────────────────
    class SplashPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);

            int W = getWidth(), H = getHeight();

            // ── Background gradient ──────────────────────────────────────────
            GradientPaint bg = new GradientPaint(0, 0, new Color(0x0A2540),
                                                  W, H, new Color(0x1A4B8C));
            g2.setPaint(bg);
            g2.fillRoundRect(0, 0, W, H, 30, 30);

            // ── Decorative circles ───────────────────────────────────────────
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fillOval(-80, -80, 320, 320);
            g2.fillOval(W - 200, H - 200, 350, 350);
            g2.setColor(new Color(240, 165, 0, 18));
            g2.fillOval(W/2 - 260, H/2 - 260, 520, 520);

            // Apply fade alpha
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f);
            g2.setComposite(ac);

            // ── Gavel / scales icon area ──────────────────────────────────────
            int cx = W / 2, cy = H / 2 - 60;

            // Spinning ring
            g2.setColor(new Color(240, 165, 0, 120));
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx - 70, cy - 70, 140, 140, (int) angle, 270);
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawArc(cx - 70, cy - 70, 140, 140, (int) (angle + 90), 270);

            // Circle background
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(cx - 58, cy - 58, 116, 116);

            // Scales of justice emoji / text
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
            FontMetrics fm = g2.getFontMetrics();
            String icon = "\u2696";   // ⚖ Scales of justice
            g2.setColor(AMSTheme.ACCENT_LIGHT);
            g2.drawString(icon, cx - fm.stringWidth(icon)/2, cy + 20);

            // ── Main title ────────────────────────────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.BOLD, 38));
            fm = g2.getFontMetrics();
            String titleLine1 = "ADVOCATE";
            g2.setColor(AMSTheme.TEXT_LIGHT);
            g2.drawString(titleLine1, cx - fm.stringWidth(titleLine1)/2, cy + 110);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 38));
            fm = g2.getFontMetrics();
            String titleLine2 = "MANAGEMENT SYSTEM";
            // Gradient text
            GradientPaint tp = new GradientPaint(cx - fm.stringWidth(titleLine2)/2, 0,
                                                  AMSTheme.ACCENT_LIGHT,
                                                  cx + fm.stringWidth(titleLine2)/2, 0,
                                                  AMSTheme.ACCENT);
            g2.setPaint(tp);
            g2.drawString(titleLine2, cx - fm.stringWidth(titleLine2)/2, cy + 155);

            // ── Tagline ────────────────────────────────────────────────────────
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            fm = g2.getFontMetrics();
            String tag = "Digitizing Justice — One Case at a Time";
            g2.drawString(tag, cx - fm.stringWidth(tag)/2, cy + 185);

            // ── Separator line ─────────────────────────────────────────────────
            g2.setColor(AMSTheme.ACCENT);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(cx - 180, cy + 200, cx + 180, cy + 200);

            // ── Bottom click hint ──────────────────────────────────────────────
            if (ready) {
                float pulse = (float)(0.65 + 0.35 * Math.abs(Math.sin(System.currentTimeMillis() / 600.0)));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                g2.setColor(AMSTheme.ACCENT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                fm = g2.getFontMetrics();
                String hint = "▶   Click anywhere to continue   ◀";
                g2.drawString(hint, cx - fm.stringWidth(hint)/2, H - 38);
            } else {
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                String loading = "Loading…";
                g2.drawString(loading, cx - fm.stringWidth(loading)/2, H - 38);
            }

            // ── Footer ─────────────────────────────────────────────────────────
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f * 0.6f));
            g2.setColor(AMSTheme.TEXT_LIGHT);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String footer = "© 2024 Advocate Management System  •  Version 1.0";
            fm = g2.getFontMetrics();
            g2.drawString(footer, cx - fm.stringWidth(footer)/2, H - 16);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SplashScreen().setVisible(true));
    }
}
