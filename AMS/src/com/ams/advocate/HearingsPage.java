package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.DemoDataStore;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

/** Hearings Page for Senior Advocate */
public class HearingsPage {
    private int advId;
    private JPanel panel;
    private DefaultTableModel model;

    public HearingsPage(int advId) { this.advId = advId; build(); }

    private void build() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));

        JLabel title = new JLabel("\uD83D\uDD56  Hearing Schedule");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));
        filterRow.setOpaque(false);
        JComboBox<String> filter = new JComboBox<>(new String[]{"All","UPCOMING","COMPLETED","ADJOURNED"});
        filter.setFont(AMSTheme.FONT_BODY);
        JLabel fLbl = new JLabel("Filter by Status:");
        fLbl.setFont(AMSTheme.FONT_BOLD); fLbl.setForeground(AMSTheme.TEXT_SECONDARY);
        RoundedButton applyBtn = new RoundedButton("Apply Filter",AMSTheme.PRIMARY,AMSTheme.PRIMARY_LIGHT);
        applyBtn.setPreferredSize(new Dimension(120,36));
        filterRow.add(fLbl); filterRow.add(filter); filterRow.add(applyBtn);

        String[] cols = {"H-ID","Case","Date","Time","Court","Room","Purpose","Judge","Status"};
        model = new DefaultTableModel(cols,0){ private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = buildTable();
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8));
        btnRow.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("+ Add Hearing",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        RoundedButton refreshBtn = new RoundedButton("⟳ Refresh",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(100,36));
        btnRow.add(refreshBtn); btnRow.add(addBtn);

        applyBtn.addActionListener(e -> loadHearings((String)filter.getSelectedItem()));
        refreshBtn.addActionListener(e -> loadHearings("All"));
        addBtn.addActionListener(e -> showAddHearingDialog());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(filterRow, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        loadHearings("All");
    }

    private void loadHearings(String statusFilter) {
        model.setRowCount(0);
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { 
                        SwingUtilities.invokeLater(() -> addDemo());
                        return null; 
                    }
                    String sql = "SELECT h.h_id,cs.c_title,TO_CHAR(h.h_date,'DD-MON-YYYY'),h.h_time," +
                                 "h.court_house,h.room_no,h.purpose,h.judge_name,h.status " +
                                 "FROM HEARING1 h JOIN CASES1 cs ON h.case_id=cs.case_id " +
                                 "JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id WHERE ac.a_id=?";
                    if (!statusFilter.equals("All")) sql += " AND h.status=?";
                    sql += " ORDER BY h.h_date";
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setInt(1,advId);
                    if (!statusFilter.equals("All")) ps.setString(2,statusFilter);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final Object[] row = {rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                            rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9)};
                        SwingUtilities.invokeLater(()->model.addRow(row));
                    }
                } catch (Exception e) { 
                    SwingUtilities.invokeLater(() -> addDemo());
                }
                return null;
            }
        }.execute();
    }

    private void addDemo(){
        SwingUtilities.invokeLater(()->{
            for (Map<String, Object> h : DemoDataStore.getHearingsList()) {
                model.addRow(new Object[]{
                    h.get("id"), h.get("case"), h.get("date"), h.get("time"),
                    h.get("court"), h.get("room"), h.get("purpose"), h.get("judge"), h.get("status")
                });
            }
        });
    }

    private void showAddHearingDialog() {
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Add New Hearing",true);
        d.setSize(460,380); d.setLocationRelativeTo(panel);
        JPanel main = new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24));
        main.setBackground(AMSTheme.BG_MAIN);
        JTextField caseF=tf(),dateF=tf(),timeF=tf(),courtF=tf(),roomF=tf(),purposeF=tf(),judgeF=tf();
        main.add(lbl("Case ID *")); main.add(caseF);
        main.add(lbl("Date (YYYY-MM-DD) *")); main.add(dateF);
        main.add(lbl("Time (HH:MM) *")); main.add(timeF);
        main.add(lbl("Court House *")); main.add(courtF);
        main.add(lbl("Room No")); main.add(roomF);
        main.add(lbl("Purpose *")); main.add(purposeF);
        main.add(lbl("Judge Name *")); main.add(judgeF);
        RoundedButton saveBtn = new RoundedButton("Save Hearing",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e -> {
            // Validation
            if (caseF.getText().trim().isEmpty() || dateF.getText().trim().isEmpty() || 
                timeF.getText().trim().isEmpty() || courtF.getText().trim().isEmpty() || 
                purposeF.getText().trim().isEmpty() || judgeF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Please fill in all required fields marked with *.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try{
                Connection c=DBConnection.getConnection();
                int newId = (int)(System.currentTimeMillis() % 100000) + 2000;
                
                if(c!=null){
                    PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO HEARING1(h_id,case_id,h_date,h_time,court_house,room_no,purpose,judge_name,status) " +
                        "VALUES(hearing_seq.NEXTVAL,?,TO_DATE(?,'YYYY-MM-DD'),?,?,?,?,?,'UPCOMING')");
                    ps.setInt(1,Integer.parseInt(caseF.getText().trim()));
                    ps.setString(2,dateF.getText().trim()); ps.setString(3,timeF.getText().trim());
                    ps.setString(4,courtF.getText().trim()); ps.setString(5,roomF.getText().trim());
                    ps.setString(6,purposeF.getText().trim()); ps.setString(7,judgeF.getText().trim());
                    ps.executeUpdate(); c.commit();
                } else {
                    // Demo mode - add to DemoDataStore
                    String caseName = "Case #" + caseF.getText().trim();
                    DemoDataStore.addHearingToDemo(newId, caseName, dateF.getText().trim(), 
                        timeF.getText().trim(), courtF.getText().trim(), roomF.getText().trim(),
                        purposeF.getText().trim(), judgeF.getText().trim(), "UPCOMING");
                }
                JOptionPane.showMessageDialog(d,"Hearing added successfully!","Success",JOptionPane.INFORMATION_MESSAGE); 
                d.dispose();
                loadHearings("All");
            } catch(Exception ex){ 
                JOptionPane.showMessageDialog(d,"Error adding hearing: "+ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE); 
            }
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        d.add(main,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private JTable buildTable(){
        JTable t=new JTable(model);
        t.setRowHeight(34); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF));
        t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        return t;
    }
    private JTextField tf(){
        JTextField f=new JTextField(); f.setFont(AMSTheme.FONT_BODY);
        f.setBackground(AMSTheme.BG_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),new EmptyBorder(5,10,5,10)));
        return f;
    }
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(AMSTheme.FONT_BOLD);l.setForeground(AMSTheme.TEXT_SECONDARY);return l;}
    public JPanel getPanel(){return panel;}
}
