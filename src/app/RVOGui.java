/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import agent.RVOAgent;
import agent.clustering.ClusteredSpace;
import environment.Obstacle.RVOObstacle;
import environment.latticegas.LatticeSpace;
import java.awt.Color;
import javax.swing.JFrame;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

/**
 * RVOGui
 *
 * @author michaellees
 * Created: Nov 24, 2010
 *
 * Copyright michaellees  
 *
 * Description:
 *
 * Gui window for running the testbed in MASON. This file that contains the main
 * method which should be executed when a visual display and control is required.
 */
public class RVOGui extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    private RVOModel model;
    /**
     * Number of pixels that each cell (or unit space) should be represented by (for display).
     */
    public static int SCALE = 80;
    public static int checkSizeX = 5;
    public static int checkSizeY = 5;


    private boolean checkBoard = true;
    ContinuousPortrayal2D geographyPortrayal;
    ContinuousPortrayal2D agentPortrayal;
    ContinuousPortrayal2D[] clusteredPortrayal;
    ObjectGridPortrayal2D checkBoardPortrayal;
 
    FastValueGridPortrayal2D latticeGasPortrayal;

    public RVOGui(SimState state) {
        super(state);
        this.model = (RVOModel) state;
        geographyPortrayal = new ContinuousPortrayal2D();
        checkBoardPortrayal = new ObjectGridPortrayal2D();

        agentPortrayal = new ContinuousPortrayal2D();

        if (RVOModel.LATTICEMODEL) {
            latticeGasPortrayal = new FastValueGridPortrayal2D();
          
        }

        if (RVOModel.USECLUSTERING) {
            clusteredPortrayal = new ContinuousPortrayal2D[ClusteredSpace.getNumberOfClusteringSpaces()];

            for (int j = 0; j < ClusteredSpace.getNumberOfClusteringSpaces(); j++) {
                clusteredPortrayal[j] = new ContinuousPortrayal2D();
            }
        }
    }

    public void setupPortrayals() {
        // tell the portrayals what to portray and how to portray them

        agentPortrayal.setField(this.model.getRvoSpace().getCurrentAgentSpace());
        //   agentPortrayal.setPortrayalForClass(RVOAgent.class, RVOAgent.getPortrayal()); // DOUBT : vvt: this code doesn't seem to have any effect on anything...

        this.checkBoardPortrayal.setField(new ObjectGrid2D(checkSizeX, checkSizeY));
        this.checkBoardPortrayal.setPortrayalForNull(new RectanglePortrayal2D(new Color(0.4f, 0.4f, 0.4f, 0.7f), 1.0f, false));

        this.geographyPortrayal.setField(this.model.getRvoSpace().getGeographySpace());

        if (RVOModel.LATTICEMODEL) {
            this.latticeGasPortrayal.setField(this.model.getLatticeSpace().getSpace());
            latticeGasPortrayal.setMap(new sim.util.gui.SimpleColorMap(
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 255, 150), Color.red}));

            
            

        }

        if (RVOModel.USECLUSTERING) {
            for (int j = 0; j < ClusteredSpace.getNumberOfClusteringSpaces(); j++) {
                this.clusteredPortrayal[j].setField(((ClusteredSpace) this.model.getRvoSpace()).getClusteredSpace(j));
            }
        }
        display.reset();
        display.repaint();
    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void init(Controller c) {
        super.init(c);

        // Make the Display2D.  We'll have it display stuff later.
        model = (RVOModel) state;       // vvt : Where does this function get "state" from?
        display = new Display2D(model.getWorldXSize() * SCALE, model.getWorldYSize() * SCALE, this, 1);

        //create and display frame
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
        displayFrame.setResizable(false);

        display.attach(geographyPortrayal, "Geography portrayal");  // attach the portrayals
        display.attach(agentPortrayal, "Agent portrayal");  // attach the portrayals
        if (checkBoard) {
            display.attach(this.checkBoardPortrayal, "Check board");
        }
        if (RVOModel.USECLUSTERING) {
            for (int j = 1; j
                    <= ClusteredSpace.getNumberOfClusteringSpaces(); j++) {
                display.attach(clusteredPortrayal[j - 1], "Cluster portrayal -" + j);
            }
        }
        if (RVOModel.LATTICEMODEL) {
            display.attach(latticeGasPortrayal, "lattice portrayal");
   

        }

        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(new Color(220, 220, 220));
    }

    public void quit() {
        super.quit();
        if (displayFrame != null) {
            displayFrame.dispose();
        }
        displayFrame = null;  // let gc
        display = null;       // let gc
    }

    public static Object getInfo() {
        return "<H2>Motion planning testbed</H2><p>A testbed for various different"
                + " motion planning and collision avoidance systems.";
    }
}
