package com.ams.login;

import com.ams.util.AMSTheme;
import com.ams.util.DBConnection;
import com.ams.components.RoundedButton;
import com.ams.dashboard.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Role-Based Login Screen — AMS (FULLSCREEN)
 * Roles: SENIOR_ADVOCATE | JUNIOR_ADVOCATE | CLIENT | STAFF
 */
public class LoginFrameV2 extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField     userField;
    private JPasswordField passField;
    private JComboBox<String> roleBox;
    private JLabel         statusLabel;
    private RoundedButton  loginBtn;
    private RoundedButton  registerBtn;

    private static final String[] ROLES = {
        "Senior Advocate", "Junior Advocate", "Client", "Staff"
    };

    public LoginFrameV2() {
        setTitle("AMS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        setResizable(true);
        
        // Set to fullscreen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setContentPane(buildContent());
    }

    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout()) { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, AMSTheme.PRIMARY_DARK, W, H, AMSTheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, W, H);
                
                // Decorative circles
                g2.setColor(new Color(255,255,255,10));
                g2.fillOval(-100, -100, 400, 400);
                g2.fillOval(W-200, H-300, 400, 400);
                
                g2.setColor(AMSTheme.ACCENT);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(W/2-150, H/2-150, 300, 300);
                
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // ── CENTER PANEL ─────────────────────────────────────────────────────
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = buildLoginCard();

        center.add(card);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel() { private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMSTheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                
                // Top accent line
                GradientPaint gp = new GradientPaint(0,0,AMSTheme.PRIMARY,getWidth(),0,AMSTheme.ACCENT);
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),6,24,24);
                
                // Border shadow
                g2.setColor(new Color(0,0,0,18));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 550));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Title
        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcome.setForeground(AMSTheme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(AMSTheme.FONT_SMALL);
        sub.setForeground(AMSTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role selector
        JLabel roleLabel = makeLabel("Select Role");
        roleBox = new JComboBox<>(ROLES);
        styleCombo(roleBox);

        // Username
        JLabel userLabel = makeLabel("Username");
        userField = new JTextField();
        styleField(userField, "Enter your username");

        // Password
        JLabel passLabel = makeLabel("Password");
        passField = new JPasswordField();
        styleField(passField, "Enter your password");

        // Status message
        statusLabel = new JLabel(" ");
        statusLabel.setFont(AMSTheme.FONT_SMALL);
        statusLabel.setForeground(AMSTheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        loginBtn = new RoundedButton("LOGIN", AMSTheme.PRIMARY, AMSTheme.PRIMARY_LIGHT);
        loginBtn.setPreferredSize(new Dimension(300, 48));
        loginBtn.setMaximumSize(new Dimension(300, 48));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        // Register button
        registerBtn = new RoundedButton("Register as Client", new Color(0x27AE60), new Color(0x2ECC71));
        registerBtn.setPreferredSize(new Dimension(300, 42));
        registerBtn.setMaximumSize(new Dimension(300, 42));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> openClientRegistration());

        // Enter key handler
        passField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        card.add(welcome);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(roleBox);
        card.add(Box.createVerticalStrut(16));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(userField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(registerBtn);

        return card;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AMSTheme.FONT_BOLD);
        l.setForeground(AMSTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f, String placeholder) {
        f.setFont(AMSTheme.FONT_BODY);
        f.setBackground(AMSTheme.BG_INPUT);
        f.setForeground(AMSTheme.TEXT_PRIMARY);
        f.setPreferredSize(new Dimension(300, 42));
        f.setMaximumSize(new Dimension(300, 42));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true),
            new EmptyBorder(6, 12, 6, 12)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AMSTheme.PRIMARY_LIGHT, 2, true),
                    new EmptyBorder(5, 11, 5, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xC8D3F0), 1, true),
                    new EmptyBorder(6, 12, 6, 12)));
            }
        });
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(AMSTheme.FONT_BODY);
        c.setBackground(AMSTheme.BG_INPUT);
        c.setForeground(AMSTheme.TEXT_PRIMARY);
        c.setPreferredSize(new Dimension(300, 42));
        c.setMaximumSize(new Dimension(300, 42));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // ── Login Logic ───────────────────────────────────────────────────────────
    private void doLogin() {
        String user     = userField.getText().trim();
        String pass     = new String(passField.getPassword()).trim();
        String roleSel  = ROLES[roleBox.getSelectedIndex()];
        String roleCode = toRoleCode(roleSel);

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("⚠  Please enter username and password.");
            return;
        }
        
        if (!DBConnection.testConnection()) {
            String err = DBConnection.getLastError();
            statusLabel.setText(" connect to database: " + (err != null ? err : "unknown"));
            loginBtn.setEnabled(true);
            return;
        }

        statusLabel.setText("Authenticating…");
        loginBtn.setEnabled(false);

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override protected Object[] doInBackground() {
                String[] result = authenticateUser(user, pass, roleCode);
                if (result != null) {
                    return new Object[]{true, result};
                }
                
                // Check database connectivity
                boolean dbConnected = DBConnection.testConnection();
                if (!dbConnected) {
                    // Demo mode - database unavailable
                    return new Object[]{false, "demo"};
                }
                
                // Database connected but credentials invalid
                return new Object[]{false, "invalid"};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    boolean authenticated = (boolean) result[0];
                    Object status = result[1];
                    
                    if (authenticated) {
                        String[] authData = (String[]) status;
                        statusLabel.setText("");
                        openDashboard(authData[0], authData[1], authData[2]);
                    } else if ("demo".equals(status)) {
                        statusLabel.setText("ℹ  Using database.");
                        openDashboardDemo(user, roleCode);
                    } else {
                        statusLabel.setText("✗  Invalid credentials or role mismatch.");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("✗  Error: " + ex.getMessage());
                }
                loginBtn.setEnabled(true);
            }
        };
        worker.execute();
    }

    private String[] authenticateUser(String username, String password, String role) {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return null;
            
            String sql = "SELECT user_id, role, ref_id FROM AMS_USERS1 " +
                         "WHERE username=? AND password=? AND role=? AND is_active=1";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("user_id"),
                    rs.getString("role"),
                    rs.getString("ref_id")
                };
            }
        } catch (Exception e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return null;
    }

    private String toRoleCode(String role) {
        switch (role) {
            case "Senior Advocate": return "SENIOR_ADVOCATE";
            case "Junior Advocate": return "JUNIOR_ADVOCATE";
            case "Client": return "CLIENT";
            case "Staff": return "STAFF";
            default: return "STAFF";
        }
    }

    private void openDashboard(String userId, String role, String refId) {
        setVisible(false);
        final int id = Integer.parseInt(refId != null ? refId : "1");
        SwingUtilities.invokeLater(() -> {
            switch (role) {
                case "SENIOR_ADVOCATE":
                    new AdvocateDashboard(id).setVisible(true);
                    break;
                case "JUNIOR_ADVOCATE":
                    new JuniorDashboard(id).setVisible(true);
                    break;
                case "CLIENT":
                    new ClientDashboard(id).setVisible(true);
                    break;
                case "STAFF":
                    new StaffDashboard(id).setVisible(true);
                    break;
                default:
                    new StaffDashboard(id).setVisible(true);
            }
        });
        dispose();
    }

    private void openDashboardDemo(String username, String role) {
        setVisible(false);
        SwingUtilities.invokeLater(() -> {
            switch (role) {
                case "SENIOR_ADVOCATE":
                    new AdvocateDashboard(1).setVisible(true);
                    break;
                case "JUNIOR_ADVOCATE":
                    new JuniorDashboard(1).setVisible(true);
                    break;
                case "CLIENT":
                    new ClientDashboard(1).setVisible(true);
                    break;
                case "STAFF":
                    new StaffDashboard(1).setVisible(true);
                    break;
                default:
                    new StaffDashboard(1).setVisible(true);
            }
        });
        dispose();
    }

    private void openClientRegistration() {
        new ClientRegistrationDialogV2(this).setVisible(true);
    }
}
