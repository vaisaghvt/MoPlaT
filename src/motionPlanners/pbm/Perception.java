package motionPlanners.pbm;

//import java.awt.Point;
import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import environment.Obstacle.RVO1Obstacle;
import environment.Obstacle.RVO2Obstacle;
import environment.Obstacle.RVOObstacle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.util.Bag;
import utility.Geometry;
import utility.PrecisePoint;

public class Perception {
    public STPattern spacepattern; //[no of predicted frames]([no of attenuation levels for attention][number of visionary section])
    public STPattern spacepattern_agtID; //predicted frames . record space is occupied by which agt   
    private int pf;
    private double visionRange; //170degree
    private double visionLength; //to be around 3.6*4 meters, to get bag, it is used in terms attention_multi[2]
    private double angle; //in degree of each visionary section, it should be dynamic as the distance to the "attended" agent in the middle changes
    
    //in terms of degree
    private double center;
    private double rightmost;
    private double leftmost;
    
    private WorkingMemory wm;
      
    //in terms of normalized vector
    private Vector2d vec_C; 
    private Vector2d vec_L; 
    private Vector2d vec_R;
    
    boolean hasNeighbourForSteering;
    boolean hasNeighbourForReaction;
    
    // A flag used to indicate when there is strategy selected.
//    private boolean strategySelected;              //shifted to wm

    //Added for sort the sensedAgent in the order of their distances to myAgent
    TreeMap<Double, RVOAgent> obsesAgents_ForSteering;
    
    //Added for record of those sensedAgents in the very near range of me, which cannot be handled through steering, but instinctive reactions
    TreeMap<Double, RVOAgent> obsesAgents_ForReaction;
     
    //new added to extend attention with different attenuation levels
    //e.g., {1,2,4...}
    //number of attenuation levels can be specified by different agents
    // we use 3 levels
    private double[] attention_multi;
    
    protected Bag sensedAgents;
    
    protected Bag sensedObstacles;
    
    
    public Vector2d getVec_C() {
        return vec_C;
    }

    public Vector2d getVec_L() {
        return vec_L;
    }

    public Vector2d getVec_R() {
        return vec_R;
    }
    
    
    public Bag getSensedObstacles(){
        return sensedObstacles;
    }
    
    public Bag getSensedAgents(){
        return sensedAgents;
    }
    
    public int getValueInSTP(int frame, int row, int column){
        return spacepattern.getValue(frame, row, column);
    }
    
    public int getValueInSTP_AgtID(int frame, int row, int column){
        return spacepattern_agtID.getValue(frame, row, column);
    }
    
    public TreeMap<Double,RVOAgent> getObsesAgents_ForReaction(){
        return obsesAgents_ForReaction;
    }
    
    public TreeMap<Double,RVOAgent> getObsesAgents_ForSteering(){
        return obsesAgents_ForSteering;
    }
    
//    public boolean isStrategySelected() {
//        return strategySelected;
//    }
//
//    public void setStrategySelected(boolean strategySelected) {
//        this.strategySelected = strategySelected;
//    }

    public STPattern getSpacepattern() {
        return spacepattern;
    }

    public STPattern getSpacepattern_agt() {
        return spacepattern_agtID;
    }

    public int getPf() {
        return pf;
    }



    public static enum AttenuationLevel {
        NEAR, MIDDIUM, OUTBOUND
//        FAR
    }
    
    
    public int getNumColumnInSTP(){
        return spacepattern.getPattern()[0][0].length;
    }
//    public static double[] predefinedColumns ={21, 16.8, 16.8, 14, 14, 14, 14, 14, 16.8, 16.8, 21}; //the old pre-defined un-evenly distributed visual columns
   
