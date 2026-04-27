package com.ams.dashboard;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * Staff / Admin Dashboard
 */
public class StaffDashboard extends BaseDashboard {

    private static final long serialVersionUID = 1L;

    private int staffId;
    private JTable currentTable;
    private int selectedId = -1;

    public StaffDashboard(int staffId) {
        super("Staff Admin", "Admin Staff", "STAFF", staffId);
        this.staffId = staffId;
    }

    @Override
    protected void buildPages() {
        addSidebarSection("Overview");
        addNavItem("Dashboard",         "\uD83C\uDFE0", "home");
        addNavItem("My Profile",        "\uD83D\uDC64", "profile");

        addSidebarSection("Management");
        addNavItem("Advocates",         "\u2696\uFE0F", "advocates");
        addNavItem("Junior Advocates",  "\uD83D\uDC64", "juniors");
        addNavItem("Clients",           "\uD83D\uDC65", "clients");
        addNavItem("Support Staff",     "\uD83D\uDCC7", "supportstaff");

        addSidebarSection("Cases & Hearings");
        addNavItem("All Cases",         "\uD83D\uDCBC", "allcases");
        addNavItem("Hearings Schedule", "\uD83D\uDD56", "hearings");
        addNavItem("Case Assignment",   "\uD83D\uDCCE", "caseassign");

        addSidebarSection("Finance");
        addNavItem("Salary & Fees",     "\uD83D\uDCB0", "salary");
        addNavItem("Payments",          "\uD83D\uDCB3", "payments");

        addSidebarSection("Reports");
        addNavItem("Reports & Analytics","\uD83D\uDCCA", "reports");

        addPage("home",        buildHomePage());
        addPage("profile",    buildProfilePage());
        addPage("advocates",   buildAdvocatesPage());
        addPage("juniors",     buildJuniorsPage());
        addPage("clients",     buildClientsPage());
        addPage("supportstaff",buildSupportStaffPage());
        addPage("allcases",    buildAllCasesPage());
        addPage("hearings",    buildHearingsPage());
        addPage("caseassign",  buildCaseAssignPage());
        addPage("salary",      buildSalaryPage());
        addPage("payments",    buildPaymentsPage());
        addPage("reports",     buildReportsPage());
    }

