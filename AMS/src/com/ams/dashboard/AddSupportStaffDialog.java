package com.ams.dashboard;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;

/**
 * Add/Edit Support Staff Dialog
 */
public class AddSupportStaffDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField jobTitleField;
    private JTextField cityField;
    private JTextField pincodeField;
    private JTextField stateField;
    private JTextField hoursField;
    private JTextField emergencyContactField;
    private JTextField salaryField;
    private JSpinner dobSpinner;
    private JSpinner joinSpinner;
    private JComboBox<String> roleTypeBox;
    private JTextArea departmentArea;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private boolean saved = false;
    private Integer staffId = null;
    
    public AddSupportStaffDialog(JFrame parent, Integer editStaffId) {
        super(parent, editStaffId != null ? "Edit Support Staff" : "Add New Support Staff", true);
        this.staffId = editStaffId;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(650, 900);
        setLocationRelativeTo(parent);
        initComponents();
        if (editStaffId != null) {
            loadStaffData(editStaffId);
        }
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel titleLabel = new JLabel(staffId != null ? "Edit Support Staff" : "Add New Support Staff");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(AMSTheme.PRIMARY);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Staff Name
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel nameLabel = new JLabel("Full Name *");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridy = row++;
        nameField = createTextField();
        formPanel.add(nameField, gbc);
        
        // Phone
        gbc.gridy = row++;
        JLabel phoneLabel = new JLabel("Phone *");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(phoneLabel, gbc);
        
        gbc.gridy = row++;
        phoneField = createTextField();
        formPanel.add(phoneField, gbc);
        
        // Job Title
        gbc.gridy = row++;
        JLabel jobLabel = new JLabel("Job Title *");
        jobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(jobLabel, gbc);
        
        gbc.gridy = row++;
        jobTitleField = createTextField();
        formPanel.add(jobTitleField, gbc);
        
        // Role Type
        gbc.gridy = row++;
        JLabel roleLabel = new JLabel("Role Type *");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(roleLabel, gbc);
        
        gbc.gridy = row++;
        roleTypeBox = new JComboBox<>(new String[]{"ADMIN", "SUPPORT"});
        roleTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleTypeBox.setPreferredSize(new Dimension(0, 40));
        formPanel.add(roleTypeBox, gbc);
        
        // City
        gbc.gridy = row++;
        JLabel cityLabel = new JLabel("City");
        cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(cityLabel, gbc);
        
        gbc.gridy = row++;
        cityField = createTextField();
        formPanel.add(cityField, gbc);
        
        // Pincode
        gbc.gridy = row++;
        JLabel pincodeLabel = new JLabel("Pincode");
        pincodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(pincodeLabel, gbc);
        
        gbc.gridy = row++;
        pincodeField = createTextField();
        formPanel.add(pincodeField, gbc);
        
        // State
        gbc.gridy = row++;
        JLabel stateLabel = new JLabel("State");
        stateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(stateLabel, gbc);
        
        gbc.gridy = row++;
        stateField = createTextField();
        formPanel.add(stateField, gbc);
        
        // Work Hours
        gbc.gridy = row++;
        JLabel hoursLabel = new JLabel("Work Hours per Day");
        hoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(hoursLabel, gbc);
        
        gbc.gridy = row++;
        hoursField = createTextField();
        hoursField.setText("8");
        formPanel.add(hoursField, gbc);
        
        // Emergency Contact
        gbc.gridy = row++;
        JLabel emergencyLabel = new JLabel("Emergency Contact");
        emergencyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(emergencyLabel, gbc);
        
        gbc.gridy = row++;
        emergencyContactField = createTextField();
        formPanel.add(emergencyContactField, gbc);
        
        // Salary
        gbc.gridy = row++;
        JLabel salaryLabel = new JLabel("Salary");
        salaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(salaryLabel, gbc);
        
        gbc.gridy = row++;
        salaryField = createTextField();
        formPanel.add(salaryField, gbc);
        
        // Date of Birth
        gbc.gridy = row++;
        JLabel dobLabel = new JLabel("Date of Birth");
        dobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(dobLabel, gbc);
        
        gbc.gridy = row++;
        dobSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, 
            java.util.Calendar.YEAR));
        JSpinner.DateEditor dobEditor = new JSpinner.DateEditor(dobSpinner, "yyyy-MM-dd");
        dobSpinner.setEditor(dobEditor);
        dobSpinner.setPreferredSize(new Dimension(0, 40));
        formPanel.add(dobSpinner, gbc);
        
        // Join Date
        gbc.gridy = row++;
        JLabel joinLabel = new JLabel("Join Date *");
        joinLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(joinLabel, gbc);
        
        gbc.gridy = row++;
        joinSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, 
            java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor joinEditor = new JSpinner.DateEditor(joinSpinner, "yyyy-MM-dd");
        joinSpinner.setEditor(joinEditor);
        joinSpinner.setPreferredSize(new Dimension(0, 40));
        formPanel.add(joinSpinner, gbc);
        
        // Department/Responsibilities
        gbc.gridy = row++;
        JLabel deptLabel = new JLabel("Department/Responsibilities");
        deptLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(deptLabel, gbc);
        
        gbc.gridy = row++;
        gbc.gridheight = 2;
        departmentArea = new JTextArea(4, 30);
        departmentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        departmentArea.setBorder(new LineBorder(new Color(200, 200, 200)));
        departmentArea.setLineWrap(true);
        departmentArea.setWrapStyleWord(true);
        JScrollPane deptScroll = new JScrollPane(departmentArea);
        formPanel.add(deptScroll, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        saveButton = new RoundedButton("Save Staff");
        saveButton.setBackground(AMSTheme.SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveStaff());
        
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
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        field.setPreferredSize(new Dimension(0, 40));
        return field;
    }
    
    private void loadStaffData(int staffId) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String sql = "SELECT st_name, phone, job_title, addr_city, addr_pincode, addr_state, " +
                        "work_hours, emergency_contact, salary, dob, join_date, role_type FROM STAFF1 WHERE st_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                nameField.setText(rs.getString("st_name"));
                phoneField.setText(rs.getString("phone"));
                jobTitleField.setText(rs.getString("job_title"));
                cityField.setText(rs.getString("addr_city"));
                pincodeField.setText(String.valueOf(rs.getInt("addr_pincode")));
                stateField.setText(rs.getString("addr_state"));
                hoursField.setText(String.valueOf(rs.getInt("work_hours")));
                emergencyContactField.setText(rs.getString("emergency_contact"));
                salaryField.setText(String.valueOf(rs.getDouble("salary")));
                
                if (rs.getDate("dob") != null) {
                    dobSpinner.setValue(rs.getDate("dob"));
                }
                if (rs.getDate("join_date") != null) {
                    joinSpinner.setValue(rs.getDate("join_date"));
                }
                
                roleTypeBox.setSelectedItem(rs.getString("role_type"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading staff data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveStaff() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String jobTitle = jobTitleField.getText().trim();
        
        if (name.isEmpty() || phone.isEmpty() || jobTitle.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields (*)",
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
            
            if (staffId == null) {
                // Insert new staff
                String sql = "INSERT INTO STAFF1 (st_id, st_name, phone, job_title, addr_city, " +
                           "addr_pincode, addr_state, work_hours, emergency_contact, salary, dob, join_date, role_type) " +
                           "VALUES (staff_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, jobTitle);
                pstmt.setString(4, cityField.getText().trim());
                pstmt.setInt(5, Integer.parseInt(pincodeField.getText().trim().isEmpty() ? "0" : pincodeField.getText()));
                pstmt.setString(6, stateField.getText().trim());
                pstmt.setInt(7, Integer.parseInt(hoursField.getText().trim().isEmpty() ? "8" : hoursField.getText()));
                pstmt.setString(8, emergencyContactField.getText().trim());
                pstmt.setDouble(9, Double.parseDouble(salaryField.getText().trim().isEmpty() ? "0" : salaryField.getText()));
                
                Date dob = (Date) dobSpinner.getValue();
                pstmt.setDate(10, new java.sql.Date(dob.getTime()));
                
                Date joinDate = (Date) joinSpinner.getValue();
                pstmt.setDate(11, new java.sql.Date(joinDate.getTime()));
                
                pstmt.setString(12, (String) roleTypeBox.getSelectedItem());
                
                pstmt.executeUpdate();
                conn.commit();
                pstmt.close();
            } else {
                // Update existing staff
                String sql = "UPDATE STAFF1 SET st_name=?, phone=?, job_title=?, addr_city=?, " +
                           "addr_pincode=?, addr_state=?, work_hours=?, emergency_contact=?, " +
                           "salary=?, dob=?, join_date=?, role_type=? WHERE st_id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, jobTitle);
                pstmt.setString(4, cityField.getText().trim());
                pstmt.setInt(5, Integer.parseInt(pincodeField.getText().trim().isEmpty() ? "0" : pincodeField.getText()));
                pstmt.setString(6, stateField.getText().trim());
                pstmt.setInt(7, Integer.parseInt(hoursField.getText().trim().isEmpty() ? "8" : hoursField.getText()));
                pstmt.setString(8, emergencyContactField.getText().trim());
                pstmt.setDouble(9, Double.parseDouble(salaryField.getText().trim().isEmpty() ? "0" : salaryField.getText()));
                
                Date dob = (Date) dobSpinner.getValue();
                pstmt.setDate(10, new java.sql.Date(dob.getTime()));
                
                Date joinDate = (Date) joinSpinner.getValue();
                pstmt.setDate(11, new java.sql.Date(joinDate.getTime()));
                
                pstmt.setString(12, (String) roleTypeBox.getSelectedItem());
                pstmt.setInt(13, staffId);
                
                pstmt.executeUpdate();
                conn.commit();
                pstmt.close();
            }
            
            saved = true;
            JOptionPane.showMessageDialog(this,
                staffId == null ? "Staff added successfully" : "Staff updated successfully",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for hours and salary",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving staff: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}
