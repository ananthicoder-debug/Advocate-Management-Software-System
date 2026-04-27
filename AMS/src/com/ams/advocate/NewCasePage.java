package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;

import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

/** New Case Requests — advocate accepts/rejects cases */
public class NewCasePage {
    private int advId; private JPanel panel;
    public NewCasePage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel("\uD83D\uDCE5  New Case Requests");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        JPanel list=new JPanel(); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setBackground(AMSTheme.BG_MAIN);
        loadPending(list);
        JScrollPane sp=new JScrollPane(list); sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton refreshBtn=new RoundedButton("⟳ Refresh",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(110,36));
        refreshBtn.addActionListener(e->{list.removeAll();loadPending(list);});
        btnRow.add(refreshBtn);

        panel.add(title,BorderLayout.NORTH); panel.add(sp,BorderLayout.CENTER); panel.add(btnRow,BorderLayout.SOUTH);
    }
    private void loadPending(JPanel list){
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemo(list);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT cs.case_id,cs.c_title,cl.c_name,cs.c_type,cs.priority_level,TO_CHAR(cs.filed_date,'DD-MON-YYYY') " +
                        "FROM CASES1 cs JOIN CLIENT1 cl ON cs.c_id=cl.c_id " +
                        "WHERE cs.assigned_adv="+advId+" AND cs.status='PENDING'").executeQuery();
                    while(rs.next()){
                        final int cid=rs.getInt(1);
                        final String t=rs.getString(2),cl=rs.getString(3),ty=rs.getString(4),dt=rs.getString(6);
                        final int pri=rs.getInt(5);
                        SwingUtilities.invokeLater(()->list.add(buildRequestCard(cid,t,cl,ty,pri,dt,list)));
                    }
                }catch(Exception e){addDemo(list);}
                return null;
            }
            @Override protected void done(){list.revalidate();list.repaint();}
        }.execute();
    }
    private void addDemo(JPanel list){
        SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(panel, "Database connection failed. Ensure Oracle XE is running and schema.sql executed.", "DB Error", JOptionPane.ERROR_MESSAGE));
    }
    private JPanel buildRequestCard(int caseId,String caseTitle,String client,String type,int priority,String date,JPanel list){
        Color priColor=priority==1?AMSTheme.DANGER:priority==2?AMSTheme.WARNING:AMSTheme.SUCCESS;
        JPanel card=new JPanel(new BorderLayout(12,4)){ private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFFF3CD)); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(0xF0A500)); g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(14,18,14,18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,120)); card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info=new JPanel(new GridLayout(4,1,0,3)); info.setOpaque(false);
        JLabel ttl=new JLabel("Case #"+caseId+": "+caseTitle); ttl.setFont(AMSTheme.FONT_BOLD); ttl.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel clL=new JLabel("Client: "+client+" | Type: "+type); clL.setFont(AMSTheme.FONT_BODY); clL.setForeground(AMSTheme.TEXT_SECONDARY);
        JLabel dtL=new JLabel("Filed: "+date); dtL.setFont(AMSTheme.FONT_SMALL); dtL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel priL=new JLabel("Priority: "+(priority==1?"HIGH":priority==2?"MEDIUM":"LOW")); priL.setFont(new Font("Segoe UI",Font.BOLD,12)); priL.setForeground(priColor);
        info.add(ttl); info.add(clL); info.add(dtL); info.add(priL);

        JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); btns.setOpaque(false);
        RoundedButton acceptBtn=new RoundedButton("Accept",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        RoundedButton rejectBtn=new RoundedButton("Reject",AMSTheme.DANGER,AMSTheme.DANGER.darker());
        acceptBtn.setPreferredSize(new Dimension(100,34)); rejectBtn.setPreferredSize(new Dimension(100,34));
        acceptBtn.addActionListener(e->updateCaseStatus(caseId,"ACTIVE",card,list));
        rejectBtn.addActionListener(e->updateCaseStatus(caseId,"REJECTED",card,list));
        btns.add(acceptBtn); btns.add(rejectBtn);

        card.add(info,BorderLayout.CENTER); card.add(btns,BorderLayout.EAST);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0,0,10,0)); wrap.add(card);
        return wrap;
    }
    private void updateCaseStatus(int caseId,String status,JPanel card,JPanel list){
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                c.prepareStatement("UPDATE CASES1 SET status='"+status+"' WHERE case_id="+caseId).executeUpdate();
                if(status.equals("ACTIVE")){
                    PreparedStatement ps=c.prepareStatement("INSERT INTO ADVOCATE_CASE1 VALUES(?,?) ");
                    ps.setInt(1,advId); ps.setInt(2,caseId);
                    try{ps.executeUpdate();}catch(Exception ignored){}
                }
                c.commit();
            }
        }catch(Exception e){JOptionPane.showMessageDialog(panel,"Error: "+e.getMessage()); return;}
        JOptionPane.showMessageDialog(panel,"Case "+(status.equals("ACTIVE")?"accepted":"rejected")+" successfully!");
        list.remove(card.getParent()); list.revalidate(); list.repaint();
    }
    public JPanel getPanel(){return panel;}
}
