package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;

import com.ams.components.RoundedButton;
import com.ams.components.LabeledField;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Cases Page — list, add, view journey timeline, hearings, evidence, notes
 */
public class CasesPage {
    private int advId;
    private JPanel panel;
    private DefaultTableModel model;
    private JTable table;

    public CasesPage(int advId) {
        this.advId = advId;
        build();
    }

    private void build() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title = new JLabel("\uD83D\uDCBC  Case Management");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("+ Add Case", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton viewBtn = new RoundedButton("View Journey", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        RoundedButton refreshBtn = new RoundedButton("⟳ Refresh", new Color(0x6C757D), new Color(0x5A6268));
        btnBar.add(refreshBtn); btnBar.add(addBtn); btnBar.add(viewBtn);

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(btnBar, BorderLayout.EAST);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchBar.setOpaque(false);
        JTextField searchF = new JTextField(25);
        searchF.setFont(AMSTheme.FONT_BODY);
        searchF.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),
            new EmptyBorder(5,10,5,10)));
        JLabel searchLbl = new JLabel("\uD83D\uDD0D  Search:");
        searchLbl.setFont(AMSTheme.FONT_BODY);
        searchLbl.setForeground(AMSTheme.TEXT_SECONDARY);
        
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All Status","ACTIVE","PENDING","CLOSED","ARCHIVED"});
        statusFilter.setFont(AMSTheme.FONT_BODY);
        RoundedButton searchBtn = new RoundedButton("Search", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        searchBtn.setPreferredSize(new Dimension(100, 36));
        searchBar.add(searchLbl); searchBar.add(searchF); searchBar.add(statusFilter); searchBar.add(searchBtn);

        // Table
        String[] cols = {"Case ID","Title","Client","Type","Status","Priority","Filed Date","Court"};
        model = new DefaultTableModel(cols, 0) { private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8), 1));
        sp.getViewport().setBackground(Color.WHITE);

        // Panel card
        JPanel card = new JPanel(new BorderLayout()) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12,12,12,12));
        card.add(searchBar, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);

        // Action row at bottom
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionRow.setOpaque(false);
        RoundedButton editBtn   = new RoundedButton("Edit",   new Color(0xF39C12), new Color(0xE67E22));
        RoundedButton deleteBtn = new RoundedButton("Delete", AMSTheme.DANGER,     AMSTheme.DANGER.darker());
        RoundedButton detailBtn = new RoundedButton("Details & Journey", AMSTheme.INFO, AMSTheme.INFO.darker());
        editBtn.setPreferredSize(new Dimension(100, 36));
        deleteBtn.setPreferredSize(new Dimension(100, 36));
        detailBtn.setPreferredSize(new Dimension(180, 36));
        actionRow.add(detailBtn); actionRow.add(editBtn); actionRow.add(deleteBtn);

        panel.add(titleBar,   BorderLayout.NORTH);
        panel.add(card,       BorderLayout.CENTER);
        panel.add(actionRow,  BorderLayout.SOUTH);

        // Button actions
        addBtn.addActionListener(e -> showAddCaseDialog());
        viewBtn.addActionListener(e -> viewCaseJourney());
        detailBtn.addActionListener(e -> viewCaseJourney());
        refreshBtn.addActionListener(e -> loadCases());
        editBtn.addActionListener(e -> editCase());
        deleteBtn.addActionListener(e -> deleteCase());
        searchBtn.addActionListener(e -> searchCases(searchF.getText(), (String)statusFilter.getSelectedItem()));
        
        // Double click
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewCaseJourney();
            }
        });

        loadCases();
    }

    private void loadCases() {
        model.setRowCount(0);
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { 
                        return null; 
                    }
                    String sql = "SELECT cs.case_id,cs.c_title,cl.c_name,cs.c_type,cs.status," +
                                 "cs.priority_level,cs.filed_date,cs.court_name " +
                                 "FROM CASES1 cs JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id " +
                                 "JOIN CLIENT1 cl ON cs.c_id=cl.c_id WHERE ac.a_id=? ORDER BY cs.filed_date DESC";
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setInt(1, advId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final Object[] row = {rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                            rs.getString(5),rs.getInt(6),rs.getDate(7),rs.getString(8)};
                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                } catch (Exception e) { 
                }
                return null;
            }
        };
        w.execute();
    }



    private void searchCases(String term, String status) {
        model.setRowCount(0);
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { 
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "Database connection failed. Ensure Oracle XE is running and schema.sql executed.", "DB Error", JOptionPane.ERROR_MESSAGE));
                        return null; 
                    }
                    String stat = status.equals("All Status") ? "%" : status;
                    String sql = "SELECT cs.case_id,cs.c_title,cl.c_name,cs.c_type,cs.status," +
                                 "cs.priority_level,cs.filed_date,cs.court_name " +
                                 "FROM CASES1 cs JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id " +
                                 "JOIN CLIENT1 cl ON cs.c_id=cl.c_id WHERE ac.a_id=? " +
                                 "AND (UPPER(cs.c_title) LIKE UPPER(?) OR UPPER(cl.c_name) LIKE UPPER(?)) " +
                                 "AND cs.status LIKE ? ORDER BY cs.filed_date DESC";
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setInt(1, advId);
                    ps.setString(2, "%"+term+"%"); ps.setString(3, "%"+term+"%");
                    ps.setString(4, stat);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final Object[] row = {rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                            rs.getString(5),rs.getInt(6),rs.getDate(7),rs.getString(8)};
                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                } catch (Exception e) { 
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "Database query failed: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        };
        w.execute();
    }

    private void viewCaseJourney() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(panel, "Please select a case.", "Select Case", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int caseId = (Integer) model.getValueAt(row, 0);
        String caseTitle = (String) model.getValueAt(row, 1);
        new CaseJourneyDialog(panel, caseId, caseTitle, advId).setVisible(true);
    }

    private void showAddCaseDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel), "Add New Case", true);
        d.setSize(580, 560);
        d.setLocationRelativeTo(panel);
        d.setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(0, 2, 12, 10));
        main.setBorder(new EmptyBorder(24,24,12,24));
        main.setBackground(AMSTheme.BG_MAIN);

        LabeledField titleF  = new LabeledField("Case Title *", 30);
        LabeledField typeF   = new LabeledField("Case Type", 20);
        LabeledField courtF  = new LabeledField("Court Name", 30);
        LabeledField clientF = new LabeledField("Client ID", 10);
        LabeledField feeF    = new LabeledField("Fee Amount", 15);
        LabeledField lawF    = new LabeledField("Law Category", 20);

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"PENDING","ACTIVE","CLOSED"});
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"1 - HIGH","2 - MEDIUM","3 - LOW"});

        JLabel sLbl = new JLabel("Status"); sLbl.setFont(AMSTheme.FONT_BOLD);
        JLabel pLbl = new JLabel("Priority"); pLbl.setFont(AMSTheme.FONT_BOLD);

        JPanel sPan = new JPanel(new BorderLayout(0,5)); sPan.setOpaque(false);
        sPan.add(sLbl, BorderLayout.NORTH); sPan.add(statusBox, BorderLayout.CENTER);
        JPanel pPan = new JPanel(new BorderLayout(0,5)); pPan.setOpaque(false);
        pPan.add(pLbl, BorderLayout.NORTH); pPan.add(priorityBox, BorderLayout.CENTER);

        main.add(titleF); main.add(clientF);
        main.add(typeF);  main.add(lawF);
        main.add(courtF); main.add(feeF);
        main.add(sPan);   main.add(pPan);

        JPanel probPanel = new JPanel(new BorderLayout(0,5));
        probPanel.setBorder(new EmptyBorder(0,24,12,24));
        probPanel.setBackground(AMSTheme.BG_MAIN);
        JLabel probLbl = new JLabel("Problem Description");
        probLbl.setFont(AMSTheme.FONT_BOLD);
        JTextArea probArea = new JTextArea(4, 40);
        probArea.setFont(AMSTheme.FONT_BODY);
        probArea.setLineWrap(true);
        probArea.setBorder(new EmptyBorder(8,10,8,10));
        probPanel.add(probLbl, BorderLayout.NORTH);
        probPanel.add(new JScrollPane(probArea), BorderLayout.CENTER);

        RoundedButton saveBtn = new RoundedButton("Save Case", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            if (titleF.getText().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Case Title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            final String caseStatus = (String)statusBox.getSelectedItem();
            
            try {
                Connection con = DBConnection.getConnection();
                
                if (con != null) {
                    int cid = 0;
                    try { cid = Integer.parseInt(clientF.getText()); } catch (NumberFormatException ignored) {}
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO CASES1(case_id,c_id,c_title,c_type,law_category,court_name,status,priority_level,fee_amount,problem_desc,filed_date) " +
                        "VALUES(case_seq.NEXTVAL,?,?,?,?,?,?,?,?,?,SYSDATE)");
                    ps.setInt(1,cid); ps.setString(2,titleF.getText()); ps.setString(3,typeF.getText());
                    ps.setString(4,lawF.getText()); ps.setString(5,courtF.getText());
                    ps.setString(6,caseStatus);
                    ps.setInt(7,priorityBox.getSelectedIndex()+1);
                    double fee=0; try{fee=Double.parseDouble(feeF.getText());}catch(Exception ignored){}
                    ps.setDouble(8,fee); ps.setString(9,probArea.getText());
                    ps.executeUpdate();
                    ResultSet rs = con.prepareStatement("SELECT case_seq.CURRVAL FROM dual").executeQuery();
                    rs.next(); int genId = rs.getInt(1);
                    PreparedStatement ps2 = con.prepareStatement("INSERT INTO ADVOCATE_CASE1 VALUES(?,?)");
                    ps2.setInt(1,advId); ps2.setInt(2,genId);
                    ps2.executeUpdate();
                    con.commit();
                } else {
                    JOptionPane.showMessageDialog(d, "Database connection failed. Ensure Oracle XE is running and schema.sql executed.", "DB Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(d, "Case added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                loadCases();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d,"Error adding case: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN);
        btnRow.add(saveBtn);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(AMSTheme.BG_MAIN);
        inner.add(main, BorderLayout.NORTH);
        inner.add(probPanel, BorderLayout.CENTER);

        d.add(inner, BorderLayout.CENTER);
        d.add(btnRow, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void editCase() {
        int row = table.getSelectedRow();
        if (row < 0) { 
            JOptionPane.showMessageDialog(panel, "Please select a case to edit.", "Select Case", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        int caseId = (Integer) model.getValueAt(row,0);
        JOptionPane.showMessageDialog(panel, "Edit Case #" + caseId + " — Update dialog opens here.");
    }

    private void deleteCase() {
        int row = table.getSelectedRow();
        if (row < 0) { 
            JOptionPane.showMessageDialog(panel, "Please select a case to delete.", "Select Case", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        int caseId = (Integer) model.getValueAt(row,0);
        String caseTitle = (String) model.getValueAt(row,1);
        int opt = JOptionPane.showConfirmDialog(panel,
            "Are you sure you want to archive case '" + caseTitle + "' (ID: " + caseId + ")?\n\nThis will mark the case as ARCHIVED.",
            "Confirm Archive Case", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                Connection c = DBConnection.getConnection();
                if (c != null) {
                    PreparedStatement ps1 = c.prepareStatement("DELETE FROM ADVOCATE_CASE1 WHERE case_id=?");
                    ps1.setInt(1, caseId);
                    ps1.executeUpdate();
                    
                    PreparedStatement ps2 = c.prepareStatement("UPDATE CASES1 SET status='ARCHIVED' WHERE case_id=?");
                    ps2.setInt(1, caseId);
                    ps2.executeUpdate();
                    
                    c.commit();
                    JOptionPane.showMessageDialog(panel, "Case archived successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadCases();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error archiving case: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(AMSTheme.FONT_BODY);
        t.setForeground(AMSTheme.TEXT_PRIMARY);
        t.setBackground(Color.WHITE);
        t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.setSelectionForeground(AMSTheme.TEXT_PRIMARY);
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF));
        t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        // Row striping
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() { private static final long serialVersionUID = 1L;
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl,val,sel,focus,row,col);
                if (!sel) c.setBackground(row%2==0 ? Color.WHITE : new Color(0xF8FAFF));
                setBorder(new EmptyBorder(0,10,0,10));
                // Colour status
                if (col == 4 && val != null) {
                    String s = val.toString();
                    Color fg = AMSTheme.DANGER;
                    if ("ACTIVE".equals(s)) fg = AMSTheme.SUCCESS;
                    else if ("PENDING".equals(s)) fg = AMSTheme.WARNING;
                    else if ("CLOSED".equals(s)) fg = AMSTheme.TEXT_MUTED;
                    c.setForeground(fg);
                } else { c.setForeground(AMSTheme.TEXT_PRIMARY); }
                return c;
            }
        });
    }

    public JPanel getPanel() { return panel; }
}