    /*
    //the old constructor but more robust
    public Perception(int attenuationLevel, int pf, double bodyRadius, double distToObst, double vr, double vl, WorkingMemory wm) {
        angle = getAngle(bodyRadius, distToObst); //here, distToObst is some convenional value from literature review, say 1m, 1.5m, 2m, 2.5m, 3m, 3.5m ... not real time value
        attention_multi = new double[attenuationLevel];
        for (int i = 0; i < attenuationLevel; i++) {
            attention_multi[i] = Math.pow(2, i) * bodyRadius  / ( Math.sin((angle * Math.PI) / (2 * 180))); //make attention attenuation regress explentially
        }
        this.pf = pf;
        visionRange = vr;
        visionLength = vl;

        this.wm = wm; //pass by reference of the agent to the vision class to specify the relationship
        setVisualZone(wm.getMyAgent().getVelocity()); //to set vision center, leftmost and rightmost of vision at the current frame
        sensedAgents = null;
        sensedObstacles = null;
        
        spacepattern = new STPattern(pf, attenuationLevel,(int)(Math.ceil(visionRange / angle)));
        spacepattern_agtID = new STPattern(pf, attenuationLevel,(int)(Math.ceil(visionRange / angle)));

        strategySelected =  false;

        obsesAgents_ForSteering = new TreeMap<Double, RVOAgent>();
        obsesAgents_ForReaction = new TreeMap<Double, RVOAgent>();
    }
    
    //The new constructor for the new unevenly distributed columns, but hardcoded many parameters
    public Perception(int pf, WorkingMemory wm, double firstAttDist){
        angle = 2 * Math.asin((1+wm.getMyAgent().getPersonalSpaceFactor()) * wm.getMyAgent().getRadius() / firstAttDist ) 
                * Math.PI / 180;  //based on fistAttDist = 3.6, roughly around 7.02 * 2 = 14 degree
        attention_multi = new double[3];
        attention_multi[0] = firstAttDist; //now passed in as 3.6, the social space 
        attention_multi[1] = 2 * firstAttDist;
        attention_multi[2] = 4 * firstAttDist;
        this.pf = pf;
        visionRange = 180;
        visionLength = wm.getMyAgent().getRadius() * agent.RVOAgent.SENSOR_RANGE; //currently radius = 0.2 and SR is 80
        this.wm = wm;
        setVisualZone(wm.getMyAgent().getVelocity());  //currently passed in current velocity, but later can pass in the mentally simulated velocity
        sensedAgents = null;
        sensedObstacles = null;
        spacepattern = new STPattern(pf,3,11);
        spacepattern_agtID = new STPattern(pf,3,11);
        strategySelected =  false;
        obsesAgents_ForSteering = new TreeMap<Double, RVOAgent>();
        obsesAgents_ForReaction = new TreeMap<Double, RVOAgent>();
    }
    
    */
    
    
    /*
     * Created on Mar30,2012
     * to make vision dynamic according to density
     */
    public Perception(WorkingMemory wm){
        //initalization
        attention_multi = new double[2];
        attention_multi[0]=3.6; //default value
        attention_multi[1]=attention_multi[0]*2;
//        attention_multi[2]=attention_multi[0]*4;
        this.wm = wm;
        pf = 0;
        visionRange = 180;
        visionLength = wm.getMyAgent().getRadius() * agent.RVOAgent.SENSOR_RANGE;
        angle = calAngle(wm.getMyAgent().getRadius() * (1+wm.getMyAgent().getPersonalSpaceFactor()),attention_multi[0]);

        sensedAgents = null;
        sensedObstacles = null;
        this.setVisualZoneNew(wm.getMyAgent().getPrefVelocity()); //initialized based on prefVel

//        spacepattern = new STPattern();
//        spacepattern_agtID = new STPattern();
//        strategySelected = false;
        obsesAgents_ForSteering = new TreeMap<Double,RVOAgent>();
        obsesAgents_ForReaction = new TreeMap<Double, RVOAgent>();     
        hasNeighbourForSteering = false;
        hasNeighbourForReaction = false;
    }
    
    public double getVisualRange(){
        return visionRange;
    }
    
    public double getAttention_multi_Level1(){
        return attention_multi[0];
    }
    
    public double getAngle(){
        return angle;
    }
    
    public boolean getHasNeighbourForSteering(){
        return hasNeighbourForSteering;
    }
    
    public boolean getHasNeighbourForReaction(){
        return hasNeighbourForReaction;
    }
    
    /**
     * Local helper function to determine angle between this and target agent
     *
     * @param bodysize
     * @param distToObst
     * @return angle per visionary section in degree
     */
    private double calAngle(double bodyRadius, double distToObst) {
        double anglePerSection = visionRange; //default visionary section as 176 degree
        anglePerSection = 2 * Math.asin(bodyRadius /distToObst) * 180 / Math.PI;
        return anglePerSection;
    }

