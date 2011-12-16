package motionPlanners.pbm;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import java.awt.Color;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import motionPlanners.pbm.WorkingMemory.STRATEGY;

public class Decision {
    RVOAgent me;
    int targetID;
    WorkingMemory wm;
    boolean left; //from which side of the target to execute the strategy
    private STRATEGY currentStrategy; //current action = MOVE or current executing strategy (selected)
    int instructedTime; //number of frames, within which to execute the strategy
    
    int midIndex;
    int targetIndex; //column index of the target in the stp
    
    //for the desired spatial pattern to occur in the current STP instance p1
    int t1;

    //for recording of whether the matching fail is because of failed for overtaking or both overtaking and following
    boolean overtakeFlag;
  
    //for overtaking strategy
    Vector2d startVelocity;
    Point2d startPosition;

    public Point2d getStartPosition() {
        return startPosition;
    }

    public Vector2d getStartVelocity() {
        return startVelocity;
    }
    
    public int getT1(){
        return t1;
    }
    
    public Decision(WorkingMemory wm) {
        this.wm = wm;
        me = wm.getMyAgent();
        targetID = -1;
        targetIndex = -1;  
        left = true; //default left
        instructedTime = -1;
        midIndex = 5;
        this.currentStrategy = STRATEGY.MOVE;
        overtakeFlag=false;
    }

    public STRATEGY getCurrentStrategy() {
        return currentStrategy;
    }

   private void setCurrentStrategy(STRATEGY strategy) {
        this.currentStrategy = strategy;
        this.wm.getAction().finishCurrentStrategy = false;
    }
    
    public boolean isLeft() {
        return left;
    }

    public int getInstructedTime() {
        return instructedTime;
    }

    public int getTargetAgentID() {
        return targetID;
    }

    void execute() {
        if (needNewDicison()) {
            selectNewStrategy();
        }
    }

    //each time when updating all agents' states, this function is called before execute specific action in Action class
    public boolean needNewDicison() {
        if (wm.getAction().frameFromLastDecision >= wm.getDecision().getT1() || wm.getMyAgent().violateExpectancy || wm.getAction().finishCurrentStrategy 
                || this.currentStrategy == STRATEGY.FOLLOW) {
            wm.getAction().frameFromLastDecision = 0; //reset frame counter for new strategy execution
            wm.getVision().setStrategySelected(false);          
            startPosition = new Point2d(me.getCurrentPosition());
            startVelocity = new Vector2d(me.getVelocity());
            return true;
        } else {
            wm.getVision().setStrategySelected(true);
            return false;
        }
    }
    
    private void selectNewStrategy() {
        //setCurrentStrategy(STRATEGY.FOLLOW);
        //The current prototypical patterns are only designed and only necessary for fast pedestrians
        
        if(wm.ps_discrete!= motionPlanners.pbm.WorkingMemory.PreferredSpeed_Discrete.SLOW){
            double fit=0; //used to measure the fit value of mathcing, 0 not matched at all, 1 perfect match
            int bestFitPattern = -1;
            
            //[0].Matched frame number for AVOID [1]: count for OVERTAKE  [2]: count for MOVE
            int[] numMatchedFrames =new int[3];
            
            //the 3 prototypical patterns for the experiences are in the order of 0. AVOID 1.OVERTAKE 2.MOVE
            for(int i=0; i<wm.getExperience().getAllExpInstances().size(); i++){
                //here, it is different from RPD, where the best fit is used rather than the first meet the thrshold
                //this is because we do not know which threshold value is good according to the matching function defined yet
                //but later can be changed to be aligned with RPD easily by giving threshold values properly
                
                //get the fit value of the 2 stps from the Matching function
                //here, the numMatchedFrames need to be specifically allocate for each comparing steering strategy
                double match = stpMatching(numMatchedFrames, wm, wm.getVision().getSpacepattern(), wm.getExperience().getAllExpInstances().get(i).getPrototypicalSTP());
                if(fit < match){
                    fit = match;
                    bestFitPattern = i;
                } 
            }//end of for, choose the best matched empirical instance
          
          //inside Matching(wm, p1,p2), should set target, instructedTime , left
          
           if(bestFitPattern>=0){
              //set the current strategy according to the matched experience instance 
                setCurrentStrategy(wm.getExperience().getAllExpInstances().get(bestFitPattern).getEmpiricalStg());
           
//           else{
//               //almost cant happen
//               System.out.println("FOLLOW is selected");
//               setCurrentStrategy(STRATEGY.FOLLOW);
//           }
//           if(this.currentStrategy!=STRATEGY.MOVE) 
                wm.getVision().setStrategySelected(true);
//           else wm.getVision().setStrategySelected(false);
           
            findTarget(currentStrategy, numMatchedFrames);
           }
        }//end of preferredSpeed>1 (FAST)
        else{
            setCurrentStrategy(STRATEGY.MOVE);
            wm.getVision().setStrategySelected(false);
        }
    }
    
