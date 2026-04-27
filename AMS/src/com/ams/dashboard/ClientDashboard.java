package com.ams.dashboard;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.FileUploadUtil;
import com.ams.util.PhotoUtils;
import com.ams.components.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Client Portal � Standard, Feature-Complete Client Dashboard
 * Features: Profile inline edit, first-case onboarding, advocate picker, evidence upload
 */
public class ClientDashboard extends BaseDashboard {

    private static final long serialVersionUID = 1L;

    private int    clientId;
    private String clientName = "Client";

    // Profile fields held at class level so they can be refreshed

    public ClientDashboard(int clientId) {
        super("Client Portal", loadClientName(clientId), "CLIENT", clientId);
        this.clientId   = clientId;
        this.clientName = currentUser;
    }

    private static String loadClientName(int id) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement(
                    "SELECT c_name FROM CLIENT1 WHERE c_id=" + id).executeQuery();
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return "Client";
    }

    @Override
    protected void buildPages() {
        addSidebarSection("Overview");
        addNavItem("Dashboard",     "\uD83C\uDFE0", "home");
        addNavItem("My Profile",    "\uD83D\uDC64", "profile");

        addSidebarSection("Legal Cases");
        addNavItem("File a Case",   "\uD83D\uDCDD", "filecase");
        addNavItem("My Cases",      "\uD83D\uDCBC", "casestatus");
        addNavItem("Hearings",      "\u2696\uFE0F",  "hearings");
        addNavItem("Case Timeline", "\uD83D\uDCC5", "timeline");

        addSidebarSection("Communication");
        addNavItem("Messages",      "\uD83D\uDCAC", "messages");
        addNavItem("My Documents",  "\uD83D\uDCC4", "documents");

        addSidebarSection("Billing");
        addNavItem("Bills & Payments", "\uD83D\uDCB3", "bills");

        addPage("home",       buildHomePage());
        addPage("profile",    buildProfilePage());
        addPage("filecase",   buildFileCasePage());
        addPage("casestatus", buildCaseStatusPage());
        addPage("hearings",   buildHearingsPage());
        addPage("timeline",   buildTimelinePage());
        addPage("messages",   buildMessagesPage());
        addPage("documents",  buildDocumentsPage());
        addPage("bills",      buildBillsPage());
    }

    // ==========================================================================
    // HOME PAGE
    // ==========================================================================
    private JScrollPane buildHomePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        // Greeting
        JLabel greeting = new JLabel("Hello,  " + clientName + "  \uD83D\uDC4B");
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 28));
        greeting.setForeground(AMSTheme.TEXT_PRIMARY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Manage your legal cases and stay informed every step of the way.");
        sub.setFont(AMSTheme.FONT_BODY);
        sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Summary cards
        DashboardCard activeCard  = new DashboardCard("Active Cases",    "�", "\uD83D\uDCBC", AMSTheme.CARD_BLUE_1,   AMSTheme.CARD_BLUE_2,   () -> showPage("casestatus"));
        DashboardCard pendCard    = new DashboardCard("Pending Cases",   "�", "\u23F3",        AMSTheme.CARD_GOLD_1,   AMSTheme.CARD_GOLD_2,   () -> showPage("casestatus"));
        DashboardCard hearCard    = new DashboardCard("Upcoming Hearings","�","\u2696\uFE0F",  AMSTheme.CARD_PURPLE_1, AMSTheme.CARD_PURPLE_2, () -> showPage("hearings"));
        DashboardCard closedCard  = new DashboardCard("Closed Cases",    "�", "\u2714\uFE0F",  AMSTheme.CARD_GREEN_1,  AMSTheme.CARD_GREEN_2,  () -> showPage("casestatus"));
        loadClientCounts(activeCard, pendCard, hearCard, closedCard);

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        cardsRow.setOpaque(false); cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.add(activeCard); cardsRow.add(pendCard); cardsRow.add(hearCard); cardsRow.add(closedCard);

        // First-case onboarding banner (shown when client has no cases)
        JPanel bannerHolder = new JPanel(new BorderLayout());
        bannerHolder.setOpaque(false); bannerHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        bannerHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) return false;
                    ResultSet r = c.prepareStatement(
                        "SELECT COUNT(*) FROM CASES1 WHERE c_id=" + clientId).executeQuery();
                    return r.next() && r.getInt(1) == 0;
                } catch (Exception e) { return false; }
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        bannerHolder.add(buildFirstCaseBanner(), BorderLayout.CENTER);
                        bannerHolder.revalidate(); bannerHolder.repaint();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();

        // Quick actions
        JLabel qaTitle = sectionTitle("Quick Actions");
        qaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel qaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        qaRow.setOpaque(false); qaRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton fileCaseBtn = new RoundedButton("\uD83D\uDCDD  File New Case",    AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton viewCasesBtn= new RoundedButton("\uD83D\uDCBC  My Cases",         AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        RoundedButton hearingBtn  = new RoundedButton("\u2696\uFE0F  View Hearings",    AMSTheme.INFO,    AMSTheme.INFO.darker());
        RoundedButton profileBtn  = new RoundedButton("\uD83D\uDC64  Update Profile",   new Color(0x8E44AD), new Color(0x9B59B6));
        for (RoundedButton b : new RoundedButton[]{fileCaseBtn, viewCasesBtn, hearingBtn, profileBtn})
            b.setPreferredSize(new Dimension(180, 44));

        fileCaseBtn.addActionListener(e -> showPage("filecase"));
        viewCasesBtn.addActionListener(e -> showPage("casestatus"));
        hearingBtn.addActionListener(e -> showPage("hearings"));
        profileBtn.addActionListener(e -> showPage("profile"));
        qaRow.add(fileCaseBtn); qaRow.add(viewCasesBtn); qaRow.add(hearingBtn); qaRow.add(profileBtn);

        // Recent cases table
        JLabel casesTitle = sectionTitle("My Recent Cases");
        casesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = {"Case ID", "Title", "Type", "Status", "Filed Date", "Advocate", "Priority"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);
        JScrollPane tSp = new JScrollPane(table);
        tSp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8), 1));

        JPanel tableCard = cardPanel(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        tableCard.add(tSp, BorderLayout.CENTER);
        loadMyCases(model);

        // Double-click ? open case status
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showPage("casestatus");
            }
        });

        page.add(greeting); page.add(Box.createVerticalStrut(4));
        page.add(sub); page.add(Box.createVerticalStrut(24));
        page.add(cardsRow); page.add(Box.createVerticalStrut(12));
        page.add(bannerHolder); page.add(Box.createVerticalStrut(16));
        page.add(qaTitle); page.add(qaRow); page.add(Box.createVerticalStrut(24));
        page.add(casesTitle); page.add(Box.createVerticalStrut(8)); page.add(tableCard);
        return scrollWrap(page);
    }

    /** Banner displayed to brand-new clients with no cases yet */
    private JPanel buildFirstCaseBanner() {
        JPanel banner = new JPanel(new BorderLayout(16, 0)) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A73E8), getWidth(), 0, new Color(0x0D47A1));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(18, 24, 18, 24));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 4)); text.setOpaque(false);
        JLabel head = new JLabel("\uD83C\uDF1F  Welcome to AMS! You don't have any cases yet.");
        head.setFont(new Font("Segoe UI", Font.BOLD, 16)); head.setForeground(Color.WHITE);
        JLabel desc = new JLabel("Get started by filing your first legal case. Our advocates are ready to help you.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13)); desc.setForeground(new Color(255, 255, 255, 210));
        text.add(head); text.add(desc);

        RoundedButton btn = new RoundedButton("File Your First Case  ?", Color.WHITE, new Color(0xE8F0FE));
        btn.setForeground(new Color(0x1A73E8));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.addActionListener(e -> showPage("filecase"));

        banner.add(text, BorderLayout.CENTER);
        banner.add(btn, BorderLayout.EAST);
        return banner;
    }

    private void loadClientCounts(DashboardCard ac, DashboardCard pc, DashboardCard hc, DashboardCard cc) {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                int[] c = {0, 0, 0, 0};
                try {
                    Connection con = DBConnection.getConnection();
                    if (con == null) return c;
                    ResultSet r = con.prepareStatement("SELECT COUNT(*) FROM CASES1 WHERE c_id=" + clientId + " AND status='ACTIVE'").executeQuery();
                    if (r.next()) c[0] = r.getInt(1);
                    r = con.prepareStatement("SELECT COUNT(*) FROM CASES1 WHERE c_id=" + clientId + " AND status='PENDING'").executeQuery();
                    if (r.next()) c[1] = r.getInt(1);
                    r = con.prepareStatement("SELECT COUNT(*) FROM HEARING1 h JOIN CASES1 cs ON h.case_id=cs.case_id WHERE cs.c_id=" + clientId + " AND h.h_date>=SYSDATE").executeQuery();
                    if (r.next()) c[2] = r.getInt(1);
                    r = con.prepareStatement("SELECT COUNT(*) FROM CASES1 WHERE c_id=" + clientId + " AND status='CLOSED'").executeQuery();
                    if (r.next()) c[3] = r.getInt(1);
                } catch (Exception ignored) {}
                return c;
            }
            @Override protected void done() {
                try {
                    int[] c = get();
                    ac.setValue("" + c[0]); pc.setValue("" + c[1]);
                    hc.setValue("" + c[2]); cc.setValue("" + c[3]);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadMyCases(DefaultTableModel model) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) {
                        SwingUtilities.invokeLater(() -> {
                            model.addRow(new Object[]{});
                            JLabel noData = new JLabel("No cases found. Connect to database.");
                            noData.setForeground(AMSTheme.TEXT_MUTED);
                        });
                        return null;
                    }
                    ResultSet rs = c.prepareStatement(
                        "SELECT cs.case_id,cs.c_title,cs.c_type,cs.status," +
                        "TO_CHAR(cs.filed_date,'DD-MON-YYYY')," +
                        "COALESCE(a.a_name,'Unassigned'),cs.priority_level " +
                        "FROM CASES1 cs LEFT JOIN ADVOCATE1 a ON cs.assigned_adv=a.a_id " +
                        "WHERE cs.c_id=" + clientId + " ORDER BY cs.filed_date DESC").executeQuery();
                    while (rs.next()) {
                        final Object[] row = {
                            rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getString(6), rs.getInt(7)
                        };
                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> model.addRow(
                        new Object[]{1001, "Demo Case", "CIVIL", "ACTIVE", "10-JAN-2024", "Adv. Demo", 2}));
                }
                return null;
            }
        }.execute();
    }

    // ===================================================
    // PROFILE PAGE � Inline editable, updates header on save
    // ===================================================
    private JScrollPane buildProfilePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        // -- Header ----------------------------------------------------------
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setOpaque(false); headerBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel pageTitle = new JLabel("\uD83D\uDC64  My Profile");
        pageTitle.setFont(AMSTheme.FONT_TITLE); pageTitle.setForeground(AMSTheme.PRIMARY);

        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBtns.setOpaque(false);

        // Toggle: view vs edit mode
        final boolean[] editMode = {false};

        RoundedButton editBtn = new RoundedButton("\u270F  Edit Profile", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        editBtn.setPreferredSize(new Dimension(140, 38));
        topBtns.add(editBtn);
        headerBar.add(pageTitle, BorderLayout.WEST);
        headerBar.add(topBtns, BorderLayout.EAST);

        // -- Photo section ----------------------------------------------------
        JPanel photoWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoWrap.setOpaque(false); photoWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoLabel = new JLabel();
        photoLabel.setIcon(PhotoUtils.getDefaultProfilePhoto());
        photoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD0D0D0), 2),
            new EmptyBorder(2, 2, 2, 2)));
        photoLabel.setPreferredSize(new Dimension(120, 120));

        final byte[][] photoHolder = {null};
        RoundedButton uploadPhotoBtn = new RoundedButton("\uD83D\uDCF7  Change Photo", new Color(0x7F8C8D), new Color(0x95A5A6));
        uploadPhotoBtn.setPreferredSize(new Dimension(140, 34));
        uploadPhotoBtn.setVisible(false);
        uploadPhotoBtn.addActionListener(e -> {
            File f = FileUploadUtil.selectPhotoFile(this);
            if (f != null) {
                try {
                    byte[] b = FileUploadUtil.readFileAsBytes(f);
                    photoHolder[0] = b;
                    ImageIcon ic = PhotoUtils.loadImageFromBytes(b, 120, 120);
                    if (ic != null) photoLabel.setIcon(ic);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Photo load failed: " + ex.getMessage());
                }
            }
        });

        JPanel photoCol = new JPanel();
        photoCol.setLayout(new BoxLayout(photoCol, BoxLayout.Y_AXIS));
        photoCol.setOpaque(false);
        photoCol.add(photoLabel);
        photoCol.add(Box.createVerticalStrut(8));
        photoCol.add(uploadPhotoBtn);
        photoWrap.add(photoCol);

        // -- Profile card with inline editable fields -------------------------
        // We store both labels (view) and text fields (edit) and toggle visibility.
        JPanel profileCard = cardPanel(null);
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new EmptyBorder(20, 24, 20, 24));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Field definitions: {label, dbColumn, maxLength}
        String[] fieldNames  = {"Full Name","Email","Phone","National ID","Client Type","City","Street Address","Pincode","Notes"};
        JTextField[] viewTexts = new JTextField[fieldNames.length]; // read-only display
        JTextField[] editTexts = new JTextField[fieldNames.length]; // editable

        JPanel fieldsGrid = new JPanel(new GridLayout(0, 2, 16, 12));
        fieldsGrid.setOpaque(false);

        for (int i = 0; i < fieldNames.length; i++) {
            JLabel lbl = new JLabel(fieldNames[i] + ":");
            lbl.setFont(AMSTheme.FONT_BOLD); lbl.setForeground(AMSTheme.TEXT_SECONDARY);

            viewTexts[i] = new JTextField("�");
            viewTexts[i].setEditable(false);
            viewTexts[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E8F8)));
            viewTexts[i].setBackground(AMSTheme.BG_CARD);
            viewTexts[i].setFont(AMSTheme.FONT_BODY);
            viewTexts[i].setForeground(AMSTheme.TEXT_PRIMARY);

            editTexts[i] = new JTextField();
            editTexts[i].setFont(AMSTheme.FONT_BODY);
            editTexts[i].setBackground(AMSTheme.BG_INPUT);
            editTexts[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AMSTheme.PRIMARY_LIGHT, 1, true),
                new EmptyBorder(5, 10, 5, 10)));
            editTexts[i].setVisible(false);

            JPanel row = new JPanel(new BorderLayout(0, 4)); row.setOpaque(false);
            row.add(lbl, BorderLayout.NORTH);
            row.add(viewTexts[i], BorderLayout.CENTER);
            row.add(editTexts[i], BorderLayout.SOUTH);
            // Make edit text visible and view text invisible together
            viewTexts[i].addPropertyChangeListener("text", evt -> {});

            fieldsGrid.add(row);
        }

        profileCard.add(fieldsGrid);

        // -- Action buttons row ------------------------------------------------
        JPanel saveCancelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        saveCancelRow.setOpaque(false); saveCancelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveCancelRow.setVisible(false);

        RoundedButton saveBtn   = new RoundedButton("\u2714  Save Changes", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton cancelBtn = new RoundedButton("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));
        saveBtn.setPreferredSize(new Dimension(160, 40));
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        saveCancelRow.add(saveBtn); saveCancelRow.add(cancelBtn);

        JLabel statusLbl = new JLabel(" ");
        statusLbl.setFont(AMSTheme.FONT_SMALL);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // -- Toggle between view/edit ------------------------------------------
        Runnable enterEdit = () -> {
            editMode[0] = true;
            for (int i = 0; i < fieldNames.length; i++) {
                editTexts[i].setText(viewTexts[i].getText().equals("�") ? "" : viewTexts[i].getText());
                viewTexts[i].setVisible(false);
                editTexts[i].setVisible(true);
            }
            uploadPhotoBtn.setVisible(true);
            saveCancelRow.setVisible(true);
            editBtn.setText("\u2716  Cancel Edit");
            statusLbl.setText(" ");
            profileCard.revalidate(); profileCard.repaint();
        };

        Runnable exitEdit = () -> {
            editMode[0] = false;
            for (int i = 0; i < fieldNames.length; i++) {
                viewTexts[i].setVisible(true);
                editTexts[i].setVisible(false);
            }
            uploadPhotoBtn.setVisible(false);
            saveCancelRow.setVisible(false);
            editBtn.setText("\u270F  Edit Profile");
            profileCard.revalidate(); profileCard.repaint();
        };

        editBtn.addActionListener(e -> { if (editMode[0]) exitEdit.run(); else enterEdit.run(); });
        cancelBtn.addActionListener(e -> exitEdit.run());

        // -- Save logic --------------------------------------------------------
        saveBtn.addActionListener(e -> {
            String nameVal = editTexts[0].getText().trim();
            if (nameVal.isEmpty()) {
                statusLbl.setForeground(AMSTheme.DANGER);
                statusLbl.setText("Full Name is required.");
                return;
            }
            saveBtn.setEnabled(false);
            saveBtn.setText("Saving�");
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    Connection c = DBConnection.getConnection();
                    if (c == null) return false;
                    int pincode = 0;
                    try { pincode = Integer.parseInt(editTexts[7].getText().trim()); } catch (Exception ignored) {}
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE CLIENT1 SET c_name=?,email=?,phone=?,nat_id=?,cl_type=?," +
                        "addr_city=?,addr_street=?,addr_pincode=?,comm_notes=?,photo_data=? WHERE c_id=?");
                    ps.setString(1, editTexts[0].getText().trim());
                    ps.setString(2, editTexts[1].getText().trim());
                    ps.setString(3, editTexts[2].getText().trim());
                    ps.setString(4, editTexts[3].getText().trim());
                    ps.setString(5, editTexts[4].getText().trim());
                    ps.setString(6, editTexts[5].getText().trim());
                    ps.setString(7, editTexts[6].getText().trim());
                    ps.setInt(8, pincode);
                    ps.setString(9, editTexts[8].getText().trim());
                    ps.setBytes(10, photoHolder[0]);
                    ps.setInt(11, clientId);
                    ps.executeUpdate();
                    c.commit();
                    return true;
                }
                @Override protected void done() {
                    saveBtn.setEnabled(true);
                    saveBtn.setText("\u2714  Save Changes");
                    try {
                        if (get()) {
                            // Update view labels
                            for (int i = 0; i < fieldNames.length; i++) {
                                String v = editTexts[i].getText().trim();
                                viewTexts[i].setText(v.isEmpty() ? "�" : v);
                            }
                            // Update clientName and reload header
                            clientName = editTexts[0].getText().trim();
                            currentUser = clientName;
                            // Refresh sidebar display name
                            refreshProfilePage();
                            exitEdit.run();
                            statusLbl.setForeground(AMSTheme.SUCCESS);
                            statusLbl.setText("\u2714 Profile updated successfully!");
                        } else {
                            // Offline: update in-memory only
                            for (int i = 0; i < fieldNames.length; i++) {
                                String v = editTexts[i].getText().trim();
                                viewTexts[i].setText(v.isEmpty() ? "�" : v);
                            }
                            clientName = editTexts[0].getText().trim();
                            exitEdit.run();
                            statusLbl.setForeground(AMSTheme.WARNING);
                            statusLbl.setText("Saved locally (DB offline).");
                        }
                    } catch (Exception ex) {
                        statusLbl.setForeground(AMSTheme.DANGER);
                        statusLbl.setText("Error: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        // -- Load current profile data -----------------------------------------
        loadProfileData(viewTexts, editTexts, photoLabel, photoHolder);

        page.add(headerBar); page.add(Box.createVerticalStrut(20));
        page.add(photoWrap); page.add(Box.createVerticalStrut(20));
        page.add(profileCard); page.add(Box.createVerticalStrut(12));
        page.add(saveCancelRow); page.add(Box.createVerticalStrut(4));
        page.add(statusLbl);
        return scrollWrap(page);
    }

    private void loadProfileData(JTextField[] view, JTextField[] edit, JLabel photoLabel, byte[][] photoHolder) {
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() {
                String[] vals = new String[9];
                Arrays.fill(vals, "");
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT c_name,email,phone,nat_id,cl_type,addr_city,addr_street," +
                            "addr_pincode,comm_notes,photo_data FROM CLIENT1 WHERE c_id=" + clientId).executeQuery();
                        if (rs.next()) {
                            for (int i = 0; i < 8; i++) vals[i] = rs.getString(i + 1) != null ? rs.getString(i + 1) : "";
                            vals[8] = rs.getString(9) != null ? rs.getString(9) : "";
                            byte[] photo = rs.getBytes(10);
                            if (photo != null) {
                                photoHolder[0] = photo;
                                ImageIcon ic = PhotoUtils.loadImageFromBytes(photo, 120, 120);
                                if (ic != null) SwingUtilities.invokeLater(() -> photoLabel.setIcon(ic));
                            }
                        }
                    }
                } catch (Exception ignored) {}
                return vals;
            }
            @Override protected void done() {
                try {
                    String[] vals = get();
                    for (int i = 0; i < view.length; i++) {
                        view[i].setText(vals[i].isEmpty() ? "�" : vals[i]);
                        edit[i].setText(vals[i]);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void refreshProfilePage() {
        JComponent old = pages.get("profile");
        if (old != null) contentPanel.remove(old);
        addPage("profile", buildProfilePage());
        showPage("profile");
    }

    // ===================================================
    // FILE A CASE PAGE � Advocate dropdown, Evidence upload
    // ===================================================
    private JScrollPane buildFileCasePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\uD83D\uDCDD  File a New Case");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Complete the form below to submit your legal case request to our firm.");
        sub.setFont(AMSTheme.FONT_BODY); sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // -- Step 1: Case Details -----------------------------------------------
        JLabel step1Lbl = stepLabel("Step 1: Case Details");

        JPanel formGrid = cardPanel(new GridLayout(0, 2, 16, 14));
        formGrid.setBorder(new EmptyBorder(20, 24, 20, 24));
        formGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        LabeledField titleF   = new LabeledField("Case Title *", 30);
        LabeledField courtF   = new LabeledField("Preferred Court", 30);
        LabeledField lawCatF  = new LabeledField("Law Category", 20);
        LabeledField feeAmtF  = new LabeledField("Estimated Fee (?)", 10);
        LabeledField caNameF  = new LabeledField("CA / Consultant Name", 20);

        JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"-- Select Case Type --","CIVIL","CRIMINAL","CORPORATE","FAMILY","PROPERTY","LABOUR","TAX","OTHER"});
        typeBox.setFont(AMSTheme.FONT_BODY);

        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"HIGH","MEDIUM","LOW"});
        priorityBox.setFont(AMSTheme.FONT_BODY); priorityBox.setSelectedIndex(1);

        formGrid.add(titleF);                        formGrid.add(comboPanel("Case Type *", typeBox));
        formGrid.add(courtF);                        formGrid.add(lawCatF);
        formGrid.add(feeAmtF);                       formGrid.add(comboPanel("Priority", priorityBox));
        formGrid.add(caNameF);                       formGrid.add(new JLabel());

        // Problem description
        JPanel descPanel = new JPanel(new BorderLayout(0, 8));
        descPanel.setOpaque(false); descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        JLabel descLbl = new JLabel("Describe Your Legal Problem *");
        descLbl.setFont(AMSTheme.FONT_BOLD); descLbl.setForeground(AMSTheme.TEXT_SECONDARY);
        JTextArea descArea = new JTextArea(5, 50);
        descArea.setFont(AMSTheme.FONT_BODY); descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true), new EmptyBorder(8, 12, 8, 12)));
        descPanel.add(descLbl, BorderLayout.NORTH);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        // -- Step 2: Preferred Advocates ---------------------------------------
        JLabel step2Lbl = stepLabel("Step 2: Choose Preferred Advocate (Optional)");

        JPanel advPanel = cardPanel(new BorderLayout(16, 10));
        advPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        advPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Advocate dropdown loaded from DB
        JComboBox<String> advDropdown1 = new JComboBox<>();
        JComboBox<String> advDropdown2 = new JComboBox<>();
        JComboBox<String> advDropdown3 = new JComboBox<>();
        advDropdown1.setFont(AMSTheme.FONT_BODY);
        advDropdown2.setFont(AMSTheme.FONT_BODY);
        advDropdown3.setFont(AMSTheme.FONT_BODY);

        // Advocate profile info panel (shown when an advocate is selected)
        JPanel advProfilePanel = new JPanel(new GridLayout(1, 3, 12, 0));
        advProfilePanel.setOpaque(false);

        loadAdvocatesIntoDropdowns(advDropdown1, advDropdown2, advDropdown3, advProfilePanel);

        RoundedButton viewAdvBtn = new RoundedButton("\uD83D\uDC64  View Profile", AMSTheme.INFO, AMSTheme.INFO.darker());
        viewAdvBtn.setPreferredSize(new Dimension(130, 34));
        viewAdvBtn.addActionListener(e -> showAdvocateProfileDialog(advDropdown1));

        JPanel dropRow = new JPanel(new GridLayout(1, 3, 12, 0));
        dropRow.setOpaque(false);
        dropRow.add(comboPanel("1st Choice", advDropdown1));
        dropRow.add(comboPanel("2nd Choice", advDropdown2));
        dropRow.add(comboPanel("3rd Choice", advDropdown3));

        JPanel advHeader = new JPanel(new BorderLayout());
        advHeader.setOpaque(false);
        JLabel advNote = new JLabel("Select from our advocates. Click 'View Profile' to see expertise & ratings.");
        advNote.setFont(AMSTheme.FONT_SMALL); advNote.setForeground(AMSTheme.TEXT_MUTED);
        advHeader.add(advNote, BorderLayout.CENTER);
        advHeader.add(viewAdvBtn, BorderLayout.EAST);

        advPanel.add(advHeader, BorderLayout.NORTH);
        advPanel.add(dropRow, BorderLayout.CENTER);
        advPanel.add(advProfilePanel, BorderLayout.SOUTH);

        // -- Step 3: Add Evidence ----------------------------------------------
        JLabel step3Lbl = stepLabel("Step 3: Attach Evidence (Optional)");

        JPanel evidencePanel = cardPanel(new BorderLayout(0, 10));
        evidencePanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        evidencePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel evNote = new JLabel("Attach supporting files: documents (PDF, Word), photos (JPG, PNG), or videos (MP4, AVI).");
        evNote.setFont(AMSTheme.FONT_SMALL); evNote.setForeground(AMSTheme.TEXT_MUTED);

        // Evidence list panel
        JPanel evList = new JPanel();
        evList.setLayout(new BoxLayout(evList, BoxLayout.Y_AXIS));
        evList.setBackground(new Color(0xF8FAFF));
        evList.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane evListScroll = new JScrollPane(evList);
        evListScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8), 1));
        evListScroll.setPreferredSize(new Dimension(0, 140));

        // Stored evidence files: {file, type, description}
        List<Object[]> evidenceFiles = new ArrayList<>();

        JPanel evBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        evBtns.setOpaque(false);

        RoundedButton addDocBtn   = evBtn("\uD83D\uDCC4  Add Document",  new Color(0x2980B9));
        RoundedButton addPhotoBtn = evBtn("\uD83D\uDDBC  Add Photo",     new Color(0x27AE60));
        RoundedButton addVideoBtn = evBtn("\uD83C\uDFA5  Add Video",     new Color(0x8E44AD));
        evBtns.add(addDocBtn); evBtns.add(addPhotoBtn); evBtns.add(addVideoBtn);

        addDocBtn.addActionListener(e ->
            pickAndAddEvidence("DOCUMENT", evidenceFiles, evList,
                new String[]{"PDF, Word, Excel", "pdf,doc,docx,xls,xlsx,txt"}));
        addPhotoBtn.addActionListener(e ->
            pickAndAddEvidence("PHOTO", evidenceFiles, evList,
                new String[]{"JPEG, PNG, BMP", "jpg,jpeg,png,bmp,gif"}));
        addVideoBtn.addActionListener(e ->
            pickAndAddEvidence("VIDEO", evidenceFiles, evList,
                new String[]{"MP4, AVI, MOV", "mp4,avi,mov,mkv,wmv"}));

        evidencePanel.add(evNote, BorderLayout.NORTH);
        evidencePanel.add(evBtns, BorderLayout.CENTER);
        evidencePanel.add(evListScroll, BorderLayout.SOUTH);

        // -- Submit ------------------------------------------------------------
        JLabel statusL = new JLabel(" ");
        statusL.setFont(AMSTheme.FONT_SMALL); statusL.setForeground(AMSTheme.DANGER);
        statusL.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton submitBtn = new RoundedButton("\u2705  Submit Case Request", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        submitBtn.setPreferredSize(new Dimension(240, 48)); submitBtn.setMaximumSize(new Dimension(240, 48));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            if (titleF.getText().trim().isEmpty()) {
                statusL.setText("Case Title is required."); return;
            }
            if (descArea.getText().trim().isEmpty()) {
                statusL.setText("Problem description is required."); return;
            }
            if (typeBox.getSelectedIndex() == 0) {
                statusL.setText("Please select a Case Type."); return;
            }
            submitBtn.setEnabled(false); submitBtn.setText("Submitting�");
            statusL.setText(" ");
            submitCaseWithEvidence(titleF, (String) typeBox.getSelectedItem(), courtF, lawCatF,
                feeAmtF, caNameF, (String) priorityBox.getSelectedItem(), descArea,
                advDropdown1, advDropdown2, advDropdown3,
                evidenceFiles, statusL, submitBtn);
        });

        page.add(title);           page.add(Box.createVerticalStrut(4));
        page.add(sub);             page.add(Box.createVerticalStrut(24));
        page.add(step1Lbl);        page.add(Box.createVerticalStrut(8));
        page.add(formGrid);        page.add(Box.createVerticalStrut(8));
        page.add(descPanel);       page.add(Box.createVerticalStrut(20));
        page.add(step2Lbl);        page.add(Box.createVerticalStrut(8));
        page.add(advPanel);        page.add(Box.createVerticalStrut(20));
        page.add(step3Lbl);        page.add(Box.createVerticalStrut(8));
        page.add(evidencePanel);   page.add(Box.createVerticalStrut(16));
        page.add(statusL);         page.add(Box.createVerticalStrut(8));
        page.add(submitBtn);
        return scrollWrap(page);
    }

    /** Load all advocates from DB into 3 dropdowns */
    private void loadAdvocatesIntoDropdowns(JComboBox<String> d1, JComboBox<String> d2,
                                             JComboBox<String> d3, JPanel profileCards) {
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() {
                List<String> items = new ArrayList<>();
                items.add("-- No Preference --");
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT a_id, a_name, expert_at, rating FROM ADVOCATE1 " +
                            "WHERE license_status='ACTIVE' ORDER BY a_name").executeQuery();
                        while (rs.next()) {
                            String expert = rs.getString(3) != null ? rs.getString(3) : "General";
                            double rat = rs.getDouble(4);
                            items.add(rs.getInt(1) + " | " + rs.getString(2)
                                + " | " + expert + " | \u2605 " + String.format("%.1f", rat));
                        }
                    }
                } catch (Exception ignored) {}
                // Demo fallbacks
                if (items.size() == 1) {
                    items.add("1 | Adv. Rajesh Kumar | Criminal Law | \u2605 4.5");
                    items.add("2 | Adv. Priya Sharma | Civil Law | \u2605 4.2");
                    items.add("3 | Adv. Suresh Iyer | Corporate Law | \u2605 4.8");
                }
                return items;
            }
            @Override protected void done() {
                try {
                    List<String> items = get();
                    for (String s : items) { d1.addItem(s); d2.addItem(s); d3.addItem(s); }
                    // Update profile cards when 1st choice changes
                    d1.addActionListener(ev -> updateAdvProfile(d1, profileCards));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    /** Shows a mini-card for selected advocate in the advocate panel */
    private void updateAdvProfile(JComboBox<String> dropdown, JPanel profileCards) {
        profileCards.removeAll();
        String sel = (String) dropdown.getSelectedItem();
        if (sel == null || sel.startsWith("--")) { profileCards.revalidate(); profileCards.repaint(); return; }
        String[] parts = sel.split("\\|");
        if (parts.length < 4) return;
        int advId = 0;
        try { advId = Integer.parseInt(parts[0].trim()); } catch (Exception e) { return; }
        final int fAdvId = advId;
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT a_name,expert_at,yoe,office_room,phone,email,rating,profile_notes " +
                            "FROM ADVOCATE1 WHERE a_id=" + fAdvId).executeQuery();
                        if (rs.next()) return new String[]{
                            rs.getString(1), rs.getString(2), String.valueOf(rs.getInt(3)),
                            rs.getString(4), rs.getString(5), rs.getString(6),
                            String.format("%.1f", rs.getDouble(7)), rs.getString(8)
                        };
                    }
                } catch (Exception ignored) {}
                return new String[]{parts[1].trim(), parts[2].trim(), "�", "�", "�", "�", parts[3].trim(), ""};
            }
            @Override protected void done() {
                try {
                    String[] d = get();
                    JPanel card = cardPanel(new GridLayout(4, 2, 8, 4));
                    card.setBorder(new EmptyBorder(10, 12, 10, 12));
                    card.add(il("\uD83D\uDC68\u200D\u2696\uFE0F  Name", d[0]));
                    card.add(il("Expertise", d[1]));
                    card.add(il("Experience", d[2] + " yrs"));
                    card.add(il("Room", d[3]));
                    card.add(il("Phone", d[4]));
                    card.add(il("Email", d[5]));
                    card.add(il("\u2605 Rating", d[6]));
                    card.add(il("Notes", d[7] != null ? d[7] : "�"));
                    profileCards.add(card);
                    profileCards.revalidate(); profileCards.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    /** Show full advocate profile dialog */
    private void showAdvocateProfileDialog(JComboBox<String> dropdown) {
        String sel = (String) dropdown.getSelectedItem();
        if (sel == null || sel.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please select an advocate first."); return;
        }
        String[] parts = sel.split("\\|");
        int advId = 0;
        try { advId = Integer.parseInt(parts[0].trim()); } catch (Exception e) { return; }

        JDialog d = new JDialog(this, "Advocate Profile", true);
        d.setSize(500, 400); d.setLocationRelativeTo(this);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AMSTheme.BG_MAIN); main.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel advTitle = new JLabel("\uD83D\uDC68\u200D\u2696\uFE0F  " + parts[1].trim());
        advTitle.setFont(AMSTheme.FONT_TITLE); advTitle.setForeground(AMSTheme.PRIMARY);

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 10));
        details.setOpaque(false);

        final int fAdvId = advId;
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement(
                    "SELECT a_name,expert_at,yoe,bar_enroll_no,office_room,phone,email,rating,profile_notes,yob,addr_city " +
                    "FROM ADVOCATE1 WHERE a_id=" + fAdvId).executeQuery();
                if (rs.next()) {
                    details.add(il("Name",        rs.getString(1)));
                    details.add(il("Expertise",   rs.getString(2)));
                    details.add(il("Experience",  rs.getInt(3) + " years"));
                    details.add(il("Bar Enroll #",rs.getString(4)));
                    details.add(il("Office Room", rs.getString(5)));
                    details.add(il("Phone",       rs.getString(6)));
                    details.add(il("Email",       rs.getString(7)));
                    details.add(il("\u2605 Rating",String.format("%.1f", rs.getDouble(8)) + " / 5.0"));
                    details.add(il("City",        rs.getString(11)));
                    details.add(il("Notes",       rs.getString(9) != null ? rs.getString(9) : "�"));
                }
            } else {
                details.add(il("Name",      parts[1].trim()));
                details.add(il("Expertise", parts[2].trim()));
                details.add(il("Rating",    parts[3].trim()));
            }
        } catch (Exception ex) {
            details.add(il("Name", parts[1].trim()));
        }

        main.add(advTitle, BorderLayout.NORTH);
        main.add(new JScrollPane(details), BorderLayout.CENTER);
        RoundedButton closeBtn = new RoundedButton("Close", new Color(0x95A5A6), new Color(0xBDC3C7));
        closeBtn.addActionListener(ev -> d.dispose());
        JPanel btmRow = new JPanel(new FlowLayout(FlowLayout.CENTER)); btmRow.setOpaque(false); btmRow.add(closeBtn);
        main.add(btmRow, BorderLayout.SOUTH);
        d.add(main); d.setVisible(true);
    }

    /** Opens file chooser for evidence, adds to list */
    private void pickAndAddEvidence(String evType, List<Object[]> evidenceFiles,
                                     JPanel evList, String[] filterInfo) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select " + evType + " File");
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String desc = JOptionPane.showInputDialog(this, "Brief description for this file:", f.getName());
            if (desc == null) desc = f.getName();

            final String fDesc = desc;
            evidenceFiles.add(new Object[]{f, evType, fDesc});

            // Add card to list
            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBackground(new Color(0xF0F4FF));
            card.setBorder(new CompoundBorder(
                new EmptyBorder(4, 0, 4, 0),
                new CompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD0DCF8), 1, true),
                    new EmptyBorder(8, 12, 8, 12))));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

            String icon = evType.equals("PHOTO") ? "\uD83D\uDDBC" : evType.equals("VIDEO") ? "\uD83C\uDFA5" : "\uD83D\uDCC4";
            JLabel fileL = new JLabel(icon + "  " + f.getName()
                + "  [" + evType + "]  � " + fDesc
                + "  (" + (f.length() / 1024) + " KB)");
            fileL.setFont(AMSTheme.FONT_BODY); fileL.setForeground(AMSTheme.TEXT_PRIMARY);

            RoundedButton removeBtn = new RoundedButton("\u2715", AMSTheme.DANGER, AMSTheme.DANGER.darker());
            removeBtn.setPreferredSize(new Dimension(40, 28));
            final Object[] evEntry = {f, evType, fDesc};
            evidenceFiles.add(evEntry);
            removeBtn.addActionListener(e -> {
                evidenceFiles.remove(evEntry);
                evList.remove(card);
                evList.revalidate(); evList.repaint();
            });

            card.add(fileL, BorderLayout.CENTER);
            card.add(removeBtn, BorderLayout.EAST);
            evList.add(card);
            evList.revalidate(); evList.repaint();
        }
    }

    /** Submit case + save evidence records */
    private void submitCaseWithEvidence(
            LabeledField titleF, String caseType, LabeledField courtF,
            LabeledField lawCatF, LabeledField feeAmtF, LabeledField caNameF,
            String priority, JTextArea descArea,
            JComboBox<String> adv1, JComboBox<String> adv2, JComboBox<String> adv3,
            List<Object[]> evidenceFiles, JLabel statusL, RoundedButton submitBtn) {

        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                Connection c = DBConnection.getConnection();
                if (c == null) return -1; // offline
                int priNum = "HIGH".equals(priority) ? 1 : "LOW".equals(priority) ? 3 : 2;
                double fee = 0;
                try { fee = Double.parseDouble(feeAmtF.getText().trim()); } catch (Exception ignored) {}

                // Insert case
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO CASES1(case_id,c_id,c_title,c_type,law_category,problem_desc,status," +
                    "priority_level,court_name,fee_amount,ca_name,filed_date) " +
                    "VALUES(case_seq.NEXTVAL,?,?,?,?,?,'PENDING',?,?,?,?,SYSDATE)");
                ps.setInt(1, clientId);
                ps.setString(2, titleF.getText().trim());
                ps.setString(3, caseType);
                ps.setString(4, lawCatF.getText().trim());
                ps.setString(5, descArea.getText().trim());
                ps.setInt(6, priNum);
                ps.setString(7, courtF.getText().trim());
                ps.setDouble(8, fee);
                ps.setString(9, caNameF.getText().trim());
                ps.executeUpdate();

                ResultSet rs = c.prepareStatement("SELECT case_seq.CURRVAL FROM dual").executeQuery();
                rs.next(); int caseId = rs.getInt(1);

                // Case event
                PreparedStatement ev = c.prepareStatement(
                    "INSERT INTO CASE_EVENT1(ev_id,case_id,title,description,event_date,task_status) " +
                    "VALUES(caseevent_seq.NEXTVAL,?,?,?,SYSDATE,'PENDING')");
                ev.setInt(1, caseId);
                ev.setString(2, "Case Filed");
                ev.setString(3, "Case filed by client. Awaiting advocate assignment.");
                ev.executeUpdate();

                // Preferred advocates
                java.util.List<JComboBox<String>> advBoxes = Arrays.asList(adv1, adv2, adv3);
                for (int rank = 0; rank < advBoxes.size(); rank++) {
                    String sel = (String) advBoxes.get(rank).getSelectedItem();
                    if (sel != null && !sel.startsWith("--")) {
                        try {
                            int aId = Integer.parseInt(sel.split("\\|")[0].trim());
                            PreparedStatement pa = c.prepareStatement(
                                "INSERT INTO PREFERRED_ADVOCATE1(pref_id,case_id,a_id,pref_rank,status) " +
                                "VALUES(pref_seq.NEXTVAL,?,?,?,'PENDING')");
                            pa.setInt(1, caseId); pa.setInt(2, aId); pa.setInt(3, rank + 1);
                            pa.executeUpdate();
                        } catch (Exception ignored) {}
                    }
                }

                // Evidence records
                File evidenceDir = new File("client_evidence");
                evidenceDir.mkdirs();
                for (Object[] evEntry : evidenceFiles) {
                    try {
                        File src = (File) evEntry[0];
                        String evType = (String) evEntry[1];
                        String evDesc = (String) evEntry[2];
                        String ext = src.getName().contains(".") ?
                            src.getName().substring(src.getName().lastIndexOf('.')) : "";
                        File dest = new File(evidenceDir,
                            evType.toLowerCase() + "_" + caseId + "_" + System.currentTimeMillis() + ext);
                        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        PreparedStatement pe = c.prepareStatement(
                            "INSERT INTO EVIDENCE1(e_id,case_id,e_type,e_source,e_desc," +
                            "collected_date,admissibility,file_path,submitted_court) " +
                            "VALUES(evidence_seq.NEXTVAL,?,?,?,?,SYSDATE,'PENDING',?,0)");
                        pe.setInt(1, caseId);
                        pe.setString(2, evType);
                        pe.setString(3, src.getName());
                        pe.setString(4, evDesc);
                        pe.setString(5, dest.getAbsolutePath());
                        pe.executeUpdate();
                    } catch (Exception ignored) {}
                }

                c.commit();
                return caseId;
            }

            @Override protected void done() {
                submitBtn.setEnabled(true);
                submitBtn.setText("\u2705  Submit Case Request");
                try {
                    int caseId = get();
                    if (caseId == -1) {
                        JOptionPane.showMessageDialog(ClientDashboard.this,
                            "Case submitted in Demo Mode!\n" +
                            "Your case has been recorded locally.\n" +
                            "Please connect to the database for full persistence.",
                            "Demo Mode", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        int evCount = evidenceFiles.size();
                        JOptionPane.showMessageDialog(ClientDashboard.this,
                            "\u2705  Case #" + caseId + " submitted successfully!\n\n" +
                            "  \u2022 Case Title: " + titleF.getText().trim() + "\n" +
                            "  \u2022 Type: " + caseType + "\n" +
                            (evCount > 0 ? "  \u2022 " + evCount + " evidence file(s) attached\n" : "") +
                            "\nOur team will review and assign an advocate shortly.",
                            "Case Filed Successfully", JOptionPane.INFORMATION_MESSAGE);
                    }
                    // Rebuild file case page (clear form) and show status
                    JComponent old = pages.get("filecase");
                    if (old != null) contentPanel.remove(old);
                    addPage("filecase", buildFileCasePage());
                    showPage("casestatus");
                    // Rebuild status page with new case
                    JComponent oldStatus = pages.get("casestatus");
                    if (oldStatus != null) contentPanel.remove(oldStatus);
                    addPage("casestatus", buildCaseStatusPage());
                    showPage("casestatus");
                } catch (Exception ex) {
                    statusL.setForeground(AMSTheme.DANGER);
                    statusL.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ===================================================
    // MY CASES (Case Status) PAGE
    // ===================================================
    private JScrollPane buildCaseStatusPage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false); titleBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("\uD83D\uDCBC  My Cases");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        RoundedButton fileNewBtn = new RoundedButton("+ File New Case", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        fileNewBtn.setPreferredSize(new Dimension(150, 36));
        fileNewBtn.addActionListener(e -> showPage("filecase"));
        titleBar.add(title, BorderLayout.WEST); titleBar.add(fileNewBtn, BorderLayout.EAST);

        JLabel sub = new JLabel("Track progress on all your active and past legal matters.");
        sub.setFont(AMSTheme.FONT_BODY); sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel casesList = new JPanel();
        casesList.setLayout(new BoxLayout(casesList, BoxLayout.Y_AXIS));
        casesList.setBackground(AMSTheme.BG_MAIN);
        casesList.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadCaseStatus(casesList);

        page.add(titleBar); page.add(Box.createVerticalStrut(4));
        page.add(sub); page.add(Box.createVerticalStrut(20));
        page.add(casesList);
        return scrollWrap(page);
    }

    private void loadCaseStatus(JPanel list) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { addDemoCaseStatus(list); return null; }
                    ResultSet rs = c.prepareStatement(
                        "SELECT cs.case_id,cs.c_title,cs.status,cs.c_type," +
                        "COALESCE(a.a_name,'Unassigned'),cs.priority_level," +
                        "TO_CHAR(cs.filed_date,'DD-MON-YYYY'),cs.court_name,cs.fee_amount " +
                        "FROM CASES1 cs LEFT JOIN ADVOCATE1 a ON cs.assigned_adv=a.a_id " +
                        "WHERE cs.c_id=" + clientId + " ORDER BY cs.filed_date DESC").executeQuery();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        final int cid = rs.getInt(1), pri = rs.getInt(6);
                        final String t = rs.getString(2), stat = rs.getString(3),
                            type = rs.getString(4), adv = rs.getString(5),
                            dt = rs.getString(7), court = rs.getString(8);
                        final double fee = rs.getDouble(9);
                        SwingUtilities.invokeLater(() ->
                            list.add(buildCaseStatusCard(cid, t, stat, type, adv, pri, dt, court, fee)));
                    }
                    if (!any) SwingUtilities.invokeLater(() -> list.add(buildNoCasesPanel()));
                } catch (Exception e) { addDemoCaseStatus(list); }
                return null;
            }
            @Override protected void done() { list.revalidate(); list.repaint(); }
        }.execute();
    }

    private void addDemoCaseStatus(JPanel list) {
        SwingUtilities.invokeLater(() -> {
            list.add(buildCaseStatusCard(1001, "State vs Rajan", "ACTIVE", "CRIMINAL",
                "Adv. Rajesh Kumar", 1, "10-JAN-2024", "High Court", 50000));
            list.add(buildCaseStatusCard(1002, "Property Dispute", "PENDING", "CIVIL",
                "Unassigned", 2, "20-FEB-2024", "District Court", 25000));
        });
    }

    private JPanel buildNoCasesPanel() {
        JPanel p = cardPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel("\uD83D\uDCC2  No cases yet. Click 'File New Case' to get started.",
            SwingConstants.CENTER);
        l.setFont(AMSTheme.FONT_BODY); l.setForeground(AMSTheme.TEXT_MUTED);
        RoundedButton b = new RoundedButton("File Your First Case", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(e -> showPage("filecase"));
        JPanel inner = new JPanel(); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        l.setAlignmentX(Component.CENTER_ALIGNMENT); b.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(l); inner.add(Box.createVerticalStrut(14)); inner.add(b);
        p.add(inner, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 14, 0)); wrap.add(p); return wrap;
    }

    private JPanel buildCaseStatusCard(int caseId, String caseTitle, String status, String type,
                                        String advocate, int priority, String filedDate,
                                        String court, double fee) {
        Color _c = "ACTIVE".equals(status) ? AMSTheme.SUCCESS :
                   "PENDING".equals(status) ? AMSTheme.WARNING :
                   "CLOSED".equals(status)  ? AMSTheme.TEXT_MUTED : AMSTheme.DANGER;
        final Color statColor = _c;

        JPanel card = new JPanel(new BorderLayout(12, 6)) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(statColor); g2.fillRoundRect(0, 0, 6, getHeight(), 4, 4);
                g2.setColor(new Color(0, 0, 0, 12)); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(16, 22, 16, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new GridLayout(4, 1, 0, 5)); left.setOpaque(false);
        JLabel ttl  = new JLabel("Case #" + caseId + "  �  " + caseTitle);
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 14)); ttl.setForeground(AMSTheme.TEXT_PRIMARY);

        JLabel meta = new JLabel("Type: " + type + "  \u2022  Court: " + (court != null ? court : "�")
            + "  \u2022  Filed: " + filedDate
            + (fee > 0 ? "  \u2022  Fee: \u20B9" + String.format("%,.0f", fee) : ""));
        meta.setFont(AMSTheme.FONT_SMALL); meta.setForeground(AMSTheme.TEXT_MUTED);

        JLabel advL = new JLabel("\uD83D\uDC68\u200D\u2696\uFE0F  " + advocate);
        advL.setFont(AMSTheme.FONT_BODY); advL.setForeground(AMSTheme.PRIMARY);

        String priStr = priority == 1 ? "HIGH" : priority == 2 ? "MEDIUM" : "LOW";
        Color priColor = priority == 1 ? AMSTheme.DANGER : priority == 2 ? AMSTheme.WARNING : AMSTheme.SUCCESS;
        JLabel priL = new JLabel("Priority: " + priStr);
        priL.setFont(new Font("Segoe UI", Font.BOLD, 11)); priL.setForeground(priColor);

        left.add(ttl); left.add(meta); left.add(advL); left.add(priL);

        // Right: status pill + progress bar
        JPanel right = new JPanel(new GridLayout(3, 1, 0, 6)); right.setOpaque(false);
        right.setPreferredSize(new Dimension(200, 100));

        JLabel statPill = new JLabel("  " + status + "  ", SwingConstants.CENTER) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(statColor.getRed(), statColor.getGreen(), statColor.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        statPill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statPill.setForeground(statColor); statPill.setOpaque(false);

        int prog = "ACTIVE".equals(status) ? 55 : "PENDING".equals(status) ? 10 :
                   "CLOSED".equals(status) ? 100 : 30;
        JLabel progLbl = new JLabel("Progress: " + prog + "%");
        progLbl.setFont(AMSTheme.FONT_SMALL); progLbl.setForeground(AMSTheme.TEXT_SECONDARY);

        final int progFinal = prog;
        JPanel progBar = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE0E8F8)); g2.fillRoundRect(0, 2, getWidth(), 10, 8, 8);
                g2.setColor(statColor);
                g2.fillRoundRect(0, 2, (int) (getWidth() * progFinal / 100.0), 10, 8, 8);
                g2.dispose();
            }
        };
        progBar.setPreferredSize(new Dimension(180, 14)); progBar.setOpaque(false);
        right.add(statPill); right.add(progLbl); right.add(progBar);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); btnPanel.setOpaque(false);
        if ("PENDING".equals(status)) {
            RoundedButton editBtn = new RoundedButton("\u270F Edit", AMSTheme.INFO, AMSTheme.INFO.darker());
            editBtn.setPreferredSize(new Dimension(90, 32));
            editBtn.addActionListener(e -> showEditCaseDialog(caseId));
            btnPanel.add(editBtn);
        }
        if ("CLOSED".equals(status)) {
            RoundedButton rateBtn = new RoundedButton("\u2605 Rate", AMSTheme.ACCENT, AMSTheme.ACCENT.darker());
            rateBtn.setPreferredSize(new Dimension(90, 32));
            rateBtn.addActionListener(e -> {
                String r = JOptionPane.showInputDialog(this, "Rate your advocate (1-5):", "5");
                if (r != null) JOptionPane.showMessageDialog(this, "Thank you for rating!");
            });
            btnPanel.add(rateBtn);
        }

        JPanel rightFull = new JPanel(new BorderLayout(0, 6)); rightFull.setOpaque(false);
        rightFull.add(right, BorderLayout.CENTER); rightFull.add(btnPanel, BorderLayout.SOUTH);

        card.add(left, BorderLayout.CENTER); card.add(rightFull, BorderLayout.EAST);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 14, 0)); wrap.add(card); return wrap;
    }

    private void showEditCaseDialog(int caseId) {
        JDialog d = new JDialog(this, "Edit Case #" + caseId, true);
        d.setSize(520, 380); d.setLocationRelativeTo(this);
        JPanel main = new JPanel(); main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(AMSTheme.BG_MAIN); main.setBorder(new EmptyBorder(24, 28, 16, 28));

        JLabel dlgTitle = new JLabel("Edit Case #" + caseId);
        dlgTitle.setFont(AMSTheme.FONT_TITLE); dlgTitle.setForeground(AMSTheme.PRIMARY);
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false); grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        LabeledField titleF  = new LabeledField("Case Title", 30);
        LabeledField typeF   = new LabeledField("Case Type", 20);
        LabeledField courtF  = new LabeledField("Court", 30);
        JTextArea descArea = new JTextArea(3, 30); descArea.setFont(AMSTheme.FONT_BODY);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1), new EmptyBorder(6, 10, 6, 10)));

        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement(
                    "SELECT c_title,c_type,court_name,problem_desc FROM CASES1 WHERE case_id=" + caseId).executeQuery();
                if (rs.next()) {
                    titleF.setText(rs.getString(1)); typeF.setText(rs.getString(2));
                    courtF.setText(rs.getString(3));
                    descArea.setText(rs.getString(4) != null ? rs.getString(4) : "");
                }
            }
        } catch (Exception ignored) {}

        grid.add(titleF); grid.add(typeF); grid.add(courtF); grid.add(new JLabel());

        JPanel descP = new JPanel(new BorderLayout(0, 6)); descP.setOpaque(false);
        descP.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descLbl = new JLabel("Problem Description");
        descLbl.setFont(AMSTheme.FONT_BOLD); descLbl.setForeground(AMSTheme.TEXT_SECONDARY);
        descP.add(descLbl, BorderLayout.NORTH); descP.add(new JScrollPane(descArea), BorderLayout.CENTER);

        RoundedButton saveBtn = new RoundedButton("Save Changes", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE CASES1 SET c_title=?,c_type=?,court_name=?,problem_desc=? WHERE case_id=?");
                    ps.setString(1, titleF.getText().trim());
                    ps.setString(2, typeF.getText().trim());
                    ps.setString(3, courtF.getText().trim());
                    ps.setString(4, descArea.getText().trim());
                    ps.setInt(5, caseId);
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d, "Case updated successfully!");
                d.dispose();
                JComponent old = pages.get("casestatus");
                if (old != null) contentPanel.remove(old);
                addPage("casestatus", buildCaseStatusPage());
                showPage("casestatus");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER)); btnRow.setOpaque(false); btnRow.add(saveBtn);
        main.add(dlgTitle); main.add(Box.createVerticalStrut(16));
        main.add(grid); main.add(Box.createVerticalStrut(12));
        main.add(descP);
        d.add(main, BorderLayout.CENTER); d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ===================================================
    // HEARINGS PAGE
    // ===================================================
    private JScrollPane buildHearingsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\u2696\uFE0F  My Hearing Schedule");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        String[] cols = {"H-ID", "Case Title", "Date", "Time", "Court House", "Room", "Purpose", "Judge", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8), 1));

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) {
                        SwingUtilities.invokeLater(() -> model.addRow(
                            new Object[]{2001, "State vs Rajan", "28-DEC-2024", "10:00 AM",
                                "High Court", "Hall 3", "Arguments", "J. Sharma", "UPCOMING"}));
                        return null;
                    }
                    ResultSet rs = c.prepareStatement(
                        "SELECT h.h_id,cs.c_title,TO_CHAR(h.h_date,'DD-MON-YYYY'),h.h_time," +
                        "h.court_house,h.room_no,h.purpose,h.judge_name,h.status " +
                        "FROM HEARING1 h JOIN CASES1 cs ON h.case_id=cs.case_id " +
                        "WHERE cs.c_id=" + clientId + " ORDER BY h.h_date").executeQuery();
                    while (rs.next()) {
                        final Object[] row = {
                            rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)
                        };
                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> model.addRow(
                        new Object[]{2001, "Demo Case", "01-JAN-2025", "10:00", "High Court", "1", "Arguments", "J. Demo", "UPCOMING"}));
                }
                return null;
            }
        }.execute();

        page.add(title, BorderLayout.NORTH);
        page.add(sp, BorderLayout.CENTER);
        return scrollWrap(page);
    }

    // ===================================================
    // TIMELINE PAGE
    // ===================================================
    private JScrollPane buildTimelinePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\uD83D\uDCC5  Case Timeline");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(AMSTheme.BG_MAIN);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) {
                        SwingUtilities.invokeLater(() -> {
                            list.add(buildTimelineCard("State vs Rajan","Evidence Submitted","15-DEC-2024","Filing documents and FIR submitted to court.","Await hearing","ON_TRACK"));
                            list.add(buildTimelineCard("Property Dispute","Case Filed","01-DEC-2024","Case filed and pending advocate assignment.","Advocate review","PENDING"));
                        });
                        return null;
                    }
                    ResultSet rs = c.prepareStatement(
                        "SELECT cs.c_title,t.title,TO_CHAR(t.entry_date,'DD-MON-YYYY'),t.e_data," +
                        "t.next_step,t.status_indicator " +
                        "FROM TIMELINE1 t JOIN CASES1 cs ON t.case_id=cs.case_id " +
                        "WHERE cs.c_id=" + clientId + " ORDER BY t.entry_date DESC").executeQuery();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        final String ct = rs.getString(1), ttl = rs.getString(2),
                            dt = rs.getString(3), data = rs.getString(4),
                            nxt = rs.getString(5), stat = rs.getString(6);
                        SwingUtilities.invokeLater(() -> list.add(buildTimelineCard(ct, ttl, dt, data, nxt, stat)));
                    }
                    if (!any) SwingUtilities.invokeLater(() -> {
                        JLabel l = new JLabel("No timeline entries yet.");
                        l.setFont(AMSTheme.FONT_BODY); l.setForeground(AMSTheme.TEXT_MUTED);
                        list.add(l);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> list.add(buildTimelineCard("Demo Case","Filed","01-JAN-2024","Case submitted.","Await review","PENDING")));
                }
                return null;
            }
            @Override protected void done() { list.revalidate(); list.repaint(); }
        }.execute();

        page.add(title); page.add(Box.createVerticalStrut(20)); page.add(list);
        return scrollWrap(page);
    }

    private JPanel buildTimelineCard(String caseTitle, String milestone, String date,
                                      String data, String nextStep, String status) {
        Color sc = "ON_TRACK".equals(status) ? AMSTheme.SUCCESS :
                   "COMPLETED".equals(status) ? AMSTheme.INFO :
                   "DELAYED".equals(status) ? AMSTheme.DANGER : AMSTheme.WARNING;
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(sc); g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new GridLayout(4, 1, 0, 4)); left.setOpaque(false);
        JLabel ttl   = new JLabel(milestone); ttl.setFont(AMSTheme.FONT_BOLD); ttl.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel caseL = new JLabel("Case: " + caseTitle); caseL.setFont(AMSTheme.FONT_SMALL); caseL.setForeground(AMSTheme.INFO);
        JLabel dtL   = new JLabel(date); dtL.setFont(AMSTheme.FONT_SMALL); dtL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel dataL = new JLabel(data != null ? data : ""); dataL.setFont(AMSTheme.FONT_SMALL); dataL.setForeground(AMSTheme.TEXT_SECONDARY);
        left.add(ttl); left.add(caseL); left.add(dtL); left.add(dataL);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 4)); right.setOpaque(false);
        JLabel statL = new JLabel(status != null ? status : ""); statL.setFont(new Font("Segoe UI", Font.BOLD, 12)); statL.setForeground(sc);
        JLabel nextL = new JLabel("Next: " + (nextStep != null ? nextStep : "�")); nextL.setFont(AMSTheme.FONT_SMALL); nextL.setForeground(AMSTheme.TEXT_SECONDARY);
        right.add(statL); right.add(nextL);

        card.add(left, BorderLayout.CENTER); card.add(right, BorderLayout.EAST);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 10, 0)); wrap.add(card); return wrap;
    }

    // ===================================================
    // MESSAGES PAGE
    // ===================================================
    private JScrollPane buildMessagesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\uD83D\uDCAC  Messages with Your Advocate");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        JPanel chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(new Color(0xF0F4FF));
        chatArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Load messages from DB if available
        loadMessages(chatArea);

        JScrollPane chatSp = new JScrollPane(chatArea);
        chatSp.setBorder(BorderFactory.createLineBorder(new Color(0xD0DCF8), 1));

        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(AMSTheme.BG_CARD);
        inputBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E8F8)),
            new EmptyBorder(10, 16, 10, 16)));

        JTextField msgField = new JTextField();
        msgField.setFont(AMSTheme.FONT_BODY);
        msgField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        msgField.setBackground(AMSTheme.BG_INPUT);

        RoundedButton sendBtn = new RoundedButton("Send \u27A4", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        sendBtn.setPreferredSize(new Dimension(100, 40));

        ActionListener sendAction = e -> {
            String msg = msgField.getText().trim();
            if (!msg.isEmpty()) {
                chatArea.add(buildChatBubble(msg, new SimpleDateFormat("hh:mm a").format(new java.util.Date()), false));
                msgField.setText(""); chatArea.revalidate(); chatArea.repaint();
                // Auto-scroll
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vsb = chatSp.getVerticalScrollBar();
                    vsb.setValue(vsb.getMaximum());
                });
            }
        };
        sendBtn.addActionListener(sendAction);
        msgField.addActionListener(sendAction);

        inputBar.add(msgField, BorderLayout.CENTER);
        inputBar.add(sendBtn, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(AMSTheme.BG_MAIN);
        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(chatSp, BorderLayout.CENTER);
        mainPanel.add(inputBar, BorderLayout.SOUTH);

        page.add(mainPanel, BorderLayout.CENTER);
        return scrollWrap(page);
    }

    private void loadMessages(JPanel chatArea) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { addDemoMessages(chatArea); return null; }
                    ResultSet rs = c.prepareStatement(
                        "SELECT a.a_name,cm.summary,TO_CHAR(cm.init_date,'DD-MON-YYYY HH24:MI'),cm.direction " +
                        "FROM COMMUNICATION1 cm " +
                        "LEFT JOIN ADVOCATE1 a ON cm.a_id=a.a_id " +
                        "WHERE cm.c_id=" + clientId + " ORDER BY cm.init_date").executeQuery();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        boolean fromAdv = "IN".equals(rs.getString(4));
                        final String msg = rs.getString(2), time = rs.getString(3);
                        final boolean fFromAdv = fromAdv;
                        SwingUtilities.invokeLater(() -> chatArea.add(buildChatBubble(msg, time, fFromAdv)));
                    }
                    if (!any) addDemoMessages(chatArea);
                } catch (Exception e) { addDemoMessages(chatArea); }
                return null;
            }
            @Override protected void done() { chatArea.revalidate(); chatArea.repaint(); }
        }.execute();
    }

    private void addDemoMessages(JPanel chatArea) {
        SwingUtilities.invokeLater(() -> {
            chatArea.add(buildChatBubble("Hello, what is the current status of my case?", "09:00 AM", false));
            chatArea.add(buildChatBubble("Good morning! Your case is progressing well. The next hearing is scheduled for 28-DEC.", "09:15 AM", true));
            chatArea.add(buildChatBubble("Do I need to bring any documents to the hearing?", "09:20 AM", false));
            chatArea.add(buildChatBubble("Yes, please bring the original title deeds, your national ID, and a passport photo.", "09:30 AM", true));
        });
    }

    private JPanel buildChatBubble(String msg, String time, boolean fromAdvocate) {
        JPanel wrap = new JPanel(new FlowLayout(fromAdvocate ? FlowLayout.LEFT : FlowLayout.RIGHT, 8, 4));
        wrap.setOpaque(false); wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel bubble = new JPanel(new BorderLayout(0, 4)) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fromAdvocate ? new Color(0xE3F2FD) : new Color(0xDCF8C6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16); g2.dispose();
            }
        };
        bubble.setOpaque(false); bubble.setBorder(new EmptyBorder(8, 14, 8, 14));
        bubble.setPreferredSize(new Dimension(Math.min(msg.length() * 8 + 40, 460), 70));

        JLabel nameL = new JLabel(fromAdvocate ? "\uD83D\uDC68\u200D\u2696\uFE0F Advocate" : "\uD83D\uDC64 You");
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 10));
        nameL.setForeground(fromAdvocate ? AMSTheme.PRIMARY : AMSTheme.SUCCESS);

        JLabel msgL = new JLabel("<html><body style='width:380px'>" + msg + "</body></html>");
        msgL.setFont(AMSTheme.FONT_BODY); msgL.setForeground(AMSTheme.TEXT_PRIMARY);

        JLabel timeL = new JLabel(time);
        timeL.setFont(new Font("Segoe UI", Font.PLAIN, 10)); timeL.setForeground(AMSTheme.TEXT_MUTED);

        bubble.add(nameL, BorderLayout.NORTH);
        bubble.add(msgL, BorderLayout.CENTER);
        bubble.add(timeL, BorderLayout.SOUTH);
        wrap.add(bubble); return wrap;
    }

    // ===================================================
    // DOCUMENTS PAGE
    // ===================================================
    private JScrollPane buildDocumentsPage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false); titleBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("\uD83D\uDCC4  My Documents & Evidence");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        RoundedButton uploadBtn = new RoundedButton("+ Upload Document", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        uploadBtn.setPreferredSize(new Dimension(170, 36));
        uploadBtn.addActionListener(e -> uploadDocumentDialog());
        titleBar.add(title, BorderLayout.WEST); titleBar.add(uploadBtn, BorderLayout.EAST);
        titleBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel sub = new JLabel("All documents and evidence files linked to your cases.");
        sub.setFont(AMSTheme.FONT_BODY); sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel docList = new JPanel();
        docList.setLayout(new BoxLayout(docList, BoxLayout.Y_AXIS));
        docList.setBackground(AMSTheme.BG_MAIN); docList.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadDocuments(docList);

        page.add(titleBar); page.add(Box.createVerticalStrut(4));
        page.add(sub); page.add(Box.createVerticalStrut(20));
        page.add(docList);
        return scrollWrap(page);
    }

    private void loadDocuments(JPanel list) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { addDemoDocuments(list); return null; }
                    ResultSet rs = c.prepareStatement(
                        "SELECT e.e_id,cs.c_title,e.e_type,e.e_source,e.file_path," +
                        "TO_CHAR(e.collected_date,'DD-MON-YYYY'),e.admissibility " +
                        "FROM EVIDENCE1 e JOIN CASES1 cs ON e.case_id=cs.case_id " +
                        "WHERE cs.c_id=" + clientId + " ORDER BY e.collected_date DESC").executeQuery();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        final int eid = rs.getInt(1);
                        final String ct = rs.getString(2), et = rs.getString(3),
                            src = rs.getString(4), fp = rs.getString(5),
                            dt = rs.getString(6), adm = rs.getString(7);
                        SwingUtilities.invokeLater(() -> list.add(buildDocCard(eid, ct, et, src, fp, dt, adm)));
                    }
                    if (!any) addDemoDocuments(list);
                } catch (Exception e) { addDemoDocuments(list); }
                return null;
            }
            @Override protected void done() { list.revalidate(); list.repaint(); }
        }.execute();
    }

    private void addDemoDocuments(JPanel list) {
        SwingUtilities.invokeLater(() -> {
            list.add(buildDocCard(3001, "State vs Rajan", "DOCUMENT", "FIR Copy", "", "10-JAN-2024", "ADMISSIBLE"));
            list.add(buildDocCard(3002, "Property Dispute", "DOCUMENT", "Title Deed", "", "20-FEB-2024", "PENDING"));
            list.add(buildDocCard(3003, "State vs Rajan", "PHOTO", "Crime Scene Photo", "", "15-DEC-2024", "PENDING"));
        });
    }

    private JPanel buildDocCard(int eid, String caseTitle, String type, String source,
                                 String filePath, String date, String admissibility) {
        String icon = "PHOTO".equals(type) ? "\uD83D\uDDBC\uFE0F" :
                      "VIDEO".equals(type) ? "\uD83C\uDFA5" : "\uD83D\uDCC4";
        Color admColor = "ADMISSIBLE".equals(admissibility) ? AMSTheme.SUCCESS :
                         "INADMISSIBLE".equals(admissibility) ? AMSTheme.DANGER : AMSTheme.WARNING;

        JPanel card = cardPanel(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(12, 18, 12, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4)); left.setOpaque(false);
        JLabel nameL = new JLabel(icon + "  " + source + "  [" + type + "]");
        nameL.setFont(AMSTheme.FONT_BOLD); nameL.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel metaL = new JLabel("Case: " + caseTitle + "  �  " + date);
        metaL.setFont(AMSTheme.FONT_SMALL); metaL.setForeground(AMSTheme.TEXT_MUTED);
        left.add(nameL); left.add(metaL);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); right.setOpaque(false);
        JLabel admL = new JLabel(admissibility);
        admL.setFont(new Font("Segoe UI", Font.BOLD, 11)); admL.setForeground(admColor);

        if (filePath != null && !filePath.isEmpty()) {
            RoundedButton viewBtn = new RoundedButton("View", AMSTheme.INFO, AMSTheme.INFO.darker());
            viewBtn.setPreferredSize(new Dimension(70, 28));
            viewBtn.addActionListener(e -> {
                File f = new File(filePath);
                if (f.exists()) {
                    try { Desktop.getDesktop().open(f); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(this, "Cannot open: " + filePath); }
                } else {
                    JOptionPane.showMessageDialog(this, "File not found on disk.");
                }
            });
            right.add(viewBtn);
        }
        right.add(admL);

        card.add(left, BorderLayout.CENTER); card.add(right, BorderLayout.EAST);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 8, 0)); wrap.add(card); return wrap;
    }

    private void uploadDocumentDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Upload Document");
        fc.setAcceptAllFileFilterUsed(true);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                "\uD83D\uDCC4 Document '" + f.getName() + "' selected.\n\n" +
                "To attach to a specific case, please use\n'File a Case' ? 'Add Evidence' section.",
                "Document Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ===================================================
    // UTILITY HELPERS
    // ===================================================
    private JLabel stepLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(AMSTheme.PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, AMSTheme.PRIMARY_LIGHT),
            new EmptyBorder(0, 0, 6, 0)));
        return l;
    }

    private JPanel comboPanel(String label, JComboBox<?> box) {
        JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(AMSTheme.FONT_BOLD); l.setForeground(AMSTheme.TEXT_SECONDARY);
        p.add(l, BorderLayout.NORTH); p.add(box, BorderLayout.CENTER); return p;
    }

    private RoundedButton evBtn(String text, Color color) {
        RoundedButton b = new RoundedButton(text, color, color.brighter());
        b.setPreferredSize(new Dimension(150, 36)); return b;
    }

    private JPanel il(String k, String v) {
        JPanel p = new JPanel(new BorderLayout(4, 0)); p.setOpaque(false);
        JLabel kl = new JLabel(k + ": "); kl.setFont(AMSTheme.FONT_BOLD); kl.setForeground(AMSTheme.TEXT_SECONDARY);
        JLabel vl = new JLabel(v != null ? v : "�"); vl.setFont(AMSTheme.FONT_BODY); vl.setForeground(AMSTheme.TEXT_PRIMARY);
        p.add(kl, BorderLayout.WEST); p.add(vl, BorderLayout.CENTER); return p;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(36); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.setSelectionForeground(AMSTheme.TEXT_PRIMARY);
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF));
        t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFF));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        return t;
    }

    // ===================================================
    // BILLS & PAYMENTS PAGE
    // ===================================================
    private JScrollPane buildBillsPage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\uD83D\uDCB3 Bills & Payments");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("View your bills and make payments for legal services.");
        subtitle.setFont(AMSTheme.FONT_BODY);
        subtitle.setForeground(AMSTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bills table
        JPanel billsCard = cardPanel(new BorderLayout());
        billsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        billsCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Pay ID", "Case", "Amount", "Category", "Type", "Bill No", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);
        loadClientBills(model);

        // Double-click to view bill details and make payment
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int payId = (Integer) model.getValueAt(table.getSelectedRow(), 0);
                    String status = (String) model.getValueAt(table.getSelectedRow(), 6);
                    showBillPaymentDialog(payId, status);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setBackground(AMSTheme.BG_CARD);
        billsCard.add(sp, BorderLayout.CENTER);
        billsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Instructions
        JPanel infoPanel = cardPanel(new BorderLayout());
        infoPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html><b>How to make payments:</b><br>" +
            "1. Double-click on a bill to view details<br>" +
            "2. Upload payment receipt (photo/image of transaction proof)<br>" +
            "3. Mark payment as completed<br>" +
            "4. Advocate will verify and update your payment status</html>");
        infoLabel.setFont(AMSTheme.FONT_BODY);
        infoLabel.setForeground(AMSTheme.TEXT_SECONDARY);
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        page.add(title);
        page.add(Box.createVerticalStrut(4));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(20));
        page.add(billsCard);
        page.add(Box.createVerticalStrut(16));
        page.add(infoPanel);

        return scrollWrap(page);
    }

    private void loadClientBills(DefaultTableModel model) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection con = DBConnection.getConnection();
                    if (con == null) {
                        SwingUtilities.invokeLater(() -> {
                            model.addRow(new Object[]{6001, "State vs Rajan", 25000.00, "TRAVEL", "FEE", "BILL001", "PENDING", "15-DEC-2024"});
                            model.addRow(new Object[]{6002, "Property Dispute", 5000.00, "HEARING", "TRAVEL", "BILL002", "PAID", "20-DEC-2024"});
                        });
                        return null;
                    }
                    String sql = "SELECT p.pay_id, cs.c_title, p.amount, p.pay_category, p.pay_type, p.bill_no, p.payment_status, TO_CHAR(p.pay_date,'DD-MON-YYYY') " +
                                 "FROM PAYMENT1 p JOIN CASES1 cs ON p.case_id = cs.case_id " +
                                 "WHERE cs.c_id = ? ORDER BY p.pay_date DESC";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, clientId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4),
                            rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        model.addRow(new Object[]{6001, "State vs Rajan", 25000.00, "TRAVEL", "FEE", "BILL001", "PENDING", "15-DEC-2024"});
                        model.addRow(new Object[]{6002, "Property Dispute", 5000.00, "HEARING", "TRAVEL", "BILL002", "PAID", "20-DEC-2024"});
                    });
                }
                return null;
            }
        };
        w.execute();
    }

    private void showBillPaymentDialog(int payId, String currentStatus) {
        JDialog d = new JDialog(this, "Bill Payment - ID: " + payId, true);
        d.setSize(600, 500);
        d.setLocationRelativeTo(this);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(AMSTheme.BG_MAIN);
        main.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Bill details
        JPanel detailsPanel = cardPanel(new GridLayout(0, 2, 12, 8));
        detailsPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Load bill details
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement(
                    "SELECT cs.c_title, p.amount, p.pay_category, p.pay_type, p.pay_mode, p.bill_no, p.payment_status, p.pay_date, p.notes, p.receipt_path " +
                    "FROM PAYMENT1 p JOIN CASES1 cs ON p.case_id = cs.case_id WHERE p.pay_id = ?");
                ps.setInt(1, payId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    detailsPanel.add(il("Case Title", rs.getString(1)));
                    detailsPanel.add(il("Amount", "₹" + String.format("%,.2f", rs.getDouble(2))));
                    detailsPanel.add(il("Category", rs.getString(3)));
                    detailsPanel.add(il("Type", rs.getString(4)));
                    detailsPanel.add(il("Payment Mode", rs.getString(5)));
                    detailsPanel.add(il("Bill Number", rs.getString(6)));
                    detailsPanel.add(il("Status", rs.getString(7)));
                    detailsPanel.add(il("Date", rs.getDate(8) != null ? rs.getDate(8).toString() : "N/A"));
                    detailsPanel.add(il("Notes", rs.getString(9) != null ? rs.getString(9) : "N/A"));

                    String receiptPath = rs.getString(10);
                    if (receiptPath != null && !receiptPath.isEmpty()) {
                        detailsPanel.add(il("Receipt", "Uploaded"));
                    } else {
                        detailsPanel.add(il("Receipt", "Not uploaded"));
                    }
                }
            } else {
                // Demo data
                detailsPanel.add(il("Case Title", "State vs Rajan"));
                detailsPanel.add(il("Amount", "₹25,000.00"));
                detailsPanel.add(il("Category", "TRAVEL"));
                detailsPanel.add(il("Type", "FEE"));
                detailsPanel.add(il("Payment Mode", "BANK_TRANSFER"));
                detailsPanel.add(il("Bill Number", "BILL001"));
                detailsPanel.add(il("Status", currentStatus));
                detailsPanel.add(il("Date", "15-DEC-2024"));
                detailsPanel.add(il("Notes", "Travel expenses for court visit"));
                detailsPanel.add(il("Receipt", "Not uploaded"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(d, "Error loading bill details: " + e.getMessage());
            return;
        }

        // Payment actions (only if status is PENDING)
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setOpaque(false);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if ("PENDING".equals(currentStatus)) {
            // Upload receipt button
            RoundedButton uploadBtn = new RoundedButton("📷 Upload Payment Receipt", AMSTheme.INFO, AMSTheme.INFO.darker());
            uploadBtn.setPreferredSize(new Dimension(200, 40));
            uploadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

            final String[] receiptPath = {null};
            uploadBtn.addActionListener(e -> {
                File f = FileUploadUtil.selectPhotoFile(d);
                if (f != null) {
                    try {
                        // Copy file to receipts directory
                        File receiptsDir = new File("client_receipts");
                        receiptsDir.mkdirs();
                        String ext = f.getName().contains(".") ? f.getName().substring(f.getName().lastIndexOf('.')) : ".jpg";
                        File dest = new File(receiptsDir, "receipt_" + payId + "_" + System.currentTimeMillis() + ext);
                        java.nio.file.Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                        receiptPath[0] = dest.getAbsolutePath();
                        JOptionPane.showMessageDialog(d, "Receipt uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        uploadBtn.setText("✅ Receipt Uploaded");
                        uploadBtn.setEnabled(false);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(d, "Error uploading receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Mark as paid button
            RoundedButton paidBtn = new RoundedButton("💰 Mark Payment as Completed", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
            paidBtn.setPreferredSize(new Dimension(250, 40));
            paidBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            paidBtn.addActionListener(e -> {
                if (receiptPath[0] == null) {
                    JOptionPane.showMessageDialog(d, "Please upload payment receipt first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        c.setAutoCommit(false);
                        PreparedStatement ps = c.prepareStatement(
                            "UPDATE PAYMENT1 SET payment_status = 'PAID', receipt_path = ?, receipt_type = 'IMAGE' WHERE pay_id = ?");
                        ps.setString(1, receiptPath[0]);
                        ps.setInt(2, payId);
                        ps.executeUpdate();
                        c.commit();

                        JOptionPane.showMessageDialog(d, "Payment marked as completed! Advocate will verify the receipt.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        d.dispose();

                        // Refresh bills page
                        JComponent old = pages.get("bills");
                        if (old != null) contentPanel.remove(old);
                        addPage("bills", buildBillsPage());
                        showPage("bills");

                    } else {
                        JOptionPane.showMessageDialog(d, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Error updating payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            actionsPanel.add(uploadBtn);
            actionsPanel.add(Box.createVerticalStrut(12));
            actionsPanel.add(paidBtn);
        } else {
            // Show payment completed message
            JLabel completedLabel = new JLabel("✅ Payment has been completed and verified.");
            completedLabel.setFont(AMSTheme.FONT_BOLD);
            completedLabel.setForeground(AMSTheme.SUCCESS);
            completedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            actionsPanel.add(completedLabel);
        }

        main.add(detailsPanel);
        main.add(Box.createVerticalStrut(20));
        main.add(actionsPanel);

        d.add(main);
        d.setVisible(true);
    }
}
