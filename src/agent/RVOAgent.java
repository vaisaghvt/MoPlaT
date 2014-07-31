package agent;

/**
 *
 *
 * @author michaellees Created: Nov 29, 2010
 *
 *
 *
 * Description:This class describes the agents themselves, except for the
 * portrayal components all the internal characteristics of the agents are
 * stored here.
 *
 */
import app.PropertySet;
import com.google.common.collect.HashMultimap;
import device.Device;
import environment.RVOSpace;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import motionPlanners.VelocityCalculator;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.LocationWrapper;
import sim.util.Bag;
import sim.util.Proxiable;
import utility.PrecisePoint;

public class RVOAgent extends AgentPortrayal implements Proxiable {

    //static constants
    // public static final double MAXACCEL = 260.0f; //m/s*s
    public static double INFO_LIMIT; // Chunks!
//    public static final double INFO_LIMIT = Double.MAX_VALUE; // Chunks!
    public static double RADIUS;
    public static double DEFAULT_PREFERRED_SPEED;
    public static int SENSOR_RANGE; //sensor range in proportion to agent radius
    public static int agentCount = 0; // number of agents
    protected int id;
    int currentGoal = 0;
    /**
     * This is used in the time to collision calculation to scale the radius of
     * self This factor is multiplied by the radius of the agent and added to
     * the original radius when calculating time to collision.
     *
     * i.e., an agent will consider itself to have a radius (r +
     * personalSpaceFactor*r)
     *
     * This means agents will estimate collisions (i.e., non infinite time to
     * collision) for a circle bigger than their actual size.
     *
     * If this is zero then the standard RVO calculation for time to collision
     * is performed
     */
    private double personalSpaceFactor = 0.2;
    /**
     * Current position of the agent from javax.vecmath
     */
    private int listenToDevice =6;
    protected PrecisePoint currentPosition;
    protected double mass = 70; // in KG
    /**
     * Current velocity of the agent
     */
    protected PrecisePoint velocity;
    private PrecisePoint chosenVelocity;
    /**
     * Agents preferred velocity, calculated each timestep according to goal and
     * current location
     */
    protected Vector2d prefVelocity;
    /**
     * Sets whether to display the agent's velocity on the map or not
     */
    protected double preferredSpeed;
    protected double maxSpeed;
    protected Device mydevice;
    /**
     * Intermediate goal destination of agent
     */
    protected Point2d goal;
    /**
     * Environmental space of the agents, contains multiple MASON fields
     */
    protected RVOSpace mySpace;
    /**
     * The motion planning system used by the agent, this can used any method
     * for motion planning that implements the VelocityCalculator interface
     */
    protected VelocityCalculator velocityCalc;
    protected Stoppable senseThinkStoppable;
    protected Stoppable actStoppable;
    private SenseThink senseThinkAgent;
    private Act actAgent;
    private boolean dead = false;
    private HashMultimap<Integer, Point2d> roadMap;
    private Point2d currentRoadMapPoint = null;
//    private PrecisePoint prevGoal;
    private int currentPriority = -1;
    private boolean isTrustingDevice=true;

