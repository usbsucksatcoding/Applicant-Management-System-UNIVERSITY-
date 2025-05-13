package gp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * Utility class to create more realistic social media icons
 */
public class RealisticIconCreator {
    
    /**
     * Creates a realistic Apple logo icon
     * @param width Width of the icon
     * @param height Height of the icon
     * @return ImageIcon containing the Apple logo
     */
    public static ImageIcon createAppleIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set up high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Using a simpler approach for the Apple logo
        double padding = width * 0.05; // 5% padding
        double w = width - 2 * padding;
        double h = height - 2 * padding;
        double x = padding;
        double y = padding;
        
        // Apple body shape
        GeneralPath applePath = new GeneralPath();
        
        // Starting point at the top of the leaf
        applePath.moveTo(x + w * 0.5, y + h * 0.18);
        
        // Right side of apple
        applePath.curveTo(
            x + w * 0.58, y + h * 0.18, // control point 1
            x + w * 0.9, y + h * 0.24,  // control point 2
            x + w * 0.9, y + h * 0.5    // end point
        );
        
        // Bottom right of apple
        applePath.curveTo(
            x + w * 0.9, y + h * 0.78,  // control point 1
            x + w * 0.78, y + h * 0.95, // control point 2
            x + w * 0.5, y + h * 0.95   // end point
        );
        
        // Bottom left of apple
        applePath.curveTo(
            x + w * 0.22, y + h * 0.95, // control point 1
            x + w * 0.1, y + h * 0.78,  // control point 2
            x + w * 0.1, y + h * 0.5    // end point
        );
        
        // Left side of apple
        applePath.curveTo(
            x + w * 0.1, y + h * 0.24,  // control point 1
            x + w * 0.42, y + h * 0.18, // control point 2
            x + w * 0.5, y + h * 0.18   // end point
        );
        
        // Bite from the right side
        GeneralPath bitePath = new GeneralPath();
        bitePath.moveTo(x + w * 0.75, y + h * 0.3);
        bitePath.curveTo(
            x + w * 0.58, y + h * 0.3,  // control point 1
            x + w * 0.58, y + h * 0.5,  // control point 2
            x + w * 0.75, y + h * 0.5   // end point
        );
        bitePath.curveTo(
            x + w * 0.92, y + h * 0.5,  // control point 1
            x + w * 0.92, y + h * 0.3,  // control point 2
            x + w * 0.75, y + h * 0.3   // end point
        );
        
        // Leaf on top
        GeneralPath leafPath = new GeneralPath();
        leafPath.moveTo(x + w * 0.5, y + h * 0.18);
        leafPath.curveTo(
            x + w * 0.55, y + h * 0.1,  // control point 1
            x + w * 0.65, y + h * 0.05, // control point 2
            x + w * 0.7, y + h * 0.1    // end point
        );
        leafPath.curveTo(
            x + w * 0.65, y + h * 0.15, // control point 1
            x + w * 0.55, y + h * 0.15, // control point 2
            x + w * 0.5, y + h * 0.18   // end point
        );
        
        // Create areas
        Area appleArea = new Area(applePath);
        Area biteArea = new Area(bitePath);
        
        // Subtract bite from apple
        appleArea.subtract(biteArea);
        
        // Draw all parts
        g2d.setColor(Color.WHITE);
        g2d.fill(appleArea);
        g2d.fill(leafPath);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * Creates a realistic Google logo icon with the 'G' and four colors
     * @param width Width of the icon
     * @param height Height of the icon
     * @return ImageIcon containing the Google logo
     */
    public static ImageIcon createGoogleIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set up high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Dimensions
        int padding = width / 10;
        int size = width - 2 * padding;
        int centerX = width / 2;
        int centerY = height / 2;
        int outerRadius = size / 2;
        int innerRadius = outerRadius - outerRadius / 4;
        
        // Draw outer circle segments with Google colors
        // Red segment (top-right)
        g2d.setColor(new Color(234, 67, 53)); // Google Red
        Arc2D.Double redArc = new Arc2D.Double(
            padding, padding, size, size, 
            -45, 90, Arc2D.PIE
        );
        g2d.fill(redArc);
        
        // Yellow segment (bottom-right)
        g2d.setColor(new Color(251, 188, 5)); // Google Yellow
        Arc2D.Double yellowArc = new Arc2D.Double(
            padding, padding, size, size, 
            45, 90, Arc2D.PIE
        );
        g2d.fill(yellowArc);
        
        // Green segment (bottom-left)
        g2d.setColor(new Color(52, 168, 83)); // Google Green
        Arc2D.Double greenArc = new Arc2D.Double(
            padding, padding, size, size, 
            135, 90, Arc2D.PIE
        );
        g2d.fill(greenArc);
        
        // Blue segment (top-left)
        g2d.setColor(new Color(66, 133, 244)); // Google Blue
        Arc2D.Double blueArc = new Arc2D.Double(
            padding, padding, size, size, 
            225, 90, Arc2D.PIE
        );
        g2d.fill(blueArc);
        
        // Draw inner white circle for the hole in the 'G'
        g2d.setColor(Color.WHITE);
        g2d.fillOval(
            centerX - innerRadius, 
            centerY - innerRadius, 
            innerRadius * 2, 
            innerRadius * 2
        );
        
        // Draw blue rectangle for the right side of 'G'
        g2d.setColor(new Color(66, 133, 244)); // Google Blue
        g2d.fillRect(
            centerX, 
            centerY - innerRadius / 2, 
            outerRadius, 
            innerRadius
        );
        
        g2d.dispose();
        return new ImageIcon(image);
    }
}