package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/** Clients Page for Senior Advocate */
public class ClientsPage {
    private int advId; private JPanel panel; private DefaultTableModel model;
    public ClientsPage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel("\uD83D\uDC65  Client Management");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        String[] cols={"Client ID","Name","Email","Phone","Type","City","Cases Handled"};
        model=new DefaultTableModel(cols,0){ private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table=buildTable();
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton viewBtn=new RoundedButton("View Client",AMSTheme.PRIMARY,AMSTheme.PRIMARY_LIGHT);
        RoundedButton editBtn=new RoundedButton("Edit Client",AMSTheme.INFO,AMSTheme.INFO.darker());
        RoundedButton refreshBtn=new RoundedButton("⟳",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(60,36));
        btnRow.add(refreshBtn); btnRow.add(editBtn); btnRow.add(viewBtn);

        refreshBtn.addActionListener(e->loadData());
        viewBtn.addActionListener(e->viewClientDetail(table));
        editBtn.addActionListener(e->editSelectedClient(table));
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){
                if(e.getClickCount()==2) viewClientDetail(table);
            }
        });

        panel.add(title,BorderLayout.NORTH); panel.add(sp,BorderLayout.CENTER); panel.add(btnRow,BorderLayout.SOUTH);
        loadData();
    }
    private void loadData(){
        model.setRowCount(0);
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemo();return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT DISTINCT cl.c_id,cl.c_name,cl.email,cl.phone,cl.cl_type,cl.addr_city," +
                        "(SELECT COUNT(*) FROM CASES1 cs2 WHERE cs2.c_id=cl.c_id) " +
                        "FROM CLIENT1 cl JOIN REPRESENTS1 r ON cl.c_id=r.c_id WHERE r.a_id="+advId).executeQuery();
                    while(rs.next()){
                        final Object[] row={rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getInt(7)};
                        SwingUtilities.invokeLater(()->model.addRow(row));
                    }
                }catch(Exception e){addDemo();}
                return null;
            }
        }.execute();
    }
    private void addDemo(){SwingUtilities.invokeLater(()->{
        model.addRow(new Object[]{1,"Arun Patel","arun@gmail.com","9111111111","INDIVIDUAL","Chennai",2});
        model.addRow(new Object[]{2,"Sunita Patel","sunita@co.com","9222222222","CORPORATE","Coimbatore",1});
        model.addRow(new Object[]{3,"Raj Kumar","raj@gmail.com","9333333333","INDIVIDUAL","Madurai",1});
    });}
    private void viewClientDetail(JTable table){
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(panel,"Please select a client.");return;}
        int cid=(Integer)model.getValueAt(row,0);
        String name=(String)model.getValueAt(row,1);
        showClientDialog(cid,name);
    }

    private void editSelectedClient(JTable table){
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select a client to edit."); return; }
        int cid = (Integer) model.getValueAt(row, 0);
        showEditClientDialog(cid);
    }

    private void showEditClientDialog(int cid){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Edit Client",true);
        d.setSize(420,320); d.setLocationRelativeTo(panel);
        JPanel form=new JPanel(new GridLayout(0,2,10,10)); form.setBackground(AMSTheme.BG_MAIN); form.setBorder(new EmptyBorder(16,18,16,18));

        JTextField nameF=new JTextField(); JTextField emailF=new JTextField(); JTextField phoneF=new JTextField();
        JComboBox<String> typeF=new JComboBox<>(new String[]{"INDIVIDUAL","CORPORATE"}); JTextField cityF=new JTextField();

        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                PreparedStatement ps=c.prepareStatement("SELECT c_name,email,phone,cl_type,addr_city FROM CLIENT1 WHERE c_id=?");
                ps.setInt(1,cid); ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    nameF.setText(rs.getString(1));
                    emailF.setText(rs.getString(2));
                    phoneF.setText(rs.getString(3));
                    typeF.setSelectedItem(rs.getString(4));
                    cityF.setText(rs.getString(5));
                }
            }
        }catch(Exception e){ JOptionPane.showMessageDialog(panel,"Unable to load client data: "+e.getMessage()); return; }

        form.add(new JLabel("Name")); form.add(nameF);
        form.add(new JLabel("Email")); form.add(emailF);
        form.add(new JLabel("Phone")); form.add(phoneF);
        form.add(new JLabel("Type")); form.add(typeF);
        form.add(new JLabel("City")); form.add(cityF);

        RoundedButton saveBtn=new RoundedButton("Save",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e->{
            try{
                Connection c=DBConnection.getConnection();
                if(c!=null){
                    c.setAutoCommit(false);
                    PreparedStatement ps=c.prepareStatement("UPDATE CLIENT1 SET c_name=?,email=?,phone=?,cl_type=?,addr_city=? WHERE c_id=?");
                    ps.setString(1,nameF.getText().trim());
                    ps.setString(2,emailF.getText().trim());
                    ps.setString(3,phoneF.getText().trim());
                    ps.setString(4,(String)typeF.getSelectedItem());
                    ps.setString(5,cityF.getText().trim());
                    ps.setInt(6,cid);
                    ps.executeUpdate();
                    c.commit();
                    loadData();
                    JOptionPane.showMessageDialog(d,"Client updated successfully.");
                    d.dispose();
                    return;
                }
                JOptionPane.showMessageDialog(d,"Unable to connect database.");
            }catch(Exception ex){
                try{Connection c=DBConnection.getConnection(); if(c!=null) c.rollback();}catch(Exception ignore){}
                JOptionPane.showMessageDialog(d,"Error saving client: "+ex.getMessage());
            }
        });

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER)); btnRow.setOpaque(false); btnRow.add(saveBtn);
        d.setLayout(new BorderLayout()); d.add(form,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }
    private void showClientDialog(int cid,String name){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Client: "+name,true);
        d.setSize(600,500); d.setLocationRelativeTo(panel);
        JPanel main=new JPanel(new BorderLayout()); main.setBackground(AMSTheme.BG_MAIN);
        main.setBorder(new EmptyBorder(20,24,20,24));

        // Profile section
        JPanel profile=new JPanel(new GridLayout(0,2,12,8)); profile.setOpaque(false);
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                ResultSet rs=c.prepareStatement("SELECT c_name,email,phone,cl_type,addr_city FROM CLIENT1 WHERE c_id="+cid).executeQuery();
                if(rs.next()){
                    profile.add(infoLbl("Name:")); profile.add(infoLbl(rs.getString(1)));
                    profile.add(infoLbl("Email:")); profile.add(infoLbl(rs.getString(2)));
                    profile.add(infoLbl("Phone:")); profile.add(infoLbl(rs.getString(3)));
                    profile.add(infoLbl("Type:")); profile.add(infoLbl(rs.getString(4)));
                    profile.add(infoLbl("City:")); profile.add(infoLbl(rs.getString(5)));
                }
            } else {
                profile.add(infoLbl("Name:")); profile.add(infoLbl(name));
                profile.add(infoLbl("Client ID:")); profile.add(infoLbl(String.valueOf(cid)));
            }
        }catch(Exception e){profile.add(infoLbl("Error loading client data"));}

        // Cases
        String[] cols={"Case ID","Title","Status","Filed Date"};
        DefaultTableModel cm=new DefaultTableModel(cols,0){ private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable ct=new JTable(cm); ct.setRowHeight(30); ct.setFont(AMSTheme.FONT_BODY);
        ct.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        ct.getTableHeader().setBackground(new Color(0xF0F4FF));
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                ResultSet rs=c.prepareStatement("SELECT case_id,c_title,status,TO_CHAR(filed_date,'DD-MON-YYYY') FROM CASES1 WHERE c_id="+cid).executeQuery();
                while(rs.next()) cm.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)});
            } else {
                cm.addRow(new Object[]{1001,"State vs Rajan","ACTIVE","10-JAN-2024"});
            }
        }catch(Exception ignored){}

        JScrollPane sp=new JScrollPane(ct); sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JLabel casesLbl=new JLabel("Cases"); casesLbl.setFont(AMSTheme.FONT_HEADING); casesLbl.setForeground(AMSTheme.PRIMARY);
        JLabel profileLbl=new JLabel("Profile"); profileLbl.setFont(AMSTheme.FONT_HEADING); profileLbl.setForeground(AMSTheme.PRIMARY);

        main.add(profileLbl,BorderLayout.NORTH); main.add(profile, BorderLayout.CENTER);

        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(profile,BorderLayout.NORTH); center.add(casesLbl, BorderLayout.SOUTH);
        center.add(sp,BorderLayout.CENTER);

        d.add(main); d.add(new JScrollPane(center));
        d.setLayout(new BorderLayout());
        d.add(profileLbl,BorderLayout.NORTH);
        d.add(new JScrollPane(center),BorderLayout.CENTER);
        d.setVisible(true);
    }
    private JLabel infoLbl(String t){JLabel l=new JLabel(t);l.setFont(AMSTheme.FONT_BODY);l.setForeground(AMSTheme.TEXT_PRIMARY);return l;}
    private JTable buildTable(){
        JTable t=new JTable(model); t.setRowHeight(34); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF)); t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        return t;
    }
    public JPanel getPanel(){return panel;}
}
