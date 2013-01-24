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
 * Description: This class defines an inspector for the RVOAgents. An inspector
 * allows user to click on an agent and get information about the agent. 
 *
 */
import app.PropertySet;
import app.PropertySet.Model;
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

public class AgentInspector extends Inspector {

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

        // set up our inspector: keep the properties inspector around too
        setLayout(new BorderLayout());
        add(originalInspector, BorderLayout.CENTER);
        add(viewBox, BorderLayout.SOUTH);


        JButton trailSwitch = new JButton("Toggle Trails");
        viewBox.add(trailSwitch);
        viewBox.add(Box.createGlue());




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
        
        if (PropertySet.MODEL == Model.RVO2) {
            JButton orcaLineSwitch = new JButton("Toggle Orca Lines");
            viewBox.add(orcaLineSwitch);
            viewBox.add(Box.createGlue());
            orcaLineSwitch.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (state.schedule) {
                        // clear trails
                        agent.toggleShowOrcaLines();

                        // update everything: console, inspectors, displays,
                        // everything that might be affected by randomization
                        console.refresh();
                    }
                }
            });
        }
        

        JButton velocityLineSwitch = new JButton("Toggle Velocity Lines");
        viewBox.add(velocityLineSwitch);
        viewBox.add(Box.createGlue());
        velocityLineSwitch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (state.schedule) {
                    // clear trails
                    agent.toggleShowVelocity();

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
