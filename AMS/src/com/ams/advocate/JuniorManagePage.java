package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/** Junior Advocate Management — assign tasks, view completion */
public class JuniorManagePage {
    private int advId; private JPanel panel;
    public JuniorManagePage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel("\uD83D\uDCCB  Junior Advocate Management");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        // Juniors list on left, tasks on right
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(300); split.setOpaque(false);
        split.setBorder(null);

        // ── Left: Junior list
        JPanel leftPanel=new JPanel(new BorderLayout()); leftPanel.setBackground(AMSTheme.BG_MAIN);
        JLabel jTitle=new JLabel("My Junior Advocates"); jTitle.setFont(AMSTheme.FONT_HEADING); jTitle.setForeground(AMSTheme.PRIMARY);
        jTitle.setBorder(new EmptyBorder(0,0,10,0));

        DefaultTableModel jModel=new DefaultTableModel(new String[]{"ID","Name","Dept","Rating"},0){ private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable jTable=new JTable(jModel);
        jTable.setRowHeight(34); jTable.setFont(AMSTheme.FONT_BODY);
        jTable.setBackground(Color.WHITE);
        jTable.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        jTable.getTableHeader().setBackground(new Color(0xF0F4FF)); jTable.getTableHeader().setForeground(AMSTheme.PRIMARY);
        JScrollPane jSp=new JScrollPane(jTable); jSp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel jBtnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); jBtnRow.setOpaque(false);
        RoundedButton assignBtn=new RoundedButton("Assign Task",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        RoundedButton viewCompBtn=new RoundedButton("View Completion",AMSTheme.INFO,AMSTheme.INFO.darker());
        jBtnRow.add(assignBtn); jBtnRow.add(viewCompBtn);

        leftPanel.add(jTitle,BorderLayout.NORTH); leftPanel.add(jSp,BorderLayout.CENTER); leftPanel.add(jBtnRow,BorderLayout.SOUTH);

        // ── Right: Tasks panel
        JPanel rightPanel=new JPanel(new BorderLayout()); rightPanel.setBackground(AMSTheme.BG_MAIN);
        rightPanel.setBorder(new EmptyBorder(0,16,0,0));
        JLabel tTitle=new JLabel("Assigned Tasks"); tTitle.setFont(AMSTheme.FONT_HEADING); tTitle.setForeground(AMSTheme.PRIMARY);
        tTitle.setBorder(new EmptyBorder(0,0,10,0));

        DefaultTableModel tModel=new DefaultTableModel(new String[]{"Task ID","Junior","Case","Type","Due Date","Status","Rating"},0){ private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tTable=new JTable(tModel);
        tTable.setRowHeight(34); tTable.setFont(AMSTheme.FONT_BODY);
        tTable.setBackground(Color.WHITE);
        tTable.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        tTable.getTableHeader().setBackground(new Color(0xF0F4FF)); tTable.getTableHeader().setForeground(AMSTheme.PRIMARY);
        JScrollPane tSp=new JScrollPane(tTable); tSp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        JPanel tBtnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8)); tBtnRow.setOpaque(false);
        RoundedButton rateBtn=new RoundedButton("Rate Task",AMSTheme.ACCENT,AMSTheme.ACCENT.darker());
        RoundedButton approveBtn=new RoundedButton("Approve",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        tBtnRow.add(approveBtn); tBtnRow.add(rateBtn);

        rightPanel.add(tTitle,BorderLayout.NORTH); rightPanel.add(tSp,BorderLayout.CENTER); rightPanel.add(tBtnRow,BorderLayout.SOUTH);

        split.setLeftComponent(leftPanel); split.setRightComponent(rightPanel);

        // Load data
        loadJuniors(jModel);
        loadTasks(tModel);

        // Button actions
        assignBtn.addActionListener(e->{
            int row=jTable.getSelectedRow();
            if(row<0){JOptionPane.showMessageDialog(panel,"Select a junior advocate.");return;}
            int jaId=(Integer)jModel.getValueAt(row,0);
            String jaName=(String)jModel.getValueAt(row,1);
            showAssignTaskDialog(jaId,jaName,tModel);
        });
        viewCompBtn.addActionListener(e->{
            int row=jTable.getSelectedRow();
            if(row<0){JOptionPane.showMessageDialog(panel,"Select a junior.");return;}
            tModel.setRowCount(0); loadJuniorTasks(tModel,(Integer)jModel.getValueAt(row,0));
        });
        rateBtn.addActionListener(e->{
            int row=tTable.getSelectedRow();
            if(row<0){JOptionPane.showMessageDialog(panel,"Select a task.");return;}
            int taskId=(Integer)tModel.getValueAt(row,0);
            String rating=JOptionPane.showInputDialog(panel,"Enter rating (1-5):","5");
            if(rating!=null) rateTask(taskId,Integer.parseInt(rating.trim()),tModel);
        });
        approveBtn.addActionListener(e->{
            int row=tTable.getSelectedRow();
            if(row<0){JOptionPane.showMessageDialog(panel,"Select a task.");return;}
            int taskId=(Integer)tModel.getValueAt(row,0);
            approveTask(taskId,tModel);
        });

        panel.add(title,BorderLayout.NORTH); panel.add(split,BorderLayout.CENTER);
    }

