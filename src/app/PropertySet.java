/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import agent.AgentPortrayal;
import agent.RVOAgent;
import agent.clustering.ClusteredSpace;
import agent.latticegas.LatticeSpace;
import environment.XMLScenarioManager;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import motionPlanners.rvo2.RVO_2_1;
import app.params.SimulationParameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import utility.Geometry;

/**
 *
 * @author vaisaghvt
 */
public class PropertySet {

  

    public static enum Model {

        RVO2, PatternBasedMotion, RVO1Standard, RVO1Acceleration, RuleBasedNew, SocialForce
    }
    //TODO : Be careful  about this seed... need to change for random simulation
    public static final String XML_SOURCE_FOLDER = "xml-resources"+File.separatorChar+"scenarios"+File.separatorChar;
    public static String PROPERTIES_FILEPATH = XML_SOURCE_FOLDER + "CrowdProperties"+File.separatorChar+
            "CW2011PaperSettings.xml";
    public static long SEED;
    public static int WORLDXSIZE;
    public static int WORLDYSIZE;
    public static double GRIDSIZE;
    public static double TIMESTEP;
    public static Model MODEL;
    public static boolean LATTICEMODEL;
    public static boolean INFOPROCESSING;
    public static boolean USECLUSTERING;
    public static boolean INITIALISEFROMXML;
    public static String FILEPATH;
    public static int SCALE;
    public static int CHECK_SIZE_X;
    public static int CHECK_SIZE_Y;
    public static boolean CHECKBOARD;
    public static boolean TRACK_DATA;

    public static void writePropertiesToFile(String fileName) {
         File file = new File(fileName);

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("writing properties");
        writer.println("Properties File ="+PROPERTIES_FILEPATH);
        
        writer.println("Scenario File ="+FILEPATH);
        
        writer.println("Model used = " + MODEL);
        
        writer.println("******Summary of main points*******");
        writer.println("Seed ="+SEED);
        writer.println("TimeStep ="+TIMESTEP);
              
        
        writer.println("Radius= "+ RVOAgent.RADIUS);
        writer.println("Sensor= "+ RVOAgent.SENSOR_RANGE);
        
        
        writer.println("Clustering ="+USECLUSTERING);
        if(USECLUSTERING){
            writer.println("\t Alpha ="+ClusteredSpace.ALPHA);
            writer.println("\t Number Of Clustering Spaces ="+ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES);
        }
        writer.println("Info Processing ="+INFOPROCESSING);
        if(INFOPROCESSING){
            writer.println("\t InfoLimit ="+RVOAgent.INFO_LIMIT);
        }
        
        writer.println("RVO parameters");
        writer.println("\t Time Horizon ="+ RVO_2_1.TIME_HORIZON);
        writer.println("\t Time Horizon obst ="+ RVO_2_1.TIME_HORIZON_OBSTACLE);
        
           if (LATTICEMODEL) {
                writer.println("Drift ="+LatticeSpace.DRIFT) ;
            }
        writer.close();
    }
    
    static void initializeProperties() {
        try {
            XMLScenarioManager settings = XMLScenarioManager.instance("app.params");
            SimulationParameters params = (SimulationParameters) settings.unmarshal(PropertySet.PROPERTIES_FILEPATH);

            //MODEL PARAMETERS
            SEED = params.getSeed();
            WORLDXSIZE = params.getWorldXSize();
            WORLDYSIZE = params.getWorldYSize();
            GRIDSIZE = params.getGridSize();
            TIMESTEP = params.getTimeStep();
            INITIALISEFROMXML = params.isInitialiseFromXML();
            LATTICEMODEL = params.isLatticeModel();
            INFOPROCESSING = params.isInfoProcessing();
            USECLUSTERING = params.isUseClustering();
            TRACK_DATA = params.isTrackData();
            FILEPATH = XML_SOURCE_FOLDER + params.getFilePath();
            //          FILEPATH= XML_SOURCE_FOLDER+"//EvacTest//5.xml";
            //FOR GUI
            CHECKBOARD = params.isCheckBoard();
            CHECK_SIZE_X = params.getDefaultCheckSizeX();
            CHECK_SIZE_Y = params.getDefaultCheckSizeY();
            SCALE = params.getDefaultScale();


            //AGENT DISPLAY PARAMETERS
            AgentPortrayal.SHOW_ORCA_LINES = params.isShowLines();
            AgentPortrayal.SHOW_VELOCITY = params.isShowVelocity();
            AgentPortrayal.SHOW_TRAILS = params.isTrails();



            //AGENT PARAMETERS
            RVOAgent.RADIUS = params.getAgentRadius();
            RVOAgent.INFO_LIMIT = params.getInfoLimit();
            RVOAgent.DEFAULT_PREFERRED_SPEED = params.getPreferredSpeed();
            RVOAgent.SENSOR_RANGE = params.getSensorRange();

            Geometry.EPSILON = params.getRVOEpsilon();
            if (USECLUSTERING) {
                ClusteredSpace.ALPHA = params.getAlpha();
                ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES = params.getNumberOfClusteringSpaces();
            }

            MODEL = Model.valueOf(params.getModel());

            if (LATTICEMODEL) {
                LatticeSpace.DRIFT = params.getDrift();
            }


            RVO_2_1.TIME_HORIZON = params.getTimeHorizon();
            RVO_2_1.TIME_HORIZON_OBSTACLE = params.getTimeHorizonObst();
          

        } catch (JAXBException ex) {
            Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
