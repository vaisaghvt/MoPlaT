/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.AgentLine;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author vaisagh
 */
class AgentGeneratorLevel extends CreatorLevel implements MouseListener, MouseMotionListener {

    private DrawingPanel interactionArea;
    private ArrayList<Position> points;
    private ArrayList<AgentLine> agentLines;
    private Position point;
    private Position prevPoint;
    private Point currentPoint;

    public AgentGeneratorLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
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

        if (model.getAgentLines().isEmpty()) {
            agentLines = new ArrayList<AgentLine>();
        } else {
            agentLines = (ArrayList<AgentLine>) model.getAgentLines();
        }


        previousButton.setEnabled(true);
        clearButton.setEnabled(true);
        nextButton.setEnabled(true);

        frame.setTitle("- Create Agent Generating Lines -"+model.getTitle() + ".xml");
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
        model.setAgentLines(agentLines);
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

        if (!agentLines.isEmpty()) {
            super.drawAgentLines(g, agentLines);
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
            statusBar.setText("Agent line started at " + point.getX() + "," + point.getY() + ". Please select the end point.");
            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());
            points.add(tempStorage);



        } else if (points.size() == 1) {
            statusBar.setText("Agent line end set at " + point.getX() + "," + point.getY());
            AgentLine tempAgentLine = new AgentLine();


            Position tempStorage = new Position();
            tempStorage.setX(point.getX() > points.get(0).getX() ? point.getX() : points.get(0).getX());
            tempStorage.setY(point.getY() > points.get(0).getY() ? point.getY() : points.get(0).getY());
            tempAgentLine.setEndPoint(tempStorage);


            tempStorage = new Position();
            tempStorage.setX(point.getX() < points.get(0).getX() ? point.getX() : points.get(0).getX());
            tempStorage.setY(point.getY() < points.get(0).getY() ? point.getY() : points.get(0).getY());
            tempAgentLine.setStartPoint(tempStorage);




            String creationDirection;
            do {
                Object[] possibilities = {"Up", "Down", "Left", "Right"};
                creationDirection = "Up";
                creationDirection = (String) JOptionPane.showInputDialog(
                        null,
                        "Which direction do you want agents to be generated?",
                        "Input",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        possibilities,
                        "Up");
            } while (creationDirection == null);
            if (creationDirection.equalsIgnoreCase("up")) {
                tempAgentLine.setDirection(3);
            } else if (creationDirection.equalsIgnoreCase("down")) {
                tempAgentLine.setDirection(2);
            } else if (creationDirection.equalsIgnoreCase("right")) {
                tempAgentLine.setDirection(1);
            } else {
                tempAgentLine.setDirection(0);
            }


            String frequency;
            do {
                frequency = (String) JOptionPane.showInputDialog(
                        null,
                        "How many time steps between agents being generate?",
                        "Input",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "1");
                try {
                    Integer.parseInt(frequency);
                } catch (NumberFormatException numException) {
                    continue;
                }
            } while (false);


            String number;
            do {
                number = (String) JOptionPane.showInputDialog(
                        null,
                        "How many agents produced per generation?",
                        "Input",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "1");
                try {
                    Integer.parseInt(number);
                } catch (NumberFormatException numException) {
                    continue;
                }
            } while (false);



            tempAgentLine.setFrequency(Integer.parseInt(frequency));
            tempAgentLine.setNumber(Integer.parseInt(number));
            agentLines.add(tempAgentLine);

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
        super.calculateCurrentPoint(me, currentPoint,true);
        interactionArea.repaint();
    }

    @Override
    public void clearAllPoints() {
        points.clear();
        agentLines.clear();
        interactionArea.repaint();
    }
}
