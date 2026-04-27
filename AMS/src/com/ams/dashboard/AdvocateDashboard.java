package com.ams.dashboard;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.FileUploadUtil;
import com.ams.util.PhotoUtils;
import com.ams.components.*;
import com.ams.advocate.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.sql.*;

/**
 * Senior Advocate Main Dashboard
 */
public class AdvocateDashboard extends BaseDashboard {

    private static final long serialVersionUID = 1L;

    private int advId;
    private String advName = "Advocate";
    private JLabel advPhotoLabel;
    private JPanel profileInfoPanel;

    private DashboardCard casesCard;
    private DashboardCard clientsCard;
    private DashboardCard hearingsCard;
    private DashboardCard tasksCard;
    private DashboardCard newCaseCard;
    private DefaultTableModel paymentRequestModel;
    private JTable paymentRequestTable;

    public AdvocateDashboard(int advId) {
        super("Senior Advocate", loadAdvName(advId), "SENIOR_ADVOCATE", advId);
        this.advId   = advId;
        this.advName = currentUser;
    }

    private static String loadAdvName(int id) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement(
                    "SELECT a_name FROM ADVOCATE1 WHERE a_id=" + id).executeQuery();
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception e) {
            // Silently handle database errors - demo mode
        }
        return "Advocate";
    }

    @Override
    protected void buildPages() {
        buildNavAndPages();
        loadCounts();
    }

    private void buildNavAndPages() {
        // ── Sidebar nav ──────────────────────────────────────────────────────
        addSidebarSection("Overview");
        addNavItem("Dashboard",        "\uD83C\uDFE0", "home");
        addNavItem("Profile",          "\uD83D\uDC64", "profile");

        addSidebarSection("Case Management");
        addNavItem("All Cases",        "\uD83D\uDCBC", "cases");
        addNavItem("New Case Request",  "\uD83D\uDCE5", "newcase");
        addNavItem("Hearings",         "\uD83D\uDD56", "hearings");
        addNavItem("Evidence",         "\uD83D\uDD0D", "evidence");
        addNavItem("Strategy & Notes", "\uD83D\uDCDD", "strategy");
        addNavItem("Timeline",         "\uD83D\uDCC5", "timeline");

        addSidebarSection("Clients & Junior");
        addNavItem("Clients",          "\uD83D\uDC65", "clients");
        addNavItem("Assign Junior",    "\uD83D\uDCCB", "junior");
        addNavItem("Communications",   "\uD83D\uDCAC", "comms");

        addSidebarSection("Billing & Payments");
        addNavItem("Generate Bills",   "\uD83D\uDCB3", "bills");
        addNavItem("Payment Requests", "\uD83D\uDCB0", "payments");

        addSidebarSection("Reminders");
        addNavItem("Reminders",        "\uD83D\uDD14", "reminders");

        // ── Pages ─────────────────────────────────────────────────────────────
        addPage("home",     buildHomePage());
        addPage("profile",  buildProfilePage());
        addPage("cases",    new CasesPage(advId).getPanel());
        addPage("newcase",  new NewCasePage(advId).getPanel());
        addPage("hearings", new HearingsPage(advId).getPanel());
        addPage("evidence", new EvidencePage(advId).getPanel());
        addPage("strategy", new StrategyPage(advId).getPanel());
        addPage("timeline", new TimelinePage(advId).getPanel());
        addPage("clients",  new ClientsPage(advId).getPanel());
        addPage("junior",   new JuniorManagePage(advId).getPanel());
        addPage("comms",    new CommunicationsPage(advId).getPanel());
        addPage("bills",    buildBillsPage());
        addPage("payments", buildPaymentsPage());
        addPage("reminders",new RemindersPage(advId).getPanel());
    }

    // ── Home Page ─────────────────────────────────────────────────────────────
    private JScrollPane buildHomePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        // Greeting
        JLabel greeting = new JLabel("Good Day,  " + advName + "  \uD83D\uDC4B");
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 26));
        greeting.setForeground(AMSTheme.TEXT_PRIMARY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Here is your case overview for today.");
        subtitle.setFont(AMSTheme.FONT_BODY);
        subtitle.setForeground(AMSTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Cards Row ─────────────────────────────────────────────────────────
        casesCard    = new DashboardCard("Total Cases",     "—", "\uD83D\uDCBC", AMSTheme.CARD_BLUE_1,   AMSTheme.CARD_BLUE_2,   () -> showPage("cases"));
        clientsCard  = new DashboardCard("Clients",         "—", "\uD83D\uDC65", AMSTheme.CARD_GREEN_1,  AMSTheme.CARD_GREEN_2,  () -> showPage("clients"));
        hearingsCard = new DashboardCard("Upcoming Hearings","—", "\uD83D\uDD56", AMSTheme.CARD_GOLD_1,   AMSTheme.CARD_GOLD_2,   () -> showPage("hearings"));
        tasksCard    = new DashboardCard("Junior Tasks",    "—", "\uD83D\uDCCB", AMSTheme.CARD_PURPLE_1, AMSTheme.CARD_PURPLE_2, () -> showPage("junior"));
        newCaseCard  = new DashboardCard("New Requests",    "—", "\uD83D\uDCE5", AMSTheme.CARD_RED_1,    AMSTheme.CARD_RED_2,    () -> showPage("newcase"));

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (DashboardCard c : new DashboardCard[]{casesCard, clientsCard, hearingsCard, tasksCard, newCaseCard})
            cardsRow.add(c);

        // ── Recent & Active Cases Table ───────────────────────────────────────
        JLabel recTitle = sectionTitle("Active Cases");
        recTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tableCard = cardPanel(new BorderLayout());
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Case ID", "Case Title", "Client", "Status", "Priority", "Filed Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);
        loadRecentCases(model);
        // Double-click a case row to edit it
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    Object cid = model.getValueAt(table.getSelectedRow(), 0);
                    if (cid instanceof Integer) showEditCaseDialog((Integer) cid);
                }
            }
        });

        JScrollPane tsp = new JScrollPane(table);
        tsp.setBorder(null);
        tsp.setBackground(AMSTheme.BG_CARD);
        tableCard.add(tsp, BorderLayout.CENTER);
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // ── Upcoming Hearings mini list ───────────────────────────────────────
        JLabel hTitle = sectionTitle("Upcoming Hearings");
        hTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel hCard = cardPanel(new BorderLayout());
        hCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        hCard.setBorder(new EmptyBorder(16,16,16,16));

        String[] hcols = {"Hearing ID","Case","Date","Court","Purpose","Status"};
        DefaultTableModel hModel = new DefaultTableModel(hcols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable hTable = buildStyledTable(hModel);
        loadUpcomingHearings(hModel);
        JScrollPane hsp = new JScrollPane(hTable);
        hsp.setBorder(null);
        hCard.add(hsp, BorderLayout.CENTER);
        hCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        page.add(greeting);
        page.add(Box.createVerticalStrut(4));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(24));
        page.add(cardsRow);
        page.add(Box.createVerticalStrut(24));
        page.add(recTitle);
        page.add(tableCard);
        page.add(Box.createVerticalStrut(24));
        page.add(hTitle);
        page.add(hCard);
        page.add(Box.createVerticalStrut(30));

        return scrollWrap(page);
    }

    // ── Profile Page ──────────────────────────────────────────────────────────
    private JScrollPane buildProfilePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("Advocate Profile");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel card = cardPanel(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoLabel = new JLabel(PhotoUtils.getDefaultProfilePhoto());
        photoLabel.setPreferredSize(new Dimension(100, 100));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(0xD0D0D0), 2));
        photoLabel.setOpaque(false);
        advPhotoLabel = photoLabel;

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 12, 8));
        infoPanel.setOpaque(false);
        profileInfoPanel = infoPanel;
        loadProfileData(infoPanel);

        RoundedButton updateBtn = new RoundedButton("Update Profile", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        updateBtn.addActionListener(e -> showUpdateProfileDialog());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.add(updateBtn);

        card.add(photoLabel, BorderLayout.WEST);
        JPanel rightInfo = new JPanel();
        rightInfo.setLayout(new BoxLayout(rightInfo, BoxLayout.Y_AXIS));
        rightInfo.setOpaque(false);
        rightInfo.add(infoPanel);
        rightInfo.add(Box.createVerticalStrut(12));
        rightInfo.add(btnRow);
        card.add(rightInfo, BorderLayout.CENTER);

        page.add(title);
        page.add(Box.createVerticalStrut(20));
        page.add(card);

        return scrollWrap(page);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(36);
        t.setFont(AMSTheme.FONT_BODY);
        t.setForeground(AMSTheme.TEXT_PRIMARY);
        t.setGridColor(new Color(0xE8EDF5));
        t.setShowGrid(true);
        t.setBackground(AMSTheme.BG_CARD);
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.setSelectionForeground(AMSTheme.TEXT_PRIMARY);
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF));
        t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0,AMSTheme.PRIMARY_LIGHT));
        return t;
    }

    private void loadCounts() {
        SwingWorker<int[], Void> w = new SwingWorker<>() {
            @Override protected int[] doInBackground() {
                int[] counts = {0, 0, 0, 0, 0};
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) return counts;
                    // cases
                    ResultSet r = c.prepareStatement("SELECT COUNT(*) FROM ADVOCATE_CASE1 WHERE a_id=" + advId).executeQuery();
                    if (r.next()) counts[0] = r.getInt(1);
                    // clients
                    r = c.prepareStatement("SELECT COUNT(DISTINCT c_id) FROM REPRESENTS1 WHERE a_id=" + advId).executeQuery();
                    if (r.next()) counts[1] = r.getInt(1);
                    // hearings
                    r = c.prepareStatement("SELECT COUNT(*) FROM HEARING1 h JOIN ADVOCATE_CASE1 ac ON h.case_id=ac.case_id WHERE ac.a_id=" + advId + " AND h.h_date>=SYSDATE").executeQuery();
                    if (r.next()) counts[2] = r.getInt(1);
                    // tasks
                    r = c.prepareStatement("SELECT COUNT(*) FROM TASK_ASSIGNMENT1 WHERE assigned_by=" + advId + " AND status='PENDING'").executeQuery();
                    if (r.next()) counts[3] = r.getInt(1);
                    // new cases
                    r = c.prepareStatement("SELECT COUNT(*) FROM CASES1 WHERE assigned_adv=" + advId + " AND status='PENDING'").executeQuery();
                    if (r.next()) counts[4] = r.getInt(1);
                } catch (Exception ignored) {}
                return counts;
            }
            @Override protected void done() {
                try {
                    int[] c = get();
                    casesCard.setValue(String.valueOf(c[0]));
                    clientsCard.setValue(String.valueOf(c[1]));
                    hearingsCard.setValue(String.valueOf(c[2]));
                    tasksCard.setValue(String.valueOf(c[3]));
                    newCaseCard.setValue(String.valueOf(c[4]));
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void loadRecentCases(DefaultTableModel model) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection con = DBConnection.getConnection();
                    if (con == null) { addDemoRows(model); return null; }
                    String sql = "SELECT cs.case_id,cs.c_title,cl.c_name,cs.status,cs.priority_level,cs.filed_date " +
                                 "FROM CASES1 cs JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id " +
                                 "JOIN CLIENT1 cl ON cs.c_id=cl.c_id WHERE ac.a_id=? ORDER BY cs.filed_date DESC";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, advId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getInt(5), rs.getDate(6)
                        });
                    }
                } catch (Exception e) { addDemoRows(model); }
                return null;
            }
        };
        w.execute();
    }

    private void addDemoRows(DefaultTableModel m) {
        SwingUtilities.invokeLater(() -> {
            m.addRow(new Object[]{1001,"State vs. Rajan","Arun Patel","ACTIVE",1,"2024-01-10"});
            m.addRow(new Object[]{1002,"Patel Property Dispute","Sunita Patel","PENDING",2,"2024-02-15"});
            m.addRow(new Object[]{1003,"Kumar Divorce Case","Raj Kumar","CLOSED",3,"2023-11-20"});
        });
    }

    private void loadUpcomingHearings(DefaultTableModel model) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection con = DBConnection.getConnection();
                    if (con == null) { addDemoHearings(model); return null; }
                    String sql = "SELECT h.h_id,cs.c_title,h.h_date,h.court_house,h.purpose,h.status " +
                                 "FROM HEARING1 h JOIN CASES1 cs ON h.case_id=cs.case_id " +
                                 "JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id " +
                                 "WHERE ac.a_id=? AND h.h_date>=SYSDATE ORDER BY h.h_date";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, advId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt(1),rs.getString(2),rs.getDate(3),
                            rs.getString(4),rs.getString(5),rs.getString(6)
                        });
                    }
                } catch (Exception e) { addDemoHearings(model); }
                return null;
            }
        };
        w.execute();
    }

    private void addDemoHearings(DefaultTableModel m) {
        SwingUtilities.invokeLater(() -> {
            m.addRow(new Object[]{2001,"State vs. Rajan","2024-12-28","High Court Chennai","Arguments","UPCOMING"});
            m.addRow(new Object[]{2002,"Patel Property","2025-01-10","District Court","Evidence","UPCOMING"});
        });
    }

    private void loadProfileData(JPanel panel) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                String name="", email="", phone="", city="", barNo="", license="", room="", street="", pincode="", expert="", yoe="", yob="", notes="", rating="";
                String joined="", photoPath="";
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        ResultSet rs = c.prepareStatement(
                            "SELECT a_name,bar_enroll_no,email,phone,license_status,office_room,joined_date,addr_city,addr_street,addr_pincode,expert_at,yoe,yob,profile_notes,rating,photo_path FROM ADVOCATE1 WHERE a_id=" + advId
                        ).executeQuery();
                        if (rs.next()) {
                            name=rs.getString(1) != null ? rs.getString(1) : ""; barNo=rs.getString(2) != null ? rs.getString(2) : ""; email = rs.getString(3) != null ? rs.getString(3) : ""; phone=rs.getString(4) != null ? rs.getString(4) : ""; license=rs.getString(5) != null ? rs.getString(5) : "";
                            room=rs.getString(6) != null ? rs.getString(6) : ""; joined = rs.getString(7) != null ? rs.getString(7) : ""; city=rs.getString(8) != null ? rs.getString(8) : ""; street=rs.getString(9) != null ? rs.getString(9) : ""; pincode=rs.getString(10) != null ? rs.getString(10) : "";
                            expert=rs.getString(11) != null ? rs.getString(11) : ""; yoe=rs.getString(12) != null ? rs.getString(12) : ""; yob=rs.getString(13) != null ? rs.getString(13) : ""; notes=rs.getString(14) != null ? rs.getString(14) : ""; rating=rs.getString(15) != null ? rs.getString(15) : "";
                            photoPath = rs.getString(16) != null ? rs.getString(16) : "";
                            if (photoPath != null && !photoPath.trim().isEmpty()) {
                                File f = new File(photoPath);
                                if (f.exists()) {
                                    ImageIcon icon = PhotoUtils.loadAndScaleImage(f, 100, 100);
                                    if (icon != null && advPhotoLabel != null) advPhotoLabel.setIcon(icon);
                                }
                            }
                        } else {
                            // Demo data if no DB data
                            name = "Adv. Rajesh Kumar"; barNo = "BAR123456"; email = "rajesh.kumar@lawfirm.com"; phone = "9876543210"; license = "ACTIVE";
                            room = "Room 101"; joined = "2020-01-15"; city = "Chennai"; street = "MG Road"; pincode = "600001";
                            expert = "Criminal Law"; yoe = "10"; yob = "1980"; notes = "Experienced criminal lawyer"; rating = "4.5";
                        }
                    }
                } catch (Exception ignored) {}
                final String n=name,bn=barNo,em=email,ph=phone,lic=license,rm=room,jn=joined,ct=city,st=street,pin=pincode,ex=expert,ye=yoe,yo=yob,no=notes,ra=rating;
                SwingUtilities.invokeLater(() -> {
                    panel.add(infoRow("Name", n.isEmpty() ? advName : n));
                    panel.add(infoRow("Bar Reg.", bn.isEmpty() ? "—" : bn));
                    panel.add(infoRow("Email", em.isEmpty() ? "—" : em));
                    panel.add(infoRow("Phone", ph.isEmpty() ? "—" : ph));
                    panel.add(infoRow("License", lic.isEmpty() ? "—" : lic));
                    panel.add(infoRow("Office", rm.isEmpty() ? "—" : rm));
                    panel.add(infoRow("City", ct.isEmpty() ? "—" : ct));
                    panel.add(infoRow("Street", st.isEmpty() ? "—" : st));
                    panel.add(infoRow("Pincode", pin.isEmpty() ? "—" : pin));
                    panel.add(infoRow("Joined", jn.isEmpty() ? "—" : jn));
                    panel.add(infoRow("Expertise", ex.isEmpty() ? "—" : ex));
                    panel.add(infoRow("Experience", ye.isEmpty() ? "—" : ye+" years"));
                    panel.add(infoRow("YOB", yo.isEmpty() ? "—" : yo));
                    panel.add(infoRow("Notes", no.isEmpty() ? "—" : no));
                    panel.add(infoRow("Rating", ra.isEmpty() ? "—" : ra));
                });
                return null;
            }
        };
        w.execute();
    }

    private void showEditCaseDialog(int caseId) {
        JDialog d = new JDialog(this, "Edit Case #" + caseId, true);
        d.setSize(700, 600);
        d.setLocationRelativeTo(this);
        JPanel main = new JPanel(new GridLayout(0, 2, 12, 10));
        main.setBorder(new EmptyBorder(20, 24, 12, 24));
        main.setBackground(AMSTheme.BG_MAIN);

        LabeledField titleF = new LabeledField("Case Title", 30);
        LabeledField typeF = new LabeledField("Case Type", 20);
        LabeledField lawCatF = new LabeledField("Law Category", 20);
        LabeledField statusF = new LabeledField("Status", 20);
        LabeledField priorityF = new LabeledField("Priority (1-3)", 5);
        LabeledField courtF = new LabeledField("Court Name", 30);
        LabeledField feeAmtF = new LabeledField("Fee Amount", 10);
        LabeledField totalFeesF = new LabeledField("Total Fees", 10);
        LabeledField caNameF = new LabeledField("CA Name", 20);
        LabeledField logTitleF = new LabeledField("Log Title", 30);

        JTextArea descArea = new JTextArea(4, 50);
        descArea.setFont(AMSTheme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        JPanel descPanel = new JPanel(new BorderLayout(0, 5));
        descPanel.setOpaque(false);
        JLabel descLbl = new JLabel("Problem Description");
        descLbl.setFont(AMSTheme.FONT_BOLD);
        descLbl.setForeground(AMSTheme.TEXT_SECONDARY);
        descPanel.add(descLbl, BorderLayout.NORTH);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        // Load current values
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement("SELECT c_title, c_type, law_category, problem_desc, status, priority_level, court_name, fee_amount, total_fees, ca_name, log_title FROM CASES1 WHERE case_id=?");
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    titleF.setText(rs.getString(1) != null ? rs.getString(1) : "");
                    typeF.setText(rs.getString(2) != null ? rs.getString(2) : "");
                    lawCatF.setText(rs.getString(3) != null ? rs.getString(3) : "");
                    descArea.setText(rs.getString(4) != null ? rs.getString(4) : "");
                    statusF.setText(rs.getString(5) != null ? rs.getString(5) : "");
                    priorityF.setText(String.valueOf(rs.getInt(6)));
                    courtF.setText(rs.getString(7) != null ? rs.getString(7) : "");
                    feeAmtF.setText(String.valueOf(rs.getDouble(8)));
                    totalFeesF.setText(String.valueOf(rs.getDouble(9)));
                    caNameF.setText(rs.getString(10) != null ? rs.getString(10) : "");
                    logTitleF.setText(rs.getString(11) != null ? rs.getString(11) : "");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(d, "Error loading case data: " + ex.getMessage());
            return;
        }

        main.add(titleF);
        main.add(typeF);
        main.add(lawCatF);
        main.add(statusF);
        main.add(priorityF);
        main.add(courtF);
        main.add(feeAmtF);
        main.add(totalFeesF);
        main.add(caNameF);
        main.add(logTitleF);
        main.add(descPanel);
        main.add(new JLabel());

        RoundedButton saveBtn = new RoundedButton("Save Changes", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    c.setAutoCommit(false);
                    PreparedStatement ps = c.prepareStatement("UPDATE CASES1 SET c_title=?, c_type=?, law_category=?, problem_desc=?, status=?, priority_level=?, court_name=?, fee_amount=?, total_fees=?, ca_name=?, log_title=? WHERE case_id=?");
                    ps.setString(1, titleF.getText().trim());
                    ps.setString(2, typeF.getText().trim());
                    ps.setString(3, lawCatF.getText().trim());
                    ps.setString(4, descArea.getText().trim());
                    ps.setString(5, statusF.getText().trim());
                    ps.setInt(6, Integer.parseInt(priorityF.getText().trim()));
                    ps.setString(7, courtF.getText().trim());
                    ps.setDouble(8, Double.parseDouble(feeAmtF.getText().trim()));
                    ps.setDouble(9, Double.parseDouble(totalFeesF.getText().trim()));
                    ps.setString(10, caNameF.getText().trim());
                    ps.setString(11, logTitleF.getText().trim());
                    ps.setInt(12, caseId);
                    ps.executeUpdate();
                    c.commit();
                    JOptionPane.showMessageDialog(d, "Case updated successfully!");
                    d.dispose();
                    // Refresh cases page
                    showPage("cases");
                }
            } catch (Exception ex) {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c != null) c.rollback();
                } catch (Exception ignore) {}
                JOptionPane.showMessageDialog(d, "Error updating case: " + ex.getMessage());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);
        d.setLayout(new BorderLayout());
        d.add(main, BorderLayout.CENTER);
        d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private JPanel infoRow(String key, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel k = new JLabel(key + ":");
        k.setFont(AMSTheme.FONT_BOLD);
        k.setForeground(AMSTheme.TEXT_SECONDARY);
        JLabel v = new JLabel(val);
        v.setFont(AMSTheme.FONT_BODY);
        v.setForeground(AMSTheme.TEXT_PRIMARY);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
    }

    private void refreshProfileInfo() {
        if (profileInfoPanel != null) {
            profileInfoPanel.removeAll();
            loadProfileData(profileInfoPanel);
            profileInfoPanel.revalidate();
            profileInfoPanel.repaint();
        }
    }

    private void showUpdateProfileDialog() {
        JDialog d = new JDialog(this, "Update Profile", true);
        d.setSize(640, 520);
        d.setLocationRelativeTo(this);

        JPanel main = new JPanel(new GridLayout(0, 2, 12, 12));
        main.setBorder(new EmptyBorder(20, 24, 20, 24));
        main.setBackground(AMSTheme.BG_MAIN);

        JLabel photoLabel = new JLabel(advPhotoLabel != null ? advPhotoLabel.getIcon() : PhotoUtils.getDefaultProfilePhoto());
        photoLabel.setPreferredSize(new Dimension(110, 110));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(0xD0D0D0), 2));

        JButton photoBtn = new JButton("Upload Photo");

        LabeledField nameF = new LabeledField("Name", 20);
        LabeledField barF  = new LabeledField("Bar ID", 24);
        LabeledField emailF = new LabeledField("Email", 30);
        LabeledField phoneF = new LabeledField("Phone", 15);
        LabeledField cityF  = new LabeledField("City", 20);
        LabeledField streetF= new LabeledField("Street", 30);
        LabeledField pinF   = new LabeledField("Pincode", 10);
        LabeledField licenseF = new LabeledField("License", 20);
        LabeledField officeF  = new LabeledField("Office Room", 10);
        LabeledField joinF    = new LabeledField("Joined Date (YYYY-MM-DD)", 15);
        LabeledField expertF  = new LabeledField("Expertise", 20);
        LabeledField yoeF     = new LabeledField("Experience (years)", 5);
        LabeledField yobF     = new LabeledField("Year of Birth", 5);
        LabeledField notesF   = new LabeledField("Profile Notes", 40);
        LabeledField ratingF  = new LabeledField("Rating", 5);

        final String[] photoPath = {null};

        photoBtn.addActionListener(e -> {
            File f = FileUploadUtil.selectPhotoFile(d);
            if (f != null) {
                ImageIcon i = PhotoUtils.loadAndScaleImage(f, 110, 110);
                if (i != null) {
                    photoLabel.setIcon(i);
                    photoPath[0] = f.getAbsolutePath();
                }
            }
        });

        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement("SELECT a_name,bar_enroll_no,email,phone,license_status,office_room,TO_CHAR(joined_date,'YYYY-MM-DD'),addr_city,addr_street,addr_pincode,expert_at,yoe,yob,profile_notes,rating,photo_path FROM ADVOCATE1 WHERE a_id="+advId).executeQuery();
                if (rs.next()) {
                    nameF.setText(rs.getString(1)); barF.setText(rs.getString(2)); emailF.setText(rs.getString(3)); phoneF.setText(rs.getString(4));
                    licenseF.setText(rs.getString(5)); officeF.setText(rs.getString(6)); joinF.setText(rs.getString(7)); cityF.setText(rs.getString(8));
                    streetF.setText(rs.getString(9)); pinF.setText(String.valueOf(rs.getInt(10))); expertF.setText(rs.getString(11)); yoeF.setText(String.valueOf(rs.getInt(12)));
                    yobF.setText(String.valueOf(rs.getInt(13))); notesF.setText(rs.getString(14)); ratingF.setText(String.valueOf(rs.getDouble(15)));
                    String storedPhoto = rs.getString(16);
                    if (storedPhoto != null && !storedPhoto.isEmpty()) {
                        File f = new File(storedPhoto);
                        if (f.exists()) {
                            ImageIcon i = PhotoUtils.loadAndScaleImage(f, 110, 110);
                            if (i != null) photoLabel.setIcon(i);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        main.add(new JLabel("Profile Photo")); main.add(photoLabel);
        main.add(new JLabel(" ")); main.add(photoBtn);
        main.add(nameF); main.add(barF);
        main.add(emailF); main.add(phoneF);
        main.add(cityF); main.add(streetF);
        main.add(pinF); main.add(licenseF);
        main.add(officeF); main.add(joinF);
        main.add(expertF); main.add(yoeF);
        main.add(yobF); main.add(notesF);
        main.add(ratingF);

        RoundedButton saveBtn = new RoundedButton("Save Changes", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    c.setAutoCommit(false);
                    PreparedStatement ps = c.prepareStatement("UPDATE ADVOCATE1 SET a_name=?,bar_enroll_no=?,email=?,phone=?,license_status=?,office_room=?,joined_date=TO_DATE(?, 'YYYY-MM-DD'),addr_city=?,addr_street=?,addr_pincode=?,expert_at=?,yoe=?,yob=?,profile_notes=?,rating=?,photo_path=? WHERE a_id=?");
                    ps.setString(1, nameF.getText().trim()); ps.setString(2, barF.getText().trim()); ps.setString(3, emailF.getText().trim()); ps.setString(4, phoneF.getText().trim());
                    ps.setString(5, licenseF.getText().trim()); ps.setString(6, officeF.getText().trim()); ps.setString(7, joinF.getText().trim()); ps.setString(8, cityF.getText().trim());
                    ps.setString(9, streetF.getText().trim()); ps.setInt(10, pinF.getText().trim().isEmpty() ? 0 : Integer.parseInt(pinF.getText().trim())); ps.setString(11, expertF.getText().trim());
                    ps.setInt(12, yoeF.getText().trim().isEmpty() ? 0 : Integer.parseInt(yoeF.getText().trim())); ps.setInt(13, yobF.getText().trim().isEmpty() ? 0 : Integer.parseInt(yobF.getText().trim()));
                    ps.setString(14, notesF.getText().trim()); ps.setDouble(15, ratingF.getText().trim().isEmpty() ? 0 : Double.parseDouble(ratingF.getText().trim()));
                    ps.setString(16, photoPath[0] != null ? photoPath[0] : "");
                    ps.setInt(17, advId);
                    ps.executeUpdate();
                    c.commit();
                }
                JOptionPane.showMessageDialog(d, "Profile updated successfully!");
                d.dispose();
                refreshProfileInfo();
            } catch (Exception ex) {
                try { Connection c = DBConnection.getConnection(); if(c!=null) c.rollback(); } catch (Exception ignore) {}
                JOptionPane.showMessageDialog(d, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);

        d.add(main, BorderLayout.CENTER);
        d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── Bills Page ────────────────────────────────────────────────────────────
    private JScrollPane buildBillsPage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\uD83D\uDCB3 Generate Bills");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Create payment requests for clients with different fee categories.");
        subtitle.setFont(AMSTheme.FONT_BODY);
        subtitle.setForeground(AMSTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Case selection dropdown
        JPanel casePanel = cardPanel(new BorderLayout(10, 0));
        casePanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        casePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        casePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel caseLabel = new JLabel("Select Case:");
        caseLabel.setFont(AMSTheme.FONT_BOLD);
        caseLabel.setForeground(AMSTheme.TEXT_SECONDARY);

        JComboBox<String> caseCombo = new JComboBox<>();
        caseCombo.addItem("Choose a case...");
        loadAdvocateCases(caseCombo);

        casePanel.add(caseLabel, BorderLayout.WEST);
        casePanel.add(caseCombo, BorderLayout.CENTER);

        // Bill generation form
        JPanel formPanel = cardPanel(new GridLayout(0, 2, 12, 12));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        LabeledField amountF = new LabeledField("Amount (₹)", 15);
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"TRAVEL", "HEARING", "MONTHLY_FEE", "CONSULTATION", "OTHER"});
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"FEE", "TRAVEL", "MISC", "ADVOCATE_FEE", "SERVICE"});
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"CASH", "BANK_TRANSFER", "ONLINE", "CHEQUE", "UPI"});
        LabeledField billNoF = new LabeledField("Bill Number", 20);
        JTextArea notesArea = new JTextArea(3, 30);
        notesArea.setFont(AMSTheme.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true),
            new EmptyBorder(8, 12, 8, 12)));

        JPanel notesPanel = new JPanel(new BorderLayout(0, 5));
        notesPanel.setOpaque(false);
        JLabel notesLabel = new JLabel("Notes/Description");
        notesLabel.setFont(AMSTheme.FONT_BOLD);
        notesLabel.setForeground(AMSTheme.TEXT_SECONDARY);
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        formPanel.add(amountF);
        formPanel.add(new JLabel("Fee Category:"));
        formPanel.add(new JLabel(" "));
        formPanel.add(categoryCombo);
        formPanel.add(new JLabel("Payment Type:"));
        formPanel.add(typeCombo);
        formPanel.add(new JLabel("Payment Mode:"));
        formPanel.add(modeCombo);
        formPanel.add(billNoF);
        formPanel.add(notesPanel);

        // Generate bill button
        RoundedButton generateBtn = new RoundedButton("Generate Bill & Send to Client", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        generateBtn.setPreferredSize(new Dimension(250, 40));
        generateBtn.addActionListener(e -> generateBill(caseCombo, amountF, categoryCombo, typeCombo, modeCombo, billNoF, notesArea));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.add(generateBtn);

        page.add(title);
        page.add(Box.createVerticalStrut(4));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(20));
        page.add(casePanel);
        page.add(Box.createVerticalStrut(16));
        page.add(formPanel);
        page.add(Box.createVerticalStrut(20));
        page.add(btnPanel);

        return scrollWrap(page);
    }

    // ── Payments Page ─────────────────────────────────────────────────────────
    private JScrollPane buildPaymentsPage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("\uD83D\uDCB0 Payment Requests");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("View and manage payment requests sent to clients.");
        subtitle.setFont(AMSTheme.FONT_BODY);
        subtitle.setForeground(AMSTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Payment requests table
        JPanel tableCard = cardPanel(new BorderLayout());
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Pay ID", "Case ID", "Client", "Amount", "Category", "Type", "Status", "Bill No", "Date", "Proof", "Receipt Path", "Receipt Type"};
        paymentRequestModel = new DefaultTableModel(cols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        paymentRequestTable = buildStyledTable(paymentRequestModel);
        paymentRequestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentRequestTable.removeColumn(paymentRequestTable.getColumnModel().getColumn(11));
        paymentRequestTable.removeColumn(paymentRequestTable.getColumnModel().getColumn(10));

        loadPaymentRequests(paymentRequestModel);

        JScrollPane sp = new JScrollPane(paymentRequestTable);
        sp.setBorder(null);
        sp.setBackground(AMSTheme.BG_CARD);
        tableCard.add(sp, BorderLayout.CENTER);
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JPanel actionCard = new JPanel(new BorderLayout(12, 12));
        actionCard.setOpaque(false);
        actionCard.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonsRow.setOpaque(false);
        RoundedButton viewProofBtn = new RoundedButton("View Payment Proof", AMSTheme.INFO, AMSTheme.INFO.darker());
        viewProofBtn.setPreferredSize(new Dimension(180, 36));
        viewProofBtn.setEnabled(false);
        RoundedButton verifyBtn = new RoundedButton("✅ Verify Payment", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        verifyBtn.setPreferredSize(new Dimension(170, 36));
        verifyBtn.setEnabled(false);
        buttonsRow.add(viewProofBtn);
        buttonsRow.add(verifyBtn);

        JLabel actionInfo = new JLabel("Select a payment to review proof and verify completion.");
        actionInfo.setFont(AMSTheme.FONT_BODY);
        actionInfo.setForeground(AMSTheme.TEXT_SECONDARY);
        actionInfo.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonsRow.add(actionInfo);

        actionCard.add(buttonsRow, BorderLayout.CENTER);

        viewProofBtn.addActionListener(e -> {
            int selected = paymentRequestTable.getSelectedRow();
            if (selected >= 0) {
                int modelIndex = paymentRequestTable.convertRowIndexToModel(selected);
                String receiptPath = (String) paymentRequestModel.getValueAt(modelIndex, 10);
                viewPaymentProof(receiptPath);
            }
        });
        verifyBtn.addActionListener(e -> {
            int selected = paymentRequestTable.getSelectedRow();
            if (selected >= 0) {
                int modelIndex = paymentRequestTable.convertRowIndexToModel(selected);
                int payId = (Integer) paymentRequestModel.getValueAt(modelIndex, 0);
                verifyPayment(payId);
            }
        });

        paymentRequestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePaymentActionControls(paymentRequestTable, paymentRequestModel, viewProofBtn, verifyBtn, actionInfo);
            }
        });

        paymentRequestTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && paymentRequestTable.getSelectedRow() >= 0) {
                    int modelIndex = paymentRequestTable.convertRowIndexToModel(paymentRequestTable.getSelectedRow());
                    String receiptPath = (String) paymentRequestModel.getValueAt(modelIndex, 10);
                    viewPaymentProof(receiptPath);
                }
            }
        });

        page.add(title);
        page.add(Box.createVerticalStrut(4));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(20));
        page.add(tableCard);
        page.add(actionCard);

        return scrollWrap(page);
    }

    private void loadAdvocateCases(JComboBox<String> combo) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection con = DBConnection.getConnection();
                    if (con == null) {
                        SwingUtilities.invokeLater(() -> {
                            combo.addItem("1001 - State vs. Rajan");
                            combo.addItem("1002 - Patel Property Dispute");
                        });
                        return null;
                    }
                    String sql = "SELECT cs.case_id, cs.c_title, cl.c_name FROM CASES1 cs " +
                                 "JOIN ADVOCATE_CASE1 ac ON cs.case_id = ac.case_id " +
                                 "JOIN CLIENT1 cl ON cs.c_id = cl.c_id " +
                                 "WHERE ac.a_id = ? ORDER BY cs.case_id";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, advId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String item = rs.getInt(1) + " - " + rs.getString(2) + " (" + rs.getString(3) + ")";
                        SwingUtilities.invokeLater(() -> combo.addItem(item));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        combo.addItem("1001 - State vs. Rajan");
                        combo.addItem("1002 - Patel Property Dispute");
                    });
                }
                return null;
            }
        };
        w.execute();
    }

    private void generateBill(JComboBox<String> caseCombo, LabeledField amountF, JComboBox<String> categoryCombo,
                             JComboBox<String> typeCombo, JComboBox<String> modeCombo, LabeledField billNoF, JTextArea notesArea) {
        if (caseCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a case.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String caseItem = (String) caseCombo.getSelectedItem();
        int caseId = Integer.parseInt(caseItem.split(" - ")[0]);

        try {
            double amount = Double.parseDouble(amountF.getText().trim());
            String category = (String) categoryCombo.getSelectedItem();
            String type = (String) typeCombo.getSelectedItem();
            String mode = (String) modeCombo.getSelectedItem();
            String billNo = billNoF.getText().trim();
            String notes = notesArea.getText().trim();

            Connection c = DBConnection.getConnection();
            if (c != null) {
                c.setAutoCommit(false);

                // Generate bill number if not provided
                if (billNo.isEmpty()) {
                    billNo = "BILL-" + System.currentTimeMillis();
                }

                // Insert payment request
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO PAYMENT1 (pay_id, case_id, pay_date, amount, pay_category, pay_type, pay_mode, payment_status, bill_no, handled_by, notes) " +
                    "VALUES (payment_seq.NEXTVAL, ?, SYSDATE, ?, ?, ?, ?, 'PENDING', ?, ?, ?)");
                ps.setInt(1, caseId);
                ps.setDouble(2, amount);
                ps.setString(3, category);
                ps.setString(4, type);
                ps.setString(5, mode);
                ps.setString(6, billNo);
                ps.setInt(7, advId);
                ps.setString(8, notes);
                ps.executeUpdate();

                c.commit();
                JOptionPane.showMessageDialog(this, "Bill generated successfully! Client will be notified.", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear form
                amountF.setText("");
                billNoF.setText("");
                notesArea.setText("");
                caseCombo.setSelectedIndex(0);
                categoryCombo.setSelectedIndex(0);
                typeCombo.setSelectedIndex(0);
                modeCombo.setSelectedIndex(0);

                // Refresh payments page
                showPage("payments");

            } else {
                JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating bill: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentRequests(DefaultTableModel model) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection con = DBConnection.getConnection();
                    if (con == null) {
                        SwingUtilities.invokeLater(() -> {
                            model.addRow(new Object[]{6001, 1001, "Arun Patel", 25000.00, "TRAVEL", "FEE", "PENDING", "BILL001", "2024-12-15", "No", null, null});
                            model.addRow(new Object[]{6002, 1001, "Arun Patel", 5000.00, "HEARING", "TRAVEL", "PENDING", "BILL002", "2024-12-20", "No", null, null});
                        });
                        return null;
                    }
                    String sql = "SELECT p.pay_id, p.case_id, cl.c_name, p.amount, p.pay_category, p.pay_type, p.payment_status, p.bill_no, TO_CHAR(p.pay_date,'DD-MON-YYYY'), p.receipt_path, p.receipt_type " +
                                 "FROM PAYMENT1 p JOIN CASES1 cs ON p.case_id = cs.case_id " +
                                 "JOIN CLIENT1 cl ON cs.c_id = cl.c_id " +
                                 "WHERE p.handled_by = ? ORDER BY p.pay_date DESC";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, advId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String receiptPath = rs.getString(10);
                        String receiptType = rs.getString(11);
                        boolean hasProof = receiptPath != null && !receiptPath.trim().isEmpty();
                        boolean verified = "IMAGE_VERIFIED".equals(receiptType);
                        String status = rs.getString(7);
                        String displayStatus = status;
                        if (verified) {
                            displayStatus = "COMPLETED";
                        } else if ("PAID".equals(status) && hasProof) {
                            displayStatus = "PAID (Awaiting verify)";
                        }
                        final int payId = rs.getInt(1);
                        final int caseId = rs.getInt(2);
                        final String clientName = rs.getString(3);
                        final double amount = rs.getDouble(4);
                        final String payCategory = rs.getString(5);
                        final String payType = rs.getString(6);
                        final String billNo = rs.getString(8);
                        final String date = rs.getString(9);
                        final String finalReceiptPath = receiptPath;
                        final String finalReceiptType = receiptType;
                        final boolean finalHasProof = hasProof;
                        final String finalStatus = displayStatus;
                        SwingUtilities.invokeLater(() -> model.addRow(new Object[] {
                            payId, caseId, clientName, amount,
                            payCategory, payType, finalStatus, billNo, date,
                            finalHasProof ? "Yes" : "No", finalReceiptPath, finalReceiptType
                        }));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        model.addRow(new Object[]{6001, 1001, "Arun Patel", 25000.00, "TRAVEL", "FEE", "PENDING", "BILL001", "2024-12-15", "No", null, null});
                        model.addRow(new Object[]{6002, 1001, "Arun Patel", 5000.00, "HEARING", "TRAVEL", "PENDING", "BILL002", "2024-12-20", "No", null, null});
                    });
                }
                return null;
            }
        };
        w.execute();
    }

    private void refreshPaymentsPage() {
        if (paymentRequestModel != null) {
            paymentRequestModel.setRowCount(0);
            loadPaymentRequests(paymentRequestModel);
        }
    }

    private void updatePaymentActionControls(JTable table, DefaultTableModel model, RoundedButton viewProofBtn, RoundedButton verifyBtn, JLabel actionInfo) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            viewProofBtn.setEnabled(false);
            verifyBtn.setEnabled(false);
            actionInfo.setText("Select a payment to review proof and verify completion.");
            return;
        }
        int modelIndex = table.convertRowIndexToModel(selected);
        String proof = (String) model.getValueAt(modelIndex, 9);
        String receiptType = (String) model.getValueAt(modelIndex, 11);
        String status = (String) model.getValueAt(modelIndex, 6);
        boolean hasProof = "Yes".equals(proof);
        boolean alreadyVerified = "IMAGE_VERIFIED".equals(receiptType);
        viewProofBtn.setEnabled(hasProof);
        verifyBtn.setEnabled(hasProof && !alreadyVerified && status != null && status.startsWith("PAID"));
        if (!hasProof) {
            actionInfo.setText("No proof image attached yet for this payment.");
        } else if (alreadyVerified) {
            actionInfo.setText("This payment has already been verified and completed.");
        } else {
            actionInfo.setText("Proof is attached. Verify the payment to complete the request.");
        }
    }

    private void viewPaymentProof(String receiptPath) {
        if (receiptPath == null || receiptPath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No payment proof file is available for this request.", "No Proof", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        File proofFile = new File(receiptPath);
        if (!proofFile.exists()) {
            JOptionPane.showMessageDialog(this, "The stored payment proof file cannot be found: " + receiptPath, "File Missing", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(proofFile);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot open proof image on this system.", "Unsupported", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening proof image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verifyPayment(int payId) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                c.setAutoCommit(false);
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE PAYMENT1 SET receipt_type = 'IMAGE_VERIFIED', notes = CASE WHEN notes IS NULL THEN '[VERIFIED]' ELSE notes || ' [VERIFIED]' END WHERE pay_id = ?");
                ps.setInt(1, payId);
                ps.executeUpdate();
                c.commit();
            }
            JOptionPane.showMessageDialog(this, "Payment receipt verified successfully. Status is now marked as completed.", "Verified", JOptionPane.INFORMATION_MESSAGE);
            refreshPaymentsPage();
            showPage("payments");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error verifying payment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
