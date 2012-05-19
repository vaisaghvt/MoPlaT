package motionPlanners.pbm;

import agent.RVOAgent;
import app.PropertySet;
import ec.util.MersenneTwisterFast;
import javax.vecmath.Vector2d;
import javax.vecmath.Point2d;
import motionPlanners.pbm.WorkingMemory.STRATEGY;
import utility.Geometry;

/*
 * Define realistic data
 * First Attention range = 3m in sparse crowd
 * Body size = 44cm (diameter)
 * Average speed = 0.4m/s
 * Frame = 300ms (according each frame is one step)
 */
public class Action {
    private RVOAgent target;
    WorkingMemory wm;
    
    private Vector2d selectedVelocity;
    
    /**
     * relative positions for determining if the agent is being followed,
     * ie., once the agent is within the range target.x+XGap it is considered
     * to have reached the target agent and will begin following. Also for overtake
     */
    private double preferGap; //correspond to the step size of (5)
    
    /**
     * Frame number of previous decision (cycle number)
     */
    public int frameFromLastDecision;
    
    /**
     * true when expectancies violated             //shifted to wm
     */
//    public boolean violateExpectation;
    
    /**
     * true when strategy is completed
     */
//    public boolean finishCurrentStrategy;
    
    /**
     * For this mechanism we set a maximum speed of 4 m/s
     */
//    private double maxSpeed = 1.8; //approximate max walking speed for pedestrians is around 350ft/min, according to Young's work

    /**
     * Relative velocity difference necessary in order to trigger completion of
     * strategy (e.g., follow). In order for follow to be considered complete
     * the velocity of the two agents involved must have less than 3% difference
     */
    public double velocityDiff = 0.05;

    public Action(WorkingMemory wm) {
        target = null;
        this.wm = wm;      
        selectedVelocity = new Vector2d(wm.getMyAgent().getVelocity());
//        System.out.println("action constructor, myAgent velocity: "+selectedVelocity); //correct
        
        frameFromLastDecision = 0;
//        violateExpectation = false;
//        finishCurrentStrategy = false;
        initPreferredGaps();
    }

    public double getPreferGap() {
        return preferGap;
    }

    //update the preferred velocity for the agent
    public Vector2d getSelectedVelocity() {
        return selectedVelocity;
    }

    /**
     * Execution of Follow steering strategy by setting the velocity in locomotion level
     * follow a particular agent
     * approaching the agent from the left or right side of the target depends on the current relative position of the two agents
     */
    public void follow(RVOAgent agt) {
        target = agt;
//        String fromLeft = "right";
//        if(left) {
//            fromLeft = "left";
//        }
        System.out.println("agent" + this.wm.getMyAgent().getId() + " is following " + "agent " + target.getId());

        //position and velocity determines whether successfully followed
        if (target.getCurrentPosition().distance(this.wm.myAgent.getCurrentPosition()) <= preferGap
                && (Math.abs(target.getVelocity().length() - this.wm.myAgent.getVelocity().length()) / Math.max(agt.getVelocity().length(), this.wm.myAgent.getVelocity().length()) ) <= velocityDiff
                && target.getVelocity().length() >= this.wm.myAgent.getVelocity().length()) {
            wm.setFinishCurrentStrategy(true);
            return;
        }
        //check expectancy violation
//        if( ){
//            wm.setViolateExpectancy(true);
//            return;
//        }
        
        
        //set preferred velocity for me to follow the target
        Vector2d destinationToMove = approachlane(target); //change direction of velocity (returns position to move to)
        adjustSpeedFollow(target, destinationToMove);//change magnitude of velocity
    }

