package motionPlanners.pbm;

//import java.awt.Point;
import agent.RVOAgent;
import java.util.Arrays;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.util.Bag;

public class PbmVision {


    public int[][][] spacepattern; //[no of predicted frames]([no of attenuation levels for attention][number of visionary section])
    public RVOAgent[][][] spacepattern_agt; //predicted frames . record space is occupied by which agt
    //num of predited frame
    //it is correlated to the pattern matching and commitment to the planning, but no solid evidence on the exact value from psych study
    private int pf;
    private double visionRange; //used to be in 120, but now set to 170 coz human is versatile in getting sensory information (by slightly rotating head etc.)
    private double visionLength; //in general, vision length is to be very large, only useful in calculating predicted spatial info
    private double angle; //in degree of each visionary section, it should be dynamic as the distance to the "attended" agent in the middle changes
    private double center;
    private double rightmost;
    private double leftmost;
    private WorkingMemory wm;
//    public int attention; //old version, single threshold for attention range
    /**
     * A flag used to indicate when there is strategy selected.
     */
    private boolean strategySelected;
    
        //new added to extend attention with different attenuation levels
    //e.g., {2,4,8...}
    //number of attenuation levels can be specified by different agents
    private double[] attention_multi;
    protected Bag sensedAgents;

    public boolean isStrategySelected() {
        return strategySelected;
    }

    public void setStrategySelected(boolean strategySelected) {
        this.strategySelected = strategySelected;
    }

    public int[][][] getSpacepattern() {
        return spacepattern;
    }

    public RVOAgent[][][] getSpacepattern_agt() {
        return spacepattern_agt;
    }

    public int getPf() {
        return pf;
    }

    public static enum AttenuationLevel {

        NEAR, MIDDIUM, FAR, OUTBOUND
    }

    public PbmVision(int attenuationLevel, int pd, double bodysize, double distToObst, double vr, double vl, WorkingMemory wm) {
        angle = getAngle(bodysize, distToObst); //here, distToObst is some convenional value from literature review, say 1m, 1.5m, 2m, 2.5m, 3m, 3.5m ... not real time value
        attention_multi = new double[attenuationLevel];
        for (int i = 0; i < attenuationLevel; i++) {
            attention_multi[i] = Math.pow(2, i) * bodysize  / ( Math.sin((angle * Math.PI) / (2 * 180))); //make attention attenuation regress explentially
        }
        pf = pd;
        visionRange = vr;
        visionLength = vl;

        this.wm = wm; //pass by reference of the agent to the vision class to specify the relationship
        calcVisibleRange(wm.getMyAgent().getVelocity()); //to set vision center, leftmost and rightmost of vision at the current frame
        sensedAgents = null;

        spacepattern = new int[pf + 1][attenuationLevel][(int) (Math.ceil(visionRange / angle))];
        spacepattern_agt = new RVOAgent[pf + 1][attenuationLevel][(int) (Math.ceil(visionRange / angle))];

        strategySelected =  false;
    }

    /**
     * Local helper function to determine angle between this and target agent
     *
     * @param bodysize
     * @param distToObst
     * @return angle per visionary section in degree
     */
    private double getAngle(double bodysize, double distToObst) {
        double anglePerSection = 10; //default visionary section as 10 degree
        anglePerSection = 2 * Math.asin(bodysize / (distToObst + bodysize)) * 180 / Math.PI;
        return anglePerSection;
    }

