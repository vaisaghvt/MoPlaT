package motionPlanners.pbm;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import java.awt.Color;
import java.util.ArrayList;
import javax.media.j3d.Geometry;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import motionPlanners.pbm.WorkingMemory.STRATEGY;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class Decision {
    Bag neighbor;
    int targetIndex; //column index of the target in the stp
    int targetID;
    WorkingMemory wm;
    boolean left; //from which side of the target to execute the strategy
    
    private STRATEGY currentStrategy; //current action = MOVE or current executing strategy (selected)
    private STRATEGY tentCurrentStategy;
    
    TreeMap<Integer,Integer> tentTargetIndex;
    TreeMap<Integer,Integer> tentTargetID;
    TreeMap<Integer,Side> tentSide;
    
    int instructedTime; //number of frames, within which to execute the strategy
    
    int midIndex;
    int midIndex_odd;
    int numOfVisualColumns;

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


    
     public static enum Side{
        LEFT,RIGHT
    }
    
    public Decision(WorkingMemory wm) {
        this.wm = wm;
//        me = wm.getMyAgent();
//        System.out.println("initial velocity at wm is: "+me.getVelocity());//correct
        targetID = -1;
        targetIndex = -1;  
        
        tentTargetID = new TreeMap<Integer,Integer>();
        tentTargetIndex = new TreeMap<Integer,Integer>();
        tentSide=new TreeMap<Integer,Side>();
        
        left = true; //default left
        instructedTime = -1;
        currentStrategy = null;
        tentCurrentStategy = null;
        
        if(wm.ps_discrete == motionPlanners.pbm.WorkingMemory.PreferredSpeed_Discrete.FAST){
            overtakeFlag=true; 
        }
        else{
            overtakeFlag=false;
        }
        
        neighbor = new Bag();
        midIndex =-1;
        midIndex_odd =-1;
        numOfVisualColumns=0;

    }

    public STRATEGY getCurrentStrategy() {
        return currentStrategy;
    }

   private void setCurrentStrategy(STRATEGY strategy) {
        this.currentStrategy = strategy;
    }
   
   public STRATEGY getTentCurrentStrategy() {
        return tentCurrentStategy;
    }

   private void setTentCurrentStrategy(STRATEGY strategy) {
        this.tentCurrentStategy = strategy;
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

    void execute(Bag sensedneighbor, Bag sensedObstacles) {
        if (needNewDicison(sensedneighbor, sensedObstacles)) {
            selectNewStrategy();
        }
    }

    //each time when updating all agents' states, this function is called before execute specific action in Action class
    public boolean needNewDicison(Bag sensedneighbor, Bag sensedObstacles) {
        if (
                (this.currentStrategy == STRATEGY.OVERTAKE && wm.getAction().frameFromLastDecision >= wm.getDecision().getInstructedTime() * 2)
                || wm.isFinishCurrentStrategy() 
                || wm.isViolateExpectancy()
                || (this.currentStrategy == STRATEGY.FOLLOW && wm.getAction().frameFromLastDecision >=10) //should be put into expectancyViolation or finishStrategy of following execution
                || this.currentStrategy == null
//                || this.currentStrategy == STRATEGY.INSTINCTIVERACTION   //no explicity strategy called instinctive reaction, if it is null, it is possibly i.r, need to identify in action and RVOAgent
           ) 
        {
            wm.setStrategyChanged(true); //need to make new decision
            wm.setFinishCurrentStrategy(false);
            wm.setViolateExpectancy(false);
            targetID = -1;
            targetIndex = -1;
            tentCurrentStategy=null;
            tentSide.clear();
            tentTargetID.clear();
            tentTargetIndex.clear();
            currentStrategy=null;

            wm.getAction().frameFromLastDecision = 0; //reset frame counter for new strategy execution        
            wm.getMyAgent().setPrefVelocity();
            
            wm.getMyAgent().setVelocity(new Vector2d(wm.getMyAgent().getVelocity().x + 0.5* (wm.getMyAgent().getPrefVelocity().x-wm.getMyAgent().getVelocity().x),
                 wm.getMyAgent().getVelocity().y + 0.5* (wm.getMyAgent().getPrefVelocity().y-wm.getMyAgent().getVelocity().y)));//vision is set based on velocity rather than prefVel currently
            
            
            startPosition = new Point2d(wm.getMyAgent().getCurrentPosition());
            startVelocity = new Vector2d(wm.getMyAgent().getVelocity());
            
            neighbor = new Bag(sensedneighbor);
            wm.getVision().execute(sensedneighbor, sensedObstacles);
           
//            if(wm.getMyAgent().getId()==3){
//                System.out.println();
//            }
//            
            midIndex=-1;
            midIndex_odd=-1;
            
            if(wm.getVision().getHasNeighbourForReaction()){
                currentStrategy = null; // instinctive reaction to resolve immenant collisions
                wm.setPreviousStrategy(wm.getCurrentStrategy());
                wm.setCurrentStrategy(currentStrategy);  
                System.out.println("Instinctive reaction to resolve immenant collisions");
                return false;
            }else if(wm.getVision().getHasNeighbourForSteering()){
                numOfVisualColumns = (int)Math.floor(wm.getVision().getVisualRange()/wm.getVision().getAngle());
                if(numOfVisualColumns >=3){
                    if(numOfVisualColumns%2==0){
                        midIndex = numOfVisualColumns/2;
                        midIndex_odd = numOfVisualColumns/2 - 1;
                    }else{
                        midIndex = numOfVisualColumns/2;
                    } 
                    System.out.println("*************Agt"+wm.getMyAgent().getId()+" needs to make new decision, its velocity reset to prefVel towards its goal: "+wm.getMyAgent().getVelocity());
                }else{
                    this.currentStrategy = null; //got agent already too close to me, instincitive reaction
                    wm.setPreviousStrategy(wm.getCurrentStrategy());
                    wm.setCurrentStrategy(currentStrategy);  
                    System.out.println("Instinctive reaction2 to resolve immenant collisions");
                    return false;
                }
            }else{
                this.currentStrategy = null;  // perceived no other agents in my vision for steering, thus strategy need to be applied
                wm.setPreviousStrategy(wm.getCurrentStrategy());
                wm.setCurrentStrategy(currentStrategy);  
                System.out.println("Non strategic moving");
                return false;
            }
            return true;
        } 
        //No New decision is Needed, Still Executing the Previous Strategy
        else {
            wm.setStrategyChanged(false); //no need to make new decision
            return false;
        }
    }
    
    private void selectNewStrategy() {       
            //set tentative strategy for three strategies, also set tentTargetID and tentTargetIndex inside this function
            initialMatching(wm.getVision().getSpacepattern());
            
            if(tentCurrentStategy==null){
                currentStrategy = null;
                wm.setPreviousStrategy(wm.getCurrentStrategy());
                wm.setCurrentStrategy(currentStrategy);  
                return;
            }
            
            //have the tentative strategy, target and side(OVERTAKE, AVOID): 
            //1. set the verifyVelocity (mental simulate the strategy execution) 
            //2. Generated predicted frames in STP
            //3. verifyMatching (this is similar or the same as violency check during execution)
            verifyMatching();
//                    wm.getVision().setStrategySelected(true);
//            wm.setStrategyChanged(true);  //a strategy is newly selected, no matther whether it is the same as the previous one as the stp may be totally different
            wm.setPreviousStrategy(wm.getCurrentStrategy());
            wm.setCurrentStrategy(currentStrategy);
    }
    
    /*
     * set the tentative strategy that intend to be executed
     * 
     * At the same time, edit the 4 variables in decision class: tentTarget, tenttargetColumnIndex, tentSide, tentCurrentStrategy
     */
    private void initialMatching(STPattern p1){
        //check p1 for the most tentative strategy
        ArrayList<Integer> centralFuzzyColValues = new ArrayList<Integer>();
        int columnNum = p1.getPattern()[0][0].length;
        int leftCentral=-1;
        int rightCentral=-1;
        //odd number of columns
        if(midIndex_odd<0){
            leftCentral=midIndex;
            rightCentral=midIndex;
            int count = 1;
            if(columnNum==3){
                leftCentral=0;
                rightCentral=2;// if 3 columns, then all 3 are considered
            }else{
                int totalCentralColNum = (int)Math.ceil(columnNum/2);
                if(totalCentralColNum/2 ==0){
                    totalCentralColNum= (int)Math.floor(columnNum/2);
                }
                while(count+2<= totalCentralColNum){
                    leftCentral--;
                    rightCentral++; //set the left and right index for the central checking area
                    count+=2;
                }
            }
        }
        //even number of columns
        else{
            leftCentral=midIndex_odd;
            rightCentral=midIndex;
            int count = 2;
            int totalCentralColNum = columnNum/2;
            if(totalCentralColNum%2!=0){
                totalCentralColNum+=1;
            }
            
            while(count+2<=totalCentralColNum){
                leftCentral--;
                rightCentral++;
                count+=2;
            }
        }
        for(int i = leftCentral; i<=rightCentral; i++){
            centralFuzzyColValues.add(fuzzyColumnValue(p1, i)); //so from left to right, the column values are in the list with index of 0 onwards
        }

        //==============================Start to Search the Fuzzy Column Value Array for Possible Patterns-=============================================
        
        
        int currentColValue; //initial value for the start of the search, for odd number of columns, it it the value of the middle column, for even, it is assigned according to the following mechanism
        //if both 00, no use, if at least a -1, then this value is set to -1, otherwise (11, 01, 10), then set to 1
        
//        int crossColumnIndexNearCenter_left = -1;
//        int crossColumnIndexNearCenter_right = centralFuzzyColValues.size();
        
        if(midIndex_odd>=0){
            if(centralFuzzyColValues.get(midIndex-leftCentral)==0 && centralFuzzyColValues.get(midIndex_odd-leftCentral)==0){
                //if the center is 0, then consier wider range latitudally, to see whether have 2 or -2 needs to be avoided
//                currentColValue=0;
//                for(int i=midIndex_odd-1; i>=0;i--){
//                    if(fuzzyColumnValue(p1, i)==2){
//                        currentColValue=2;
//                        crossColumnIndexNearCenter_left=i;  //the index of the first "2" nearer to center on the left hand side in centralFuzzyColValues
//                        break;
//                    }
//                }
////                if(currentColValue!=2){
//                for(int i=midIndex+1;i<columnNum;i++){
//                    if(fuzzyColumnValue(p1, i) ==-2){
//                        currentColValue=-2;
//                        crossColumnIndexNearCenter_right=i;
//                        break;
//                    }
//                }
////                }
//                if(currentColValue!=0){
//                    if(midIndex_odd-crossColumnIndexNearCenter_left<crossColumnIndexNearCenter_right-midIndex){   //agents crossing from left to right is neared to my center, more immenant
//                        currentColValue = 2;
////                        tentCurrentStategy = STRATEGY.AVOID;
////                        tentSide.put(1, Side.RIGHT);
////                        tentTargetIndex.put(1, crossColumnIndexNearCenter_left);
////                        int tgtRowIndex = 0;
////                        for(int i=0;i<3;i++){
////                            if(p1.getValue(0, i, crossColumnIndexNearCenter_left)==2){
////                                tgtRowIndex=i;
////                                break;
////                            }
////                        }
////                        tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, tgtRowIndex, crossColumnIndexNearCenter_left));
//                    }else if(midIndex_odd-crossColumnIndexNearCenter_left>crossColumnIndexNearCenter_right-midIndex){
//                        currentColValue = -2;
//                    }else{
//                        tentCurrentStategy = null; //in case there are both from left to right and right to left crossing in front of me, currently set to instinctive reaction (no pattern designed for this scenario yet)
//                        return;
//                    }
//                }
//                if(currentColValue==2 || currentColValue==-2){
//                    //go to check avoid with crossing feature       
//                    
//                }else{
                    tentCurrentStategy = null; //non-strategic
                    return;
//                }
            }else if(centralFuzzyColValues.get(midIndex-leftCentral)==1 || centralFuzzyColValues.get(midIndex_odd-leftCentral)==1){ //if the direct middle of me is 01 11 or 10 then verify for overtake
                currentColValue=1;
            }else if(centralFuzzyColValues.get(midIndex-leftCentral)== -1 || centralFuzzyColValues.get(midIndex_odd-leftCentral)==-1){
                currentColValue=-1;
            }
            
//            else if(centralFuzzyColValues.get(midIndex-leftCentral) == 2 || centralFuzzyColValues.get(midIndex_odd-leftCentral)==2
//                    || centralFuzzyColValues.get(midIndex-leftCentral) == -2 || centralFuzzyColValues.get(midIndex_odd-leftCentral)== -2){
//                currentColValue=2;
//            }
            else{
                tentCurrentStategy = null;    //the case where fuzzy value == -9, which is initial value representing obstacles
                return;
            }
        }else{
            currentColValue = centralFuzzyColValues.get(midIndex-leftCentral);
//            if(currentColValue==0){
//                for(int i=midIndex-1; i>=0;i--){
//                    if(fuzzyColumnValue(p1, i) ==2){
//                        currentColValue=2;
////                        crossColumnIndexNearCenter=i;
//                        break;
//                    }
//                }
//                for(int i=midIndex+1;i<columnNum;i++){
//                        if(fuzzyColumnValue(p1, i) ==-2){
//                            currentColValue=-2;
//    //                        crossColumnIndexNearCenter=i;
//                            break;
//                        }
//                }
//                if(currentColValue!=0){
//                    if(midIndex-crossColumnIndexNearCenter_left<crossColumnIndexNearCenter_right-midIndex){   //agents crossing from left to right is neared to my center, more immenant
//                        currentColValue = 2;
//                    }else if(midIndex-crossColumnIndexNearCenter_left>crossColumnIndexNearCenter_right-midIndex){
//                        currentColValue = -2;
//                    }else{
//                        tentCurrentStategy = null; //in case there are both from left to right and right to left crossing in front of me, currently set to instinctive reaction (no pattern designed for this scenario yet)
//                        return;
//                    }
//                }
//            }
        }
     
        if(currentColValue==0){
            tentCurrentStategy = null; //non-strategic
//                currentStrategy = null;
            return;
        }else if(currentColValue==1){
            initialMatch_CenterOne(centralFuzzyColValues, p1, leftCentral); //initial "previousvalue" =1
        } 
        else if (currentColValue==-1){ 
            initialMatch_CenterNegativeOne(centralFuzzyColValues, p1, leftCentral); //initial "previousvalue"=-1

        }
//        else if(currentColValue==2 || currentColValue== -2){
//            initialMatch_AvoidCross(p1);
//        }
        else{
            tentCurrentStategy = null;     //the case where fuzzy value == -9, which is initial value representing obstacles
            return;
        }
//      }
        
    }//end of function initialMatch
    
    //when my middle is a fuzzy 1, which trying to match for overtake, if overtake is not feasible, follow
    private void initialMatch_CenterOne(ArrayList<Integer> centralFuzzyColValues, STPattern p1, int leftCentral){
        int midAgtId = -1;
        int midAgtId_odd = -1;
        RVOAgent midAgt = null;
        RVOAgent midAgt_odd = null;
        
        boolean relativeFaster=false;       
            
        if(midIndex_odd<0){
            /*
             * because we do not know whether the middle 1 is caused by 1 at R0, or R1 or R2, this is to get the row index to avoid returning null pointer
             */
            int midAgtRowIndex = 0;
            while(midAgtRowIndex< p1.getPattern()[0].length-1 && p1.getValue(0, midAgtRowIndex, midIndex)!= 1){
                midAgtRowIndex++;
            }
            
//            midAgtId = p1.getValue(0, 0, midIndex)==1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, midIndex): wm.getVision().getSpacepattern_agt().getValue(0, 1, midIndex);
            midAgtId = wm.getVision().getSpacepattern_agt().getValue(0, midAgtRowIndex, midIndex);
            midAgt= wm.getAgent(neighbor, midAgtId);
         
            Vector2d relativeVelMetoMidAgt = new Vector2d(midAgt.getVelocity());
            relativeVelMetoMidAgt.sub(wm.getMyAgent().getVelocity()); 
            if(utility.Geometry.sameDirection(wm.getMyAgent().getVelocity(), relativeVelMetoMidAgt)< Math.cos((Math.PI+wm.getVision().getAngle())/2)
                    && Math.abs(wm.getMyAgent().getVelocity().x)>Math.abs(midAgt.getVelocity().x)  ){   //the midAgt is relatively moving in towards me
                relativeFaster=true;
            } 
        }else{
            int midAgtRowIndex = 0;
            int midAgtRowIndex_odd = 0;
            while(midAgtRowIndex< p1.getPattern()[0].length-1 && p1.getValue(0, midAgtRowIndex, midIndex)!= 1){
                midAgtRowIndex++;
            }
            while(midAgtRowIndex_odd< p1.getPattern()[0].length-1 && p1.getValue(0, midAgtRowIndex_odd, midIndex_odd)!= 1){
                midAgtRowIndex_odd++;
            }
                         
//            midAgtId = p1.getValue(0, 0, midIndex)==1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, midIndex): wm.getVision().getSpacepattern_agt().getValue(0, 1, midIndex);
//            midAgtId_odd = p1.getValue(0, 0, midIndex_odd)==1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, midIndex_odd): wm.getVision().getSpacepattern_agt().getValue(0, 1, midIndex_odd);
           
            midAgtId = wm.getVision().getSpacepattern_agt().getValue(0, midAgtRowIndex, midIndex);
            midAgtId_odd = wm.getVision().getSpacepattern_agt().getValue(0, midAgtRowIndex_odd, midIndex_odd);
            
            midAgt= wm.getAgent(neighbor, midAgtId);
            midAgt_odd= wm.getAgent(neighbor, midAgtId_odd);
                    
            if(midAgt==null){
                Vector2d relativeVelMetoMidAgt = new Vector2d(midAgt_odd.getVelocity());
                relativeVelMetoMidAgt.sub(wm.getMyAgent().getVelocity()); 
                if(utility.Geometry.sameDirection(wm.getMyAgent().getVelocity(), relativeVelMetoMidAgt)< Math.cos((Math.PI+wm.getVision().getAngle())/2)){   //the midAgt is relatively moving in towards me
                    relativeFaster=true;
                } 
            }else if(midAgt_odd==null){
                Vector2d relativeVelMetoMidAgt = new Vector2d(midAgt.getVelocity());
                relativeVelMetoMidAgt.sub(wm.getMyAgent().getVelocity()); 
                if(utility.Geometry.sameDirection(wm.getMyAgent().getVelocity(), relativeVelMetoMidAgt)< Math.cos((Math.PI+wm.getVision().getAngle())/2)){   //the midAgt is relatively moving in towards me
                    relativeFaster=true;
                } 
            }else{
                Vector2d relativeVelMetoMidAgt = new Vector2d(midAgt.getVelocity());
                Vector2d relativeVelMetoMidAgt_odd = new Vector2d(midAgt_odd.getVelocity());
                
                relativeVelMetoMidAgt.sub(wm.getMyAgent().getVelocity());
                relativeVelMetoMidAgt_odd.sub(wm.getMyAgent().getVelocity());
                
                if(utility.Geometry.sameDirection(wm.getMyAgent().getVelocity(), relativeVelMetoMidAgt)< Math.cos((Math.PI+wm.getVision().getAngle())/2)
                        || utility.Geometry.sameDirection(wm.getMyAgent().getVelocity(), relativeVelMetoMidAgt_odd)< Math.cos((Math.PI+wm.getVision().getAngle())/2)){   //the midAgt is relatively moving in towards me
                    relativeFaster=true;
                } 
            }
        }
        int leftIndexPointer;
        int rightIndexPointer;
        
        int initialLeft;
        int initialRight;
        
        if(midIndex_odd<0){
            initialLeft = midIndex-leftCentral;
            leftIndexPointer = initialLeft;
            initialRight = midIndex-leftCentral;
            rightIndexPointer = initialRight;
        }else{
            initialLeft = midIndex_odd-leftCentral;
            leftIndexPointer= initialLeft;
            initialRight = midIndex-leftCentral;
            rightIndexPointer= initialRight;
        }
        boolean leftOvertakeFound = false;
        boolean rightOvertakeFound = false;


        if(relativeFaster){  //leftindexpointer search for 0 that is nearer to center, only when 0 is found, leftStgyFound will be set to true
            if(overtakeFlag){
                while(leftIndexPointer>=0){
                    int currentColValue=centralFuzzyColValues.get(leftIndexPointer);
                    if(currentColValue==0){
                        leftOvertakeFound=true;
                        break;
                    }else if(currentColValue==1){
                        leftOvertakeFound=false;
                        leftIndexPointer--;
                    }
                    else{ //include (currentColValue==-1) and other value for static obstacles
                        leftOvertakeFound=false;
                        break;
                    }
                }
         //search in right half
               while(rightIndexPointer<centralFuzzyColValues.size()){
                    int currentColValue=centralFuzzyColValues.get(rightIndexPointer);
        //            if(relativeFaster){  //rightindexpointer search for 0 that is nearer to center, only when 0 is found, rightStgyFound will be set to true
                        if(currentColValue==0){
                            rightOvertakeFound=true;
                            break;
                        }else if(currentColValue==1){
                            rightIndexPointer++;
                            rightOvertakeFound=false;
                        }
                        else{ //include (currentColValue==-1) and other value for static obstacles
                            rightOvertakeFound=false;
                            break;
                        }
        //            }
                }

                int targetAgtRowIndex = 0;
                
                if(leftOvertakeFound){ //found 01 on the left half
                    tentCurrentStategy=STRATEGY.OVERTAKE;
                    if(!rightOvertakeFound){
                        tentSide.put(1,Side.LEFT);
                        tentTargetIndex.put(1,leftIndexPointer+1+leftCentral); //return the column index of 1, not 0
                        
                        while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral)!= 1){
                            targetAgtRowIndex++;
                        }                   
                        tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral));
                    }else{
                        if(initialLeft-leftIndexPointer < rightIndexPointer-initialRight){
                            tentSide.put(1,Side.LEFT);
                            tentTargetIndex.put(1,leftIndexPointer + 1+leftCentral);
                            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral)!= 1){
                                targetAgtRowIndex++;
                            }                   
                            tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral));                        
                        }
                        else if (initialLeft - leftIndexPointer > rightIndexPointer - initialRight) {
                            tentSide.put(1,Side.RIGHT);
                            tentTargetIndex.put(1,rightIndexPointer-1+leftCentral);
                            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral)!= 1){
                                targetAgtRowIndex++;
                            }                   
                            tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral));         