    //used in follow()
    private Vector2d approachlane(RVOAgent agt) {
        double randomAngle = 0; //in radian
        //use the updated information of agt all the way during following
        Vector2d locationToMove = new Vector2d(agt.getVelocity());
        locationToMove.normalize();
        locationToMove.negate();
        locationToMove.scale(preferGap * 0.8);
//        locationToMove.add(agt.getCurrentPosition());
        
//        MersenneTwisterFast random = wm.getMyAgent().getMySpace().getRvoModel().random;
//        if (left) {
//            randomAngle = random.nextDouble() * (Math.PI / 4);
//        } else {
//            randomAngle = random.nextDouble() * (Math.PI / 4) * (-1);
//        }

//        rotate2d(locationToMove, randomAngle);
        //rotate the vector around (0,0) clokcwise by randomAngle
//        rotate2d(locationToMove, randomAngle);
        locationToMove.add(agt.getCurrentPosition());
        return locationToMove;
    }

    /**
     * Rotates a given vector in 2d in clockwise direction about Z-axis
     *
     * @param v
     * @param angle
     * @return
     */
    private void rotate2d(Vector2d v, double angle) {
        double newx = v.x * Math.cos(angle) - v.y * Math.sin(angle);
        double newy = v.x * Math.sin(angle) + v.y * Math.cos(angle);
        v.setX(newx);
        v.setY(newy);
    }

    //rotate vector2d v around point p clockwise by angle
    private void rotate2dAroundaPoint(Vector2d v, Point2d p, double angle){
        double newx = (v.x-p.x) * Math.cos(angle) - (v.y-p.y) * Math.sin(angle) + p.x;
        double newy = (v.y-p.y) * Math.cos(angle) + (v.x-p.x) * Math.sin(angle) + p.y;
        v.setX(newx);
        v.setY(newy);
    }
    /**
     * for helper function testing
     * @param args
     */
//    public static void main(String args[]){
//        Vector2d v = new Vector2d(1,0);
//        Point2d p = new Point2d(1,1);
//        double angle = Math.PI / 2;
////        rotate2d(v,angle);
//        rotate2dAroundaPoint(v, p, angle);
//        System.out.print("new vector is " + v.x + ", " + v.y );
//    }
    /**
     * Sets velocity for follow
     * @param agt: target agent
     * @param pointToMove
     */
    private void adjustSpeedFollow(RVOAgent agt, Vector2d pointToMove) {
        //to set the moving direction
        pointToMove.sub(this.wm.getMyAgent().getCurrentPosition());
        pointToMove.normalize();

        //new Speed is proportional to relative speed difference
        //assume target is slower than this agent, otherwise follow doesn't make sense

        double approachSpeed = wm.getMyAgent().getVelocity().length() + 0.6 * (agt.getVelocity().length() - wm.getMyAgent().getVelocity().length());
        pointToMove.scale(approachSpeed);
        selectedVelocity = new Vector2d(pointToMove);
    }

    /*
     * Execution of Overtake steering strategy by setting the velocity in locomotion level
     */
    public void overtake(RVOAgent agt, boolean left, int T, final Vector2d startVel, final Point2d startPos) {//T is the number of frames to finish the first phase
        String leftSide = "left side";
        String rightSide = "right side";
        if (left) {
            System.out.println("Agent" + wm.getMyAgent().getId() + " is overtaking " + "Agent" + agt.getId() + " from the " + leftSide);
        } else {
            System.out.println("Agent" + wm.getMyAgent().getId() + " is overtaking " + "Agent" + agt.getId() + " from the " + rightSide);
        }

        Vector2d directionToTarget = new Vector2d(agt.getCurrentPosition());
        directionToTarget.sub(wm.getMyAgent().getCurrentPosition());
        double ahead = utility.Geometry.sameDirection(directionToTarget, startVel);
//        double ahead = utility.Geometry.sameDirection(directionToTarget, agt.getVelocity());
//        
        if (ahead >= 0) {
            //execute catching up behavior in phase 1 of overtaking
            CatchUp(agt, left, T);

        }else if (ahead > -0.2) {
            Vector2d passAheadVel = new Vector2d(startVel);
            passAheadVel.scale(1.05);
            selectedVelocity = new Vector2d(passAheadVel);
        }
        else{
//           do not implement resume original course, such behavior can emerge through the rough preferredVelocity setting
            wm.setFinishCurrentStrategy(true);
        }
        
        //check expectancy from the current situation, actually along the velocity in front of me, whether got collisions with other agents
        
        
        
    }

