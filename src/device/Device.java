/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package device;

import agent.RVOAgent;
import environment.RVOSpace;
import java.util.HashMap;
import javax.vecmath.Point2d;
import sim.engine.*;
import sim.util.*;
import utility.PrecisePoint;

/**
 *
 * @author angela
 */
public class Device extends DevicePortrayal implements Proxiable {
// change to public static final for constants
//    private int range = 1;
//    private int threshold = 7; // 130 agents to consider dense
//    private int messageThreshold = 3; //received messages more than this amount, device is dense
//    private int maxHops = 2; // this is the max. hops to consider in dense map

    private boolean dense = false;
    private int deviceId;
    public static int deviceIdCount = 0;
    protected RVOSpace mySpace;
    protected RVOAgent attachedAgent; //device has an attached agent
    public static double RADIUS;
    public static int SENSOR_RANGE; //sensor range in proportion to device radius
    public static int DEVICE_THRESHOLD; // max threshold for number of devices around agent
    public static int DEVICE_MSG_THRESHOLD; // max message threshold received by device to determine if agent is dense
    public static int DEVICE_MAX_HOPS;  // no. of hops of message passing
    public static double DEVICE_MOVE_PROBABILITY;
    public static int STOP_LENGTH = 10; //number of ticks to stop for when too dense
    public static int deviceCount = 0; // no. of devices
    private HashMap<Integer, Integer> denseMap; //map of denseness of neighbours
    protected PrecisePoint currentPosition;
    protected Bag neighbours = new Bag();
    private SenseThinkDevice senseThinkDevice; //for steppable
    private ActDevice actDevice; //for steppable
    private int stopCounter = 0;
    private boolean[] FA = new boolean[4]; //tracks the forbidden zones in four quadrants
    public static boolean FORBIDDENAREA_APPROACH; //to put into xml definition file
    public static boolean MIN_DIST_TO_GOAL;

    public Device() {
        deviceId = deviceIdCount++;
        //System.out.println("device=" + deviceId + " " + deviceIdCount);
    }

    public Device(RVOSpace mySpace) {
        super();
        this.mySpace = mySpace;
        currentPosition = new PrecisePoint();
        denseMap = new HashMap<Integer, Integer>();
        deviceId = deviceIdCount++;
    }

    public int getDeviceId() {
        return deviceId;
    }

    // given a device, which agent is attached to it
    public void setAttachedAgent(RVOAgent thisAgent) {
        attachedAgent = thisAgent;
    }

    public RVOAgent getAttachedAgent() {
        return attachedAgent;
    }

    public boolean isDense() {
        return dense;
    }

    public Bag getNeighbours() {
        if (neighbours.isEmpty()) {
            neighbours = mySpace.findDeviceNeighbours(this.getCurrentPosition(), Device.SENSOR_RANGE);
        }
        return neighbours;
    }

    public void clearNeighbours() {
        neighbours.clear();
    }

    public void clearDenseMap() {
        denseMap.clear();
    }

    public void sendTooDense(int devId, int hops) {

        if (hops <= Device.DEVICE_MAX_HOPS) {   //within range, update dense map
            if (!denseMap.containsKey(devId)) {
                denseMap.put(devId, hops);
            } else {
                if (hops < denseMap.get(devId)) {
                    denseMap.put(devId, hops); //does this to replace the ID, hops key-value pair??
                }

            }

            for (Object neighbour : getNeighbours()) {
                Device dev = (Device) neighbour;
                if (dev.getDeviceId() != devId) {   // added to prevent sending to itself
                    dev.sendTooDense(devId, hops + 1);
                }
            }

        }
        if ((neighbours.size() > Device.DEVICE_THRESHOLD || denseMap.size() > Device.DEVICE_MSG_THRESHOLD) && mySpace.getRvoModel().random.nextDouble() < Device.DEVICE_MOVE_PROBABILITY) {    
            dense = true;
        } else {
            dense = false;
        }
    }

    /* This method is called in device steppable */
    public void execute() {
        this.clearNeighbours();
//        minFA = 0.0;
//        maxFA = 0.0;
//        hasForbiddenArea = false;

        //original device that sends to neighbours that it is in the dense situation
        neighbours = this.getNeighbours();
        if (neighbours.size() > Device.DEVICE_THRESHOLD) {
            sendTooDense(this.getDeviceId(), 1);
        } else {
            dense = false;
        }
        
        //edited on 30th September 2013
        if (FORBIDDENAREA_APPROACH) {
            getForbiddenArea();
        } else {//use the stop device approach
            if (dense && !isStopped()) {
                System.out.println("start stop executed!");
                setStopped();
            }
        }


    }

    public SenseThinkDevice getSenseThinkDevice() {
        return this.senseThinkDevice;
    }

    public ActDevice getActDevice() {
        return this.actDevice;
    }

    public void createSteppables() {
        this.senseThinkDevice = new SenseThinkDevice();
        this.actDevice = new ActDevice();
    }

    /**
     * Use a counter to record the number of steps the device stops for
     *
     * 0 means no stop triggered 1 means stop triggered and start counting Once
     * stopCounter reaches STOP_LENGTH the agent will resume moving
     *
     * @return
     */
    public boolean checkStillStopped() {
        if (this.stopCounter > STOP_LENGTH) {
            this.stopCounter = 0;
            return false;
        } else {
            this.stopCounter++;
            return true;
        }

    }

    public boolean isStopped() {
        return stopCounter > 0;
    }

    public void setStopped() {
        this.stopCounter = 1;
    }

    public class SenseThinkDevice implements Steppable {

        public void step(SimState ss) {
            execute();
        }
    }

