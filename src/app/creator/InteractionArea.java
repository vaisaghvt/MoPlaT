/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.Agent;
import environment.geography.AgentLine;
import environment.geography.Obstacle;
import environment.geography.Position;
import environment.geography.SimulationScenario;
import agent.latticegas.LatticeSpace;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import java.awt.Robot;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JOptionPane;

/**
 *
 * @author Vaisagh
 */
class InteractionArea extends JPanel implements MouseListener, MouseMotionListener {

    private ArrayList<Position> points = new ArrayList<Position>();
    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private ArrayList<Agent> agents = new ArrayList<Agent>();
    private ArrayList<AgentLine> agentLines = new ArrayList<AgentLine>();
    private ArrayList<Boolean> highlightedAgent = new ArrayList<Boolean>();
    private JLabel statusBar;
    private Position prevPoint;
    private Position point;
    private int currentLevel;
    private double scale;
    private int xSize;
    private int ySize;
    private Point currentPoint = new Point(0, 0);
    private boolean latticeSpaceFlag = false;

    public boolean islatticeSpaceFlag() {
        return latticeSpaceFlag;
    }

    public void setLatticeSpaceFlag(boolean integerPointFlag) {
        this.latticeSpaceFlag = integerPointFlag;
    }

    public InteractionArea(JLabel statusBar) {
        points = new ArrayList<Position>();
        obstacles = new ArrayList<Obstacle>();




        this.statusBar = statusBar;

        this.setVisible(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);


        prevPoint = new Position();
        point = new Position();
        point.setX(0.0);
        point.setY(0.0);

    }