    /**
     * Local helper function to calculate the vision angle
     * at which the agent can perceive
     *
     */
    private void calcVisibleRange(Vector2d centerVel) {
        /*
         * center, leftmost, rightmost are in degree
         */

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
     *
     * @param Point2d pos1 (me)
     * @param Point2d pos2 (agent)
     * @return
     */
    private int calColumnIndex(Point2d pos1, Point2d pos2) {
        int columnIndex = -1;
        Vector2d p1top2 = new Vector2d(pos2);
        p1top2.sub(pos1);
        Vector2d leftBoundary = new Vector2d(Math.cos(leftmost * Math.PI / 180), (-1) * Math.sin(leftmost * Math.PI / 180)); //the coordinate system for y is different
//        Vector2d rightBoundary = new Vector2d(Math.cos(rightmost * Math.PI / 180), (-1) * Math.sin(rightmost * Math.PI / 180));
        Vector2d vCenter = new Vector2d(Math.cos(center * Math.PI / 180), (-1) * Math.sin(center * Math.PI / 180));
        

        double AngleToLeftBoundary = p1top2.angle(leftBoundary) * 180 / Math.PI;
//        double AngleToRightBoundary = p1top2.angle(rightBoundary) * 180 / Math.PI;
        double AngleToVCenter = p1top2.angle(vCenter) * 180 / Math.PI;
        
        if( AngleToVCenter <= visionRange / 2)
            columnIndex = (int)Math.floor(AngleToLeftBoundary/angle);
        return columnIndex;
    }

    /**
     * This method checks the current situation, and checks the first row of the
     * pattern at the current frame. This is used by the action system to verify
     * expectancies,
     *
     * @return an integer array containing first row of pattern
     * while currentRow_agt is passed by as a reference, its value also modified in this function
     */
    public int[] getCurrentView_1stRow(Vector2d directionLookFor, RVOAgent[] currentRow_agt) {
        int[] firstRow = new int[(int) Math.ceil(visionRange / angle)];
        Arrays.fill(firstRow, 0);
        calcVisibleRange(directionLookFor);

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

    /**
     * Main method in vision system to update the local pattern data structure.
     * Called by execute every timestep.
     * 
     */
    private void setSpacePattern() {//pass in a group of agents within the total vision range and visual distance at the current frame
        //initialize the spatial pattern to be plain with all available space
        calcVisibleRange(wm.getMyAgent().getVelocity());
        for (int i = 0; i < pf + 1; i++) {
            for (int j = 0; j < spacepattern[0].length; j++) {
                Arrays.fill(spacepattern[i][j], 0);//current frame, all attention levels, all space available
            }

            for (Object o : sensedAgents) {
                RVOAgent agent = (RVOAgent) o;
//                Vector2d agentCurrentVelocity = new Vector2d((Vector2d)(((WorkingMemory)(agent.getRvoCalc())).getLastFrameBuffer().get(1)));

                Point2d myNextPos = new Point2d(this.wm.getMyAgent().getNextPosition(i));
                Point2d agentNextPos = new Point2d(agent.getNextPosition(i));

                //calculate relative distance between me and agt[i]
                double relative_dist = myNextPos.distance(agentNextPos) - agent.getRadius() * wm.getMyAgent().getPersonalSpaceFactor();

                if (agent != wm.getMyAgent()) {
                    int columnIndex = calColumnIndex(myNextPos, agentNextPos);
                    boolean samedirection = false;
                    if (sameDirection(agent.getVelocity(), wm.getMyAgent().getVelocity())) {
                        samedirection = true; //in future may change if acceleration !=0 any more
                    }
                    AttenuationLevel att_level;
                    if (relative_dist <= attention_multi[0]) {
                        att_level = AttenuationLevel.NEAR;
                    } else if (relative_dist <= attention_multi[1]) {
                        att_level = AttenuationLevel.MIDDIUM;
                    } else if (relative_dist <= attention_multi[2]) {
                        att_level = AttenuationLevel.FAR;
                    } else {
                        att_level = AttenuationLevel.OUTBOUND;
                    }

                    // if (center>90 && center<270) {//agent "me" is moving towards left
//                    if (relativeSlope >= rightmost && relativeSlope <= leftmost) { //check whether agt[i] within vision range of me
                    if(columnIndex >= 0){
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
                            if (samedirection && spacepattern[i][rowIndex][columnIndex] == 0) {
                                spacepattern[i][rowIndex][columnIndex] = 1;
                                spacepattern_agt[i][rowIndex][columnIndex] = agent;
                            } else if(!samedirection){
                                spacepattern[i][rowIndex][columnIndex] = -1;
                                spacepattern_agt[i][rowIndex][columnIndex] = agent; //currently, the agtID is not correct as always the last agent ID to be checked is filled in the array*************todo**************

                            }
                        }
                    }
                }
            }
        }
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

    private boolean sameDirection(Vector2d v1, Vector2d v2) {
        return (v1.dot(v2) / (v1.length() * v2.length()) > 0);
    }

      /**
     * Returns the predicted position i time steps in the future based on linear dead reckoning.
     *
     * @param i
     * @return
     */
//    private Point2d getNextPosition(RVOAgent agt, int i){
//        Point2d agtCurrentPosition = new Point2d(agt.getCurrentPosition());
//        if(agt != this.wm.getMyAgent())
//            agtCurrentPosition = (Point2d)(((WorkingMemory)(agt.getRvoCalc())).getLastFrameBuffer().get(2));
//        Vector2d agtCurrentVelocity = new Vector2d(agt.getVelocity());
//        if(agt != this.wm.getMyAgent())
//            agtCurrentVelocity = (Vector2d)(((WorkingMemory)(agt.getRvoCalc())).getLastFrameBuffer().get(1));
//
//        Point2d predictPos = new Point2d(agtCurrentVelocity);
//        predictPos.scale(i * RVOModel.timeStep);
//        predictPos.add(agtCurrentPosition);
//        return predictPos;
//    }
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
//TODO3. Decision based on pattern needs to consider rows at behind if first row is available