        /*
     * Hierarchical STPattern Matching process, returns a final matching value between[0,1]
     * given wm (the agent) and two comparing STPs
     * return the matching fit value (0-1), 0 not matched, 1 perfect match
     * At the same time, edit the 4 variables in wm: target, targetColumnIndex, left, instructedTime
     */
    private double stpMatching(int[] count, WorkingMemory wm, STPattern p1, STPattern p2){
        //matching value for each prototypical value
        
        //everytime, clean the count of numMatchedFrames first
//        count = 0;
        motionPlanners.pbm.WorkingMemory.strategymatchingCommitment commit = me.getCommitementLevel();
        
        double match=0.0;
            //TODO: get a score for the match  between p1 and p2
            //TODO: according to the commitmentlevel of the agent, process the match and result a final match score 
            //Theoretically, should apply a general matching mechanism between p1 and p2,however, we manually to check some constituents 
            //that are in prototypical STP for different steering strategys now, which means we do not use p2 now

            //If Prototypical STP is for MOVE
        if(p2.getValue(0, 0, 5)==0){
            int requiredMatch =0;
            int maxMatch = (int) (Math.ceil((3.6/wm.getMyAgent().getPrefVelocity().length())/PropertySet.TIMESTEP));
            switch(commit){
                case HIGHCOMMITMENT:
                    requiredMatch= (int) Math.round(0.2 * maxMatch); // 3.6/(1.3*1.2-1) / TIMESTEP = 7*20 = 140  increase around 1.2 times of speed during catch up
                    break;
                case MIDCOMMITMENT:
                    requiredMatch= (int)Math.round(0.1 * maxMatch); //  3.6/(1.3*1.5-1) 
                    break;
                case LOWCOMMITMENT:
                    requiredMatch= 1;  // once see the chance now, just start
                    break;
                default: 
                    break;              
            }
        
            for(int i=0; i< wm.getVision().getPf()+1; i++){
                if(p1.getValue(i, 0, 5)==0){
                    //the current frame has most impact (requiredMatch), the last frame has less (0)
                    count[2]++; 
                }else{
                    break;
                }
            }
            // count = (requiredMatch + 0.5 requiredMatch + 0.25 requiredMarch + ....)
//            double totalMatch = 1-Math.pow(0.5, requiredMatch);
            match = count[2] / requiredMatch; //match with MOVE
            if(match>1) match =1;
        }
        //for prototypical STP of SIde-AVOID
        else if(p2.getValue(0, 0, 5)==-1){
            int requiredMatch =0;
            int maxMatch = (int) (Math.ceil((1.8/wm.getMyAgent().getPrefVelocity().length())/PropertySet.TIMESTEP));
            switch(commit){
                case HIGHCOMMITMENT:
                    requiredMatch= (int) Math.round(0.2 * maxMatch); 
                    break;
                case MIDCOMMITMENT:
                    requiredMatch= (int) Math.round(0.1 * maxMatch); 
                    break;
                case LOWCOMMITMENT:
                    requiredMatch= 1;  // once see the chance now, just start
                    break;
                default: 
                    break;              
            }
            for(int i=0; i<wm.getVision().getPf()+1; i++){  //for side-avoid, two parties walking towards each other
                if(p1.getValue(0, 0, 5)== -1){
                    count[0]++;
                }else{
                    break;
                }
            }
            match = count[0] / requiredMatch; //match with AVOID
            if(match>1) match =1;
        }
        //for OVERTAKE or FOLLOW
        else{     
//            int count = 0;
            overtakeFlag=true;
//            match++; //once enter this, has to enable the count to be >=0 for follow and overtake
            int requiredMatch =0;
            int maxMatch = (int) (wm.getVision().getPf());
            switch(commit){
                case HIGHCOMMITMENT:
                    requiredMatch= (int) Math.round(0.6 * maxMatch); 
                    break;
                case MIDCOMMITMENT:
                    requiredMatch= (int) Math.round(0.3 * maxMatch); 
                    break;
                case LOWCOMMITMENT:
                    requiredMatch= 1;  // once see the chance now, just start
                    break;
                default: 
                    break;              
            }
            for(int j=0; j< wm.getVision().getPf()+1; j++){
                //look for coditional "01" (the constitute for Overtake STP) in different slices from p1
                int OtargetCurrentIndex = checkConstituteOVERTAKE(j, p1);
                if(OtargetCurrentIndex>=0 && OtargetCurrentIndex<=10){
                    count[1]++;
                    if(j==0){
                        targetIndex = OtargetCurrentIndex;
                        targetID=wm.getVision().getSpacepattern_agt().getPattern()[0][0][targetIndex];
                        if(OtargetCurrentIndex<=5) left = true;
                        else left = false;
                    }
                }else{
                    break;
                }
            }
            match = count[1] / requiredMatch;
            if(match>1) match =1;
        }    
        return match;
    }
    

