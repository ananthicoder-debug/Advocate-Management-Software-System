package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

/**
 * Case Journey Dialog — timeline pathway, hearings, evidence, notes, client contact
 */
public class CaseJourneyDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private int caseId;
    private int advId;
    private String caseTitle;

    // Journey timeline data
    @SuppressWarnings("unused")
    private java.util.List<int[]> yearBounds = new ArrayList<>();   // {year, startX}
    private java.util.List<String> yearList  = new ArrayList<>();
    private int selectedYear = -1;
    private JPanel journeyPathwayPanel;
    private JPanel journeyEventsPanel;

    public CaseJourneyDialog(Component parent, int caseId, String caseTitle, int advId) {
        super(SwingUtilities.getWindowAncestor(parent) instanceof JFrame ?
              (JFrame) SwingUtilities.getWindowAncestor(parent) : new JFrame(),
              "Case Journey — " + caseTitle, true);
        this.caseId    = caseId;
        this.caseTitle = caseTitle;
        this.advId     = advId;
        setSize(1100, 720);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AMSTheme.BG_MAIN);

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout()) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,0,AMSTheme.PRIMARY_DARK,getWidth(),0,AMSTheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(AMSTheme.ACCENT);
                g2.fillRect(0,getHeight()-3,getWidth(),3);
                g2.dispose();
            }
        };
        top.setPreferredSize(new Dimension(0,64));
        top.setBorder(new EmptyBorder(0,24,0,24));
        JLabel caseLabel = new JLabel("\uD83D\uDCBC  " + caseTitle + "  (Case #" + caseId + ")");
        caseLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        caseLabel.setForeground(Color.WHITE);
        top.add(caseLabel, BorderLayout.WEST);

        // Menu bar with tabs
        JMenuBar mb = buildMenuBar();
        top.add(mb, BorderLayout.EAST);

        // ── Main content — tabbed ─────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(AMSTheme.FONT_BOLD);
        tabs.setBackground(AMSTheme.BG_MAIN);

        tabs.addTab("\uD83D\uDDD3  Journey",    buildJourneyTab());
        tabs.addTab("\uD83D\uDD56  Hearings",   buildHearingsTab());
        tabs.addTab("\uD83D\uDD0D  Evidence",   buildEvidenceTab());
        tabs.addTab("\uD83D\uDCDD  Notes",      buildNotesTab());
        tabs.addTab("\uD83D\uDCAC  Client Chat",buildClientTab());

        root.add(top,  BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Journey Tab ───────────────────────────────────────────────────────────
    private JPanel buildJourneyTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AMSTheme.BG_MAIN);

        // Pathway panel
        JPanel pathway = new JPanel() { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                paintJourney(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 180); }
        };
        pathway.setBackground(new Color(0x0A2540));
        pathway.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.journeyPathwayPanel = pathway;
        // Year events panel
        JPanel eventsPanel = new JPanel();
        this.journeyEventsPanel = eventsPanel;
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
        eventsPanel.setBackground(AMSTheme.BG_MAIN);
        eventsPanel.setBorder(new EmptyBorder(20,28,20,28));

        pathway.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                // Find clicked year node
                for (int i = 0; i < yearList.size(); i++) {
                    int nx = getNodeX(i, pathway.getWidth());
                    if (Math.abs(x - nx) < 30) {
                        selectedYear = Integer.parseInt(yearList.get(i));
                        pathway.repaint();
                        loadYearEvents(eventsPanel, selectedYear);
                        break;
                    }
                }
            }
        });

        JScrollPane evScroll = new JScrollPane(eventsPanel);
        evScroll.setBorder(null);
        evScroll.setBackground(AMSTheme.BG_MAIN);
        evScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnRow.setBackground(AMSTheme.BG_MAIN);
        RoundedButton addEventBtn  = new RoundedButton("+ Add Event",  AMSTheme.SUCCESS,  AMSTheme.SUCCESS.brighter());
        RoundedButton viewBriefBtn = new RoundedButton("View Briefly", AMSTheme.INFO,     AMSTheme.INFO.darker());
        btnRow.add(viewBriefBtn); btnRow.add(addEventBtn);

        addEventBtn.addActionListener(e -> showAddEventDialog());
        viewBriefBtn.addActionListener(e -> showBriefView());

        page.add(pathway,    BorderLayout.NORTH);
        page.add(evScroll,   BorderLayout.CENTER);
        page.add(btnRow,     BorderLayout.SOUTH);

        // Load years
        loadYears(pathway, eventsPanel);
        return page;
    }

    private void paintJourney(Graphics2D g2, int W, int H) {
        if (yearList.isEmpty()) {
            g2.setColor(new Color(255,255,255,100));
            g2.setFont(AMSTheme.FONT_BODY);
            g2.drawString("No timeline data available. Events will appear here after adding case events.", 30, H/2);
            return;
        }
        int n = yearList.size();
        // Draw connecting line
        int y0 = H/2;
        g2.setColor(new Color(255,255,255,60));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8,6}, 0));
        g2.drawLine(getNodeX(0,W)-20, y0, getNodeX(n-1,W)+20, y0);

        for (int i = 0; i < n; i++) {
            int nx = getNodeX(i, W);
            boolean sel = yearList.get(i).equals(String.valueOf(selectedYear));
            // Connector dot
            if (i < n-1) {
                g2.setStroke(new BasicStroke(2));
                g2.setColor(new Color(255,255,255,40));
                g2.drawLine(nx+20, y0, getNodeX(i+1,W)-20, y0);
            }
            // Node circle
            g2.setColor(sel ? AMSTheme.ACCENT : new Color(45,108,207));
            g2.fillOval(nx-18, y0-18, 36, 36);
            if (sel) {
                g2.setColor(AMSTheme.ACCENT_LIGHT);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(nx-22, y0-22, 44, 44);
            }
            // Year text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(sel ? AMSTheme.ACCENT_LIGHT : Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String yr = yearList.get(i);
            g2.drawString(yr, nx - fm.stringWidth(yr)/2, y0 + 40);
        }
    }

    private int getNodeX(int index, int W) {
        int n = yearList.size();
        if (n <= 1) return W/2;
        int margin = 80;
        return margin + index * (W - 2*margin) / (n-1);
    }

    private void loadYears(JPanel pathway, JPanel eventsPanel) {
        yearList.clear();
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try (Connection c = DBConnection.getConnection()) {
                    if (c != null) {
                        String sql = "SELECT DISTINCT EXTRACT(YEAR FROM event_date) yr FROM CASE_EVENT " +
                                     "WHERE case_id=? ORDER BY yr";
                        try (PreparedStatement ps = c.prepareStatement(sql)) {
                            ps.setInt(1, caseId);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    yearList.add(String.valueOf(rs.getInt(1)));
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                if (yearList.isEmpty()) {
                    yearList.add("2022"); yearList.add("2023"); yearList.add("2024"); yearList.add("2025");
                }
                return null;
            }
            @Override protected void done() {
                pathway.repaint();
                if (!yearList.isEmpty()) {
                    selectedYear = Integer.parseInt(yearList.get(yearList.size()-1));
                    loadYearEvents(eventsPanel, selectedYear);
                }
            }
        };
        w.execute();
    }

    private void loadYearEvents(JPanel eventsPanel, int year) {
        eventsPanel.removeAll();
        JLabel yLbl = new JLabel("Events in " + year);
        yLbl.setFont(AMSTheme.FONT_HEADING);
        yLbl.setForeground(AMSTheme.PRIMARY);
        eventsPanel.add(yLbl);
        eventsPanel.add(Box.createVerticalStrut(12));

        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try (Connection c = DBConnection.getConnection()) {
                    if (c != null) {
                        String sql = "SELECT ev_id,title,TO_CHAR(event_date,'DD-MON-YYYY'),description,outcome,is_important " +
                                     "FROM CASE_EVENT WHERE case_id=? AND EXTRACT(YEAR FROM event_date)=? ORDER BY event_date";
                        try (PreparedStatement ps = c.prepareStatement(sql)) {
                            ps.setInt(1, caseId);
                            ps.setInt(2, year);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    final int id = rs.getInt(1);
                                    final String ttl = rs.getString(2);
                                    final String dt  = rs.getString(3);
                                    final String dsc = rs.getString(4);
                                    final String out = rs.getString(5);
                                    final boolean imp = rs.getInt(6) == 1;
                                    SwingUtilities.invokeLater(() -> eventsPanel.add(buildEventCard(id, ttl, dt, dsc, out, imp)));
                                }
                            }
                        }
                    } else {
                        // Demo
                        SwingUtilities.invokeLater(() -> {
                            eventsPanel.add(buildEventCard(1,"Initial Filing","10-JAN-2024","Case was filed at district court.","Filed successfully.",true));
                            eventsPanel.add(buildEventCard(2,"First Hearing","15-FEB-2024","Arguments presented.","Adjourned for evidence.",false));
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        eventsPanel.add(buildEventCard(1,"Initial Filing","10-JAN-" + year,"Case filed.","Filed.",true));
                    });
                }
                return null;
            }
            @Override protected void done() {
                eventsPanel.revalidate();
                eventsPanel.repaint();
            }
        };
        w.execute();
    }

    private JPanel buildEventCard(int id, String title, String date, String desc, String outcome, boolean important) {
        JPanel card = new JPanel(new BorderLayout(12,0)) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(important ? new Color(0xFFF8E7) : AMSTheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(important ? AMSTheme.ACCENT : new Color(0xE0E8F8));
                g2.setStroke(new BasicStroke(important?2:1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                // Left bar
                g2.setColor(important ? AMSTheme.ACCENT : AMSTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0,0,5,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12,16,12,16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new GridLayout(3,1,0,3));
        left.setOpaque(false);
        JLabel ttl = new JLabel((important?"* ":"") + title);
        ttl.setFont(AMSTheme.FONT_BOLD);
        ttl.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel dtL = new JLabel(date);
        dtL.setFont(AMSTheme.FONT_SMALL);
        dtL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel dscL = new JLabel(desc != null ? desc : "");
        dscL.setFont(AMSTheme.FONT_SMALL);
        dscL.setForeground(AMSTheme.TEXT_SECONDARY);
        left.add(ttl); left.add(dtL); left.add(dscL);

        JLabel outL = new JLabel(outcome != null ? "→ "+outcome : "");
        outL.setFont(AMSTheme.FONT_SMALL);
        outL.setForeground(AMSTheme.SUCCESS);

        card.add(left,  BorderLayout.CENTER);
        card.add(outL,  BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0,0,8,0));
        wrapper.add(card);
        return wrapper;
    }

    // ── Hearings Tab ──────────────────────────────────────────────────────────
    private JPanel buildHearingsTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(20,24,20,24));

        JLabel title = new JLabel("\uD83D\uDD56  Hearings — Case #" + caseId);
        title.setFont(AMSTheme.FONT_HEADING); title.setForeground(AMSTheme.PRIMARY);

        String[] cols = {"H-ID","Date","Time","Court","Room","Purpose","Judge","Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = buildTable(m);
        loadHearings(m);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        btnRow.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("+ Add Hearing", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton refreshBtn = new RoundedButton("⟳",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(60,36));
        addBtn.addActionListener(e -> showAddHearingDialog(m));
        refreshBtn.addActionListener(e -> { m.setRowCount(0); loadHearings(m); });
        btnRow.add(refreshBtn); btnRow.add(addBtn);

        page.add(title, BorderLayout.NORTH);
        page.add(sp,    BorderLayout.CENTER);
        page.add(btnRow,BorderLayout.SOUTH);
        return page;
    }

    private void loadHearings(DefaultTableModel m) {
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT h_id,TO_CHAR(h_date,'DD-MON-YYYY'),h_time,court_house,room_no,purpose,judge_name,status " +
                            "FROM HEARING1 WHERE case_id="+caseId+" ORDER BY h_date").executeQuery();
                        while (rs.next()) {
                            final Object[] row = {rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                                rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8)};
                            SwingUtilities.invokeLater(() -> m.addRow(row));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            m.addRow(new Object[]{2001,"28-DEC-2024","10:00 AM","High Court","Hall 3","Arguments","J. Sharma","UPCOMING"});
                            m.addRow(new Object[]{2002,"10-JAN-2025","02:00 PM","District Ct","Rm 5","Evidence","J. Patel","UPCOMING"});
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> m.addRow(new Object[]{2001,"28-DEC-2024","10:00","High Court","3","Arguments","J. Sharma","UPCOMING"}));
                }
                return null;
            }
        };
        w.execute();
    }

    private void showAddHearingDialog(DefaultTableModel m) {
        JDialog d = new JDialog(this, "Add Hearing", true);
        d.setSize(460, 420);
        d.setLocationRelativeTo(this);

        JPanel main = new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24));
        main.setBackground(AMSTheme.BG_MAIN);

        JTextField dateF  = tf(); JTextField timeF  = tf();
        JTextField courtF = tf(); JTextField roomF  = tf();
        JTextField purposeF=tf();JTextField judgeF = tf();

        main.add(lbl("Date (YYYY-MM-DD)")); main.add(dateF);
        main.add(lbl("Time"));              main.add(timeF);
        main.add(lbl("Court House"));       main.add(courtF);
        main.add(lbl("Room No"));           main.add(roomF);
        main.add(lbl("Purpose"));           main.add(purposeF);
        main.add(lbl("Judge Name"));        main.add(judgeF);

        RoundedButton saveBtn = new RoundedButton("Save",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO HEARING1(h_id,case_id,h_date,h_time,court_house,room_no,purpose,judge_name,status) " +
                        "VALUES(hearing_seq.NEXTVAL,?,TO_DATE(?,'YYYY-MM-DD'),?,?,?,?,?,'UPCOMING')");
                    ps.setInt(1,caseId); ps.setString(2,dateF.getText()); ps.setString(3,timeF.getText());
                    ps.setString(4,courtF.getText()); ps.setString(5,roomF.getText());
                    ps.setString(6,purposeF.getText()); ps.setString(7,judgeF.getText());
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d,"Hearing added!");
                d.dispose();
                m.setRowCount(0); loadHearings(m);
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN);
        btnRow.add(saveBtn);
        d.add(main, BorderLayout.CENTER);
        d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── Evidence Tab ──────────────────────────────────────────────────────────
    private JPanel buildEvidenceTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(20,24,20,24));

        JLabel title = new JLabel("\uD83D\uDD0D  Evidence — Case #" + caseId);
        title.setFont(AMSTheme.FONT_HEADING); title.setForeground(AMSTheme.PRIMARY);

        String[] cols = {"E-ID","Type","Source","Description","Collected","Admissibility","Verified"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = buildTable(m);
        loadEvidence(m);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        btnRow.setOpaque(false);
        RoundedButton addBtn  = new RoundedButton("+ Add Evidence", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton grantBtn = new RoundedButton("Grant Access to Junior", AMSTheme.INFO, AMSTheme.INFO.darker());
        addBtn.addActionListener(e -> showAddEvidenceDialog(m));
        grantBtn.addActionListener(e -> JOptionPane.showMessageDialog(page,"Select evidence and junior to grant access."));
        btnRow.add(grantBtn); btnRow.add(addBtn);

        page.add(title, BorderLayout.NORTH);
        page.add(sp, BorderLayout.CENTER);
        page.add(btnRow, BorderLayout.SOUTH);
        return page;
    }

    private void loadEvidence(DefaultTableModel m) {
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT e_id,e_type,e_source,SUBSTR(e_desc,1,60),TO_CHAR(collected_date,'DD-MON-YYYY'),admissibility,verified_by " +
                            "FROM EVIDENCE1 WHERE case_id="+caseId+" ORDER BY collected_date DESC").executeQuery();
                        while (rs.next()) {
                            final Object[] row = {rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                                rs.getString(5),rs.getString(6),rs.getInt(7)};
                            SwingUtilities.invokeLater(() -> m.addRow(row));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            m.addRow(new Object[]{3001,"DOCUMENT","Police Report","FIR copy attached...","10-JAN-2024","ADMITTED",advId});
                            m.addRow(new Object[]{3002,"PHOTO","Witness","Crime scene photo","15-JAN-2024","PENDING",0});
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> m.addRow(new Object[]{3001,"DOCUMENT","Source","Description","Date","PENDING",0}));
                }
                return null;
            }
        };
        w.execute();
    }

    private void showAddEvidenceDialog(DefaultTableModel m) {
        JDialog d = new JDialog(this, "Add Evidence", true);
        d.setSize(460, 380);
        d.setLocationRelativeTo(this);
        JPanel main = new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24));
        main.setBackground(AMSTheme.BG_MAIN);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"DOCUMENT","PHOTO","VIDEO","AUDIO","WITNESS","OTHER"});
        JTextField sourceF = tf();
        JTextField dateF   = tf();
        JTextArea  descA   = new JTextArea(3,20); descA.setFont(AMSTheme.FONT_BODY);
        JComboBox<String> admBox = new JComboBox<>(new String[]{"PENDING","ADMITTED","REJECTED"});
        main.add(lbl("Evidence Type"));  main.add(typeBox);
        main.add(lbl("Source"));         main.add(sourceF);
        main.add(lbl("Collected Date")); main.add(dateF);
        main.add(lbl("Admissibility"));  main.add(admBox);
        RoundedButton saveBtn = new RoundedButton("Save",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO EVIDENCE1(e_id,case_id,e_type,e_source,collected_date,admissibility,verified_by,e_desc) " +
                        "VALUES(evidence_seq.NEXTVAL,?,?,?,TO_DATE(?,'YYYY-MM-DD'),?,?,?)");
                    ps.setInt(1,caseId); ps.setString(2,(String)typeBox.getSelectedItem());
                    ps.setString(3,sourceF.getText()); ps.setString(4,dateF.getText());
                    ps.setString(5,(String)admBox.getSelectedItem()); ps.setInt(6,advId);
                    ps.setString(7,descA.getText());
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d,"Evidence added!"); d.dispose();
                m.setRowCount(0); loadEvidence(m);
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });
        JPanel desc = new JPanel(new BorderLayout(0,5)); desc.setBackground(AMSTheme.BG_MAIN);
        desc.add(lbl("Description"), BorderLayout.NORTH);
        desc.add(new JScrollPane(descA), BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AMSTheme.BG_MAIN);
        content.add(main, BorderLayout.NORTH);
        content.add(desc, BorderLayout.CENTER);
        d.add(content, BorderLayout.CENTER); d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── Notes Tab ─────────────────────────────────────────────────────────────
    private JPanel buildNotesTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(20,24,20,24));

        JLabel title = new JLabel("\uD83D\uDCDD  Legal Notes — Case #" + caseId);
        title.setFont(AMSTheme.FONT_HEADING); title.setForeground(AMSTheme.PRIMARY);

        JPanel notesList = new JPanel();
        notesList.setLayout(new BoxLayout(notesList, BoxLayout.Y_AXIS));
        notesList.setBackground(AMSTheme.BG_MAIN);

        loadNotes(notesList);

        JScrollPane sp = new JScrollPane(notesList);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        btnRow.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("+ Add Note", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        addBtn.addActionListener(e -> showAddNoteDialog(notesList));
        btnRow.add(addBtn);

        page.add(title, BorderLayout.NORTH);
        page.add(sp, BorderLayout.CENTER);
        page.add(btnRow, BorderLayout.SOUTH);
        return page;
    }

    private void loadNotes(JPanel notesList) {
        notesList.removeAll();
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT n_id,SUBSTR(analysis,1,200),TO_CHAR(en_data,'DD-MON-YYYY'),followup_q,imp_judgement,con_status " +
                            "FROM NOTE1 WHERE case_id="+caseId+" ORDER BY en_data DESC").executeQuery();
                        while (rs.next()) {
                            final String anal=rs.getString(2),dt=rs.getString(3),fq=rs.getString(4),ij=rs.getString(5),cs=rs.getString(6);
                            SwingUtilities.invokeLater(() -> notesList.add(buildNoteCard(anal,dt,fq,ij,cs)));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> notesList.add(buildNoteCard("Client claims alibi for the night of incident. Need to verify with CCTV footage.",
                            "15-DEC-2024","Check CCTV","Sec 302 IPC applies","OPEN")));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> notesList.add(buildNoteCard("Demo note — analysis here...","Today","Follow up","Judgment ref","OPEN")));
                }
                return null;
            }
            @Override protected void done() { notesList.revalidate(); notesList.repaint(); }
        };
        w.execute();
    }

    private JPanel buildNoteCard(String analysis, String date, String followup, String judgment, String status) {
        JPanel card = new JPanel(new BorderLayout(10,8)) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFFFBF0));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(AMSTheme.ACCENT);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.fillRoundRect(0,0,5,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12,16,12,16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateL = new JLabel(date); dateL.setFont(AMSTheme.FONT_SMALL); dateL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel analL = new JLabel("<html>" + (analysis!=null?analysis:"") + "</html>");
        analL.setFont(AMSTheme.FONT_BODY); analL.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel fqL   = new JLabel("Follow-up: " + (followup!=null?followup:"—")); fqL.setFont(AMSTheme.FONT_SMALL); fqL.setForeground(AMSTheme.INFO);
        JLabel stL   = new JLabel("Status: " + (status!=null?status:"—")); stL.setFont(AMSTheme.FONT_SMALL); stL.setForeground(AMSTheme.SUCCESS);

        JPanel left = new JPanel(new GridLayout(4,1,0,4)); left.setOpaque(false);
        left.add(dateL); left.add(analL); left.add(fqL); left.add(stL);
        card.add(left, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0,0,8,0)); wrap.add(card);
        return wrap;
    }

    private void showAddNoteDialog(JPanel notesList) {
        JDialog d = new JDialog(this,"Add Legal Note",true);
        d.setSize(480,360);
        d.setLocationRelativeTo(this);
        JPanel main = new JPanel(new GridLayout(0,1,0,10));
        main.setBorder(new EmptyBorder(20,24,12,24));
        main.setBackground(AMSTheme.BG_MAIN);

        JTextArea analArea = new JTextArea(4,30); analArea.setFont(AMSTheme.FONT_BODY); analArea.setLineWrap(true);
        JTextField fqF = tf(); JTextField ijF = tf();

        main.add(lbl("Analysis / Notes *")); main.add(new JScrollPane(analArea));
        main.add(lbl("Follow-up Question")); main.add(fqF);
        main.add(lbl("Important Judgment Ref")); main.add(ijF);

        RoundedButton saveBtn = new RoundedButton("Save Note",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            if (analArea.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d,"Analysis is required."); return; }
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    Integer clientId = loadCaseClientId();
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO NOTE1(n_id,a_id,c_id,case_id,analysis,en_data,followup_q,imp_judgement,con_status) " +
                        "VALUES(note_seq.NEXTVAL,?,?,?,? ,SYSDATE,?,?,'OPEN')");
                    ps.setInt(1,advId);
                    if (clientId != null) ps.setInt(2, clientId); else ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setInt(3, caseId);
                    ps.setString(4, analArea.getText());
                    ps.setString(5, fqF.getText());
                    ps.setString(6, ijF.getText());
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d,"Note saved!"); d.dispose();
                loadNotes(notesList);
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        d.add(main, BorderLayout.CENTER); d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── Client Contact Tab ────────────────────────────────────────────────────
    private JPanel buildClientTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(20,24,20,24));

        JLabel title = new JLabel("\uD83D\uDCAC  Client Communication — Case #" + caseId);
        title.setFont(AMSTheme.FONT_HEADING); title.setForeground(AMSTheme.PRIMARY);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(0xF5F7FF));
        chatPanel.setBorder(new EmptyBorder(12,12,12,12));

        loadComms(chatPanel);

        JScrollPane sp = new JScrollPane(chatPanel);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel inputRow = new JPanel(new BorderLayout(8,0));
        inputRow.setBackground(AMSTheme.BG_MAIN);
        inputRow.setBorder(new EmptyBorder(10,0,0,0));
        JTextField msgF = new JTextField();
        msgF.setFont(AMSTheme.FONT_BODY);
        msgF.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),
            new EmptyBorder(8,12,8,12)));
        RoundedButton sendBtn = new RoundedButton("Send \u27A4", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        sendBtn.setPreferredSize(new Dimension(110,40));
        sendBtn.addActionListener(e -> {
            if (!msgF.getText().trim().isEmpty()) {
                sendMessage(chatPanel, msgF.getText().trim());
                msgF.setText("");
            }
        });
        msgF.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendBtn.doClick();
            }
        });
        inputRow.add(msgF, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);

        page.add(title, BorderLayout.NORTH);
        page.add(sp, BorderLayout.CENTER);
        page.add(inputRow, BorderLayout.SOUTH);
        return page;
    }

    private void loadComms(JPanel chatPanel) {
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT SUBSTR(summary,1,200),TO_CHAR(init_date,'DD-MON-YYYY HH24:MI'),direction " +
                            "FROM COMMUNICATION1 WHERE case_id="+caseId+" ORDER BY init_date").executeQuery();
                        while (rs.next()) {
                            final String msg=rs.getString(1),dt=rs.getString(2),dir=rs.getString(3);
                            SwingUtilities.invokeLater(() -> chatPanel.add(buildChatBubble(msg,dt,dir.equals("OUT"))));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            chatPanel.add(buildChatBubble("Hello, any update on my case?","10-DEC-2024 09:00",false));
                            chatPanel.add(buildChatBubble("Yes, next hearing is on 28-DEC. Please bring original documents.","10-DEC-2024 09:30",true));
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> chatPanel.add(buildChatBubble("Demo chat message here.","Today",false)));
                }
                return null;
            }
            @Override protected void done() { chatPanel.revalidate(); chatPanel.repaint(); }
        };
        w.execute();
    }

    private JPanel buildChatBubble(String msg, String time, boolean outgoing) {
        JPanel wrap = new JPanel(new FlowLayout(outgoing ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubble = new JPanel(new BorderLayout(0,4)) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(outgoing ? new Color(0xDCF8C6) : Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(8,12,8,12));
        bubble.setPreferredSize(new Dimension(Math.min(msg.length()*8+30, 420), -1));

        JLabel msgL = new JLabel("<html>"+msg+"</html>");
        msgL.setFont(AMSTheme.FONT_BODY);
        msgL.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel timeL = new JLabel(time);
        timeL.setFont(new Font("Segoe UI",Font.PLAIN,10));
        timeL.setForeground(AMSTheme.TEXT_MUTED);
        bubble.add(msgL, BorderLayout.CENTER);
        bubble.add(timeL, BorderLayout.SOUTH);
        wrap.add(bubble);
        return wrap;
    }

    private void sendMessage(JPanel chatPanel, String msg) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                Integer clientId = loadCaseClientId();
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO COMMUNICATION1(com_id,c_id,case_id,a_id,init_date,comm_mode,duration,summary,follow_action,direction) " +
                    "VALUES(comm_seq.NEXTVAL,?,?,?,?,SYSDATE,'MESSAGE',NULL,?,?)");
                if (clientId != null) ps.setInt(1, clientId); else ps.setNull(1, java.sql.Types.INTEGER);
                ps.setInt(2, caseId);
                ps.setInt(3, advId);
                ps.setString(4, msg);
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setString(6, "OUT");
                ps.executeUpdate(); c.commit();
            }
        } catch (Exception ignored) {}
        chatPanel.add(buildChatBubble(msg, "Just now", true));
        chatPanel.revalidate(); chatPanel.repaint();
    }

    private Integer loadCaseClientId() {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement("SELECT c_id FROM CASES1 WHERE case_id=?");
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── Brief View ────────────────────────────────────────────────────────────
    private void showBriefView() {
        JDialog d = new JDialog(this,"Brief Case Summary — "+caseTitle,false);
        d.setSize(680,500);
        d.setLocationRelativeTo(this);
        JTextArea area = new JTextArea();
        area.setFont(AMSTheme.FONT_BODY);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(20,20,20,20));
        area.setText("Loading case details...");

        SwingWorker<String,Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT cs.c_title,cs.c_type,cs.status,cs.priority_level,TO_CHAR(cs.filed_date,'DD-MON-YYYY')," +
                            "cs.court_name,cl.c_name,cs.problem_desc " +
                            "FROM CASES1 cs JOIN CLIENT1 cl ON cs.c_id=cl.c_id WHERE cs.case_id="+caseId).executeQuery();
                        if (rs.next()) {
                            sb.append("CASE TITLE    : ").append(rs.getString(1)).append("\n");
                            sb.append("TYPE          : ").append(rs.getString(2)).append("\n");
                            sb.append("STATUS        : ").append(rs.getString(3)).append("\n");
                            sb.append("PRIORITY      : ").append(rs.getInt(4)).append("\n");
                            sb.append("FILED DATE    : ").append(rs.getString(5)).append("\n");
                            sb.append("COURT         : ").append(rs.getString(6)).append("\n");
                            sb.append("CLIENT        : ").append(rs.getString(7)).append("\n\n");
                            sb.append("PROBLEM:\n").append(rs.getString(8)).append("\n\n");
                        }
                        // Events summary
                        sb.append("────── CASE EVENTS ──────\n");
                        rs = c.prepareStatement(
                            "SELECT title,TO_CHAR(event_date,'DD-MON-YYYY'),outcome FROM CASE_EVENT1 " +
                            "WHERE case_id="+caseId+" ORDER BY event_date").executeQuery();
                        while (rs.next()) sb.append("• ").append(rs.getString(2)).append(" — ").append(rs.getString(1)).append(" → ").append(rs.getString(3)).append("\n");
                        sb.append("\n────── HEARINGS ──────\n");
                        rs = c.prepareStatement(
                            "SELECT TO_CHAR(h_date,'DD-MON-YYYY'),purpose,status FROM HEARING1 WHERE case_id="+caseId+" ORDER BY h_date").executeQuery();
                        while (rs.next()) sb.append("• ").append(rs.getString(1)).append(" — ").append(rs.getString(2)).append(" [").append(rs.getString(3)).append("]\n");
                    } else {
                        sb.append("CASE #").append(caseId).append(" — ").append(caseTitle).append("\n\n");
                        sb.append("Demo brief view. Connect database for full details.\n");
                    }
                } catch (Exception e) { sb.append("Error loading details: ").append(e.getMessage()); }
                return sb.toString();
            }
            @Override protected void done() {
                try { area.setText(get()); area.setCaretPosition(0); } catch (Exception ignored) {}
            }
        };
        w.execute();

        d.add(new JScrollPane(area));
        d.setVisible(true);
    }

    // ── Menu Bar ──────────────────────────────────────────────────────────────
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setOpaque(false);
        mb.setBorder(null);
        // Just close button
        JButton closeBtn = new JButton("✕ Close");
        closeBtn.setFont(AMSTheme.FONT_SMALL);
        closeBtn.setForeground(AMSTheme.ACCENT_LIGHT);
        closeBtn.setBackground(new Color(255,255,255,0));
        closeBtn.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,80),1,true));
        closeBtn.setOpaque(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        mb.add(closeBtn);
        return mb;
    }

    private void showAddEventDialog() {
        JDialog d = new JDialog(this,"Add Case Event",true);
        d.setSize(480,380);
        d.setLocationRelativeTo(this);
        JPanel main = new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24));
        main.setBackground(AMSTheme.BG_MAIN);
        JTextField titleF=tf(), dateF=tf(), locF=tf(), outF=tf();
        JCheckBox impChk = new JCheckBox("Mark as Important"); impChk.setFont(AMSTheme.FONT_BODY);
        main.add(lbl("Event Title *")); main.add(titleF);
        main.add(lbl("Date (YYYY-MM-DD)")); main.add(dateF);
        main.add(lbl("Location")); main.add(locF);
        main.add(lbl("Outcome")); main.add(outF);
        main.add(impChk); main.add(new JLabel());
        RoundedButton saveBtn = new RoundedButton("Save Event",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            if (titleF.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d,"Title required."); return; }
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO CASE_EVENT(ev_id,case_id,event_date,title,location,outcome,is_important,task_status) " +
                        "VALUES(caseevent_seq.NEXTVAL,?,TO_DATE(?,'YYYY-MM-DD'),?,?,?,?,'COMPLETED')");
                    ps.setInt(1,caseId); ps.setString(2,dateF.getText()); ps.setString(3,titleF.getText());
                    ps.setString(4,locF.getText()); ps.setString(5,outF.getText());
                    ps.setInt(6, impChk.isSelected()?1:0);
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d,"Event added!");
                d.dispose();
                if (journeyPathwayPanel != null && journeyEventsPanel != null) {
                    loadYears(journeyPathwayPanel, journeyEventsPanel);
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        d.add(main,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────
    private JTable buildTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(34); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF));
        t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        return t;
    }

    private JTextField tf() {
        JTextField f = new JTextField();
        f.setFont(AMSTheme.FONT_BODY);
        f.setBackground(AMSTheme.BG_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),
            new EmptyBorder(5,10,5,10)));
        return f;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(AMSTheme.FONT_BOLD);
        l.setForeground(AMSTheme.TEXT_SECONDARY); return l;
    }
}
