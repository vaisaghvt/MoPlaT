/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.AgentGroup;
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
public class AgentGroupCreatorLevel extends CreatorLevel implements MouseListener, MouseMotionListener{

    private DrawingPanel interactionArea;
    private ArrayList<Position> points;
    private ArrayList<AgentGroup> agentGroups;
    private Position point;
    private Position prevPoint;
    private Point currentPoint;

    public AgentGroupCreatorLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
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

        if (model.getAgentGroups().isEmpty()) {
            agentGroups = new ArrayList<AgentGroup>();
        } else {
            agentGroups = (ArrayList<AgentGroup>) model.getAgentGroups();
        }


        previousButton.setEnabled(true);
        clearButton.setEnabled(true);
        nextButton.setEnabled(true);

        frame.setTitle(model.getTitle() + ".xml  - Create Agent Groups -");
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
        model.setAgentGroups(agentGroups);
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
        
        if(!model.getAgentLines().isEmpty()){
            super.drawAgentLines(g, model.getAgentLines());
        }
        
        
        if(!agentGroups.isEmpty()){
            super.drawAgentGroups(g, agentGroups);
        }
    }

    @Override
    public void clearAllPoints() {
        points.clear();
        agentGroups.clear();
        interactionArea.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        boolean validityCheck = super.mouseReleaseDefaultActions(e, currentPoint, point, prevPoint);

        if (!validityCheck) {
            return;
        }

        //If the click is not in the same line as the previous one then reset X and Y vluaes to previous values
        // If it is near enough to approximate then do that... 
        
        if (prevPoint.getX() >= 0) {
            if (Math.abs(point.getX() - prevPoint.getX()) < (CreatorMain.AGENT_RADIUS * 2.0) || 
                    Math.abs(point.getY() - prevPoint.getY()) < (CreatorMain.AGENT_RADIUS * 2.0) ) {
                point.setX(prevPoint.getX());
                point.setY(prevPoint.getY());
                return;
            }
            
            int xDifference = (((int)(Math.abs(point.getX() - prevPoint.getX())*100.0))
                                %((int)(CreatorMain.AGENT_RADIUS *200.0)));
            int yDifference = (((int)(Math.abs(point.getY() - prevPoint.getY())*100.0))
                                %((int)(CreatorMain.AGENT_RADIUS *200.0)));
            boolean success = false;
            
            if ( xDifference!=0 && (double)xDifference/100.0 < CreatorMain.AGENT_RADIUS){
                point.setX(point.getX() + (double)xDifference/100.0);
                success = true;
            }
            if ( yDifference !=0 && (double)yDifference/100.0 < CreatorMain.AGENT_RADIUS){
                point.setY(point.getY() + (double)yDifference/100.0);
                success = true;
            }
            if(!(success||(xDifference ==0 && yDifference ==0))){
                return;
            }
          
        }

        if (points.size() < 1) {
            statusBar.setText("Agent Group started at " + point.getX() + "," + point.getY() + ". Please select the other corner.");
            Position tempStorage = new Position();
            tempStorage.setX(point.getX());
            tempStorage.setY(point.getY());
            points.add(tempStorage);
      } else if (points.size() == 1) {
            
            
            
            
            AgentGroup tempAgentGroup = new AgentGroup();
            
            int size=0;
            
            
            Position tempStorage = new Position();
            tempStorage.setX(point.getX()>points.get(0).getX()?point.getX():points.get(0).getX());
            tempStorage.setY(point.getY()>points.get(0).getY()?point.getY():points.get(0).getY());
            tempAgentGroup.setEndPoint(tempStorage);
            
            tempStorage = new Position();
            tempStorage.setX(point.getX()<points.get(0).getX()?point.getX():points.get(0).getX());
            tempStorage.setY(point.getY()<points.get(0).getY()?point.getY():points.get(0).getY());
            tempAgentGroup.setStartPoint(tempStorage);

         
            statusBar.setText("Agent Group end corner set at  " + point.getX() + "," + point.getY());
            
                       
            
            String number;
            do {
                number = (String) JOptionPane.showInputDialog(
                        null,
                        "How many agents should be put in this area?",
                        "Input",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "1");
               try {
                    size = Integer.parseInt(number);
                } catch (NumberFormatException numException) {
                    continue;
                }
            } while (!isValidSize(size, tempAgentGroup));
            
            tempAgentGroup.setSize(size);
            agentGroups.add(tempAgentGroup);

            prevPoint.setX(-1.0);
            prevPoint.setY(-1.0);
            point.setX(-1.0) ;
            point.setY(-1.0);
            points.clear();


        }

        interactionArea.repaint();
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
         super.calculateCurrentPoint(e, currentPoint,false);
        interactionArea.repaint();
    }

    private boolean isValidSize(int numberOfAgents, AgentGroup tempAgentGroup) {
        double width = tempAgentGroup.getEndPoint().getX() - tempAgentGroup.getStartPoint().getX();
        double height = tempAgentGroup.getEndPoint().getY() - tempAgentGroup.getStartPoint().getY();
        
        // TODO : Checks if so many agents can possible be created in this area.
        double minAreaRequired 
                = Math.pow((Math.ceil(Math.sqrt(numberOfAgents))),2.0) 
                    * (Math.pow(CreatorMain.AGENT_RADIUS*2, 2.0));
        
              
        if(minAreaRequired > width * height){
            
             statusBar.setText("Cannot fit in this area. Please choose a lower number");
            
            return false;
            
        }
        
        return true;
    }
}
