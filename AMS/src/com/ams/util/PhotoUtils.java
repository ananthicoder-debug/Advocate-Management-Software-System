package com.ams.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Photo Processing Utility for AMS
 * Handles photo loading, scaling, and display
 */
public class PhotoUtils {

    public static final int PROFILE_PHOTO_WIDTH = 150;
    public static final int PROFILE_PHOTO_HEIGHT = 150;
    public static final int THUMBNAIL_SIZE = 100;

    /**
     * Load image from file and scale to specified size
     * @param file Image file
     * @param width Target width
     * @param height Target height
     * @return ImageIcon or null if failed
     */
    public static ImageIcon loadAndScaleImage(File file, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) return null;
            
            // Scale image
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load image from byte array and scale
     * @param imageBytes Byte array of image data
     * @param width Target width
     * @param height Target height
     * @return ImageIcon or null if failed
     */
    public static ImageIcon loadImageFromBytes(byte[] imageBytes, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) return null;
            
            // Scale image
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            System.err.println("Error loading image from bytes: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create a circular image icon for profile photos
     * @param imageIcon Original image icon
     * @return Circular image icon
     */
    public static ImageIcon makeCircularImage(ImageIcon imageIcon) {
        if (imageIcon == null) return getDefaultProfilePhoto();
        
        Image img = imageIcon.getImage();
        int size = PROFILE_PHOTO_WIDTH;
        
        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        
        // White background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size, size);
        
        // Draw circular clip
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(img, 0, 0, size, size, null);
        
        // Create circle border
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(0xE0E0E0));
        g2.drawOval(0, 0, size - 1, size - 1);
        
        g2.dispose();
        return new ImageIcon(bufferedImage);
    }

    /**
     * Get default profile photo placeholder
     * @return Default profile photo icon
     */
    public static ImageIcon getDefaultProfilePhoto() {
        int size = PROFILE_PHOTO_WIDTH;
        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        
        // Light gray background
        g2.setColor(new Color(0xF0F0F0));
        g2.fillRect(0, 0, size, size);
        
        // Border
        g2.setColor(new Color(0xD0D0D0));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(0, 0, size - 1, size - 1);
        
        // Person icon (simple)
        g2.setColor(new Color(0x999999));
        g2.fillOval(size / 4, size / 6, size / 2, size / 3);
        g2.fillArc(size / 8, size / 2, 3 * size / 4, size / 2, 0, 180);
        
        g2.dispose();
        return new ImageIcon(bufferedImage);
    }

    /**
     * Create profile photo panel with upload button
     * @param currentPhoto Current photo icon or null
     * @param uploadListener Action listener for upload button
     * @return JPanel containing photo and upload button
     */
    public static JPanel createProfilePhotoPanel(ImageIcon currentPhoto, 
            java.awt.event.ActionListener uploadListener) {
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // Photo label
        JLabel photoLabel = new JLabel(currentPhoto != null ? currentPhoto : getDefaultProfilePhoto());
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(0xD0D0D0), 2));
        
        // Upload button
        JButton uploadBtn = new JButton("📷 Upload Photo");
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadBtn.setPreferredSize(new Dimension(150, 36));
        uploadBtn.setMaximumSize(new Dimension(150, 36));
        uploadBtn.addActionListener(uploadListener);
        
        panel.add(photoLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(uploadBtn);
        
        return panel;
    }

    /**
     * Convert image to byte array
     * @param image BufferedImage to convert
     * @return Byte array of image data
     * @throws IOException If conversion fails
     */
    public static byte[] imageToBytes(BufferedImage image) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    /**
     * Resize image to specified dimensions
     * @param image Original image
     * @param width Target width
     * @param height Target height
     * @return Resized BufferedImage
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(image, 0, 0, width, height, null);
        g2.dispose();
        return resized;
    }

    /**
     * Compress image to reduce file size
     * @param file Original image file
     * @param quality Compression quality (0.0 to 1.0)
     * @return Byte array of compressed image
     * @throws IOException If compression fails
     */
    public static byte[] compressImage(File file, float quality) throws IOException {
        BufferedImage image = ImageIO.read(file);
        BufferedImage compressed = resizeImage(image, PROFILE_PHOTO_WIDTH, PROFILE_PHOTO_HEIGHT);
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(compressed, "jpg", baos);
        return baos.toByteArray();
    }
}
