/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import agent.AgentPortrayal;
import agent.RVOAgent;
import agent.clustering.ClusteredSpace;
import agent.latticegas.LatticeSpace;
import app.params.SimulationParameters;
import device.Device;
import device.DevicePortrayal;
import environment.xml.XMLScenarioManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import motionPlanners.rvo2.RVO_2_1;
import utility.Geometry;

/**
 *
 * @author vaisaghvt
 * 
 * Description : Initialize parameters from xml files. The first line sets which 
 * xml file will be used.
 * 
 */
public class PropertySet {

    /**
     * The file to use specifying the parameters for this run.
     */
    public static String PROPERTIES_FILENAME = "CrowdProperties" + File.separatorChar +
//            "CW2011" + File.separatorChar +
            "CW2011PaperSettings.xml";
    
    public static enum Model {
        // Add full path of class to be called for the constructor. Also if there are
        // parameters for the constructor change at the place where it is called. 
        // (find references to get Associated Class
        RVO2("motionPlanners.rvo2.RVO_2_1"), 
        RVO1Standard("motionPlanners.rvo1.RVO_1_Standard"), 
        RVO1Acceleration("motionPlanners.rvo1.RVO_1_WithAccel"), 
        RuleBasedNew("motionPlanners.rvo1.RuleBasedNew"), 
        SocialForce("motionPlanners.socialforce.SocialForce");
        
        private String velocityCalculator;
   
        Model(String calculator){
            this.velocityCalculator = calculator;
        }
        
        
        public String getAssociatedClass(){
            return this.velocityCalculator;
        }
        
       
    }
    //TODO : Be careful  about this seed... need to change for random simulation
    public static String XML_SOURCE_FOLDER = "xml-resources" + File.separatorChar;
    public static String PROPERTIES_FILEPATH;
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
    public static int PBMSCENARIO;
    public static boolean HIGH_PRECISION;
    public static int DEVICE_SENSOR_RANGE;
    public static int DEVICE_THRESHOLD;
    public static int DEVICE_MSG_THRESHOLD;
    public static int DEVICE_MAX_HOPS;

    
    public static void writePropertiesToFile(String fileName) {
        File file = new File(fileName);

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("writing properties");
        writer.println("Properties File =" + PROPERTIES_FILEPATH);

        writer.println("Scenario File =" + FILEPATH);

        writer.println("Model used = " + MODEL);

        writer.println("******Summary of main points*******");
        writer.println("Seed =" + SEED);
        writer.println("TimeStep =" + TIMESTEP);


        writer.println("Radius= " + RVOAgent.RADIUS);
        writer.println("Sensor= " + RVOAgent.SENSOR_RANGE);


        writer.println("Clustering =" + USECLUSTERING);
        if (USECLUSTERING) {
            writer.println("\t Alpha =" + ClusteredSpace.ALPHA);
            writer.println("\t Number Of Clustering Spaces =" + ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES);
        }
        writer.println("Info Processing =" + INFOPROCESSING);
        if (INFOPROCESSING) {
            writer.println("\t InfoLimit =" + RVOAgent.INFO_LIMIT);
        }

        writer.println("RVO parameters");
        writer.println("\t Time Horizon =" + RVO_2_1.TIME_HORIZON);
        writer.println("\t Time Horizon obst =" + RVO_2_1.TIME_HORIZON_OBSTACLE);

        if (LATTICEMODEL) {
            writer.println("Drift =" + LatticeSpace.DRIFT);
        }
        writer.print("PBM Scenario ="+PBMSCENARIO);
        writer.close();
    }

    static void initializeProperties(String filePath) {
        if (filePath.isEmpty()) {
            PROPERTIES_FILEPATH = XML_SOURCE_FOLDER + PROPERTIES_FILENAME;
        } else {
            PROPERTIES_FILEPATH = filePath;
        }
        XML_SOURCE_FOLDER = XML_SOURCE_FOLDER + "scenarios" + File.separatorChar;
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
            HIGH_PRECISION = params.isHighPrecision();
            
            //AGENT DISPLAY PARAMETERS
            AgentPortrayal.SHOW_ORCA_LINES = params.isShowLines();
            AgentPortrayal.SHOW_VELOCITY = params.isShowVelocity();
            AgentPortrayal.SHOW_TRAILS = params.isTrails();
            
//            AgentPortrayal.SHOW_PERCEPTION = params.isShowPerception();
            AgentPortrayal.SHOW_STP = false; //default false

            //AGENT PARAMETERS
            RVOAgent.RADIUS = params.getAgentRadius();
            RVOAgent.INFO_LIMIT = params.getInfoLimit();
            RVOAgent.DEFAULT_PREFERRED_SPEED = params.getPreferredSpeed();
            RVOAgent.SENSOR_RANGE = params.getSensorRange();

            Geometry.EPSILON = params.getRVOEpsilon();  //@hunan: in this case, can avoid symmetric deadlock in 1to1 case, but cannot avoid crossing4corner cases
            
          
            //Device PARAMETERS
            Device.RADIUS = params.getAgentRadius();
//           Device.DEFAULT_PREFERRED_SPEED = params.getPreferredSpeed();
//            Device.SENSOR_RANGE = params.getSensorRange();
            Device.SENSOR_RANGE = params.getDeviceSensorRange();
            Device.DEVICE_THRESHOLD= params.getDeviceThreshold();
            Device.DEVICE_MSG_THRESHOLD = params.getDeviceMsgThreshold();
            Device.DEVICE_MAX_HOPS = params.getDeviceMaxHops();
            
            Device.DEVICE_HOLDING_PROBABILITY = params.getDeviceHoldingProbability();
            Device.DEVICE_TRUST  = params.getDeviceTrust();
            
            Device.FORBIDDENAREA_APPROACH = params.isForbiddenAreaApproach();
            Device.MIN_DIST_TO_GOAL = params.isMinDistToGoal();

            
            if (USECLUSTERING) {
                ClusteredSpace.ALPHA = params.getAlpha();
                ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES = params.getNumberOfClusteringSpaces();
            }

            MODEL = Model.valueOf(params.getModel());

            if (MODEL == Model.SocialForce) {
                LATTICEMODEL = false;
            }

            if (LATTICEMODEL) {
                LatticeSpace.DRIFT = params.getDrift();
            }

//            if (MODEL == PropertySet.Model.RVO2) {
                
//                RVO_2_1.TIME_HORIZON = params.getTimeHorizon();
//                RVO_2_1.TIME_HORIZON_OBSTACLE = params.getTimeHorizonObst();
//            }
            
            if(params.isUseClustering()){
                ClusteredSpace.NUMBER_OF_CLUSTERING_SPACES = params.getNumberOfClusteringSpaces();
                
            }

            RVO_2_1.TIME_HORIZON = params.getTimeHorizon();
            RVO_2_1.TIME_HORIZON_OBSTACLE = params.getTimeHorizonObst();
         
        } catch (JAXBException ex) {
            Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
