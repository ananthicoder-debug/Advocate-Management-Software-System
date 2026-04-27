package com.ams.dashboard;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.FileUploadDownloadUtil;
import com.ams.util.PhotoUtils;
import com.ams.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

/**
 * Junior Advocate Dashboard
 */
public class JuniorDashboard extends BaseDashboard {

    private int jaId;
    private String jaName = "Junior Advocate";

    public JuniorDashboard(int jaId) {
        super("Junior Advocate", loadJAName(jaId), "JUNIOR_ADVOCATE", jaId);
        this.jaId   = jaId;
        this.jaName = currentUser;
    }

    private static String loadJAName(int id) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                ResultSet rs = c.prepareStatement("SELECT ja_name FROM JUNIOR_ADVOCATE1 WHERE ja_id="+id).executeQuery();
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return "Junior Advocate";
    }

    @Override
    protected void buildPages() {
        addSidebarSection("Overview");
        addNavItem("Dashboard",     "\uD83C\uDFE0", "home");
        addNavItem("Profile",       "\uD83D\uDC64", "profile");

        addSidebarSection("Tasks");
        addNavItem("My Tasks",      "\uD83D\uDCCB", "tasks");
        addNavItem("Assigned Cases","\uD83D\uDCBC", "cases");

        addSidebarSection("Clients");
        addNavItem("My Clients",    "\uD83D\uDC65", "clients");

        addPage("home",    buildHomePage());
        addPage("profile", buildProfilePage());
        addPage("tasks",   buildTasksPage());
        addPage("cases",   buildAssignedCasesPage());
        addPage("clients", buildClientsPage());
    }

    // ── Home ──────────────────────────────────────────────────────────────────
    private JScrollPane buildHomePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28,30,28,30));

        JLabel greeting = new JLabel("Welcome,  " + jaName + " \uD83D\uDC4B");
        greeting.setFont(new Font("Segoe UI",Font.BOLD,26));
        greeting.setForeground(AMSTheme.TEXT_PRIMARY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Here are your pending tasks and assignments.");
        sub.setFont(AMSTheme.FONT_BODY); sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Summary cards
        DashboardCard tasksCard = new DashboardCard("My Tasks","—","\uD83D\uDCCB",AMSTheme.CARD_BLUE_1,AMSTheme.CARD_BLUE_2,()->showPage("tasks"));
        DashboardCard casesCard = new DashboardCard("Assigned Cases","—","\uD83D\uDCBC",AMSTheme.CARD_GREEN_1,AMSTheme.CARD_GREEN_2,()->showPage("cases"));
        DashboardCard clientsCard = new DashboardCard("Clients","—","\uD83D\uDC65",AMSTheme.CARD_GOLD_1,AMSTheme.CARD_GOLD_2,()->showPage("clients"));

        loadJACount(tasksCard, casesCard, clientsCard);

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT,16,8));
        cardsRow.setOpaque(false); cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.add(tasksCard); cardsRow.add(casesCard); cardsRow.add(clientsCard);

        // Recent tasks list
        JLabel taskTitle = sectionTitle("My Recent Tasks");
        taskTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel taskList = new JPanel(); taskList.setLayout(new BoxLayout(taskList,BoxLayout.Y_AXIS));
        taskList.setBackground(AMSTheme.BG_MAIN); taskList.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadRecentTasks(taskList);
        JScrollPane taskScroll = new JScrollPane(taskList);
        taskScroll.setBorder(null); taskScroll.setPreferredSize(new Dimension(0,300));
        taskScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(greeting); page.add(Box.createVerticalStrut(4));
        page.add(sub); page.add(Box.createVerticalStrut(24));
        page.add(cardsRow); page.add(Box.createVerticalStrut(24));
        page.add(taskTitle); page.add(taskScroll);

        return scrollWrap(page);
    }

    private void loadJACount(DashboardCard tc, DashboardCard cc, DashboardCard cl) {
        new SwingWorker<int[],Void>(){
            @Override protected int[] doInBackground(){
                int[] counts={0,0,0};
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null)return counts;
                    ResultSet r=c.prepareStatement("SELECT COUNT(*) FROM TASK_ASSIGNMENT1 WHERE assigned_to="+jaId+" AND status='PENDING'").executeQuery();
                    if(r.next())counts[0]=r.getInt(1);
                    r=c.prepareStatement("SELECT COUNT(*) FROM JUNIOR_CASE1 WHERE ja_id="+jaId).executeQuery();
                    if(r.next())counts[1]=r.getInt(1);
                    r=c.prepareStatement("SELECT COUNT(DISTINCT cs.c_id) FROM CASES1 cs JOIN JUNIOR_CASE1 jc ON cs.case_id=jc.case_id WHERE jc.ja_id="+jaId).executeQuery();
                    if(r.next())counts[2]=r.getInt(1);
                }catch(Exception ignored){}
                return counts;
            }
            @Override protected void done(){
                try{int[] c=get(); tc.setValue(""+c[0]); cc.setValue(""+c[1]); cl.setValue(""+c[2]);}catch(Exception ignored){}
            }
        }.execute();
    }

    private void loadRecentTasks(JPanel taskList) {
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){addDemoTasks(taskList);return null;}
                    ResultSet rs=c.prepareStatement(
                        "SELECT t.task_id,t.task_type,SUBSTR(t.task_desc,1,100),TO_CHAR(t.due_date,'DD-MON-YYYY'),t.status,a.a_name " +
                        "FROM TASK_ASSIGNMENT1 t JOIN ADVOCATE1 a ON t.assigned_by=a.a_id " +
                        "WHERE t.assigned_to="+jaId+" ORDER BY t.assigned_dt DESC").executeQuery();
                    while(rs.next()){
                        final int tid=rs.getInt(1); final String type=rs.getString(2),desc=rs.getString(3),
                            due=rs.getString(4),stat=rs.getString(5),adv=rs.getString(6);
                        SwingUtilities.invokeLater(()->taskList.add(buildTaskCard(tid,type,desc,due,stat,adv)));
                    }
                }catch(Exception e){addDemoTasks(taskList);}
                return null;
            }
            @Override protected void done(){taskList.revalidate();taskList.repaint(); SwingUtilities.getWindowAncestor(taskList).repaint();}
        }.execute();
    }

    private void addDemoTasks(JPanel list){
        SwingUtilities.invokeLater(()->{
            list.add(buildTaskCard(5001,"CLIENT_INFO","Collect client information from Arun Patel regarding witnesses","28-DEC-2024","PENDING","Adv. Rajesh Kumar"));
            list.add(buildTaskCard(5002,"REPORT","Prepare case summary report for property dispute","05-JAN-2025","PENDING","Adv. Rajesh Kumar"));
            list.add(buildTaskCard(5003,"RESEARCH","Research precedents for criminal alibi defense","20-DEC-2024","COMPLETED","Adv. Rajesh Kumar"));
        });
    }

    private JPanel buildTaskCard(int taskId,String type,String desc,String due,String status,String assignedBy){
        boolean done="COMPLETED".equals(status)||"APPROVED".equals(status);
        boolean hasAttachment = checkTaskHasAttachment(taskId);
        Color statColor=done?AMSTheme.SUCCESS:hasAttachment?new Color(0xFF8C00):AMSTheme.WARNING; // Orange for has attachment
        
        JPanel card=new JPanel(new BorderLayout(12,4)){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = done ? new Color(0xF0FFF4) : hasAttachment ? new Color(0xFFF8F0) : AMSTheme.BG_CARD;
                g2.setColor(bgColor);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(statColor); 
                g2.fillRoundRect(0,0,5,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(12,18,12,18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,110)); card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left=new JPanel(new GridLayout(4,1,0,3)); left.setOpaque(false);
        JLabel typeL=new JLabel("["+type+"] Task #"+taskId); typeL.setFont(AMSTheme.FONT_BOLD); typeL.setForeground(AMSTheme.PRIMARY);
        JLabel descL=new JLabel("<html>"+(desc!=null?desc:"")+"</html>"); descL.setFont(AMSTheme.FONT_BODY); descL.setForeground(AMSTheme.TEXT_PRIMARY);
        JLabel dueL=new JLabel("Due: "+due+" | By: "+assignedBy); dueL.setFont(AMSTheme.FONT_SMALL); dueL.setForeground(AMSTheme.TEXT_MUTED);
        String statusText = done ? status : hasAttachment ? "READY TO SUBMIT" : "PENDING ATTACHMENT";
        JLabel statL=new JLabel(statusText); statL.setFont(new Font("Segoe UI",Font.BOLD,11)); statL.setForeground(statColor);
        left.add(typeL); left.add(descL); left.add(dueL); left.add(statL);

        JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); btns.setOpaque(false);
        if(!done){
            if(!hasAttachment){
                RoundedButton attachBtn=new RoundedButton("📎 Attach Files",new Color(0xFF8C00),new Color(0xFFA500));
                attachBtn.setPreferredSize(new Dimension(120,34));
                attachBtn.addActionListener(e->attachFilesToTask(taskId, card));
                btns.add(attachBtn);
            } else {
                RoundedButton finishBtn=new RoundedButton("✅ Submit Task",AMSTheme.SUCCESS,AMSTheme.SUCCESS.brighter());
                finishBtn.setPreferredSize(new Dimension(120,34));
                finishBtn.addActionListener(e->markTaskDone(taskId));
                btns.add(finishBtn);
            }
        } else {
            JLabel doneL=new JLabel("✓ "+status); doneL.setFont(AMSTheme.FONT_BOLD); doneL.setForeground(AMSTheme.SUCCESS);
            btns.add(doneL);
        }

        card.add(left,BorderLayout.CENTER); card.add(btns,BorderLayout.EAST);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0,0,8,0)); wrap.add(card);
        return wrap;
    }

    private void markTaskDone(int taskId){
        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                c.prepareStatement("UPDATE TASK_ASSIGNMENT1 SET status='COMPLETED',completed_dt=SYSDATE WHERE task_id="+taskId).executeUpdate();
                c.commit();
            }
            JOptionPane.showMessageDialog(this,"Task #"+taskId+" submitted successfully! Awaiting advocate approval.");
            // Refresh the tasks page
            showPage("tasks");
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Error: "+e.getMessage());}
    }

    private boolean checkTaskHasAttachment(int taskId) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement("SELECT submitted_file FROM TASK_ASSIGNMENT1 WHERE task_id = ?");
                ps.setInt(1, taskId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String filePath = rs.getString(1);
                    return filePath != null && !filePath.trim().isEmpty();
                }
            }
        } catch (Exception e) {
            // Silently handle database errors
        }
        return false;
    }

    private void attachFilesToTask(int taskId, JPanel taskCard) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Files/Folders to Attach to Task #" + taskId);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length > 0) {
                // Create a directory for task attachments
                File taskDir = new File("junior_task_attachments");
                taskDir.mkdirs();

                // Create subdirectory for this task
                File taskSubDir = new File(taskDir, "task_" + taskId);
                taskSubDir.mkdirs();

                StringBuilder attachmentPaths = new StringBuilder();

                try {
                    // Copy selected files/folders to task directory
                    for (File sourceFile : selectedFiles) {
                        if (sourceFile.isDirectory()) {
                            // Copy entire directory
                            File destDir = new File(taskSubDir, sourceFile.getName());
                            copyDirectory(sourceFile, destDir);
                            if (attachmentPaths.length() > 0) attachmentPaths.append(";");
                            attachmentPaths.append(destDir.getAbsolutePath());
                        } else {
                            // Copy single file
                            File destFile = new File(taskSubDir, sourceFile.getName());
                            java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            if (attachmentPaths.length() > 0) attachmentPaths.append(";");
                            attachmentPaths.append(destFile.getAbsolutePath());
                        }
                    }

                    // Update database with attachment paths
                    Connection c = DBConnection.getConnection();
                    if (c != null) {
                        PreparedStatement ps = c.prepareStatement("UPDATE TASK_ASSIGNMENT1 SET submitted_file = ? WHERE task_id = ?");
                        ps.setString(1, attachmentPaths.toString());
                        ps.setInt(2, taskId);
                        ps.executeUpdate();
                        c.commit();

                        JOptionPane.showMessageDialog(this, 
                            "Files attached successfully! You can now submit the task.", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);

                        // Refresh the tasks page to show updated status
                        showPage("tasks");
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error attaching files: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void copyDirectory(File sourceDir, File destDir) throws java.io.IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    copyDirectory(file, new File(destDir, file.getName()));
                } else {
                    java.nio.file.Files.copy(file.toPath(), 
                        new File(destDir, file.getName()).toPath(), 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    private JScrollPane buildProfilePage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("Junior Advocate Profile");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoLabel = new JLabel(PhotoUtils.getDefaultProfilePhoto());
        photoLabel.setPreferredSize(new Dimension(120,120));
        photoLabel.setBorder(new EmptyBorder(12,12,12,12));

        RoundedButton uploadBtn = new RoundedButton("Upload Photo", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        uploadBtn.setPreferredSize(new Dimension(140,36));
        uploadBtn.addActionListener(e -> uploadJuniorPhoto(photoLabel));

        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setOpaque(false);
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        photoPanel.add(uploadBtn, BorderLayout.SOUTH);

        JPanel card=cardPanel(new GridLayout(0,2,12,10));
        card.setBorder(new EmptyBorder(20,20,20,20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,220)); card.setAlignmentX(Component.LEFT_ALIGNMENT);

        try{
            Connection c=DBConnection.getConnection();
            if(c!=null){
                ResultSet rs=c.prepareStatement("SELECT ja_name,email,mobile,qualification,department,work_status,desk_location,photo_path FROM JUNIOR_ADVOCATE1 WHERE ja_id="+jaId).executeQuery();
                if(rs.next()){
                    String photoPath = rs.getString(8);
                    card.add(il("Name",rs.getString(1))); card.add(il("Email",rs.getString(2)));
                    card.add(il("Mobile",rs.getString(3))); card.add(il("Qualification",rs.getString(4)));
                    card.add(il("Department",rs.getString(5))); card.add(il("Status",rs.getString(6)));
                    card.add(il("Desk",rs.getString(7)));
                    if(photoPath != null && !photoPath.trim().isEmpty()){
                        File f = new File(photoPath);
                        if(f.exists()) photoLabel.setIcon(PhotoUtils.loadAndScaleImage(f,120,120));
                    }
                }
            } else {
                card.add(il("Name",jaName)); card.add(il("Role","Junior Advocate"));
            }
        }catch(Exception e){card.add(il("Name",jaName));}

        JPanel header=new JPanel(new BorderLayout(20,0));
        header.setOpaque(false);
        header.add(photoPanel, BorderLayout.WEST);
        header.add(card, BorderLayout.CENTER);

        page.add(title); page.add(Box.createVerticalStrut(20)); page.add(header);
        return scrollWrap(page);
    }

    private void uploadJuniorPhoto(JLabel photoLabel) {
        String path = FileUploadDownloadUtil.choosePhotoFile(this);
        if (path == null) return;
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement("UPDATE JUNIOR_ADVOCATE1 SET photo_path=? WHERE ja_id=?");
                ps.setString(1, path);
                ps.setInt(2, jaId);
                ps.executeUpdate();
                c.commit();
                File photoFile = new File(path);
                photoLabel.setIcon(PhotoUtils.loadAndScaleImage(photoFile, 120, 120));
                JOptionPane.showMessageDialog(this, "Photo uploaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save photo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel il(String k,String v){JPanel p=new JPanel(new BorderLayout());p.setOpaque(false);JLabel kl=new JLabel(k+": ");kl.setFont(AMSTheme.FONT_BOLD);kl.setForeground(AMSTheme.TEXT_SECONDARY);JLabel vl=new JLabel(v!=null?v:"—");vl.setFont(AMSTheme.FONT_BODY);vl.setForeground(AMSTheme.TEXT_PRIMARY);p.add(kl,BorderLayout.WEST);p.add(vl,BorderLayout.CENTER);return p;}

    // ── Tasks Page ────────────────────────────────────────────────────────────
    private JScrollPane buildTasksPage(){
        JPanel page=new JPanel(); page.setLayout(new BoxLayout(page,BoxLayout.Y_AXIS));
        page.setBackground(AMSTheme.BG_MAIN); page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("\uD83D\uDCCB  My Tasks");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sort options
        JPanel sortRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8)); sortRow.setOpaque(false); sortRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<String> sortBox=new JComboBox<>(new String[]{"All Tasks","PENDING","COMPLETED","APPROVED"});
        sortBox.setFont(AMSTheme.FONT_BODY);
        JLabel sortLbl=new JLabel("Filter: "); sortLbl.setFont(AMSTheme.FONT_BOLD); sortLbl.setForeground(AMSTheme.TEXT_SECONDARY);
        sortRow.add(sortLbl); sortRow.add(sortBox);

        JPanel taskList=new JPanel(); taskList.setLayout(new BoxLayout(taskList,BoxLayout.Y_AXIS));
        taskList.setBackground(AMSTheme.BG_MAIN); taskList.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadRecentTasks(taskList);

        sortBox.addActionListener(e->{taskList.removeAll();loadRecentTasks(taskList);});

        page.add(title); page.add(Box.createVerticalStrut(8));
        page.add(sortRow); page.add(Box.createVerticalStrut(12));
        page.add(taskList);
        return scrollWrap(page);
    }

    // ── Cases Page ────────────────────────────────────────────────────────────
    private JScrollPane buildAssignedCasesPage(){
        JPanel page=new JPanel(new BorderLayout()); page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("\uD83D\uDCBC  Assigned Cases");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        String[] cols={"Case ID","Title","Client","Type","Status","Priority","Granted By"};
        DefaultTableModel model=new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model); table.setRowHeight(34); table.setFont(AMSTheme.FONT_BODY);
        table.setBackground(Color.WHITE); table.setGridColor(new Color(0xEEF2FF));
        table.setSelectionBackground(new Color(0xDCEAFF));
        table.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(0xF0F4FF)); table.getTableHeader().setForeground(AMSTheme.PRIMARY);

        JScrollPane sp=new JScrollPane(table); sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));

        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){
                        SwingUtilities.invokeLater(()->{
                            model.addRow(new Object[]{1001,"State vs Rajan","Arun Patel","CRIMINAL","ACTIVE",1,"Adv. Rajesh"});
                        });
                        return null;
                    }
                    ResultSet rs=c.prepareStatement(
                        "SELECT cs.case_id,cs.c_title,cl.c_name,cs.c_type,cs.status,cs.priority_level,a.a_name " +
                        "FROM CASES1 cs JOIN JUNIOR_CASE1 jc ON cs.case_id=jc.case_id " +
                        "JOIN CLIENT1 cl ON cs.c_id=cl.c_id " +
                        "JOIN ADVOCATE1 a ON jc.granted_by=a.a_id " +
                        "WHERE jc.ja_id="+jaId).executeQuery();
                    while(rs.next()){
                        final Object[] row={rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getInt(6),rs.getString(7)};
                        SwingUtilities.invokeLater(()->model.addRow(row));
                    }
                }catch(Exception e){
                    SwingUtilities.invokeLater(()->{model.addRow(new Object[]{1001,"State vs Rajan","Arun Patel","CRIMINAL","ACTIVE",1,"Adv. Rajesh"});});
                }
                return null;
            }
        }.execute();

        page.add(title,BorderLayout.NORTH); page.add(sp,BorderLayout.CENTER);
        return scrollWrap(page);
    }

    // ── Clients Page ──────────────────────────────────────────────────────────
    private JScrollPane buildClientsPage(){
        JPanel page=new JPanel(new BorderLayout()); page.setBackground(AMSTheme.BG_MAIN);
        page.setBorder(new EmptyBorder(28,30,28,30));
        JLabel title=new JLabel("\uD83D\uDC65  My Clients");
        title.setFont(AMSTheme.FONT_TITLE); title.setForeground(AMSTheme.PRIMARY);

        String[] cols={"Client ID","Name","Email","Phone","Case Title"};
        DefaultTableModel model=new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model); table.setRowHeight(34); table.setFont(AMSTheme.FONT_BODY);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(0xF0F4FF)); table.getTableHeader().setForeground(AMSTheme.PRIMARY);

        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground(){
                try{
                    Connection c=DBConnection.getConnection();
                    if(c==null){
                        SwingUtilities.invokeLater(()->{model.addRow(new Object[]{1,"Arun Patel","arun@gmail.com","9111111111","State vs Rajan"});});
                        return null;
                    }
                    ResultSet rs=c.prepareStatement(
                        "SELECT DISTINCT cl.c_id,cl.c_name,cl.email,cl.phone,cs.c_title " +
                        "FROM CLIENT1 cl JOIN CASES1 cs ON cl.c_id=cs.c_id " +
                        "JOIN JUNIOR_CASE1 jc ON cs.case_id=jc.case_id WHERE jc.ja_id="+jaId).executeQuery();
                    while(rs.next()){
                        final Object[] row={rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)};
                        SwingUtilities.invokeLater(()->model.addRow(row));
                    }
                }catch(Exception e){SwingUtilities.invokeLater(()->{model.addRow(new Object[]{1,"Arun Patel","arun@gmail.com","9111111111","Demo Case"});});}
                return null;
            }
        }.execute();

        JScrollPane sp=new JScrollPane(table); sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8),1));
        page.add(title,BorderLayout.NORTH); page.add(sp,BorderLayout.CENTER);
        return scrollWrap(page);
    }
}