//                            tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer-1+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer-1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer-1+leftCentral)));
                        }
                        else{
                        //01 10 is found at equal distance on both sides
//                        if(rightIndexPointer-leftIndexPointer==2){
//                            tentTargetIndex.put(1,leftIndexPointer + 1);
//                            tentTargetID.put(1,p1.getValue(0, 0, leftIndexPointer + 1) == 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer + 1) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer + 1));
//                            tentSide.put(1,Side.LEFT);
//                        }else{
                            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer + 1+leftCentral)!= 1){
                                targetAgtRowIndex++;
                            }  
                            
                            tentTargetIndex.put(1,leftIndexPointer + 1+leftCentral);
//                            tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer + 1+leftCentral) == 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer + 1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer + 1+leftCentral)));
                            tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral));                        
                            tentSide.put(1,Side.LEFT);
                            
                            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral)!= 1){
                                targetAgtRowIndex++;
                            }                  
                            tentSide.put(2,Side.RIGHT);
                            tentTargetIndex.put(2,rightIndexPointer-1+leftCentral);
                            tentTargetID.put(2,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral));
//                        }
                        }
                    }
                }else if(rightOvertakeFound){
                    tentCurrentStategy=STRATEGY.OVERTAKE;
                    tentSide.put(1, Side.RIGHT);
                    tentTargetIndex.put(1,rightIndexPointer-1+leftCentral);
                    while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral)!= 1){
                        targetAgtRowIndex++;
                    }
                    tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral));
