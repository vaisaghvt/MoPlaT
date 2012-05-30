/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import agent.clustering.ClusteredSpace;
import app.PropertySet.Model;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
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

    /**
     * This is a singleton class that holds all the display information
     */
    public Display2D display;
    public JFrame displayFrame;
    private RVOModel model;
    /**
     * Number of pixels that each cell (or unit space) should be represented by (for display).
     */
    public static int scale;
    public static int checkSizeX;
    public static int checkSizeY;
    ContinuousPortrayal2D geographyPortrayal;
    ContinuousPortrayal2D agentPortrayal;
    ContinuousPortrayal2D[] clusteredPortrayal;
    ObjectGridPortrayal2D checkBoardPortrayal;
    FastValueGridPortrayal2D latticeGasPortrayal;
    FastValueGridPortrayal2D socialForcePortrayal;

    public RVOGui() {
        this(new RVOModel(PropertySet.SEED));
    }

    public RVOGui(SimState state) {
        super(state);
        if (!PropertySet.INITIALISEFROMXML) {
            scale = PropertySet.SCALE;
            checkSizeX = PropertySet.CHECK_SIZE_X;
            checkSizeY = PropertySet.CHECK_SIZE_Y;
        }
        model = (RVOModel) state;
        geographyPortrayal = new ContinuousPortrayal2D();
        checkBoardPortrayal = new ObjectGridPortrayal2D();
        agentPortrayal = new ContinuousPortrayal2D();


        if (PropertySet.LATTICEMODEL) {
            latticeGasPortrayal = new FastValueGridPortrayal2D();
        }

        if (PropertySet.MODEL == Model.SocialForce) {
            socialForcePortrayal = new FastValueGridPortrayal2D();
        }


        /**
         *If clustering is being used then create multiple layers for each 
         * cluster layer
         */
        if (PropertySet.USECLUSTERING) {
            clusteredPortrayal = new ContinuousPortrayal2D[ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES];
            for (int j = 0; j < ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES; j++) {
                clusteredPortrayal[j] = new ContinuousPortrayal2D();
            }
        }
    }

    /**
     *  tell the portrayals what to portray and how to portray them
     */
    public void setupPortrayals() {


        agentPortrayal.setField(model.getRvoSpace().getCurrentAgentSpace());


        checkBoardPortrayal.setField(new ObjectGrid2D(checkSizeX, checkSizeY));
        checkBoardPortrayal.setPortrayalForNull(new RectanglePortrayal2D(new Color(0.4f, 0.4f, 0.4f, 0.7f), 1.0f, false));

        geographyPortrayal.setField(model.getRvoSpace().getGeographySpace());

        if (PropertySet.LATTICEMODEL) {
            latticeGasPortrayal.setField(model.getLatticeSpace().getSpace());
            latticeGasPortrayal.setMap(new sim.util.gui.SimpleColorMap(
                    new Color[]{new Color(0, 0, 255, 140), new Color(0, 0, 255, 150), Color.red}));
        }

        if (PropertySet.USECLUSTERING) {
            for (int j = 0; j < ClusteredSpace.getNumberOfClusteringSpaces(); j++) {
                clusteredPortrayal[j].setField(((ClusteredSpace) model.getRvoSpace()).getClusteredSpace(j));
            }
        }

//        if(PropertySet.MODEL == Model.SocialForce) {
//            socialForcePortrayal.setField(SocialForce.forceGrid);
//            socialForcePortrayal.setMap(new sim.util.gui.SimpleColorMap(
//                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 255, 150), Color.red}));
//        }
    }

    @Override
    public void start() {
        super.start();
        setupPortrayals();
        
        if (PropertySet.TRACK_DATA) {
            if (model.getDataTracker().hasChart()) {

                JPanel chartPanel = new ChartPanel(model.getDataTracker().getChart());
                JFrame frame = new JFrame("CW2011 chart");
                frame.add(chartPanel);
                // perhaps you might move the chart to where you like.
                frame.setTitle(model.getDataTracker().trackerType());

                frame.setVisible(true);
                frame.setLocation(50, 50);
                frame.setSize(500, 500);
                this.controller.registerFrame(frame);
            }
        }
        
        display.reset();
        display.repaint();
    }

    @Override
    public void load(SimState state) {
        super.load(state);
        setupPortrayals();  // set up our portrayals for the new SimState model
        display.reset();    // reschedule the displayer
        display.repaint();  // redraw the display
    }

    /**
     * This function controls the controller on the side of the display with all 
     * the play and stuff
     * @param c Controller that is responsible for running the simulation
     */
    @Override
    public void init(Controller c) {
        super.init(c);

        // Make the Display2D.  We'll have it display stuff later.
        model = (RVOModel) state;

        display = new Display2D(model.getWorldXSize() * scale, model.getWorldYSize() * scale, this, 1);

        //create and display frame
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
        displayFrame.setResizable(false);

        display.attach(geographyPortrayal, "Geography portrayal");  // attach the portrayals
        display.attach(agentPortrayal, "Agent portrayal");  // attach the portrayals
        if (PropertySet.CHECKBOARD) {
            display.attach(this.checkBoardPortrayal, "Check board");
        }
        if (PropertySet.USECLUSTERING) {
            for (int j = 1; j
                    <= ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES; j++) {
                display.attach(clusteredPortrayal[j - 1], "Cluster portrayal -" + j);
            }
        }
        if (PropertySet.LATTICEMODEL) {
            display.attach(latticeGasPortrayal, "lattice portrayal");


        }

        

        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(new Color(220, 220, 220));
    }

    @Override
    public void quit() {
        super.quit();
        if (displayFrame != null) {
            displayFrame.dispose();
        }
        displayFrame = null;  // let gc
        display = null;       // let gc
    }

    /**
     * These two methods return the name of the applet window and it's description 
     * wherever used. It actually practically overrides Mason's in-built methods
     * of the same name. For how this is actually done, check documentation.
     * @return 
     */
    public static String getName() {
        return "Crowd Simulation";
    }

    public static Object getInfo() {
        return "<H2>Motion planning testbed</H2><p>A testbed for various different"
                + " motion planning and collision avoidance systems.</p>";
    }

    public static void main(String[] args) {
        PropertySet.initializeProperties("");
        new RVOGui().createController();
    }
}