    /**
     * Local helper function to calculate the vision angle
     * at which the agent can perceive
     *
     */
    private void setVisualZone(Vector2d centerVel) {
        // center, leftmost, rightmost are in degree
        //This gives us the angle of the current selected velocity within [0,360]
        PbmVelocity pbmCenterVel = new PbmVelocity(centerVel);
        center = pbmCenterVel.getTheta();        
        rightmost = center - visionRange / 2;
        if (rightmost < 0) {
            rightmost += 360;
        }
        leftmost = (center + visionRange / 2) % 360;
    }
    
    /**
     *new function to use normalized vector2d for vec_C, vec_L and vec_R
     *
     */
    private void setVisualZoneNew(Vector2d centerVel) {
        vec_C = new Vector2d(centerVel.x,centerVel.y);
//        vec_C.normalize();
        
//        double lx = (vec_C.x * Math.cos(Math.PI/2)) - (vec_C.y * Math.sin(Math.PI/2));
//        double ly = (vec_C.x * Math.sin(Math.PI/2)) + (vec_C.y * Math.cos(Math.PI/2));
        vec_L = Geometry.helpRotate(vec_C, -visionRange/2);
        
//        double rx = (vec_C.x * Math.cos((-1)*Math.PI/2)) - (vec_C.y * Math.sin((-1)*Math.PI/2));
//        double ry = (vec_C.x * Math.sin((-1)*Math.PI/2)) + (vec_C.y * Math.cos((-1)*Math.PI/2));
        vec_R = Geometry.helpRotate(vec_C, visionRange/2);
                
    }

    /**
     * Local helper function to determines the index in the pattern of the
     * target agent
     * Last updated: nov 24, 2011, by hu nan
     * @param Point2d pos1 (me)
     * @param Point2d pos2 (agent)
     * @return an ArrayList of indeces that the agent has occupied in the unevenly distributed attention system of me
     */
//    private ArrayList<Integer> calColumnIndex(Point2d pos1, Point2d pos2) {
//        //in some cases, one agent may occupy several attention columns
//        ArrayList<Integer> columnIndex =  new ArrayList<Integer>();
//        
//        Vector2d p1top2 = new Vector2d(pos2);
//        p1top2.sub(pos1);
//        Vector2d vleftBoundary = new Vector2d(Math.cos(leftmost * Math.PI / 180), (-1) * Math.sin(leftmost * Math.PI / 180)); //the coordinate system for y is different
////        Vector2d rightBoundary = new Vector2d(Math.cos(rightmost * Math.PI / 180), (-1) * Math.sin(rightmost * Math.PI / 180));
//        Vector2d vCenter = new Vector2d(Math.cos(center * Math.PI / 180), (-1) * Math.sin(center * Math.PI / 180));
//        
//        //The circle is considered as myAgent's radius (1+* myPersonalSpace)
//        //this represent the visual impact of the size of the agent to me, the closer, the larger
//        
//        //Here, detected a problem on Dec 23, 2011.
//        //when the distance p1top2-radius is very small (almost the same as radius * (1+personalSpaceFactor)), the perceived angle is as large as to cover all attention columns
//        
//        double halfAngle = Math.asin(wm.getMyAgent().getRadius() * (wm.getMyAgent().getPersonalSpaceFactor()+1) /(p1top2.length()-wm.getMyAgent().getRadius())) * 180 / Math.PI;
//        
//        double angleToLeftBoundary = p1top2.angle(vleftBoundary) * 180 / Math.PI;
//        double angleToLeftBoundary_1 = angleToLeftBoundary - halfAngle;
//        if(angleToLeftBoundary_1<0) {
//            angleToLeftBoundary_1 = 0;
//        }
//        double angleToLeftBoundary_2 = angleToLeftBoundary + halfAngle;
//        if(angleToLeftBoundary_2>180) {
//            angleToLeftBoundary_2 = 180;
//        }
//        
//        double angleToVCenter = p1top2.angle(vCenter) * 180 / Math.PI;
//        
//        if( angleToVCenter <= visionRange / 2){ //the agent is within the vision range
//           //1. for evenly distributed attention sections:
////          columnIndex = (int)Math.floor(angleToLeftBoundary/angle);
//            
//            //2. for unevenly distributed ateention sections:
//            int i;
//            double countAngle=0;
//            for (i=0; i<Perception.predefinedColumns.length; i++){
//                countAngle+=predefinedColumns[i];
//                if(countAngle<angleToLeftBoundary_1){
//                    continue;
//                }
//                else if(countAngle>=angleToLeftBoundary_2){
//                    columnIndex.add(i);
//                    break;
//                }
//                else{            
//                    columnIndex.add(i);
//                    continue;
//                }
////                else break;
//            }      
//        } 
//        //else: the agent at position p2 is not within the vision range of me
//        return columnIndex;
//    }
    