    public class ActDevice implements Steppable {

        public void step(SimState ss) {
            clearDenseMap();
        }
    }

    public Point2d getCurrentPosition() {
        return currentPosition.toPoint();
    }

    public void setCurrentPosition(Point2d pos) {
        currentPosition.setX(pos.getX());
        currentPosition.setY(pos.getY());
    }

    public boolean hasForbiddenArea() {
        return FA[0] || FA[1] || FA[2] || FA[3];
    }

    private double getWeightedAngle(PrecisePoint source, Point2d dest) {
        double xCoord, yCoord, neighbourAngle;
        xCoord = dest.getX() - source.getX();
        yCoord = dest.getY() - source.getY();
        neighbourAngle = xCoord / Math.sqrt(xCoord * xCoord + yCoord * yCoord);//should we use currVelocity??
        if (xCoord >= 0 && yCoord >= 0) {
            neighbourAngle = 1 - neighbourAngle; //weighted
        } else if (xCoord >= 0 && yCoord < 0) {
            neighbourAngle = 3 + neighbourAngle;
        } else if (xCoord < 0 && yCoord >= 0) {
            neighbourAngle = 1 - neighbourAngle;
        } else if (xCoord < 0 && yCoord < 0) {
            neighbourAngle = 3 + neighbourAngle;
        }
        return neighbourAngle;
    }

    public boolean inForbiddenArea(Point2d testVector) {
        /* 23rd September 2013
         double neighbourAngle;
         neighbourAngle = getWeightedAngle(this.currentPosition, testVector);
         //myAngle = Geometry.angleBetween(curr, next);
         if (neighbourAngle >= minFA && neighbourAngle <= maxFA) {
         return true;
         }
         return false;
         */
        //TODO: instead of calculating angle, just calculate only quadrant by takin positive or negative x and y
        //updated on 25th September 2013
        double neighbourAngle;
        neighbourAngle = getWeightedAngle(this.currentPosition, testVector);

        // within the first quadrant
        if (neighbourAngle >= 0 && neighbourAngle < 1 && FA[0]) {
            return true;
            // within the second quadrant
        } else if (neighbourAngle >= 1 && neighbourAngle < 2 && FA[1]) {
            return true;
            // within the third quadrant
        } else if (neighbourAngle >= 2 && neighbourAngle < 3 && FA[2]) {
            return true;
            // within the fourth quadrant
        } else if (FA[3]) {
            return true;
        }
        return false;

    }

    private void getForbiddenArea() {
        Double neighbourAngle;
        int maxAgents = 0;
        int[] agentList = new int[4];
        this.clearNeighbours();
        neighbours = this.getNeighbours();
//        minFA = 0.0;
//        maxFA = 0.0;
//        hasForbiddenArea = false;
        // initialise the number of agents in the 4 quadrants
        for (int i = 0; i < 4; i++) {
            agentList[i] = 0;
        }
        for (Object neighbour : neighbours) {
            Device dev = (Device) neighbour;
            if (dev.isDense()) {
                //next stores the resultant vector obtained from current positions of this and neighbour
                neighbourAngle = getWeightedAngle(this.currentPosition, (Point2d) dev.getCurrentPosition());
                // the neighbour falls within the first quadrant
                if (neighbourAngle >= 0 && neighbourAngle < 1) {
                    agentList[0]++;
                    if (agentList[0] > maxAgents) {
                        maxAgents = agentList[0];
                    }
                    // the neighbour falls within the second quadrant    
                } else if (neighbourAngle >= 1 && neighbourAngle < 2) {
                    agentList[1]++;
                    if (agentList[1] > maxAgents) {
                        maxAgents = agentList[1];
                    }
                    // the neighbour falls within the third quadrant
                } else if (neighbourAngle >= 2 && neighbourAngle < 3) {
                    agentList[2]++;
                    if (agentList[2] > maxAgents) {
                        maxAgents = agentList[2];
                    }
                    // the neighbour falls within the fourth quadrant
                } else {
                    agentList[3]++;
                    if (agentList[3] > maxAgents) {
                        maxAgents = agentList[3];
                    }
                }
            }
        }
        for (int j = 0; j < 4; j++) {
            if ((agentList[j] == maxAgents || agentList[j] >= 1) && maxAgents > 0) {
                FA[j] = true;
//                hasForbiddenArea = true;
            } else {
                FA[j] = false;
            }
        }

        /* Updated on 23rd September 2013
         Double neighbourAngle;
         int count = 0;
         this.clearNeighbours();
         neighbours = this.getNeighbours();
         minFA = 0.0;
         maxFA = 0.0;
         hasForbiddenArea = false;
         for (Object neighbour : neighbours) {
         Device dev = (Device) neighbour;
         if (dev.isDense()) {
         //next stores the resultant vector obtained from current positions of this and neighbour
         neighbourAngle = getWeightedAngle(this.currentPosition, (Point2d) dev.getCurrentPosition());
         if (!Double.isNaN(neighbourAngle)) {
         if (count == 0) {
         minFA = neighbourAngle;
         count++;
         } else if (count == 1) {
         count++;
         hasForbiddenArea = true;
         if (neighbourAngle < minFA) {
         maxFA = minFA;
         minFA = neighbourAngle;
         } else if (neighbourAngle > maxFA) {
         maxFA = neighbourAngle;
         }
         } else {
         if (neighbourAngle < minFA) {
         minFA = neighbourAngle;
         } else if (neighbourAngle > maxFA) {
         maxFA = neighbourAngle;
         }
         }
         }
         }
         }
         */
    }

    @Override
    public Object propertiesProxy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
