/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

/**
 * 
 *
 * @author Vaisagh
 * 
 * Description: This class defines an inspector for the RVOAgents.
 *
 */
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;


class AgentInspector extends Inspector {

    public Inspector originalInspector;

    public AgentInspector(Inspector originalInspector, LocationWrapper wrapper, GUIState guiState) {

        this.originalInspector = originalInspector;

        // get info out of the wrapper

//        ContinuousPortrayal2D agentPortrayal = (ContinuousPortrayal2D) wrapper.getFieldPortrayal();
        // these are final so that we can use them in the anonymous inner class below...
//        final Continuous2D agentField = (Continuous2D) (agentPortrayal.getField());


        final RVOAgent agent = (RVOAgent) wrapper.getObject();
        final SimState state = guiState.state;
        final Controller console = guiState.controller;  // The Console (it's a Controller subclass)

        // now let's add a Button
        Box viewBox = new Box(BoxLayout.X_AXIS);
        JButton trailSwitch = new JButton("Toggle Trails");
        viewBox.add(trailSwitch);
        viewBox.add(Box.createGlue());

        // set up our inspector: keep the properties inspector around too
        setLayout(new BorderLayout());
        add(originalInspector, BorderLayout.CENTER);
        add(viewBox, BorderLayout.SOUTH);

        // set what the trailSwitch does
        trailSwitch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (state.schedule) {
                    // clear trails
                    agent.toggleTrails();

                    // update everything: console, inspectors, displays,
                    // everything that might be affected by randomization
                    console.refresh();
                }
            }
        });
        
        
    }

    @Override
    public void updateInspector() {
        originalInspector.updateInspector();
    }
}