//                    tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer-1+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer-1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer-1+leftCentral)));
                }else{
                //when no 10 or 01 found on both sides, follow. No side is specified for following 
                    tentCurrentStategy=STRATEGY.FOLLOW;                                       
                    if(initialLeft-leftIndexPointer<rightIndexPointer-initialRight){
                        tentTargetIndex.put(1,leftIndexPointer+1+leftCentral);
                        while(targetAgtRowIndex< p1.getPattern()[0].length -1&& p1.getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral)!= 1){
                            targetAgtRowIndex++;
                        }
                        tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral));     
//                        tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer+1+leftCentral) == 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer + 1+leftCentral)));
                    }else if(initialLeft-leftIndexPointer>rightIndexPointer-initialRight){
                        tentTargetIndex.put(1,rightIndexPointer-1+leftCentral);
                        while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral)!= 1){
                                targetAgtRowIndex++;
                            }                  
                        tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral));
//                        tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer-1+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer-1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer-1+leftCentral)));
                    }else{
                        //randomly choose a column to follow?
                        int randomFollowColumn = leftIndexPointer+1 + RVOModel.publicInstance.random.nextInt(rightIndexPointer-1-leftIndexPointer-1+1)+leftCentral;
                        tentTargetIndex.put(1,randomFollowColumn);
                        while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, randomFollowColumn)!= 1){
                                targetAgtRowIndex++;
                            }                  
                        tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, randomFollowColumn));
                        
