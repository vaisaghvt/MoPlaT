/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.Agent;
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
class IndividualAgentAdderLevel extends CreatorLevel implements MouseListener, MouseMotionListener {

    private DrawingPanel interactionArea;
    private ArrayList<Position> points;
    private ArrayList<Agent> agents;
    private Position point;
    private Position prevPoint;
    private Point currentPoint;

    public IndividualAgentAdderLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
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

        if (model.getAgents().isEmpty()) {
            agents = new ArrayList<Agent>();
        } else {
            agents = (ArrayList<Agent>) model.getAgents();
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
        model.setAgents(agents);
        interactionArea.removeMouseListener(this);
        interactionArea.removeMouseMotionListener(this);

        interactionArea.setEnabled(false);
        frame.remove(interactionArea);
    }

    @Override
    public void draw(Graphics g) {
        super.drawGridLines(g);

        super.drawCurrentPoint(g, currentPoint);

        if (!model.getObstacles().isEmpty()) {
            super.drawObstacles(g, model.getObstacles());
        }

        if (!model.getAgentGroups().isEmpty()) {
            super.drawAgentGroups(g, (ArrayList) model.getAgentGroups());
        }

        if (!model.getAgentLines().isEmpty()) {
            super.drawAgentLines(g, model.getAgentLines());
        }

        if (!agents.isEmpty()) {
            super.drawAgents(g, agents, null);
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
        if (points.size() < 1) {
            for (Agent agent : this.agents) {
                if ((Math.abs(agent.getPosition().getX() - point.getX()) < (CreatorMain.AGENT_RADIUS * 2.0))
                        && (Math.abs(agent.getPosition().getY() - point.getY()) < (CreatorMain.AGENT_RADIUS * 2.0))) {
                    statusBar.setText("Agent can't be created here");
                    return;
                }
            }



            statusBar.setText("Agent created at " + point.getX() + "," + point.getY() + ". Please select the goal.");
            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());
            points.add(tempStorage);



        } else if (points.size() == 1) {
            statusBar.setText("Agent goal set at " + point.getX() + "," + point.getY());
            Agent tempAgent = new Agent();
            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());
            tempAgent.setGoal(tempStorage);
            tempAgent.setPosition(points.get(0));


            if (agents.size() > 0) {
                tempAgent.setId(agents.get(agents.size() - 1).getId() + 1);


            } else {
                tempAgent.setId(1);


            }
            agents.add(tempAgent);

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
        super.calculateCurrentPoint(me, currentPoint, true);
        interactionArea.repaint();
    }

    @Override
    public void clearAllPoints() {
        this.points.clear();
        this.agents.clear();
    }

    @Override
    public String getName() {
        return "Agent Adder Level";
    }
}
