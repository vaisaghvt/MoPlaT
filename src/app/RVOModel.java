package app;

import app.dataTracking.CWDataCollector;
import agent.AgentGenerator;
import agent.RVOAgent;
import agent.RVOAgent.Act;
import agent.RVOAgent.SenseThink;
import agent.clustering.ClusteredSpace;
import environment.Obstacle.RVOObstacle;
import environment.RVOSpace;
import environment.XMLScenarioManager;
import environment.geography.Agent;
import environment.geography.AgentLine;
import environment.geography.Goals;
import environment.geography.Obstacle;
import environment.geography.SimulationScenario;
import agent.latticegas.LatticeSpace;
import app.PropertySet.Model;
import app.dataTracking.DataTracker;
import app.dataTracking.PhysicaDataTracker;
import app.dataTracking.dataTrackPBM_binary;
import com.google.common.collect.HashMultimap;
import ec.util.MersenneTwisterFast;
import environment.geography.AgentGroup;
import environment.geography.RoadMapPoint;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.xml.bind.JAXBException;
import motionPlanners.socialforce.SocialForce;
import sim.engine.RandomSequence;
import sim.engine.Schedule;
import sim.engine.Sequence;
import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.util.Bag;

/**
 * RVOModel
 *
 * @author michaellees
 * Created: Nov 24, 2010
 *
 * Copyright michaellees
 *
 * Description:
 *
 * The RVOModel is the core of the program. It contains the agentList,
 * the obstacleList and the rvoSpace and also the different parameters of the
 * MODEL itself. It also contains the main().
 *
 */
public class RVOModel extends SimState {

    private int worldXSize = PropertySet.WORLDXSIZE;
    private int worldYSize = PropertySet.WORLDYSIZE;
    private double gridSize = PropertySet.GRIDSIZE;
    private RVOSpace rvoSpace;
    private LatticeSpace latticeSpace;
    /**
     * Actually initialised to ArrayList as can be seen in second constructor, 
     * But using List type here so that we can change the implementation later 
     * if needed
     */
    private List<RVOAgent> agentList;
    private List<AgentGenerator> agentLineList;
    private Stoppable generatorStopper;
    private DataTracker dataTracker = null;
    private String name;
    public static RVOModel publicInstance = null;
    
//    //for different pbm scenarios to set initial preferredVelocity
//    private int pbmScenario = 0;
//    
//    public void setPbmSecenario(int pbmTestCase){
//        pbmScenario = pbmTestCase;
//    }
//    public int getPbmScenario(){
//        return pbmScenario;
//    }

//    //the list to keep record of every agent's status in each timestep
//    //each record contains a list of status for each agent
//    //for each agent, there is a list to record all the necessary status (e.g., velocity, position etc)
//    public ArrayList<ArrayList<ArrayList>> records;
    public RVOModel() {
        this(PropertySet.SEED);

    }

