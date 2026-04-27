package com.ams.login;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.util.FileUploadUtil;
import com.ams.util.PhotoUtils;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;

/**
 * Client Self-Registration Dialog with Photo Upload
 */
public class ClientRegistrationDialogV2 extends JDialog {

    private static final long serialVersionUID = 1L;

    private JTextField nameF, emailF, phoneF, cityF, usernameF;
    private JPasswordField passwordF;
    private JComboBox<String> typeBox;
    private JLabel statusL, photoLabel;
    private JButton uploadPhotoBtn;
    private byte[] photoData;
    @SuppressWarnings("unused")
    private File selectedPhotoFile;

    public ClientRegistrationDialogV2(JFrame parent) {
        super(parent, "Register as Client", true);
        setSize(550, 750);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(AMSTheme.BG_MAIN);
        main.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title
        JLabel title = new JLabel("Client Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(AMSTheme.PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Create your account to file cases");
        sub.setFont(AMSTheme.FONT_SMALL);
        sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Scroll pane for form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(AMSTheme.BG_MAIN);

        // Photo upload section
        JPanel photoPanel = createPhotoPanel();
        formPanel.add(photoPanel);
        formPanel.add(Box.createVerticalStrut(16));

        // Form fields
        nameF     = field("Full Name *");
        emailF    = field("Email Address *");
        phoneF    = field("Phone Number *");
        cityF     = field("City");

        JLabel typeLabel = new JLabel("Client Type");
        typeLabel.setFont(AMSTheme.FONT_BOLD);
        typeLabel.setForeground(AMSTheme.TEXT_SECONDARY);
        typeBox = new JComboBox<>(new String[]{"INDIVIDUAL", "CORPORATE"});
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        typeBox.setFont(AMSTheme.FONT_BODY);
        typeBox.setBackground(AMSTheme.BG_INPUT);
        typeBox.setForeground(AMSTheme.TEXT_PRIMARY);

        usernameF = field("Username *");
        passwordF = new JPasswordField();
        styleF(passwordF);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(AMSTheme.FONT_BOLD);
        passLabel.setForeground(AMSTheme.TEXT_SECONDARY);

        // Status message
        statusL = new JLabel(" ");
        statusL.setFont(AMSTheme.FONT_SMALL);
        statusL.setForeground(AMSTheme.DANGER);
        statusL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Submit button
        RoundedButton submitBtn = new RoundedButton("Register", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.setPreferredSize(new Dimension(250, 44));
        submitBtn.setMaximumSize(new Dimension(250, 44));
        submitBtn.addActionListener(e -> doRegister());

        // Add fields to form
        formPanel.add(makeLabel("Full Name"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(nameF);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(makeLabel("Email Address"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(emailF);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(makeLabel("Phone Number"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(phoneF);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(makeLabel("City"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(cityF);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(typeLabel);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(typeBox);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(makeLabel("Username"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(usernameF);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(passLabel);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(passwordF);
        formPanel.add(Box.createVerticalStrut(16));

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        main.add(title);
        main.add(Box.createVerticalStrut(4));
        main.add(sub);
        main.add(Box.createVerticalStrut(20));
        main.add(scrollPane);
        main.add(Box.createVerticalStrut(12));
        main.add(statusL);
        main.add(Box.createVerticalStrut(12));
        main.add(submitBtn);

        setContentPane(main);
    }

    private JPanel createPhotoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AMSTheme.BG_MAIN);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoTitle = new JLabel("📷 Profile Photo (Optional)");
        photoTitle.setFont(AMSTheme.FONT_BOLD);
        photoTitle.setForeground(AMSTheme.TEXT_SECONDARY);
        photoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(120, 120));
        photoLabel.setMaximumSize(new Dimension(120, 120));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(0xD0D0D0), 2));
        photoLabel.setIcon(PhotoUtils.getDefaultProfilePhoto());
        photoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        uploadPhotoBtn = new JButton("Choose Photo");
        uploadPhotoBtn.setPreferredSize(new Dimension(120, 36));
        uploadPhotoBtn.setMaximumSize(new Dimension(120, 36));
        uploadPhotoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadPhotoBtn.addActionListener(e -> selectPhoto());

        panel.add(photoTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(photoLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(uploadPhotoBtn);

        return panel;
    }

    private void selectPhoto() {
        File file = FileUploadUtil.selectPhotoFile(this);
        if (file != null) {
            selectedPhotoFile = file;
            try {
                photoData = FileUploadUtil.readFileAsBytes(file);
                ImageIcon icon = PhotoUtils.loadAndScaleImage(file, 120, 120);
                photoLabel.setIcon(icon);
                uploadPhotoBtn.setText("Change Photo");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading photo: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doRegister() {
        String name = nameF.getText().trim();
        String email = emailF.getText().trim();
        String phone = phoneF.getText().trim();
        String city = cityF.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        String username = usernameF.getText().trim();
        String password = new String(passwordF.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            statusL.setText("⚠  Please fill all required fields (*) ");
            statusL.setForeground(AMSTheme.WARNING);
            return;
        }

        if (!email.contains("@")) {
            statusL.setText("⚠  Invalid email format");
            statusL.setForeground(AMSTheme.WARNING);
            return;
        }

        statusL.setText("Registering...");
        statusL.setForeground(AMSTheme.INFO);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                Connection con = null;
                try {
                    con = DBConnection.getConnection();
                    if (con == null) return false;
                    con.setAutoCommit(false);

                    // Insert into CLIENT table
                    String sql1 = "INSERT INTO CLIENT1(c_id, c_name, email, phone, cl_type, addr_city, photo_data) " +
                                  "VALUES(client_seq.NEXTVAL, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps1 = con.prepareStatement(sql1);
                    ps1.setString(1, name);
                    ps1.setString(2, email);
                    ps1.setString(3, phone);
                    ps1.setString(4, type);
                    ps1.setString(5, city);
                    ps1.setBytes(6, photoData);
                    ps1.executeUpdate();

                    // Get generated client ID (Oracle uses sequence CURRVAL after NEXTVAL in session)
                    ResultSet rs = con.createStatement().executeQuery("SELECT client_seq.CURRVAL FROM dual");
                    int clientId = 0;
                    if (rs.next()) {
                        clientId = rs.getInt(1);
                    }

                    // Insert into AMS_USERS table
                    String sql2 = "INSERT INTO AMS_USERS1(user_id, username, password, role, ref_id, is_active, created_dt) " +
                                  "VALUES(user_seq.NEXTVAL, ?, ?, 'CLIENT', ?, 1, SYSDATE)";
                    PreparedStatement ps2 = con.prepareStatement(sql2);
                    ps2.setString(1, username);
                    ps2.setString(2, password);
                    ps2.setInt(3, clientId);
                    ps2.executeUpdate();

                    con.commit();
                    return true;
                } catch (SQLException e) {
                    if (con != null) {
                        try {
                            con.rollback();
                        } catch (SQLException rb) {
                            System.err.println("Rollback failed: " + rb.getMessage());
                        }
                    }
                    System.err.println("Registration error: " + e.getMessage());
                    return false;
                } finally {
                    if (con != null) {
                        try {
                            con.setAutoCommit(false);
                        } catch (SQLException ignored) {
                        }
                    }
                }
            }

            @Override protected void done() {
                try {
                    if (get()) {
                        statusL.setText("✓ Registration successful! You can now login.");
                        statusL.setForeground(AMSTheme.SUCCESS);
                        JOptionPane.showMessageDialog(ClientRegistrationDialogV2.this,
                            "Registration successful!\nYou can now login with your credentials.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        statusL.setText(" Registration successful.");
                        statusL.setForeground(AMSTheme.DANGER);
                    }
                } catch (Exception ex) {
                    statusL.setText("✗ Error: " + ex.getMessage());
                    statusL.setForeground(AMSTheme.DANGER);
                }
            }
        }.execute();
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AMSTheme.FONT_BOLD);
        l.setForeground(AMSTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field(String label) {
        JTextField f = new JTextField();
        styleF(f);
        return f;
    }

    private void styleF(JTextField f) {
        f.setFont(AMSTheme.FONT_BODY);
        f.setBackground(AMSTheme.BG_INPUT);
        f.setForeground(AMSTheme.TEXT_PRIMARY);
        f.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1),
            new EmptyBorder(6, 12, 6, 12)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