//                        tentTargetID.put(1,(p1.getValue(0, 0, randomFollowColumn)==1? wm.getVision().getSpacepattern_agt().getValue(0, 0, randomFollowColumn):wm.getVision().getSpacepattern_agt().getValue(0, 1, randomFollowColumn)));
                    //Bug fixed, when trying to find 10 or 01 for overtake failed, resort to follow and in this case, the leftIndexPointer and rightIndexPointer already going out of the actual cental boundary already
                    }
                }
            }//end of overtakeFlag equal to true, which means has the intention to overtake
            else{
                tentCurrentStategy=STRATEGY.FOLLOW;
                if(midAgt_odd==null){
                    tentTargetIndex.put(1,midIndex);
                    tentTargetID.put(1,midAgtId);
                }else if(midAgt==null){
                    tentTargetIndex.put(1,midIndex_odd);
                    tentTargetID.put(1,midAgtId_odd);
                }else{
//                    int randomChance = RVOModel.publicInstance.random.nextInt(2);
//                    if(randomChance==1){
                        tentTargetIndex.put(1, midIndex);
                        tentTargetID.put(1,midAgtId);
                        tentTargetIndex.put(2,midIndex_odd);
                        tentTargetID.put(2,midAgtId_odd);
//                    }else{
//                        tentTargetIndex.put(1,midIndex_odd);
//                        tentTargetID.put(1,midAgtId_odd);
//                        tentTargetIndex.put(2, midIndex);
//                        tentTargetID.put(2,midAgtId);
//                    }
                }
            }
        }//end of relative faster than the one in front of me blocking the way.
        else{
            // if I am relatively slow, no strategy is matched if the center is 1
            tentCurrentStategy=null; //if I am relatively slow, non-strategic
        }
    }
    
    private void initialMatch_CenterNegativeOne(ArrayList<Integer> centralFuzzyColValues, STPattern p1, int leftCentral){
        int leftIndexPointer;
        int rightIndexPointer;
        
        int initialLeftPointer;
        int initialRightPointer;
        
        if(midIndex_odd<0){
            initialLeftPointer = midIndex-leftCentral; //initialLeft pointer is the index in centralArray
            leftIndexPointer= initialLeftPointer;
            initialRightPointer = midIndex-leftCentral;
            rightIndexPointer = initialRightPointer;
        }else{
            initialLeftPointer = midIndex_odd-leftCentral;
            leftIndexPointer= initialLeftPointer;
            initialRightPointer = midIndex-leftCentral; 
            rightIndexPointer = initialRightPointer;
        }
        boolean leftFollowFound = false; //assume follow to avoid is better than direct avoid
        boolean rightFollowFound = false;
            
        //search in left half
        while(leftIndexPointer>=0){
            int currentColValue=centralFuzzyColValues.get(leftIndexPointer);
            if(currentColValue==0){
                leftFollowFound=false;
                break;
            }else if(currentColValue==1){
                leftFollowFound=true; // _ 1 -1 case, just follow the 1, after approach lane, finish follow, then to make new decision on whether can overtake the 1
                break;
            }else if(currentColValue==-9){
                leftFollowFound = false;
                break;
            }
            else{
               leftIndexPointer--;
               leftFollowFound=false;
            }
        }
                 //search in right half
        while(rightIndexPointer<centralFuzzyColValues.size()){
            int currentColValue=centralFuzzyColValues.get(rightIndexPointer);
            if(currentColValue==0){
                rightFollowFound=false;
                break;
            }else if(currentColValue==1){
                rightFollowFound=true;
                break;
            }else if(currentColValue==-9){
                rightFollowFound = false;
                break;
            }
            else{ //include (currentColValue==-1) and other value for static obstacles
                rightFollowFound=false;
                rightIndexPointer++;
            }
        }
        
        int targetAgtRowIndex =0;
        if(leftFollowFound){ //found 0-1 on the left half
            tentCurrentStategy=STRATEGY.FOLLOW;
            if(!rightFollowFound){
                tentTargetIndex.put(1,leftIndexPointer+leftCentral); //return the column index of 1, not 0
                while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+leftCentral)!= 1){
                    targetAgtRowIndex++;
                }
                tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+leftCentral));
                    