    @Override
    public void paint(Graphics g) {
        //   super.paintComponent(g);


        super.paintComponent(g);

        if (!this.latticeSpaceFlag) {
            for (int i = 0; i <= xSize; i++) {
                g.setColor(Color.black);
                g.drawLine((int) (i * scale), 0, (int) (i * scale), (int) (ySize * scale));
            }

            for (int i = 0; i <= ySize; i++) {
                g.setColor(Color.black);
                g.drawLine(0, (int) (i * scale), (int) (xSize * scale), (int) (i * scale));
            }
        } else {


            for (double i = 0.0; i <= xSize; i += LatticeSpace.LATTICEGRIDSIZE) {
                g.setColor(Color.black);
                g.drawLine((int) (i * scale), 0, (int) (i * scale), (int) (ySize * scale));
            }

            for (double i = 0.0; i <= ySize; i += LatticeSpace.LATTICEGRIDSIZE) {
                g.setColor(Color.black);
                g.drawLine(0, (int) (i * scale), (int) (xSize * scale), (int) (i * scale));
            }
        }
        g.setColor(Color.BLUE);
        g.drawLine(0, (int) (currentPoint.getY()), (int) (xSize * scale), (int) (currentPoint.getY()));
        g.drawLine((int) (currentPoint.getX()), 0, (int) (currentPoint.getX()), (int) (ySize * scale));



        if (!points.isEmpty()) {

            g.setColor(Color.blue);
            for (int i = 0; i < points.size(); i++) {

                double x = points.get(i).getX();
                double y = points.get(i).getY();
                g.fillOval((int) (x * scale - 3), (int) (y * scale - 3), 6, 6);
            }
        }

        if (!obstacles.isEmpty() && currentLevel >= 1) {
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

        if (!agentLines.isEmpty() && currentLevel >= 2) {
            for (int i = 0; i < agentLines.size(); i++) {

                double startX = agentLines.get(i).getStartPoint().getX() * scale;
                double startY = agentLines.get(i).getStartPoint().getY() * scale;
                double goalX = agentLines.get(i).getEndPoint().getX() * scale;
                double goalY = agentLines.get(i).getEndPoint().getY() * scale;


                g.setColor(Color.blue);

                g.drawLine((int) startX, (int) startY, (int) goalX, (int) goalY);

            }
        }


        if (!agents.isEmpty() && currentLevel >= 3) {
            for (int i = 0; i < agents.size(); i++) {

                double startX = agents.get(i).getPosition().getX() * scale;
                double startY = agents.get(i).getPosition().getY() * scale;
                double goalX = agents.get(i).getGoal().getX() * scale;
                double goalY = agents.get(i).getGoal().getY() * scale;

                if (!highlightedAgent.get(i)) {
                    g.setColor(Color.gray);
                } else {
                    g.setColor(Color.black);
                }
                g.fillOval((int) (startX - (0.15 * EnvironmentCreatorApp.scale)), (int) (startY - (0.15 * EnvironmentCreatorApp.scale)), (int) (0.3 * EnvironmentCreatorApp.scale), (int) (0.3 * EnvironmentCreatorApp.scale));


                g.setColor(Color.CYAN);

                g.drawLine((int) startX, (int) startY, (int) goalX, (int) goalY);

            }
        }
    }

    public ArrayList<Position> getPoints() {
        return points;


    }

    public void setStatusBar(String st) {
        statusBar.setText(st);



    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (currentPoint.getX() > xSize * scale || currentPoint.getY() > ySize * scale
                || currentPoint.getX() < 0 || currentPoint.getY() < 0) {
            return;
        }
        if (point != null) {
            prevPoint.setX(point.getX());
            prevPoint.setY(point.getY());
        } else {
            point = new Position();
        }
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

 
        if (prevPoint != null) {
            if (e.isControlDown() || currentLevel == 2) {

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
        }

        if (prevPoint == null) {
            prevPoint = new Position();
        }


        if (currentLevel == 1) {
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
        } else if (currentLevel == 3) {//adding agents
            if (points.size() < 1) {
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
                highlightedAgent.add(false);
                points.clear();


            }

        } else if (currentLevel == 2) {//adding agent line


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
                tempStorage.setX(point.getX());
                tempStorage.setY(point.getY());
                tempAgentLine.setEndPoint(tempStorage);
                tempStorage = new Position();
                tempStorage.setX(points.get(0).getX());
                tempStorage.setY(points.get(0).getY());

                tempAgentLine.setStartPoint(tempStorage);




                String creationDirection;


                do {
                    Object[] possibilities = {"Up", "Down", "Left", "Right"};
                    creationDirection = new String("Up");
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
//                    System.out.println("here");
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

                prevPoint = null;
                point = null;
                points.clear();


            }


        }
        this.repaint();


    }

    @Override
    public void mousePressed(
            MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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
    public void mouseMoved(MouseEvent e) {
        if (e.getX() > xSize * scale || e.getY() > ySize * scale
                || e.getX() < 0 || e.getY() < 0) {
            return;
        }

        currentPoint = e.getPoint();



        if (latticeSpaceFlag) {

            currentPoint.setLocation(
                    Math.round(e.getPoint().getX() / (LatticeSpace.LATTICEGRIDSIZE * scale)),
                    Math.round(e.getPoint().getY() / (LatticeSpace.LATTICEGRIDSIZE * scale)));



        }







        if (latticeSpaceFlag) {

            currentPoint.setLocation(
                    currentPoint.getX() * LatticeSpace.LATTICEGRIDSIZE * scale,
                    currentPoint.getY() * LatticeSpace.LATTICEGRIDSIZE * scale);
            if (currentLevel >= 2) {
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
                        currentPoint.getX() + directionx * 0.5 * LatticeSpace.LATTICEGRIDSIZE * scale,
                        currentPoint.getY() + directiony * 0.5 * LatticeSpace.LATTICEGRIDSIZE * scale);
            }
        }

        NumberFormat nf = new DecimalFormat("#0.00");
        statusBar.setText("Mouse at " + nf.format(currentPoint.getX() / (LatticeSpace.LATTICEGRIDSIZE * scale)) + "," + nf.format(currentPoint.getY() / (LatticeSpace.LATTICEGRIDSIZE * scale)));
        repaint();



    }

    void clearAllPoints() {

        switch (currentLevel) {
            case 1:
                obstacles.clear();

                repaint();


                break;

            case 2:
                agentLines.clear();

                repaint();
                break;

            case 3:
                agents.clear();

                repaint();


                break;


        }
    }

    void setLevel(
            int currentLevel) {

        //this.loadObjectsIntoFile();
        points.clear();
        prevPoint = null;
        point = null;
        this.currentLevel = currentLevel;
        //this.loadExistingObjectsFromFile();


        this.repaint();


    }

    public void loadObjectsIntoScenario(SimulationScenario environment) {
//        throw new UnsupportedOperationException("Not yet implemented");

        environment.getObstacles().clear();



        if (!obstacles.isEmpty() && currentLevel >= 1) {
            for (int i = 0; i
                    < obstacles.size(); i++) {
                Obstacle tempObstacle = new Obstacle();


                for (int j = 0; j
                        < obstacles.get(i).getVertices().size(); j++) {
                    Position tempStorage = new Position();
                    tempStorage.setX((double) obstacles.get(i).getVertices().get(j).getX());
                    tempStorage.setY((double) obstacles.get(i).getVertices().get(j).getY());

                    tempObstacle.getVertices().add(tempStorage);



                }


                environment.getObstacles().add(tempObstacle);


            }
        }

        environment.getCrowd().clear();


        if (!agents.isEmpty() && currentLevel >= 3) {
            for (int i = 0; i
                    < agents.size(); i++) {
                Agent tempAgent = new Agent();

                Position start = new Position();
                start.setX((double) agents.get(i).getPosition().getX());
                start.setY((double) agents.get(i).getPosition().getY());

                Position end = new Position();
                end.setX((double) agents.get(i).getGoal().getX());
                end.setY((double) agents.get(i).getGoal().getY());

                tempAgent.setGoal(end);
                tempAgent.setPosition(start);

                tempAgent.setId(agents.get(i).getId());
                tempAgent.setPreferedSpeed(agents.get(i).getPreferedSpeed());
                tempAgent.setCommitmentLevel(agents.get(i).getCommitmentLevel());
                environment.getCrowd().add(tempAgent);



            }
        }

        environment.getGenerationLines().clear();


        if (!agentLines.isEmpty() && currentLevel >= 2) {
            for (int i = 0; i
                    < agentLines.size(); i++) {
                AgentLine tempLine = new AgentLine();

                Position start = new Position();
                start.setX((double) agentLines.get(i).getStartPoint().getX());
                start.setY((double) agentLines.get(i).getStartPoint().getY());

                Position end = new Position();
                end.setX((double) agentLines.get(i).getEndPoint().getX());
                end.setY((double) agentLines.get(i).getEndPoint().getY());

                tempLine.setStartPoint(start);
                tempLine.setEndPoint(end);

                tempLine.setFrequency(agentLines.get(i).getFrequency());
                tempLine.setNumber(agentLines.get(i).getNumber());
                tempLine.setDirection(agentLines.get(i).getDirection());

                environment.getGenerationLines().add(tempLine);



            }
        }
    }

    void setXSize(int xSize) {
        this.xSize = xSize;


    }

    void setYSize(int ySize) {
        this.ySize = ySize;


    }

    void setScale(int scale) {
        this.scale = scale;


    }

    public List<Agent> getAgents() {
        return agents;


    }

    void setAgentHighlight(int highlightedAgentNumber, boolean value) {
        highlightedAgent.set(highlightedAgentNumber, value);




    }

    void setObstacles(List<Obstacle> passedObstacles) {
        obstacles.clear();


        if (!passedObstacles.isEmpty()) {
            for (int i = 0; i
                    < passedObstacles.size(); i++) {
                Obstacle tempObstacle = new Obstacle();


                for (int j = 0; j
                        < passedObstacles.get(i).getVertices().size(); j++) {
                    Position tempStorage = new Position();
                    tempStorage.setX((double) passedObstacles.get(i).getVertices().get(j).getX());
                    tempStorage.setY((double) passedObstacles.get(i).getVertices().get(j).getY());

                    tempObstacle.getVertices().add(tempStorage);



                }


                obstacles.add(tempObstacle);


            }
        }

    }

    void setAgents(List<Agent> crowd) {
        agents.clear();


        for (int i = 0; i
                < crowd.size(); i++) {
            agents.add(crowd.get(i));
            highlightedAgent.add(false);

        }

    }
}
