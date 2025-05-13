package gp;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A simplified version that focuses on ensuring the buttons work properly
 */
public class BrunelLoginButtonFix {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Button Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Google button
            RoundedButton googleButton = new RoundedButton("Google");
            googleButton.setBackground(Color.WHITE);
            googleButton.setURL("https://accounts.google.com/signin");
            
            // Facebook button
            RoundedButton facebookButton = new RoundedButton("Facebook");
            facebookButton.setBackground(new Color(66, 103, 178));
            facebookButton.setForeground(Color.WHITE);
            facebookButton.setURL("https://www.facebook.com/login");
            
            // Apple button
            RoundedButton appleButton = new RoundedButton("Apple ID");
            appleButton.setBackground(Color.BLACK);
            appleButton.setForeground(Color.WHITE);
            appleButton.setURL("https://appleid.apple.com/sign-in");
            
            panel.add(Box.createVerticalStrut(20));
            panel.add(googleButton);
            panel.add(Box.createVerticalStrut(20));
            panel.add(facebookButton);
            panel.add(Box.createVerticalStrut(20));
            panel.add(appleButton);
            
            frame.add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    
    /**
     * A custom button class that implements rounded corners and URL navigation
     */
    static class RoundedButton extends JButton {
        private String url;
        private int arcWidth = 20;
        private int arcHeight = 20;
        
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            
            // Add hand cursor on hover
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            });
        }
        
        public void setURL(String url) {
            this.url = url;
            
            // Remove any existing action listeners
            for (ActionListener al : getActionListeners()) {
                removeActionListener(al);
            }
            
            // Add new action listener
            addActionListener(e -> openURL(url));
        }
        
        private void openURL(String url) {
            try {
                System.out.println("Opening URL: " + url);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Desktop browsing not supported on this platform.", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                        "Could not open browser: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Paint the background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
            
            // Call the UI delegate's paint method
            super.paintComponent(g2);
            
            g2.dispose();
        }
        
        @Override
        protected void paintBorder(Graphics g) {
            // Paint nothing to avoid default border
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(200, 40); // Fixed size for consistency
        }
        
        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }
}