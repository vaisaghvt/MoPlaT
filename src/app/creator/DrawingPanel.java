/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * Paints each level.
 *
 * @author vaisagh
 */
class DrawingPanel extends JPanel {
 
    AbstractLevel currentLevel;
    
    public void setCurrentLevel(AbstractLevel currentLevel){
        this.currentLevel = currentLevel;
    }
    
    @Override
    public void paint(Graphics g){
        super.paintComponent(g);
        currentLevel.draw(g);
    }
    
}
