package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.DemoDataStore;
import com.ams.util.DocumentViewerUtil;
import com.ams.components.RoundedButton;
import com.ams.components.LabeledField;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Evidence Management Page — Add, View, Edit, Delete evidence with file attachments */
public class EvidencePage {

    private int advId;
    private JPanel panel;
    private DefaultTableModel model;

    private static final int COL_EID  = 0;
    private static final int COL_PATH = 8; // hidden column for file path

    public EvidencePage(int advId) { this.advId = advId; build(); }

    private void build() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(AMSTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("\uD83D\uDD0D  Evidence Management");
        title.setFont(AMSTheme.FONT_TITLE);
        title.setForeground(AMSTheme.PRIMARY);

        String[] cols = {"E-ID","Case ID","Type","Source","File Name","Collected","Admissibility","Location","FilePath"};
        model = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildTable();

        // Hide the file path column (col 8)
        table.getColumnModel().getColumn(COL_PATH).setMinWidth(0);
        table.getColumnModel().getColumn(COL_PATH).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_PATH).setWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E8F8), 1));

        // Action buttons
        RoundedButton addBtn     = btn("+ Add Evidence",  AMSTheme.SUCCESS,            AMSTheme.SUCCESS.brighter());
        RoundedButton viewBtn    = btn("\uD83D\uDC41 View File",  AMSTheme.PRIMARY,            AMSTheme.PRIMARY_LIGHT);
        RoundedButton editBtn    = btn("\u270F Edit",     new Color(0xF39C12),          new Color(0xE67E22));
        RoundedButton delBtn     = btn("\uD83D\uDDD1 Delete",   new Color(0xE74C3C),          new Color(0xC0392B));
        RoundedButton refreshBtn = btn("\u27F3 Refresh",  new Color(0x6C757D),          new Color(0x5A6268));

        addBtn.addActionListener(e -> showAddDialog(table));
        viewBtn.addActionListener(e -> viewFile(table));
        editBtn.addActionListener(e -> showEditDialog(table));
        delBtn.addActionListener(e -> deleteSelected(table));
        refreshBtn.addActionListener(e -> loadData());

        // Double-click to view file
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewFile(table);
            }
        });

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        leftBtns.setOpaque(false);
        leftBtns.add(addBtn); leftBtns.add(viewBtn); leftBtns.add(editBtn); leftBtns.add(delBtn);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightBtns.setOpaque(false);
        rightBtns.add(refreshBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(leftBtns, BorderLayout.WEST);
        south.add(rightBtns, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(sp,    BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        loadData();
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    private void loadData() {
        model.setRowCount(0);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    Connection c = DBConnection.getConnection();
                    if (c == null) { addDemo(); return null; }
                    PreparedStatement ps = c.prepareStatement(
                        "SELECT e.e_id, e.case_id, e.e_type, e.e_source, e.file_path, " +
                        "TO_CHAR(e.collected_date,'DD-MON-YYYY'), e.admissibility, e.e_location " +
                        "FROM EVIDENCE1 e JOIN ADVOCATE_CASE1 ac ON e.case_id=ac.case_id " +
                        "WHERE ac.a_id=? ORDER BY e.collected_date DESC");
                    ps.setInt(1, advId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String fp   = rs.getString(5) != null ? rs.getString(5) : "";
                        String name = fp.isEmpty() ? "\u2014" : new File(fp).getName();
                        final Object[] row = {
                            rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                            name, rs.getString(6), rs.getString(7), rs.getString(8), fp
                        };
                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                } catch (Exception e) { addDemo(); }
                return null;
            }
        }.execute();
    }

    private void addDemo() {
        SwingUtilities.invokeLater(() -> {
            for (Map<String, Object> e : DemoDataStore.getEvidenceList()) {
                model.addRow(new Object[]{
                    e.get("id"), e.get("caseId"), e.get("type"), e.get("source"),
                    "demo_file.pdf", e.get("date"), e.get("status"), "Court Room A", ""
                });
            }
        });
    }

    // ── Add Evidence Dialog ────────────────────────────────────────────────────

    private void showAddDialog(JTable table) {
        JDialog d = new JDialog((Window) SwingUtilities.getWindowAncestor(panel),
            "Add Evidence", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(680, 540);
        d.setLocationRelativeTo(panel);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setBorder(new EmptyBorder(20, 24, 12, 24));
        form.setBackground(AMSTheme.BG_MAIN);

        JComboBox<String> caseBox = new JComboBox<>();
        caseBox.setFont(AMSTheme.FONT_BODY);
        loadCases(caseBox);

        JComboBox<String> typeBox  = new JComboBox<>(new String[]{"DOCUMENT","IMAGE","VIDEO","AUDIO","PHYSICAL","OTHER"});
        JComboBox<String> admitBox = new JComboBox<>(new String[]{"PENDING","ADMISSIBLE","INADMISSIBLE"});
        typeBox.setFont(AMSTheme.FONT_BODY); admitBox.setFont(AMSTheme.FONT_BODY);

        LabeledField sourceF   = new LabeledField("Source / Origin", 50);
        LabeledField locationF = new LabeledField("Storage Location", 30);
        LabeledField dateF     = new LabeledField("Collected Date (YYYY-MM-DD)", 15);
        dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        JTextArea descArea = new JTextArea(3, 40);
        descArea.setFont(AMSTheme.FONT_BODY); descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1),
            new EmptyBorder(6, 10, 6, 10)));

        final File[] chosen = {null};
        JLabel fileLabel = grayLabel("No file selected");
        RoundedButton selectFileBtn = btn("Select File \uD83D\uDCC2", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        selectFileBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setAcceptAllFileFilterUsed(true);
            fc.setDialogTitle("Select Evidence File");
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                chosen[0] = fc.getSelectedFile();
                fileLabel.setText(chosen[0].getName() + " (" + chosen[0].length() / 1024 + " KB)");
            }
        });

        form.add(comboPanel("Case", caseBox));     form.add(comboPanel("Evidence Type", typeBox));
        form.add(sourceF);                          form.add(locationF);
        form.add(dateF);                            form.add(comboPanel("Admissibility", admitBox));
        form.add(areaPanel("Description", descArea)); form.add(fileRowPanel("Attach File", selectFileBtn, fileLabel));

        RoundedButton saveBtn   = btn("Save Evidence", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton cancelBtn = btn("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));

        saveBtn.addActionListener(ev -> {
            String sel = (String) caseBox.getSelectedItem();
            if (sel == null || sel.trim().isEmpty()) { JOptionPane.showMessageDialog(d, "Please select a case."); return; }
            int cid;
            try { cid = Integer.parseInt(sel.split(":")[0].trim()); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Invalid case."); return; }
            saveBtn.setEnabled(false); saveBtn.setText("Saving…");
            final int fCid = cid;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    String dest = copyFile(chosen[0], fCid, "evidence");
                    Connection c = DBConnection.getConnection();
                    if (c == null) return false;
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO EVIDENCE1(e_id,case_id,e_type,e_source,e_desc,collected_date," +
                        "admissibility,verified_by,e_location,file_path,submitted_court) " +
                        "VALUES(evidence_seq.NEXTVAL,?,?,?,?,TO_DATE(?,'YYYY-MM-DD'),?,?,?,?,0)");
                    ps.setInt(1, fCid);
                    ps.setString(2, (String) typeBox.getSelectedItem());
                    ps.setString(3, sourceF.getText());
                    ps.setString(4, descArea.getText().trim());
                    ps.setString(5, dateF.getText().trim());
                    ps.setString(6, (String) admitBox.getSelectedItem());
                    ps.setInt(7, advId);
                    ps.setString(8, locationF.getText());
                    ps.setString(9, dest);
                    ps.executeUpdate(); c.commit(); return true;
                }
                @Override protected void done() {
                    try {
                        if (get()) { JOptionPane.showMessageDialog(d, "Evidence saved successfully!"); d.dispose(); loadData(); }
                        else JOptionPane.showMessageDialog(d, "Database unavailable \u2014 evidence not saved.");
                    } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage()); }
                    saveBtn.setEnabled(true); saveBtn.setText("Save Evidence");
                }
            }.execute();
        });
        cancelBtn.addActionListener(e -> d.dispose());

        d.add(new JScrollPane(form), BorderLayout.CENTER);
        d.add(btnRow(cancelBtn, saveBtn), BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── View File ──────────────────────────────────────────────────────────────

    private void viewFile(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a record first."); return; }
        String path = (String) model.getValueAt(row, COL_PATH);
        if (path == null || path.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No file is attached to this evidence record.");
            return;
        }
        if (!new File(path).exists()) {
            JOptionPane.showMessageDialog(panel, "File not found on disk:\n" + path);
            return;
        }
        boolean ok = DocumentViewerUtil.openFileFromPath(path);
        if (!ok) JOptionPane.showMessageDialog(panel, "Could not open:\n" + path + "\nTry opening it manually.");
    }

    // ── Edit Dialog ────────────────────────────────────────────────────────────

    private void showEditDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a record to edit."); return; }
        int eid = (int) model.getValueAt(row, COL_EID);

        JDialog d = new JDialog((Window) SwingUtilities.getWindowAncestor(panel),
            "Edit Evidence #" + eid, Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(660, 500);
        d.setLocationRelativeTo(panel);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setBorder(new EmptyBorder(20, 24, 12, 24));
        form.setBackground(AMSTheme.BG_MAIN);

        JComboBox<String> typeBox  = new JComboBox<>(new String[]{"DOCUMENT","IMAGE","VIDEO","AUDIO","PHYSICAL","OTHER"});
        JComboBox<String> admitBox = new JComboBox<>(new String[]{"PENDING","ADMISSIBLE","INADMISSIBLE"});
        typeBox.setFont(AMSTheme.FONT_BODY); admitBox.setFont(AMSTheme.FONT_BODY);

        LabeledField sourceF   = new LabeledField("Source", 50);
        LabeledField locationF = new LabeledField("Location", 30);
        LabeledField dateF     = new LabeledField("Collected Date (YYYY-MM-DD)", 15);
        JTextArea descArea = new JTextArea(3, 40);
        descArea.setFont(AMSTheme.FONT_BODY); descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1), new EmptyBorder(6, 10, 6, 10)));

        final File[] newFile     = {null};
        final String[] curPath   = {""};
        JLabel fileLabel = grayLabel("No replacement selected");
        RoundedButton replaceBtn = btn("Replace File", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        replaceBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(true);
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                newFile[0] = fc.getSelectedFile();
                fileLabel.setText(newFile[0].getName());
            }
        });

        // Load current values
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement(
                    "SELECT e_type,e_source,e_desc,TO_CHAR(collected_date,'YYYY-MM-DD')," +
                    "admissibility,e_location,file_path FROM EVIDENCE1 WHERE e_id=?");
                ps.setInt(1, eid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    typeBox.setSelectedItem(rs.getString(1));
                    sourceF.setText(rs.getString(2) != null ? rs.getString(2) : "");
                    descArea.setText(rs.getString(3) != null ? rs.getString(3) : "");
                    dateF.setText(rs.getString(4) != null ? rs.getString(4) : "");
                    admitBox.setSelectedItem(rs.getString(5));
                    locationF.setText(rs.getString(6) != null ? rs.getString(6) : "");
                    curPath[0] = rs.getString(7) != null ? rs.getString(7) : "";
                    if (!curPath[0].isEmpty()) fileLabel.setText("Current: " + new File(curPath[0]).getName());
                }
            }
        } catch (Exception ignored) {}

        form.add(comboPanel("Evidence Type", typeBox)); form.add(sourceF);
        form.add(locationF);                             form.add(dateF);
        form.add(comboPanel("Admissibility", admitBox));form.add(new JLabel());
        form.add(areaPanel("Description", descArea));   form.add(fileRowPanel("Attached File", replaceBtn, fileLabel));

        RoundedButton saveBtn   = btn("Save Changes", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        RoundedButton cancelBtn = btn("Cancel", new Color(0x95A5A6), new Color(0xBDC3C7));

        saveBtn.addActionListener(ev -> {
            saveBtn.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    String dest = newFile[0] != null ? copyFile(newFile[0], eid, "edit") : curPath[0];
                    Connection c = DBConnection.getConnection(); if (c == null) return false;
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE EVIDENCE1 SET e_type=?,e_source=?,e_desc=?," +
                        "collected_date=TO_DATE(?,'YYYY-MM-DD'),admissibility=?,e_location=?,file_path=? WHERE e_id=?");
                    ps.setString(1, (String) typeBox.getSelectedItem());
                    ps.setString(2, sourceF.getText()); ps.setString(3, descArea.getText().trim());
                    ps.setString(4, dateF.getText().trim()); ps.setString(5, (String) admitBox.getSelectedItem());
                    ps.setString(6, locationF.getText()); ps.setString(7, dest); ps.setInt(8, eid);
                    ps.executeUpdate(); c.commit(); return true;
                }
                @Override protected void done() {
                    try {
                        if (get()) { JOptionPane.showMessageDialog(d, "Evidence updated!"); d.dispose(); loadData(); }
                        else JOptionPane.showMessageDialog(d, "Database unavailable.");
                    } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage()); }
                    saveBtn.setEnabled(true);
                }
            }.execute();
        });
        cancelBtn.addActionListener(e -> d.dispose());

        d.add(new JScrollPane(form), BorderLayout.CENTER);
        d.add(btnRow(cancelBtn, saveBtn), BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    private void deleteSelected(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a record to delete."); return; }
        int    eid  = (int)    model.getValueAt(row, COL_EID);
        String path = (String) model.getValueAt(row, COL_PATH);

        int ok = JOptionPane.showConfirmDialog(panel,
            "Delete Evidence #" + eid + "? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Connection c = DBConnection.getConnection(); if (c == null) return false;
                c.prepareStatement("DELETE FROM EVIDENCE_ACCESS1 WHERE e_id=" + eid).executeUpdate();
                c.prepareStatement("DELETE FROM EVIDENCE1 WHERE e_id=" + eid).executeUpdate();
                c.commit();
                if (path != null && !path.isEmpty()) try { new File(path).delete(); } catch (Exception ignored) {}
                return true;
            }
            @Override protected void done() {
                try {
                    if (get()) loadData();
                    else JOptionPane.showMessageDialog(panel, "Database unavailable.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage()); }
            }
        }.execute();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void loadCases(JComboBox<String> box) {
        try {
            Connection c = DBConnection.getConnection();
            if (c != null) {
                PreparedStatement ps = c.prepareStatement(
                    "SELECT cs.case_id, cs.c_title FROM CASES1 cs JOIN ADVOCATE_CASE1 ac ON cs.case_id=ac.case_id " +
                    "WHERE ac.a_id=? ORDER BY cs.case_id DESC");
                ps.setInt(1, advId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) box.addItem(rs.getInt(1) + ": " + rs.getString(2));
            }
        } catch (Exception ignored) {}
        if (box.getItemCount() == 0) box.addItem("1001: Demo Case");
    }

    private String copyFile(File src, int refId, String prefix) throws Exception {
        if (src == null) return "";
        File dir = new File("evidence_files"); dir.mkdirs();
        String fn = src.getName();
        String ext = fn.contains(".") ? fn.substring(fn.lastIndexOf('.')) : "";
        File dest = new File(dir, prefix + "_" + refId + "_" + System.currentTimeMillis() + ext);
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getAbsolutePath();
    }

    private JTable buildTable() {
        JTable t = new JTable(model);
        t.setRowHeight(34); t.setFont(AMSTheme.FONT_BODY);
        t.setBackground(Color.WHITE); t.setGridColor(new Color(0xEEF2FF));
        t.setSelectionBackground(new Color(0xDCEAFF));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(AMSTheme.FONT_BOLD);
        t.getTableHeader().setBackground(new Color(0xF0F4FF));
        t.getTableHeader().setForeground(AMSTheme.PRIMARY);
        return t;
    }

    private RoundedButton btn(String text, Color c1, Color c2) {
        RoundedButton b = new RoundedButton(text, c1, c2);
        b.setPreferredSize(new Dimension(140, 36));
        return b;
    }

    private JLabel grayLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(AMSTheme.FONT_SMALL); l.setForeground(AMSTheme.TEXT_MUTED); return l;
    }

    private JPanel comboPanel(String label, JComboBox<?> box) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(AMSTheme.FONT_BOLD); l.setForeground(AMSTheme.TEXT_SECONDARY);
        p.add(l, BorderLayout.NORTH); p.add(box, BorderLayout.CENTER); return p;
    }

    private JPanel areaPanel(String label, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(AMSTheme.FONT_BOLD); l.setForeground(AMSTheme.TEXT_SECONDARY);
        p.add(l, BorderLayout.NORTH); p.add(new JScrollPane(area), BorderLayout.CENTER); return p;
    }

    private JPanel fileRowPanel(String label, RoundedButton btn, JLabel info) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(AMSTheme.FONT_BOLD); l.setForeground(AMSTheme.TEXT_SECONDARY);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); row.setOpaque(false);
        row.add(btn); row.add(info);
        p.add(l, BorderLayout.NORTH); p.add(row, BorderLayout.CENTER); return p;
    }

    private JPanel btnRow(RoundedButton cancel, RoundedButton save) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10)); p.setOpaque(false);
        p.add(cancel); p.add(save); return p;
    }

    public JPanel getPanel() { return panel; }
}