        /*
     *The new calColumnIndex function using vec_C, vec_L and vec_R 
     */
        private ArrayList<Integer> calColumnIndexNew(Vector2d predictVec_C, Point2d pos1, Point2d pos2) {
        //in some cases, one agent may occupy several attention columns
        ArrayList<Integer> columnIndices =  new ArrayList<Integer>();
        
        Vector2d p1top2 = new Vector2d(pos2);
        p1top2.sub(pos1);
       
        Vector2d vCenter = new Vector2d(predictVec_C);
        Vector2d vleftBoundary = Geometry.helpRotate(predictVec_C, -visionRange/2); 
        //The circle is considered as tempAgent's radius (1+* myPersonalSpace)
        //this represent the visual impact of the size of the agent to me, the closer, the larger
        
        //Here, detected a problem on Dec 23, 2011.
        //when the distance p1top2-radius is very small (almost the same as radius * (1+personalSpaceFactor)), the perceived angle is as large as to cover all attention columns
        //now, the problem has been solved as only those absolute distance >0.45 will be included in the obsesAgent_ForSteering list
        
        double halfAngle = Math.asin(RVOAgent.RADIUS * (wm.getMyAgent().getPersonalSpaceFactor()+1) / p1top2.length()) * 180 / Math.PI;
        
        double angleToLeftBoundary = p1top2.angle(vleftBoundary) * 180 / Math.PI;
        double angleToLeftBoundary_1 = angleToLeftBoundary - halfAngle;
        if(angleToLeftBoundary_1<0) {
            angleToLeftBoundary_1 = 0;
        }
        double angleToLeftBoundary_2 = angleToLeftBoundary + halfAngle;
        if(angleToLeftBoundary_2>180) {
            angleToLeftBoundary_2 = 180;
        }
        
        double angleToVCenter = p1top2.angle(vCenter) * 180 / Math.PI;
        
        if( angleToVCenter <= visionRange / 2){ //the agent is within the vision range
           //1. for evenly distributed attention sections:
//          columnIndex = (int)Math.floor(angleToLeftBoundary/angle);
            int columnNum = (int)Math.floor(visionRange/angle);
            double restAngleHalf = (visionRange - columnNum*angle)/2;
            double countAngle=restAngleHalf;
            int i;
            for (i=0; i<columnNum; i++){
                countAngle+=angle;
                if(countAngle<=angleToLeftBoundary_1){
                    continue;
                }
                else if(countAngle>=angleToLeftBoundary_2){
                    columnIndices.add(i);
                    break;
                }
                else{            
                    columnIndices.add(i);
                    continue;
                }
//                else break;
            }      
        } 
        //else: the agent at position p2 is not within the vision range of me
        return columnIndices;
    }
    