    /**
     * Used in catch up during overtaking or approach to target during side-avoiding
     * 
     * Calculates a point perpendicular to the target agent velocity at a random distance away.
     * @param agt
     * @return
     */
    private Vector2d approachSide(RVOAgent agt, boolean left) {
        double angle = Math.PI / 2;
        if (!left) {
            angle *= -1;
        }
        Vector2d locationToMove_overtake = new Vector2d(agt.getVelocity());
//        Vector2d locationToMove_avoid = new Vector2d(startVel);
        //if this method is called in "Catchup during overtaking"
        if (wm.getDecision().getCurrentStrategy() == STRATEGY.OVERTAKE) {
            locationToMove_overtake.negate();
        }
        //if this method is called in "approachToTarget during Side-Avoiding"
        locationToMove_overtake.normalize();
//        locationToMove.add(agt.getCurrentPosition());
//        rotate2dAroundaPoint(locationToMove, agt.getCurrentPosition(), angle);
        rotate2d(locationToMove_overtake,angle);
        
        if (wm.getDecision().getCurrentStrategy() == STRATEGY.OVERTAKE) {
            locationToMove_overtake.scale(RVOAgent.RADIUS * 2* (1+this.wm.getMyAgent().getPersonalSpaceFactor()) + 
                    1.5 * (1+wm.getMyAgent().getPersonalSpaceFactor())* RVOAgent.RADIUS
                    );
        } else if (wm.getDecision().getCurrentStrategy() == STRATEGY.AVOID) {
            locationToMove_overtake.scale(RVOAgent.RADIUS * 2* (1+this.wm.getMyAgent().getPersonalSpaceFactor()) 
                    + 1 *(1+wm.getMyAgent().getPersonalSpaceFactor())* RVOAgent.RADIUS
//                    + 0.5 * agt.getRadius() * (1+agt.getPersonalSpaceFactor()) 
//                    + this.wm.getMyAgent().getRadius()* (1+this.wm.getMyAgent().getPersonalSpaceFactor())
//                    (this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble()) * RVOAgent.RADIUS
                    );
        }
        locationToMove_overtake.add(agt.getCurrentPosition());
        return locationToMove_overtake;
    }

    /**
     * Catch up is a two stage process, it first selects an appropriate position
     * to the side of the target agent and then increases its speed if necessary.
     *
     * @param agt
     * @param fromLeft
     * @param T
     */
    private void CatchUp(RVOAgent agt, boolean fromLeft, int T) {
        Vector2d catchUpVelocity = approachSide(agt, fromLeft);
        catchUpVelocity.sub(wm.getMyAgent().getCurrentPosition());
        catchUpVelocity.normalize();
        catchUpVelocity.scale(wm.getMyAgent().getSpeed());
        
        if (calculateTTA(this.wm.getMyAgent(), agt) + frameFromLastDecision < T) {
            //if we're ahead of schedule then ok, or slightly increase the catching up speed
            catchUpVelocity.normalize();
            catchUpVelocity.scale(wm.getMyAgent().getSpeed() 
            + 0.01 * wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() * (wm.getMyAgent().getMaxSpeed()-wm.getMyAgent().getSpeed()));
           
        } else {
            // speed up
            catchUpVelocity.normalize();
            catchUpVelocity.scale(wm.getMyAgent().getSpeed() 
            + 0.01 * wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() * (wm.getMyAgent().getMaxSpeed()-wm.getMyAgent().getSpeed()));
        }
        selectedVelocity = new Vector2d(catchUpVelocity);
    }
    
