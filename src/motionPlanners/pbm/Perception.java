package motionPlanners.pbm;

//import java.awt.Point;
import agent.RVOAgent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.util.Bag;

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

    // A flag used to indicate when there is strategy selected.
    private boolean strategySelected;
    
    //new added to extend attention with different attenuation levels
    //e.g., {1,2,4...}
    //number of attenuation levels can be specified by different agents
    // we use 3 levels
    private double[] attention_multi;
    
    protected Bag sensedAgents;
    
    public Bag getSensedAgents(){
        return sensedAgents;
    }

    public boolean isStrategySelected() {
        return strategySelected;
    }

    public void setStrategySelected(boolean strategySelected) {
        this.strategySelected = strategySelected;
    }

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
        NEAR, MIDDIUM, FAR, OUTBOUND
    }
    
    public static double[] predefinedColumns ={21, 37.8, 54.6, 68.6, 82.6, 96.6, 110.6, 124.6, 141.4, 158.2, 179.2};
    
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

        spacepattern = new STPattern(pf, attenuationLevel,(int)(Math.ceil(visionRange / angle)));
        spacepattern_agtID = new STPattern(pf, attenuationLevel,(int)(Math.ceil(visionRange / angle)));

        strategySelected =  false;
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
        spacepattern = new STPattern(pf,3,11);
        spacepattern_agtID = new STPattern(pf,3,11);
        strategySelected =  false;
    }
    /**
     * Local helper function to determine angle between this and target agent
     *
     * @param bodysize
     * @param distToObst
     * @return angle per visionary section in degree
     */
    private double getAngle(double bodyRadius, double distToObst) {
        double anglePerSection = 10; //default visionary section as 10 degree
        anglePerSection = 2 * Math.asin(bodyRadius / (distToObst + bodyRadius)) * 180 / Math.PI;
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
     * Local helper function to determines the index in the pattern of the
     * target agent
     * Last updated: nov 24, 2011, by hu nan
     * @param Point2d pos1 (me)
     * @param Point2d pos2 (agent)
     * @return an ArrayList of indeces that the agent has occupied in the unevenly distributed attention system of me
     */
    private ArrayList<Integer> calColumnIndex(Point2d pos1, Point2d pos2) {
        //in some cases, one agent may occupy several attention columns
        ArrayList<Integer> columnIndex =  new ArrayList<Integer>();
        
        Vector2d p1top2 = new Vector2d(pos2);
        p1top2.sub(pos1);
        Vector2d vleftBoundary = new Vector2d(Math.cos(leftmost * Math.PI / 180), (-1) * Math.sin(leftmost * Math.PI / 180)); //the coordinate system for y is different
//        Vector2d rightBoundary = new Vector2d(Math.cos(rightmost * Math.PI / 180), (-1) * Math.sin(rightmost * Math.PI / 180));
        Vector2d vCenter = new Vector2d(Math.cos(center * Math.PI / 180), (-1) * Math.sin(center * Math.PI / 180));
        
        //The circle is considered as myAgent's radius (1+* myPersonalSpace)
        //this represent the visual impact of the size of the agent to me, the closer, the larger
        double halfAngle = Math.asin(wm.getMyAgent().getRadius() * (wm.getMyAgent().getPersonalSpaceFactor()+1) /(p1top2.length()-wm.getMyAgent().getRadius())) * 180 / Math.PI;
        
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
            
            //2. for unevenly distributed ateention sections:
            int i;
            double countAngle=0;
            for (i=0; i<Perception.predefinedColumns.length; i++){
                countAngle+=predefinedColumns[i];
                if(countAngle<angleToLeftBoundary_1){
                    continue;
                }
                else if(countAngle>=angleToLeftBoundary_1 && countAngle<=angleToLeftBoundary_2){
                    columnIndex.add(i);
                }
                else break;
            }      
        } 
        //else: the agent at position p2 is not within the vision range of me
        return columnIndex;
    }
    
    /**
     * Main method in vision system to update the local pattern data structure.
     * Called by this.execute()
     * 
     */
    private void setSpacePattern() {//pass in a group of agents within the total vision range and visual distance at the current frame
        //initialize the spatial pattern to be plain with all available space
        setVisualZone(wm.getMyAgent().getVelocity());
        for (int i = 0; i < pf + 1; i++) {
            for (int j = 0; j < spacepattern.getPattern()[0].length; j++) {
                Arrays.fill(spacepattern.getPattern()[i][j], 0);//current frame, all attention levels, all space available
            }

            for (Object o : sensedAgents) {
                RVOAgent agent = (RVOAgent) o;
                RVOAgent me = wm.getMyAgent();
                
                Point2d myNextPos = new Point2d(me.getNextPosition(i));
                Point2d agentNextPos = new Point2d(agent.getNextPosition(i));

                //calculate relative distance between me and agt[i]
//                double relative_dist = myNextPos.distance(agentNextPos) - me.getRadius() - agent.getRadius()* me.getPersonalSpaceFactor();
                
                double eyeToCenter_dist = myNextPos.distance(agentNextPos) - me.getRadius();
//                double spatial_dist = eyeToCenter_dist - agent.getRadius()* me.getPersonalSpaceFactor();
                        
                if (agent != me) {
                   //To get 1 or -1
                    boolean samedirection = false;
                    if (sameDirection(agent.getVelocity(), wm.getMyAgent().getVelocity())) {
                        samedirection = true; //in future may change if acceleration !=0 any more
                    }
                    
                    //To get row index
                    AttenuationLevel att_level;
                    if (eyeToCenter_dist <= attention_multi[0]) {
                        att_level = AttenuationLevel.NEAR;
                    } else if (eyeToCenter_dist <= attention_multi[1]) {
                        att_level = AttenuationLevel.MIDDIUM;
                    } else if (eyeToCenter_dist <= attention_multi[2]) {
                        att_level = AttenuationLevel.FAR;
                    } else {
                        att_level = AttenuationLevel.OUTBOUND;
                    }
                     
                    //To get column index
                    ArrayList<Integer> columnIndex = calColumnIndex(myNextPos, agentNextPos);
                    
                    if(!columnIndex.isEmpty()){
                        int rowIndex = -1;
                        switch (att_level) {
                            case NEAR:
                                rowIndex = 0;
                                break;
                            case MIDDIUM:
                                rowIndex = 1;
                                break;
                            case FAR:
                                rowIndex = 2;
                                break;
                            case OUTBOUND:
                                break;
                        }
                        if (rowIndex >= 0) {
                            //if the current value on this row and column in the array is -1, cannot set it back to 1
                            //this is to address the more importance of -1 in the visiual attention during decision making
                            for(int k=0; k<columnIndex.size(); k++){
                                int column=columnIndex.get(k);
                                if (samedirection && spacepattern.getValue(i, rowIndex, column)==0) {
                                    spacepattern.setValue(i, rowIndex, column, 1);
                                    spacepattern_agtID.setValue(i, rowIndex, column, agent.getId());
                                } else if(!samedirection ){
                                    spacepattern.setValue(i, rowIndex, column, -1);
                                    spacepattern_agtID.setValue(i, rowIndex, column, agent.getId()); 
                                    //currently, the agtID is not correct as always the last agent ID to be checked is filled in the array*************todo**************
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
    public void execute(Bag sensedAgents) {
        this.sensedAgents = sensedAgents;
        if (!strategySelected) { //if no strategy (e.g., violation)
            this.setSpacePattern(); //execute pattern recognition system
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


//TODO1. Modification in PbmVision
    /**
     * 1. check to make sure degree and radians are used correctly in all places
     * 2. Test the vision system alone to see whether can generate 3d array correctly
     * 3. if got time, in the future, change the distToObstacle, which is used
     * to calculating angle per vision section, to a dynamic value from the visualized
     * distance in vision. If this modification needs to be made, the structure of 
     * vision needs to be changed also, consider to create new 3d spacepattern array
     * everytime when needed instead of create one in the vision constructor and set
     * the value everytime when call the fucntion setSpacepattern() because, if the
     * distToObstale is dynamic, then angle per section is also dynamic, then the 
     * number of columns in the array also is dynamic! 
     */
//TODO2. I doubt why the velocity is changed, but in the appelet, it does not change
//TODO3. Changed Pattern Matching mechanism
    /**
     * This method checks the current situation, and checks the first row of the
     * pattern at the current frame. This is used by the action system to verify
     * expectancies,
     *
     * @return an integer array containing first row of pattern
     * while currentRow_agt is passed by as a reference, its value also modified in this function
     */
    /*
    public int[] getCurrentView_1stRow(Vector2d directionLookFor, RVOAgent[] currentRow_agt) {
        int[] firstRow = new int[(int) Math.ceil(visionRange / angle)];
        Arrays.fill(firstRow, 0);
        setVisualZone(directionLookFor);

        for (Object o : sensedAgents) {
            RVOAgent agent = (RVOAgent) o;
            if (agent != wm.getMyAgent()) {
                
                //this is the buffered current agent position, help maintain the consistancy of decision within one simulatio step
                //currently, it is used to replace agent.getCurrentPosition()
//                Point2d agentCurrentPosition = new Point2d(agent.getCurrentPosition());

                int columnIndex = calColumnIndex(wm.getMyAgent().getCurrentPosition(),agent.getCurrentPosition());
                double relativeDist = agent.getCurrentPosition().distance(wm.getMyAgent().getCurrentPosition()) - agent.getRadius();
                boolean samedirection = false;

                //similarly, this is the bufferred current velocity of the agent
                //currently, it is used to replace agent.getVelocity()
//                Vector2d agentCurrentVelocity = new Vector2d((Vector2d)(((WorkingMemory)(agent.getRvoCalc())).getLastFrameBuffer().get(1)));

                if (sameDirection(wm.getMyAgent().getVelocity(), agent.getVelocity())) {
                    samedirection = true;
                }
                if (columnIndex >= 0) {
                    if (samedirection) {
                        if (relativeDist <= attention_multi[0] && firstRow[columnIndex] == 0) {
                            firstRow[columnIndex] = 1;
                            currentRow_agt[columnIndex] = agent;
                        }
                    } else {
                        if (relativeDist <= attention_multi[0]) {
                            firstRow[columnIndex] = -1;
                            currentRow_agt[columnIndex] = agent;
                        }
                    }
                }
            }
        }
        return firstRow;
    }
*/