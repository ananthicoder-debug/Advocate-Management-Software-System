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

/** Case Timeline Page */
public class TimelinePage {
    private int advId; private JPanel panel;
    public TimelinePage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel("\uD83D\uDCC5  Case Timeline");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        JPanel list=new JPanel(); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setBackground(AMSTheme.BG_MAIN);

        loadTimeline(list);

        JScrollPane sp=new JScrollPane(list); sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(title, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton addBtn=new RoundedButton("+ Add Event", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        addBtn.setPreferredSize(new Dimension(130,36));
        addBtn.addActionListener(e -> {
            String caseInput = JOptionPane.showInputDialog(panel, "Enter Case ID for the new timeline event:", "Case ID", JOptionPane.PLAIN_MESSAGE);
            if (caseInput == null || caseInput.trim().isEmpty()) return;
            try {
                int caseId = Integer.parseInt(caseInput.trim());
                JFrame parentFrame = null;
                java.awt.Component comp = SwingUtilities.getWindowAncestor(panel);
                if (comp instanceof JFrame) parentFrame = (JFrame) comp;
                AddTimelineEventDialog dialog = new AddTimelineEventDialog(parentFrame, caseId);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    list.removeAll();
                    loadTimeline(list);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid numeric Case ID.", "Invalid Case ID", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnRow.add(addBtn);
        RoundedButton refreshBtn=new RoundedButton("⟳ Refresh",new Color(0x6C757D),new Color(0x5A6268));
        refreshBtn.setPreferredSize(new Dimension(110,36));
        refreshBtn.addActionListener(e->{list.removeAll(); loadTimeline(list);});
        btnRow.add(refreshBtn);
        topRow.add(btnRow, BorderLayout.EAST);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(sp,BorderLayout.CENTER);
    }
    private void loadTimeline(JPanel list){
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemo(list);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT t.time_id,cs.c_title,TO_CHAR(t.entry_date,'DD-MON-YYYY'),t.title,t.progress_sum,t.next_step,t.status_indicator " +
                        "FROM TIMELINE1 t JOIN CASES1 cs ON t.case_id=cs.case_id " +
                        "JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id " +
                        "WHERE ac.a_id="+advId+" ORDER BY t.entry_date DESC").executeQuery();
                    boolean hasItems=false;
                    while(rs.next()){
                        hasItems=true;
                        final String caseTitle=rs.getString(2),dt=rs.getString(3),ttl=rs.getString(4),
                            prog=rs.getString(5),next=rs.getString(6),stat=rs.getString(7);
                        SwingUtilities.invokeLater(()->list.add(buildTimelineCard(caseTitle,dt,ttl,prog,next,stat)));
                    }
                    if(!hasItems) {
                        SwingUtilities.invokeLater(() -> showEmptyState(list, "No timeline events found. Use the Add Event button to create one."));
                    }
                }catch(Exception e){addDemo(list);}
                return null;
            }
            @Override protected void done(){list.revalidate();list.repaint();}
        }.execute();
    }
    private void addDemo(JPanel list){
        SwingUtilities.invokeLater(()->{
            for (Map<String, Object> t : DemoDataStore.getTimelinesList()) {
                list.add(buildTimelineCard(
                    "Case #" + t.get("caseId"),
                    (String)t.get("date"),
                    (String)t.get("title"),
                    (String)t.get("description"),
                    (String)t.get("nextStep"),
                    (String)t.get("status")
                ));
            }
        });
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
    private JPanel buildTimelineCard(String caseTitle,String date,String milestoneTitle,String progress,String nextStep,String status){
        String st = status == null ? "" : status;
        Color _sc = AMSTheme.WARNING;
        if ("ON_TRACK".equals(st)) _sc = AMSTheme.SUCCESS;
        else if ("DELAYED".equals(st)) _sc = AMSTheme.DANGER;
        else if ("COMPLETED".equals(st)) _sc = AMSTheme.INFO;
        final Color statusColor = _sc;
        JPanel card=new JPanel(new BorderLayout(12,0)){ private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(statusColor); g2.fillRoundRect(0,0,5,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(14,18,14,18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left=new JPanel(new GridLayout(4,1,0,3)); left.setOpaque(false);
        JLabel ttl=new JLabel(milestoneTitle); ttl.setFont(AMSTheme.FONT_BOLD); ttl.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel caseL=new JLabel("Case: "+caseTitle); caseL.setFont(AMSTheme.FONT_SMALL); caseL.setForeground(AMSTheme.INFO);
        JLabel dtL=new JLabel(date); dtL.setFont(AMSTheme.FONT_SMALL); dtL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel progL=new JLabel(progress!=null?progress:""); progL.setFont(AMSTheme.FONT_SMALL); progL.setForeground(AMSTheme.TEXT_SECONDARY);
        left.add(ttl); left.add(caseL); left.add(dtL); left.add(progL);

        JPanel right=new JPanel(new GridLayout(2,1,0,4)); right.setOpaque(false);
        JLabel statL=new JLabel(status!=null?status:""); statL.setFont(new Font("Segoe UI",Font.BOLD,12)); statL.setForeground(statusColor);
        JLabel nextL=new JLabel("Next: "+(nextStep!=null?nextStep:"")); nextL.setFont(AMSTheme.FONT_SMALL); nextL.setForeground(AMSTheme.TEXT_SECONDARY);
        right.add(statL); right.add(nextL);

        card.add(left,BorderLayout.CENTER); card.add(right,BorderLayout.EAST);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0,0,10,0)); wrap.add(card);
        return wrap;
    }
    public JPanel getPanel(){return panel;}
}
