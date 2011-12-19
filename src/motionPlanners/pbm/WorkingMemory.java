/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.pbm;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import ec.util.MersenneTwisterFast;
import javax.vecmath.Point2d;
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

    public RVOAgent getAgent(Bag neighbors, int targetAgentID) {
        for(Object o: neighbors){
            RVOAgent agent = (RVOAgent) o;
            if (agent.getId()== targetAgentID) return agent;
        }
        return null;
    }

//    public long getLastUpdate() {
//        return lastUpdate;
//    }
//    private long lastUpdate;

    public static enum STRATEGY {
        MOVE, FOLLOW, OVERTAKE, AVOID, INSTINCTIVERACTION
    }

    
    public static enum strategymatchingCommitment {
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
//    RVOAgent target;
    Action action;
    Perception vision;
    Decision decision;
    Experience experience;
    STRATEGY strategy;

    public PreferredSpeed_Discrete ps_discrete;
//    long lastStep;
//    ArrayList lastFrameBuffer; //in the arrayList, [0]-agentId [1]-Velocity [2]-Position

//    public ArrayList getLastFrameBuffer() {
//        return lastFrameBuffer;
//    }
//    boolean madeCopy;
//    public strategymatchingCommitment commitmentLevel;

    public WorkingMemory(RVOAgent ag) {
        myAgent = ag; //which constructor to call
//        target = null;
        action = new Action(this);   
        
                //can use magnitude * 1.x and angle * 1.x to represent a range for preferred speed
        if (myAgent.getPreferredSpeed() <= 0.8) {
            setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.SLOW);
        } else if (myAgent.getPreferredSpeed() >= 1.3) {
            setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.FAST);
        } else {
            setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.MID);
        }

        //vision = new Perception(3, (int) Math.floor(0.5 / RVOModel.getTimeStep()), myAgent.getRadius(), 1.5, 170, 9, this); //bodysize=25cm=10 distToObst=1.5m, vr=170, vl=350 (around 9 meters)
        //TIMESTEP == 0.05
        
        //by right, pf is better to be dynamic according to the seen agents in the center
        //then pf = distance (3.6) / |(v1-v2)|
        //for now, make it the worst case (v1 v2 at the same direction) and the assume the MAX speed variance is 0.5
        vision = new Perception((int) Math.ceil(7.2 / PropertySet.TIMESTEP), this, 3.6);
        decision = new Decision(this);//decision to be modified

//        commitmentLevel = CommitToHighSpeed.LOWCOMMITMENT;
//        MersenneTwisterFast random = myAgent.getMySpace().getRvoModel().random;
//        if (random.nextDouble() > 0.99) {
//            commitmentLevel = CommitToHighSpeed.HIGHCOMMITMENT;
//        }
//      commitmentLevel = ag.getCommitementLevel();
        experience = new Experience(myAgent);
        
        //@hunan: currently hardcode the prototypical pattern for each strategy now.
         //------------------for MOVE -------------------------------------------------
        STPattern stp_move = new STPattern(0,3,11);
        int p_move [][]={
        {8,8,8,8,8,0,8,8,8,8,8},
        {8,8,8,8,0,0,0,8,8,8,8},
        {8,8,8,8,8,8,8,8,8,8,8}
        };
        stp_move.setSlice(0,p_move); //follow has only 1 phase, only need to set pf==0

        ExperienceInstance ei_move = new ExperienceInstance(STRATEGY.MOVE, stp_move);
        experience.addExpInstance(ei_move);
  
   /*
        //------------------for follow -------------------------------------------------
        STPattern stp_follow = new STPattern(0,3,11);
        int p_follow [][]={
        {8,8,1,1,1,1,1,1,1,8,8},
        {8,8,8,8,8,8,8,8,8,8,8},
        {8,8,8,8,8,8,8,8,8,8,8}
        };
        stp_follow.setSlice(0,p_follow); //follow has only 1 phase, only need to set pf==0

        ExperienceInstance ei_follow = new ExperienceInstance(STRATEGY.AVOID, stp_follow);
        experience.addExpInstance(ei_follow);
        //------------------------------------------------------------------------------
   */
        
        //-----------for OVERTAKE (and FOLLOW)------------------------------------------------------
        STPattern stp_ot = new STPattern(0,3,11);
        int p_ot_phase1 [][]={
        {8,8,8,8,0,1,1,1,8,8,8},
        {8,8,8,0,0,0,8,8,8,8,8},
        {8,8,8,8,8,8,8,8,8,8,8}
        }; //currently only specify for one side (left), later add right
        
//        int p_ot_phase2 [][]={
//            {8,8,8,0,0,0,8,8,8,8,8},
//            {8,8,8,8,8,8,8,8,8,8,8},
//            {8,8,8,8,8,8,8,8,8,8,8}
//        };
        int t_ot = 10; //just assume the T for phase 1 in prototypical pattern for ot is 10 for now
        int ot;
        for (ot=0; ot< t_ot; ot++) 
            stp_ot.setSlice(ot, p_ot_phase1);  //for the first 10 frames, set prototypical spatial pattern for phase 1
//        stp_ot.setSlice(ot+1,p_ot_phase2); //for the 11th frame for the phase 2

        ExperienceInstance ei_ot = new ExperienceInstance(STRATEGY.OVERTAKE, stp_ot);
        experience.addExpInstance(ei_ot);
        //----------------------------------------------------------------------------
        
         //-----------for AVOID--------------------------------------------------------
        STPattern stp_avoid = new STPattern(0,3,11);
        int p_avoid [][]={
            {8,8,8,8,8,-1,8,8,8,8,8},
            {8,8,8,8,8,8,8,8,8,8,8},
            {8,8,8,8,8,8,8,8,8,8,8}
        }; //only for avoid from left now, add right later
        
        stp_avoid.setSlice(0,p_avoid); //side-avoid has only 1 phase, only need to set pf==0

        ExperienceInstance ei_avoid = new ExperienceInstance(STRATEGY.AVOID, stp_avoid);
        experience.addExpInstance(ei_avoid);
        //----------------------------------------------------------------------------
    }//end of WorkingMemory Constructor
    

//    public void setCommitmentLevel(strategymatchingCommitment commitmentLevel) {
//        this.commitmentLevel = commitmentLevel;
//    }

    public final void setPs_discrete(PreferredSpeed_Discrete ps_discrete) {
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

    public Perception getVision() {
        return vision;
    }
    
    public Experience getExperience() {
        return experience;
    }
    
    //The main function to interface with the higher level simulation (return a preferred velocity)
    @Override
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
//        vision.execute(neighbors);
        decision.execute(neighbors);
        
        RVOAgent targetAgent = getAgent(neighbors, decision.getTargetAgentID());
        Vector2d startVelocity = decision.getStartVelocity();
        Point2d startPosition = decision.getStartPosition();
        
        action.execute(decision.getCurrentStrategy(), targetAgent, decision.isLeft(), decision.getInstructedTime(), startVelocity, startPosition);       
        System.out.println("The newly calculated velocity from Action is: " + action.getSelectedVelocity().x+ " " + action.getSelectedVelocity().y);
//        System.out.println("Current Velocity is: " + me.getVelocity());
        System.out.println("------------------------------");
        return action.getSelectedVelocity();
    }
}
