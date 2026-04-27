package com.ams.util;

import java.io.*;
import java.nio.file.*;

/**
 * Document Viewer Utility for AMS
 * Handles opening files with system default applications
 */
public class DocumentViewerUtil {

    /**
     * Open file with system default application
     * @param filePath Path to file or null
     * @param fileBytes Byte array data or null
     * @param fileName File name with extension
     * @return True if successfully opened, false otherwise
     */
    public static boolean openFile(String filePath, byte[] fileBytes, String fileName) {
        File fileToOpen = null;
        
        try {
            if (filePath != null && !filePath.isEmpty()) {
                // Open existing file from path
                fileToOpen = new File(filePath);
                if (!fileToOpen.exists()) {
                    System.err.println("File not found: " + filePath);
                    return false;
                }
            } else if (fileBytes != null) {
                // Create temp file from byte array
                String tempDir = System.getProperty("java.io.tmpdir");
                fileToOpen = new File(tempDir, fileName);
                Files.write(fileToOpen.toPath(), fileBytes, 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, 
                    StandardOpenOption.TRUNCATE_EXISTING);
                fileToOpen.deleteOnExit(); // Delete when JVM exits
            } else {
                System.err.println("No file data provided");
                return false;
            }
            
            // Open file with system default application
            openFileWithSystem(fileToOpen);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error opening file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Open file using system default application
     * @param file File to open
     */
    private static void openFileWithSystem(File file) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) {
                // Windows
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", file.getAbsolutePath()});
            } else if (osName.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
            } else {
                // Linux and others
                Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
            }
        } catch (IOException e) {
            System.err.println("Error opening file with system application: " + e.getMessage());
        }
    }

    /**
     * Get appropriate viewer application for file type
     * @param fileExtension File extension (e.g., "pdf", "doc")
     * @return Application command or null
     */
    public static String getViewerForFileType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                return "AdobeAcrobat.Document.DC"; // Windows
            case "doc":
            case "docx":
                return "Word.Document.12"; // Windows
            case "xls":
            case "xlsx":
                return "Excel.Sheet.12"; // Windows
            case "txt":
                return "notepad"; // Windows
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "mspaint"; // Windows
            default:
                return null;
        }
    }

    /**
     * Check if file can be viewed
     * @param fileExtension File extension
     * @return True if file type is viewable
     */
    public static boolean isViewableFileType(String fileExtension) {
        String ext = fileExtension.toLowerCase();
        return ext.matches("pdf|doc|docx|txt|xls|xlsx|jpg|jpeg|png|gif|bmp");
    }

    /**
     * Get file type display name
     * @param fileExtension File extension
     * @return Display name for file type
     */
    public static String getFileTypeDisplayName(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                return "PDF Document";
            case "doc":
            case "docx":
                return "Word Document";
            case "xls":
            case "xlsx":
                return "Excel Spreadsheet";
            case "txt":
                return "Text File";
            case "jpg":
            case "jpeg":
                return "JPEG Image";
            case "png":
                return "PNG Image";
            case "gif":
                return "GIF Image";
            case "bmp":
                return "Bitmap Image";
            default:
                return "File";
        }
    }

    /**
     * Get icon emoji for file type
     * @param fileExtension File extension
     * @return Emoji icon representation
     */
    public static String getFileTypeIcon(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                return "[PDF]";
            case "doc":
            case "docx":
                return "[DOC]";
            case "xls":
            case "xlsx":
                return "[XLS]";
            case "txt":
                return "[TXT]";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "[IMG]";
            default:
                return "[FILE]";
        }
    }

    /**
     * Save byte array to temporary file and open
     * @param fileBytes File data as bytes
     * @param fileName File name with extension
     * @return True if successfully opened
     */
    public static boolean openFileFromBytes(byte[] fileBytes, String fileName) {
        return openFile(null, fileBytes, fileName);
    }

    /**
     * Open file from file path
     * @param filePath Full path to file
     * @return True if successfully opened
     */
    public static boolean openFileFromPath(String filePath) {
        return openFile(filePath, null, new File(filePath).getName());
    }
}
