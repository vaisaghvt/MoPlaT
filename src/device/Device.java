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
//    public static double DEFAULT_PREFERRED_SPEED;
    public static int SENSOR_RANGE; //sensor range in proportion to device radius
    public static int DEVICE_THRESHOLD;
    public static int DEVICE_MSG_THRESHOLD;
    public static int DEVICE_MAX_HOPS;
    public static double DEVICE_MOVE_PROBABILITY;
    public static int STOP_LENGTH = 10; //number of ticks to stop for when too dense
    
    public static int deviceCount = 0; // no. of devices
    private HashMap<Integer, Integer> denseMap; //map of denseness of neighbours
 //   int currentGoal = 0;
    protected PrecisePoint currentPosition;
//    protected PrecisePoint velocity;
//    protected Vector2d prefVelocity;
//    protected double preferredSpeed;
//    protected double maxSpeed;
//    protected Vector2d prefDirection;
//    protected Point2d goal;
    protected Bag neighbours = new Bag();
    
    private SenseThinkDevice senseThinkDevice; //for steppable
    private ActDevice actDevice; //for steppable
    private int stopCounter=0;

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
    public void setAttachedAgent(RVOAgent thisAgent)    {
        attachedAgent = thisAgent;
    }
    
    public RVOAgent getAttachedAgent()  {
        return attachedAgent;
    }

    public boolean isDense() {
        return dense;
    }

    public  Bag getNeighbours() {
        if (neighbours.isEmpty()) {
            neighbours = mySpace.findDeviceNeighbours(this.getCurrentPosition(), Device.SENSOR_RANGE);
        }
        return neighbours;
    }

    public  void clearNeighbours() {
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
                if(dev.getDeviceId() != devId)  {   // added to prevent sending to itself
                    dev.sendTooDense(devId, hops + 1);
                }
            }

        }
        if(denseMap.size() > Device.DEVICE_MSG_THRESHOLD && mySpace.getRvoModel().random.nextDouble() < Device.DEVICE_MOVE_PROBABILITY)  {
            dense=true;
            this.setStopped();
        }
        else    {
            dense=false;
        }
    }

    public void execute() {
        this.clearNeighbours();
        //original device that sends to neighbours that it is in the dense situation
        neighbours = this.getNeighbours();
        if (neighbours.size() > Device.DEVICE_THRESHOLD) {
            sendTooDense(this.getDeviceId(), 1);
            System.out.println(neighbours.size());
        } else {
            dense = false;          
            
        }
    }
    
    public SenseThinkDevice getSenseThinkDevice()   {
        return this.senseThinkDevice;
    }
    
    public ActDevice getActDevice() {
        return this.actDevice;
    }
    
    public void createSteppables()  {
        this.senseThinkDevice = new SenseThinkDevice();
        this.actDevice = new ActDevice();
    }
/**
 * Use a counter to record the number of steps the device stops for
 * 
 * 0 means no stop triggered
 * 1 means stop triggered and start counting
 * Once stopCounter reaches STOP_LENGTH the agent will resume moving
 * @return 
 */
    public boolean checkStillStopped() {
        if(this.stopCounter > STOP_LENGTH){  
            this.stopCounter =0;
            return false;
        }else{
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
    
    public class SenseThinkDevice implements Steppable  {
        public void step(SimState ss)   {
            execute();
        }
    }
    
    public class ActDevice implements Steppable {
        public void step(SimState ss)   {
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

    @Override
    public Object propertiesProxy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
