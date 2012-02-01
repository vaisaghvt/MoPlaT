/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.Goals;
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
public class EnvironmentGoalLineLevel extends CreatorLevel implements MouseListener, MouseMotionListener{
    private DrawingPanel interactionArea;
    private ArrayList<Position> points;
    private ArrayList<Goals> goalLines;
    private Position point;
    private Position prevPoint;
    private Point currentPoint;

    public EnvironmentGoalLineLevel(ModelDetails model, JFrame frame, 
            JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
        super(model, frame, statusBar, buttonArea);
        this.interactionArea = interactionArea;

    }

    @Override
    public void setUpLevel() {
        points = new ArrayList<Position>();
        prevPoint = new Position();
        point = new Position();
        point.setX(-1.0);
        point.setY(-1.0);
        prevPoint.setX(-1.0);
        prevPoint.setY(-1.0);
        currentPoint = new Point(0, 0);

        if (model.getGoalLines().isEmpty()) {
            goalLines = new ArrayList<Goals>();
        } else {
            goalLines = (ArrayList<Goals>) model.getGoalLines();
        }


        previousButton.setEnabled(true);
        clearButton.setEnabled(true);
        nextButton.setEnabled(true);

      


        frame.add(interactionArea, BorderLayout.CENTER);
        interactionArea.setBackground(Color.white);
        interactionArea.setCurrentLevel(this);
        interactionArea.setEnabled(true);
        interactionArea.repaint();
        interactionArea.addMouseListener(this);
        interactionArea.addMouseMotionListener(this);
        
        frame.repaint();
    }

    @Override
    public void clearUp() {
        model.setGoalLines(goalLines);
        interactionArea.removeMouseListener(this);
        interactionArea.removeMouseMotionListener(this);

        interactionArea.setEnabled(false);
        frame.remove(interactionArea);
    }

    @Override
    public void draw(Graphics g) {
        super.drawGridLines(g);

        super.drawCurrentPoint(g, currentPoint);

        if (!points.isEmpty()) {
            super.drawPoints(g, points);
        }

        if (!model.getObstacles().isEmpty()) {
            super.drawObstacles(g, model.getObstacles());
        }
        
        if(!model.getAgentGroups().isEmpty()){
            super.drawAgentGroups(g, (ArrayList) model.getAgentGroups());
        }

        if(!model.getAgentLines().isEmpty()){
            super.drawAgentLines(g, model.getAgentLines());
        }
        
        if(!model.getAgents().isEmpty()){
            super.drawAgents(g,model.getAgents(), null);
        }
        
        if (!goalLines.isEmpty()) {
            super.drawGoalLines(g, goalLines);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        boolean validityCheck = super.mouseReleaseDefaultActions(me, currentPoint, point, prevPoint);
        if (!validityCheck) {
            return;
        }

        if (prevPoint.getX() >= 0) {
            if (Math.abs(point.getX() - prevPoint.getX()) < 0.3) {
                point.setX(prevPoint.getX());
            } else if (Math.abs(point.getY() - prevPoint.getY()) < 0.3) {
                point.setY(prevPoint.getY());
            } else {
                point.setX(prevPoint.getX());
                point.setY(prevPoint.getY());
                return;
            }
        }

        if (points.size() < 1) {
            statusBar.setText("Goal line started at " + point.getX() + "," +
                    point.getY() + ". Please select the end point.");
            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());
            points.add(tempStorage);



        } else if (points.size() == 1) {
            statusBar.setText("Goal line end set at " + point.getX() + 
                    "," + point.getY());
            Goals tempGoalLines = new Goals();

            Position tempStorage = new Position();
            tempStorage.setX(point.getX() > points.get(0).getX() ? point.getX() 
                    : points.get(0).getX());
            tempStorage.setY(point.getY() > points.get(0).getY() ? point.getY() 
                    : points.get(0).getY());
            tempGoalLines.setEndPoint(tempStorage);

            tempStorage = new Position();
            tempStorage.setX(point.getX() < points.get(0).getX() ? point.getX() 
                    : points.get(0).getX());
            tempStorage.setY(point.getY() < points.get(0).getY() ? point.getY() 
                    : points.get(0).getY());
            tempGoalLines.setStartPoint(tempStorage);

            goalLines.add(tempGoalLines);

            prevPoint.setX(-1.0);
            prevPoint.setY(-1.0);
            point.setX(-1.0);
            point.setY(-1.0);
            points.clear();


        }

        interactionArea.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        super.calculateCurrentPoint(me, currentPoint, false);
        interactionArea.repaint();
    }

    @Override
    public void clearAllPoints() {
        points.clear();
        goalLines.clear();
        interactionArea.repaint();
    }

    @Override
    public String getName() {
        return "Goal Line Adder Level";
    }
}
