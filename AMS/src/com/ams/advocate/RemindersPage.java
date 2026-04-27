package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.DemoDataStore;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

/** Reminders Page */
public class RemindersPage {
    private int advId; private JPanel panel;
    public RemindersPage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel("\uD83D\uDD14  Reminders & Alerts");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        JPanel list=new JPanel(); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setBackground(AMSTheme.BG_MAIN);
        loadReminders(list);
        JScrollPane sp=new JScrollPane(list); sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(title, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton addBtn=new RoundedButton("+ Add Reminder",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        RoundedButton refreshBtn=new RoundedButton("⟳",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(60,36));
        addBtn.addActionListener(e->showAddReminderDialog(list));
        refreshBtn.addActionListener(e->{list.removeAll();loadReminders(list);});
        btnRow.add(refreshBtn); btnRow.add(addBtn);
        topRow.add(btnRow, BorderLayout.EAST);

        panel.add(topRow,BorderLayout.NORTH); panel.add(sp,BorderLayout.CENTER);
    }
    private void loadReminders(JPanel list){
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemo(list);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT rem_id,case_id,TO_CHAR(due_date,'DD-MON-YYYY'),SUBSTR(message,1,120),priority,rem_status " +
                        "FROM REMINDER1 WHERE a_id="+advId+" ORDER BY due_date").executeQuery();
                    boolean hasItems=false;
                    while(rs.next()){
                        hasItems=true;
                        final int id=rs.getInt(1),cid=rs.getInt(2);
                        final String dt=rs.getString(3),msg=rs.getString(4),pri=rs.getString(5),stat=rs.getString(6);
                        SwingUtilities.invokeLater(()->list.add(buildReminderCard(id,cid,dt,msg,pri,stat)));
                    }
                    if(!hasItems){
                        SwingUtilities.invokeLater(() -> showEmptyState(list, "No reminders found. Use the Add Reminder button above."));
                    }
                }catch(Exception e){addDemo(list);}
                return null;
            }
            @Override protected void done(){list.revalidate();list.repaint();}
        }.execute();
    }
    private void addDemo(JPanel list){
        SwingUtilities.invokeLater(()->{
            for (Map<String, Object> r : DemoDataStore.getRemindersList()) {
                list.add(buildReminderCard(
                    ((Number)r.get("id")).intValue(),
                    ((Number)r.get("caseId")).intValue(),
                    (String)r.get("dueDate"),
                    (String)r.get("message"),
                    (String)r.get("priority"),
                    (String)r.get("status")
                ));
            }
        });
    }
    private JPanel buildReminderCard(int id,int caseId,String dueDate,String msg,String priority,String status){
        String pri = priority == null ? "" : priority;
        Color _pc = AMSTheme.WARNING;
        if ("HIGH".equals(pri)) _pc = AMSTheme.DANGER;
        else if ("LOW".equals(pri)) _pc = AMSTheme.SUCCESS;
        final Color priColor = _pc;
        boolean done=("COMPLETED".equals(status)||"DISMISSED".equals(status));
        JPanel card=new JPanel(new BorderLayout(12,4)){ private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(done?new Color(0xF0F0F0):AMSTheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(priColor); g2.fillRoundRect(0,0,5,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(12,18,12,18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,100)); card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left=new JPanel(new GridLayout(3,1,0,3)); left.setOpaque(false);
        JLabel msgL=new JLabel("<html>"+(msg!=null?msg:"")+"</html>"); msgL.setFont(AMSTheme.FONT_BOLD);
        msgL.setForeground(done?AMSTheme.TEXT_MUTED:AMSTheme.TEXT_PRIMARY);
        JLabel dtL=new JLabel("Due: "+dueDate+" | Case #"+caseId); dtL.setFont(AMSTheme.FONT_SMALL); dtL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel priL=new JLabel(priority+" Priority | "+status); priL.setFont(new Font("Segoe UI",Font.BOLD,11));
        priL.setForeground(done?AMSTheme.TEXT_MUTED:priColor);
        left.add(msgL); left.add(dtL); left.add(priL);

        // Done button
        RoundedButton doneBtn=new RoundedButton(done?"✓ Done":"Mark Done",
            done?new Color(0x9B9B9B):AMSTheme.SUCCESS, done?new Color(0x7A7A7A):AMSTheme.SUCCESS.brighter());
        doneBtn.setPreferredSize(new Dimension(110,34));
        doneBtn.setEnabled(!done);
        doneBtn.addActionListener(e->markDone(id,card));

        card.add(left,BorderLayout.CENTER); card.add(doneBtn,BorderLayout.EAST);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0,0,8,0)); wrap.add(card);
        return wrap;
    }
    private void markDone(int remId,JPanel card){
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                c.prepareStatement("UPDATE REMINDER1 SET rem_status='COMPLETED' WHERE rem_id="+remId).executeUpdate();
                c.commit();
            }
        }catch(Exception ignored){}
        JOptionPane.showMessageDialog(panel,"Reminder marked as done!");
    }
    private void showAddReminderDialog(JPanel list){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Add Reminder",true);
        d.setSize(460,320); d.setLocationRelativeTo(panel);
        JPanel main=new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(24,24,12,24)); main.setBackground(AMSTheme.BG_MAIN);
        JTextField caseF=tf(),dateF=tf();
        JComboBox<String> priBox=new JComboBox<>(new String[]{"HIGH","MEDIUM","LOW"});
        JTextArea msgArea=new JTextArea(3,20); msgArea.setFont(AMSTheme.FONT_BODY); msgArea.setLineWrap(true);
        main.add(lbl("Case ID")); main.add(caseF);
        main.add(lbl("Due Date (YYYY-MM-DD)")); main.add(dateF);
        main.add(lbl("Priority")); main.add(priBox);
        JPanel msgP=new JPanel(new BorderLayout(0,5)); msgP.setBorder(new EmptyBorder(0,24,12,24)); msgP.setBackground(AMSTheme.BG_MAIN);
        msgP.add(lbl("Message"),BorderLayout.NORTH); msgP.add(new JScrollPane(msgArea),BorderLayout.CENTER);
        RoundedButton saveBtn=new RoundedButton("Save",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e->{
            if(msgArea.getText().trim().isEmpty()){JOptionPane.showMessageDialog(d,"Message required.");return;}
            try{
                final int newId = (int)(System.currentTimeMillis() % 100000) + 4000;
                Connection c=DBConnection.getConnection();
                
                if(c!=null){
                    PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO REMINDER1(rem_id,a_id,case_id,due_date,message,priority,rem_status,created_date) " +
                        "VALUES(reminder_seq.NEXTVAL,?,?,TO_DATE(?,'YYYY-MM-DD'),?,?,'PENDING',SYSDATE)");
                    ps.setInt(1,advId);
                    int cid=0; try{cid=Integer.parseInt(caseF.getText());}catch(Exception ignored){}
                    ps.setInt(2,cid); ps.setString(3,dateF.getText());
                    ps.setString(4,msgArea.getText()); ps.setString(5,(String)priBox.getSelectedItem());
                    ps.executeUpdate(); c.commit();
                } else {
                    // Demo mode - add to DemoDataStore
                    int cid = 0;
                    try { cid = Integer.parseInt(caseF.getText()); } catch (Exception ignored) {}
                    String dateStr = dateF.getText().isEmpty() ? new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) : dateF.getText();
                    DemoDataStore.addReminderToDemo(newId, cid, dateStr, msgArea.getText(), 
                        (String)priBox.getSelectedItem(), "PENDING");
                }
                JOptionPane.showMessageDialog(d,"Reminder added!"); d.dispose();
                list.removeAll(); loadReminders(list);
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER)); btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        JPanel content=new JPanel(new BorderLayout()); content.setBackground(AMSTheme.BG_MAIN);
        content.add(main,BorderLayout.NORTH); content.add(msgP,BorderLayout.CENTER);
        d.add(content,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }
    private void showEmptyState(JPanel list, String message) {
        JLabel emptyLabel = new JLabel("<html><div style='text-align:center; color:#7A7D85;'>" + message + "</div></html>", SwingConstants.CENTER);
        emptyLabel.setFont(AMSTheme.FONT_BODY);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.setBorder(new EmptyBorder(40, 24, 40, 24));
        holder.add(emptyLabel, BorderLayout.CENTER);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        list.add(holder);
    }

    private JTextField tf(){JTextField f=new JTextField();f.setFont(AMSTheme.FONT_BODY);f.setBackground(AMSTheme.BG_INPUT);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),new EmptyBorder(5,10,5,10)));return f;}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(AMSTheme.FONT_BOLD);l.setForeground(AMSTheme.TEXT_SECONDARY);return l;}
    public JPanel getPanel(){return panel;}
}