    /**
     * This is similar to TTC in RVO, calculate time to available 
     * space beside the target agent targetAgt
     * @param myAgt - myAgt
     * @param targetAgt - targetAgent
     * @return
     */
    protected int calculateTTA(RVOAgent myAgt, RVOAgent targetAgt) {
        int frameNum = Integer.MAX_VALUE; //agent myAgt can never collide with agent targetAgt
        Vector2d relativeS = new Vector2d(myAgt.getVelocity());
        relativeS.sub(targetAgt.getVelocity());
        double distanceToMove = Integer.MAX_VALUE; //if the relative speed vector doesnt cut the relative circle

        Point2d p1 = myAgt.getCurrentPosition();
        Point2d p2 = targetAgt.getCurrentPosition();
        Vector2d pa2pb = new Vector2d(p2);
        pa2pb.sub(p1);
        double relativeDistance = pa2pb.length();

        //TODO, shall we use radius or radius*personalSpaceFactor here?
        double relativeRadius = myAgt.getRadius()*(1+ myAgt.getPersonalSpaceFactor()) + targetAgt.getRadius()*(1+targetAgt.getPersonalSpaceFactor());
        //define line's formulae representing the vector of relative speed
        //y-ya - slope(x-xa)=0
        double slope = Math.tan(relativeS.y / relativeS.x);
        //distance of p2 to the line
        double d = (float) Math.abs(p2.y - p1.y - slope * (p2.x - p1.x)) / Math.sqrt(1 + slope * slope);

        if (d > relativeRadius) {
            return frameNum; //never will reach the perceived space beside the potential target
        } else {
            double m = Math.sqrt(relativeRadius * relativeRadius - d * d);
            distanceToMove = Math.sqrt(relativeDistance * relativeDistance - d * d) - m;
            frameNum = (int) ((distanceToMove / relativeS.length()) / PropertySet.TIMESTEP); //if within 1 frames can reach, return 0
            return frameNum;
        }
    }
    
//    /*
//     * To return the intersection point of two lines, not segment
//     * startPosbefOvertake
//     */
//    private Point2d line2lineIntersection(Point2d point1, Vector2d direction1, Point2d point2, Vector2d direction2) {
//        //get the tangent, slope of the two lines
//        double slope1 = Math.tan(direction1.y / direction1.x);
//        double slope2 = Math.tan(direction2.y / direction2.x);
//
//        //get equation for line 1 and line 2 - defined by point-slope formulae
//        // point.y = slope * point.x + c
//        double c1 = point1.y - slope1 * point1.x;
//        double c2 = point2.y - slope2 * point2.x;
//
//        //check whether 2 lines are parallel to each other
//        //if parellel or overlap onto each other, then there is no intersection
//        if (slope1 == slope2) {
//            return null;
//        }
//        double newX = (c2 - c1) / (slope1 - slope2);
//        double newY = newX * slope1 + c1;
//        Point2d intersectionPoint = new Point2d(newX, newY);
//        return intersectionPoint;
//    }

