package com.ams.components;

import com.ams.util.FileUploadDownloadUtil;
import com.ams.util.AMSTheme;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Photo Upload Dialog for Advocate, Junior, and Staff Profiles
 */
public class PhotoUploadDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JLabel photoPreviewLabel;
    private JButton uploadButton;
    private JButton removeButton;
    private JButton okButton;
    private JButton cancelButton;
    private String uploadedPhotoPath;
    private BufferedImage previewImage;
    private boolean confirmed = false;
    
    public PhotoUploadDialog(JFrame parent, String title) {
        super(parent, title, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(parent);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(AMSTheme.BG_SIDEBAR);
        topPanel.setPreferredSize(new Dimension(0, 40));
        JLabel titleLabel = new JLabel("Upload Profile Photo");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(AMSTheme.TEXT_LIGHT);
        topPanel.add(titleLabel);
        
        // Center panel with preview
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        centerPanel.setBackground(Color.WHITE);
        
        // Photo preview area
        photoPreviewLabel = new JLabel();
        photoPreviewLabel.setIcon(createDefaultPhotoIcon());
        photoPreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        photoPreviewLabel.setVerticalAlignment(JLabel.CENTER);
        photoPreviewLabel.setBorder(new LineBorder(AMSTheme.ACCENT, 2));
        photoPreviewLabel.setPreferredSize(new Dimension(250, 250));
        photoPreviewLabel.setBackground(new Color(240, 240, 240));
        photoPreviewLabel.setOpaque(true);
        
        JPanel previewWrapper = new JPanel(new GridBagLayout());
        previewWrapper.setBackground(Color.WHITE);
        previewWrapper.add(photoPreviewLabel);
        
        centerPanel.add(previewWrapper, BorderLayout.CENTER);
        
        // Info label
        JLabel infoLabel = new JLabel("<html>Supported formats: JPG, PNG<br>Max size: 5MB</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        centerPanel.add(infoLabel, BorderLayout.SOUTH);
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(240, 240, 240));
        
        uploadButton = new RoundedButton("Choose Photo");
        uploadButton.setBackground(AMSTheme.ACCENT);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.addActionListener(e -> choosePhoto());
        
        removeButton = new RoundedButton("Remove Photo");
        removeButton.setBackground(new Color(200, 50, 50));
        removeButton.setForeground(Color.WHITE);
        removeButton.addActionListener(e -> removePhoto());
        
        okButton = new RoundedButton("OK");
        okButton.setBackground(new Color(50, 150, 50));
        okButton.setForeground(Color.WHITE);
        okButton.addActionListener(e -> confirmUpload());
        
        cancelButton = new RoundedButton("Cancel");
        cancelButton.setBackground(new Color(100, 100, 100));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());
        
        bottomPanel.add(uploadButton);
        bottomPanel.add(removeButton);
        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void choosePhoto() {
        String photoPath = FileUploadDownloadUtil.choosePhotoFile(this);
        if (photoPath != null) {
            uploadedPhotoPath = photoPath;
            try {
                previewImage = ImageIO.read(new File(photoPath));
                // Scale image for preview
                int maxWidth = 250;
                int maxHeight = 250;
                double scale = Math.min((double) maxWidth / previewImage.getWidth(),
                                       (double) maxHeight / previewImage.getHeight());
                int newWidth = (int) (previewImage.getWidth() * scale);
                int newHeight = (int) (previewImage.getHeight() * scale);
                
                BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, 
                    BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.drawImage(previewImage, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                
                photoPreviewLabel.setIcon(new ImageIcon(scaledImage));
                photoPreviewLabel.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading image: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removePhoto() {
        uploadedPhotoPath = null;
        previewImage = null;
        photoPreviewLabel.setIcon(createDefaultPhotoIcon());
        photoPreviewLabel.setText("");
        JOptionPane.showMessageDialog(this, "Photo removed", "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void confirmUpload() {
        confirmed = true;
        dispose();
    }
    
    private ImageIcon createDefaultPhotoIcon() {
        // Create a default user icon
        BufferedImage img = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(230, 230, 230));
        g.fillRect(0, 0, 250, 250);
        
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        g.drawString("📷", 100, 130);
        
        g.dispose();
        return new ImageIcon(img);
    }
    
    public String getUploadedPhotoPath() {
        return uploadedPhotoPath;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
