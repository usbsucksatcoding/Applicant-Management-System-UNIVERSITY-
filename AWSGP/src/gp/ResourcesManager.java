package gp;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * This class helps manage resources for the Brunel University Login System.
 * It provides methods to create simple colored icons instead of loading from external files.
 */
public class ResourcesManager {
    
    /**
     * Create a simple colored rectangle icon to represent Google
     * @param width Desired width
     * @param height Desired height
     * @return Simple Google icon (colored rectangle)
     */
    public static ImageIcon getGoogleIcon(int width, int height) {
        return createColorIcon(width, height, Color.WHITE, "G");
    }
    
    /**
     * Create a simple colored rectangle icon to represent Facebook
     * @param width Desired width
     * @param height Desired height
     * @return Simple Facebook icon (colored rectangle)
     */
    public static ImageIcon getFacebookIcon(int width, int height) {
        return createColorIcon(width, height, new Color(66, 103, 178), "f");
    }
    
    /**
     * Create a simple Apple icon with the apple shape
     * @param width Desired width
     * @param height Desired height
     * @return Apple icon
     */
    public static ImageIcon getAppleIcon(int width, int height) {
        // Create an image with transparent background
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        
        // Set antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw apple shape (simplified)
        g2d.setColor(Color.WHITE);
        
        // Main apple body (circle)
        g2d.fillOval(width/6, height/3, 2*width/3, 2*height/3);
        
        // Top part of apple (indent)
        g2d.fillOval(width/6, height/6, width/3, height/3);
        g2d.fillOval(width/2, height/6, width/3, height/3);
        
        // Leaf
        int[] leafX = {2*width/3, 3*width/4, 2*width/3};
        int[] leafY = {height/4, height/6, height/8};
        g2d.fillPolygon(leafX, leafY, 3);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * Create a simple colored rectangle icon with optional text
     * @param width Width of the icon
     * @param height Height of the icon
     * @param color Background color
     * @param text Optional text to display (can be empty)
     * @return A new ImageIcon
     */
    private static ImageIcon createColorIcon(int width, int height, Color color, String text) {
        Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        
        // Set antialiasing for smoother edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill rounded background
        g2d.setColor(color);
        g2d.fillRoundRect(0, 0, width, height, height/2, height/2);
        
        // Add border
        g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(0, 0, width-1, height-1, height/2, height/2);
        
        // Add text if provided
        if (text != null && !text.isEmpty()) {
            g2d.setColor(color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, height/2));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g2d.drawString(text, (width - textWidth) / 2, height / 2 + textHeight / 4);
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }
}