package com.ams.util;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.awt.Component;

/**
 * File Upload Utility for AMS
 * Handles file selection, validation, and processing
 */
public class FileUploadUtil {

    // File size limits (in MB)
    public static final int PHOTO_MAX_SIZE_MB = 5;
    public static final int DOCUMENT_MAX_SIZE_MB = 50;

    /**
     * Open file chooser for photo selection
     * @param parent Parent component
     * @return Selected file or null if cancelled
     */
    public static File selectPhotoFile(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Photo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        // Add filters for image files
        javax.swing.filechooser.FileFilter imageFilter = 
            new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", 
                "jpg", "jpeg", "png", "gif", "bmp");
        chooser.addChoosableFileFilter(imageFilter);
        
        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            
            // Validate file size
            long fileSizeMB = file.length() / (1024 * 1024);
            if (fileSizeMB > PHOTO_MAX_SIZE_MB) {
                JOptionPane.showMessageDialog(parent, 
                    "Photo size must be less than " + PHOTO_MAX_SIZE_MB + " MB", 
                    "File Too Large", 
                    JOptionPane.WARNING_MESSAGE);
                return null;
            }
            
            return file;
        }
        return null;
    }

    /**
     * Open file chooser for document selection
     * @param parent Parent component
     * @return Selected file or null if cancelled
     */
    public static File selectDocumentFile(JComponent parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Document");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);
        
        // Add filters for common document types
        javax.swing.filechooser.FileFilter docFilter = 
            new javax.swing.filechooser.FileNameExtensionFilter(
                "Documents (*.pdf, *.doc, *.docx, *.txt, *.xls, *.xlsx, *.jpg, *.png)", 
                "pdf", "doc", "docx", "txt", "xls", "xlsx", "jpg", "png", "gif", "bmp");
        chooser.addChoosableFileFilter(docFilter);
        
        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            
            // Validate file size
            long fileSizeMB = file.length() / (1024 * 1024);
            if (fileSizeMB > DOCUMENT_MAX_SIZE_MB) {
                JOptionPane.showMessageDialog(parent, 
                    "Document size must be less than " + DOCUMENT_MAX_SIZE_MB + " MB", 
                    "File Too Large", 
                    JOptionPane.WARNING_MESSAGE);
                return null;
            }
            
            return file;
        }
        return null;
    }

    /**
     * Read file as byte array
     * @param file File to read
     * @return Byte array of file contents
     * @throws IOException If file cannot be read
     */
    public static byte[] readFileAsBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Write byte array to file
     * @param file File to write to
     * @param data Byte array data
     * @throws IOException If file cannot be written
     */
    public static void writeFileFromBytes(File file, byte[] data) throws IOException {
        Files.write(file.toPath(), data);
    }

    /**
     * Get file extension
     * @param file File object
     * @return File extension (e.g., "pdf", "doc")
     */
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Get file extension from filename
     * @param fileName File name
     * @return File extension
     */
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Check if file is image type
     * @param file File to check
     * @return True if file is image
     */
    public static boolean isImageFile(File file) {
        String ext = getFileExtension(file);
        return ext.matches("jpg|jpeg|png|gif|bmp");
    }

    /**
     * Check if file is document type
     * @param file File to check
     * @return True if file is document
     */
    public static boolean isDocumentFile(File file) {
        String ext = getFileExtension(file);
        return ext.matches("pdf|doc|docx|txt|xls|xlsx");
    }

    /**
     * Get file size as formatted string
     * @param file File object
     * @return Formatted file size (e.g., "2.5 MB")
     */
    public static String getFormattedFileSize(File file) {
        long bytes = file.length();
        if (bytes <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        return String.format("%.2f %s", 
            bytes / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }

    /**
     * Save file to application temp directory
     * @param sourceFile Source file to copy
     * @param destinationName Destination file name
     * @return Destination file path
     * @throws IOException If copy fails
     */
    public static File saveFileToTemp(File sourceFile, String destinationName) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        File destFile = new File(tempDir, destinationName);
        Files.copy(sourceFile.toPath(), destFile.toPath(), 
            StandardCopyOption.REPLACE_EXISTING);
        return destFile;
    }
}
