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
 * @author hunan
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

    public STRATEGY getPreviousStrategy() {
        return previousStrategy;
    }
    
    public STRATEGY getCurrentStrategy() {
        return currentStrategy;
    }    
    
    public void setPreviousStrategy(STRATEGY s){
        previousStrategy = s;
    }
    
    public void setCurrentStrategy(STRATEGY s){
        currentStrategy = s;
    }

    public static enum STRATEGY {
        FOLLOW, OVERTAKE, AVOID, 
//        INSTINCTIVERACTION
    }
    
    
    /*
     * set and get status flags
     */
    public boolean isStrategyChanged(){
        return strategyChanged;
    }
    
    public void setStrategyChanged(boolean strategyChange){
        strategyChanged = strategyChange;
    }
    
    public boolean isFinishCurrentStrategy(){
        return finishCurrentStrategy;
    }
    
    public void setFinishCurrentStrategy(boolean finish){
        finishCurrentStrategy=finish;
    }
    
    public boolean isViolateExpectancy(){
        return violateExpectancy;
    }
    
    public void setViolateExpectancy(boolean violate){
        violateExpectancy = violate;
    }

    
    public static enum strategymatchingCommitment {
        LOWCOMMITMENT, MIDCOMMITMENT, HIGHCOMMITMENT 
    }


    public static enum PreferredSpeed_Discrete {
        SLOW, MID, FAST
    }
    
    
    
    /**
     * Stores a reference back the the RVOAgent container (for retrieving agent parameters)
     */
    RVOAgent myAgent;
    Action action;
    Perception vision;
    Decision decision;
    Experience experience;
    STRATEGY currentStrategy;
    STRATEGY previousStrategy;
    boolean strategyChanged;
    boolean violateExpectancy;
    boolean finishCurrentStrategy;

    
    public PreferredSpeed_Discrete ps_discrete;

    public WorkingMemory(RVOAgent ag) {
        previousStrategy = null;
        currentStrategy = null;
        strategyChanged=false;
        violateExpectancy = false;
        finishCurrentStrategy = false;
        
        myAgent = ag; //which constructor to call
        action = new Action(this);   
        
        if (myAgent.getPreferredSpeed() <= 0.8) {
            setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.SLOW);
        } else if (myAgent.getPreferredSpeed() >= 1.3) {
            setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.FAST);
        } else {
            setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.MID);
        }
        vision = new Perception(this);
        decision = new Decision(this);
        experience = new Experience(myAgent);
        
        //@hunan: currently hardcode the prototypical pattern for each strategy now.
   
        //------------------for follow -------------------------------------------------
        STPattern stp_follow = new STPattern(0,3,11);
        int p_follow [][]={
        {8,8,1,1,1,1,1,1,1,8,8},
        {8,8,8,8,8,8,8,8,8,8,8},
        {8,8,8,8,8,8,8,8,8,8,8}
        };
        stp_follow.setSlice(0,p_follow); //follow has only 1 phase, only need to set pf==0

        ExperienceInstance ei_follow = new ExperienceInstance(STRATEGY.FOLLOW, stp_follow);
        experience.addExpInstance(ei_follow);
        //------------------------------------------------------------------------------
   
        
        //------------------for MOVE -------------------------------------------------
  /*    STPattern stp_move = new STPattern(0,3,11);
        int p_move [][]={
        {8,8,8,8,8,0,8,8,8,8,8},
        {8,8,8,8,0,0,0,8,8,8,8},
        {8,8,8,8,8,8,8,8,8,8,8}
        };
        stp_move.setSlice(0,p_move); //follow has only 1 phase, only need to set the slice when pf==0

        ExperienceInstance ei_move = new ExperienceInstance(STRATEGY.MOVE, stp_move);
        experience.addExpInstance(ei_move);
  */
        
        //-----------for OVERTAKE------------------------------------------------------
        STPattern stp_ot = new STPattern(0,3,11);
        int p_ot_phase1 [][]={
        {8,8,8,0,0,1,8,8,8,8,8},
        {8,8,8,0,0,0,8,8,8,8,8},
        {8,8,8,8,8,8,8,8,8,8,8}
        }; //currently only specify for one side (left), later add right
        
//        int t_ot = 10; //just assume the T for phase 1 in prototypical pattern for ot is 10 for now
//        int ot;
//        for (ot=0; ot< t_ot; ot++) 
            stp_ot.setSlice(0, p_ot_phase1);  //for the first 10 frames, set prototypical spatial pattern for phase 1
//        stp_ot.setSlice(ot+1,p_ot_phase2); //for the 11th frame for the phase 2

        ExperienceInstance ei_ot = new ExperienceInstance(STRATEGY.OVERTAKE, stp_ot);
        experience.addExpInstance(ei_ot);
        
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
    public Vector2d calculateVelocity(RVOAgent me, Bag neighbors, Bag obstacles, Vector2d preferredVelocity, double timeStep) {           
        decision.execute(neighbors, obstacles);
        //if no steering strategy 
        if(decision.getCurrentStrategy()== null){
            System.out.println("No steering strategy is selected");
             return this.getMyAgent().getPrefVelocity(); //non-strategic move or strategic but with no strategy matched, both will return the default prefVel towards goal and pass to locomotion control (rvo)
        }else{
            RVOAgent targetAgent = getAgent(neighbors, decision.getTargetAgentID());
            action.execute(decision.getCurrentStrategy(), targetAgent, decision.isLeft(), decision.getInstructedTime(), new Vector2d(decision.getStartVelocity()), new Point2d(decision.getStartPosition()));       
//          System.out.println("Current Velocity is: " + me.getVelocity());
            return action.getSelectedVelocity();
        }
    }
}