    /**
     * this method is used to avoid the agent with another target agent with "opposite" moving direction towards it
     * the whole process is similar to overtaking strategy, however, different mainly in:
     * 1)
     */
    public void avoid(RVOAgent agt, boolean left, final Vector2d startVel, final Point2d startPos) {
        String leftSide = "left side";
        String rightSide = "right side";
        if (left) {
            System.out.println("Agent" + wm.getMyAgent().getId() + " is avoiding " + "Agent" + agt.getId() + " from the " + leftSide);
        } else {
            System.out.println("Agent" + wm.getMyAgent().getId() + " is avoiding " + "Agent" + agt.getId() + " from the " + rightSide);
        }

        Vector2d directionToTarget = new Vector2d(agt.getCurrentPosition());
        directionToTarget.sub(wm.getMyAgent().getCurrentPosition());
        directionToTarget.normalize();
        //cos(theta), where theta is the angle between vector(directionToTarget) and vector (myAgent's velocity)
        double ahead = Geometry.sameDirection(directionToTarget, startVel);
        //ahead> 0 means angle <90, means, myAgent still not cross target agt yet
        if (ahead > 0) {
//            if (frameFromLastDecision >= T) {
//                violateExpectation = true;
//                return;
//            }
          //To check FininishCurrentStrategy:
          //here, the finishCurrentStrategy condition can be more restricted as once there is enuf for space already moving towards its goal, no need to further deviate
           
           //1. to check the distance of my new Position to the line from target current position towards his goal, whether collide 
//           Point2d targetGoalPos = agt.getGoal();
//           Point2d myGoal = wm.getMyAgent().getGoal();
//           
//           Point2d targetCurrentPos = agt.getCurrentPosition();
//           Point2d myCurrentPos = wm.getMyAgent().getCurrentPosition();
//                      
//           Vector2d myToGoal = new Vector2d(myGoal);
//           myToGoal.sub(myCurrentPos);
//           
//           Vector2d targetToGoal = new Vector2d(targetGoalPos);
//           targetToGoal.sub(targetCurrentPos);
//           
//           double distanceToLine = 0;
//           double myDistToTargetLine = 0;
//           double targetDistToMyLine = 0;
//           
//           double slopeMyLine = (targetCurrentPos.x-myCurrentPos.x)*(myGoal.x-myCurrentPos.x) + (targetCurrentPos.y-myCurrentPos.y)*(myGoal.y-myCurrentPos.y);
//           slopeMyLine = slopeMyLine / Math.pow(myToGoal.length(), 2);
//           
//           double crossOnMyLine_X = myCurrentPos.x + slopeMyLine * (myGoal.x-myCurrentPos.x);
//           double crossOnMyLine_Y = myCurrentPos.y + slopeMyLine * (myGoal.y-myCurrentPos.y);
//           
//           targetDistToMyLine = Math.pow((crossOnMyLine_X-targetCurrentPos.x), 2) + Math.pow((crossOnMyLine_Y-targetCurrentPos.y), 2);
//           targetDistToMyLine = Math.sqrt(targetDistToMyLine);
//           
//           
//           double slopeTargetLine = (myCurrentPos.x-targetCurrentPos.x)*(targetGoalPos.x-targetCurrentPos.x) + (myCurrentPos.y-targetCurrentPos.y)*(targetGoalPos.y-targetCurrentPos.y);
//           slopeTargetLine = slopeTargetLine / Math.pow(targetToGoal.length(), 2);
//           
//           double crossOnTargetLine_X = targetCurrentPos.x + slopeTargetLine * (targetGoalPos.x-targetCurrentPos.x);
//           double crossOnTargetLine_Y = targetCurrentPos.y + slopeTargetLine * (targetGoalPos.y-targetCurrentPos.y);
//           
//           myDistToTargetLine = Math.pow((crossOnTargetLine_X-myCurrentPos.x), 2) + Math.pow((crossOnTargetLine_Y-myCurrentPos.y), 2);
//           myDistToTargetLine = Math.sqrt(myDistToTargetLine);
//           
////           distanceToLine =  Math.min(targetDistToMyLine, myDistToTargetLine);
//           distanceToLine = myDistToTargetLine;
//           double personalSpaceRadius = Math.max(wm.getMyAgent().getRadius() * (1+wm.getMyAgent().getPersonalSpaceFactor()), agt.getRadius()*(1+agt.getPersonalSpaceFactor()));
//  
//           //2. check whether my new location moving towards the side of the target set by "left"
//           
//           boolean crossedTargetToGoalLine = false;
//         
//
//               Vector2d targetToMyCurrentPos = new Vector2d(myCurrentPos);
//               targetToMyCurrentPos.sub(targetCurrentPos);
//               double angleBt = Geometry.angleBetweenWSign(targetToGoal, targetToMyCurrentPos); //-pi to pi, suppose clockwise roration is +
//               
//               if(left){
//                   if(angleBt>0)
//                       crossedTargetToGoalLine = true;
//               }else{
//                   if(angleBt<0)
//                       crossedTargetToGoalLine = true;
//               }
//
//           
//           if(distanceToLine > personalSpaceRadius && crossedTargetToGoalLine){
//               //if finished steering already based on the current position and the line towards the goal, set the selectedVel = prefVel, where prefVel is towards its goal
//               selectedVelocity = wm.getMyAgent().findPrefVelocity();
//                finishCurrentStrategy = true;
//                return;
//            }      
            
           Point2d targetGoalPos = agt.getGoal();
           Point2d myGoal = wm.getMyAgent().getGoal();
           
           Point2d targetCurrentPos = agt.getCurrentPosition();
           Point2d myCurrentPos = wm.getMyAgent().getCurrentPosition();
                      
           Vector2d myToGoal = new Vector2d(myGoal);
           myToGoal.sub(myCurrentPos);
           myToGoal.normalize();
           myToGoal.scale(wm.getMyAgent().getSpeed());
           
           Vector2d targetToGoal = new Vector2d(targetGoalPos);
           targetToGoal.sub(targetCurrentPos);
           targetToGoal.normalize();
           targetToGoal.scale(agt.getSpeed());
           
           double ttc = Geometry.calcTTC(myCurrentPos, myToGoal, (wm.getMyAgent().getPersonalSpaceFactor()+1)*RVOAgent.RADIUS, targetCurrentPos, targetToGoal, (agt.getPersonalSpaceFactor()+1)*RVOAgent.RADIUS);
           if(ttc>999){
//               selectedVelocity=wm.getMyAgent().setPrefVelocity();
               wm.setFinishCurrentStrategy(true);
               return;
           }
            Vector2d deviatedVel = approachSide(agt, left);
            deviatedVel.sub(wm.getMyAgent().getCurrentPosition());
            deviatedVel.normalize();
            deviatedVel.scale(wm.getMyAgent().getSpeed());
            selectedVelocity = new Vector2d(deviatedVel);
        } else {
            wm.setFinishCurrentStrategy(true);
            return;
        }
    }