 private int checkConstituteOVERTAKE(int frame, STPattern p1){
     int currentTargetIndex=-1;
     
     int leftTIndex = -1;
     int rightTIndex = 11;
     //index ==1: for overtake

     //search for the left half
     for(int i = 5; i> 2; i--){
        if(p1.getValue(frame, 0, i)==1 && p1.getValue(frame, 0, i-1)==0 
        && p1.getValue(frame, 1, i)!=-1 && p1.getValue(frame, 1, i-1)==0 ){
            leftTIndex=i;
        }
     }
     //search for the right half
     for(int j = 5; j<8; j++){
        if(p1.getValue(frame, 0, j)==1 && p1.getValue(frame, 0, j+1)==0
        && p1.getValue(frame,1,j)!= -1 && p1.getValue(frame, 1, j+1)==0 ){
            rightTIndex=j;
        }
     }
     
     if((5-leftTIndex) <= (rightTIndex-5)){
         currentTargetIndex = leftTIndex;
     }else{
         currentTargetIndex = rightTIndex;
     } 
     return currentTargetIndex;
 }
    
    /*
     * Based on the current selected strategy to specify target and left
     * based on match*RequiredMax = matched number of frames, can calculate the time, where the existing perceived spatial pattern exist for
     * then can specify T (: from now until T, have to finish the potential strategy, then increase speed accordingly)
     */
    private void findTarget(STRATEGY currentStrategy, int[] matchedFrames) {        
        if(currentStrategy==STRATEGY.AVOID){
           int leftEdgeIndex = -1;
           int rightEdgeIndex = 11;
            
           //find left edge index of the potential avoiding group
           for(int i=4; i>2; i--){
                if(wm.getVision().getSpacepattern().getValue(0, 0, i)==-1){
                    leftEdgeIndex = i;
               }
           }
            //find the right potential edge of the avoiding group
           for(int j=6;j<8;j++){
               if(wm.getVision().getSpacepattern().getValue(0, 0, j)==-1){
                  rightEdgeIndex = j;
               }
           }
           //prefer left
           int edgeIndex = leftEdgeIndex;
           left = true;

           if(rightEdgeIndex-5<5-leftEdgeIndex){
               edgeIndex= rightEdgeIndex;
               left = false;
           }
           else if(rightEdgeIndex-5==5-leftEdgeIndex){
               if(wm.getVision().getSpacepattern().getValue(0, 0, leftEdgeIndex-1)==0) edgeIndex=leftEdgeIndex;
               else if(wm.getVision().getSpacepattern().getValue(0, 0, rightEdgeIndex+1)==0) {
                   edgeIndex=rightEdgeIndex;
                   left = false;
               }else{
                   int densityLeft = 0;
                   int densityRight = 0;
                   for(int k=leftEdgeIndex; k>=leftEdgeIndex-2; k--)
                       for(int l = 1; l<=2;l++){
                           //for 1 at row 1 has more attention impact  than a 1 in row 2, the significance can is scaled by abs(1.7-j)
                           densityLeft += Math.abs(wm.getVision().getSpacepattern().getValue(0, l, k))* Math.abs(l-1.7);
                   }
                   for(int m=rightEdgeIndex; m<=rightEdgeIndex+2; m++)
                       for(int n = 1; n<=2; n++){
                           densityRight += Math.abs(wm.getVision().getSpacepattern().getValue(0, n, m)) * Math.abs(n-1.7);
                   }
                   if(densityLeft<=densityRight) edgeIndex=leftEdgeIndex;
                   else{
                       edgeIndex=rightEdgeIndex;
                       left=false;
                   }
               } 
           }
           if(edgeIndex>=0 && edgeIndex<=10){    
               targetID = wm.getVision().getSpacepattern_agt().getValue(0, 0, edgeIndex);
               
//               int requiredMatch =0;
//               int maxMatch = (int) (Math.ceil((1.8/wm.getMyAgent().getVelocity().length())/RVOModel.getTimeStep()));
//               switch(wm.commitmentLevel){
//                    case HIGHCOMMITMENT:
//                        requiredMatch= (int) Math.round(0.6 * maxMatch); 
//                        break;
//                    case MIDCOMMITMENT:
//                        requiredMatch= (int) Math.round(0.3 * maxMatch); 
//                        break;
//                    case LOWCOMMITMENT:
//                        requiredMatch= 1;  // once see the chance now, just start
//                        break;
//                    default: 
//                        break;              
//               }
//               t1= (int) (match * requiredMatch);
               t1= matchedFrames[0];
           }
           else System.out.println("Error: steering strategy AVOID was selected but without valid target to avoid");
           return;
        } //end of SIDE-AVOID
        else if(currentStrategy==STRATEGY.MOVE){        
            targetID=-1;
            targetIndex=-1;
            t1=matchedFrames[2];
            return;
        }
        else if(currentStrategy==STRATEGY.OVERTAKE){
//            int requiredMatch =0;
//            int maxMatch = (int) (wm.getVision().getPf()/RVOModel.getTimeStep());
//
//            switch(wm.commitmentLevel){
//               case HIGHCOMMITMENT:
//                    requiredMatch= (int) Math.round(0.6 * maxMatch); 
//                    break;
//                case MIDCOMMITMENT:
//                    requiredMatch= (int) Math.round(0.3 * maxMatch); 
//                    break;
//                case LOWCOMMITMENT:
//                    requiredMatch= 1;  // once see the chance now, just start
//                    break;
//                default: 
//                    break;           
//            }
            //when the match of "OVERTAKE" does not meet the number of frames as required by commitment
            t1 = matchedFrames[1];
            
            Point2d myPos = wm.getMyAgent().getCurrentPosition();
            RVOAgent targetAgent = wm.getAgent(wm.getVision().getSensedAgents(), targetID);
            Point2d targetPos = targetAgent.getCurrentPosition();
            double distanceToTarget = myPos.distance(targetPos);
            
            //if using the max speed of the agent still cannot reach where the target is (estimate the distance for cathing up)
            if((wm.getMyAgent().getMaxSpeed()- targetAgent.getVelocity().length()) * t1 * PropertySet.TIMESTEP < distanceToTarget){
                currentStrategy= STRATEGY.FOLLOW;
                System.out.println("The perceived spatial pattern for OVERTAKE does not exist long enough for me to overtake, FOLLOW instead");
            }else{
                System.out.println("SELECT to OVERTAKE");
                return;
            }
        }
        //for FOLLOW
        System.out.println("selecting target for FOLLOW...");
        STPattern p1 = wm.getVision().getSpacepattern();
        STPattern p1_agt = wm.getVision().getSpacepattern_agt();
        
        double signifiedDensity_left = 0.0;
        double signifiedDensity_right = 0.0;
        
        //which means FOLLOW because there are not enough frames to meet OVERTAKE, but at the current frame, it can OVERTAKE
        //the potential OVERTAKE target and side has been set based on Frame0
        if(targetID>=0){
            //maintain targetID and left that as to be overtaken
            return;
        }
        //has to follow from the beginning (frame 0 even cannot allow OVERTAKE)
        else{
            //simply set to follow the agent in the middle
           targetID = p1_agt.getValue(0, 0, 5);
            //side to follow based on the heuristic of signifiedDensity value
           double valueSignificance;

           for(int i=0; i<p1.getPattern()[0].length; i++)
                for(int j=1; j<3;j++){
                    for(int k=3;k<5;k++){
                        //a -1 gives a significance of 1.5 of that of a 1
                        //a -1 or 1 at row 1 gives a significance of 2.0 that of at row 2
                        valueSignificance = 1.0;
                        if(p1.getValue(i, j, k)==-1) valueSignificance = 1.5;
                        signifiedDensity_left += Math.abs(p1.getValue(i, j, k))* valueSignificance * (3-j);
                    }
                    for(int l=6;l<8;l++){
                        //a -1 gives a significance of 1.5 of that of a 1
                        //a -1 or 1 at row 1 gives a significance of 2.0 that of at row 2
                        valueSignificance = 1.0;
                        if(p1.getValue(i, j, l)== -1) valueSignificance = 1.5;
                        signifiedDensity_right += Math.abs(p1.getValue(i, j, l))* valueSignificance * (3-j);
                    }
                }
            if(signifiedDensity_left <= signifiedDensity_right) left = true;
            else left = false;
            return;
        }
    }
}