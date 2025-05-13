package gp;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Updated Brunel University Login System with properly working buttons
 */
public class BrunelUniversityLoginSystem {
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    private ImageIcon googleIcon;
    private ImageIcon facebookIcon;
    private ImageIcon appleIcon;
    
    public BrunelUniversityLoginSystem() {
        // Load icons
        googleIcon = RealisticIconCreator.createGoogleIcon(20, 20);
        facebookIcon = ResourcesManager.getFacebookIcon(20, 20);
        appleIcon = RealisticIconCreator.createAppleIcon(20, 20);
        
        // Create the main application frame
        mainFrame = new JFrame("Brunel University London");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        
        // Create card layout for switching between login and sign up pages
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Create login and signup panels
        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(createSignUpPanel(), "signup");
        
        // Add card panel to frame
        mainFrame.add(cardPanel);
        
        // Show the frame
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
 
    public JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header panel with university name
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(173, 216, 255)); // Light blue background
        headerPanel.setPreferredSize(new Dimension(panel.getWidth(), 80));
        JLabel universityLabel = new JLabel("Brunel University London");
        universityLabel.setFont(new Font("Serif", Font.BOLD, 32));
        universityLabel.setForeground(Color.WHITE);
        headerPanel.add(universityLabel);
        
        // Center panel with login form
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        
        // Sign In label
        JLabel signInLabel = new JLabel("Sign In");
        signInLabel.setFont(new Font("Sans-serif", Font.BOLD, 28));
        signInLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Email field
        JPanel emailPanel = new JPanel(new BorderLayout(10, 0)); // Added gap between label and field
        emailPanel.setMaximumSize(new Dimension(500, 60));
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        emailLabel.setPreferredSize(new Dimension(120, 30)); // Fixed width for alignment
        JTextField emailField = new JTextField();
        emailPanel.add(emailLabel, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);
        
        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0)); // Added gap between label and field
        passwordPanel.setMaximumSize(new Dimension(500, 60));
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        passwordLabel.setPreferredSize(new Dimension(120, 30)); // Same width as email label
        JPasswordField passwordField = new JPasswordField();
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        // Forgot password link
        JPanel forgotPasswordPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotPasswordPanel.setMaximumSize(new Dimension(500, 25));
        JLabel forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setForeground(new Color(173, 216, 230));
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordPanel.add(forgotPasswordLabel);
        
        // Don't have an account? Sign Up link
        JPanel signUpLinkPanel = new JPanel();
        signUpLinkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel noAccountLabel = new JLabel("Don't have an account? ");
        JLabel signUpLabel = new JLabel("Sign Up");
        signUpLabel.setForeground(new Color(173, 216, 230));
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLinkPanel.add(noAccountLabel);
        signUpLinkPanel.add(signUpLabel);
     // Sign In button panel (aligned right like Sign Up button in signup panel)
        JPanel signInButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        signInButtonPanel.setMaximumSize(new Dimension(500, 40));
        RoundedButton signInButton = new RoundedButton("Sign In", new Color(66, 133, 244), Color.WHITE);
        signInButton.setFont(new Font("Sans-serif", Font.BOLD, 16));
        signInButton.setPreferredSize(new Dimension(120, 40));
        signInButton.addActionListener(e -> {
            mainFrame.dispose();
            new ApplicantManagementSystem();
        });
        signInButtonPanel.add(signInButton);
        // Make the sign up link clickable and add hand cursor
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardPanel, "signup");
            }
        });
        
        // Social login buttons
        JPanel socialButtonsPanel = new JPanel();
        socialButtonsPanel.setLayout(new BoxLayout(socialButtonsPanel, BoxLayout.Y_AXIS));
        socialButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create social media buttons using the RoundedButton class
        RoundedButton googleButton = new RoundedButton("Google", googleIcon, new Color(245, 245, 245), Color.BLACK);
        googleButton.setURL("https://accounts.google.com/signin");
        googleButton.setMaximumSize(new Dimension(300, 40));
        googleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        RoundedButton facebookButton = new RoundedButton("facebook", facebookIcon, new Color(66, 103, 178), Color.WHITE);
        facebookButton.setURL("https://www.facebook.com/login");
        facebookButton.setMaximumSize(new Dimension(300, 40));
        facebookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        RoundedButton appleButton = new RoundedButton("Apple Id", appleIcon, Color.BLACK, Color.WHITE);
        appleButton.setURL("https://appleid.apple.com/sign-in");
        appleButton.setMaximumSize(new Dimension(300, 40));
        appleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        socialButtonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        socialButtonsPanel.add(googleButton);
        socialButtonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        socialButtonsPanel.add(facebookButton);
        socialButtonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        socialButtonsPanel.add(appleButton);
        
        // Add components to center panel
     // Add components to center panel (updated order)
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(signInLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(emailPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(passwordPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(forgotPasswordPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Space before sign-in button
        centerPanel.add(signInButtonPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));  // Space before sign-up link
        centerPanel.add(signUpLinkPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Space before social buttons
        centerPanel.add(socialButtonsPanel);
        centerPanel.add(Box.createVerticalGlue());
        
        // Add panels to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    public JPanel createSignUpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header panel with university name
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(173, 216, 255)); // Light blue background
        headerPanel.setPreferredSize(new Dimension(panel.getWidth(), 80));
        JLabel universityLabel = new JLabel("Brunel University London");
        universityLabel.setFont(new Font("Serif", Font.BOLD, 32));
        universityLabel.setForeground(Color.WHITE);
        headerPanel.add(universityLabel);
        
        // Center panel with signup form
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        
        // Create Account label
        JLabel createAccountLabel = new JLabel("Create Account");
        createAccountLabel.setFont(new Font("Sans-serif", Font.BOLD, 28));
        createAccountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Email field
        JPanel emailPanel = new JPanel(new BorderLayout(10, 0)); // Added gap between label and field
        emailPanel.setMaximumSize(new Dimension(500, 60));
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        emailLabel.setPreferredSize(new Dimension(170, 30)); // Fixed width for alignment
        JTextField emailField = new JTextField();
        emailPanel.add(emailLabel, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);
        
        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0)); // Added gap between label and field
        passwordPanel.setMaximumSize(new Dimension(500, 60));
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        passwordLabel.setPreferredSize(new Dimension(170, 30)); // Same width as other labels
        JPasswordField passwordField = new JPasswordField();
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        // Confirm Password field
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout(10, 0)); // Added gap between label and field
        confirmPasswordPanel.setMaximumSize(new Dimension(500, 60));
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        confirmPasswordLabel.setPreferredSize(new Dimension(170, 30)); // Same width as other labels
        JPasswordField confirmPasswordField = new JPasswordField();
        confirmPasswordPanel.add(confirmPasswordLabel, BorderLayout.WEST);
        confirmPasswordPanel.add(confirmPasswordField, BorderLayout.CENTER);
        
        // Contact Number field
        JPanel contactNumberPanel = new JPanel(new BorderLayout(10, 0)); // Added gap between label and field
        contactNumberPanel.setMaximumSize(new Dimension(500, 60));
        JLabel contactNumberLabel = new JLabel("Contact Number:");
        contactNumberLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        contactNumberLabel.setPreferredSize(new Dimension(170, 30)); // Same width as other labels
        JTextField contactNumberField = new JTextField();
        contactNumberPanel.add(contactNumberLabel, BorderLayout.WEST);
        contactNumberPanel.add(contactNumberField, BorderLayout.CENTER);
        
        // Sign Up button using RoundedButton
        RoundedButton signUpButton = new RoundedButton("Sign Up", new Color(66, 133, 244), Color.WHITE);
        signUpButton.setFont(new Font("Sans-serif", Font.BOLD, 16));
        signUpButton.setMaximumSize(new Dimension(120, 40));
        
        // Or Sign Up Using label
        JPanel orSignUpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel orLabel = new JLabel("Or");
        orLabel.setForeground(new Color(173, 216, 230)); // Light blue color for "Or" text
        JLabel signUpUsingLabel = new JLabel("Sign Up Using");
        signUpUsingLabel.setForeground(new Color(173, 216, 230)); // Light blue color for "Sign Up Using" text
        orSignUpPanel.add(orLabel);
        orSignUpPanel.add(signUpUsingLabel);
        
        // Social login buttons
        JPanel socialButtonsPanel = new JPanel();
        socialButtonsPanel.setLayout(new BoxLayout(socialButtonsPanel, BoxLayout.Y_AXIS));
        socialButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create social media buttons using the RoundedButton class
        RoundedButton googleButton = new RoundedButton("Google", googleIcon, new Color(245, 245, 245), Color.BLACK);
        googleButton.setURL("https://accounts.google.com/signin");
        googleButton.setMaximumSize(new Dimension(300, 40));
        googleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        RoundedButton facebookButton = new RoundedButton("facebook", facebookIcon, new Color(66, 103, 178), Color.WHITE);
        facebookButton.setURL("https://www.facebook.com/login");
        facebookButton.setMaximumSize(new Dimension(300, 40));
        facebookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        RoundedButton appleButton = new RoundedButton("Apple Id", appleIcon, Color.BLACK, Color.WHITE);
        appleButton.setURL("https://appleid.apple.com/sign-in");
        appleButton.setMaximumSize(new Dimension(300, 40));
        appleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        socialButtonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        socialButtonsPanel.add(googleButton);
        socialButtonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        socialButtonsPanel.add(facebookButton);
        socialButtonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        socialButtonsPanel.add(appleButton);
        
        // Already have an account? Log in link
        JPanel loginLinkPanel = new JPanel();
        loginLinkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel alreadyAccountLabel = new JLabel("Already have an account? ");
        JLabel logInLabel = new JLabel("Log in");
        logInLabel.setForeground(new Color(173, 216, 230));
        logInLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLinkPanel.add(alreadyAccountLabel);
        loginLinkPanel.add(logInLabel);
        
        // Make the login link clickable
        logInLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardPanel, "login");
            }
        });
        
        // Add components to center panel
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(createAccountLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(emailPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(passwordPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(confirmPasswordPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(contactNumberPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(signUpButton);
        buttonPanel.setMaximumSize(new Dimension(500, 40));
        centerPanel.add(buttonPanel);
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(orSignUpPanel);
        centerPanel.add(socialButtonsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(loginLinkPanel);
        centerPanel.add(Box.createVerticalGlue());
        
        // Add panels to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BrunelUniversityLoginSystem();
            }
        });
    }
}