    /*
     * TODO: add in instinctive action in the future
     * This is one example of instinctive reactions, jump aside suddenly to avoid the immenant potential collision
     * This function is usually called in situation where exceptional cases occurs, where prediction cannot match with the real situation, thus the decision made was wrong
     */
    public void slideaside(boolean left) {
        /**
         * slide aside means the agent will rotate its body and decrease its personal space to as small as half body size
         * to make it through when trying to avoid with an incoming agent
         * since current body is represented by a circle rather than cylinder, this action is not implemented yet
         */
    }

    public void execute(STRATEGY selectedStrategy, RVOAgent target, boolean left, int T, Vector2d startVel, Point2d startPos) {
            this.target = target;
            this.selectedVelocity = new Vector2d(wm.getMyAgent().getPrefVelocity());
            switch(selectedStrategy) {
                case FOLLOW:
                    System.out.println("Following");
                    follow(this.target);
                    frameFromLastDecision++;
                    break;
                case OVERTAKE:
                    System.out.println("Overtaking");
                    overtake(this.target, left, T, startVel, startPos);
                    frameFromLastDecision++;
                    break;
                case AVOID:
                    System.out.println("SideAvoiding");
                    avoid(this.target, left, startVel, startPos);
                    frameFromLastDecision++;
                    break;
                //SUDDENSLOW is one example of instinctive reaction, where is full of obstacles in front and no other strategy can be executed
//                case INSTINCTIVERACTION:
//                    System.out.println("Some people is near me already! motion planned through lower level mechanisms e.g., RVO");
//                    //set the prefVel using the default function to set it towards its goal, then collision avoidance is handled by low level RVO
//                    selectedVelocity = new Vector2d(wm.getMyAgent().getPrefVelocity());
////                    selectedVelocity.scale(0.2); //scale the velocity to 0.2
//                    finishCurrentStrategy  = true;              
//                    // or could pass in the obsesAgents_ForReaction and call RVO with this set of near neighbors to calculate the actual velocity that avoid collisions
//                    break;
                default: break;
            }
        
    }
    
    private void initPreferredGaps() {
        //for controlling gaps when follow and overtake is performed
        preferGap = (wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() * 2 + 2) * (1+wm.getMyAgent().getPersonalSpaceFactor()) * RVOAgent.RADIUS; //random represent the other agent's personal space
        
        //future improvement could make this adapt to the surrounding density at run time
    }

}
