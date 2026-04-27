package com.ams.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

/**
 * File Upload/Download Utility for AMS
 * Handles file operations for evidence, documents, and media
 */
public class FileUploadDownloadUtil {
    
    private static final String BASE_UPLOAD_DIR = "uploads";
    private static final String EVIDENCE_DIR = "evidence";
    private static final String DOCUMENTS_DIR = "documents";
    private static final String PHOTOS_DIR = "photos";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    static {
        createDirectories();
    }
    
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(BASE_UPLOAD_DIR, EVIDENCE_DIR));
            Files.createDirectories(Paths.get(BASE_UPLOAD_DIR, DOCUMENTS_DIR));
            Files.createDirectories(Paths.get(BASE_UPLOAD_DIR, PHOTOS_DIR));
        } catch (IOException e) {
            System.err.println("Error creating upload directories: " + e.getMessage());
        }
    }
    
    /**
     * Open file chooser for evidence files (images, videos, PDFs)
     */
    public static String chooseEvidenceFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Evidence File");
        fileChooser.setMultiSelectionEnabled(false);
        
        // Add file filters for different types
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
            "Images (*.jpg, *.png, *.gif, *.bmp)", "jpg", "jpeg", "png", "gif", "bmp");
        FileNameExtensionFilter videoFilter = new FileNameExtensionFilter(
            "Videos (*.mp4, *.avi, *.mov, *.mkv)", "mp4", "avi", "mov", "mkv");
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
            "PDF Files (*.pdf)", "pdf");
        FileNameExtensionFilter docFilter = new FileNameExtensionFilter(
            "Documents (*.doc, *.docx, *.xlsx, *.txt)", "doc", "docx", "xlsx", "txt");
        
        fileChooser.addChoosableFileFilter(imageFilter);
        fileChooser.addChoosableFileFilter(videoFilter);
        fileChooser.addChoosableFileFilter(pdfFilter);
        fileChooser.addChoosableFileFilter(docFilter);
        fileChooser.setFileFilter(imageFilter);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return uploadFile(selectedFile, EVIDENCE_DIR);
        }
        return null;
    }
    
    /**
     * Open file chooser for document files (task submissions)
     */
    public static String chooseDocumentFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Document File");
        fileChooser.setMultiSelectionEnabled(false);
        
        FileNameExtensionFilter docFilter = new FileNameExtensionFilter(
            "Documents (*.doc, *.docx, *.pdf, *.xlsx, *.ppt, *.txt)", 
            "doc", "docx", "pdf", "xlsx", "ppt", "txt");
        fileChooser.setFileFilter(docFilter);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return uploadFile(selectedFile, DOCUMENTS_DIR);
        }
        return null;
    }
    
    /**
     * Open file chooser for photo (JPG, PNG only)
     */
    public static String choosePhotoFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Photo");
        fileChooser.setMultiSelectionEnabled(false);
        
        FileNameExtensionFilter photoFilter = new FileNameExtensionFilter(
            "Photos (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(photoFilter);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.length() > MAX_FILE_SIZE) {
                JOptionPane.showMessageDialog(parent, 
                    "File size exceeds 50MB limit", 
                    "File Too Large", 
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return uploadFile(selectedFile, PHOTOS_DIR);
        }
        return null;
    }
    
    /**
     * Upload file to server directory
     */
    private static String uploadFile(File sourceFile, String targetDir) {
        try {
            if (!sourceFile.exists()) {
                return null;
            }
            
            if (sourceFile.length() > MAX_FILE_SIZE) {
                System.err.println("File too large: " + sourceFile.getName());
                return null;
            }
            
            // Generate unique filename using UUID
            String extension = getFileExtension(sourceFile.getName());
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            
            // Create target path
            Path targetPath = Paths.get(BASE_UPLOAD_DIR, targetDir, uniqueFileName);
            
            // Copy file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return targetPath.toString();
        } catch (IOException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Download file to user's selected location
     */
    public static boolean downloadFile(String filePath, JFrame parent) {
        try {
            File sourceFile = new File(filePath);
            if (!sourceFile.exists()) {
                JOptionPane.showMessageDialog(parent, 
                    "File not found: " + filePath, 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save File As");
            fileChooser.setSelectedFile(new File(sourceFile.getName()));
            
            int result = fileChooser.showSaveDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                File destinationFile = fileChooser.getSelectedFile();
                Files.copy(sourceFile.toPath(), destinationFile.toPath(), 
                    StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(parent, 
                    "File downloaded successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, 
                "Error downloading file: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    /**
     * Open file for viewing (using system default application)
     */
    public static void openFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return;
            }
            
            java.awt.Desktop.getDesktop().open(file);
        } catch (IOException e) {
            System.err.println("Error opening file: " + e.getMessage());
        }
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0) ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Check if file type is image
     */
    public static boolean isImageFile(String filePath) {
        String ext = getFileExtension(filePath).toLowerCase();
        return ext.matches("jpg|jpeg|png|gif|bmp");
    }
    
    /**
     * Check if file type is video
     */
    public static boolean isVideoFile(String filePath) {
        String ext = getFileExtension(filePath).toLowerCase();
        return ext.matches("mp4|avi|mov|mkv|wmv");
    }
    
    /**
     * Delete file
     */
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get file size in MB
     */
    public static double getFileSizeInMB(String filePath) {
        try {
            return Files.size(Paths.get(filePath)) / (1024.0 * 1024.0);
        } catch (IOException e) {
            return 0;
        }
    }
}
