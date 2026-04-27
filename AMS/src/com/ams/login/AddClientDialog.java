package com.ams.login;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

/**
 * Add/Edit Client Dialog
 */
public class AddClientDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField natIdField;
    private JComboBox<String> typeBox;
    private JTextField cityField;
    private JTextField streetField;
    private JTextField pincodeField;
    private JTextArea notesArea;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private boolean saved = false;
    private Integer clientId = null;
    
    public AddClientDialog(JFrame parent, Integer editClientId) {
        super(parent, editClientId != null ? "Edit Client" : "Add New Client", true);
        this.clientId = editClientId;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 750);
        setLocationRelativeTo(parent);
        initComponents();
        if (editClientId != null) {
            loadClientData(editClientId);
        }
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel titleLabel = new JLabel(clientId != null ? "Edit Client" : "Add New Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(AMSTheme.PRIMARY);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Client Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name *");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridy = 1;
        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameField.setBorder(new LineBorder(new Color(200, 200, 200)));
        nameField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(nameField, gbc);
        
        // Email
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email *");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(emailLabel, gbc);
        
        gbc.gridy = 3;
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBorder(new LineBorder(new Color(200, 200, 200)));
        emailField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridy = 4;
        JLabel phoneLabel = new JLabel("Phone *");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(phoneLabel, gbc);
        
        gbc.gridy = 5;
        phoneField = new JTextField();
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneField.setBorder(new LineBorder(new Color(200, 200, 200)));
        phoneField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(phoneField, gbc);
        
        // National ID
        gbc.gridy = 6;
        JLabel natIdLabel = new JLabel("National ID");
        natIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(natIdLabel, gbc);
        
        gbc.gridy = 7;
        natIdField = new JTextField();
        natIdField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        natIdField.setBorder(new LineBorder(new Color(200, 200, 200)));
        natIdField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(natIdField, gbc);
        
        // Client Type
        gbc.gridy = 8;
        JLabel typeLabel = new JLabel("Client Type");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(typeLabel, gbc);
        
        gbc.gridy = 9;
        typeBox = new JComboBox<>(new String[]{"INDIVIDUAL", "CORPORATE"});
        typeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeBox.setPreferredSize(new Dimension(0, 40));
        formPanel.add(typeBox, gbc);
        
        // City
        gbc.gridy = 10;
        JLabel cityLabel = new JLabel("City");
        cityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(cityLabel, gbc);
        
        gbc.gridy = 11;
        cityField = new JTextField();
        cityField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cityField.setBorder(new LineBorder(new Color(200, 200, 200)));
        cityField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(cityField, gbc);
        
        // Address Street
        gbc.gridy = 12;
        JLabel streetLabel = new JLabel("Street Address");
        streetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(streetLabel, gbc);
        
        gbc.gridy = 13;
        streetField = new JTextField();
        streetField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        streetField.setBorder(new LineBorder(new Color(200, 200, 200)));
        streetField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(streetField, gbc);
        
        // Pincode
        gbc.gridy = 14;
        JLabel pincodeLabel = new JLabel("Pincode");
        pincodeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(pincodeLabel, gbc);
        
        gbc.gridy = 15;
        pincodeField = new JTextField();
        pincodeField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pincodeField.setBorder(new LineBorder(new Color(200, 200, 200)));
        pincodeField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(pincodeField, gbc);
        
        // Notes
        gbc.gridy = 16;
        JLabel notesLabel = new JLabel("Notes");
        notesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(notesLabel, gbc);
        
        gbc.gridy = 17;
        gbc.gridheight = 2;
        notesArea = new JTextArea(3, 30);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setBorder(new LineBorder(new Color(200, 200, 200)));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);
        
        // Username (only for new clients)
        if (clientId == null) {
            gbc.gridheight = 1;
            gbc.gridy = 19;
            JLabel usernameLabel = new JLabel("Username *");
            usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            formPanel.add(usernameLabel, gbc);
            
            gbc.gridy = 20;
            usernameField = new JTextField();
            usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            usernameField.setBorder(new LineBorder(new Color(200, 200, 200)));
            usernameField.setPreferredSize(new Dimension(0, 40));
            formPanel.add(usernameField, gbc);
            
            // Password
            gbc.gridy = 21;
            JLabel passwordLabel = new JLabel("Password *");
            passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            formPanel.add(passwordLabel, gbc);
            
            gbc.gridy = 22;
            passwordField = new JPasswordField();
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            passwordField.setBorder(new LineBorder(new Color(200, 200, 200)));
            passwordField.setPreferredSize(new Dimension(0, 40));
            formPanel.add(passwordField, gbc);
        }
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        saveButton = new RoundedButton("Save Client");
        saveButton.setBackground(AMSTheme.SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveClient());
        
        cancelButton = new RoundedButton("Cancel");
        cancelButton.setBackground(new Color(150, 150, 150));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadClientData(int clientId) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String sql = "SELECT c_name, email, phone, nat_id, cl_type, addr_city, " +
                        "addr_street, addr_pincode, comm_notes FROM CLIENT1 WHERE c_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                nameField.setText(rs.getString("c_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                natIdField.setText(rs.getString("nat_id"));
                typeBox.setSelectedItem(rs.getString("cl_type") != null ? 
                    rs.getString("cl_type") : "INDIVIDUAL");
                cityField.setText(rs.getString("addr_city"));
                streetField.setText(rs.getString("addr_street"));
                pincodeField.setText(rs.getString("addr_pincode") != null ? 
                    rs.getString("addr_pincode") : "");
                notesArea.setText(rs.getString("comm_notes"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading client data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveClient() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields (*)",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (clientId == null && (usernameField == null || usernameField.getText().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Please enter username and password",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (clientId == null) {
                // Insert new client
                String sql = "INSERT INTO CLIENT1 (c_id, c_name, email, phone, nat_id, cl_type, " +
                           "addr_city, addr_street, addr_pincode, comm_notes, username, password_hash, first_contact_dt) " +
                           "VALUES (client_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, natIdField.getText().trim());
                pstmt.setString(5, (String) typeBox.getSelectedItem());
                pstmt.setString(6, cityField.getText().trim());
                pstmt.setString(7, streetField.getText().trim());
                pstmt.setString(8, pincodeField.getText().trim());
                pstmt.setString(9, notesArea.getText().trim());
                pstmt.setString(10, usernameField.getText().trim());
                pstmt.setString(11, new String(passwordField.getPassword()));
                
                pstmt.executeUpdate();
                conn.commit();
                pstmt.close();
            } else {
                // Update existing client
                String sql = "UPDATE CLIENT1 SET c_name=?, email=?, phone=?, nat_id=?, " +
                           "cl_type=?, addr_city=?, addr_street=?, addr_pincode=?, comm_notes=? " +
                           "WHERE c_id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, natIdField.getText().trim());
                pstmt.setString(5, (String) typeBox.getSelectedItem());
                pstmt.setString(6, cityField.getText().trim());
                pstmt.setString(7, streetField.getText().trim());
                pstmt.setString(8, pincodeField.getText().trim());
                pstmt.setString(9, notesArea.getText().trim());
                pstmt.setInt(10, clientId);
                
                pstmt.executeUpdate();
                conn.commit();
                pstmt.close();
            }
            
            saved = true;
            JOptionPane.showMessageDialog(this, 
                clientId == null ? "Client added successfully" : "Client updated successfully",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving client: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}