//                tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer+leftCentral)));
            }else{
                if(initialLeftPointer-leftIndexPointer < rightIndexPointer-initialRightPointer){
                    tentTargetIndex.put(1,leftIndexPointer+leftCentral);
                    while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+leftCentral)!= 1){
                        targetAgtRowIndex++;
                    }
                    tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+leftCentral));
//                    tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer+leftCentral) == 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer+leftCentral)));
                }else if(initialLeftPointer- leftIndexPointer > rightIndexPointer - initialRightPointer){
                    tentTargetIndex.put(1,rightIndexPointer+leftCentral);
                    while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer+leftCentral)!= 1){
                        targetAgtRowIndex++;
                    }
                    tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer+leftCentral));
//                    tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer+leftCentral)));
                }
                else{
                    //both left, right found group can follow at the same angle deviation to avoid the current -1
                    //currently set it to random follow one side, but can create a function to calculate the energy consumed for follow either side so that choose the side with less energy consumption to follow
                    if(wm.getMyAgent().getMySpace().getRvoModel().random.nextBoolean()){
                        tentTargetIndex.put(1,leftIndexPointer+leftCentral);
                        while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+leftCentral)!= 1){
                            targetAgtRowIndex++;
                        }
                        tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+leftCentral));
//                        tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer+leftCentral ) == 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer+leftCentral)));
                    }else{
                        tentTargetIndex.put(1,rightIndexPointer+leftCentral);             
                        while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer+leftCentral)!= 1){
                            targetAgtRowIndex++;
                        }
                        tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer+leftCentral));
//                        tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer+leftCentral)));
                    }
                }
            }
        }else if(rightFollowFound){
            tentCurrentStategy=STRATEGY.FOLLOW;
            tentTargetIndex.put(1,rightIndexPointer+leftCentral);
            
            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer+leftCentral)!= 1){
                targetAgtRowIndex++;
            }
            tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer+leftCentral));         
//            tentTargetID.put(1,p1.getValue(0, 0, rightIndexPointer+leftCentral)== 1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer+leftCentral));
        }else{
            //center is -1 and there is no 1 to follow at either side
            tentCurrentStategy=STRATEGY.AVOID;                                       
            if(initialLeftPointer-leftIndexPointer<rightIndexPointer-initialRightPointer){
                tentSide.put(1,Side.LEFT);
                tentTargetIndex.put(1,leftIndexPointer+1+leftCentral);
                while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral)!= -1){
                    targetAgtRowIndex++;
                }
                tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral));
//                tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer+1+leftCentral) == -1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer + 1+leftCentral)));
            }else if(initialLeftPointer-leftIndexPointer>rightIndexPointer-initialRightPointer){
                tentSide.put(1,Side.RIGHT);
                tentTargetIndex.put(1,rightIndexPointer-1+leftCentral);
                while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral)!= -1){
                    targetAgtRowIndex++;
                }
                tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral));
//                tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer-1+leftCentral)== -1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer-1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer-1+leftCentral)));
            }else{
                if(leftIndexPointer<0 && rightIndexPointer>centralFuzzyColValues.size()-1){
                    tentCurrentStategy=null; //instinctive reaction, let rvo to handle if all the central is filled with -1
                }else{
                    //consider both potential avoid target in tentative strategy information tager
//                        if(RVOModel.publicInstance.random.nextBoolean()){
                            tentSide.put(1,Side.LEFT);
                            tentTargetIndex.put(1,leftIndexPointer+1+leftCentral);
                            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral)!= -1){
                                targetAgtRowIndex++;
                            }
                            tentTargetID.put(1,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, leftIndexPointer+1+leftCentral)); 
//                            tentTargetID.put(1,(p1.getValue(0, 0, leftIndexPointer+1+leftCentral) == -1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer+1+leftCentral)));

                            tentSide.put(2,Side.RIGHT);
                            tentTargetIndex.put(2,rightIndexPointer-1+leftCentral);
                            while(targetAgtRowIndex< p1.getPattern()[0].length -1 && p1.getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral)!= -1){
                                targetAgtRowIndex++;
                            }
                            tentTargetID.put(2,wm.getVision().getSpacepattern_agt().getValue(0, targetAgtRowIndex, rightIndexPointer-1+leftCentral));
//                            tentTargetID.put(2,(p1.getValue(0, 0, rightIndexPointer-1+leftCentral)== -1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer-1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer-1+leftCentral)));
//                        }else{
//                            tentSide.put(2,Side.LEFT);
//                            tentTargetIndex.put(2,leftIndexPointer+1+leftCentral);
//                            tentTargetID.put(2,(p1.getValue(0, 0, leftIndexPointer+1+leftCentral) == -1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, leftIndexPointer+1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, leftIndexPointer+1+leftCentral)));

