/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.Obstacle;
import environment.geography.Position;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author vaisagh
 */
class ObstacleLevel extends CreatorLevel implements MouseListener, MouseMotionListener {

    private DrawingPanel interactionArea;
    private ArrayList<Position> points;
    private ArrayList<Obstacle> obstacles;
    private Position point;
    private Position prevPoint;
    private Point currentPoint;
   
    public ObstacleLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
        super(model, frame, statusBar, buttonArea);
        this.interactionArea = interactionArea;
        
    }

    @Override
    public void setUpLevel() {

        points = new ArrayList<Position>();
        prevPoint = new Position();
        point = new Position();
        point.setX(0.0);
        point.setY(0.0);
        prevPoint.setX(-1.0);
        prevPoint.setY(-1.0);
        currentPoint = new Point(0, 0);
  

        if (model.getObstacles() == null || model.getObstacles().isEmpty()) {
            obstacles = new ArrayList<Obstacle>();
        } else {
            obstacles = (ArrayList<Obstacle>) model.getObstacles();
        }


        previousButton.setEnabled(true);
        clearButton.setEnabled(true);
        nextButton.setEnabled(true);
        
        frame.setTitle("- Create Obstacles -"+model.getTitle() + ".xml");
        frame.setSize(model.getxSize() * model.getScale() + 8, model.getySize() * model.getScale() + 100);
        frame.repaint();


        frame.add(interactionArea, BorderLayout.CENTER);
        interactionArea.setBackground(Color.white);
        interactionArea.setCurrentLevel(this);
        interactionArea.setEnabled(true);
        interactionArea.repaint();
        interactionArea.addMouseListener(this);
        interactionArea.addMouseMotionListener(this);

    }

    @Override
    public void clearUp() {
        model.setObstacles(this.obstacles);
        interactionArea.removeMouseListener(this);
        interactionArea.removeMouseMotionListener(this);
        frame.remove(interactionArea);
    }
    
    
    @Override
    public void clearAllPoints() {
        this.points.clear();
        this.obstacles.clear();
    }

    @Override
    public void draw(Graphics g) {
        super.drawGridLines(g);
        
        super.drawCurrentPoint(g,currentPoint);
        


        if (!points.isEmpty()) {
            super.drawPoints(g,points);
            
        }

        if (!obstacles.isEmpty()) {
            super.drawObstacles(g,obstacles);
        }
    }

 

    @Override
    public void mouseReleased(MouseEvent e) {
        boolean validityCheck = super.mouseReleaseDefaultActions(e, currentPoint, point, prevPoint);

        if(!validityCheck){
            return;
        }


        if (prevPoint == null) {
            prevPoint = new Position();
        }

        if (e.getClickCount() < 2 || points.size() <= 2) {


            statusBar.setText("Clicked at " + point.getX() + "," + point.getY());


            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());


            points.add(tempStorage);



        } else if (e.getClickCount() == 2 && points.size() > 2) {
            statusBar.setText("Double click: Polygon created");
            Obstacle tempObstacle = new Obstacle();


            for (int i = 0; i
                    < points.size(); i++) {
                Position tempStorage = new Position();
                tempStorage.setX((double) points.get(i).getX());
                tempStorage.setY((double) points.get(i).getY());

                tempObstacle.getVertices().add(tempStorage);



            }
            obstacles.add(tempObstacle);
            points.clear();


        }
        interactionArea.repaint();
        frame.validate();

    }

    @Override
    public void mouseMoved(MouseEvent e) {
 
        super.calculateCurrentPoint(e,currentPoint,false);
         interactionArea.repaint();
    }

    
    @Override
    public void mouseClicked(MouseEvent e) {
       
    }

    @Override
    public void mousePressed(MouseEvent e) {
       
    }
    
    
    @Override
    public void mouseEntered(MouseEvent e) {
       
    }

    @Override
    public void mouseExited(MouseEvent e) {
       
    }

    @Override
    public void mouseDragged(MouseEvent e) {
       
    }


}
