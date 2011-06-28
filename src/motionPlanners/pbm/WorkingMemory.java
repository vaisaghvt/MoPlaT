/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.pbm;

import agent.RVOAgent;
import app.RVOModel;
import ec.util.MersenneTwisterFast;
import javax.vecmath.Vector2d;
import motionPlanners.VelocityCalculator;
import sim.util.Bag;

/**
 * WorkingMemory
 *
 * @author michaellees
 * Created: Dec 7, 2010
 *
 * Copyright Hu Nan
 *
 * Description: Stores all the variables necessary for the the pbm system. This
 * essentially represents the working memory of the pbm agent.
 *
 *
 *
 */
public class WorkingMemory implements VelocityCalculator {



//    public long getLastUpdate() {
//        return lastUpdate;
//    }
//    private long lastUpdate;

    public static enum STRATEGY {

        MOVE, FOLLOW, OVERTAKE, AVOID, SUDDENSLOW, SIDESLIDING
    }


    public Vector2d calculateVelocity(RVOAgent me, Bag neighbors, Bag obses, Vector2d preferredVelocity, double timeStep) {     
//        lastUpdate = me.getMySpace().getRvoModel().schedule.getSteps();
         //before updating velocity of this agent by executing the action, make a copy of current PbmVelocity
        //the decisions of all agents should be made based on the PbmVelocity rather than Agent's Velocity
//        lastFrameBuffer = new ArrayList();
//        madeCopy = lastFrameBuffer.add(myAgent.getId()); //record agent ID
//        madeCopy = lastFrameBuffer.add(myAgent.getVelocity()); //record agent current velocity before action
//        madeCopy = lastFrameBuffer.add(myAgent.getCurrentPosition()); //record agent's current position
//        System.out.println("This is the backup record for the agent before its velocity and position is updated: " + lastFrameBuffer.toString());
                
        //execute vision system and update pattern if necessary.
        vision.execute(neighbors);
        decision.execute();
        action.execute(decision.getCurrentStrategy(), decision.getTargetAgent(), decision.isLeft(), decision.getInstructedTime(), decision.getStartPosDirection(), decision.getStartPosbefAvoid());
        System.out.println("The newly calculated velocity from Action is: " + action.getSelectedVelocity().speed_x + " " + action.getSelectedVelocity().speed_y);
        System.out.println("Current Velocity is: " + me.getVelocity());
            
        System.out.println("action velocity: " + action.getVelocity());
        System.out.println("------------------------------");
        return action.getVelocity();
    }


    public static enum CommitToHighSpeed {

        LOWCOMMITMENT, MIDCOMMITMENT, HIGHCOMMITMENT //if more experienced, can make more consistent decision, less change on planned strategy
    }

    /**
     * Slow, mid and fast relate to walking speeds of x<1 m/s 1<x<3 m/s and x>3m/s
     */
    public static enum PreferredSpeed_Discrete {

        SLOW, MID, FAST
    }
    /**
     * Stores a reference back the the RVOAgent container (for retrieving agent parameters)
     */
    RVOAgent myAgent;
    Action action;
    PbmVision vision;
    Decision decision;
    STRATEGY strategy;
//    long lastStep;
//    ArrayList lastFrameBuffer; //in the arrayList, [0]-agentId [1]-Velocity [2]-Position

//    public ArrayList getLastFrameBuffer() {
//        return lastFrameBuffer;
//    }
//    boolean madeCopy;
    public CommitToHighSpeed commitmentLevel;

    public WorkingMemory(RVOAgent ag) {
        myAgent = ag; //which constructor to call
        action = new Action(this);
        //according to some literature, decay time for visual memory is around 500ms, which is 0.5 here in calculating the number of predicted frames
        vision = new PbmVision(3, (int) Math.floor(0.5 / RVOModel.getTimeStep()), myAgent.getRadius(), 1.5, 170, 9, this); //bodysize=25cm=10 distToObst=1.5m, vr=170, vl=350 (around 9 meters)
        decision = new Decision(this);//decision to be modified

//        commitmentLevel = CommitToHighSpeed.LOWCOMMITMENT;
//        MersenneTwisterFast random = myAgent.getMySpace().getRvoModel().random;
//        if (random.nextDouble() > 0.99) {
//            commitmentLevel = CommitToHighSpeed.HIGHCOMMITMENT;
//        }
//        commitmentLevel = ag.getCommitementLevel();
    }
    /**
     * Defined as walking speed
     */
    public PreferredSpeed_Discrete ps_discrete;

    public void setCommitmentLevel(CommitToHighSpeed commitmentLevel) {
        this.commitmentLevel = commitmentLevel;
    }

    public void setPs_discrete(PreferredSpeed_Discrete ps_discrete) {
        this.ps_discrete = ps_discrete;
    }

    public Action getAction() {
        return action;
    }

    public Decision getDecision() {
        return decision;
    }

    public RVOAgent getMyAgent() {
        return myAgent;
    }

    public PbmVision getVision() {
        return vision;
    }
}