    /**
     * Main method in vision system to update the local pattern data structure.
     * Called by this.execute()
         * parameter: set the frame from firstFrame to lastFrame according to the veolocity_verify
     * 
     */
    public void setSpacePattern(int firstFrame, int lastFrame, Vector2d velocity_verify) {//pass in a group of agents within the total vision range and visual distance at the current frame
        
          //---------------old spatialtemporal pattern specification based on the same current velocity of me--------------------------------------------
        for (int i = firstFrame; i < lastFrame + 1; i++){
            //predict me agent's position            
             RVOAgent me = wm.getMyAgent();

             PrecisePoint predictPos = new PrecisePoint(velocity_verify.getX(), velocity_verify.getY());
             //assume the velocity_verify is fixed for a period during this stage of strategy
             //could change it to certain cost function of time and the initial deviating angle later
             predictPos.scale(i * PropertySet.TIMESTEP);
             predictPos.add(me.getCurrentPosition());
             Point2d myNextPos = new Point2d(predictPos.toPoint());             
             
             Vector2d myNextPosAtEyeAlongVerifyVel = new Vector2d(velocity_verify);
             myNextPosAtEyeAlongVerifyVel.normalize();
             myNextPosAtEyeAlongVerifyVel.scale(RVOAgent.RADIUS);
             myNextPosAtEyeAlongVerifyVel.add(myNextPos);
             
             Point2d myNextPosAtEye = new Point2d(myNextPosAtEyeAlongVerifyVel.x,myNextPosAtEyeAlongVerifyVel.y);
             
             Vector2d verify_Left = Geometry.helpRotate(velocity_verify, -visionRange/2);
            
            //to set STP according to perceived obstacles------------------------------------------------------
            ArrayList<Line2D> obstacleLines = new ArrayList<Line2D>();
            for (Object tempObject : sensedObstacles) {
                RVO2Obstacle tempObstacle = (RVO2Obstacle)tempObject; //for rvo2obstacle, it is just a point2d (vertex)
//                if(tempObstacle.getVertices().size()==1){
//                    System.out.println("Specify at least 2 points for obstacles");
//                }else if(tempObstacle.getVertices().size()==2){
//                    obstacleLines.add(new Line2D.Double(tempObstacle.getVertices().get(0).x,tempObstacle.getVertices().get(0).y, tempObstacle.getVertices().get(1).x,tempObstacle.getVertices().get(1).y));
//                }else{
//                    //bug here! tempObstacle 
//                    for(int j=0;j<tempObstacle.getVertices().size();j++){
                        Point2d tempPoint1 = tempObstacle.getPoint();
                        Point2d tempPoint2 = tempObstacle.getNext().getPoint();
//                        if(j==tempObstacle.getVertices().size()-1){
//                            tempPoint2 = new Point2d(tempObstacle.getVertices().get(0));
//                        }else{
//                            tempPoint2 = new Point2d(tempObstacle.getVertices().get(j+1));
//                        }
                        obstacleLines.add(new Line2D.Double(tempPoint1.x, tempPoint1.y, tempPoint2.x, tempPoint2.y));
//                     }
//                }
            }
            for(int j=0;j<spacepattern.getPattern()[0][0].length;j++)
                for(int k=1; k>=0;k--){
                    Vector2d radial=Geometry.helpRotate(verify_Left, angle*j); //rotate the vector clockwise by a specific degree and return this vector
                    radial.normalize();
                    radial.scale(attention_multi[k]);
                    radial.add(me.getMyPositionAtEye());
                    Point2d p2 = new Point2d(radial.x,radial.y);
                    Point2d p1 = me.getMyPositionAtEye();
                    for(Object obj: obstacleLines){
                        Line2D obstacleLine = (Line2D)obj;
                        if (Geometry.lineSegmentIntersectionTest(p1, p2, new Point2d(obstacleLine.getX1(),obstacleLine.getY1()), new Point2d(obstacleLine.getX2(),obstacleLine.getY2()))){
                            if(spacepattern.getValue(i, k, j) == 0){
                                spacepattern.setValue(i, k, j, -9);
                            } //use -9 to indicate static obstacle at the section in a STP
                            
                        }
                    }
            }            
            //to set STP according to perceived agents-----------------------------------------------------------
            Set set = obsesAgents_ForSteering.entrySet();
            Iterator itr = set.iterator();
            while(itr.hasNext()){
                Map.Entry myEntry = (Map.Entry)itr.next(); //automatically sorted based on the distance of this neighbour to me
                RVOAgent agent = (RVOAgent) myEntry.getValue();
               
                Point2d agentNextPos = new Point2d(agent.getNextPosition(i)); //agent's next position is calculated based its current velocity

                //calculate relative distance between me and agt[i]
//                double relative_dist = myNextPos.distance(agentNextPos) - me.getRadius() - agent.getRadius()* me.getPersonalSpaceFactor();
                
                double eyeToCenter_dist = myNextPos.distance(agentNextPos) - me.getRadius();
//                double spatial_dist = eyeToCenter_dist - agent.getRadius()* me.getPersonalSpaceFactor();
                        
                if (agent != me) {
                   //To get 1 or -1
                    boolean samedirection = false;
                    if (sameDirection(agent.getVelocity(), velocity_verify)){
                        samedirection = true; //in future may change if acceleration !=0 any more
                    }
                    
                    //To get row index
                    AttenuationLevel att_level;
                    if (eyeToCenter_dist <= attention_multi[0]) {
                        att_level = AttenuationLevel.NEAR;
                    } else if (eyeToCenter_dist <= attention_multi[1]) {
                        att_level = AttenuationLevel.MIDDIUM;
                    } 
//                    else if (eyeToCenter_dist <= attention_multi[2]) {
//                        att_level = AttenuationLevel.FAR;
//                    } 
                    else {
                        att_level = AttenuationLevel.OUTBOUND;
                    }
                     
                    //To get column index
                    ArrayList<Integer> columnIndices = calColumnIndexNew(velocity_verify, myNextPosAtEye, agentNextPos);  //using the new calColumnIndex function, making use of vector rather than degree
                    
                    if(!columnIndices.isEmpty()){
                        int rowIndex = -1;
                        switch (att_level) {
                            case NEAR:
                                rowIndex = 0;
                                break;
                            case MIDDIUM:
                                rowIndex = 1;
                                break;
//                            case FAR:
//                                rowIndex = 2;
//                                break;
                            case OUTBOUND:
                                break;
                        }
                        if (rowIndex >= 0) {
                            //if the current value on this row and column in the array is -1, cannot set it back to 1
                            //this is to address the more importance of -1 in the visiual attention during decision making
                            for(int k=0; k<columnIndices.size(); k++){
                                int column=columnIndices.get(k);
                                if (samedirection && spacepattern.getValue(i, rowIndex, column)==0) {
                                    spacepattern.setValue(i, rowIndex, column, 1);
                                    spacepattern_agtID.setValue(i, rowIndex, column, agent.getId());
                                } else if(!samedirection && spacepattern.getValue(i, rowIndex, column)==0){
                                    spacepattern.setValue(i, rowIndex, column, -1);
                                    spacepattern_agtID.setValue(i, rowIndex, column, agent.getId()); 
                                }//end of else if
                            }//end of for
                        }//end of if(rowIndex>=0)
                    }//end of  if(!columnIndex.isEmpty())
                }//end of if (agent!=me)
            }// end of for(Object o : sensedAgents)
        }//end of for loop for predicted frames
        
        
    }

