package com.ams.dashboard;

import com.ams.util.AMSTheme;
import com.ams.login.LoginFrame;
import com.ams.util.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base Dashboard Frame — shared by all roles.
 * Provides: top bar, collapsible sidebar, content area.
 */
public abstract class BaseDashboard extends JFrame {

    private static final long serialVersionUID = 1L;

    protected JPanel    sidebarPanel;
    protected JPanel    contentPanel;
    protected JLabel    userNameLabel;
    protected JLabel    roleLabel;
    protected JPanel    topBar;
    protected String    currentUser;
    protected String    role;
    protected int       refId;
    protected Map<String, JComponent>  pages = new LinkedHashMap<>();
    protected Map<String, JButton>    navBtns = new LinkedHashMap<>();

    public BaseDashboard(String title, String currentUser, String role, int refId) {
        this.currentUser = currentUser;
        this.role        = role;
        this.refId       = refId;

        setTitle("AMS - " + title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        // Set to fullscreen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 680));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AMSTheme.BG_MAIN);

        topBar       = buildTopBar(title);
        sidebarPanel = buildSidebar();
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(AMSTheme.BG_MAIN);

        root.add(topBar,       BorderLayout.NORTH);
        root.add(sidebarPanel, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);

        // Build role-specific pages after frame is set
        SwingUtilities.invokeLater(() -> {
            buildPages();
            showFirstPage();
            updateNotificationBadge();
        });

        // DB status refresh timer
        new Timer(10000, e -> {
            if (dbStatusLabel != null) {
                dbStatusLabel.setText("DB: " + com.ams.util.DBConnection.getStatus());
            }
            updateNotificationBadge();
        }).start();
    }

    private JLabel dbStatusLabel;
    private JLabel notificationBell;

    // ── Top Bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar(String appTitle) {
        JPanel bar = new JPanel(new BorderLayout()) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0, AMSTheme.PRIMARY_DARK, getWidth(),0, AMSTheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                // Bottom accent line
                g2.setColor(AMSTheme.ACCENT);
                g2.fillRect(0, getHeight()-3, getWidth(), 3);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, AMSTheme.TOPBAR_H));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Left: logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("\u2696");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        logo.setForeground(AMSTheme.ACCENT_LIGHT);
        JLabel titleL = new JLabel("ADVOCATE MANAGEMENT SYSTEM");
        titleL.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleL.setForeground(AMSTheme.TEXT_LIGHT);
        left.add(logo);
        left.add(titleL);

        // Right: user info + bell + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // Bell notification
        notificationBell = new JLabel("\uD83D\uDD14");
        notificationBell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        notificationBell.setForeground(AMSTheme.ACCENT_LIGHT);
        notificationBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        notificationBell.setToolTipText("Notifications");
        notificationBell.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                showNotificationsDialog();
            }
        });

        // User avatar circle
        JLabel avatar = new JLabel(currentUser.substring(0, 1).toUpperCase()) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(AMSTheme.TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        // User name
        userNameLabel = new JLabel(currentUser);
        userNameLabel.setFont(AMSTheme.FONT_BOLD);
        userNameLabel.setForeground(AMSTheme.TEXT_LIGHT);

        roleLabel = new JLabel(getRoleDisplay());
        roleLabel.setFont(AMSTheme.FONT_SMALL);
        roleLabel.setForeground(new Color(255,255,255,160));

        JPanel userInfo = new JPanel(new GridLayout(2,1));
        userInfo.setOpaque(false);
        userInfo.add(userNameLabel);
        userInfo.add(roleLabel);

        // Logout
        JButton logoutBtn = new JButton("Logout \u2192");
        logoutBtn.setFont(AMSTheme.FONT_SMALL);
        logoutBtn.setForeground(AMSTheme.ACCENT_LIGHT);
        logoutBtn.setBackground(new Color(255,255,255,0));
        logoutBtn.setBorder(BorderFactory.createLineBorder(AMSTheme.ACCENT_LIGHT, 1, true));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setOpaque(false);
        logoutBtn.addActionListener(e -> logout());

        right.add(notificationBell);
        right.add(avatar);
        right.add(userInfo);
        right.add(Box.createHorizontalStrut(8));
        right.add(logoutBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AMSTheme.BG_SIDEBAR);
                g2.fillRect(0,0,getWidth(),getHeight());
                // Accent bar right edge
                g2.setColor(new Color(255,255,255,15));
                g2.fillRect(getWidth()-1,0,1,getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(AMSTheme.SIDEBAR_W, 0));
        sidebar.setOpaque(false);
        sidebar.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Section label — populated by subclass via addNavItem
        sidebar.putClientProperty("ready", true);
        return sidebar;
    }

    // ── Nav Item Builder ──────────────────────────────────────────────────────
    protected void addSidebarSection(String sectionName) {
        JLabel sec = new JLabel("  " + sectionName.toUpperCase());
        sec.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sec.setForeground(new Color(255,255,255,80));
        sec.setBorder(new EmptyBorder(14, 14, 4, 0));
        sec.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(sec);
    }

    protected void addNavItem(String label, String icon, String pageKey) {
        JButton btn = new JButton(icon + "  " + label) { private static final long serialVersionUID = 1L;
            boolean hover = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(AMSTheme.FONT_NAV);
                setForeground(new Color(255,255,255,200));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                setBorder(new EmptyBorder(8, 20, 8, 10));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = pageKey.equals(getActivePageKey());
                if (active) {
                    g2.setColor(AMSTheme.PRIMARY_LIGHT);
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 10, 10);
                    g2.setColor(AMSTheme.ACCENT);
                    g2.fillRoundRect(0, 8, 4, getHeight()-16, 4, 4);
                } else if (hover) {
                    g2.setColor(new Color(255,255,255,15));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.addActionListener(e -> showPage(pageKey));
        navBtns.put(pageKey, btn);
        sidebarPanel.add(btn);
    }

    protected String activePageKey = "";

    protected void showPage(String key) {
        activePageKey = key;
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, key);
        navBtns.values().forEach(b -> b.repaint());
    }

    protected String getActivePageKey() { return activePageKey; }

    protected void showFirstPage() {
        if (!pages.isEmpty()) {
            String first = pages.keySet().iterator().next();
            showPage(first);
        }
    }

    protected void addPage(String key, JComponent page) {
        pages.put(key, page);
        contentPanel.add(page, key);
    }

    protected String getRoleDisplay() {
        if ("SENIOR_ADVOCATE".equals(role)) return "Senior Advocate";
        if ("JUNIOR_ADVOCATE".equals(role)) return "Junior Advocate";
        if ("CLIENT".equals(role)) return "Client";
        return "Staff";
    }

    private void showNotificationsDialog() {
        java.util.List<String> items = new ArrayList<>();
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                String reminderSql = "SELECT case_id, TO_CHAR(due_date,'DD-MON-YYYY'), message, priority FROM REMINDER1 " +
                    "WHERE a_id=? AND rem_status='PENDING' AND due_date <= TRUNC(SYSDATE)+1 ORDER BY due_date";
                PreparedStatement rp = c.prepareStatement(reminderSql);
                rp.setInt(1, refId);
                ResultSet rr = rp.executeQuery();
                while (rr.next()) {
                    items.add("Reminder: Case #" + rr.getInt(1) + " | " + rr.getString(2) + " | " + rr.getString(4) + "\n" + rr.getString(3));
                }
                rr.close(); rp.close();

                String followupSql = "SELECT case_id, TO_CHAR(follow_up_date,'DD-MON-YYYY'), log_subject, follow_up_status FROM LOG_COMMUNICATION " +
                    "WHERE a_id=? AND follow_up_status='PENDING' AND follow_up_date <= TRUNC(SYSDATE)+1 ORDER BY follow_up_date";
                PreparedStatement fp = c.prepareStatement(followupSql);
                fp.setInt(1, refId);
                ResultSet fr = fp.executeQuery();
                while (fr.next()) {
                    items.add("Follow-up: Case #" + fr.getInt(1) + " | " + fr.getString(2) + " | " + fr.getString(4) + "\n" + fr.getString(3));
                }
                fr.close(); fp.close();
            }
        } catch (Exception ignored) {}

        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No notifications for today or tomorrow.",
                "Notifications", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextArea area = new JTextArea();
        area.setFont(AMSTheme.FONT_BODY);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append("\n\n");
        }
        area.setText(sb.toString().trim());
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 320));
        JOptionPane.showMessageDialog(this, scroll, "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateNotificationBadge() {
        int count = 0;
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) count = getPendingNotificationCount(c);
        } catch (Exception ignored) {}
        notificationBell.setText(count > 0 ? "\uD83D\uDD14 (" + count + ")" : "\uD83D\uDD14");
        notificationBell.setToolTipText(count > 0 ? "Notifications (" + count + ")" : "Notifications");
    }

    private int getPendingNotificationCount(Connection c) {
        int count = 0;
        try {
            PreparedStatement rp = c.prepareStatement(
                "SELECT COUNT(*) FROM REMINDER1 WHERE a_id=? AND rem_status='PENDING' AND due_date <= TRUNC(SYSDATE)+1");
            rp.setInt(1, refId);
            ResultSet rr = rp.executeQuery();
            if (rr.next()) count += rr.getInt(1);
            rr.close(); rp.close();

            PreparedStatement fp = c.prepareStatement(
                "SELECT COUNT(*) FROM LOG_COMMUNICATION WHERE a_id=? AND follow_up_status='PENDING' AND follow_up_date <= TRUNC(SYSDATE)+1");
            fp.setInt(1, refId);
            ResultSet fr = fp.executeQuery();
            if (fr.next()) count += fr.getInt(1);
            fr.close(); fp.close();
        } catch (Exception ignored) {}
        return count;
    }

    protected void logout() {
        int opt = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            setVisible(false);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    /** Subclasses implement to build their pages and nav items */
    protected abstract void buildPages();

    // ── Utility: scroll wrapper ───────────────────────────────────────────────
    protected JScrollPane scrollWrap(JPanel p) {
        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.setBackground(AMSTheme.BG_MAIN);
        sp.getViewport().setBackground(AMSTheme.BG_MAIN);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ── Utility: section heading label ────────────────────────────────────────
    protected JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AMSTheme.FONT_HEADING);
        l.setForeground(AMSTheme.TEXT_PRIMARY);
        l.setBorder(new EmptyBorder(0, 0, 10, 0));
        return l;
    }

    // ── Utility: card panel ───────────────────────────────────────────────────
    protected JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(0,0,0,12));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }
}
