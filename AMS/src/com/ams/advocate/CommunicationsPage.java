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

/** Communications Page */
public class CommunicationsPage {
    private int advId; private JPanel panel;
    public CommunicationsPage(int advId){this.advId=advId;build();}
    private void build(){
        panel=new JPanel(new BorderLayout()); panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24,28,24,28));
        JLabel title=new JLabel("\uD83D\uDCAC  Client Communications");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        JPanel list=new JPanel(); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setBackground(AMSTheme.BG_MAIN);
        loadComms(list);
        JScrollPane sp=new JScrollPane(list); sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setOpaque(false);
        RoundedButton addBtn=new RoundedButton("+ Log Communication",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        addBtn.addActionListener(e->showAddCommDialog(list));
        btnRow.add(addBtn);

        panel.add(title,BorderLayout.NORTH); panel.add(sp,BorderLayout.CENTER); panel.add(btnRow,BorderLayout.SOUTH);
    }
    private void loadComms(JPanel list){
        list.removeAll();
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemo(list);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT cl.c_name,cs.c_title,TO_CHAR(cm.init_date,'DD-MON-YYYY HH24:MI'),cm.comm_mode,SUBSTR(cm.summary,1,150),cm.follow_action " +
                        "FROM COMMUNICATION1 cm JOIN CLIENT1 cl ON cm.c_id=cl.c_id " +
                        "LEFT JOIN CASES1 cs ON cm.case_id=cs.case_id " +
                        "WHERE cm.a_id="+advId+" ORDER BY cm.init_date DESC").executeQuery();
                    while(rs.next()){
                        final String cn=rs.getString(1),ct=rs.getString(2),dt=rs.getString(3),
                            mode=rs.getString(4),summ=rs.getString(5),fa=rs.getString(6);
                        SwingUtilities.invokeLater(()->list.add(buildCommCard(cn,ct,dt,mode,summ,fa)));
                    }
                }catch(Exception e){addDemo(list);}
                return null;
            }
            @Override protected void done(){list.revalidate();list.repaint();}
        }.execute();
    }
    private void addDemo(JPanel list){
        SwingUtilities.invokeLater(()->{
            for (Map<String, Object> cm : DemoDataStore.getCommsList()) {
                list.add(buildCommCard(
                    (String)cm.get("client"),
                    "Case #" + cm.get("caseId"),
                    (String)cm.get("date"),
                    (String)cm.get("mode"),
                    (String)cm.get("summary"),
                    (String)cm.get("summary")  // Use summary as followUp for demo
                ));
            }
        });
    }
    private JPanel buildCommCard(String client,String caseTitle,String date,String mode,String summary,String followUp){
        String md = mode == null ? "" : mode;
        Color _mc = AMSTheme.TEXT_MUTED;
        if ("CALL".equals(md)) _mc = AMSTheme.SUCCESS;
        else if ("EMAIL".equals(md)) _mc = AMSTheme.INFO;
        else if ("MEETING".equals(md)) _mc = AMSTheme.ACCENT;
        final Color modeColor = _mc;
        JPanel card=new JPanel(new BorderLayout(12,4)){ private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(modeColor); g2.fillRoundRect(0,0,5,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(12,18,12,18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,110)); card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left=new JPanel(new GridLayout(4,1,0,3)); left.setOpaque(false);
        JLabel header=new JLabel(client+" — "+caseTitle); header.setFont(AMSTheme.FONT_BOLD); header.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel dtL=new JLabel(date+" ["+mode+"]"); dtL.setFont(AMSTheme.FONT_SMALL); dtL.setForeground(AMSTheme.TEXT_MUTED);
        JLabel summL=new JLabel("<html>"+(summary!=null?summary:"")+"</html>"); summL.setFont(AMSTheme.FONT_SMALL); summL.setForeground(AMSTheme.TEXT_SECONDARY);
        JLabel faL=new JLabel("Follow-up: "+(followUp!=null?followUp:"—")); faL.setFont(AMSTheme.FONT_SMALL); faL.setForeground(AMSTheme.INFO);
        left.add(header); left.add(dtL); left.add(summL); left.add(faL);
        card.add(left,BorderLayout.CENTER);

        JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0,0,8,0)); wrap.add(card);
        return wrap;
    }
    private void showAddCommDialog(JPanel list){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(panel),"Log Communication",true);
        d.setSize(500,400); d.setLocationRelativeTo(panel);
        JPanel main=new JPanel(new GridLayout(0,2,12,10));
        main.setBorder(new EmptyBorder(20,24,12,24)); main.setBackground(AMSTheme.BG_MAIN);
        JTextField clientF=tf(),caseF=tf(),durationF=tf();
        JComboBox<String> modeBox=new JComboBox<>(new String[]{"CALL","EMAIL","MEETING","MESSAGE"});
        JTextArea summArea=new JTextArea(3,20); summArea.setFont(AMSTheme.FONT_BODY); summArea.setLineWrap(true);
        JTextField faF=tf();
        main.add(lbl("Client ID")); main.add(clientF);
        main.add(lbl("Case ID")); main.add(caseF);
        main.add(lbl("Mode")); main.add(modeBox);
        main.add(lbl("Duration (mins)")); main.add(durationF);
        main.add(lbl("Follow-up Action")); main.add(faF);
        JPanel summP=new JPanel(new BorderLayout(0,5)); summP.setBorder(new EmptyBorder(0,24,12,24)); summP.setBackground(AMSTheme.BG_MAIN);
        summP.add(lbl("Summary"),BorderLayout.NORTH); summP.add(new JScrollPane(summArea),BorderLayout.CENTER);
        RoundedButton saveBtn=new RoundedButton("Save",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
        saveBtn.addActionListener(e->{
            try{
                final int newId = (int)(System.currentTimeMillis() % 100000) + 5000;
                Connection c=DBConnection.getConnection();
                
                if(c!=null){
                    PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO COMMUNICATION1(com_id,c_id,case_id,a_id,init_date,comm_mode,duration,summary,follow_action,direction) " +
                        "VALUES(comm_seq.NEXTVAL,?,?,?,SYSDATE,?,?,?,?,'OUT')");
                    int cid=0,csid=0; try{cid=Integer.parseInt(clientF.getText());}catch(Exception ignored){}
                    try{csid=Integer.parseInt(caseF.getText());}catch(Exception ignored){}
                    int dur=0; try{dur=Integer.parseInt(durationF.getText());}catch(Exception ignored){}
                    ps.setInt(1,cid); ps.setInt(2,csid); ps.setInt(3,advId);
                    ps.setString(4,(String)modeBox.getSelectedItem()); ps.setInt(5,dur);
                    ps.setString(6,summArea.getText()); ps.setString(7,faF.getText());
                    ps.executeUpdate(); c.commit();
                } else {
                    // Demo mode - add to DemoDataStore
                    String clientName = clientF.getText().isEmpty() ? "Demo Client" : clientF.getText();
                    int caseId = 0;
                    try { caseId = Integer.parseInt(caseF.getText()); } catch (Exception ignored) {}
                    String dateStr = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new java.util.Date());
                    DemoDataStore.addCommToDemo(newId, clientName, caseId, (String)modeBox.getSelectedItem(), 
                        dateStr, summArea.getText(), "OUT");
                }
                JOptionPane.showMessageDialog(d,"Communication logged!"); d.dispose();
                loadComms(list);
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER)); btnRow.setBackground(AMSTheme.BG_MAIN); btnRow.add(saveBtn);
        JPanel content=new JPanel(new BorderLayout()); content.setBackground(AMSTheme.BG_MAIN);
        content.add(main,BorderLayout.NORTH); content.add(summP,BorderLayout.CENTER);
        d.add(content,BorderLayout.CENTER); d.add(btnRow,BorderLayout.SOUTH);
        d.setVisible(true);
    }
    private JTextField tf(){JTextField f=new JTextField();f.setFont(AMSTheme.FONT_BODY);f.setBackground(AMSTheme.BG_INPUT);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xC8D3F0),1,true),new EmptyBorder(5,10,5,10)));return f;}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(AMSTheme.FONT_BOLD);l.setForeground(AMSTheme.TEXT_SECONDARY);return l;}
    public JPanel getPanel(){return panel;}
}
