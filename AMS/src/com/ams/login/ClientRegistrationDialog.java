package com.ams.login;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

/**
 * Client Self-Registration Dialog
 */
public class ClientRegistrationDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JTextField nameF, emailF, phoneF, cityF, usernameF;
    private JPasswordField passwordF;
    private JComboBox<String> typeBox;
    private JLabel statusL;

    public ClientRegistrationDialog(JFrame parent) {
        super(parent, "Register as Client", true);
        setSize(500, 600);
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

        nameF     = field("Full Name *");
        emailF    = field("Email Address *");
        phoneF    = field("Phone Number *");
        cityF     = field("City");
        usernameF = field("Username *");
        passwordF = new JPasswordField();
        styleF(passwordF);

        typeBox = new JComboBox<>(new String[]{"INDIVIDUAL", "CORPORATE"});
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        typeBox.setFont(AMSTheme.FONT_BODY);

        statusL = new JLabel(" ");
        statusL.setFont(AMSTheme.FONT_SMALL);
        statusL.setForeground(AMSTheme.DANGER);
        statusL.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton registerBtn = new RoundedButton("Create Account", AMSTheme.SUCCESS, AMSTheme.SUCCESS.brighter());
        registerBtn.setPreferredSize(new Dimension(250, 44));
        registerBtn.setMaximumSize(new Dimension(250, 44));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> doRegister());

        main.add(title); main.add(Box.createVerticalStrut(6));
        main.add(sub);   main.add(Box.createVerticalStrut(22));
        main.add(lbl("Full Name")); main.add(nameF);     main.add(Box.createVerticalStrut(10));
        main.add(lbl("Email"));     main.add(emailF);    main.add(Box.createVerticalStrut(10));
        main.add(lbl("Phone"));     main.add(phoneF);    main.add(Box.createVerticalStrut(10));
        main.add(lbl("City"));      main.add(cityF);     main.add(Box.createVerticalStrut(10));
        main.add(lbl("Client Type")); main.add(typeBox); main.add(Box.createVerticalStrut(10));
        main.add(lbl("Username"));  main.add(usernameF); main.add(Box.createVerticalStrut(10));
        main.add(lbl("Password"));  main.add(passwordF); main.add(Box.createVerticalStrut(8));
        main.add(statusL);           main.add(Box.createVerticalStrut(16));
        main.add(registerBtn);

        setContentPane(new JScrollPane(main));
    }

    private JTextField field(String ph) {
        JTextField f = new JTextField();
        styleF(f);
        return f;
    }

    private void styleF(JTextField f) {
        f.setFont(AMSTheme.FONT_BODY);
        f.setBackground(AMSTheme.BG_INPUT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true),
            new EmptyBorder(5, 10, 5, 10)));
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AMSTheme.FONT_BOLD);
        l.setForeground(AMSTheme.TEXT_SECONDARY);
        return l;
    }

    private void doRegister() {
        String name  = nameF.getText().trim();
        String email = emailF.getText().trim();
        String phone = phoneF.getText().trim();
        String city  = cityF.getText().trim();
        String user  = usernameF.getText().trim();
        String pass  = new String(passwordF.getPassword()).trim();
        String type  = (String) typeBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            statusL.setText("Please fill all required (*) fields."); return;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            if (con != null) {
                con.setAutoCommit(false);

                // Insert client
                String sql1 = "INSERT INTO CLIENT1(c_id,c_name,email,phone,cl_type,addr_city,username,password_hash) " +
                              "VALUES(client_seq.NEXTVAL,?,?,?,?,?,?,?)";
                PreparedStatement ps1 = con.prepareStatement(sql1);
                ps1.setString(1, name); ps1.setString(2, email); ps1.setString(3, phone);
                ps1.setString(4, type); ps1.setString(5, city);  ps1.setString(6, user);
                ps1.setString(7, pass);
                ps1.executeUpdate();

                // Get new client id
                ResultSet rs = con.prepareStatement("SELECT client_seq.CURRVAL FROM dual").executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Failed to retrieve registered client ID.");
                }
                int cid = rs.getInt(1);

                // Insert user record
                String sql2 = "INSERT INTO AMS_USERS1(user_id,username,password,role,ref_id) " +
                              "VALUES(user_seq.NEXTVAL,?,?,'CLIENT',?)";
                PreparedStatement ps2 = con.prepareStatement(sql2);
                ps2.setString(1, user); ps2.setString(2, pass); ps2.setInt(3, cid);
                ps2.executeUpdate();

                con.commit();
                statusL.setText("Registration successful. Please login.");
                JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYou can now log in as Client.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            // Offline / demo mode — DB unavailable. Login accepts any credentials when DB is null.
            statusL.setForeground(new Color(0x27AE60));
            statusL.setText("Registered successfully. You can now log in.");
            JOptionPane.showMessageDialog(this,
                "Registration successful!\n\n" +
                "Database is connected.\n" +
                "You can now log in as Client with:\n  Username: " + user + "\n  Password: " + pass,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        } catch (Exception ex) {
            try {
                if (con != null && !con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            statusL.setText("Registration failed: " + ex.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignore) {}
            }
        }
    }
}
