package gp;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.toedter.calendar.JDateChooser;

public class ApplicantManagementSystem extends JFrame {
    // UI Components
    private JTextField searchField;
    private JButton newApplicantsButton;
    private JButton reportsButton;
    private JList<Applicant> applicantList;
    private DefaultListModel<Applicant> listModel;
    private JPanel detailsPanel;
    private JTabbedPane tabbedPane;
    private JPanel personalDetailsPanel;
    private JLabel selectedApplicantIdLabel;
    
    // Data
    private List<Applicant> allApplicants;
    private Applicant currentApplicant;
    
    // Constants
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 700;
    private static final Color BACKGROUND_COLOR = new Color(228, 228, 228); // Light gray background
    private static final Color BUTTON_COLOR = new Color(157, 195, 230); // Light blue button color
    private static final Color REPORTS_BUTTON_COLOR = new Color(65, 90, 119); // Dark blue for reports button
    private static final Color ACCENT_COLOR = new Color(157, 195, 230); // Light blue for selection
    private static final Color FIELD_BACKGROUND = new Color(245, 245, 245); // Light gray for text fields
    private static final int CORNER_RADIUS = 15; // Standard corner radius
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE; // Better contrast
    private static final Color PANEL_BORDER_COLOR = new Color(220, 220, 220);
    // Add database connection fields
    private Connection connection;
    private static final String DB_PATH = "applicant_db.db";
    private static final String DB_URL = "jdbc:sqlite:applicant_db.db";
    private static final String USER = "applicant_user";
    private static final String PASSWORD = "your_password";
    
    public ApplicantManagementSystem() {
        setTitle("Applicant Management System");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize database
        initializeDatabase();
        
        // Load data from database
        loadApplicantsFromDatabase();
        
        fixNullApplicantIds();
        
        // Setup UI
        setupUI();
        
        // Set visible
        setVisible(true);
    }
    
    
    private void filterApplicants() {
        String searchText = searchField.getText().toLowerCase();
        
        if (searchText.equals("search applicants")) {
            // Reset to show all applicants
            listModel.clear();
            for (Applicant applicant : allApplicants) {
                listModel.addElement(applicant);
            }
            return;
        }
        
        listModel.clear();
        
        // Check if the search is focused on ID (if text contains underscore, it's likely an ID search)
        boolean isIdSearch = searchText.contains("_") || searchText.matches(".*\\d{4}.*");
        
        for (Applicant applicant : allApplicants) {
            // Search by multiple criteria
            boolean nameMatch = applicant.getName().toLowerCase().contains(searchText);
            boolean idMatch = applicant.getId() != null && applicant.getId().toLowerCase().contains(searchText);
            boolean certificateMatch = applicant.getCertificate().toLowerCase().contains(searchText);
            
            // If it looks like an ID search, prioritize ID matches
            if (isIdSearch) {
                if (idMatch) {
                    listModel.addElement(applicant);
                }
            } else {
                // Otherwise, use any criterion to match
                if (nameMatch || idMatch || certificateMatch) {
                    listModel.addElement(applicant);
                }
            }
        }
        
        // If no results found with an ID search, show a message
        if (isIdSearch && listModel.isEmpty()) {
            // Add a visual indicator that no matches were found
            showNoMatchesIndicator("No applicants found with ID: " + searchText);
        }
    }

    
    private void showNoMatchesIndicator(String message) {
        // This method can be customized based on how you want to show the message
        // Option 1: Show a message dialog
        // JOptionPane.showMessageDialog(this, message, "Search Results", JOptionPane.INFORMATION_MESSAGE);
        
        // Option 2: Show a status message near the search field (better UX)
        // Create a panel to show the message
        JPanel noMatchPanel = new JPanel(new BorderLayout());
        noMatchPanel.setBackground(Color.WHITE);
        
        JLabel noMatchLabel = new JLabel(message);
        noMatchLabel.setForeground(Color.RED);
        noMatchLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        noMatchLabel.setHorizontalAlignment(JLabel.CENTER);
        noMatchLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        noMatchPanel.add(noMatchLabel, BorderLayout.CENTER);
        
        
        
        searchField.setToolTipText(message);
        
        
        flashSearchField();
    }

    
    private void flashSearchField() {
        // Save original colors
        final Color originalBg = searchField.getBackground();
        final Color originalFg = searchField.getForeground();
        
        // Flash effect with a light red background
        searchField.setBackground(new Color(255, 220, 220));
        searchField.setForeground(Color.RED);
        
        // Create a timer to restore the original colors after a delay
        Timer timer = new Timer(1500, e -> {
            searchField.setBackground(originalBg);
            searchField.setForeground(originalFg);
        });
        timer.setRepeats(false);
        timer.start();
    }
    private JPanel createDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        // Create a panel for document upload sections
        JPanel documentsContainer = new JPanel();
        documentsContainer.setLayout(new BoxLayout(documentsContainer, BoxLayout.Y_AXIS));
        documentsContainer.setBackground(Color.WHITE);
        
        // Add document upload sections with improved UI and functionality - Certificate removed
        documentsContainer.add(createDocumentUploadSection("Profile Picture", "PNG format only, max 300×300 pixels"));
        documentsContainer.add(Box.createVerticalStrut(20)); // Add spacing
        documentsContainer.add(createDocumentUploadSection("Passport", "PNG format only, max 500×500 pixels"));
        documentsContainer.add(Box.createVerticalStrut(20)); // Add spacing
        documentsContainer.add(createDocumentUploadSection("National ID", "PNG/JPG format, max 1000×1000 pixels"));
        
