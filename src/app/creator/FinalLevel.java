/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author vaisagh
 */
class FinalLevel extends CreatorLevel {

    private DrawingPanel interactionArea;

    public FinalLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
        super(model, frame, statusBar, buttonArea);
        this.interactionArea = interactionArea;
    }

    @Override
    public void setUpLevel() {
        nextButton.setText("Finish and Save");
        clearButton.setEnabled(false);
        previousButton.setEnabled(true);
        nextButton.setEnabled(true);


        frame.setTitle(model.getTitle() + ".xml  - Final Stage -");


        frame.setSize(model.getxSize() * model.getScale() + 8, model.getySize() * model.getScale() + 100);
        frame.repaint();


        frame.add(interactionArea, BorderLayout.CENTER);
        interactionArea.setBackground(Color.lightGray);
        interactionArea.setEnabled(false);
        statusBar.setText("Final stage");
        interactionArea.setCurrentLevel(this);

        interactionArea.repaint();
    }

    @Override
    public void clearUp() {
        nextButton.setText("Next");
        interactionArea.setEnabled(false);
        frame.remove(interactionArea);
    }

    @Override
    public void draw(Graphics g) {

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

        if (!model.getGoalLines().isEmpty()) {
            super.drawGoalLines(g, (ArrayList) model.getGoalLines());
        }
    }

    @Override
    public void clearAllPoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