//                            tentSide.put(1,Side.RIGHT);
//                            tentTargetIndex.put(1,rightIndexPointer-1+leftCentral);                      
//                            tentTargetID.put(1,(p1.getValue(0, 0, rightIndexPointer-1+leftCentral)== -1 ? wm.getVision().getSpacepattern_agt().getValue(0, 0, rightIndexPointer-1+leftCentral) : wm.getVision().getSpacepattern_agt().getValue(0, 1, rightIndexPointer-1+leftCentral)));    
//                        }
                }
            }
        }
    }//end of function        
    
    private void initialMatch_AvoidCross(STPattern p1) {
        int leftIndexPointer;
        int rightIndexPointer;

        int initialLeftPointer;
        int initialRightPointer;

        if(midIndex_odd<0){
            initialLeftPointer = midIndex; //initialLeft pointer is the index in centralArray
            leftIndexPointer= initialLeftPointer;
            initialRightPointer = midIndex;
            rightIndexPointer = initialRightPointer;
        }else{
            initialLeftPointer = midIndex_odd;
            leftIndexPointer= initialLeftPointer;
            initialRightPointer = midIndex; 
            rightIndexPointer = initialRightPointer;
        }
        
        boolean crossLeftInCenter = false; //assume follow to avoid is better than direct avoid
        boolean crossRightInCenter = false;
            
        //search in left half
        while(leftIndexPointer>=0){
            int currentColValue=fuzzyColumnValue(p1, leftIndexPointer);
            if(currentColValue==2){
                crossLeftInCenter=true;
                break;
            }
            else{
               leftIndexPointer--;
//               crossLeftInCenter=false;
            }
        }
                 //search in right half
        while(rightIndexPointer<p1.getPattern()[0][0].length){
            int currentColValue=fuzzyColumnValue(p1, rightIndexPointer);
            if(currentColValue==-2){
                crossRightInCenter=true;
                break;
            }
            else{ //include (currentColValue==-1) and other value for static obstacles
//                crossRightInCenter=false;
                rightIndexPointer++;
            }
        }
        
        if(crossLeftInCenter){
//            tentCurrentStategy = STRATEGY.AVOID;
//            tentSide.put(1, Side.LEFT);
//            tentTargetIndex.put(1, leftIndexPointer);
//            int tgtRowIndex = 0;
//            for(int i=0;i<3;i++){
//                if(p1.getValue(0, i, leftIndexPointer)==2){
//                    tgtRowIndex=i;
//                    break;
//                }
//            }
//            tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, tgtRowIndex, leftIndexPointer));
            wm.getMyAgent().deviatePrefVelocity(45);
            wm.getMyAgent().acceleratePrefVelocity(2);
        }else if(crossRightInCenter){
//            tentCurrentStategy = STRATEGY.OVERTAKE;
//            tentSide.put(1, Side.LEFT);
//            tentTargetIndex.put(1, rightIndexPointer);
//            int tgtRowIndex = 0;
//            for(int i=0;i<3;i++){
//                if(p1.getValue(0, i, rightIndexPointer)==-2){
//                    tgtRowIndex=i;
//                    break;
//                }
//            }
//            tentTargetID.put(1, wm.getVision().getSpacepattern_agt().getValue(0, tgtRowIndex, rightIndexPointer));
            wm.getMyAgent().deviatePrefVelocity(-45);
            wm.getMyAgent().acceleratePrefVelocity(0.0001);
        }
        
    }

    /*
     * given a STP (only the current frame is needed), and a column index number, return the fuzzy column value based on the logic value of two rows of that column
     */
    private int fuzzyColumnValue(STPattern p1,int colIndex){
        int value = -9; //arbitratry value first
        int numOfRowToCommit = 2;
        switch(RVOModel.publicInstance.random.nextInt(2)){
            case 0: 
                numOfRowToCommit = 2;
                break;
            case 1:
                numOfRowToCommit = 3;
                break;
            default: break;
        }
        int absoluteSum=0;
        for(int i=0; i<numOfRowToCommit;i++){
            absoluteSum+=Math.abs(p1.getValue(0, i, colIndex));
            if(p1.getValue(0, i, colIndex)==0){
                continue;
            }
            else if(p1.getValue(0, i, colIndex)==1){
                value=1;
                break;
            }else if (p1.getValue(0, i, colIndex)==-1){
                value=-1;
                break;
            }
//            else if(p1.getValue(0, i, colIndex)==2){
//                if(i<=1){
//                   value=2;
//                }
//                break;
//            }else if(p1.getValue(0, i, colIndex)==-2){
//                if(i<=1){
//                    value=-2;
//                }
//                break;
//            }
        }
        if(absoluteSum==0)
                value=0;
//        
//        if(p1.getValue(0, 0, colIndex)==0 && p1.getValue(0, 1, colIndex)==0 && p1.getValue(0, 2, colIndex)==0){
//            value=0;
//        }
//        else if(p1.getValue(0, 0, colIndex)==1 || (p1.getValue(0, 0, colIndex)==0 && p1.getValue(0, 1, colIndex)==1) || (p1.getValue(0, 0, colIndex)==0 && p1.getValue(0,1, colIndex)==0 && p1.getValue(0, 2, colIndex)==1)){
//            value = 1;
//        }
//        else if(p1.getValue(0, 0, colIndex)==-1 || (p1.getValue(0, 0, colIndex)==0 && p1.getValue(0, 1, colIndex)==-1)|| (p1.getValue(0, 0, colIndex)==0 && p1.getValue(0,1, colIndex)==0 && p1.getValue(0, 2, colIndex)==-1) ){
//            value = -1;
//        }
        return value;
    }
        
    
    
    /*
     * when tentCurrentStrategy == O, F or A, then comes into this function
     * 1. Set verifyVelocity according to tentStrategy, target and side
     * 2. Set predicted frames of STP
     * 3. Match STP to find out whether it meets the commitment level of the agent to match certain spatial patterns(similar to check exepctancy at one frame) along temporal dimension 
     */
    private void verifyMatching(){
        boolean matched = false;
        for(int i=0; i<tentTargetID.size();i++){                                                                                                             //1. set verifyVelocity
            int potentialTargetId = tentTargetID.pollFirstEntry().getValue();
            int potentialTargetIndex = tentTargetIndex.pollFirstEntry().getValue();
            Side potentialSide=null;
                 
            Vector2d verifyVelocity = new Vector2d(wm.getMyAgent().getVelocity());
            double rotateClockwiseAngle = 0.0;
            if(midIndex_odd<0){
                rotateClockwiseAngle = (potentialTargetIndex - midIndex) * wm.getVision().getAngle();
            }else{
                rotateClockwiseAngle = (potentialTargetIndex - midIndex_odd - 0.5) * wm.getVision().getAngle();
            }
            verifyVelocity=utility.Geometry.helpRotate(verifyVelocity, rotateClockwiseAngle);  //this verifyVelocity towards the target with my velocity

            switch(tentCurrentStategy){
                case FOLLOW:      //no need to verify the tentative follow strategy, verifyVelocity towards the target
                    matched = true;
                    break;
                case OVERTAKE:    //verify the tentative overtake strategy
                    //verify velocity is set identical as the target 1, in order to maintain the vision layout in predicted frames
                    //predicted time for catching up is determined by the relative velocity towards the 0 beside the target
                    //based on the predicted t, can set the pf in stp accordingly
//                        for(int j=0;j<tentSide.size();j++){
//                            potentialSide = tentSide.pollFirstEntry().getValue();
                        potentialSide = tentSide.pollFirstEntry().getValue();
                        if(potentialSide==Side.LEFT){
                            verifyVelocity=utility.Geometry.helpRotate(verifyVelocity, -1 * wm.getVision().getAngle());
                        }else{
                            verifyVelocity=utility.Geometry.helpRotate(verifyVelocity, wm.getVision().getAngle());
                        }             
                        RVOAgent potentialTarget_overtake = wm.getAgent(neighbor, potentialTargetId);
                        double alpha = verifyVelocity.angle(potentialTarget_overtake.getVelocity());

                        double a= 1;
                        double b= -2 * Math.cos(alpha) * potentialTarget_overtake.getVelocity().length();
                        double c= Math.pow(potentialTarget_overtake.getVelocity().length(),2)-Math.pow(verifyVelocity.length(), 2);
                        double relativeSpeedToSpace = (Math.sqrt(Math.pow(b, 2)- 4*a*c)-b)/(2*a);

                        double distanceToPotentialTarget = wm.getMyAgent().getMyPositionAtEye().distance(potentialTarget_overtake.getCurrentPosition());

                        int T_phase1= (int)Math.ceil(distanceToPotentialTarget / relativeSpeedToSpace / PropertySet.TIMESTEP);
                        wm.getVision().setSpacePattern(1, Math.min(wm.getVision().getPf(), T_phase1), potentialTarget_overtake.getVelocity());                 //2. set STP predicted frames  , according to the same velocity as the target  
                        matched = matchForOvertake(potentialTargetIndex,potentialSide, Math.min(wm.getVision().getPf(), T_phase1));                                                                                            //3. match STP for overtake
//                            if(matched) break;
//                        }
                    break;
                case AVOID:       //verify the tentative avoid strategy, in the case both side of the target is tentative, verify will choose one side                   

                    //to avoid deadlock
//                        matched = true;
                    potentialSide = tentSide.pollFirstEntry().getValue();
                    RVOAgent potentialTarget_avoid = wm.getAgent(neighbor, potentialTargetId);
                    boolean isLeft=false;
                    switch(potentialSide){
                        case LEFT: 
                            isLeft=true;
                            break;
                        case RIGHT:
                            isLeft =false;
                            break;
                        default: break;
                    }

                    if(potentialSide == Side.LEFT){
                        verifyVelocity=utility.Geometry.helpRotate(verifyVelocity, -1 * wm.getVision().getAngle());
                    }else{
                        verifyVelocity=utility.Geometry.helpRotate(verifyVelocity, wm.getVision().getAngle());
                    }

                    double alpha_avoid = verifyVelocity.angle(potentialTarget_avoid.getVelocity());
                    double a_1= 1;
                    double b_1= -2 * Math.cos(alpha_avoid) * potentialTarget_avoid.getVelocity().length();
                    double c_1= Math.pow(potentialTarget_avoid.getVelocity().length(),2)-Math.pow(verifyVelocity.length(), 2);
                    double relativeSpeedToSpace_avoid = (Math.sqrt(Math.pow(b_1, 2)- 4*a_1*c_1)-b_1)/(2*a_1);

                    double distanceToPotentialTarget_avoid = wm.getMyAgent().getMyPositionAtEye().distance(potentialTarget_avoid.getCurrentPosition()); //distance for layer 0 of attention range

                    int T_avoid= (int)Math.ceil(distanceToPotentialTarget_avoid / relativeSpeedToSpace_avoid / PropertySet.TIMESTEP);
                    wm.getVision().setSpacePattern(1, Math.min(wm.getVision().getPf(), T_avoid), potentialTarget_avoid.getVelocity());  

                    matched = matchForOvertake(potentialTargetIndex,potentialSide,Math.min(wm.getVision().getPf(), T_avoid));


                    if(((WorkingMemory)potentialTarget_avoid.getVelocityCalculator()).getDecision().getTargetAgentID()== wm.getMyAgent().getId()
                            && ((WorkingMemory)potentialTarget_avoid.getVelocityCalculator()).getDecision().getCurrentStrategy() == STRATEGY.AVOID
                            && ((WorkingMemory)potentialTarget_avoid.getVelocityCalculator()).getDecision().isLeft()!= isLeft
                    ){
                       matched = false; //deliberately break the deadlock 
                    }
                    break;
                default: break;
            }
            if(matched) {
                currentStrategy = tentCurrentStategy;
                targetID = potentialTargetId;
                if(currentStrategy!=STRATEGY.FOLLOW){
                    switch(potentialSide){
                        case LEFT: left = true; break;
                        case RIGHT: left = false; break;
                        default: break;
                    }
                }
                return;
            }
//            }
        }
    }//end of function verifyMatching
    
    private boolean matchForOvertake(int tentTargetColumnIndex, Side tentSide, int maxFrameForCatchUp){
        int matchFrame = 0;
        
        
        
        
        int requiredMatch = maxFrameForCatchUp;
        switch(wm.getMyAgent().getCommitementLevel()){
            case HIGHCOMMITMENT:
                requiredMatch *= 0.9; 
                break;
            case MIDCOMMITMENT:
                requiredMatch *= 0.5; 
                break;
            case LOWCOMMITMENT:
                requiredMatch *= 0.2;  // once see the chance now, just start
                break;
            default: 
                break;              
        }
        int spaceColumnIndex = tentTargetColumnIndex;
        if(tentSide==Side.LEFT) spaceColumnIndex--;
        else if(tentSide == Side.RIGHT) spaceColumnIndex++;
        
        for(int i = 1; i<=maxFrameForCatchUp;i++){
            if(wm.getVision().getSpacepattern().getValue(i, 0, spaceColumnIndex)==0)
                matchFrame++;
        }       
        
        if(matchFrame>=requiredMatch){
//            targetID = tentTargetID;
            instructedTime = matchFrame;
            return true;
        }
        return false;
    }
    
    
    /*
     * function to check the sustainability of the spatial pattern required for overtake over the number of frames as required
     */
    private int checkForOvertake(int ptIndex, STPattern p1, STPattern p1_id, boolean fromLeft, int maxMatch) {
        int matchedFrames = 0;
        //overtake from left
        int ptId = p1_id.getValue(0, 0, ptIndex);
        
        if(fromLeft){     
             for(int j=0; j< maxMatch; j++){
                //look for coditional "01" (the constitute for Overtake STP) in different slices from p1
                 int ptCIndexNew = p1_id.returnColumnIndex(ptId, fromLeft, j);   
                 if(ptCIndexNew<0){
                     break;
                 }
                 if(p1.getValue(j, 0, ptCIndexNew-1)==0 && p1.getValue(j, 0, ptCIndexNew)== 1
                    && p1.getValue(j, 1, ptIndex-1)==0 
//                    &&  p1.getValue(j, 1, ptIndex)!= -1
                 ){
                        matchedFrames++;
                 }else{
                    break;
                 }
            }                 
        }else{  
             for(int j=0; j< maxMatch; j++){
                //look for coditional "10" (the constitute for Overtake STP) in different slices from p1
                 int ptCIndexNew = p1_id.returnColumnIndex(ptId, fromLeft, j);   
                 if(ptCIndexNew<0){
                     break;
                 }
                 if(p1.getValue(j, 0, ptCIndexNew+1)==0 && p1.getValue(j, 0, ptCIndexNew)== 1
                        && p1.getValue(j, 1, ptIndex+1)==0 
//                         &&  p1.getValue(j, 1, ptIndex)!= -1
                 ){
                        matchedFrames++;
                 }else{
                    break;
                 }
            }
        }
        return matchedFrames;
    }
    
    /*
     * Based on the current selected strategy to specify target and left
     * based on match*RequiredMax = matched number of frames, can calculate the time, where the existing perceived spatial pattern exist for
     * then can specify T (: from now until T, have to finish the potential strategy, then increase speed accordingly)
     */
    private void findTarget(STRATEGY currentStrategy, int[] matchedFrames) {        
        if(currentStrategy==STRATEGY.AVOID){
           int leftEdgeIndex = -1;
           int rightEdgeIndex = numOfVisualColumns;
            
           //find left edge index of the potential avoiding group
           for(int i=midIndex; i>1; i--){
               if(wm.getVision().getSpacepattern().getValue(0, 0, i)== -1
               || (wm.getVision().getSpacepattern().getValue(0, 0, i)==0 && wm.getVision().getSpacepattern().getValue(0, 1, i)== -1)){
                    leftEdgeIndex = i;
               }
           }
            //find the right potential edge of the avoiding group
           for(int j=midIndex;j<numOfVisualColumns-2;j++){
               if(wm.getVision().getSpacepattern().getValue(0, 0, j)== -1
               || (wm.getVision().getSpacepattern().getValue(0, 0, j)==0 && wm.getVision().getSpacepattern().getValue(0, 1, j)== -1)){
                    rightEdgeIndex = j;
               }
           }
           //prefer left
           int edgeIndex = leftEdgeIndex;
           left = true;

           if(rightEdgeIndex-midIndex<midIndex-leftEdgeIndex){
               edgeIndex= rightEdgeIndex;
               left = false;
           }
           else if(rightEdgeIndex-midIndex==midIndex-leftEdgeIndex){
               if((wm.getVision().getSpacepattern().getValue(0, 0, leftEdgeIndex)== -1 && wm.getVision().getSpacepattern().getValue(0, 0, leftEdgeIndex-1)== 0) ||
                  (wm.getVision().getSpacepattern().getValue(0, 0, leftEdgeIndex)== 0 && wm.getVision().getSpacepattern().getValue(0, 1, leftEdgeIndex)== -1 && wm.getVision().getSpacepattern().getValue(0, 1, leftEdgeIndex-1)== 0) ){
                   edgeIndex=leftEdgeIndex;
               }
               else if((wm.getVision().getSpacepattern().getValue(0, 0, rightEdgeIndex)== -1 &&wm.getVision().getSpacepattern().getValue(0, 0, rightEdgeIndex+1)==0) ||
                       (wm.getVision().getSpacepattern().getValue(0, 0, rightEdgeIndex)== 0 && wm.getVision().getSpacepattern().getValue(0, 1, rightEdgeIndex)== -1) &&(wm.getVision().getSpacepattern().getValue(0, 1, rightEdgeIndex+1)== 0) ){
                   edgeIndex=rightEdgeIndex;
                   left = false;
               }else{
                   int densityLeft = 0;
                   int densityRight = 0;
                   for(int k=leftEdgeIndex; k>= Math.max(leftEdgeIndex-2,0); k--)
                       for(int l = 1; l<=2;l++){
                           //for 1 at row 1 has more attention impact  than a 1 in row 2, the significance can is scaled by abs(1.7-j)
                           densityLeft += Math.abs(wm.getVision().getSpacepattern().getValue(0, l, k))* Math.abs(l-1.7);
                   }
                   for(int m=rightEdgeIndex; m<=Math.min(rightEdgeIndex+2,numOfVisualColumns-1); m++)
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
           if(edgeIndex>=0 && edgeIndex<=numOfVisualColumns-1){
               if(wm.getVision().getSpacepattern().getValue(0, 0, edgeIndex)==-1){
                    targetID = wm.getVision().getSpacepattern_agt().getValue(0, 0, edgeIndex);
               }else{
                    targetID = wm.getVision().getSpacepattern_agt().getValue(0, 1, edgeIndex);
               }
               instructedTime= matchedFrames[0];
           }
           else System.out.println("Error: steering strategy AVOID was selected but without valid target to avoid");
           return;
        } //end of SIDE-AVOID
//        else if(currentStrategy==STRATEGY.MOVE){        
//            targetID=-1;
//            targetIndex=-1;
//            instructedTime=matchedFrames[2];
//            return;
//        }
        else if(currentStrategy==STRATEGY.OVERTAKE){
            //when the match of "OVERTAKE" does not meet the number of frames as required by commitment
            instructedTime = matchedFrames[1];
            
            Point2d myPos = wm.getMyAgent().getCurrentPosition();
            RVOAgent targetAgent = wm.getAgent(wm.getVision().getSensedAgents(), targetID);
            Point2d targetPos = targetAgent.getCurrentPosition();
            double distanceToTarget = myPos.distance(targetPos);
            
            //if using the max speed of the agent still cannot reach where the target is (estimate the distance for cathing up)
            if((wm.getMyAgent().getMaxSpeed()- targetAgent.getVelocity().length()) * instructedTime * PropertySet.TIMESTEP < 
                    distanceToTarget - (2+wm.getMyAgent().getPersonalSpaceFactor())* RVOAgent.RADIUS){
                currentStrategy= STRATEGY.FOLLOW;
                System.out.println("The perceived spatial pattern for OVERTAKE does not exist long enough for me to overtake, FOLLOW instead");
                findTarget(currentStrategy, matchedFrames); //call this function again for follow
            }else{
                System.out.println("SELECT to OVERTAKE");
                return;
            }
        }
        else if(currentStrategy == STRATEGY.FOLLOW){
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
               targetID = p1_agt.getValue(0, 0, midIndex);
                //side to follow based on the heuristic of signifiedDensity value
               double valueSignificance;

               for(int i=0; i<p1.getPattern()[0].length; i++)
                    for(int j=1; j<3;j++){
                        for(int k=Math.max(0,midIndex-2);k<midIndex;k++){
                            //a -1 gives a significance of 1.5 of that of a 1
                            //a -1 or 1 at row 1 gives a significance of 2.0 that of at row 2
                            valueSignificance = 1.0;
                            if(p1.getValue(i, j, k)==-1) valueSignificance = 1.5;
                            signifiedDensity_left += Math.abs(p1.getValue(i, j, k))* valueSignificance * (3-j);
                        }
                        for(int l=midIndex;l<Math.min(midIndex+2,numOfVisualColumns-1);l++){
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
    
}