    /**
     * Used by RVOModel to create an agent with just the space initialized.
     *
     * To be called by all other constructors for RVOAgent.
     *
     * @param mySpace
     */
    public RVOAgent(RVOSpace mySpace) {
        super(); //for portraying the trails on the agentportrayal layer
        this.mySpace = mySpace;
//        currentPosition = new PrecisePoint();
//        goal = new Point2d();

        //DEFAULT_PREFERRED_SPEED is the default value specified in 1.xml
        //the parameter value of preferredSpeed should be only set to this default value when it is not set with the value from the xml initialization file
        if (preferredSpeed == 0) {
            preferredSpeed = RVOAgent.DEFAULT_PREFERRED_SPEED;
        }

        Object[] args = new Object[0];              // Change these two if you need to pass arguments to your motion plannign constructor
        Class<?>[] classes = new Class<?>[0];      // Change these two if you want to pass arguments to your motion planning constructor
        try {

            velocityCalc = (VelocityCalculator) Class.forName(PropertySet.MODEL.getAssociatedClass()).getConstructor(classes).newInstance(args);

        } catch (NoSuchMethodException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RVOAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        id = agentCount++;
        
        if(mySpace.getRvoModel().random.nextDouble()  < Device.DEVICE_HOLDING_PROBABILITY){
            setDevice(mySpace); //assigns a device to agents in mySpace
            if(mySpace.getRvoModel().random.nextDouble() < Device.DEVICE_TRUST){
                this.isTrustingDevice = true;
            }else{
                this.isTrustingDevice = false;
            }
        }
        
    }

    public RVOAgent(Point2d myLocation,Point2d goal,RVOSpace rvoSpace){
        this(rvoSpace);
        this.setCurrentPosition(myLocation.x, myLocation.y);
        this.setGoal(goal);
        
    }
    /**
     * Not used. Maybe used in clustered agents. Created by VT
     *
     * @param otherAgent
     */
    @Deprecated
    public RVOAgent(RVOAgent otherAgent) {
        this(otherAgent.getMySpace());
        preferredSpeed = otherAgent.getPreferredSpeed();
        maxSpeed = preferredSpeed * 1.5;
//        currentPosition = new PrecisePoint();
        this.setCurrentPosition(otherAgent.getCurrentPosition().getX(), otherAgent.getCurrentPosition().getY());
        /*
         if (this.hasDevice()) {
         this.getDevice().setCurrentPosition(currentPosition.toPoint());
         }
         * */
        goal = new Point2d(otherAgent.getGoal().getX(), otherAgent.getGoal().getY());
        mySpace = otherAgent.mySpace;
        mydevice = otherAgent.mydevice; // added for device
        id = otherAgent.getId();
        agentCount--;
    }

    /**
     * Get's the final point from the roadmap if roadmap is set. Otherwise
     * returns the goal variable that might have been set.
     *
     * @return
     */
    public Point2d getGoal() {
        if (this.roadMap == null) {
            return goal;
        } else {
            return this.getFinalGoal();
        }
    }

    public void setGoal(Point2d goal) {
        this.goal = goal;
        //  findPrefVelocity();
        //  velocity = new Vector2d(this.prefVelocity);
    }

    public VelocityCalculator getVelocityCalculator() {
        return velocityCalc;
    }

    // check if agent has a device
    public boolean hasDevice() {
        return (mydevice != null);
    }

    public Device getDevice() {
        return mydevice;
    }

    private void setDevice(RVOSpace mySpace) {
        mydevice = new Device(mySpace);
        mydevice.setAttachedAgent(this); //set device to the agent
    }

    private boolean reachedGoal() {
return false;
//        if (goal != null) {
//
//            return (currentPosition.toPoint().distance(goal) < RADIUS);
//        } else {
//            return false;
//        }
    }

    public Point2d getCurrentPosition() {
        return currentPosition.toPoint();
    }

    /*
     * returns the position at the face (edge of the circle) along the velocity direction
     * redundant with getMyPositionAtEye
     */
//    public Point2d getCurrentEyePosition(){
//        PrecisePoint predictPos = new PrecisePoint(this.getVelocity().getX(), this.getVelocity().getY());
//        predictPos.scale(RADIUS);
//        predictPos.add(this.getCurrentPosition());
//        return predictPos.toPoint();
//    }
    public double getX() {
        return currentPosition.getX();
    }

    public double getY() {
        return currentPosition.getY();
    }

    //everytime you set position of the agent, if agent has device, it will also set the position of the device
    final public void setCurrentPosition(double x, double y) {
        currentPosition = new PrecisePoint(x, y);
        if (this.hasDevice()) {
            this.getDevice().setCurrentPosition(currentPosition.toPoint());
        }
    }

    public Vector2d getVelocity() {
        if (velocity == null) {
            velocity = new PrecisePoint();
        }
//        return velocity.toVector(); //the error comes from here, 1.3,0 gives vector value of 1,0
//        System.out.println(velocity);
        return new Vector2d(velocity.getX(), velocity.getY());
    }

    public void setVelocity(Vector2d vel) {
        velocity = new PrecisePoint(vel.getX(), vel.getY());
    }

    public double getSpeed() {
        return getVelocity().length();
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }
//    public RVOAgent getAgent(int id){
//        if(this.id==id) return this;
//        return null;
//    }
//

    /**
     * Sets and returns the prefered velocity. Generally this is just the
     * velocity towards goal. But in the evacTest scenario, this is set to the
     * checkpoint nearby
     *
     * @return new Preferred Velocity
     */
    public final void setPrefVelocity() {
        if (this.hasRoadMap()) {
            prefVelocity = this.determinePrefVelocity();
            assert !Double.isNaN(prefVelocity.x);
            prefVelocity.scale(preferredSpeed);
        } else if (this.goal != null) {
            //no preferredDirection
            prefVelocity = new Vector2d(goal);
            prefVelocity.sub(currentPosition.toPoint());
            prefVelocity.normalize();
            double distanceToGoal = currentPosition.toPoint().distance(goal);
            if(distanceToGoal < preferredSpeed) {
                prefVelocity.scale(distanceToGoal);
            }
            else {
                prefVelocity.scale(preferredSpeed);
            } //@hunan:added the scale for perferredSpeed
        } //according to preferredDirection
//        else {
//            prefVelocity = new Vector2d(prefDirection);
//            prefVelocity.normalize();
//            prefVelocity.scale(preferredSpeed);
////            //assumes the bi-directional scenario, where preferred velocity is determined by the direction only. Rather than a precise waypoint
////            if(PropertySet.MODEL==PropertySet.Model.PatternBasedMotion){
//                 //this is only for horizontal directional move!
//                if(PropertySet.PBMSCENARIO == 1){
//                    if(prefVelocity.x>=0){
//                       prefVelocity = new Vector2d(preferredSpeed,0);
//                   }else{
//                       prefVelocity = new Vector2d(-preferredSpeed,0);
//                   }
//                }
//                //for crossing scenario
//                else if(PropertySet.PBMSCENARIO ==2){
//
//                }
//            }
//        }
//        return prefVelocity;
    }

    public Vector2d getPrefVelocity() {
        return prefVelocity;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getPersonalSpaceFactor() {
        return personalSpaceFactor;
    }

    public RVOSpace getMySpace() {
        return mySpace;
    }

    /**
     * Returns the predicted position i simulation steps in the future based on
     * linear dead reckoning.
     *
     * @last modified by: hunan　at Dec 20th, 2011
     */
    public Point2d getNextPosition(int i) {
        PrecisePoint predictPos = new PrecisePoint(this.getVelocity().getX(), this.getVelocity().getY());
//        Vector2d predictPos =new Vector2d(this.getVelocity().getX(),this.getVelocity().getY());
        predictPos.scale(i * PropertySet.TIMESTEP);
        predictPos.add(this.getCurrentPosition());
        return predictPos.toPoint();
    }

    public Point2d getMyPositionAtEye() {
        Vector2d myVel = getVelocity();
        myVel.normalize();
        PrecisePoint myPos = new PrecisePoint(myVel.x, myVel.y);
//        Vector2d predictPos =new Vector2d(this.getVelocity().getX(),this.getVelocity().getY());
        myPos.scale(RADIUS);
        myPos.add(this.getCurrentPosition());
        return myPos.toPoint();
    }

    public int getId() {
        return id;
    }

    public void setPreferredSpeed(double preferredSpeed) {
        this.preferredSpeed = preferredSpeed;
    }

    @Override
    public String getName(LocationWrapper wrapper) {
        return "Agent " + id;
    }

    @Override
    public String toString() {
        return "Agent" + id;
    }

    public double getPreferredSpeed() {
        return preferredSpeed;
    }

    @Override
    public Object propertiesProxy() {
        return new MyProxy();
    }

    public SenseThink getSenseThink() {
        return this.senseThinkAgent;
    }

    public Act getAct() {
        return this.actAgent;
    }

    public void createSteppables() {
        this.senseThinkAgent = new SenseThink();
        this.actAgent = new Act();

    }

    public void setMaximumSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public RVOAgent getAgentWithId(Bag sensedNei, int id) {
        for (Object o : sensedNei) {
            RVOAgent agent = (RVOAgent) o;
            if (agent.getId() == id) {
                return agent;
            }
        }
        return null;
    }

    private Vector2d findVelocityFromRoadMap(Point2d localCurrentGoal) {
        currentRoadMapPoint = new Point2d(localCurrentGoal);
        PrecisePoint cleanCurrentGoal = new PrecisePoint(localCurrentGoal.getX(), localCurrentGoal.getY());
        Vector2d result = new Vector2d(cleanCurrentGoal.toVector());
        result.sub(this.getCurrentPosition());
        if (result.length() != 0) {
            result.normalize();
        }
        return result;
    }

    private boolean isTrustingDevice() {
        return isTrustingDevice;
    }

    public class SenseThink implements Steppable {

        @Override
        public void step(SimState ss) {
            if (!dead) {
                if (reachedGoal()) {
                    RVOAgent.this.dead = true;
                    return;
                }
//                chosenVelocity = new PrecisePoint(prefVelocity.getX(), prefVelocity.getY());

                Bag sensedNeighbours = mySpace.senseNeighbours(RVOAgent.this);

                if (PropertySet.INFOPROCESSING) {
                    /**
                     * Here we process the neighbour list that was passed to it
                     * to determine collisions
                     */
                    List<RVOAgent> sortedList = new ArrayList<RVOAgent>();
                    determineInitialLists(sortedList, sensedNeighbours);
                    sensedNeighbours.clear();
                    sensedNeighbours.addAll(sortedList);

                }

                //updated on 30th September 2013
                if (hasDevice() && Device.FORBIDDENAREA_APPROACH) {
                    setPrefVelocity();
                } else {//use the stop device approach

                    //the code commented out here is to implement no-move on device for about 10 tics
                    if (hasDevice() && getDevice().isStopped() ) {
                        if (getDevice().checkStillStopped()&& RVOAgent.this.isTrustingDevice()) {
                            prefVelocity = new Vector2d(0, 0);
//                            RVOAgent.this.setPreferredSpeed(0.);
                        } else {
                            setPrefVelocity();
                        }
                    } else {
                        setPrefVelocity();
                    }
                }

                //updated on Aug 20 2013
                /* if agent has device and is dense, set preferred velocity to 0 */

                /**
                 * Very slight perturbation of velocity to remove deadlock
                 * problems of perfect symmetry
                 */
                //updated on Aug 20 2013
                assert !Double.isNaN(prefVelocity.x);
                Vector2d tempVelocity = velocityCalc.calculateVelocity(
                        RVOAgent.this, sensedNeighbours,
                        mySpace.senseObstacles(RVOAgent.this),
                        prefVelocity, PropertySet.TIMESTEP);
                if (Double.isNaN(
                        tempVelocity.getX())) {
                    assert false;
                }
                chosenVelocity = new PrecisePoint(tempVelocity.getX(), tempVelocity.getY());

            }//end of if(!dead)
        }//end of step(ss)

        private void determineInitialLists(List<RVOAgent> sortedList, Bag sensedNeighbours) {
            List<Double> distanceScoreList = new ArrayList<Double>();
            for (Object temp : sensedNeighbours) {
                RVOAgent tempAgent = (RVOAgent) temp;
                Vector2d distanceVector = new Vector2d(tempAgent.getCurrentPosition());
                distanceVector.sub(RVOAgent.this.getCurrentPosition());

//                        double distance = distanceVector.length();
                double distanceScore = assignDistanceScore(distanceVector, tempAgent);
                boolean added = false;
                int j = 0;
//                        if (tempAgent.getCurrentPosition().getX() < 2.5) {
//                            System.out.println("test");
//                        }
                for (; j < distanceScoreList.size(); j++) {

                    if (tempAgent.equals( // don't sense the same element
                            RVOAgent.this)
                            || (tempAgent.getCurrentPosition().equals(
                            sortedList.get(j).getCurrentPosition())
                            && tempAgent.getRadius() == sortedList.get(j).getRadius())) {
                        added = true;
                        break;
                    }


                    if (distanceScoreList.get(j).compareTo(new Double(distanceScore)) < 0) { // this item is closer than exisiting
                        distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                        sortedList.add(j, tempAgent);
                        added = true;
                        break;
                    } else if (distanceScoreList.get(j).equals(distanceScore)) { // this item is closer than exisiting
                        if (tempAgent.getVelocity().dot(sortedList.get(j).getVelocity()) < 0) {
                            if (tempAgent.getVelocity().dot(RVOAgent.this.getVelocity()) < 0) {
                                // the currently sensed agent is moving in the opposite direction of my agent

                                distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                                sortedList.add(j, tempAgent);
                                added = true;
                                break;
                            } else {
                                continue;
                            }
                        }

                        Vector2d distanceCheck = new Vector2d(sortedList.get(j).getCurrentPosition());
                        distanceCheck.sub(RVOAgent.this.getCurrentPosition());
                        double sortedDistance = distanceCheck.length() - sortedList.get(j).getRadius();
                        double tempDistance = distanceVector.length() - tempAgent.getRadius();

                        if (tempDistance < sortedDistance) {

                            distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                            sortedList.add(j, tempAgent);
                            added = true;
                            break;
                        }
                    }
                }
                if (!added) {
                    distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                    sortedList.add(j, tempAgent);
                }
            }
            for (int j = 0; j < sortedList.size(); j++) {
                if (sortedList.get(j).getRadius() > 0.16) {
                    RVOAgent clusteredAgent = (RVOAgent) sortedList.get(j);
                    for (int k = 0; k < sortedList.size(); k++) {
                        if (j != k) {
                            RVOAgent tempAgent = (RVOAgent) sortedList.get(k);
                            Vector2d distanceCheck = new Vector2d(clusteredAgent.getCurrentPosition());
                            distanceCheck.sub(tempAgent.getCurrentPosition());
                            if (distanceCheck.length() + tempAgent.getRadius() <= clusteredAgent.getRadius()) {
                                sortedList.remove(k);
                                distanceScoreList.remove(k);
                                k--;
                            }
                        }
                    }
                }
            }
            double accumulatedScore = 0.0;
            int indexToBeDeleted = 0;
            for (; indexToBeDeleted < distanceScoreList.size(); indexToBeDeleted++) {
                accumulatedScore += distanceScoreList.get(indexToBeDeleted);
                if (accumulatedScore >= RVOAgent.INFO_LIMIT) {
                    break;
                }
            }
            for (int j = distanceScoreList.size() - 1; j > indexToBeDeleted; j--) {
                distanceScoreList.remove(indexToBeDeleted);
                sortedList.remove(j);
            }
        }

        private double assignDistanceScore(Vector2d distanceVector, RVOAgent tempAgent) {

            double distance = (distanceVector.length() - tempAgent.getRadius() - RVOAgent.RADIUS);
            if (distance < 0) {
                return 1.5; //1.5? interesting effect
            }
//            double distanceScore = RVOAgent.INFO_LIMIT- (RVOAgent.INFO_LIMIT/10.0)*Math.log((RVOAgent.INFO_LIMIT/10.0)*distanceVector.length());
            double distanceScore = Math.max(Math.min(1.0f, (Math.expm1(5.0f / distance) - 0.11)), 0.1);

            Vector2d angleFormedVector = new Vector2d(RVOAgent.this.getGoal());
            angleFormedVector.sub(RVOAgent.this.currentPosition.toPoint());
            double angleFormed = angleFormedVector.dot(distanceVector);
            double angleScore = 0.0f;
            if (angleFormed < 0) {
                angleScore = 0.1f;
            } else {
                angleFormed = angleFormed / (distanceVector.length() * angleFormedVector.length());
                angleFormed = Math.acos(angleFormed);


                if (angleFormed < Math.PI / 3.0f) {
                    angleScore = 1.0f;

                } else if (angleFormed < 4.0f * Math.PI / 9.0f) {
                    angleScore = (8.1f * (3 * angleFormed - Math.PI) / (Math.PI)) + 0.1f;
                } else {
                    angleScore = 0.1f;
                }
            }
//            if (RVOAgent.this.getId() == 0) {
//                System.out.println(tempAgent.getCurrentPosition().getX() + "," + angleFormed * 180.0f / Math.PI + " as= " + angleScore);
//                //    System.out.println(distanceScore+" ds= "+distanceScore);
//            }

//            return (distanceScore + angleScore) / 2.0f;
            return (distanceScore > 1.0 ? distanceScore : distanceScore * angleScore);
        }
    }

    /**
     * updates the actual position after calculation. The division of steps is
     * to ensure that all agents update their positions and move simultaneously.
     * Implementation of Removable step is to make sure agents die after exiting
     * the simulation area
     */
    public class Act implements Steppable {

        @Override
        public void step(SimState ss) {
            if (!dead) {

//            if (reachedGoal()) {
//                currentPosition = new Point2d(-4000, 4000);
//                goal = new Point2d(-4000, 4000);
//                //             currentGoal++;
//                actStoppable.stop();
//                return;
//            }
//                velocity = chosenVelocity; //TO VT: this u previously said need to use precise point, why changed back without justification here?

                /* 19th August 2013 : Moved to sensethink to compute preferred velocity instead
                 //agent dont move as it listens to the device
                 if (hasDevice() && getDevice().isStopped()) {
                 velocity = new PrecisePoint(0, 0);
                 getDevice().checkStillStopped();
                 } else {
                 */
                velocity = new PrecisePoint(
                        chosenVelocity.getX() + mySpace.getRvoModel().random.nextFloat() * utility.Geometry.EPSILON,
                        chosenVelocity.getY() + mySpace.getRvoModel().random.nextFloat() * utility.Geometry.EPSILON);
                //}
                double currentPosition_x = (currentPosition.getX()
                        + velocity.getX() * PropertySet.TIMESTEP);
                double currentPosition_y = (currentPosition.getY()
                        + velocity.getY() * PropertySet.TIMESTEP);
                
                setCurrentPosition(currentPosition_x, currentPosition_y);
                /*
                 if (hasDevice()) {
                 getDevice().setCurrentPosition(currentPosition.toPoint());
                 //getDevice().execute();
                 }
                 */
                getMySpace().updatePositionOnMap(RVOAgent.this, currentPosition_x,
                        currentPosition_y);
            }
        }
    }
    /*
     * for Display in the Property Window
     */

    public class MyProxy {

        public Vector2d getVelocity() {
            if (velocity != null) {
                return velocity.toVector();
            } else {
                return new Vector2d();
            }

        }

        public Vector2d getPrefVelocity() {
            return prefVelocity;
        }

        public Point2d getPosition() {
            return currentPosition.toPoint();
        }
//
//        public Point2d getGoal() {
//            return goal;
//        }
//
//        public Point2d getCurrentGoal() {
//            return currentGoalPoint;
//        }

//        public double getPreferredSpeed() {
//            return RVOAgent.this.preferredSpeed;
//        }
        public String getName() {
            return "Agent " + RVOAgent.this.getId();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RVOAgent other = (RVOAgent) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.id;
        return hash;
    }

    public void setRoadMap(HashMultimap<Integer, Point2d> actualRoadMap) {
        this.roadMap = actualRoadMap;
    }

    public Point2d getFinalGoal() {
        assert roadMap.get(roadMap.keySet().size() - 1).size() == 1;
        return roadMap.get(roadMap.keySet().size() - 1).iterator().next();
    }

    public boolean hasRoadMap() {
        return !(roadMap == null);
    }

    public Vector2d determinePrefVelocity() {
  
        Vector2d result = null;

        Vector2d agentUnitVelocity = new Vector2d(this.getVelocity());
        if (this.getVelocity().getX() != 0.0f
                || this.getVelocity().getY() != 0.0f) {
            agentUnitVelocity.normalize();
        }
        Point2d agentTopPosition = new Point2d();
        agentTopPosition.setX(this.getCurrentPosition().getX() - agentUnitVelocity.getY() * RVOAgent.RADIUS);
        agentTopPosition.setY(this.getCurrentPosition().getY() + agentUnitVelocity.getX() * RVOAgent.RADIUS);

        Point2d agentBottomPosition = new Point2d();
        agentBottomPosition.setX(this.getCurrentPosition().getX() + agentUnitVelocity.getY() * RVOAgent.RADIUS);
        agentBottomPosition.setY(this.getCurrentPosition().getY() - agentUnitVelocity.getX() * RVOAgent.RADIUS);

        Set<Point2d> ignoredPoints = new LinkedHashSet<Point2d> ();
        
        for (int localCurrentPriority = roadMap.keySet().size() - 1; 
                localCurrentPriority >= 0; localCurrentPriority--) {
            
            if (currentRoadMapPoint!=null && localCurrentPriority == currentPriority){ 
//                // for current priority level use the last used roadmap without 
//                // calculating min distance. The only reason to not take this 
//                // would be that this point is forbidden.
                if (mySpace.visibleFrom(currentRoadMapPoint, agentTopPosition)
                            && mySpace.visibleFrom(currentRoadMapPoint, agentBottomPosition)) {
                    if (listenToDevice>0 && this.hasDevice() && Device.FORBIDDENAREA_APPROACH 
                            && this.getDevice().hasForbiddenArea() 
                            && this.getDevice().inForbiddenArea(currentRoadMapPoint)) {
                        ignoredPoints.add(currentRoadMapPoint);
                        
                    }else{
                        result = findVelocityFromRoadMap(currentRoadMapPoint);
                        return result;
                    }
                }
//                
            }
            
            double minDistance = Double.MAX_VALUE;
            Point2d localCurrentGoal = null;
            for (Point2d tempCurrentGoal : roadMap.get(localCurrentPriority)) {
                if(ignoredPoints.contains(tempCurrentGoal)){
                    continue;
                }
                if (mySpace.visibleFrom(tempCurrentGoal, agentTopPosition)
                            && mySpace.visibleFrom(tempCurrentGoal, agentBottomPosition)) {
                    if (this.hasDevice() && Device.FORBIDDENAREA_APPROACH 
                            && this.getDevice().hasForbiddenArea() 
                            && this.getDevice().inForbiddenArea(tempCurrentGoal)) {
                        ignoredPoints.add(tempCurrentGoal);
                        continue;
                    }else{
                        if (this.getCurrentPosition().distance(tempCurrentGoal) < minDistance) {
                            minDistance = this.getCurrentPosition().distance(tempCurrentGoal);
                            localCurrentGoal = tempCurrentGoal;
                         
                        }
                    }
                }
            
            }

            if(!ignoredPoints.isEmpty()){
                listenToDevice--;
           
            }
            if (localCurrentGoal != null) {
                result = findVelocityFromRoadMap(localCurrentGoal);
                currentPriority = localCurrentPriority;
                
                break;
            }
            if (result != null) {
                assert !Double.isNaN(result.x);
            }
        }
        if (result == null) {
//            assert false;

            result = tryWeakTest();
          
//            result = new Vector2d();
            assert !Double.isNaN(result.x);
        }
//        System.out.println(result);
        assert !Double.isNaN(result.x);
        return result;
    }

    private Vector2d tryWeakTest() {
        Vector2d result = null;


        for (int i = roadMap.keySet().size() - 1; i >= 0; i--) {
            double minDistance = Double.MAX_VALUE;
            Point2d localCurrentGoal = null;
            for (Point2d tempCurrentGoal : roadMap.get(i)) {
                if (mySpace.visibleFrom(tempCurrentGoal, getCurrentPosition())) {
                    if (this.getCurrentPosition().distance(tempCurrentGoal) < minDistance) {
                        minDistance = this.getCurrentPosition().distance(tempCurrentGoal);
                        localCurrentGoal = tempCurrentGoal;
                    }
                }
            }

            if (localCurrentGoal != null) {
                result = findVelocityFromRoadMap(localCurrentGoal);
                currentPriority = i;
                break;
            }
        }
        if (result == null) {
//            assert false;

            result = new Vector2d();
        }
//        System.out.println(result);
        return result;
    }
}
