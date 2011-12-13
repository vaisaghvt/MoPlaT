/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import agent.latticegas.LatticeSpace;
import environment.geography.Agent;
import environment.geography.AgentGroup;
import environment.geography.AgentLine;
import environment.geography.Goals;
import environment.geography.Obstacle;
import environment.geography.Position;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author vaisagh
 */
public abstract class CreatorLevel {

    protected ModelDetails model;
    protected JFrame frame;
    protected JLabel statusBar;
    protected JButton previousButton;
    protected JButton clearButton;
    protected JButton nextButton;

    public CreatorLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea) {
        this.model = model;
        this.frame = frame;
        this.statusBar = statusBar;
        this.previousButton = (JButton) buttonArea.getComponent(0);
        this.clearButton = (JButton) buttonArea.getComponent(1);
        this.nextButton = (JButton) buttonArea.getComponent(2);

    }

    public abstract void setUpLevel();

    public abstract void clearUp();

    public abstract void draw(Graphics g);

    public void drawGridLines(Graphics g) {
        int xSize = model.getxSize();
        int ySize = model.getySize();
        int scale = model.getScale();
        if (!model.getLatticeSpaceFlag()) {
            for (int i = 0; i <= xSize; i++) {
                g.setColor(Color.black);
                g.drawLine((int) (i * scale), 0, (int) (i * scale), (int) (ySize * scale));
            }

            for (int i = 0; i <= ySize; i++) {
                g.setColor(Color.black);
                g.drawLine(0, (int) (i * scale), (int) (xSize * scale), (int) (i * scale));
            }
        } else {


            for (double i = 0.0; i <= xSize; i += 2.0 * CreatorMain.AGENT_RADIUS) {
                g.setColor(Color.black);
                g.drawLine((int) (i * scale), 0, (int) (i * scale), (int) (ySize * scale));
            }

            for (double i = 0.0; i <= ySize; i += 2.0 * CreatorMain.AGENT_RADIUS) {
                g.setColor(Color.black);
                g.drawLine(0, (int) (i * scale), (int) (xSize * scale), (int) (i * scale));
            }
        }
    }

    void drawCurrentPoint(Graphics g, Point currentPoint) {
        int xSize = model.getxSize();
        int ySize = model.getySize();
        int scale = model.getScale();
        g.setColor(Color.BLUE);
        g.drawLine(0, (int) (currentPoint.getY()), (int) (xSize * scale), (int) (currentPoint.getY()));
        g.drawLine((int) (currentPoint.getX()), 0, (int) (currentPoint.getX()), (int) (ySize * scale));

    }

    void drawPoints(Graphics g, List<Position> points) {
        g.setColor(Color.blue);
        int scale = model.getScale();
        for (int i = 0; i < points.size(); i++) {

            double x = points.get(i).getX();
            double y = points.get(i).getY();
            g.fillOval((int) (x * scale - 3), (int) (y * scale - 3), 6, 6);
        }
    }

    void drawObstacles(Graphics g, List<Obstacle> obstacles) {
        int scale = model.getScale();
        for (int i = 0; i < obstacles.size(); i++) {

            List<Position> vertices = obstacles.get(i).getVertices();
            int xPoints[] = new int[vertices.size()];
            int yPoints[] = new int[vertices.size()];
            for (int j = 0; j < vertices.size(); j++) {
                double x = vertices.get(j).getX() * scale;
                double y = vertices.get(j).getY() * scale;
                xPoints[j] = (int) x;
                yPoints[j] = (int) y;
            }
            g.setColor(Color.red);
            g.fillPolygon(xPoints, yPoints, vertices.size());

        }
    }

    void calculateCurrentPoint(MouseEvent e, Point currentPoint, boolean halfway) {
      
        double LATTICEGRIDSIZE = CreatorMain.AGENT_RADIUS * 2.0;
        int xSize = model.getxSize();
        int ySize = model.getySize();
        int scale = model.getScale();
        if (e.getX() > xSize * scale || e.getY() > ySize * scale
                || e.getX() < 0 || e.getY() < 0) {
            return;
        }

        currentPoint.setLocation(e.getPoint().x, e.getPoint().y);


        if (model.getLatticeSpaceFlag()) {

            currentPoint.setLocation(
                    Math.round(e.getPoint().getX() / (LATTICEGRIDSIZE * scale)),
                    Math.round(e.getPoint().getY() / (LATTICEGRIDSIZE * scale)));

            currentPoint.setLocation(
                        currentPoint.getX() * LATTICEGRIDSIZE * scale,
                        currentPoint.getY() * LATTICEGRIDSIZE * scale);

            
            if (halfway) {
                

                int directionx = +1;
                int directiony = +1;
                if (e.getPoint().getX() > currentPoint.getX()) {
                    directionx = +1;
                } else {
                    directionx = -1;
                }
                if (e.getPoint().getY() > currentPoint.getY()) {
                    directiony = +1;
                } else {
                    directiony = -1;
                }
                currentPoint.setLocation(
                        currentPoint.getX() + directionx * 0.5 * LATTICEGRIDSIZE * scale,
                        currentPoint.getY() + directiony * 0.5 * LATTICEGRIDSIZE * scale);
            }
        }
        NumberFormat decimalFormat = new DecimalFormat("#0.00");
//        statusBar.setText("Mouse at "
//                + decimalFormat.format(currentPoint.getX() / (LatticeSpace.LATTICEGRIDSIZE * scale))
//                + ","
//                + decimalFormat.format(currentPoint.getY() / (LatticeSpace.LATTICEGRIDSIZE * scale))
//                );

    }

    public void drawAgentLines(Graphics g, List<AgentLine> agentLines) {
        int scale = model.getScale();
        for (int i = 0; i < agentLines.size(); i++) {

            double startX = agentLines.get(i).getStartPoint().getX() * scale;
            double startY = agentLines.get(i).getStartPoint().getY() * scale;
            double goalX = agentLines.get(i).getEndPoint().getX() * scale;
            double goalY = agentLines.get(i).getEndPoint().getY() * scale;


            g.setColor(Color.blue);

            g.drawLine((int) startX, (int) startY, (int) goalX, (int) goalY);

        }
    }

    public boolean mouseReleaseDefaultActions(MouseEvent e, Point currentPoint, Position point, Position prevPoint) {
        int xSize = model.getxSize();
        int ySize = model.getySize();
        int scale = model.getScale();
        if (currentPoint.getX() > xSize * scale || currentPoint.getY() > ySize * scale
                || currentPoint.getX() < 0 || currentPoint.getY() < 0) {
            return false;
        }
        if (point.getX() >= 0) {
            prevPoint.setX(point.getX());
            prevPoint.setY(point.getY());
        }
//         else {
//            point = new Position();
//        }
        //        if (!islatticeSpaceFlag()) {
//            point.setX((double) e.getX() / scale);
//            point.setY((double) e.getY() / scale);
//        } else {
//
//
//            point.setX(new Double((int) (e.getX() / scale)));
//            point.setY(new Double((int) (e.getY() / scale)));
//        }
        point.setX(currentPoint.getX() / scale);
        point.setY(currentPoint.getY() / scale);


        if (prevPoint.getX() >= 0) {
            if (e.isControlDown()) {

                if (Math.abs(point.getX() - prevPoint.getX()) < 0.3) {

                    point.setX(prevPoint.getX());


                } else if (Math.abs(point.getY() - prevPoint.getY()) < 0.3) {

                    point.setY(prevPoint.getY());


                } else {
                    point.setX(prevPoint.getX());
                    point.setY(prevPoint.getY());


                    return false;


                }
            }
        }

//        if (prevPoint == null) {
//            prevPoint = new Position();
//        }
        return true;
    }

    void drawAgents(Graphics g, List<Agent> agents, List<Boolean> highlightedAgents) {
        assert highlightedAgents.size() == agents.size();
        int scale = model.getScale();
        for (int i = 0; i < agents.size(); i++) {

            double startX = agents.get(i).getPosition().getX() * scale;
            double startY = agents.get(i).getPosition().getY() * scale;
            double goalX = agents.get(i).getGoal().getX() * scale;
            double goalY = agents.get(i).getGoal().getY() * scale;

            if (highlightedAgents != null && !highlightedAgents.get(i)) {
                g.setColor(Color.gray);
            } else {
                g.setColor(Color.black);
            }
            g.fillOval((int) (startX - (CreatorMain.AGENT_RADIUS * scale)),
                    (int) (startY - (CreatorMain.AGENT_RADIUS * scale)),
                    (int) (CreatorMain.AGENT_RADIUS * 2.0 * scale),
                    (int) (CreatorMain.AGENT_RADIUS * 2.0 * scale));


            g.setColor(Color.CYAN);

            g.drawLine((int) startX, (int) startY, (int) goalX, (int) goalY);

        }
    }

    public abstract void clearAllPoints();

    void drawAgentGroups(Graphics g, ArrayList<AgentGroup> agentGroups) {
        int scale = model.getScale();
        for (AgentGroup tempGroup : agentGroups) {


            double startX = tempGroup.getStartPoint().getX() * scale;
            double startY = tempGroup.getStartPoint().getY() * scale;
            double goalX = tempGroup.getEndPoint().getX() * scale;
            double goalY = tempGroup.getEndPoint().getY() * scale;


            g.setColor(Color.ORANGE);

            g.drawRect((int) startX, (int) startY, (int) (goalX - startX), (int) (goalY - startY));

        }
    }

    void drawGoalLines(Graphics g, ArrayList<Goals> goalLines) {
        int scale = model.getScale();
        for (int i = 0; i < goalLines.size(); i++) {

            double startX = goalLines.get(i).getStartPoint().getX() * scale;
            double startY = goalLines.get(i).getStartPoint().getY() * scale;
            double goalX = goalLines.get(i).getEndPoint().getX() * scale;
            double goalY = goalLines.get(i).getEndPoint().getY() * scale;


            g.setColor(Color.YELLOW);

            g.drawLine((int) startX, (int) startY, (int) goalX, (int) goalY);

        }
    }

    public abstract String getName();

    void drawRoadMap(Graphics g, ArrayList<Position> roadMapPoints) {
         int scale = model.getScale();
        for (int i = 0; i < roadMapPoints.size()-1; i++) {
             double startX = roadMapPoints.get(i).getX() * scale;
            double startY = roadMapPoints.get(i).getY() * scale;
            double goalX = roadMapPoints.get(i+1).getX() * scale;
            double goalY = roadMapPoints.get(i+1).getY() * scale;
            
            g.setColor(Color.YELLOW);
            g.drawLine((int) startX, (int) startY, (int) goalX, (int) goalY);
        }
    }
}
