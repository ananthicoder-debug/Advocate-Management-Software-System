package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/** Strategy & Notes Page */
public class StrategyPage {
    private int advId; private JPanel panel;
    public StrategyPage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));

        JLabel title=new JLabel("\uD83D\uDCDD  Strategy & Legal Notes");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        String[] cols={"STR-ID","Case ID","Heading","Law Section","Status","Created"};
        DefaultTableModel model=new DefaultTableModel(cols,0){ private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table=new JTable(model);
        table.setRowHeight(34); table.setFont(AMSTheme.FONT_BODY);
        table.setBackground(Color.WHITE); table.setGridColor(new Color(0xEEF2FF));
        table.setSelectionBackground(new Color(0xDCEAFF));
        table.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(0xF0F4FF));
        table.getTableHeader().setForeground(AMSTheme.PRIMARY);

        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton addBtn=new RoundedButton("+ Add Strategy",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        RoundedButton refreshBtn=new RoundedButton("⟳",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(60,36));
        btnRow.add(refreshBtn); btnRow.add(addBtn);

        // Load demo
        model.addRow(new Object[]{4001,1001,"Alibi Defense","IPC Sec 302","READY","10-DEC-2024"});
        model.addRow(new Object[]{4002,1001,"Evidence Challenge","Evidence Act Sec 45","DRAFT","15-DEC-2024"});
        model.addRow(new Object[]{4003,1002,"Title Deed Argument","Transfer of Property Act Sec 54","READY","20-FEB-2024"});

        addBtn.addActionListener(e->showAddStrategyDialog(model));

        panel.add(title,BorderLayout.NORTH); panel.add(sp,BorderLayout.CENTER); panel.add(btnRow,BorderLayout.SOUTH);
    }

    private void showAddStrategyDialog(DefaultTableModel model){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Add Strategy",true);
        d.setSize(560,480); d.setLocationRelativeTo(panel);
        JPanel main=new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24)); main.setBackground(AMSTheme.BG_MAIN);

        JTextField caseF=tf(), headF=tf(), lawF=tf(), coArgF=tf();
        JComboBox<String> statusBox=new JComboBox<>(new String[]{"DRAFT","READY","SUBMITTED"});

        main.add(lbl("Case ID *")); main.add(caseF);
        main.add(lbl("Heading *")); main.add(headF);
        main.add(lbl("Law Section")); main.add(lawF);
        main.add(lbl("Counter Argument")); main.add(coArgF);
        main.add(lbl("Status")); main.add(statusBox);

        JPanel argPanel=new JPanel(new BorderLayout(0,5));
        argPanel.setBorder(new EmptyBorder(0,24,12,24));
        argPanel.setBackground(AMSTheme.BG_MAIN);
        argPanel.add(lbl("Argument Text"),BorderLayout.NORTH);
        JTextArea argArea=new JTextArea(4,30); argArea.setFont(AMSTheme.FONT_BODY); argArea.setLineWrap(true);
        argPanel.add(new JScrollPane(argArea),BorderLayout.CENTER);

        RoundedButton saveBtn=new RoundedButton("Save Strategy",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e->{
            if(headF.getText().trim().isEmpty()){JOptionPane.showMessageDialog(d,"Heading required."); return;}
            try{
                Connection c=DBConnection.getConnection();
                if(c!=null){
                    PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO STRATEGY(str_id,case_id,heading,law_section,co_argument,arg_text,status,created_by,created_dt) " +
                        "VALUES(strategy_seq.NEXTVAL,?,?,?,?,?,?,?,SYSDATE)");
                    ps.setInt(1,Integer.parseInt(caseF.getText())); ps.setString(2,headF.getText());
                    ps.setString(3,lawF.getText()); ps.setString(4,coArgF.getText());
                    ps.setString(5,argArea.getText()); ps.setString(6,(String)statusBox.getSelectedItem());
                    ps.setInt(7,advId);
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d,"Strategy saved!");
                model.addRow(new Object[]{0,caseF.getText(),headF.getText(),lawF.getText(),statusBox.getSelectedItem(),"Today"});
                d.dispose();
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        JPanel content=new JPanel(new BorderLayout()); content.setBackground(AMSTheme.BG_MAIN);
        content.add(main,BorderLayout.NORTH); content.add(argPanel,BorderLayout.CENTER);
        d.add(content,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }
    private JTextField tf(){JTextField f=new JTextField();f.setFont(AMSTheme.FONT_BODY);f.setBackground(AMSTheme.BG_INPUT);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),new EmptyBorder(5,10,5,10)));return f;}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(AMSTheme.FONT_BOLD);l.setForeground(AMSTheme.TEXT_SECONDARY);return l;}
    public JPanel getPanel(){return panel;}
}