    public RVOModel(long seed) {
        super(seed);
        publicInstance = this;
        if (PropertySet.INITIALISEFROMXML) {
            try {
                XMLScenarioManager settings = XMLScenarioManager.instance("environment.geography");
                SimulationScenario scenario = (SimulationScenario) settings.unmarshal(PropertySet.FILEPATH);
                RVOGui.scale = scenario.getScale();
                worldXSize = SocialForce.Xmax = RVOGui.checkSizeX = scenario.getXsize();
                worldYSize = SocialForce.Ymax = RVOGui.checkSizeY = scenario.getYsize();
            } catch (JAXBException ex) {
                Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        agentList = new ArrayList<RVOAgent>();
        //obstacleList = new ArrayList<RVOObstacle>();
        agentLineList = new ArrayList<AgentGenerator>();

    }

    @Override
    public void start() {

        super.start();
        // This function is equivalent to a reset. 
        //Need to readup a bit more to see if it is even necessary...
        setup();
        if (PropertySet.INITIALISEFROMXML) {
            initialiseFromXML();
        } else {
            buildSpace();
            createAgents();
        }
        if (PropertySet.TRACK_DATA) {
//            dataTracker = new CWDataCollector(this, agentList);
          if(PropertySet.MODEL==Model.PatternBasedMotion){
             dataTracker =  new dataTrackPBM_binary(this,agentList);
          }else{
             dataTracker = new PhysicaDataTracker(this, agentList); 
          }
//            dataTracker = new PhysicaDataTracker(this, agentList);
            schedule.scheduleRepeating(dataTracker, 4, 1.0);
        }
        schedule.scheduleRepeating(new WrapUp(this, agentList), 5, 1.0);

    }

    /**
     * Creates the continuous 2D space that will be used for the simulation. 
     * Creates an appropriate space for Clustering
     */
    private void buildSpace() {
        if (!PropertySet.USECLUSTERING) {
            rvoSpace = new RVOSpace(worldXSize, worldYSize, gridSize, this);
        } else {
            rvoSpace = new ClusteredSpace(worldXSize, worldYSize, gridSize, this);
        }
    }

    /**
     * To use RVO without using an xml to initialise layout. Arranges agents in 
     * a pre decided order
     */
    private void createAgents() {
        int numAgentsPerSide = 5;
        double gap = 1.5f;
        for (int i = 1; i < numAgentsPerSide + 1; i++) {
            addNewAgent(
                    new RVOAgent(new Point2d(2, i * gap + 0.5),
                    new Point2d(8, i * gap + 0.5),
                    rvoSpace,
                    new Color(Color.HSBtoRGB((float) i / (float) numAgentsPerSide, 1.0f, 0.68f))));
            addNewAgent(
                    new RVOAgent(new Point2d(8, i * gap + 0.5),
                    new Point2d(2, i * gap + 0.5),
                    rvoSpace,
                    new Color(Color.HSBtoRGB(0.7f - (float) i / (float) numAgentsPerSide, 1.0f, 0.68f))));
        }
        this.scheduleAgents();
//        this.addNewAgent(new RVOAgent( new Point2d(8,3.5), new Point2d(2,3.5)
//        , this.rvoSpace, new Color(Color.HSBtoRGB(1.0f, 1.0f, 0.68f))));

    }

    /**
     * resets all the values to the initial values. this is just to be safe.
     */
    public void setup() {

        rvoSpace = null;
        agentList = new ArrayList<RVOAgent>();
        //obstacleList = new ArrayList<RVOObstacle>();
        agentLineList = new ArrayList<AgentGenerator>();
        RVOAgent.agentCount = 0;

    }

    public void scheduleAgents() {
        List<SenseThink> senseThinkAgents = new ArrayList<SenseThink>();

        List<Act> actAgents = new ArrayList<Act>();
        for (RVOAgent agent : agentList) {
            senseThinkAgents.add(agent.getSenseThink());
            actAgents.add(agent.getAct());
        }

//        senseThinkStoppable = mySpace.getRvoModel().schedule.scheduleRepeating(senseThinkAgent, 2, 1.0);
//        actStoppable = mySpace.getRvoModel().schedule.scheduleRepeating(actAgent, 3, 1.0);
//        (new RVOAgent(this.rvoSpace)).scheduleAgent();
        schedule.scheduleRepeating(Schedule.EPOCH, 1, new RandomSequence(senseThinkAgents.toArray(new SenseThink[]{})), 1.0);
        schedule.scheduleRepeating(Schedule.EPOCH, 2, new Sequence(actAgents.toArray(new Act[]{})), 1.0);
    }

    public List<RVOAgent> getAgentList() {
        return agentList;
    }

    public int getWorldYSize() {
        return worldYSize;
    }

    public int getWorldXSize() {
        return worldXSize;
    }

    public double getGridSize() {
        return gridSize;
    }

    public RVOSpace getRvoSpace() {
        return rvoSpace;
    }

    public LatticeSpace getLatticeSpace() {
        return latticeSpace;
    }

    public Stoppable getGeneratorStoppable() {
        return generatorStopper;
    }

    private void addNewObstacle(RVOObstacle obstacle) {
        //obstacleList.add(obstacle);
        rvoSpace.addNewObstacle(obstacle);
    }

    public void addNewAgent(RVOAgent a) {
        a.createSteppables();
        agentList.add(a);
        rvoSpace.updatePositionOnMap(a, a.getX(), a.getY());
        if (PropertySet.LATTICEMODEL) {
            latticeSpace.addAgentAt(a.getX(), a.getY(), a.getId());
        }
    }

    private void addNewAgentLine(AgentGenerator tempAgentLine, int frequency) {
        agentLineList.add(tempAgentLine);
        generatorStopper = schedule.scheduleRepeating(tempAgentLine, 1, (double) frequency);
    }

    /**
     * Initialize data from the XML file specified
     */
    private void initialiseFromXML() {
        try {
            XMLScenarioManager settings = XMLScenarioManager.instance("environment.geography");
            SimulationScenario scenario = (SimulationScenario) settings.unmarshal(PropertySet.FILEPATH);
            this.name = scenario.getName();
            double averageSpeed = 0.0;
            if (!PropertySet.USECLUSTERING) {
                rvoSpace = new RVOSpace(worldXSize, worldYSize, gridSize, this);
            } else {
                rvoSpace = new ClusteredSpace(worldXSize, worldYSize, gridSize, this);
                ((ClusteredSpace) rvoSpace).scheduleClustering();
            }

            if (PropertySet.LATTICEMODEL) {
                latticeSpace = new LatticeSpace(worldXSize, worldYSize, this);
                latticeSpace.scheduleLattice();
                latticeSpace.setDirection(scenario.getDirection());
            }

            List<Agent> xmlAgentList = scenario.getCrowd();
            for (int i = 0; i < xmlAgentList.size(); i++) {
                Agent tempAgent = xmlAgentList.get(i);

                //@hunan: added in a new RVOAgent constructor to set the necessary parameters for PBM use only
                if (PropertySet.MODEL == Model.PatternBasedMotion) {
                    Vector2d prefDir =  new Vector2d(tempAgent.getPrefDirectionX(),tempAgent.getPrefDirectionY());
                    if(prefDir!=null)
                        prefDir.normalize();
                    int agentColor = 0;
                    if(tempAgent.getAgentColor()>0){
                        agentColor = tempAgent.getAgentColor();
//                        System.out.println("color of agent: "+agentColor);
                    }//if not specified in xml, then default as 0, which results in black
                    
                    
                    RVOAgent tempRVOAgent = new RVOAgent(
                            new Point2d(tempAgent.getPosition().getX(), tempAgent.getPosition().getY()),
                            new Point2d(tempAgent.getGoal().getX(), tempAgent.getGoal().getY()),
                            prefDir,
                            rvoSpace, 
//                            new Color((float)((float)agentColor / 4),(float)((float)agentColor/4),(float)((float)agentColor/4)),    //has problem here, need to change
                             new Color(Color.HSBtoRGB((float) agentColor / (float)4, 1.0f, 0.68f)),
//                            Color.BLACK,
                            tempAgent.getPreferedSpeed(), tempAgent.getCommitmentLevel());
                    addNewAgent(tempRVOAgent);
                } else {
                    RVOAgent tempRVOAgent = new RVOAgent(
                            new Point2d(tempAgent.getPosition().getX(), tempAgent.getPosition().getY()),
                            new Point2d(tempAgent.getGoal().getX(), tempAgent.getGoal().getY()),
                            rvoSpace,
                            Color.black);
//                    if (tempRVOAgent.getId() == 0) {
//                        tempRVOAgent.setColor(Color.BLACK);
//                    }

                    tempRVOAgent.setPreferredSpeed(tempAgent.getPreferedSpeed());
                    tempRVOAgent.setMaximumSpeed(tempAgent.getPreferedSpeed() * 2.0);

                    addNewAgent(tempRVOAgent);
                }
            }

            List<Goals> xmlGoalList = scenario.getEnvironmentGoals();
            if (PropertySet.LATTICEMODEL) {
                if (xmlGoalList.size() > 0) {
                    for (Goals tempGoal : xmlGoalList) {
                        latticeSpace.addGoal(tempGoal);
                    }
                }
            }

            List<Obstacle> xmlObstacleList = scenario.getObstacles();
            for (int i = 0; i < xmlObstacleList.size(); i++) {
                Obstacle tempObst = xmlObstacleList.get(i);
                RVOObstacle tempRvoObst = new RVOObstacle(tempObst);
                addNewObstacle(tempRvoObst);
                if (PropertySet.LATTICEMODEL) {
                    latticeSpace.addObstacle(tempObst);
                }
            }
            if (PropertySet.MODEL == Model.SocialForce) {
                SocialForce.initializeObstacleSet(xmlObstacleList);
            }


            List<RoadMapPoint> xmlRoadMap = scenario.getRoadMap();
            HashMultimap<Integer, Point2d> actualRoadMap = null;
            if (!xmlRoadMap.isEmpty()) {
                actualRoadMap = HashMultimap.create();
                for (RoadMapPoint point : xmlRoadMap) {
                    actualRoadMap.put(point.getNumber(), new Point2d(point.getPosition().getX(), point.getPosition().getY()));

                }
//                rvoSpace.addRoadMap(actualRoadMap);
            }

            //this is used to generate a set of agents in a line
            List<AgentLine> xmlAgentLineList = scenario.getGenerationLines();
            for (int i = 0; i < xmlAgentLineList.size(); i++) {
                Point2d start = new Point2d(xmlAgentLineList.get(i).getStartPoint().getX(), xmlAgentLineList.get(i).getStartPoint().getY());
                Point2d end = new Point2d(xmlAgentLineList.get(i).getEndPoint().getX(), xmlAgentLineList.get(i).getEndPoint().getY());
                AgentGenerator tempAgentLine = new AgentGenerator(start, end, scenario.getGenerationLines().get(i).getNumber(), scenario.getGenerationLines().get(i).getDirection(), scenario.getEnvironmentGoals(), this);
                addNewAgentLine(tempAgentLine, scenario.getGenerationLines().get(i).getFrequency());
            }

            //group is useful in large crowd scenarios
            List<AgentGroup> xmlAgentGroupList = scenario.getAgentGroups();
            int counter = 0; //for error tracking purpose only
            for (AgentGroup tempAgentGroup : xmlAgentGroupList) {
                counter ++;
                Color initialGrpColor = new Color(Color.HSBtoRGB((float) counter / (float) xmlAgentGroupList.size(),1.0f, 0.68f));
                
                
                final double maxSpeed = tempAgentGroup.getMaxSpeed();
                final double minSpeed = tempAgentGroup.getMinSpeed();
                final double meanSpeed = tempAgentGroup.getMeanSpeed();
                averageSpeed += meanSpeed;
                final double sdevSpeed = tempAgentGroup.getSDevSpeed();
//                int[][] spaces = initializeLattice(tempAgentGroup.getStartPoint().getX(), tempAgentGroup.getStartPoint().getY(),
//                        tempAgentGroup.getEndPoint().getX(), tempAgentGroup.getEndPoint().getY());
                
                Vector2d groupDirection = new Vector2d(tempAgentGroup.getGroupDirectionX(),tempAgentGroup.getGroupDirectionY());//normalized vector to specify the group direction
                groupDirection.normalize();
//                
                Point2d grpleftUpperX = new Point2d(tempAgentGroup.getStartPoint().getX(),tempAgentGroup.getStartPoint().getY());
                Point2d grpRightLowerX = new Point2d(tempAgentGroup.getEndPoint().getX(),tempAgentGroup.getEndPoint().getY());
                double xLength = grpRightLowerX.x - grpleftUpperX.x;
                double yLength = grpRightLowerX.y - grpleftUpperX.y;
                
                int numSlotsOnX = (int)Math.floor(xLength / (2* RVOAgent.RADIUS * (1+ tempAgentGroup.getMaxDensityFactor()* 3))); //maxDensity factor is the global personal space factor, predefined
                int numSlotsOnY = (int)Math.floor(yLength / (2* RVOAgent.RADIUS * (1+ tempAgentGroup.getMaxDensityFactor()))); 
                
                System.out.println("Group "+counter+" can have "+numSlotsOnX*numSlotsOnY+" agents maximum");
                
                if(tempAgentGroup.getSize()> numSlotsOnX * numSlotsOnY){
                    System.err.println("too many agents defined in Group "+ counter);
                    return;
                }
                for (int i = 0; i < tempAgentGroup.getSize(); i++) {
//                    Point2d position = this.getRandomPosition(rvoSpace, grpleftUpperX, RVOAgent.RADIUS * (1+ tempAgentGroup.getMaxDensityFactor()), numSlotsOnX, numSlotsOnY); //get a random position within the group range, the function will check whether too crowded for the existing range
                    
                    Point2d position = new Point2d(grpleftUpperX.x + RVOAgent.RADIUS * (1+ tempAgentGroup.getMaxDensityFactor()) * (1+ 2 * (i/numSlotsOnY)), 
                            grpleftUpperX.y + RVOAgent.RADIUS * (1+ tempAgentGroup.getMaxDensityFactor()) *(1+ 2*(i- (i/numSlotsOnY)*numSlotsOnY-1)));
                    
                    double initialSpeed = random.nextGaussian() * sdevSpeed + meanSpeed;
                    if (initialSpeed < minSpeed) {
                        initialSpeed = minSpeed;
                    } else if (initialSpeed > maxSpeed) {
                        initialSpeed = maxSpeed;
                    }
                    
                    int initialCommitmentLevel = 1+random.nextInt(3);  //later, during sensitivity analysis, we can make different profile of crowd with different percentage for this parameter
                    
                   
                    
                    //@hunan: added in a new RVOAgent constructor to set the necessary parameters for PBM use only
                    if (PropertySet.MODEL == Model.PatternBasedMotion) {
                        RVOAgent tempRVOAgent = new RVOAgent(
                                new Point2d(position),
                                null, //goal
                                groupDirection,
                                rvoSpace, 
    //                            new Color(Color.HSBtoRGB((float) i / (float) xmlAgentList.size(),1.0f, 0.68f)),
//                                Color.BLACK,
                                initialGrpColor,
                                initialSpeed, initialCommitmentLevel);
                        addNewAgent(tempRVOAgent);
                    } else {
                        RVOAgent tempRVOAgent = new RVOAgent(
                                new Point2d(position),
                                groupDirection,
                                rvoSpace,
//                                Color.black
                                initialGrpColor
                                );
    //                    if (tempRVOAgent.getId() == 0) {
    //                        tempRVOAgent.setColor(Color.BLACK);
    //                    }

                        tempRVOAgent.setPreferredSpeed(initialSpeed);
                        tempRVOAgent.setMaximumSpeed(initialSpeed * 2.0);

                        addNewAgent(tempRVOAgent);
                    }
                    
            /*
                    RVOAgent agent = new RVOAgent(this.getRvoSpace());
                    Point2d position = this.getAgentPosition(tempAgentGroup.getStartPoint().getX(), tempAgentGroup.getStartPoint().getY(),
                            spaces);
                    agent.setCurrentPosition(position.x, position.y);

                    double initialSpeed = random.nextGaussian() * sdevSpeed + meanSpeed;
                    if (initialSpeed < minSpeed) {
                        initialSpeed = minSpeed;
                    } else if (initialSpeed > maxSpeed) {
                        initialSpeed = maxSpeed;
                    }

                    agent.setPreferredSpeed(initialSpeed);
                    agent.setMaximumSpeed(maxSpeed);
                    
                   agent.setPrefDirection(groupDirection);  //to set prefDirection of individual agent accoridng to its group prefDirection
                   agent.setPrefVelocity();
                   agent.setVelocity(agent.getPrefVelocity());
                   
                    this.addNewAgent(agent);
                    if (actualRoadMap != null) {
                        agent.addRoadMap(actualRoadMap);
                    }
               */

//                    agent.setPrefVelocity(); //set prefVel according to prefDirection
//                    groupDirection.scale(initialSpeed);
//                    agent.setVelocity(groupDirection);
                }
            }
            if (PropertySet.LATTICEMODEL) {
                latticeSpace.setSpeed(averageSpeed);
            }
        } catch (JAXBException ex) {
            Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.scheduleAgents();
    }

    DataTracker getDataTracker() {
        return this.dataTracker;
    }

    @Override
    public void finish() {
        System.out.println("wrapping up");
        if (dataTracker != null) {
            dataTracker.storeToFile();
            dataTracker = null;
        }
    }

    private Point2d getAgentPosition(double mnx, double mny, int[][] spaces) {
        // determine the mass of the agent;
        int x = 0, y = 0;
 
        while (true) {
            x = this.random.nextInt(spaces.length);
            y = this.random.nextInt(spaces[0].length);
//            pos = new Point2d(x, y);
            if (spaces[x][y] == 0) {
                spaces[x][y] = 1;
                break;
            }
            // make sure agents are not created on top of each other
//            for (RVOAgent agent : this.getAgentList()) {
//                double dx = x - agent.getCurrentPosition().getX();
//                double dy = y - agent.getCurrentPosition().getY();
//                double d = Math.hypot(dx, dy);

//
//                double minDist = (agent.getRadius() * 2 + RVOAgent.RADIUS) / 2.0;
//
//                // if d<=minDist then the agents are 'overlapping'...
//                if (d <= minDist) {
//                    pos = null;
//                    break;
//                }
//            }
        }


        return new Point2d(mnx + (x * RVOAgent.RADIUS * 2) + RVOAgent.RADIUS, mny + (y * RVOAgent.RADIUS * 2) + RVOAgent.RADIUS);

    }
    
    private Point2d getRandomPosition(RVOSpace rvospace, Point2d leftUpperPoint, double circleSpaceRadius, int xSlots, int ySlots){
        int randX = random.nextInt(xSlots);
        int randY = random.nextInt(ySlots);
        Point2d randPosition = new Point2d(leftUpperPoint.x + circleSpaceRadius * (1+randX),leftUpperPoint.y + circleSpaceRadius*(1+randY));
        Bag positionAvailability = rvospace.findNeighbours(randPosition,circleSpaceRadius);
        
        if(positionAvailability.isEmpty()){
            return randPosition;
        }
        return getRandomPosition(rvospace, leftUpperPoint, circleSpaceRadius, xSlots, ySlots);  //recursion here????!!!!!!!!!!!!!!!!!!!
    }

    public static void main(String[] args) {
        // Read tutorial 2 of mason to see what this does.. or refer to documentation of this function
        String filePath="";
        for(int i=0;i<args.length;i++){
            if(args[i].equalsIgnoreCase("-fileName")){
                filePath=args[i+1];
            }
        }
        
        PropertySet.initializeProperties(filePath);

        doLoop(RVOModel.class, args);

        System.exit(0);
    }

    public String getScenarioName() {
        return name;
    }

    private int[][] initializeLattice(Double startx, Double starty, Double endx, Double endy) {
        int sizeX = (int) Math.floor((endx - startx) / (RVOAgent.RADIUS * 2));
        int sizeY = (int) Math.floor((endy - starty) / (RVOAgent.RADIUS * 2));
        return new int[sizeX][sizeY];

    }
}