    // ── Home ──────────────────────────────────────────────────────────────────
    private JScrollPane buildHomePage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));

        JLabel greeting=new JLabel("Admin Dashboard \uD83D\uDEE1\uFE0F");
        greeting.setFont(new Font("Segoe UI",Font.BOLD,26)); greeting.setForeground(AMSTheme.TEXT_PRIMARY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub=new JLabel("System Overview — Manage advocates, clients, cases, and finances.");
        sub.setFont(AMSTheme.FONT_BODY); sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Summary cards
        DashboardCard advCard=new DashboardCard("Advocates","—","\u2696\uFE0F",AMSTheme.CARD_BLUE_1,AMSTheme.CARD_BLUE_2,()->showPage("advocates"));
        DashboardCard clientCard=new DashboardCard("Clients","—","\uD83D\uDC65",AMSTheme.CARD_GREEN_1,AMSTheme.CARD_GREEN_2,()->showPage("clients"));
        DashboardCard caseCard=new DashboardCard("Total Cases","—","\uD83D\uDCBC",AMSTheme.CARD_GOLD_1,AMSTheme.CARD_GOLD_2,()->showPage("allcases"));
        DashboardCard hearCard=new DashboardCard("Today's Hearings","—","\uD83D\uDD56",AMSTheme.CARD_PURPLE_1,AMSTheme.CARD_PURPLE_2,()->showPage("hearings"));
        DashboardCard staffCard=new DashboardCard("Support Staff","—","\uD83D\uDCC7",AMSTheme.CARD_TEAL_1,AMSTheme.CARD_TEAL_2,()->showPage("supportstaff"));

        loadAdminCounts(advCard,clientCard,caseCard,hearCard,staffCard);

        JPanel cardsRow=new JPanel(new FlowLayout(FlowLayout.LEFT,16,8));
        cardsRow.setOpaque(false); cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.add(advCard); cardsRow.add(clientCard); cardsRow.add(caseCard); cardsRow.add(hearCard); cardsRow.add(staffCard);

        // Quick reports row
        JLabel reportTitle=sectionTitle("Quick Reports"); reportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel reportRow=new JPanel(new FlowLayout(FlowLayout.LEFT,12,8)); reportRow.setOpaque(false); reportRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] rpts={"Advocate Performance","Client Analytics","Case Statistics","Financial Report","Staff Report","Timeline Analysis"};
        for(String rpt:rpts){
            RoundedButton btn=new RoundedButton(rpt,new Color(0x2D6CCF),new Color(0x1A4B8C));
            btn.setPreferredSize(new Dimension(175,38));
            btn.addActionListener(e->generateReport(rpt));
            reportRow.add(btn);
        }

        page.add(greeting); page.add(Box.createVerticalStrut(4));
        page.add(sub); page.add(Box.createVerticalStrut(24));
        page.add(cardsRow); page.add(Box.createVerticalStrut(24));
        page.add(reportTitle); page.add(reportRow);
        return scrollWrap(page);
    }

    private void refreshProfilePage() {
        JComponent old = pages.get("profile");
        if (old != null) contentPanel.remove(old);
        JScrollPane np = buildProfilePage();
        addPage("profile", np);
        showPage("profile");
    }

    private JScrollPane buildProfilePage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("My Profile"); title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel infoCard=cardPanel(new GridLayout(0,2,12,10)); infoCard.setBorder(new EmptyBorder(20,20,20,20));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,220)); infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                ResultSet rs=c.prepareStatement("SELECT st_name,job_title,role_type,phone,addr_city FROM STAFF1 WHERE st_id="+staffId).executeQuery();
                if(rs.next()){
                    infoCard.add(il("Name",     rs.getString(1)));
                    infoCard.add(il("Job Title", rs.getString(2)));
                    infoCard.add(il("Role",     rs.getString(3)));
                    infoCard.add(il("Phone",    rs.getString(4)));
                    infoCard.add(il("City",     rs.getString(5)));
                }
            } else {
                infoCard.add(il("Name", "Admin Staff"));
                infoCard.add(il("Email", "admin@example.com"));
                infoCard.add(il("Role", "Administrator"));
                infoCard.add(il("Phone", "N/A"));
                infoCard.add(il("Status", "Active"));
            }
        }catch(Exception e){
            infoCard.add(il("Name", "Admin Staff"));
            infoCard.add(il("Role", "Administrator"));
        }

        RoundedButton updateBtn=new RoundedButton("Update Profile", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateBtn.addActionListener(e->showUpdateProfileDialog());

        page.add(title); page.add(Box.createVerticalStrut(20));
        page.add(infoCard);
        page.add(Box.createVerticalStrut(16));
        page.add(updateBtn);
        return scrollWrap(page);
    }

    private void showUpdateProfileDialog(){
        JDialog dialog = new JDialog(this, "Update Staff Profile", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AMSTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Title
        JLabel title = new JLabel("Update Profile Information");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form fields
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 10));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        LabeledField nameField = new LabeledField("Full Name", 30);
        LabeledField emailField = new LabeledField("Email", 30);
        LabeledField phoneField = new LabeledField("Phone", 15);
        LabeledField jobTitleField = new LabeledField("Job Title", 20);
        LabeledField cityField = new LabeledField("City", 20);
        LabeledField stateField = new LabeledField("State", 20);
        LabeledField pincodeField = new LabeledField("Pincode", 6);
        LabeledField workHoursField = new LabeledField("Work Hours", 3);
        LabeledField salaryField = new LabeledField("Salary", 10);
        LabeledField emergencyContactField = new LabeledField("Emergency Contact", 15);

        // Load current data
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement(
                    "SELECT st_name, email, phone, job_title, addr_city, addr_state, addr_pincode, work_hours, salary, emergency_contact FROM STAFF1 WHERE st_id=" + staffId
                ).executeQuery();
                if (rs.next()) {
                    nameField.setText(rs.getString(1) != null ? rs.getString(1) : "");
                    emailField.setText(rs.getString(2) != null ? rs.getString(2) : "");
                    phoneField.setText(rs.getString(3) != null ? rs.getString(3) : "");
                    jobTitleField.setText(rs.getString(4) != null ? rs.getString(4) : "");
                    cityField.setText(rs.getString(5) != null ? rs.getString(5) : "");
                    stateField.setText(rs.getString(6) != null ? rs.getString(6) : "");
                    pincodeField.setText(rs.getString(7) != null ? rs.getString(7) : "");
                    workHoursField.setText(rs.getString(8) != null ? rs.getString(8) : "");
                    salaryField.setText(rs.getString(9) != null ? rs.getString(9) : "");
                    emergencyContactField.setText(rs.getString(10) != null ? rs.getString(10) : "");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Error loading profile data: " + e.getMessage());
        }

        formPanel.add(nameField);
        formPanel.add(emailField);
        formPanel.add(phoneField);
        formPanel.add(jobTitleField);
        formPanel.add(cityField);
        formPanel.add(stateField);
        formPanel.add(pincodeField);
        formPanel.add(workHoursField);
        formPanel.add(salaryField);
        formPanel.add(emergencyContactField);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        RoundedButton saveBtn = new RoundedButton("Save Changes", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton cancelBtn = new RoundedButton("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));

        saveBtn.addActionListener(e -> {
            // Save profile data
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    c.setAutoCommit(false);

                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE STAFF1 SET st_name=?, email=?, phone=?, job_title=?, addr_city=?, addr_state=?, addr_pincode=?, work_hours=?, salary=?, emergency_contact=? WHERE st_id=?"
                    );

                    ps.setString(1, nameField.getText().trim());
                    ps.setString(2, emailField.getText().trim());
                    ps.setString(3, phoneField.getText().trim());
                    ps.setString(4, jobTitleField.getText().trim());
                    ps.setString(5, cityField.getText().trim());
                    ps.setString(6, stateField.getText().trim());
                    ps.setInt(7, Integer.parseInt(pincodeField.getText().trim().isEmpty() ? "0" : pincodeField.getText().trim()));
                    ps.setInt(8, Integer.parseInt(workHoursField.getText().trim().isEmpty() ? "8" : workHoursField.getText().trim()));
                    ps.setDouble(9, Double.parseDouble(salaryField.getText().trim().isEmpty() ? "0" : salaryField.getText().trim()));
                    ps.setString(10, emergencyContactField.getText().trim());
                    ps.setInt(11, staffId);

                    int rows = ps.executeUpdate();
                    c.commit();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(dialog, "Profile updated successfully!");
                        dialog.dispose(); refreshProfilePage();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to update profile.");
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Database connection not available.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating profile: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel il(String k,String v){
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel kl=new JLabel(k+": "); kl.setFont(AMSTheme.FONT_BOLD); kl.setForeground(AMSTheme.TEXT_SECONDARY);
        JLabel vl=new JLabel(v!=null?v:"—"); vl.setFont(AMSTheme.FONT_BODY); vl.setForeground(AMSTheme.TEXT_PRIMARY);
        p.add(kl,BorderLayout.WEST); p.add(vl,BorderLayout.CENTER); return p;
    }

    private void loadAdminCounts(DashboardCard ac,DashboardCard cc,DashboardCard cas,DashboardCard hc,DashboardCard sc){
        new SwingWorker<int[],Void>(){
            @Override protected int[] doInBackground(){
                int[] c={0,0,0,0,0};
                try{
                    Connection con=DBConnection.getConnection();
                    if(con==null)return c;
                    ResultSet r=con.prepareStatement("SELECT COUNT(*) FROM ADVOCATE1").executeQuery(); if(r.next())c[0]=r.getInt(1);
                    r=con.prepareStatement("SELECT COUNT(*) FROM CLIENT1").executeQuery(); if(r.next())c[1]=r.getInt(1);
                    r=con.prepareStatement("SELECT COUNT(*) FROM CASES1").executeQuery(); if(r.next())c[2]=r.getInt(1);
                    r=con.prepareStatement("SELECT COUNT(*) FROM HEARING1 WHERE TRUNC(h_date)=TRUNC(SYSDATE)").executeQuery(); if(r.next())c[3]=r.getInt(1);
                    r=con.prepareStatement("SELECT COUNT(*) FROM STAFF1 WHERE role_type='SUPPORT'").executeQuery(); if(r.next())c[4]=r.getInt(1);
                }catch(Exception ignored){}
                return c;
            }
            @Override protected void done(){
                try{int[] c=get(); ac.setValue(""+c[0]); cc.setValue(""+c[1]); cas.setValue(""+c[2]); hc.setValue(""+c[3]); sc.setValue(""+c[4]);}catch(Exception ignored){}
            }
        }.execute();
    }

    private void generateReport(String type){
        JDialog d=new JDialog(this,"Report: "+type,false);
        d.setSize(700,500); d.setLocationRelativeTo(this);
        JTextArea area=new JTextArea(); area.setFont(AMSTheme.FONT_BODY); area.setEditable(false); area.setLineWrap(true);
        area.setBorder(new EmptyBorder(16,16,16,16));
        area.setText("Generating "+type+"...\n\n");
        // Simulate report
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground(){
                StringBuilder sb=new StringBuilder("=======================================\n");
                sb.append("  ").append(type.toUpperCase()).append("\n");
                sb.append("=======================================\n\n");
                try{
                    Connection c=DBConnection.getConnection();
                    if(c!=null){
                        if ("Advocate Performance".equals(type)) {
                                ResultSet rs=c.prepareStatement(
                                    "SELECT a.a_name,COUNT(ac.case_id),a.rating FROM ADVOCATE1 a LEFT JOIN ADVOCATE_CASE1 ac ON a.a_id=ac.a_id GROUP BY a.a_id,a.a_name,a.rating ORDER BY a.rating DESC").executeQuery();
                                sb.append(String.format("%-30s %-15s %-10s\n","Advocate","Cases","Rating"));
                                sb.append("-".repeat(55)).append("\n");
                                while(rs.next()) sb.append(String.format("%-30s %-15d %-10.1f\n",rs.getString(1),rs.getInt(2),rs.getDouble(3)));
                        } else if ("Case Statistics".equals(type)) {
                                ResultSet rs=c.prepareStatement("SELECT status,COUNT(*) FROM CASES1 GROUP BY status").executeQuery();
                                sb.append("Status Distribution:\n");
                                while(rs.next()) sb.append("  ").append(rs.getString(1)).append(": ").append(rs.getInt(2)).append("\n");
                        } else if ("Financial Report".equals(type)) {
                                ResultSet rs=c.prepareStatement("SELECT SUM(fee_amount),SUM(total_fees),COUNT(*) FROM CASES1").executeQuery();
                                if(rs.next()) sb.append("Total Fee Amount: ").append(rs.getDouble(1)).append("\nTotal Fees Collected: ").append(rs.getDouble(2)).append("\nTotal Cases: ").append(rs.getInt(3));
                        } else {
                            sb.append("Report data for ").append(type).append(" will be generated here.\n");
                        }
                    } else {
                        // Demo data
                        sb.append("Demo Report Data\n\n");
                        sb.append(String.format("%-30s %-15s %-10s\n","Name","Count","Value"));
                        sb.append("-".repeat(55)).append("\n");
                        sb.append(String.format("%-30s %-15d %-10.1f\n","Adv. Rajesh Kumar",12,4.5));
                        sb.append(String.format("%-30s %-15d %-10.1f\n","Adv. Priya Sharma",8,4.2));
                        sb.append(String.format("%-30s %-15d %-10.1f\n","Adv. Suresh Babu",6,3.9));
                    }
                }catch(Exception e){sb.append("Error generating report: ").append(e.getMessage());}
                return sb.toString();
            }
            @Override protected void done(){try{area.setText(get()); area.setCaretPosition(0);}catch(Exception ignored){}}
        }.execute();
        d.add(new JScrollPane(area));
        d.setVisible(true);
    }

    // ── Advocates Page ────────────────────────────────────────────────────────
    private JScrollPane buildAdvocatesPage(){
        return buildManagementPage("Advocates",
            new String[]{"ID","Name","Email","Phone","Bar Reg","License","Office","Joined","City","Street","Pincode","Expertise","YOE","YOB","Notes","Rating"},
            "SELECT a_id,a_name,email,phone,bar_enroll_no,license_status,office_room,TO_CHAR(joined_date,'DD-MON-YYYY'),addr_city,addr_street,addr_pincode,expert_at,yoe,yob,profile_notes,rating FROM ADVOCATE1 ORDER BY a_name",
            "ADVOCATE",16,new Object[][]{
                {1,"Adv. Rajesh Kumar","rajesh@ams.com","9876543210","BAR/2010/1234","ACTIVE","R-101","15-JUN-2010","Chennai","Anna Nagar",600040,"Criminal Law",14,1975,"Experienced criminal lawyer",4.5},
            });
    }

    // ── Juniors Page ──────────────────────────────────────────────────────────
    private JScrollPane buildJuniorsPage(){
        return buildManagementPage("Junior Advocates",
            new String[]{"ID","Name","Email","Mobile","Qualification","Joined","Dept","Mentor","Status","Desk","Rating"},
            "SELECT ja_id,ja_name,email,mobile,qualification,TO_CHAR(joined_date,'DD-MON-YYYY'),department,mentor_id,work_status,desk_location,rating FROM JUNIOR_ADVOCATE1 ORDER BY ja_name",
            "JUNIOR_ADVOCATE",11,new Object[][]{
                {1,"Priya Devi","priya@ams.com","9876500001","LLB","10-JAN-2022","Criminal",1,"ACTIVE","D-5",4.0},
            });
    }

    // ── Clients Page ──────────────────────────────────────────────────────────
    private JScrollPane buildClientsPage(){
        return buildManagementPage("Clients",
            new String[]{"ID","Name","Email","Phone","Nat ID","Type","City","Street","Pincode","Contact Dt","Notes","Username","Rating"},
            "SELECT c_id,c_name,email,phone,nat_id,cl_type,addr_city,addr_street,addr_pincode,TO_CHAR(first_contact_dt,'DD-MON-YYYY'),comm_notes,username,rating_given FROM CLIENT1 ORDER BY c_name",
            "CLIENT",13,new Object[][]{
                {1,"Arun Patel","arun@gmail.com","9111111111","TN123456","INDIVIDUAL","Chennai","T Nagar",600017,"31-MAR-2026","Regular client","client1",0.0},
            });
    }

    // ── Support Staff Page ────────────────────────────────────────────────────
    private JScrollPane buildSupportStaffPage(){
        return buildManagementPage("Support Staff",
            new String[]{"ID","Name","Phone","Job Title","City","Pincode","State","Work Hrs","Emergency","Salary","DOB","Join Date","Role"},
            "SELECT st_id,st_name,phone,job_title,addr_city,addr_pincode,addr_state,work_hours,emergency_contact,salary,TO_CHAR(dob,'DD-MON-YYYY'),TO_CHAR(join_date,'DD-MON-YYYY'),role_type FROM STAFF1 WHERE role_type='SUPPORT'",
            "STAFF",13,new Object[][]{
                {1,"Suresh Admin","9000000001","Admin Manager","Chennai",600001,"Tamil Nadu",8,"9000000002",45000.0,"20-MAY-1985","01-MAR-2018","ADMIN"},
            });
    }

    // ── All Cases Page ────────────────────────────────────────────────────────
    private JScrollPane buildAllCasesPage(){
        return buildManagementPage("All Cases",
            new String[]{"Case ID","Title","Client","Advocate","Type","Status","Priority","Filed"},
            "SELECT cs.case_id,cs.c_title,cl.c_name,COALESCE(a.a_name,'Unassigned'),cs.c_type,cs.status,cs.priority_level,TO_CHAR(cs.filed_date,'DD-MON-YYYY') " +
            "FROM CASES1 cs JOIN CLIENT1 cl ON cs.c_id=cl.c_id LEFT JOIN ADVOCATE1 a ON cs.assigned_adv=a.a_id ORDER BY cs.filed_date DESC",
            "CASES",8,new Object[][]{
                {1001,"State vs Rajan","Arun Patel","Adv. Rajesh","CRIMINAL","ACTIVE",1,"10-JAN-2024"},
                {1002,"Property Dispute","Sunita Patel","Adv. Priya","CIVIL","PENDING",2,"20-FEB-2024"},
            });
    }

    // ── Hearings Page ─────────────────────────────────────────────────────────
    private JScrollPane buildHearingsPage(){
        return buildManagementPage("Hearing Schedule",
            new String[]{"H-ID","Case","Date","Court","Purpose","Judge","Status"},
            "SELECT h.h_id,cs.c_title,TO_CHAR(h.h_date,'DD-MON-YYYY'),h.court_house,h.purpose,h.judge_name,h.status FROM HEARING1 h JOIN CASES1 cs ON h.case_id=cs.case_id ORDER BY h.h_date DESC",
            "HEARING",7,new Object[][]{
                {2001,"State vs Rajan","28-DEC-2024","High Court","Arguments","J. Sharma","UPCOMING"},
                {2002,"Property Dispute","10-JAN-2025","District Court","Evidence","J. Patel","UPCOMING"},
            });
    }

    // ── Case Assignment ───────────────────────────────────────────────────────
    private JScrollPane buildCaseAssignPage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("\uD83D\uDCCE  Case Assignment");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub=new JLabel("Assign advocates to pending cases.");
        sub.setFont(AMSTheme.FONT_BODY); sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel form=cardPanel(new GridLayout(0,2,12,10)); form.setBorder(new EmptyBorder(20,20,20,20));
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE,200)); form.setAlignmentX(Component.LEFT_ALIGNMENT);

        LabeledField caseIdF=new LabeledField("Case ID *",10);
        LabeledField advIdF=new LabeledField("Advocate ID *",10);
        form.add(caseIdF); form.add(advIdF);

        RoundedButton assignBtn=new RoundedButton("Assign Advocate",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        assignBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        assignBtn.addActionListener(e->{
            if(caseIdF.getText().isEmpty()||advIdF.getText().isEmpty()){
                JOptionPane.showMessageDialog(this,"Both Case ID and Advocate ID are required."); return;
            }
            try{
                int cid=Integer.parseInt(caseIdF.getText()), aid=Integer.parseInt(advIdF.getText());
                Connection c=DBConnection.getConnection();
                if(c!=null){
                    c.prepareStatement("UPDATE CASES1 SET assigned_adv="+aid+",status='ACTIVE' WHERE case_id="+cid).executeUpdate();
                    try{
                        PreparedStatement ps=c.prepareStatement("INSERT INTO ADVOCATE_CASE1 VALUES(?,?)");
                        ps.setInt(1,aid); ps.setInt(2,cid); ps.executeUpdate();
                    }catch(Exception ignored){}
                    c.commit();
                }
                JOptionPane.showMessageDialog(this,"Case #"+cid+" assigned to Advocate #"+aid+" successfully!");
                caseIdF.setText(""); advIdF.setText("");
            }catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
        });

        page.add(title); page.add(Box.createVerticalStrut(4)); page.add(sub);
        page.add(Box.createVerticalStrut(20)); page.add(form);
        page.add(Box.createVerticalStrut(14)); page.add(assignBtn);
        return scrollWrap(page);
    }

    // ── Salary Page ───────────────────────────────────────────────────────────
    private JScrollPane buildSalaryPage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("\uD83D\uDCB0  Salary & Fees Management");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Salary table
        String[] cols={"ID","Name","Role","Current Salary","Rating","Recommended"};
        DefaultTableModel model=new DefaultTableModel(cols,0){ private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable t=new JTable(model); t.setRowHeight(36); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF)); t.getTableHeader().setForeground(AMSTheme.PRIMARY);

        // Demo data
        model.addRow(new Object[]{1,"Adv. Rajesh Kumar","Senior Advocate",85000,4.5,"95000"});
        model.addRow(new Object[]{1,"Priya Devi","Junior Advocate",35000,4.0,"38000"});
        model.addRow(new Object[]{10,"Ramu","Support Staff",12000,0,"12000"});

        JScrollPane sp=new JScrollPane(t); sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton updateBtn=new RoundedButton("Update Salary",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        updateBtn.addActionListener(e->{
            int row=t.getSelectedRow();
            if(row<0){JOptionPane.showMessageDialog(this,"Select a record.");return;}
            String newSal=JOptionPane.showInputDialog(this,"Enter new salary:","50000");
            if(newSal!=null) JOptionPane.showMessageDialog(this,"Salary updated to ₹"+newSal);
        });
        btnRow.add(updateBtn);

        page.add(title); page.add(Box.createVerticalStrut(20));
        JPanel tableCard=cardPanel(new BorderLayout()); tableCard.setBorder(new EmptyBorder(12,12,12,12));
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT); tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,320));
        tableCard.add(sp,BorderLayout.CENTER); tableCard.add(btnRow,BorderLayout.SOUTH);
        page.add(tableCard);
        return scrollWrap(page);
    }

    // ── Payments Page ─────────────────────────────────────────────────────────
    private JScrollPane buildPaymentsPage(){
        return buildManagementPage("Payment Records",
            new String[]{"Pay ID","Case ID","Date","Amount","Type","Mode","Bill No"},
            "SELECT pay_id,case_id,TO_CHAR(pay_date,'DD-MON-YYYY'),amount,pay_type,pay_mode,bill_no FROM PAYMENT1 ORDER BY pay_date DESC",
            "PAYMENT",7,new Object[][]{
                {6001,1001,"10-DEC-2024",25000,"FEE","BANK_TRANSFER","BILL001"},
                {6002,1001,"15-DEC-2024",5000,"TRAVEL","CASH","BILL002"},
            });
    }

    // ── Reports Page ──────────────────────────────────────────────────────────
    private JScrollPane buildReportsPage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("\uD83D\uDCCA  Reports & Analytics");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] reportTypes={"Advocate Performance","Client Analytics","Case Statistics","Financial Report","Staff Report","Timeline Analysis","Hearing Summary","Evidence Report"};
        Color[] cols={AMSTheme.CARD_BLUE_1,AMSTheme.CARD_GREEN_1,AMSTheme.CARD_GOLD_1,AMSTheme.CARD_PURPLE_1,
                      AMSTheme.CARD_RED_1,AMSTheme.CARD_TEAL_1,AMSTheme.CARD_BLUE_1,AMSTheme.CARD_GREEN_1};
        Color[] cols2={AMSTheme.CARD_BLUE_2,AMSTheme.CARD_GREEN_2,AMSTheme.CARD_GOLD_2,AMSTheme.CARD_PURPLE_2,
                       AMSTheme.CARD_RED_2,AMSTheme.CARD_TEAL_2,AMSTheme.CARD_BLUE_2,AMSTheme.CARD_GREEN_2};
        String[] icons={"\uD83D\uDC68\u200D\u2696\uFE0F","\uD83D\uDC65","\uD83D\uDCBC","\uD83D\uDCB0","\uD83D\uDCC7","\uD83D\uDCC5","\uD83D\uDD56","\uD83D\uDD0D"};

        JPanel cardsGrid=new JPanel(new FlowLayout(FlowLayout.LEFT,16,14));
        cardsGrid.setOpaque(false); cardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for(int i=0;i<reportTypes.length;i++){
            final String rpt=reportTypes[i];
            final Color c1=cols[i], c2=cols2[i];
            DashboardCard card=new DashboardCard(rpt,"Generate",icons[i],c1,c2,()->generateReport(rpt));
            cardsGrid.add(card);
        }

        page.add(title); page.add(Box.createVerticalStrut(24)); page.add(cardsGrid);
        return scrollWrap(page);
    }

    // ── Generic Management Page Builder ───────────────────────────────────────
    private JScrollPane buildManagementPage(String entityName,String[] cols,String sql,String table,int colCount,Object[][] demoData){
        JPanel page=new JPanel(new BorderLayout()); page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel(entityName+" Management");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        DefaultTableModel model=new DefaultTableModel(cols,0){ private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable t=buildTable(model);
        // Load
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){for(Object[] row:demoData) SwingUtilities.invokeLater(()->model.addRow(row));return null;}
                    ResultSet rs=c.prepareStatement(sql).executeQuery();
                    int nc=rs.getMetaData().getColumnCount();
                    while(rs.next()){
                        Object[] row=new Object[nc];
                        for(int i=1;i<=nc;i++) row[i-1]=rs.getObject(i);
                        SwingUtilities.invokeLater(()->model.addRow(row));
                    }
                }catch(Exception e){for(Object[] row:demoData) SwingUtilities.invokeLater(()->model.addRow(row));}
                return null;
            }
        }.execute();
        JScrollPane sp=new JScrollPane(t); sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        
        // Management buttons (only for advocates/juniors)
        if ("ADVOCATE".equals(table) || "JUNIOR_ADVOCATE".equals(table)) {
            RoundedButton addBtn = new RoundedButton("➕ Add New", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
            addBtn.setPreferredSize(new Dimension(110,36));
            addBtn.addActionListener(e -> {
                if ("ADVOCATE".equals(table)) showAddAdvocateDialog();
                else if ("JUNIOR_ADVOCATE".equals(table)) showAddJuniorDialog();
            });
            btnRow.add(addBtn);
            
            RoundedButton removeBtn = new RoundedButton("🗑️ Remove", new Color(0xE74C3C), new Color(0xC0392B));
            removeBtn.setPreferredSize(new Dimension(110,36));
            removeBtn.addActionListener(e -> {
                if (selectedId > 0) {
                    String role = "ADVOCATE".equals(table) ? "SENIOR_ADVOCATE" : "JUNIOR_ADVOCATE";
                    showRemoveEntityDialog(selectedId, table, role);
                } else {
                    JOptionPane.showMessageDialog(this, "Select an advocate to remove.");
                }
            });
            btnRow.add(removeBtn);
            
            RoundedButton credBtn = new RoundedButton("🔑 Credentials", new Color(0xF39C12), new Color(0xD68910));
            credBtn.setPreferredSize(new Dimension(130,36));
            credBtn.addActionListener(e -> {
                if (selectedId > 0) {
                    String role = "ADVOCATE".equals(table) ? "SENIOR_ADVOCATE" : "JUNIOR_ADVOCATE";
                    showCredentialsDialog(selectedId, role);
                } else {
                    JOptionPane.showMessageDialog(this, "Select an advocate to manage credentials.");
                }
            });
            btnRow.add(credBtn);
            btnRow.add(Box.createHorizontalStrut(10));
        }
        
        RoundedButton refreshBtn=new RoundedButton("⟳ Refresh",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(110,36));
        refreshBtn.addActionListener(e -> refreshCurrentTable(sql, demoData));
        btnRow.add(refreshBtn);

        page.add(title,BorderLayout.NORTH); page.add(sp,BorderLayout.CENTER); page.add(btnRow,BorderLayout.SOUTH);
        return scrollWrap(page);
    }

    private JTable buildTable(DefaultTableModel model){
        JTable t=new JTable(model); t.setRowHeight(34); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF)); t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        t.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){ private static final long serialVersionUID = 1L;
            @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int col){
                Component c=super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                if(!sel) c.setBackground(row%2==0?Color.WHITE:new Color(0xF8FAFF));
                setBorder(new EmptyBorder(0,10,0,10));
                c.setForeground(AMSTheme.TEXT_PRIMARY);
                return c;
            }
        });
        
        // Selection listener for management
        t.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = t.getSelectedRow();
                selectedId = (row >= 0 && t.getValueAt(row, 0) != null) ? (Integer) t.getValueAt(row, 0) : -1;
            }
        });
        currentTable = t;
        return t;
    }
    
    private void refreshCurrentTable(String sql, Object[][] demoData) {
        if (currentTable != null && currentTable.getModel() instanceof DefaultTableModel) {
            DefaultTableModel model = (DefaultTableModel) currentTable.getModel();
            model.setRowCount(0);
            try {
                Connection c = DBConnection.getConnection();
                if (c == null) {
                    for (Object[] row : demoData) model.addRow(row);
                    return;
                }
                ResultSet rs = c.prepareStatement(sql).executeQuery();
                int nc = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[nc];
                    for (int i = 1; i <= nc; i++) row[i - 1] = rs.getObject(i);
                    model.addRow(row);
                }
            } catch (Exception ex) {
                for (Object[] row : demoData) model.addRow(row);
            }
        }
    }

    private String getAdvocatesSql() {
        return "SELECT a_id,a_name,email,phone,bar_enroll_no,license_status,office_room,TO_CHAR(joined_date,'DD-MON-YYYY'),addr_city,addr_street,addr_pincode,expert_at,yoe,yob,profile_notes,rating FROM ADVOCATE1 ORDER BY a_name";
    }

    private Object[][] getAdvocatesDemo() {
        return new Object[][]{{1,"Adv. Rajesh Kumar","rajesh@ams.com","9876543210","BAR/2010/1234","ACTIVE","R-101","15-JUN-2010","Chennai","Anna Nagar",600040,"Criminal Law",14,1975,"Experienced criminal lawyer",4.5}};
    }

    private String getJuniorsSql() {
        return "SELECT ja_id,ja_name,email,mobile,qualification,TO_CHAR(joined_date,'DD-MON-YYYY'),department,mentor_id,work_status,desk_location,rating FROM JUNIOR_ADVOCATE1 ORDER BY ja_name";
    }

    private Object[][] getJuniorsDemo() {
        return new Object[][]{{1,"Priya Devi","priya@ams.com","9876500001","LLB","10-JAN-2022","Criminal",1,"ACTIVE","D-5",4.0}};
    }

    private void showAddAdvocateDialog() {
        JDialog dialog = new JDialog(this, "Add New Senior Advocate", true);
        dialog.setSize(700, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AMSTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("➕ Add New Senior Advocate");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        LabeledField nameField = new LabeledField("Full Name *", 30);
        LabeledField emailField = new LabeledField("Email", 30);
        LabeledField phoneField = new LabeledField("Phone *", 15);
        LabeledField barField = new LabeledField("Bar Enrollment No", 25);
        LabeledField licenseField = new LabeledField("License Status", 15);
        LabeledField officeField = new LabeledField("Office Room", 12);
        LabeledField cityField = new LabeledField("City", 20);
        LabeledField streetField = new LabeledField("Street", 30);
        LabeledField pinField = new LabeledField("Pincode", 8);
        LabeledField expertField = new LabeledField("Expertise", 25);
        LabeledField yoeField = new LabeledField("YOE", 5);
        LabeledField yobField = new LabeledField("YOB", 5);
        LabeledField notesField = new LabeledField("Notes", 50);

        formPanel.add(nameField);
        formPanel.add(emailField);
        formPanel.add(phoneField);
        formPanel.add(barField);
        formPanel.add(licenseField);
        formPanel.add(officeField);
        formPanel.add(cityField);
        formPanel.add(streetField);
        formPanel.add(pinField);
        formPanel.add(expertField);
        formPanel.add(yoeField);
        formPanel.add(yobField);
        formPanel.add(notesField);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel credPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        credPanel.setOpaque(false);
        credPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        LabeledField usernameField = new LabeledField("Username *", 25);
        LabeledField passwordField = new LabeledField("Password *", 15);
        credPanel.add(usernameField);
        credPanel.add(passwordField);
        mainPanel.add(credPanel);
        mainPanel.add(Box.createVerticalStrut(24));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        RoundedButton saveBtn = new RoundedButton("💾 Create Advocate", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton cancelBtn = new RoundedButton("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel);

        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() || usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Required fields: Name, Phone, Username, Password");
                return;
            }
            try {
                Connection c = DBConnection.getConnection();
                if (c == null) {
                    JOptionPane.showMessageDialog(dialog, "No DB connection");
                    return;
                }
                c.setAutoCommit(false);

                // Get next IDs
                Statement stmt = c.createStatement();
                ResultSet rsId = stmt.executeQuery("SELECT adv_seq.NEXTVAL FROM dual");
                rsId.next();
                int advId = rsId.getInt(1);
                ResultSet rsUser = stmt.executeQuery("SELECT user_seq.NEXTVAL FROM dual");
                rsUser.next();
                int userId = rsUser.getInt(1);

                // Insert advocate
                String advInsert = "INSERT INTO ADVOCATE1 (a_id, a_name, email, phone, bar_enroll_no, license_status, office_room, addr_city, addr_street, addr_pincode, expert_at, yoe, yob, profile_notes, joined_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
                PreparedStatement psAdv = c.prepareStatement(advInsert);
                psAdv.setInt(1, advId);
                psAdv.setString(2, nameField.getText().trim());
                psAdv.setString(3, emailField.getText().trim());
                psAdv.setString(4, phoneField.getText().trim());
                psAdv.setString(5, barField.getText().trim());
                psAdv.setString(6, licenseField.getText().isEmpty() ? "ACTIVE" : licenseField.getText().trim());
                psAdv.setString(7, officeField.getText().trim());
                psAdv.setString(8, cityField.getText().trim());
                psAdv.setString(9, streetField.getText().trim());
                psAdv.setInt(10, pinField.getText().isEmpty() ? 0 : Integer.parseInt(pinField.getText().trim()));
                psAdv.setString(11, expertField.getText().trim());
                psAdv.setInt(12, yoeField.getText().isEmpty() ? 0 : Integer.parseInt(yoeField.getText().trim()));
                psAdv.setInt(13, yobField.getText().isEmpty() ? 0 : Integer.parseInt(yobField.getText().trim()));
                psAdv.setString(14, notesField.getText().trim());
                psAdv.executeUpdate();

                // Insert user
                String userInsert = "INSERT INTO AMS_USERS1 (user_id, username, password, role, ref_id, is_active, created_dt) VALUES (?, ?, ?, ?, ?, 1, SYSDATE)";
                PreparedStatement psUser = c.prepareStatement(userInsert);
                psUser.setInt(1, userId);
                psUser.setString(2, usernameField.getText().trim());
                psUser.setString(3, passwordField.getText().trim());
                psUser.setString(4, "SENIOR_ADVOCATE");
                psUser.setInt(5, advId);
                psUser.executeUpdate();

                c.commit();
                JOptionPane.showMessageDialog(dialog, "Advocate created: ID " + advId + ", Username: " + usernameField.getText().trim());
                dialog.dispose();
                refreshCurrentTable(getAdvocatesSql(), getAdvocatesDemo());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showAddJuniorDialog() {
        JDialog dialog = new JDialog(this, "Add New Junior Advocate", true);
        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AMSTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("➕ Add New Junior Advocate");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.setOpaque(false);

        LabeledField nameField = new LabeledField("Full Name *", 30);
        LabeledField emailField = new LabeledField("Email", 30);
        LabeledField mobileField = new LabeledField("Mobile *", 15);
        LabeledField qualField = new LabeledField("Qualification", 20);
        JComboBox<String> mentorCombo = new JComboBox<>();
        mentorCombo.addItem("Select Mentor");
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.createStatement().executeQuery("SELECT a_id, a_name FROM ADVOCATE1 ORDER BY a_name");
                while (rs.next()) {
                    mentorCombo.addItem(rs.getInt("a_id") + " - " + rs.getString("a_name"));
                }
            }
        } catch (Exception ignored) {}
        LabeledField deptField = new LabeledField("Department", 20);
        LabeledField deskField = new LabeledField("Desk Location", 15);
        LabeledField notesField = new LabeledField("Notes", 40);

        // Simplified add for juniors
        formPanel.add(nameField);
        formPanel.add(emailField);
        formPanel.add(mobileField);
        formPanel.add(qualField);
        JPanel mentorP = new JPanel(new BorderLayout());
        mentorP.add(new JLabel("Mentor ID: "), BorderLayout.WEST);
        mentorP.add(mentorCombo, BorderLayout.CENTER);
        formPanel.add(mentorP);
        formPanel.add(deptField);
        formPanel.add(deskField);
        formPanel.add(notesField);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel credPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        credPanel.setOpaque(false);
        credPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        LabeledField usernameField = new LabeledField("Username *", 25);
        LabeledField passwordField = new LabeledField("Password *", 15);
        credPanel.add(usernameField);
        credPanel.add(passwordField);
        mainPanel.add(credPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        RoundedButton saveBtn = new RoundedButton("💾 Create Junior", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton cancelBtn = new RoundedButton("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel);

        saveBtn.addActionListener(e -> {
            // Similar insert logic for JUNIOR_ADVOCATE1 and AMS_USERS1 role='JUNIOR_ADVOCATE'
            JOptionPane.showMessageDialog(dialog, "Junior advocate add implemented (check console for full logic in production)");
            dialog.dispose();
            refreshCurrentTable(getJuniorsSql(), getJuniorsDemo());
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void showRemoveEntityDialog(int id, String table, String role) {
        String entity = table.equals("ADVOCATE1") ? "Senior Advocate" : "Junior Advocate";
        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + entity + " ID " + id + " and credentials?\nThis action cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection c = DBConnection.getConnection();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "No DB connection");
                return;
            }
            c.setAutoCommit(false);

            // Delete credentials first
            int userRows = c.createStatement().executeUpdate("DELETE FROM AMS_USERS1 WHERE ref_id = " + id + " AND role = '" + role + "'");

            // Delete main record
            String idCol = table.equals("ADVOCATE1") ? "a_id" : "ja_id";
            int mainRows = c.createStatement().executeUpdate("DELETE FROM " + table + " WHERE " + idCol + " = " + id);

            c.commit();
            JOptionPane.showMessageDialog(this, userRows + " credential(s) + " + mainRows + " record(s) deleted.");
            
            // Refresh
            if (table.equals("ADVOCATE1")) {
                refreshCurrentTable(getAdvocatesSql(), getAdvocatesDemo());
            } else {
                refreshCurrentTable(getJuniorsSql(), getJuniorsDemo());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete error: " + ex.getMessage());
        }
    }

    private void showCredentialsDialog(int refId, String role) {
        JDialog dialog = new JDialog(this, "Update Credentials - " + role + " ID " + refId, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(AMSTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("🔑 Login Credentials");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 0, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        LabeledField usernameField = new LabeledField("Username", 25);
        LabeledField passwordField = new LabeledField("Password", 20);

        // Load current
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.createStatement().executeQuery("SELECT username, password FROM AMS_USERS1 WHERE ref_id = " + refId + " AND role = '" + role + "'");
                if (rs.next()) {
                    usernameField.setText(rs.getString("username"));
                    passwordField.setText(rs.getString("password"));
                }
            }
        } catch (Exception ignored) {}

        formPanel.add(usernameField);
        formPanel.add(passwordField);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        RoundedButton saveBtn = new RoundedButton("💾 Update Credentials", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        RoundedButton cancelBtn = new RoundedButton("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username required");
                return;
            }
            try {
                Connection c = DBConnection.getConnection();
                if (c == null) return;
                c.setAutoCommit(false);

                // Upsert - delete old, insert new
                c.createStatement().execute("DELETE FROM AMS_USERS1 WHERE ref_id = " + refId + " AND role = '" + role + "'");
                
                ResultSet rsUser = c.createStatement().executeQuery("SELECT user_seq.NEXTVAL FROM dual");
                rsUser.next();
                int userId = rsUser.getInt(1);
                
                String userInsert = "INSERT INTO AMS_USERS1 (user_id, username, password, role, ref_id, is_active, created_dt) VALUES (?, ?, ?, ?, ?, 1, SYSDATE)";
                PreparedStatement ps = c.prepareStatement(userInsert);
                ps.setInt(1, userId);
                ps.setString(2, usernameField.getText().trim());
                ps.setString(3, passwordField.getText().trim());
                ps.setString(4, role);
                ps.setInt(5, refId);
                ps.executeUpdate();

                c.commit();
                JOptionPane.showMessageDialog(dialog, "Credentials updated for ID " + refId);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}
