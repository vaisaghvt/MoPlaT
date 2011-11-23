/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creatorNew;

import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author vaisagh
 */
class DrawingPanel extends JPanel {
 
    CreatorLevel currentLevel;
    
    public void setCurrentLevel(CreatorLevel currentLevel){
        this.currentLevel = currentLevel;
    }
    
    @Override
    public void paint(Graphics g){
        super.paintComponent(g);
        currentLevel.draw(g);
    }
    
}
