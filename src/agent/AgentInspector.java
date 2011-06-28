/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Apr 26, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */import sim.portrayal.grid.ObjectGridPortrayal2D;

import sim.util.Int2D;
class AgentInspector extends Inspector{

    public Inspector originalInspector;

    public AgentInspector(Inspector originalInspector,
        LocationWrapper wrapper,
        GUIState guiState)
        {
        this.originalInspector = originalInspector;

        // get info out of the wrapper
        ObjectGridPortrayal2D gridportrayal = (ObjectGridPortrayal2D) wrapper.getFieldPortrayal();
        // these are final so that we can use them in the anonymous inner class below...
        final ObjectGrid2D grid = (ObjectGrid2D)(gridportrayal.getField());
        final RVOAgent agent = (RVOAgent) wrapper.getObject();
        final SimState state = guiState.state;
        final Controller console = guiState.controller;  // The Console (it's a Controller subclass)

        // now let's add a Button
        Box box = new Box(BoxLayout.X_AXIS);
        JButton button = new JButton("Roll the Dice");
        box.add(button);
        box.add(Box.createGlue());

        // set up our inspector: keep the properties inspector around too
        setLayout(new BorderLayout());

        add(originalInspector, BorderLayout.CENTER);

        add(box, BorderLayout.SOUTH);

    // set what the button does
        button.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synchronized(state.schedule)
                    {
                   
                    // randomize location
                    Int2D loc = agent.getMySpace().generateRandomLocation();


                    agent.getMySpace().updatePositionOnMap(agent, loc.x, loc.y);


                    // repaint everything: console, inspectors, displays,
                    // everything that might be affected by randomization
                    console.refresh();
                    }
                }
            });
        }

    public void updateInspector()
        {
        originalInspector.updateInspector();
        }


   

}