    /**
     * Main method called every timestep - passes in sensed agents and updates
     * the visual system, including the pattern and situational awareness
     * @param sensedAgents
     */
    public void execute(Bag sensedAgents, Bag sensedObstacles) {
        //to set the visual zone with the vec_C, vec_L and vec_R
        setVisualZoneNew(wm.getMyAgent().getVelocity());
        
        this.sensedAgents = sensedAgents;
        this.sensedObstacles = sensedObstacles;
        
        //reset TreeMap
        obsesAgents_ForSteering.clear();
        obsesAgents_ForReaction.clear();
        
        double minTTC = 9999; 
        int minTTCAgtId = -1;
        
        for (Object tempObject : sensedAgents) {
            RVOAgent tempAgent = (RVOAgent) tempObject;
            if(tempAgent.getId()!= wm.getMyAgent().getId()){
                double distanceToMe;
                if (!obsesAgents_ForSteering.containsValue(tempAgent)) {
                    Vector2d meToAgent = new Vector2d(tempAgent.getCurrentPosition());
                    meToAgent.sub(this.wm.getMyAgent().getMyPositionAtEye()); //bug fixed with my position at eye instead of myPosition
                    distanceToMe = meToAgent.length() - (1+wm.getMyAgent().getPersonalSpaceFactor())* RVOAgent.RADIUS;  //this distance is from my eye to the edge of personal space of the agent
                    //if the neighbour is around 0.33 meters away, then can plan steering strategy based on patterns (only 3 columns are formed in this case)
                    if(distanceToMe > 0){  
                        //only consider the ones in front of me according to my prefVelocity
                        if(meToAgent.angle(wm.getMyAgent().getVelocity())< Math.PI / 2){
                            obsesAgents_ForSteering.put((Double) distanceToMe, tempAgent); //inside the steering bag is the absolute distance
                        }
                        double TTC = Geometry.calcTTC(wm.getMyAgent().getCurrentPosition(),wm.getMyAgent().getVelocity(),wm.getMyAgent().getRadius() * (1+wm.getMyAgent().getPersonalSpaceFactor()),
                                tempAgent.getCurrentPosition(),tempAgent.getVelocity(), tempAgent.getRadius()*(1+tempAgent.getPersonalSpaceFactor()));
                        if(TTC<minTTC){
                            minTTC = TTC;
                            minTTCAgtId = tempAgent.getId();
                        }
                    }else{    
                        if(meToAgent.angle(wm.getMyAgent().getVelocity())< Math.PI / 2){
                            obsesAgents_ForReaction.put((Double) distanceToMe, tempAgent);
                        }
                    }
                }
            }
        }
      /*
        if(minTTCAgtId!=-1){
            pf= (int) Math.floor(minTTC/PropertySet.TIMESTEP);  //pf is set according to the nearest possible agent that may collide with me
        }else{
            pf = 0;
        }
      */
       //Mike suggests to make pf constant, e.g., the time for visual perception or anticipation time as talked about in the paper
        pf = (int) Math.ceil(3 / PropertySet.TIMESTEP); //assume 3seconds now
        
        
        
//        if (wm.isStrategyChanged()) { //if strategy is changed and need new decisions
        
        if(!obsesAgents_ForReaction.isEmpty()){
            hasNeighbourForReaction=true;
        }
        
        if(!obsesAgents_ForSteering.isEmpty()){
                hasNeighbourForSteering = true;
                //to decide the angle per agent in the first level of attention range
//                RVOAgent nearestAgtInVision = obsesAgents_ForSteering.firstEntry().getValue();
                double nearestAgtDist = obsesAgents_ForSteering.firstKey();  //absolute distance
                
//                System.out.println("nearest neighbour distance is: "+nearestAgtDist);
                
                if(nearestAgtDist>3.6-(1+wm.getMyAgent().getPersonalSpaceFactor())*RVOAgent.RADIUS){
                    nearestAgtDist = 3.6-(1+wm.getMyAgent().getPersonalSpaceFactor())*RVOAgent.RADIUS; //default the first attention range to be 2.5m in a sparse crowd
                }else if(nearestAgtDist < (1+wm.getMyAgent().getPersonalSpaceFactor())*RVOAgent.RADIUS){
                    nearestAgtDist = (1+wm.getMyAgent().getPersonalSpaceFactor())* RVOAgent.RADIUS; //to ensure minimal three columns are there
                }
                attention_multi[0]=nearestAgtDist+(1+wm.getMyAgent().getPersonalSpaceFactor())*RVOAgent.RADIUS;
                attention_multi[1]=attention_multi[0]*2;
//                attention_multi[2]=nearestAgtDist*4;
                
                angle = calAngle(RVOAgent.RADIUS *(1+wm.getMyAgent().getPersonalSpaceFactor()),attention_multi[0]);
                if(angle<Math.asin((1+wm.getMyAgent().getPersonalSpaceFactor())*RVOAgent.RADIUS / 3.6)* 360/Math.PI){
                    angle = Math.asin((1+wm.getMyAgent().getPersonalSpaceFactor())*RVOAgent.RADIUS / 3.6) * 360/Math.PI;
                }else if(angle > 60){
                    angle = 60;
                }
                //thus to decide the numbre of columns in the STP       
                spacepattern = new STPattern(pf,2,(int)Math.floor(visionRange/angle));
                spacepattern_agtID = new STPattern(pf,2,(int)Math.floor(visionRange/angle));
                resetSTP();
                this.setSpacePattern(0,0, new Vector2d(vec_C)); //execute pattern recognition system
        }else{
                hasNeighbourForSteering = false;
        }
//        }
    }
    
    private void resetSTP(){
        for (int i = 0; i < pf + 1; i++)
            for (int j = 0; j < spacepattern.getPattern()[0].length; j++) 
                for(int k =0; k< spacepattern.getPattern()[0][0].length;k++){
                    spacepattern.setValue(i, j, k, 0);//current frame, all attention levels, all space available
                    spacepattern_agtID.setValue(i, j, k, -1);
        }
    }
    
    
    /*
     * helper function to determine whether 2 vectors are with angles(undirectional) that are smaller than 90 degrees
     * also can be found under util.Geometry class, where should idealy locate all such helper functions
     */
    private boolean sameDirection(Vector2d v1, Vector2d v2) {
        return (v1.dot(v2) / (v1.length() * v2.length()) > 0);
    }

    
  
}