        // Add to a scroll pane for large number of documents
        JScrollPane scrollPane = new JScrollPane(documentsContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.BLUE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

   
    private void setupSearchFieldWithIdOption() {
        // Find your existing search field setup code and add this placeholder text
        searchField.setText("Search by Name or ID");
        searchField.setForeground(Color.GRAY);
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search by Name or ID")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search by Name or ID");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
    }

    
    private void addAdvancedIdSearch() {
        // Create a right-click (context) menu for the search field
        JPopupMenu searchMenu = new JPopupMenu();
        
        JMenuItem idSearchItem = new JMenuItem("Search by ID");
        idSearchItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter applicant ID (e.g., ABC_0001):", 
                "ID Search", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                searchField.setText(input.trim());
                filterApplicants();
            }
        });
        
        searchMenu.add(idSearchItem);
        
        // Add a separator and another option for name search
        searchMenu.addSeparator();
        
        JMenuItem nameSearchItem = new JMenuItem("Search by Name");
        nameSearchItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter applicant name:", 
                "Name Search", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                searchField.setText(input.trim());
                filterApplicants();
            }
        });
        
        searchMenu.add(nameSearchItem);
        
        // Add right-click menu to search field
        searchField.setComponentPopupMenu(searchMenu);
        
        // Optional: Add a search button next to the search field that shows the popup menu
        JButton advSearchButton = new JButton("▼");
        advSearchButton.setToolTipText("Advanced Search Options");
        advSearchButton.setPreferredSize(new Dimension(25, 25));
        advSearchButton.setFocusPainted(false);
        advSearchButton.setMargin(new Insets(0, 0, 0, 0));
        
        advSearchButton.addActionListener(e -> {
            searchMenu.show(advSearchButton, 0, advSearchButton.getHeight());
        });
        
        // Add this button to your search panel (you'll need to find/modify the appropriate code)
        // searchPanel.add(advSearchButton, BorderLayout.EAST);
    }

   
    private void addSearchShortcuts() {
        // Add keyboard shortcut (Ctrl+F) to focus the search field
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().registerKeyboardAction(e -> {
            searchField.requestFocus();
            searchField.selectAll();
        }, ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        // Add keyboard shortcut (F3) for ID-specific search popup
        KeyStroke f3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        getRootPane().registerKeyboardAction(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter applicant ID (e.g., ABC_0001):", 
                "ID Search", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                searchField.setText(input.trim());
                filterApplicants();
            }
        }, f3, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    
    private void setupSearchFunctionality() {
        setupSearchFieldWithIdOption();
        addAdvancedIdSearch();
        addSearchShortcuts();
    }
    // Method to save file to a dedicated storage location
    private String saveFileToStorage(File sourceFile, String documentType, String applicantId) throws IOException {
        // Create directory structure if it doesn't exist
        File storageDir = new File("documents/" + applicantId + "/" + documentType.replaceAll("\\s+", "_"));
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        // Generate a unique filename with timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fileExtension = getFileExtension(sourceFile);
        String newFileName = documentType.replaceAll("\\s+", "_") + "_" + timestamp + "." + fileExtension;
        
        // Create destination file
        File destFile = new File(storageDir, newFileName);
        
        // Copy the file
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // Return the relative path to be stored in the database
        return destFile.getAbsolutePath();
    }

    private JPanel createQualificationPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 20, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Certificate Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel certTypeLabel = new JLabel("Certificate Type");
        certTypeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(certTypeLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        RoundedTextField certTypeField = new RoundedTextField(FIELD_BACKGROUND, 15);
        certTypeField.setEditable(false);
        certTypeField.setPreferredSize(new Dimension(300, 40));
        certTypeField.setName("certType");
        contentPanel.add(certTypeField, gbc);
        
        // Certificate Grade
        gbc.gridx = 1;
        gbc.gridy = 0;
        JLabel certGradeLabel = new JLabel("Certificate Grade");
        certGradeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(certGradeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        RoundedTextField certGradeField = new RoundedTextField(FIELD_BACKGROUND, 15);
        certGradeField.setEditable(false);
        certGradeField.setPreferredSize(new Dimension(300, 40));
        certGradeField.setName("certGrade");
        contentPanel.add(certGradeField, gbc);
        
        // Certificate Document Upload Section - Enhanced with proper validation
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        
        // Create enhanced certificate upload panel
        JPanel certificateUploadPanel = createEnhancedCertificateUploadPanel();
        contentPanel.add(certificateUploadPanel, gbc);
        
        // Add navigation buttons
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navigationPanel.setBackground(Color.WHITE);
        
        
        
        // Add panels to main panel
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(navigationPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    private JPanel createEnhancedCertificateUploadPanel() {
        JPanel certificateUploadPanel = new JPanel();
        certificateUploadPanel.setLayout(new BoxLayout(certificateUploadPanel, BoxLayout.Y_AXIS));
        certificateUploadPanel.setBackground(new Color(245, 245, 245));
        certificateUploadPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        certificateUploadPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        // Upload icon
        JLabel uploadIcon = new JLabel();
        uploadIcon.setIcon(createUploadIcon());
        uploadIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Certificate Document label
        JLabel certDocLabel = new JLabel("Certificate Document");
        certDocLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        certDocLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Format description - updated to be more specific
        JLabel formatLabel = new JLabel("PDF Format Only (Max 5MB)");
        formatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formatLabel.setForeground(Color.GRAY);
        formatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add status label to show selected file
        JLabel statusLabel = new JLabel("No file selected");
        statusLabel.setName("Certificate_status");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton uploadButton = new JButton("Upload");
        uploadButton.setBackground(BUTTON_COLOR); // Use blue
        uploadButton.setForeground(Color.BLACK); // Changed from Color.WHITE to Color.BLACK
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(new RoundedBorder(BUTTON_COLOR, 15));
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadButton.setMaximumSize(new Dimension(120, 40));
        uploadButton.addActionListener(e -> openFileChooser("Certificate"));
        
        // View button
        JButton viewButton = new JButton("View");
        viewButton.setBackground(new Color(100, 150, 200));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        viewButton.setBorder(new RoundedBorder(new Color(100, 150, 200), 15));
        viewButton.setEnabled(false); // Disabled by default
        viewButton.addActionListener(e -> viewDocument("Certificate"));
        viewButton.setName("Certificate_view");
        viewButton.setMaximumSize(new Dimension(120, 40));
        
        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(200, 100, 100));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(new RoundedBorder(new Color(200, 100, 100), 15));
        deleteButton.setEnabled(false); // Disabled by default
        deleteButton.addActionListener(e -> deleteDocumentWithConfirmation("Certificate"));
        deleteButton.setName("Certificate_delete");
        deleteButton.setMaximumSize(new Dimension(120, 40));
        
        // Add buttons to the button panel with some spacing
        buttonPanel.add(uploadButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(viewButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(deleteButton);
        
        // Add components to certificate upload panel
        certificateUploadPanel.add(uploadIcon);
        certificateUploadPanel.add(Box.createVerticalStrut(15));
        certificateUploadPanel.add(certDocLabel);
        certificateUploadPanel.add(Box.createVerticalStrut(5));
        certificateUploadPanel.add(formatLabel);
        certificateUploadPanel.add(Box.createVerticalStrut(10));
        certificateUploadPanel.add(statusLabel);
        certificateUploadPanel.add(Box.createVerticalStrut(20));
        certificateUploadPanel.add(buttonPanel);
        // Upload button
        
        return certificateUploadPanel;
    }
    // Method to save document reference to database
    private boolean saveDocumentToDatabase(String applicantId, String documentType, String filePath) {
        String sql = "INSERT INTO documents (applicant_id, document_type, file_path) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, applicantId);
            pstmt.setString(2, documentType);
            pstmt.setString(3, filePath);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    

    // Add this method to delete a document
    private boolean deleteDocument(String applicantId, String documentType) {
        String sql = "DELETE FROM documents WHERE applicant_id = ? AND document_type = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, applicantId);
            pstmt.setString(2, documentType);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void saveApplicant(Applicant applicant) {
        // Generate ID if needed
    	if (applicant.getId() == null || applicant.getId().trim().isEmpty()) {
    	    String generatedId = DatabaseManager.generateApplicantId(applicant.getName());
    	    applicant.setId(generatedId);
    	}
        
        String sql = """
            INSERT INTO applicants (id, name, date_of_application, certificate, grade, 
                                  email, phone, address, date_of_birth)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getId());
            pstmt.setString(2, applicant.getName());
            pstmt.setDate(3, java.sql.Date.valueOf(applicant.getDateOfApplication()));
            pstmt.setString(4, applicant.getCertificate());
            pstmt.setString(5, applicant.getGrade());
            pstmt.setString(6, applicant.getEmail());
            pstmt.setString(7, applicant.getPhone());
            pstmt.setString(8, applicant.getAddress());
            
            // Handle date of birth with proper conversion
            String dob = applicant.getDateOfBirth();
            if (dob != null && !dob.isEmpty()) {
                try {
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date date = inputFormat.parse(dob);
                    String sqlDateStr = outputFormat.format(date);
                    pstmt.setDate(9, java.sql.Date.valueOf(sqlDateStr));
                } catch (Exception e) {
                    pstmt.setNull(9, Types.DATE);
                    System.err.println("Error parsing date: " + e.getMessage());
                }
            } else {
                pstmt.setNull(9, Types.DATE);
            }
            
            pstmt.executeUpdate();
            
            showCustomDialog(
                "Applicant saved successfully with ID: " + applicant.getId(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (SQLException e) {
            showCustomDialog(
                "Failed to save applicant: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Helper method to get file extension
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }

    // Method to view a document
    private void viewDocument(String documentType) {
        if (currentApplicant == null) {
            showCustomDialog(
                "Please select an applicant first.",
                "No Applicant Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String filePath = currentApplicant.getDocument(documentType);
        if (filePath == null || filePath.isEmpty()) {
            showCustomDialog(
                "No document found for " + documentType,
                "Document Not Found",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            showCustomDialog(
                "File not found at location: " + filePath,
                "File Missing",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        try {
            // Open the file with the system's default application
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
            showCustomDialog(
                "Error opening file: " + e.getMessage(),
                "File Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Method to delete a document with confirmation
    private void deleteDocumentWithConfirmation(String documentType) {
        if (currentApplicant == null) {
            showCustomDialog(
                "Please select an applicant first.",
                "No Applicant Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this " + documentType + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            String applicantId = currentApplicant.getId();
            String filePath = currentApplicant.getDocument(documentType);
            
            try {
                // Delete from database
                boolean dbSuccess = false;
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement deleteStmt = conn.prepareStatement(
                         "DELETE FROM documents WHERE applicant_id = ? AND document_type = ?")) {
                    
                    deleteStmt.setString(1, applicantId);
                    deleteStmt.setString(2, documentType);
                    
                    dbSuccess = deleteStmt.executeUpdate() > 0;
                }
                
                // Try to delete the physical file
                boolean fileSuccess = true;
                if (filePath != null && !filePath.isEmpty()) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        fileSuccess = file.delete();
                        if (!fileSuccess) {
                            System.err.println("Warning: Could not delete file at " + filePath);
                        }
                    }
                }
                
                if (dbSuccess) {
                    // Update the applicant object
                    currentApplicant.removeDocument(documentType);
                    
                    // Reset the UI
                    resetDocumentStatus(documentType);
                    
                    showCustomDialog(
                        documentType + " deleted successfully!",
                        "Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    showCustomDialog(
                        "Failed to delete document from database.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showCustomDialog(
                    "Error during document deletion: " + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Add this method to handle document deletion when an applicant is deleted
    private void deleteAllDocumentsForApplicant(String applicantId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // First, retrieve all documents to delete the physical files
            try (PreparedStatement selectStmt = conn.prepareStatement(
                     "SELECT file_path FROM documents WHERE applicant_id = ?")) {
                
                selectStmt.setString(1, applicantId);
                
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        String filePath = rs.getString("file_path");
                        if (filePath != null && !filePath.isEmpty()) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                }
            }
            
            // Now delete all documents from the database
            try (PreparedStatement deleteStmt = conn.prepareStatement(
                     "DELETE FROM documents WHERE applicant_id = ?")) {
                
                deleteStmt.setString(1, applicantId);
                deleteStmt.executeUpdate();
            }
            
            // Try to delete the documents directory for this applicant
            File docDir = new File("documents/" + applicantId);
            if (docDir.exists()) {
                deleteDirectory(docDir);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error deleting documents for applicant: " + e.getMessage());
        }
    }

    // Helper method to recursively delete a directory
    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }
 // Enhanced date format handling when loading applicant details
    private void loadApplicantDetails(Applicant applicant, ResultSet rs) throws SQLException {
        // Set optional fields, checking for null values where appropriate
        applicant.setEmail(rs.getString("email"));
        applicant.setPhone(rs.getString("phone"));
        applicant.setAddress(rs.getString("address"));
        
        // Handle date of birth with proper format conversion
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            try {
                // Convert from SQL date format (yyyy-MM-dd) to display format (dd/MM/yyyy)
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                applicant.setDateOfBirth(outputFormat.format(dob));
                System.out.println("Loaded date of birth: " + outputFormat.format(dob));
            } catch (Exception e) {
                System.err.println("Error formatting date: " + e.getMessage());
                
                // Fallback option - pass the date string directly
                String dobStr = rs.getString("date_of_birth");
                if (dobStr != null && !dobStr.isEmpty()) {
                    try {
                        // Try to convert from yyyy-MM-dd to dd/MM/yyyy
                        java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        java.util.Date parsedDate = inputFormat.parse(dobStr);
                        String formattedDate = outputFormat.format(parsedDate);
                        applicant.setDateOfBirth(formattedDate);
                        System.out.println("Converted date string: " + dobStr + " to " + formattedDate);
                    } catch (Exception ex) {
                        System.err.println("Fallback date parsing failed: " + ex.getMessage());
                        applicant.setDateOfBirth(dobStr); // Use as is if parsing fails
                    }
                }
            }
        }
    }
  
	public static Connection getConnection() throws SQLException {
		try {
			// Load the SQLite JDBC driver
			Class.forName("org.sqlite.JDBC");

			// Log database path for debugging
			System.out.println("Using database at: " + DB_URL);

			return DriverManager.getConnection(DB_URL);
		} catch (ClassNotFoundException e) {
			throw new SQLException("SQLite JDBC Driver not found", e);
		}
	}
    private void initializeDatabase() {
        try {
            // First, check if we can establish a database connection
            Connection testConnection = DatabaseManager.getConnection();
            if (testConnection != null) {
                testConnection.close();
                
                // If connection successful, proceed with initialization
                DatabaseManager.initializeDatabase();
            }
        } catch (SQLException e) {
            // Handle different types of SQL errors with specific messages
            String errorMessage = "Database initialization failed: ";
            
            if (e.getErrorCode() == 1045) {
                errorMessage += "Invalid username or password";
            } else if (e.getErrorCode() == 1049) {
                errorMessage += "Database does not exist";
            } else if (e.getErrorCode() == 1007) {
                errorMessage += "Database already exists";
            } else {
                errorMessage += e.getMessage();
            }
            
            // Log the error for debugging
            System.err.println("SQL Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
            
            // Show error to user and handle gracefully
            handleDatabaseError(errorMessage, e);
        }
    }
    private void fixNullApplicantIds() {
        boolean anyUpdated = false;
        
        for (int i = 0; i < allApplicants.size(); i++) {
            Applicant applicant = allApplicants.get(i);
            boolean needsUpdate = false;
            
            // Check for null or empty ID
            if (applicant.getId() == null || applicant.getId().trim().isEmpty()) {
                needsUpdate = true;
            }
            
            // If needs update, generate and assign a new ID
            if (needsUpdate) {
                String name = applicant.getName();
                // Handle null name edge case
                if (name == null || name.trim().isEmpty()) {
                    name = "Unknown_" + i; // Use index as fallback
                }
                
                String generatedId = DatabaseManager.generateApplicantId(name);
                applicant.setId(generatedId);
                
                // Update in database - use prepared statement for safety
                try (Connection conn = DatabaseManager.getConnection()) {
                    // First check if the record exists in the database
                    boolean recordExists = false;
                    try (PreparedStatement checkStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM applicants WHERE name = ?")) {
                        checkStmt.setString(1, name);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                recordExists = true;
                            }
                        }
                    }
                    
                    if (recordExists) {
                        // Update existing record
                        try (PreparedStatement pstmt = conn.prepareStatement(
                                "UPDATE applicants SET id = ? WHERE name = ?")) {
                            pstmt.setString(1, generatedId);
                            pstmt.setString(2, name);
                            
                            int affected = pstmt.executeUpdate();
                            if (affected > 0) {
                                anyUpdated = true;
                                System.out.println("Updated ID for " + name + ": " + generatedId);
                            } else {
                                System.out.println("Failed to update ID for " + name);
                            }
                        }
                    } else {
                        // This record isn't in the database yet - log the issue
                        System.out.println("Warning: Applicant '" + name + "' not found in database");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Error updating ID for " + name + ": " + e.getMessage());
                }
            }
        }
        
        if (anyUpdated) {
            // Refresh the UI
            SwingUtilities.invokeLater(() -> {
                listModel.clear();
                for (Applicant applicant : allApplicants) {
                    listModel.addElement(applicant);
                }
                // Force list to repaint
                applicantList.repaint();
            });
        }
    }

    private void createTablesIfNotExist() {
        try (Statement stmt = connection.createStatement()) {
            // Create applicants table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS applicants (
                    id VARCHAR(10) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    date_of_application DATE NOT NULL,
                    certificate VARCHAR(100) NOT NULL,
                    grade VARCHAR(50) NOT NULL,
                    email VARCHAR(100),
                    phone VARCHAR(20),
                    address TEXT,
                    date_of_birth DATE
                )
            """);
            
            // Additional table creation statements...
            
        } catch (SQLException e) {
            handleDatabaseError("Failed to create database tables", e);
        }
    }
 
    private void loadApplicantsFromDatabase() {
        allApplicants = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("Step 1: Database connection established");
            
            // First, let's verify the table structure
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "applicants", null);
            System.out.println("\nTable structure verification:");
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME") + 
                                 " Type: " + columns.getString("TYPE_NAME"));
            }
            
            // Now let's verify and load the data
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM applicants")) {
                
                System.out.println("\nStep 2: Executing data query");
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    try {
                        // Get each field with error checking
                        String id = rs.getString("id");
                        String name = rs.getString("name");
                        String dateStr = rs.getString("date_of_application");
                        String cert = rs.getString("certificate");
                        String grade = rs.getString("grade");
                        
                        System.out.println(String.format(
                            "Loading record %d: ID=%s, Name=%s, Date=%s, Cert=%s, Grade=%s",
                            count, id, name, dateStr, cert, grade
                        ));
                        
                        // Create applicant object
                        Applicant applicant = new Applicant(id, name, dateStr, cert, grade);
                        
                        // Load additional details
                        applicant.setEmail(rs.getString("email"));
                        applicant.setPhone(rs.getString("phone"));
                        applicant.setAddress(rs.getString("address"));
                        String dob = rs.getString("date_of_birth");
                        if (dob != null) {
                            applicant.setDateOfBirth(dob);
                        }
                        
                        allApplicants.add(applicant);
                    } catch (SQLException e) {
                        System.err.println("Error loading record " + count + ": " + e.getMessage());
                        continue; // Skip problematic records but continue loading others
                    }
                }
                
                System.out.println("\nStep 3: Loaded " + count + " applicants");
                
                // Verify the list model update
                SwingUtilities.invokeLater(() -> {
                    System.out.println("\nStep 4: Updating UI");
                    if (listModel != null) {
                        listModel.clear();
                        System.out.println("List model cleared");
                        
                        for (Applicant applicant : allApplicants) {
                            listModel.addElement(applicant);
                            System.out.println("Added to list model: " + applicant.getName());
                        }
                        
                        // Force the list to update
                        applicantList.revalidate();
                        applicantList.repaint();
                        
                        // Select the first item if available
                        if (!allApplicants.isEmpty()) {
                            applicantList.setSelectedIndex(0);
                            System.out.println("Selected first applicant");
                        }
                    } else {
                        System.err.println("ERROR: List model is null!");
                    }
                });
                
            } catch (SQLException e) {
                System.err.println("Error in data query: " + e.getMessage());
                e.printStackTrace();
                handleDatabaseError("Failed to query applicants data", e);
            }
        } catch (SQLException e) {
            System.err.println("Error in database connection: " + e.getMessage());
            e.printStackTrace();
            handleDatabaseError("Failed to connect to database", e);
        }
        
    }

  
    private void handleDatabaseError(String message, SQLException e) {
        // Log the full stack trace for debugging
        e.printStackTrace();
        
        // Build detailed error message
        StringBuilder errorDetails = new StringBuilder();
        errorDetails.append(message).append("\n\n");
        errorDetails.append("Error Code: ").append(e.getErrorCode()).append("\n");
        errorDetails.append("SQL State: ").append(e.getSQLState()).append("\n");
        errorDetails.append("Details: ").append(e.getMessage());
        
        // Show error dialog to user
        showCustomDialog(
            errorDetails.toString(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    private String createDatabaseErrorMessage(SQLException e) {
        String baseMessage = "Failed to load applicants from database: ";
        
        // Add specific messages for common SQL error codes
        switch (e.getErrorCode()) {
            case 0:
                return baseMessage + "Cannot connect to database server. Please check if the server is running.";
            case 1045:
                return baseMessage + "Invalid username or password for database access.";
            case 1049:
                return baseMessage + "Database does not exist.";
            case 1146:
                return baseMessage + "Required database table does not exist.";
            case 1064:
                return baseMessage + "SQL syntax error in query.";
            case 1042:
                return baseMessage + "Cannot connect to database server at specified host.";
            default:
                return baseMessage + e.getMessage();
        }
    }

    private int showRetryDialog() {
        return JOptionPane.showConfirmDialog(
            this,
            "Unable to connect to the database after multiple attempts.\n" +
            "Would you like to try again?",
            "Database Connection Error",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );
    }
   
    private void loadDocuments(Applicant applicant) {
        String sql = "SELECT document_type, file_path FROM documents WHERE applicant_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applicant.addDocument(
                        rs.getString("document_type"),
                        rs.getString("file_path")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateComponentColors(Container container, Color buttonColor) {
        for (Component component : container.getComponents()) {
            // If it's a button with white background, change it to blue
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                Color currentBg = button.getBackground();
                
                // Check if it's a white or default button
                if (currentBg.equals(Color.WHITE) || 
                    currentBg.equals(UIManager.getColor("Button.background"))) {
                    button.setBackground(buttonColor);
                    button.setForeground(Color.BLACK); // Changed from Color.WHITE to Color.BLACK
                    
                    // If it has a border that's a RoundedBorder, update that too
                    if (button.getBorder() instanceof RoundedBorder) {
                        button.setBorder(new RoundedBorder(buttonColor, 20));
                    }
                }
            }
            
            // Recursively process child containers
            if (component instanceof Container) {
                updateComponentColors((Container) component, buttonColor);
            }
        }
    }
    private void loadComments(Applicant applicant) {
        String sql = "SELECT timestamp, comment_text FROM comments WHERE applicant_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String timestamp = rs.getTimestamp("timestamp").toString();
                    String commentText = rs.getString("comment_text");
                    applicant.addComment(timestamp + ": " + commentText);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to save a new applicant
    
    // Method to update an existing applicant
    private void updateApplicant(Applicant applicant) {
        String sql = """
            UPDATE applicants 
            SET name = ?, date_of_application = ?, certificate = ?, grade = ?,
                email = ?, phone = ?, address = ?, date_of_birth = ?
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicant.getName());
            pstmt.setDate(2, java.sql.Date.valueOf(applicant.getDateOfApplication()));
            pstmt.setString(3, applicant.getCertificate());
            pstmt.setString(4, applicant.getGrade());
            pstmt.setString(5, applicant.getEmail());
            pstmt.setString(6, applicant.getPhone());
            pstmt.setString(7, applicant.getAddress());
            pstmt.setDate(8, java.sql.Date.valueOf(applicant.getDateOfBirth()));
            pstmt.setString(9, applicant.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            showCustomDialog(
                "Failed to update applicant: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    // Method to delete an applicant

    private void deleteRelatedRecords(String applicantId) {
        try {
            // Delete documents
            PreparedStatement docStmt = connection.prepareStatement(
                "DELETE FROM documents WHERE applicant_id = ?"
            );
            docStmt.setString(1, applicantId);
            docStmt.executeUpdate();
            
            // Delete comments
            PreparedStatement commentStmt = connection.prepareStatement(
                "DELETE FROM comments WHERE applicant_id = ?"
            );
            commentStmt.setString(1, applicantId);
            commentStmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Method to save a new comment
    private void saveComment(String applicantId, String commentText) {
        String sql = "INSERT INTO comments (applicant_id, comment_text) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicantId);
            pstmt.setString(2, commentText);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            showCustomDialog(
                "Failed to save comment: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Method to save a document
    private void saveDocument(String applicantId, String documentType, String filePath) {
        String sql = "INSERT INTO documents (applicant_id, document_type, file_path) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, applicantId);
            pstmt.setString(2, documentType);
            pstmt.setString(3, filePath);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            showCustomDialog(
                "Failed to save document: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private String generateEmail(String name) {
        String sanitizedName = name.toLowerCase().replaceAll("[^a-z]", "");
        return sanitizedName + "@email.com";
    }
    
    private String generatePhone() {
        Random rand = new Random();
        return "+" + (40 + rand.nextInt(10)) + rand.nextInt(10) + 
               rand.nextInt(10000000, 999999999);
    }
    
    private String generateAddress() {
        Random rand = new Random();
        String[] streets = {"Main St", "Oak Avenue", "Cedar Lane", "Pine Road", "Maple Drive"};
        String[] cities = {"London", "New York", "Sydney", "Toronto", "Berlin", "Paris"};
        
        return (rand.nextInt(1, 200)) + " " + 
               streets[rand.nextInt(streets.length)] + "," +
               cities[rand.nextInt(cities.length)];
    }
    
    private String generateDOB() {
        Random rand = new Random();
        int day = rand.nextInt(1, 29);
        int month = rand.nextInt(1, 13);
        int year = rand.nextInt(1990, 2006);
        
        return String.format("%02d/%02d/%04d", day, month, year);
    }
   
    
    private void setupUI() {
        // Main layout
        setLayout(new BorderLayout());
        
        // Top panel with search only (removed buttons)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Search field with icon
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setPreferredSize(new Dimension(300, 40));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new RoundedBorder(Color.WHITE, 20));
        
        JLabel searchIcon = new JLabel("\uD83D\uDD0D"); // Unicode magnifying glass
        searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));
        
        searchField = new JTextField("Search Applicants");
        searchField.setBorder(null);
        searchField.setForeground(Color.GRAY);
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search Applicants")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search Applicants");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterApplicants();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterApplicants();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterApplicants();
            }
        });
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Add only the search panel to the top panel (buttons removed)
        topPanel.add(searchPanel);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Main content panel with split pane
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setBackground(BACKGROUND_COLOR);
        
        // Left panel - Applicants list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        JLabel applicantsLabel = new JLabel("Applicants");
        applicantsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        applicantsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        listModel = new DefaultListModel<>();
        for (Applicant applicant : allApplicants) {
            listModel.addElement(applicant);
        }
        
        applicantList = new JList<>(listModel);
        applicantList.setCellRenderer(new ApplicantListCellRenderer());
        applicantList.setFixedCellHeight(80);
        applicantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        applicantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Applicant selected = applicantList.getSelectedValue();
                if (selected != null) {
                    displayApplicantDetails(selected);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(applicantList);
        scrollPane.setBorder(null);
        
        leftPanel.add(applicantsLabel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Right panel - Applicant details
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        JLabel detailsLabel = new JLabel("Applicants Details");
        detailsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        detailsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tabbedPane = new JTabbedPane();
        
        // Create the tabs
        personalDetailsPanel = createPersonalDetailsPanel();
        JPanel documentsPanel = createDocumentsPanel();
        JPanel qualificationPanel = createQualificationPanel();
        JPanel commentsPanel = createCommentsPanel(); // Add the Comments panel
        
        tabbedPane.addTab("Personal Details", personalDetailsPanel);
        tabbedPane.addTab("Documents", documentsPanel);
        tabbedPane.addTab("Qualification", qualificationPanel);
        tabbedPane.addTab("Comments", commentsPanel);
        
        // Customize tab styling
        tabbedPane.setUI(new CustomTabbedPaneUI());
        
        detailsPanel.add(detailsLabel, BorderLayout.NORTH);
        detailsPanel.add(tabbedPane, BorderLayout.CENTER);
        
        
        
        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(detailsPanel);
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Select first applicant by default
        if (!allApplicants.isEmpty()) {
            applicantList.setSelectedIndex(0);
        }
        
        // Update all button colors
        updateButtonColors();
        
        // Set visible
        setVisible(true);
    }

    private void updateQualificationTabFields(Applicant applicant) {
        Component[] qualComponents = ((JPanel)tabbedPane.getComponentAt(2)).getComponents();
        for (Component comp : qualComponents) {
            if (comp instanceof JPanel) {
                // The main content panel
                Component[] contentComps = ((JPanel) comp).getComponents();
                for (Component contentComp : contentComps) {
                    if (contentComp instanceof JTextField) {
                        JTextField field = (JTextField) contentComp;
                        String name = field.getName();
                        
                        if (name != null) {
                            switch (name) {
                                case "certType":
                                    field.setText(applicant.getCertificate());
                                    break;
                                case "certGrade":
                                    field.setText(applicant.getGrade());
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private JTextArea findAddressTextArea() {
        Component scrollPane = findComponentByType(personalDetailsPanel, JScrollPane.class);
        if (scrollPane != null) {
            Component view = ((JScrollPane) scrollPane).getViewport().getView();
            if (view instanceof JTextArea && "address".equals(view.getName())) {
                return (JTextArea) view;
            }
        }
        return null;
    }

    // Updated savePersonalDetails method with better address handling
    private void savePersonalDetails() {
        if (currentApplicant == null) {
            showCustomDialog(
                "Please select an applicant first.",
                "No Applicant Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            // Get the values from form fields
            String email = null;
            String phone = null;
            String address = null;
            String dateOfBirth = null;
            
            // Get email field value
            JTextField emailField = (JTextField) findComponentByName(personalDetailsPanel, "email");
            if (emailField != null && !emailField.getText().isEmpty() && 
                !emailField.getText().equals("Enter email address")) {
                email = emailField.getText().trim();
            }
            
            // Get phone field value
            JTextField contactField = (JTextField) findComponentByName(personalDetailsPanel, "contact");
            if (contactField != null && !contactField.getText().isEmpty() && 
                !contactField.getText().equals("Enter phone number")) {
                phone = contactField.getText().trim();
            }
            
            // Get address field value
            JTextArea addressArea = findAddressTextArea();
            if (addressArea != null) {
                System.out.println("Found address area with text: " + addressArea.getText());
                if (!addressArea.getText().isEmpty() && 
                    !addressArea.getText().equals("Enter full address")) {
                    address = addressArea.getText().trim();
                    System.out.println("Using address: " + address);
                }
            }
            
            // Get date of birth value
            JDateChooser dateChooser = findDateChooser();
            if (dateChooser != null && dateChooser.getDate() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                dateOfBirth = sdf.format(dateChooser.getDate());
                System.out.println("Using date of birth: " + dateOfBirth);
            }
            
            // Log all values for debugging
            System.out.println("Form values gathered:");
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("Address: " + address);
            System.out.println("Date of Birth: " + dateOfBirth);
            
            // Validate inputs (at least one field should be filled)
            if ((email == null || email.isEmpty()) && 
                (phone == null || phone.isEmpty()) && 
                (address == null || address.isEmpty()) && 
                dateOfBirth == null) {
                
                showCustomDialog(
                    "Please fill in at least one field to update.",
                    "Empty Fields",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            // Update the applicant object
            if (email != null) {
                currentApplicant.setEmail(email);
            }
            
            if (phone != null) {
                currentApplicant.setPhone(phone);
            }
            
            if (address != null) {
                currentApplicant.setAddress(address);
            }
            
            if (dateOfBirth != null) {
                currentApplicant.setDateOfBirth(dateOfBirth);
            }
            
            // Update the database
            boolean success = updateApplicantPersonalDetailsByName(currentApplicant);
            
            if (success) {
                showCustomDialog(
                    "Personal details saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                showCustomDialog(
                    "Failed to update details. Please try again.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showCustomDialog(
                "Error saving personal details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private JPanel createPersonalDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Full Name (non-editable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel fullNameLabel = new JLabel("Full Name");
        fullNameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(fullNameLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        RoundedTextField fullNameField = new RoundedTextField(FIELD_BACKGROUND, CORNER_RADIUS);
        fullNameField.setEditable(false); // Name stays non-editable
        fullNameField.setPreferredSize(new Dimension(200, 30));
        fullNameField.setName("fullName");
        panel.add(fullNameField, gbc);
        
        // Date of Birth with JDateChooser
        gbc.gridx = 1;
        gbc.gridy = 0;
        JLabel dobLabel = new JLabel("Date of Birth");
        dobLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(dobLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        
        // Create a panel to hold the date chooser
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setBackground(Color.WHITE);
        
        // Add the date chooser with proper styling
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setBackground(FIELD_BACKGROUND);
        dateChooser.setName("dobChooser");
        JButton saveButton = new JButton("Save Personal Details");
        saveButton.setBackground(BUTTON_COLOR);
        saveButton.setForeground(Color.BLACK); // Changed from Color.WHITE to Color.BLACK
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBorder(new RoundedBorder(BUTTON_COLOR, CORNER_RADIUS));
        saveButton.setPreferredSize(new Dimension(200, 40));
        // Create and style the date chooser's text field
        JTextField dateTextField = ((JTextField)dateChooser.getDateEditor().getUiComponent());
        dateTextField.setBackground(FIELD_BACKGROUND);
        dateTextField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Add a hint placeholder for the date format
        dateTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dateTextField.getText().equals("DD/MM/YYYY")) {
                    dateTextField.setText("");
                    dateTextField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (dateTextField.getText().isEmpty()) {
                    dateTextField.setText("DD/MM/YYYY");
                    dateTextField.setForeground(Color.GRAY);
                }
            }
        });
        
        if (dateTextField.getText().isEmpty()) {
            dateTextField.setText("DD/MM/YYYY");
            dateTextField.setForeground(Color.GRAY);
        }
        
        datePanel.add(dateChooser, BorderLayout.CENTER);
        panel.add(datePanel, gbc);
        
        // Email Address
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(emailLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        RoundedTextField emailField = new RoundedTextField(FIELD_BACKGROUND, CORNER_RADIUS);
        emailField.setName("email");
        emailField.setPreferredSize(new Dimension(200, 30));
        
        // Add email validation visual feedback
        emailField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            
            @Override
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
            
            @Override
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
            
            private void validateEmail() {
                String email = emailField.getText();
                if (!email.isEmpty() && !email.contains("@")) {
                    emailField.setBackground(new Color(255, 240, 240)); // Light red for invalid
                } else {
                    emailField.setBackground(FIELD_BACKGROUND);
                }
            }
        });
        
        panel.add(emailField, gbc);
        
        // Contact number
        gbc.gridx = 1;
        gbc.gridy = 2;
        JLabel contactLabel = new JLabel("Contact Number");
        contactLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(contactLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        RoundedTextField contactField = new RoundedTextField(FIELD_BACKGROUND, CORNER_RADIUS);
        contactField.setName("contact");
        contactField.setPreferredSize(new Dimension(200, 30));
        
        // Add placeholder text
        contactField.setText("Enter phone number");
        contactField.setForeground(Color.GRAY);
        contactField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (contactField.getText().equals("Enter phone number")) {
                    contactField.setText("");
                    contactField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (contactField.getText().isEmpty()) {
                    contactField.setText("Enter phone number");
                    contactField.setForeground(Color.GRAY);
                }
            }
        });
        
        panel.add(contactField, gbc);
        
        // Home Address
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JLabel addressLabel = new JLabel("Home Address");
        addressLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(addressLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JTextArea addressArea = new JTextArea();
        addressArea.setName("address");
        addressArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBackground(FIELD_BACKGROUND);
        addressArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add placeholder text
        addressArea.setText("Enter full address");
        addressArea.setForeground(Color.GRAY);
        addressArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (addressArea.getText().equals("Enter full address")) {
                    addressArea.setText("");
                    addressArea.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (addressArea.getText().isEmpty()) {
                    addressArea.setText("Enter full address");
                    addressArea.setForeground(Color.GRAY);
                }
            }
        });
        
        // Create a scroll pane for the address text area
        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        addressScrollPane.setBorder(new RoundedBorder(FIELD_BACKGROUND, CORNER_RADIUS));
        addressScrollPane.setPreferredSize(new Dimension(400, 80));
        panel.add(addressScrollPane, gbc);
        
        // Add Save Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 5, 15, 20); // More space above button
        
        
        
        // Add hover effect
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(new Color(55, 80, 109)); // Darker shade when hovering
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(REPORTS_BUTTON_COLOR);
            }
        });
        
        // Add action listener for saving data
        saveButton.addActionListener(e -> savePersonalDetails());
        
        panel.add(saveButton, gbc);
        
        // Add status label to show feedback
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JLabel statusLabel = new JLabel("");
        statusLabel.setName("statusLabel");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(statusLabel, gbc);
        
        return panel;
    } 
    // Helper method to handle null strings
    private String nullSafeString(String str) {
        return str == null ? "" : str;
    }

    // Helper method to find a component by name
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            
            if (component instanceof Container) {
                Component result = findComponentByName((Container) component, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    // Helper method to find a component by type
    private Component findComponentByType(Container container, Class<?> type) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                return component;
            }
            
            if (component instanceof Container) {
                Component result = findComponentByType((Container) component, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    // Update personal details by name
    public static boolean updatePersonalDetailsByName(String name, String email, 
                                              String phone, String address, String dateOfBirth) {
        String sql = """
            UPDATE applicants 
            SET email = ?, phone = ?, address = ?, date_of_birth = ?
            WHERE name = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Log the update operation for debugging
            System.out.println("Updating applicant: " + name);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("Address: " + address);
            System.out.println("Date of Birth: " + dateOfBirth);
            
            // Set parameters in the prepared statement
            if (email != null && !email.trim().isEmpty()) {
                pstmt.setString(1, email);
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                pstmt.setString(2, phone);
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            
            if (address != null && !address.trim().isEmpty()) {
                pstmt.setString(3, address);
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            
            // Convert date format for SQLite (yyyy-MM-dd)
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date date = inputFormat.parse(dateOfBirth);
                    String sqlDateStr = outputFormat.format(date);
                    pstmt.setString(4, sqlDateStr);
                    System.out.println("Converted date for SQL: " + sqlDateStr);
                } catch (Exception e) {
                    pstmt.setNull(4, Types.DATE);
                    System.err.println("Error parsing date: " + e.getMessage());
                }
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            
            pstmt.setString(5, name);
            
            // Execute the update
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows updated: " + rowsAffected);
            
            // Return true if at least one row was updated
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating applicant details: " + e.getMessage());
            return false;
        }
    }
    private String getTextFieldValue(String fieldName) {
        Component[] components = personalDetailsPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JTextField && fieldName.equals(component.getName())) {
                return ((JTextField) component).getText();
            }
        }
        return null;
    }
    private JDateChooser findDateChooser() {
        Component[] components = personalDetailsPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                Component[] panelComponents = ((JPanel) component).getComponents();
                for (Component panelComp : panelComponents) {
                    if (panelComp instanceof JDateChooser) {
                        return (JDateChooser) panelComp;
                    }
                }
            }
        }
        return null;
    }
    private JPanel createDocumentUploadSection(String title, String description) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(new Color(245, 245, 245));
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        // Document title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Document description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Upload section with icon and button
        JPanel uploadPanel = new JPanel();
        uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.X_AXIS));
        uploadPanel.setBackground(new Color(245, 245, 245));
        uploadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Upload icon
        JLabel uploadIcon = new JLabel("\uD83D\uDCC1"); // Unicode for document icon
        uploadIcon.setFont(new Font("SansSerif", Font.PLAIN, 24));
        
        // Status label for showing selected file
        JLabel statusLabel = new JLabel("No file selected");
        statusLabel.setName(title + "_status");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        
        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
     // Upload button
        JButton uploadButton = new JButton("Upload");
        uploadButton.setBackground(BUTTON_COLOR); // Use blue instead of any existing color
        uploadButton.setForeground(Color.BLACK);
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(new RoundedBorder(BUTTON_COLOR, 15));
        uploadButton.addActionListener(e -> openFileChooser(title));
        
        // View button - keep a different color to distinguish functionality
        JButton viewButton = new JButton("View");
        viewButton.setBackground(new Color(100, 150, 200)); // A different blue shade
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        viewButton.setBorder(new RoundedBorder(new Color(100, 150, 200), 15));
        viewButton.setEnabled(false); // Disabled by default
        viewButton.addActionListener(e -> viewDocument(title));
        viewButton.setName(title + "_view");
        
        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(200, 100, 100));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(new RoundedBorder(new Color(200, 100, 100), 15));
        deleteButton.setEnabled(false); // Disabled by default
        deleteButton.addActionListener(e -> deleteDocumentWithConfirmation(title));
        deleteButton.setName(title + "_delete");
        
        // Add buttons to the button panel with some spacing
        buttonPanel.add(uploadButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(viewButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(deleteButton);
        
        // Add components to the upload panel
        uploadPanel.add(uploadIcon);
        uploadPanel.add(Box.createHorizontalStrut(10));
        uploadPanel.add(statusLabel);
        uploadPanel.add(Box.createHorizontalGlue());
        
        // Add all components to the section panel
        sectionPanel.add(titleLabel);
        sectionPanel.add(Box.createVerticalStrut(5));
        sectionPanel.add(descLabel);
        sectionPanel.add(Box.createVerticalStrut(15));
        sectionPanel.add(uploadPanel);
        sectionPanel.add(Box.createVerticalStrut(15));
        sectionPanel.add(buttonPanel);
     
        return sectionPanel;
    }
    //Qualification
    
    private ImageIcon createUploadIcon() {
        // Create a simple upload icon using vector graphics
        BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw cloud shape
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRoundRect(8, 28, 32, 16, 10, 10);
        
        // Draw arrow
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawLine(24, 5, 24, 25);  // Arrow stem
        g2d.drawLine(16, 15, 24, 5);  // Arrow left side
        g2d.drawLine(32, 15, 24, 5);  // Arrow right side
        
        g2d.dispose();
        return new ImageIcon(img);
    }

    private JPanel createCommentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Comments area
        JTextArea commentsTextArea = new JTextArea();
        commentsTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        commentsTextArea.setLineWrap(true);
        commentsTextArea.setWrapStyleWord(true);
        commentsTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        commentsTextArea.setBackground(FIELD_BACKGROUND);
        commentsTextArea.setText("Enter Any Additional Comments Here...");
        commentsTextArea.setForeground(Color.GRAY);
        commentsTextArea.setName("comments");
        
        // Add focus listener to clear placeholder text
        commentsTextArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (commentsTextArea.getText().equals("Enter Any Additional Comments Here...")) {
                    commentsTextArea.setText("");
                    commentsTextArea.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (commentsTextArea.getText().isEmpty()) {
                    commentsTextArea.setText("Enter Any Additional Comments Here...");
                    commentsTextArea.setForeground(Color.GRAY);
                }
            }
        });
        
        // Scroll pane for comments area
        JScrollPane scrollPane = new JScrollPane(commentsTextArea);
        scrollPane.setBorder(new RoundedBorder(FIELD_BACKGROUND, 15));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        // Add comment history button
        JButton historyButton = new JButton("View Comment History");
        historyButton.setBackground(Color.DARK_GRAY);
        historyButton.setForeground(Color.BLACK);
        historyButton.setFocusPainted(false);
        historyButton.setBorder(new RoundedBorder(Color.DARK_GRAY, 15));
        historyButton.addActionListener(e -> showCommentHistory());
        
        // Add save button
        JButton saveButton = new JButton("Save Comment");
        saveButton.setBackground(REPORTS_BUTTON_COLOR);
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(new RoundedBorder(REPORTS_BUTTON_COLOR, 15));
        saveButton.addActionListener(e -> saveComment(commentsTextArea.getText()));
        
        buttonPanel.add(historyButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(saveButton);
        
        // Add to main panel
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // Method to save a comment for the current applicant
    private void saveComment(String commentText) {
        if (currentApplicant == null) {
            showCustomDialog(
                "Please select an applicant first.",
                "No Applicant Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        if (commentText.equals("Enter Any Additional Comments Here...") || commentText.trim().isEmpty()) {
            showCustomDialog(
                "Please enter a comment before saving.",
                "Empty Comment",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Add timestamp to comment
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedComment = timestamp + ": " + commentText;
        
        // Add comment to applicant
        currentApplicant.addComment(formattedComment);
        
        showCustomDialog(
            "Comment saved successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Reset the comment text area
        JTextArea commentsTextArea = findCommentsTextArea();
        if (commentsTextArea != null) {
            commentsTextArea.setText("Enter Any Additional Comments Here...");
            commentsTextArea.setForeground(Color.GRAY);
        }
    }

    private void showCommentHistory() {
        if (currentApplicant == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an applicant first.", 
                "No Applicant Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<String> comments = currentApplicant.getComments();
        
        if (comments.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No comments available for this applicant.", 
                "No Comments", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create a dialog to display comment history with rounded corners
        JDialog historyDialog = new JDialog(this, "Comment History", true);
        historyDialog.setSize(500, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());
        
        // Create a list to display comments
        DefaultListModel<String> commentListModel = new DefaultListModel<>();
        for (String comment : comments) {
            commentListModel.addElement(comment);
        }
        
        JList<String> commentList = new JList<>(commentListModel);
        commentList.setCellRenderer(new CommentListCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(commentList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create a panel with rounded corners for the dialog content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new RoundedBorder(PANEL_BORDER_COLOR, CORNER_RADIUS));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add close button with improved visibility
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(REPORTS_BUTTON_COLOR);
        closeButton.setForeground(BUTTON_TEXT_COLOR);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(new RoundedBorder(REPORTS_BUTTON_COLOR, CORNER_RADIUS));
        closeButton.addActionListener(e -> historyDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(closeButton);
        
        // Add panels to dialog
        historyDialog.add(contentPanel, BorderLayout.CENTER);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.setVisible(true);
    }
    // Helper method to find the comments text area
    private JTextArea findCommentsTextArea() {
        Component commentsPanel = tabbedPane.getComponentAt(3); // Comments tab index
        if (commentsPanel instanceof JPanel) {
            return findTextAreaInPanel((JPanel) commentsPanel, "comments");
        }
        return null;
    }

    // Helper method to recursively find a text area with a specific name in a panel
    private JTextArea findTextAreaInPanel(JPanel panel, String name) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextArea && name.equals(comp.getName())) {
                return (JTextArea) comp;
            } else if (comp instanceof JScrollPane) {
                Component viewComp = ((JScrollPane) comp).getViewport().getView();
                if (viewComp instanceof JTextArea && name.equals(viewComp.getName())) {
                    return (JTextArea) viewComp;
                }
            } else if (comp instanceof JPanel) {
                JTextArea result = findTextAreaInPanel((JPanel) comp, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

 // Custom renderer for comment list with rounded cell appearance
    private class CommentListCellRenderer extends JPanel implements ListCellRenderer<String> {
        private JTextArea commentArea = new JTextArea();
        private JLabel dateLabel = new JLabel();
        
        public CommentListCellRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(245, 245, 245), CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            setBackground(Color.WHITE);
            
            dateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            dateLabel.setForeground(Color.DARK_GRAY);
            
            commentArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            commentArea.setEditable(false);
            commentArea.setBackground(new Color(245, 245, 245));
            commentArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            add(dateLabel, BorderLayout.NORTH);
            add(commentArea, BorderLayout.CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, 
                String comment, 
                int index, 
                boolean isSelected, 
                boolean cellHasFocus) {
            
            // Split the comment into timestamp and content
            int colonIndex = comment.indexOf(":");
            if (colonIndex > 0) {
                String timestamp = comment.substring(0, colonIndex);
                String content = comment.substring(colonIndex + 1).trim();
                
                dateLabel.setText(timestamp);
                commentArea.setText(content);
            } else {
                commentArea.setText(comment);
                dateLabel.setText("");
            }
            
            if (isSelected) {
                setBackground(ACCENT_COLOR);
                commentArea.setBackground(new Color(200, 220, 240));
            } else {
                setBackground(Color.WHITE);
                commentArea.setBackground(new Color(245, 245, 245));
            }
            
            return this;
        }
    }
    // Helper method to create a document upload section
    private void updateDocumentStatus(String documentType, String fileName) {
        // Find the document panel that corresponds to the document type
        Component[] components = ((JPanel) tabbedPane.getComponentAt(1)).getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                
                if (view instanceof JPanel) {
                    JPanel container = (JPanel) view;
                    
                    // Look through the components in the container
                    for (Component containerComp : container.getComponents()) {
                        if (containerComp instanceof JPanel) {
                            JPanel sectionPanel = (JPanel) containerComp;
                            
                            // Check if this is the section we're looking for
                            Component[] sectionComps = sectionPanel.getComponents();
                            for (Component sectionComp : sectionComps) {
                                if (sectionComp instanceof JLabel) {
                                    JLabel label = (JLabel) sectionComp;
                                    if (label.getText().equals(documentType)) {
                                        // Found our section, now update the status label and buttons
                                        updateSectionComponents(sectionPanel, fileName);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper method to update components in a document section
    private void updateSectionComponents(JPanel sectionPanel, String fileName) {
        // Look for status label, view button, and delete button
        for (Component comp : sectionPanel.getComponents()) {
            // Update status label
            if (comp instanceof JLabel && comp.getName() != null && comp.getName().endsWith("_status")) {
                JLabel statusLabel = (JLabel) comp;
                statusLabel.setText(fileName);
                statusLabel.setForeground(new Color(0, 100, 0)); // Dark green
            }
            
            // Enable view and delete buttons if they exist
            if (comp instanceof JPanel) {
                for (Component panelComp : ((JPanel) comp).getComponents()) {
                    // Enable view button
                    if (panelComp instanceof JButton && panelComp.getName() != null && panelComp.getName().endsWith("_view")) {
                        ((JButton) panelComp).setEnabled(true);
                    }
                    
                    // Enable delete button
                    if (panelComp instanceof JButton && panelComp.getName() != null && panelComp.getName().endsWith("_delete")) {
                        ((JButton) panelComp).setEnabled(true);
                    }
                }
            }
        }
    }

   

    // Method to reset document status in UI
    private void resetDocumentStatus(String documentType) {
        // Same basic structure as updateDocumentStatus, but reset status and disable buttons
        Component[] components = ((JPanel) tabbedPane.getComponentAt(1)).getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                
                if (view instanceof JPanel) {
                    JPanel container = (JPanel) view;
                    
                    for (Component containerComp : container.getComponents()) {
                        if (containerComp instanceof JPanel) {
                            JPanel sectionPanel = (JPanel) containerComp;
                            
                            Component[] sectionComps = sectionPanel.getComponents();
                            for (Component sectionComp : sectionComps) {
                                if (sectionComp instanceof JLabel) {
                                    JLabel label = (JLabel) sectionComp;
                                    if (label.getText().equals(documentType)) {
                                        // Reset the components
                                        for (Component panelComp : sectionPanel.getComponents()) {
                                            if (panelComp instanceof JLabel && panelComp.getName() != null && 
                                                panelComp.getName().endsWith("_status")) {
                                                JLabel statusLabel = (JLabel) panelComp;
                                                statusLabel.setText("No file selected");
                                                statusLabel.setForeground(Color.GRAY);
                                            }
                                            
                                            if (panelComp instanceof JPanel) {
                                                for (Component buttonComp : ((JPanel) panelComp).getComponents()) {
                                                    if (buttonComp instanceof JButton && buttonComp.getName() != null) {
                                                        if (buttonComp.getName().endsWith("_view") || 
                                                            buttonComp.getName().endsWith("_delete")) {
                                                            ((JButton) buttonComp).setEnabled(false);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Helper method to update the document status label in the UI
   
   
    private void displayApplicantDetails(Applicant applicant) {
        currentApplicant = applicant;
        
        System.out.println("Displaying details for: " + applicant.getName());
        System.out.println("Date of Birth: " + applicant.getDateOfBirth());
        System.out.println("Address: " + applicant.getAddress());
        
        // Find all components in the personal details panel
        Component[] components = personalDetailsPanel.getComponents();
        
        // Track if we need to update placeholder texts
        boolean hasEmail = false;
        boolean hasPhone = false;
        boolean hasAddress = false;
        boolean hasDateOfBirth = false;
        
        for (Component component : components) {
            // Handle text fields
            if (component instanceof JTextField) {
                JTextField field = (JTextField) component;
                String name = field.getName();
                
                if (name != null) {
                    if (name.equals("fullName")) {
                        field.setText(applicant.getName());
                    } else if (name.equals("email")) {
                        String email = applicant.getEmail();
                        if (email != null && !email.isEmpty()) {
                            field.setText(email);
                            field.setForeground(Color.BLACK);
                            hasEmail = true;
                        }
                    } else if (name.equals("contact")) {
                        String phone = applicant.getPhone();
                        if (phone != null && !phone.isEmpty()) {
                            field.setText(phone);
                            field.setForeground(Color.BLACK);
                            hasPhone = true;
                        }
                    }
                }
            } 
            // Handle scroll panes (for address text area)
            else if (component instanceof JScrollPane) {
                Component view = ((JScrollPane) component).getViewport().getView();
                if (view instanceof JTextArea && view.getName() != null && view.getName().equals("address")) {
                    JTextArea addressArea = (JTextArea) view;
                    String address = applicant.getAddress();
                    System.out.println("Setting address in text area: " + address);
                    if (address != null && !address.isEmpty()) {
                        addressArea.setText(address);
                        addressArea.setForeground(Color.BLACK);
                        hasAddress = true;
                    }
                }
            }
            // Handle panels (for date chooser)
            else if (component instanceof JPanel) {
                for (Component panelComp : ((JPanel) component).getComponents()) {
                    if (panelComp instanceof JDateChooser) {
                        JDateChooser dateChooser = (JDateChooser) panelComp;
                        try {
                            String dateOfBirth = applicant.getDateOfBirth();
                            System.out.println("Setting date in chooser: " + dateOfBirth);
                            
                            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                                try {
                                    // Try parsing with dd/MM/yyyy format
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                                    sdf.setLenient(false);
                                    java.util.Date date = sdf.parse(dateOfBirth);
                                    dateChooser.setDate(date);
                                    hasDateOfBirth = true;
                                    
                                    // Update text field color
                                    JTextField dateField = ((JTextField)dateChooser.getDateEditor().getUiComponent());
                                    dateField.setForeground(Color.BLACK);
                                } catch (Exception e) {
                                    System.err.println("Error parsing date with dd/MM/yyyy format: " + e.getMessage());
                                    
                                    // Try alternative format (yyyy-MM-dd)
                                    try {
                                        java.text.SimpleDateFormat altSdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                                        altSdf.setLenient(false);
                                        java.util.Date date = altSdf.parse(dateOfBirth);
                                        dateChooser.setDate(date);
                                        hasDateOfBirth = true;
                                        
                                        // Update text field color
                                        JTextField dateField = ((JTextField)dateChooser.getDateEditor().getUiComponent());
                                        dateField.setForeground(Color.BLACK);
                                    } catch (Exception e2) {
                                        System.err.println("Error parsing date with yyyy-MM-dd format: " + e2.getMessage());
                                        dateChooser.setDate(null);
                                    }
                                }
                            } else {
                                dateChooser.setDate(null);
                            }
                        } catch (Exception e) {
                            System.err.println("Error setting date: " + e.getMessage());
                            e.printStackTrace();
                            dateChooser.setDate(null);
                        }
                    }
                }
            }
        }
        
        // Reset placeholder texts for empty fields
        if (!hasEmail) {
            JTextField emailField = (JTextField) findComponentByName(personalDetailsPanel, "email");
            if (emailField != null) {
                emailField.setText("Enter email address");
                emailField.setForeground(Color.GRAY);
            }
        }
        
        if (!hasPhone) {
            JTextField contactField = (JTextField) findComponentByName(personalDetailsPanel, "contact");
            if (contactField != null) {
                contactField.setText("Enter phone number");
                contactField.setForeground(Color.GRAY);
            }
        }
        
        if (!hasAddress) {
            System.out.println("No address found, setting placeholder");
            Component scrollPane = findComponentByType(personalDetailsPanel, JScrollPane.class);
            if (scrollPane != null) {
                Component view = ((JScrollPane) scrollPane).getViewport().getView();
                if (view instanceof JTextArea) {
                    JTextArea addressArea = (JTextArea) view;
                    addressArea.setText("Enter full address");
                    addressArea.setForeground(Color.GRAY);
                }
            }
        }
        
        // Clear status label
        JLabel statusLabel = (JLabel) findComponentByName(personalDetailsPanel, "statusLabel");
        if (statusLabel != null) {
            statusLabel.setText("");
        }
        
        // Update qualification tab fields (existing code)
        updateQualificationTabFields(applicant);
     // Update qualification tab fields
     
        
        // Load documents for this applicant
        loadDocumentsForApplicant(applicant.getId());
        
        // Update certificate document status in Qualification tab
        updateCertificateDocumentStatus(applicant);
       
    }
    
    private void updateCertificateDocumentStatus(Applicant applicant) {
        // Check if applicant has a certificate document
        String certificatePath = applicant.getDocument("Certificate");
        
        if (certificatePath != null && !certificatePath.isEmpty()) {
            // Get the filename from the path
            File file = new File(certificatePath);
            if (file.exists()) {
                // Update status label
                JLabel statusLabel = findStatusLabel("Certificate_status");
                if (statusLabel != null) {
                    statusLabel.setText(file.getName());
                    statusLabel.setForeground(new Color(0, 100, 0)); // Dark green
                }
                
                // Enable view and delete buttons
                JButton viewButton = findButton("Certificate_view");
                if (viewButton != null) {
                    viewButton.setEnabled(true);
                }
                
                JButton deleteButton = findButton("Certificate_delete");
                if (deleteButton != null) {
                    deleteButton.setEnabled(true);
                }
            }
        } else {
            // Reset to default state if no certificate
            JLabel statusLabel = findStatusLabel("Certificate_status");
            if (statusLabel != null) {
                statusLabel.setText("No file selected");
                statusLabel.setForeground(Color.GRAY);
            }
            
            // Disable view and delete buttons
            JButton viewButton = findButton("Certificate_view");
            if (viewButton != null) {
                viewButton.setEnabled(false);
            }
            
            JButton deleteButton = findButton("Certificate_delete");
            if (deleteButton != null) {
                deleteButton.setEnabled(false);
            }
        }
    }
    private JLabel findStatusLabel(String name) {
        // Look in the Qualification tab (index 2)
        Component qualificationPanel = tabbedPane.getComponentAt(2);
        if (qualificationPanel instanceof JPanel) {
            return (JLabel) findComponentByName((Container) qualificationPanel, name);
        }
        return null;
    }
    private JButton findButton(String name) {
        // Look in the Qualification tab (index 2)
        Component qualificationPanel = tabbedPane.getComponentAt(2);
        if (qualificationPanel instanceof JPanel) {
            return (JButton) findComponentByName((Container) qualificationPanel, name);
        }
        return null;
    }
    private void loadDocumentsForApplicant(String applicantId) {
        System.out.println("Loading documents for applicant: " + applicantId);
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT document_type, file_path FROM documents WHERE applicant_id = ?")) {
            
            pstmt.setString(1, applicantId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // Clear existing documents in the applicant object
                if (currentApplicant != null) {
                    currentApplicant.clearDocuments();
                }
                
                // Process each document
                while (rs.next()) {
                    String docType = rs.getString("document_type");
                    String filePath = rs.getString("file_path");
                    
                    System.out.println("Found document: " + docType + " at " + filePath);
                    
                    // Update the applicant object
                    if (currentApplicant != null) {
                        currentApplicant.addDocument(docType, filePath);
                    }
                    
                    // Update UI to show document status
                    File file = new File(filePath);
                    if (file.exists()) {
                        updateDocumentStatus(docType, file.getName());
                    } else {
                        System.out.println("Warning: File does not exist at " + filePath);
                        // Update UI to show missing file status
                        updateDocumentStatus(docType, "File missing: " + file.getName());
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading documents: " + e.getMessage());
        }
    }
    private boolean updateApplicantPersonalDetailsByName(Applicant applicant) {
        String sql = """
            UPDATE applicants 
            SET email = ?, phone = ?, address = ?, date_of_birth = ?
            WHERE name = ?
        """;
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                // Log the update operation
                System.out.println("Updating applicant details for: " + applicant.getName());
                System.out.println("Email: " + applicant.getEmail());
                System.out.println("Phone: " + applicant.getPhone());
                System.out.println("Address: " + applicant.getAddress());
                System.out.println("Date of Birth: " + applicant.getDateOfBirth());
                
                // Set parameters with careful null handling
                if (applicant.getEmail() != null && !applicant.getEmail().trim().isEmpty()) {
                    pstmt.setString(1, applicant.getEmail().trim());
                } else {
                    pstmt.setNull(1, Types.VARCHAR);
                }
                
                if (applicant.getPhone() != null && !applicant.getPhone().trim().isEmpty()) {
                    pstmt.setString(2, applicant.getPhone().trim());
                } else {
                    pstmt.setNull(2, Types.VARCHAR);
                }
                
                if (applicant.getAddress() != null && !applicant.getAddress().trim().isEmpty()) {
                    pstmt.setString(3, applicant.getAddress().trim());
                } else {
                    pstmt.setNull(3, Types.VARCHAR);
                }
                
                // Convert date format for SQLite (yyyy-MM-dd)
                String dateOfBirth = applicant.getDateOfBirth();
                if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                    try {
                        // First try parsing with dd/MM/yyyy format (UI format)
                        java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        inputFormat.setLenient(false);
                        java.util.Date date = inputFormat.parse(dateOfBirth);
                        
                        // Convert to SQLite format
                        java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        String sqlDateStr = outputFormat.format(date);
                        
                        pstmt.setString(4, sqlDateStr);
                        System.out.println("Converted date for SQL: " + sqlDateStr);
                    } catch (Exception e) {
                        System.err.println("Error parsing date as dd/MM/yyyy: " + e.getMessage());
                        
                        // Try alternative format (maybe it's already in SQLite format)
                        try {
                            java.text.SimpleDateFormat altFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                            altFormat.setLenient(false);
                            java.util.Date date = altFormat.parse(dateOfBirth);
                            
                            // If we get here, it's already in SQL format
                            pstmt.setString(4, dateOfBirth);
                            System.out.println("Date is already in SQL format: " + dateOfBirth);
                        } catch (Exception e2) {
                            System.err.println("Alternative date parsing failed: " + e2.getMessage());
                            pstmt.setNull(4, Types.DATE);
                        }
                    }
                } else {
                    pstmt.setNull(4, Types.DATE);
                }
                
                // Set the applicant name for WHERE clause
                pstmt.setString(5, applicant.getName());
                
                // Execute the update
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Rows updated: " + rowsAffected);
                
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    
    // Custom cell renderer for applicant list
    private class ApplicantListCellRenderer extends JPanel implements ListCellRenderer<Applicant> {
        private JLabel nameLabel = new JLabel();
        private JLabel idLabel = new JLabel();
        
        public ApplicantListCellRenderer() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            nameLabel.setForeground(Color.BLACK);
            
            idLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            idLabel.setForeground(Color.GRAY);
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(idLabel);
            
            add(textPanel, BorderLayout.CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(
                JList<? extends Applicant> list, 
                Applicant applicant, 
                int index, 
                boolean isSelected, 
                boolean cellHasFocus) {
            
            nameLabel.setText(applicant.getName());
            
            // Display the ID
            if (applicant.getId() != null && !applicant.getId().isEmpty()) {
                idLabel.setText(applicant.getId());
            } else {
                // If ID is empty, we can show a placeholder or generate one on-the-fly
                idLabel.setText("ID: (not assigned)");
            }
            
            if (isSelected) {
                setBackground(ACCENT_COLOR);
                nameLabel.setForeground(Color.WHITE);
                idLabel.setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                nameLabel.setForeground(Color.BLACK);
                idLabel.setForeground(Color.GRAY);
            }
            
            return this;
        }
    }
    private void ensureAllApplicantsHaveIds() {
        boolean anyUpdated = false;
        
        for (Applicant applicant : allApplicants) {
            if (applicant.getId() == null || applicant.getId().trim().isEmpty()) {
                // Generate an ID
                String generatedId = DatabaseManager.generateApplicantId(applicant.getName());
                applicant.setId(generatedId);
                
                // Update in database
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE applicants SET id = ? WHERE name = ?")) {
                    
                    pstmt.setString(1, generatedId);
                    pstmt.setString(2, applicant.getName());
                    
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        anyUpdated = true;
                        System.out.println("Updated ID for " + applicant.getName() + ": " + generatedId);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Error updating ID for " + applicant.getName() + ": " + e.getMessage());
                }
            }
        }
        
        if (anyUpdated) {
            // Refresh the UI
            listModel.clear();
            for (Applicant applicant : allApplicants) {
                listModel.addElement(applicant);
            }
        }
    }
    private void showCustomDialog(String message, String title, int messageType) {
        // Create custom dialog
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        
        // Create message panel with rounded corners
        JPanel messagePanel = new JPanel(new BorderLayout(20, 20));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(PANEL_BORDER_COLOR, CORNER_RADIUS),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Add appropriate icon
        JLabel iconLabel = new JLabel();
        switch (messageType) {
            case JOptionPane.WARNING_MESSAGE:
                iconLabel.setText("⚠️");
                break;
            case JOptionPane.INFORMATION_MESSAGE:
                iconLabel.setText("ℹ️");
                break;
            case JOptionPane.ERROR_MESSAGE:
                iconLabel.setText("❌");
                break;
        }
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        
        // Add message
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        messagePanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add OK button with rounded corners
        JButton okButton = new JButton("OK");
        okButton.setBackground(REPORTS_BUTTON_COLOR);
        okButton.setForeground(BUTTON_TEXT_COLOR);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        okButton.setBorder(new RoundedBorder(REPORTS_BUTTON_COLOR, CORNER_RADIUS));
        okButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);
        
        messagePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add to dialog
        dialog.add(messagePanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    // Custom TabbedPane UI
 // Custom TabbedPane UI with improved rounded tabs
    private class CustomTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            lightHighlight = Color.WHITE;
            shadow = Color.WHITE;
            darkShadow = Color.WHITE;
            focus = Color.WHITE;
        }
        
        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isSelected) {
                g2d.setColor(Color.BLACK);
                // Create a more pronounced rounded rect for selected tabs
                g2d.fillRoundRect(x, y, w, h + 5, CORNER_RADIUS, CORNER_RADIUS);
            }
            g2d.dispose();
        }
        
        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isSelected) {
                g2d.setColor(Color.BLACK);
            } else {
                g2d.setColor(new Color(230, 230, 230));
            }
            
            // Create consistent rounded corners for all tabs
            g2d.fillRoundRect(x, y, w, h, CORNER_RADIUS, CORNER_RADIUS);
            g2d.dispose();
        }
        
        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // Do not paint the content border
        }
        
        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            if (isSelected) {
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(Color.BLACK);
            }
            
            // Make the font a bit bolder for better visibility
            Font boldFont = font.deriveFont(Font.BOLD);
            g2d.setFont(boldFont);
            g2d.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2d.dispose();
        }
    }
    // Custom rounded border
    private static class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;
        
        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius / 2, this.radius / 2, this.radius / 2, this.radius / 2);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    private void openFileChooser(String documentType) {
        if (currentApplicant == null) {
            showCustomDialog(
                "Please select an applicant first.",
                "No Applicant Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select " + documentType);
        
        // Set file filter based on document type
        if (documentType.contains("Certificate")) {
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        } else if (documentType.equals("Profile Picture") || documentType.equals("Passport")) {
            // For image documents, only allow PNG
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        } else {
            // For other documents, allow common image formats
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validate file extension first
            String fileExtension = getFileExtension(selectedFile).toLowerCase();
            
            // Verify document type requirements
            if (documentType.equals("Profile Picture") && !fileExtension.equals("png")) {
                showCustomDialog(
                    "Profile Picture must be a PNG file.",
                    "Invalid File Format",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            if (documentType.equals("Passport") && !fileExtension.equals("png")) {
                showCustomDialog(
                    "Passport image must be a PNG file.",
                    "Invalid File Format",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Now validate image dimensions for specific document types
            if (documentType.equals("Profile Picture") || documentType.equals("Passport")) {
                try {
                    BufferedImage img = ImageIO.read(selectedFile);
                    if (img == null) {
                        showCustomDialog(
                            "Could not read the image file. Please select a valid image.",
                            "Invalid Image",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    
                    int width = img.getWidth();
                    int height = img.getHeight();
                    
                    // Different dimension limits based on document type
                    if (documentType.equals("Profile Picture") && (width > 300 || height > 300)) {
                        showCustomDialog(
                            "Profile Picture dimensions must not exceed 300x300 pixels. Current dimensions: " 
                                + width + "x" + height,
                            "Image Too Large",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    } else if (documentType.equals("Passport") && (width > 500 || height > 500)) {
                        showCustomDialog(
                            "Passport image dimensions must not exceed 500x500 pixels. Current dimensions: " 
                                + width + "x" + height,
                            "Image Too Large",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    
                    // Log the successful validation
                    System.out.println("Image validated successfully: " + documentType + 
                                     " - Dimensions: " + width + "x" + height);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    showCustomDialog(
                        "Error reading image dimensions: " + e.getMessage(),
                        "Image Validation Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }
            
            // Continue with existing file saving logic...
            String applicantId = currentApplicant.getId();
            
            try {
                // Create a dedicated storage location
                File storageDir = new File("documents/" + applicantId);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                
                // Create a unique filename with timestamp
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String newFileName = documentType.replaceAll("\\s+", "_") + "_" + timestamp + "." + fileExtension;
                
                // Create destination file
                File destFile = new File(storageDir, newFileName);
                
                // Copy the file
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                String storedFilePath = destFile.getAbsolutePath();
                
                // Check if this document type already exists
                boolean documentExists = false;
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement checkStmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM documents WHERE applicant_id = ? AND document_type = ?")) {
                    
                    checkStmt.setString(1, applicantId);
                    checkStmt.setString(2, documentType);
                    
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            documentExists = rs.getInt(1) > 0;
                        }
                    }
                }
                
                // Update or insert the document record
                boolean success;
                if (documentExists) {
                    // Update existing document
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement updateStmt = conn.prepareStatement(
                             "UPDATE documents SET file_path = ? WHERE applicant_id = ? AND document_type = ?")) {
                        
                        updateStmt.setString(1, storedFilePath);
                        updateStmt.setString(2, applicantId);
                        updateStmt.setString(3, documentType);
                        
                        success = updateStmt.executeUpdate() > 0;
                    }
                } else {
                    // Insert new document
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement insertStmt = conn.prepareStatement(
                             "INSERT INTO documents (applicant_id, document_type, file_path) VALUES (?, ?, ?)")) {
                        
                        insertStmt.setString(1, applicantId);
                        insertStmt.setString(2, documentType);
                        insertStmt.setString(3, storedFilePath);
                        
                        success = insertStmt.executeUpdate() > 0;
                    }
                }
                
                if (success) {
                    // Update the applicant object
                    currentApplicant.addDocument(documentType, storedFilePath);
                    
                    // Update the UI
                    updateDocumentStatus(documentType, selectedFile.getName());
                    
                    showCustomDialog(
                        documentType + " uploaded successfully!",
                        "Upload Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    showCustomDialog(
                        "Failed to update database with document information.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                showCustomDialog(
                    "Error during file upload: " + e.getMessage(),
                    "Upload Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    // Custom rounded text field
    private static class RoundedTextField extends JTextField {
        private Color backgroundColor;
        private int radius;
        
        public RoundedTextField(Color backgroundColor, int radius) {
            super();
            this.backgroundColor = backgroundColor;
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }
    // Don't forget to close the database connection when the application closes
    @Override
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
      
}
 // Data Validator Class for validating user inputs
    public static class DataValidator {
        // Email validation
        public static boolean isValidEmail(String email) {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            // Basic email validation: contains @ and at least one dot after @
            int atIndex = email.indexOf('@');
            if (atIndex < 1) {
                return false;
            }
            
            int dotIndex = email.indexOf('.', atIndex);
            return dotIndex > atIndex + 1 && dotIndex < email.length() - 1;
        }
        
        // Phone validation
        public static boolean isValidPhone(String phone) {
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            // Allow digits, +, -, space, and parentheses
            return phone.matches("[\\d\\+\\-\\s\\(\\)]+") && phone.length() >= 7;
        }
        
        // Date validation
        public static boolean isValidDate(String dateStr) {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return false;
            }
            
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false); // Strict date parsing
                sdf.parse(dateStr);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        // Format date for SQLite (yyyy-MM-dd)
        public static String formatDateForSQLite(String dateStr) {
            try {
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (Exception e) {
                return null;
            }
        }
    }

    // Enhanced database operations for personal details
    public class PersonalDetailsDAO {
        
        // Get applicant's personal details from database
        public static Map<String, String> getPersonalDetails(String applicantId) {
            Map<String, String> details = new HashMap<>();
            
            String sql = "SELECT email, phone, address, date_of_birth FROM applicants WHERE id = ?";
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, applicantId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        details.put("email", rs.getString("email"));
                        details.put("phone", rs.getString("phone"));
                        details.put("address", rs.getString("address"));
                        
                        // Convert date format from SQLite (yyyy-MM-dd) to display format (dd/MM/yyyy)
                        Date dob = rs.getDate("date_of_birth");
                        if (dob != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                            details.put("date_of_birth", sdf.format(dob));
                        } else {
                            details.put("date_of_birth", null);
                        }
                    }
                }
                
                return details;
                
            } catch (SQLException e) {
                e.printStackTrace();
                return details;
            }
        }
        
        // Update applicant's personal details in database
        public static boolean updatePersonalDetails(String applicantId, Map<String, String> details) {
            String sql = """
                UPDATE applicants 
                SET email = ?, phone = ?, address = ?, date_of_birth = ?
                WHERE id = ?
            """;
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, details.get("email"));
                pstmt.setString(2, details.get("phone"));
                pstmt.setString(3, details.get("address"));
                
                // Format date for SQLite
                String dateOfBirth = details.get("date_of_birth");
                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    String sqlDateStr = DataValidator.formatDateForSQLite(dateOfBirth);
                    if (sqlDateStr != null) {
                        pstmt.setString(4, sqlDateStr);
                    } else {
                        pstmt.setNull(4, Types.DATE);
                    }
                } else {
                    pstmt.setNull(4, Types.DATE);
                }
                
                pstmt.setString(5, applicantId);
                
                return pstmt.executeUpdate() > 0;
                
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        // Check if applicant exists in database
        public static boolean applicantExists(String applicantId) {
            String sql = "SELECT COUNT(*) FROM applicants WHERE id = ?";
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, applicantId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
                
                return false;
                
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // Database implementation for the personal details data
    public class DBImplementation {
        
    	private boolean updateApplicantPersonalDetailsByName(Applicant applicant) {
    	    String sql = """
    	        UPDATE applicants 
    	        SET email = ?, phone = ?, address = ?, date_of_birth = ?
    	        WHERE name = ?
    	    """;
    	    
    	    try (Connection conn = DatabaseManager.getConnection()) {
    	        // Log connection status
    	        System.out.println("Database connection established successfully: " + !conn.isClosed());
    	        
    	        // Print applicant name for debugging
    	        System.out.println("Updating applicant with Name: " + applicant.getName());
    	        
    	        // Prepare statement with debugging
    	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    	            
    	            // Log the update operation for debugging
    	            System.out.println("Updating applicant details for Name: " + applicant.getName());
    	            System.out.println("Email: " + applicant.getEmail());
    	            System.out.println("Phone: " + applicant.getPhone());
    	            System.out.println("Address: " + applicant.getAddress());
    	            System.out.println("Date of Birth: " + applicant.getDateOfBirth());
    	            
    	            // Set parameters in the prepared statement with careful null handling
    	            if (applicant.getEmail() != null && !applicant.getEmail().trim().isEmpty()) {
    	                pstmt.setString(1, applicant.getEmail().trim());
    	            } else {
    	                pstmt.setNull(1, Types.VARCHAR);
    	            }
    	            
    	            if (applicant.getPhone() != null && !applicant.getPhone().trim().isEmpty()) {
    	                pstmt.setString(2, applicant.getPhone().trim());
    	            } else {
    	                pstmt.setNull(2, Types.VARCHAR);
    	            }
    	            
    	            if (applicant.getAddress() != null && !applicant.getAddress().trim().isEmpty()) {
    	                pstmt.setString(3, applicant.getAddress().trim());
    	            } else {
    	                pstmt.setNull(3, Types.VARCHAR);
    	            }
    	            
    	            // Convert date format for SQLite (yyyy-MM-dd)
    	            String dateOfBirth = applicant.getDateOfBirth();
    	            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
    	                try {
    	                    // First try standard format dd/MM/yyyy
    	                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
    	                    inputFormat.setLenient(false); // Strict date parsing
    	                    java.util.Date date = inputFormat.parse(dateOfBirth);
    	                    
    	                    // Format for SQLite
    	                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
    	                    String sqlDateStr = outputFormat.format(date);
    	                    
    	                    pstmt.setString(4, sqlDateStr);
    	                    System.out.println("Converted date for SQL: " + sqlDateStr);
    	                } catch (Exception e) {
    	                    System.err.println("Error parsing date: " + e.getMessage());
    	                    System.out.println("Attempting alternate date format...");
    	                    
    	                    // Try alternative date formats
    	                    try {
    	                        // Try to parse as yyyy-MM-dd (maybe it's already in SQLite format)
    	                        java.text.SimpleDateFormat altFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
    	                        altFormat.setLenient(false);
    	                        altFormat.parse(dateOfBirth); // Just to validate
    	                        
    	                        // If we get here, it's a valid yyyy-MM-dd date
    	                        pstmt.setString(4, dateOfBirth);
    	                        System.out.println("Using date directly: " + dateOfBirth);
    	                    } catch (Exception e2) {
    	                        System.err.println("Alternative date parsing failed: " + e2.getMessage());
    	                        pstmt.setNull(4, Types.DATE);
    	                    }
    	                }
    	            } else {
    	                pstmt.setNull(4, Types.DATE);
    	            }
    	            
    	            // CRITICAL: Set the applicant name parameter (WHERE clause)
    	            pstmt.setString(5, applicant.getName());
    	            
    	            // Print the complete SQL statement for debugging
    	            System.out.println("Executing SQL: " + sql);
    	            System.out.println("With parameters: 1=" + applicant.getEmail() + 
    	                             ", 2=" + applicant.getPhone() + 
    	                             ", 3=" + applicant.getAddress() + 
    	                             ", 4=" + applicant.getDateOfBirth() + 
    	                             ", 5=" + applicant.getName());
    	            
    	            // Execute the update
    	            int rowsAffected = pstmt.executeUpdate();
    	            System.out.println("Rows updated: " + rowsAffected);
    	            
    	            // Return true if at least one row was updated
    	            return rowsAffected > 0;
    	        }
    	        
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	        System.err.println("SQL Error Code: " + e.getErrorCode());
    	        System.err.println("SQL State: " + e.getSQLState());
    	        System.err.println("Error Message: " + e.getMessage());
    	        
    	        showCustomDialog(
    	            "Failed to update applicant details: " + e.getMessage(),
    	            "Database Error",
    	            JOptionPane.ERROR_MESSAGE
    	        );
    	        return false;
    	    }
    	}
    }
    private void deleteApplicant(String applicantId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // First delete all documents for this applicant
                deleteAllDocumentsForApplicant(applicantId);
                
                // Delete comments
                try (PreparedStatement commentStmt = conn.prepareStatement(
                         "DELETE FROM comments WHERE applicant_id = ?")) {
                    commentStmt.setString(1, applicantId);
                    commentStmt.executeUpdate();
                }
                
                // Finally delete the applicant
                try (PreparedStatement applicantStmt = conn.prepareStatement(
                         "DELETE FROM applicants WHERE id = ?")) {
                    applicantStmt.setString(1, applicantId);
                    applicantStmt.executeUpdate();
                }
                
                // Commit transaction
                conn.commit();
                
                // Update UI
                int index = -1;
                for (int i = 0; i < listModel.size(); i++) {
                    if (listModel.getElementAt(i).getId().equals(applicantId)) {
                        index = i;
                        break;
                    }
                }
                
                if (index >= 0) {
                    listModel.remove(index);
                    
                    // Select a new applicant if available
                    if (listModel.size() > 0) {
                        applicantList.setSelectedIndex(Math.min(index, listModel.size() - 1));
                    } else {
                        // Clear UI if no applicants left
                        currentApplicant = null;
                        clearDetailsPanel();
                    }
                }
                
                showCustomDialog(
                    "Applicant deleted successfully.",
                    "Delete Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (SQLException e) {
                // Rollback on error
                conn.rollback();
                throw e;
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showCustomDialog(
                "Failed to delete applicant: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Helper method to clear the details panel when no applicant is selected
    private void clearDetailsPanel() {
        // Clear name field
        JTextField nameField = (JTextField) findComponentByName(personalDetailsPanel, "fullName");
        if (nameField != null) {
            nameField.setText("");
        }
        
        // Clear email field
        JTextField emailField = (JTextField) findComponentByName(personalDetailsPanel, "email");
        if (emailField != null) {
            emailField.setText("Enter email address");
            emailField.setForeground(Color.GRAY);
        }
        
        // Clear phone field
        JTextField phoneField = (JTextField) findComponentByName(personalDetailsPanel, "contact");
        if (phoneField != null) {
            phoneField.setText("Enter phone number");
            phoneField.setForeground(Color.GRAY);
        }
        
        // Clear address field
        JTextArea addressArea = findAddressTextArea();
        if (addressArea != null) {
            addressArea.setText("Enter full address");
            addressArea.setForeground(Color.GRAY);
        }
        
        // Clear date chooser
        JDateChooser dateChooser = findDateChooser();
        if (dateChooser != null) {
            dateChooser.setDate(null);
            JTextField dateField = ((JTextField)dateChooser.getDateEditor().getUiComponent());
            dateField.setText("DD/MM/YYYY");
            dateField.setForeground(Color.GRAY);
        }
        
        // Reset document statuses
        resetAllDocumentStatuses();
        
        // Clear certificate and grade fields
        Component[] qualComponents = ((JPanel)tabbedPane.getComponentAt(2)).getComponents();
        for (Component comp : qualComponents) {
            if (comp instanceof JPanel) {
                Component[] contentComps = ((JPanel) comp).getComponents();
                for (Component contentComp : contentComps) {
                    if (contentComp instanceof JTextField) {
                        JTextField field = (JTextField) contentComp;
                        field.setText("");
                    }
                }
            }
        }
    }

    // Helper method to reset all document statuses
    private void resetAllDocumentStatuses() {
        // Get the documents panel
        Component documentsPanel = tabbedPane.getComponentAt(1);
        if (documentsPanel instanceof JPanel) {
            // Find the scroll pane containing the document sections
            Component[] panelComponents = ((JPanel) documentsPanel).getComponents();
            for (Component comp : panelComponents) {
                if (comp instanceof JScrollPane) {
                    Component view = ((JScrollPane) comp).getViewport().getView();
                    if (view instanceof JPanel) {
                        // This is the document container
                        Component[] sections = ((JPanel) view).getComponents();
                        for (Component section : sections) {
                            if (section instanceof JPanel) {
                                // Find the section title
                                String documentType = null;
                                Component[] sectionComps = ((JPanel) section).getComponents();
                                for (Component sectionComp : sectionComps) {
                                    if (sectionComp instanceof JLabel) {
                                        JLabel label = (JLabel) sectionComp;
                                        if (label.getFont().isBold() && !label.getText().isEmpty()) {
                                            documentType = label.getText();
                                            break;
                                        }
                                    }
                                }
                                
                                // If we found a document type, reset its status
                                if (documentType != null) {
                                    resetDocumentStatus(documentType);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private JButton createButton(String text, Color bgColor) {
        // Use BUTTON_COLOR for all buttons instead of the passed bgColor
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR); // Light blue from your constants
        button.setBorder(new RoundedBorder(BUTTON_COLOR, 20));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setForeground(Color.WHITE); // Ensure text is white for contrast
        return button;
    }

    /**
     * For buttons created directly (not through createButton method),
     * add this method to update all buttons in the application
     */
    private void updateButtonColors() {
        // Define the button color (same as your BUTTON_COLOR constant)
        final Color buttonColor = new Color(157, 195, 230); // Light blue
        
        // Recursively update all buttons in the application
        updateComponentColors(this, buttonColor);
    }
   
    // Applicant class to hold data
    public static class Applicant {
        private String id;
        private String name;
        private String dateOfApplication;
        private String certificate;
        private String grade;
        private String email;
        private String phone;
        private String address;
        private String dateOfBirth;
        private Map<String, String> documents; // To store document file paths
        private List<String> comments; // To store comments with timestamps
        
        public Applicant(String id, String name, String dateOfApplication, String certificate, String grade) {
            this.id = (id != null) ? id : "";
            this.name = name;
            this.dateOfApplication = dateOfApplication;
            this.certificate = certificate;
            this.grade = grade;
            this.documents = new HashMap<>();
            this.comments = new ArrayList<>();
        }
        public Applicant(String name, String dateOfApplication, String certificate, String grade) {
            this("", name, dateOfApplication, certificate, grade);  // Pass empty string instead of null
        }
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDateOfApplication() {
            return dateOfApplication;
        }
        
        public String getCertificate() {
            return certificate;
        }
        
        public String getGrade() {
            return grade;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
        
        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
        
        // Comment management methods
        public void addComment(String comment) {
            comments.add(comment);
        }
        
        public List<String> getComments() {
            return new ArrayList<>(comments);
        }
        public void addDocument(String documentType, String filePath) {
            documents.put(documentType, filePath);
        }

        // Get document file path
        public String getDocument(String documentType) {
            return documents.get(documentType);
        }

        // Check if applicant has a document
        public boolean hasDocument(String documentType) {
            return documents.containsKey(documentType) && documents.get(documentType) != null;
        }

        // Remove document
        public void removeDocument(String documentType) {
            documents.remove(documentType);
        }

        // Clear all documents
        public void clearDocuments() {
            documents.clear();
        }

        // Get all documents
        public Map<String, String> getAllDocuments() {
            return new HashMap<>(documents);
        }
        public void setId(String id) {
            this.id = id;
        }
        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
        
    }
}