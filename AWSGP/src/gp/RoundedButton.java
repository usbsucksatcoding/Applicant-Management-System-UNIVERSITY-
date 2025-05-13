package gp;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * A custom JButton that has rounded corners and can open URLs
 */
public class RoundedButton extends JButton {
    private Color backgroundColor;
    private Color foregroundColor;
    private String url;
    private boolean isHovered = false;
    private final int cornerRadius = 20;
    
    public RoundedButton(String text) {
        super(text);
        setup();
    }
    
    public RoundedButton(String text, Color backgroundColor, Color foregroundColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        setup();
    }
    
    public RoundedButton(String text, Icon icon, Color backgroundColor, Color foregroundColor) {
        super(text);
        setIcon(icon);
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        setup();
    }
    
    private void setup() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        
        if (backgroundColor != null) {
            setBackground(backgroundColor);
        }
        
        if (foregroundColor != null) {
            setForeground(foregroundColor);
        }
        
        // Add mouse listener for hover effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }
    
    public void setURL(String url) {
        this.url = url;
        
        // Remove any existing action listeners
        for (ActionListener al : getActionListeners()) {
            removeActionListener(al);
        }
        
        // Add new action listener for URL opening
        addActionListener(e -> {
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
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                        "Could not open browser: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Paint the background with rounded corners
        Color bgColor = isHovered ? adjustColor(getBackground(), -20) : getBackground();
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        
        // Set rendering hints for text
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Delegate to UI for text and icon painting
        super.paintComponent(g2);
        
        g2.dispose();
    }
    
    private Color adjustColor(Color color, int amount) {
        int red = Math.max(0, Math.min(255, color.getRed() + amount));
        int green = Math.max(0, Math.min(255, color.getGreen() + amount));
        int blue = Math.max(0, Math.min(255, color.getBlue() + amount));
        return new Color(red, green, blue);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // No border painting
    }
    
    @Override
    public boolean contains(int x, int y) {
        // Only detect clicks within the rounded rectangle
        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        return shape.contains(x, y);
    }
}