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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import motionPlanners.rvo2.RVO_2_1;
import app.params.SimulationParameters;

/**
 *
 * @author vaisaghvt
 */
public class PropertySet {

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
            RVOAgent.PREFERRED_SPEED = params.getPreferredSpeed();
            RVOAgent.SENSOR_RANGE = params.getSensorRange();

            if (USECLUSTERING) {
                ClusteredSpace.ALPHA = params.getAlpha();
                ClusteredSpace.CLUSTER_DIAMETER = (int) Math.round(
                        ClusteredSpace.ALPHA * 2 * RVOAgent.RADIUS * 100.0);
                ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES = params.getNumberOfClusteringSpaces();
            }

            MODEL = Model.valueOf(params.getModel());

            if (LATTICEMODEL) {
                LatticeSpace.DRIFT = params.getDrift();
            }

            if (MODEL == PropertySet.Model.RVO2) {
                RVO_2_1.RVO_EPSILON = params.getRVOEpsilon();
                RVO_2_1.TIME_HORIZON = params.getTimeHorizon();
                RVO_2_1.TIME_HORIZON_OBSTACLE = params.getTimeHorizonObst();
            }
            
            if(params.isUseClustering()){
                ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES = params.getNumberOfClusteringSpaces();
                
            }
            
            
            
            

        } catch (JAXBException ex) {
            Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static enum Model {

        RVO2, PatternBasedMotion, RVO1Standard, RVO1Acceleration, RuleBasedNew, SocialForce
    }
    //TODO : Be careful  about this seed... need to change for random simulation
    public static final String XML_SOURCE_FOLDER = "xml-resources//scenarios";
    public static String PROPERTIES_FILEPATH = XML_SOURCE_FOLDER + "//CrowdProperties//1.xml";
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
}