    private void loadJuniors(DefaultTableModel m){
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemoJuniors(m);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT ja_id,ja_name,department,rating FROM JUNIOR_ADVOCATE1 WHERE mentor_id="+advId).executeQuery();
                    while(rs.next()){
                        final Object[] row={rs.getInt(1),rs.getString(2),rs.getString(3),rs.getDouble(4)};
                        SwingUtilities.invokeLater(()->m.addRow(row));
                    }
                }catch(Exception e){addDemoJuniors(m);}
                return null;
            }
        }.execute();
    }
    private void addDemoJuniors(DefaultTableModel m){
        SwingUtilities.invokeLater(()->{
            m.addRow(new Object[]{1,"Priya Devi","Criminal",4.2});
            m.addRow(new Object[]{2,"Suresh Kumar","Civil",3.8});
        });
    }
    private void loadTasks(DefaultTableModel m){
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemoTasks(m);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT t.task_id,ja.ja_name,t.case_id,t.task_type,TO_CHAR(t.due_date,'DD-MON-YYYY'),t.status,t.rating " +
                        "FROM TASK_ASSIGNMENT1 t JOIN JUNIOR_ADVOCATE1 ja ON t.assigned_to=ja.ja_id " +
                        "WHERE t.assigned_by="+advId+" ORDER BY t.assigned_dt DESC").executeQuery();
                    while(rs.next()){
                        final Object[] row={rs.getInt(1),rs.getString(2),rs.getInt(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getInt(7)};
                        SwingUtilities.invokeLater(()->m.addRow(row));
                    }
                }catch(Exception e){addDemoTasks(m);}
                return null;
            }
        }.execute();
    }
    private void addDemoTasks(DefaultTableModel m){
        SwingUtilities.invokeLater(()->{
            m.addRow(new Object[]{5001,"Priya Devi",1001,"CLIENT_INFO","28-DEC-2024","PENDING",0});
            m.addRow(new Object[]{5002,"Suresh Kumar",1002,"REPORT","05-JAN-2025","COMPLETED",4});
        });
    }
    private void loadJuniorTasks(DefaultTableModel m, int jaId){
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemoTasks(m);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT t.task_id,ja.ja_name,t.case_id,t.task_type,TO_CHAR(t.due_date,'DD-MON-YYYY'),t.status,t.rating " +
                        "FROM TASK_ASSIGNMENT t JOIN JUNIOR_ADVOCATE ja ON t.assigned_to=ja.ja_id " +
                        "WHERE t.assigned_by="+advId+" AND t.assigned_to="+jaId+" ORDER BY t.assigned_dt DESC").executeQuery();
                    while(rs.next()){
                        final Object[] row={rs.getInt(1),rs.getString(2),rs.getInt(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getInt(7)};
                        SwingUtilities.invokeLater(()->m.addRow(row));
                    }
                }catch(Exception e){addDemoTasks(m);}
                return null;
            }
        }.execute();
    }
    private void showAssignTaskDialog(int jaId,String jaName,DefaultTableModel tModel){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Assign Task to "+jaName,true);
        d.setSize(480,400); d.setLocationRelativeTo(panel);
        JPanel main=new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24)); main.setBackground(AMSTheme.BG_MAIN);
        JTextField caseF=tf(),dueF=tf();
        JComboBox<String> typeBox=new JComboBox<>(new String[]{"CLIENT_INFO","REPORT","RESEARCH","DOCUMENT_COLLECTION","OTHER"});
        JTextArea descArea=new JTextArea(3,20); descArea.setFont(AMSTheme.FONT_BODY); descArea.setLineWrap(true);
        main.add(lbl("Case ID")); main.add(caseF);
        main.add(lbl("Task Type")); main.add(typeBox);
        main.add(lbl("Due Date (YYYY-MM-DD)")); main.add(dueF);
        JPanel descP=new JPanel(new BorderLayout(0,5)); descP.setBorder(new EmptyBorder(0,24,12,24));
        descP.setBackground(AMSTheme.BG_MAIN);
        descP.add(lbl("Task Description"),BorderLayout.NORTH); descP.add(new JScrollPane(descArea),BorderLayout.CENTER);

        RoundedButton saveBtn=new RoundedButton("Assign Task",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e->{
            try{
                Connection c=DBConnection.getConnection();
                if(c!=null){
                    PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO TASK_ASSIGNMENT1(task_id,assigned_by,assigned_to,case_id,task_desc,task_type,due_date,status,assigned_dt) " +
                        "VALUES(task_seq.NEXTVAL,?,?,?,?,?,TO_DATE(?,'YYYY-MM-DD'),'PENDING',SYSDATE)");
                    ps.setInt(1,advId); ps.setInt(2,jaId);
                    int caseId=0; try{caseId=Integer.parseInt(caseF.getText());}catch(Exception ignored){}
                    ps.setInt(3,caseId); ps.setString(4,descArea.getText());
                    ps.setString(5,(String)typeBox.getSelectedItem()); ps.setString(6,dueF.getText());
                    ps.executeUpdate(); c.commit();
                }
                JOptionPane.showMessageDialog(d,"Task assigned to "+jaName+"!"); d.dispose();
                tModel.setRowCount(0); loadTasks(tModel);
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER)); btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        JPanel content=new JPanel(new BorderLayout()); content.setBackground(AMSTheme.BG_MAIN);
        content.add(main,BorderLayout.NORTH); content.add(descP,BorderLayout.CENTER);
        d.add(content,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }
    private void rateTask(int taskId,int rating,DefaultTableModel tModel){
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                c.prepareStatement("UPDATE TASK_ASSIGNMENT1 SET rating="+rating+" WHERE task_id="+taskId).executeUpdate();
                c.commit();
            }
            JOptionPane.showMessageDialog(panel,"Rating "+rating+" given to task #"+taskId);
            tModel.setRowCount(0); loadTasks(tModel);
        }catch(Exception e){JOptionPane.showMessageDialog(panel,"Error: "+e.getMessage());}
    }
    private void approveTask(int taskId,DefaultTableModel tModel){
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                c.prepareStatement("UPDATE TASK_ASSIGNMENT1 SET status='APPROVED',completed_dt=SYSDATE WHERE task_id="+taskId).executeUpdate();
                c.commit();
            }
            JOptionPane.showMessageDialog(panel,"Task #"+taskId+" approved!");
            tModel.setRowCount(0); loadTasks(tModel);
        }catch(Exception e){JOptionPane.showMessageDialog(panel,"Error: "+e.getMessage());}
    }
    private JTextField tf(){JTextField f=new JTextField();f.setFont(AMSTheme.FONT_BODY);f.setBackground(AMSTheme.BG_INPUT);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),new EmptyBorder(5,10,5,10)));return f;}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(AMSTheme.FONT_BOLD);l.setForeground(AMSTheme.TEXT_SECONDARY);return l;}
    public JPanel getPanel(){return panel;}
}
