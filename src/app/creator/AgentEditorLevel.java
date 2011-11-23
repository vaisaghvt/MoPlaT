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
class AgentEditorLevel extends CreatorLevel  {

    private DrawingPanel interactionArea;
    private ArrayList<Boolean> highlightedAgents;
    private ArrayList<Agent> agents;
  
    private AgentDescriptionFrame agentArea;

    public AgentEditorLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
        super(model, frame, statusBar, buttonArea);
        this.interactionArea = interactionArea;
       
    }

    @Override
    public void setUpLevel() {
        if (model.getAgents().isEmpty()) {
            agents = new ArrayList<Agent>();
            highlightedAgents = new ArrayList<Boolean>();
        } else {
            agents = (ArrayList<Agent>) model.getAgents();
             highlightedAgents = new ArrayList<Boolean>();
            for (Agent agent : agents) {
                highlightedAgents.add(false);
            }
        }


        previousButton.setEnabled(true);
        clearButton.setEnabled(false);
        nextButton.setEnabled(true);

        frame.setTitle(model.getTitle() + ".xml  - Agent Editting -");
        frame.setSize(model.getxSize() * model.getScale() + 8, model.getySize() * model.getScale() + 100);
        frame.repaint();


        interactionArea.setBackground(Color.lightGray);
        interactionArea.setEnabled(false);
        statusBar.setText("Agent Editting stage");
        
        agentArea = new AgentDescriptionFrame(agents, this);


        frame.add(interactionArea, BorderLayout.CENTER);
        interactionArea.setCurrentLevel(this);      
        interactionArea.repaint();
   
    }

    @Override
    public void clearUp() {
        agentArea.dispose();
        model.setAgents(agents);
        interactionArea.setEnabled(false);
        frame.remove(interactionArea);
    }

    @Override
    public void draw(Graphics g) {
        super.drawGridLines(g);

        if (!model.getObstacles().isEmpty()) {
            super.drawObstacles(g, model.getObstacles());
        }
        if (!model.getAgentLines().isEmpty()) {
            super.drawAgentLines(g, model.getAgentLines());
        }
        if (!agents.isEmpty()) {
            if(highlightedAgents.isEmpty()){
                super.drawAgents(g, agents, null);
            }else{
                super.drawAgents(g, agents, highlightedAgents);
            }
        }
    }


    void setAgentHighlight(int i, boolean value) {
        this.highlightedAgents.set(i, value);
    }

    void repaintRequest() {
        interactionArea.repaint();
    }

    @Override
    public void clearAllPoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  
}
