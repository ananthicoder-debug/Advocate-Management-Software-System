package com.ams;

import com.ams.splash.SplashScreen;
import javax.swing.*;

/**
 * Main Entry Point — Advocate Management System
 */
public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // Global UI defaults for bright theme
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 0));

        // Launch splash screen on EDT
        SwingUtilities.invokeLater(() -> new SplashScreen().setVisible(true));
    }
}
