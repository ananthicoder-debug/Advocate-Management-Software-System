package com.ams.advocate;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;

/**
 * Add Timeline Event Dialog for Case Journey
 */
public class AddTimelineEventDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JTextField eventTitleField;
    private JComboBox<String> eventTypeBox;
    private JSpinner eventDateSpinner;
    private JTextField eventTimeField;
    private JTextArea eventDescriptionArea;
    private JComboBox<String> eventStatusBox;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private int caseId;
    private boolean saved = false;
    
    public AddTimelineEventDialog(JFrame parent, int caseId) {
        super(parent, "Add Timeline Event", true);
        this.caseId = caseId;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(parent);
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel titleLabel = new JLabel("Add Timeline Event");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(AMSTheme.PRIMARY);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Event Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel2 = new JLabel("Event Title *");
        titleLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(titleLabel2, gbc);
        
        gbc.gridy = 1;
        eventTitleField = new JTextField();
        eventTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventTitleField.setBorder(new LineBorder(new Color(200, 200, 200)));
        eventTitleField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(eventTitleField, gbc);
        
        // Event Type
        gbc.gridy = 2;
        JLabel typeLabel = new JLabel("Event Type *");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(typeLabel, gbc);
        
        gbc.gridy = 3;
        eventTypeBox = new JComboBox<>(new String[]{
            "HEARING", "FILING", "MEETING", "SUBMISSION", "CLIENT_MEETING", "OTHER"
        });
        eventTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventTypeBox.setPreferredSize(new Dimension(0, 35));
        formPanel.add(eventTypeBox, gbc);
        
        // Event Date
        gbc.gridy = 4;
        JLabel dateLabel = new JLabel("Event Date *");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(dateLabel, gbc);
        
        gbc.gridy = 5;
        eventDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, 
            java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(eventDateSpinner, "yyyy-MM-dd");
        eventDateSpinner.setEditor(dateEditor);
        eventDateSpinner.setPreferredSize(new Dimension(0, 35));
        formPanel.add(eventDateSpinner, gbc);
        
        // Event Time
        gbc.gridy = 6;
        JLabel timeLabel = new JLabel("Event Time (HH:MM)");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(timeLabel, gbc);
        
        gbc.gridy = 7;
        eventTimeField = new JTextField("09:00");
        eventTimeField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventTimeField.setBorder(new LineBorder(new Color(200, 200, 200)));
        eventTimeField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(eventTimeField, gbc);
        
        // Event Status
        gbc.gridy = 8;
        JLabel statusLabel = new JLabel("Event Status");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(statusLabel, gbc);
        
        gbc.gridy = 9;
        eventStatusBox = new JComboBox<>(new String[]{
            "SCHEDULED", "COMPLETED", "CANCELLED"
        });
        eventStatusBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventStatusBox.setPreferredSize(new Dimension(0, 35));
        formPanel.add(eventStatusBox, gbc);
        
        // Event Description
        gbc.gridy = 10;
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(descLabel, gbc);
        
        gbc.gridy = 11;
        gbc.gridheight = 2;
        eventDescriptionArea = new JTextArea(4, 30);
        eventDescriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventDescriptionArea.setBorder(new LineBorder(new Color(200, 200, 200)));
        eventDescriptionArea.setLineWrap(true);
        eventDescriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(eventDescriptionArea);
        formPanel.add(descScroll, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        saveButton = new RoundedButton("Save Event");
        saveButton.setBackground(AMSTheme.SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveEvent());
        
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
    
    private void saveEvent() {
        String eventTitle = eventTitleField.getText().trim();
        
        if (eventTitle.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter event title", 
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
            
            String eventType = (String) eventTypeBox.getSelectedItem();
            String eventStatus = (String) eventStatusBox.getSelectedItem();
            Date selectedDate = (Date) eventDateSpinner.getValue();
            String eventTime = eventTimeField.getText().trim();
            String description = eventDescriptionArea.getText().trim();
            
            String sql = "INSERT INTO TIMELINE1 " +
                "(time_id, case_id, entry_date, title, e_data, progress_sum, next_step, status_indicator) " +
                "VALUES (timeline_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, caseId);
            pstmt.setDate(2, new java.sql.Date(selectedDate.getTime()));
            pstmt.setString(3, eventTitle);
            pstmt.setString(4, description);
            pstmt.setString(5, eventType);
            pstmt.setString(6, eventTime);
            pstmt.setString(7, eventStatus);
            
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            
            saved = true;
            JOptionPane.showMessageDialog(this, "Timeline event added successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving event: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}
