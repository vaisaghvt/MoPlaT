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
class RoadMapLevel extends AbstractLevel implements MouseListener, MouseMotionListener {

    private DrawingPanel interactionArea;
    private ArrayList<Position> points;
    private ArrayList<Position> roadMapPoints;
    private Position point;
    private Position prevPoint;
    private Point currentPoint;
   
    public RoadMapLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
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
  

        if (model.getRoadMap() == null || model.getRoadMap().isEmpty()) {
            roadMapPoints = new ArrayList<Position>();
        } else {
            System.out.println("blah blag");
            roadMapPoints = (ArrayList<Position>) model.getRoadMap();
        }


        previousButton.setEnabled(true);
        clearButton.setEnabled(true);
        nextButton.setEnabled(true);
        
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
        model.setRoadMap(this.roadMapPoints);
        interactionArea.removeMouseListener(this);
        interactionArea.removeMouseMotionListener(this);
        frame.remove(interactionArea);
    }
    
    
    @Override
    public void clearAllPoints() {
        this.points.clear();
        this.roadMapPoints.clear();
    }

    @Override
    public void draw(Graphics g) {
        super.drawGridLines(g);
        
        super.drawCurrentPoint(g,currentPoint);
        


        if (!points.isEmpty()) {
            super.drawPoints(g,points);
            
        }

       if (!model.getObstacles().isEmpty()) {
            super.drawObstacles(g, model.getObstacles());
        }

        if (!model.getAgentGroups().isEmpty()) {
            super.drawAgentGroups(g, (ArrayList) model.getAgentGroups());
        }

        if (!model.getAgentLines().isEmpty()) {
            super.drawAgentLines(g, model.getAgentLines());
        }

        if (!model.getAgents().isEmpty()) {
            super.drawAgents(g, model.getAgents(), null);
        }

        if(!this.roadMapPoints.isEmpty()){
            super.drawRoadMap(g, this.roadMapPoints);
        }
    }

 

    @Override
    public void mouseReleased(MouseEvent e) {
        boolean validityCheck = super.mouseReleaseDefaultActions(e, currentPoint, point, prevPoint);

        if(!validityCheck&&!roadMapPoints.isEmpty()){
            return;
        }


        if (prevPoint == null) {
            prevPoint = new Position();
        }

        if (e.getClickCount() < 2) {


            statusBar.setText("Clicked at " + point.getX() + "," + point.getY());


            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());


            points.add(tempStorage);



        } else if (e.getClickCount() == 2) {
            statusBar.setText("Double click: Roadmap created");
            


            for (Position tempPoint: points) {
                Position tempStorage = new Position();
                tempStorage.setX((double) tempPoint.getX());
                tempStorage.setY((double) tempPoint.getY());

                this.roadMapPoints.add(tempStorage);



            }
            
            points.clear();


        }
        interactionArea.repaint();
        frame.validate();

    }

    @Override
    public void mouseMoved(MouseEvent e) {
 
        super.calculateCurrentPoint(e,currentPoint,true);
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

    @Override
    public String getName() {
        return "RoadMap Level";
    }


}
