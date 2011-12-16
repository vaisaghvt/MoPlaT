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
 * First Attention range = 3.6m
 * Body size = 44cm (diameter)
 * Average speed = 0.4m/s
 * Frame = 300ms (according each frame is one step)
 */
public class Action {
    private RVOAgent target;
    WorkingMemory wm;
    /*
     * may not be necessary any more
     */
//    private PbmVelocity selectedVelocity;
//    
//    private PbmVelocity preferredVelocity; //
    
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
     * true when expectancies violated
     */
    public boolean violateExpectation;
    
    /**
     * true when strategy is completed
     */
    public boolean finishCurrentStrategy;
    
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

        frameFromLastDecision = 0;
        violateExpectation = false;
        finishCurrentStrategy = false;
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
    public void follow(RVOAgent agt, boolean left) {
        target = agt;
        String fromLeft = "right";
        if (left) {
            fromLeft = "left";
        }
        System.out.println("agent" + this.wm.getMyAgent() + " is following " + "agent " + target.getId() + " from its " + fromLeft);

        //position and velocity determines whether successfully followed
        if (target.getCurrentPosition().distance(this.wm.myAgent.getCurrentPosition()) <= preferGap
                && (Math.abs(target.getVelocity().length() - this.wm.myAgent.getVelocity().length()) / Math.max(agt.getVelocity().length(), this.wm.myAgent.getVelocity().length()) ) <= velocityDiff
                && target.getVelocity().length() >= this.wm.myAgent.getVelocity().length()) {
            finishCurrentStrategy = true;
            return;
        }
        //set preferred velocity for me to follow the target
        Vector2d destinationToMove = approachlane(target, left); //change direction of velocity (returns position to move to)
        adjustSpeedFollow(target, destinationToMove);//change magnitude of velocity
    }

    //used in follow()
    private Vector2d approachlane(RVOAgent agt, boolean left) {
        double randomAngle = 0; //in radian
        //use the updated information of agt all the way during following
        Vector2d locationToMove = new Vector2d(agt.getVelocity());
        locationToMove.normalize();
        locationToMove.negate();
        locationToMove.scale(preferGap);
        locationToMove.add(agt.getCurrentPosition());
        
        MersenneTwisterFast random = wm.getMyAgent().getMySpace().getRvoModel().random;
        if (left) {
            randomAngle = random.nextDouble() * (Math.PI / 4);
        } else {
            randomAngle = random.nextDouble() * (Math.PI / 4) * (-1);
        }

//        rotate2d(locationToMove, randomAngle);
        //rotate the vector around agt's position clokcwise by randomAngle
        rotate2dAroundaPoint(locationToMove, agt.getCurrentPosition(), randomAngle);
        return locationToMove;
    }

    /**
     * Rotates a given vector in 2d in clockwise direction about Z-axis
     *
     * @param v
     * @param angle
     * @return
     */
    private static void rotate2d(Vector2d v, double angle) {
        double newx = v.x * Math.cos(angle) - v.y * Math.sin(angle);
        double newy = v.x * Math.sin(angle) + v.y * Math.cos(angle);
        v.x = newx;
        v.y = newy;
    }

    //rotate vector2d v around point p clockwise by angle
    private void rotate2dAroundaPoint(Vector2d v, Point2d p, double angle){
        double newx = (v.x-p.x) * Math.cos(angle) - (v.y-p.y) * Math.sin(angle) + p.x;
        double newy = (v.y-p.y) * Math.cos(angle) + (v.x-p.x) * Math.sin(angle) + p.y;
        v.x = newx;
        v.y = newy;
    }
    /**
     * for helper function testing
     * @param args
     */
//    public static void main(String args[]){
//        Vector2d v = new Vector2d(1,0);
//        Point2d p = new Point2d(1,1);
//        double angle = Math.PI / 4;
//        Action.rotate2d(v,angle);
//        Action.rotate2dAroundaPoint(v, p, angle);
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

        double approachSpeed = wm.getMyAgent().getVelocity().length() + 0.5 * (agt.getVelocity().length() - wm.getMyAgent().getVelocity().length());
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
            System.out.println("Agent" + wm.getMyAgent() + " is overtaking " + "Agent" + agt + " from the " + leftSide);
        } else {
            System.out.println("Agent" + wm.getMyAgent() + " is overtaking " + "Agent" + agt + " from the " + rightSide);
        }

        Vector2d directionToTarget = new Vector2d(agt.getCurrentPosition());
        directionToTarget.sub(wm.getMyAgent().getCurrentPosition());
        double ahead = utility.Geometry.sameDirection(directionToTarget, startVel);

        if (ahead >= 0) {//this agent is behind the target and trying to catch up
            if (frameFromLastDecision >= T) {
                violateExpectation = true;
                return;
            }
            //execute catching up behavior in phase 1 of overtaking
            CatchUp(agt, left, T);

        } else if (ahead > -0.65) {
            Vector2d passAheadVel = new Vector2d(startVel);
            passAheadVel.scale(wm.getMyAgent().getPreferredSpeed());
            selectedVelocity.set(passAheadVel);
        }
        else{
//           do not implement resume original course, such behavior can emerge through the rough preferredVelocity setting
            finishCurrentStrategy = true;
        }
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
        Vector2d locationToMove = new Vector2d(agt.getVelocity());
        //if this method is called in "Catchup during overtaking"
        if (wm.getDecision().getCurrentStrategy() == STRATEGY.OVERTAKE) {
            locationToMove.negate();
        }
        //if this method is called in "approachToTarget during Side-Avoiding"
        locationToMove.normalize();
//        locationToMove.add(agt.getCurrentPosition());
        rotate2dAroundaPoint(locationToMove, agt.getCurrentPosition(), angle);
        
        if (wm.getDecision().getCurrentStrategy() == STRATEGY.OVERTAKE) {
            locationToMove.scale(this.wm.getMyAgent().getRadius() * (1+this.wm.getMyAgent().getPersonalSpaceFactor())
                    + agt.getRadius() * (1+agt.getPersonalSpaceFactor()) 
                    + 2 * (this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() + 1) * this.wm.getMyAgent().getRadius());
        } else if (wm.getDecision().getCurrentStrategy() == STRATEGY.AVOID) {
            locationToMove.scale(0.5*this.wm.getMyAgent().getRadius() * (1+this.wm.getMyAgent().getPersonalSpaceFactor())
                    + 0.5 * agt.getRadius() * (1+agt.getPersonalSpaceFactor()) 
                    + 1 * (this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() + 1) * this.wm.getMyAgent().getRadius());
        }
        locationToMove.add(agt.getCurrentPosition());
        return locationToMove;
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
        catchUpVelocity.scale(wm.getMyAgent().getSpeed());
        
        if (calculateTTA(this.wm.getMyAgent(), agt) + frameFromLastDecision < T) {
            //if we're ahead of schedule then ok
           
        } else {
            // speed up
            catchUpVelocity.normalize();
            catchUpVelocity.scale(wm.getMyAgent().getSpeed() 
            + 0.5 * wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() * (wm.getMyAgent().getMaxSpeed()-wm.getMyAgent().getSpeed()));
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
    public void avoid(RVOAgent agt, boolean left, int T, final Vector2d startVel, final Point2d startPos) {
        String leftSide = "left side";
        String rightSide = "right side";
        if (left) {
            System.out.println("Agent" + wm.getMyAgent() + " is avoiding " + "Agent" + agt + " from the " + leftSide);
        } else {
            System.out.println("Agent" + wm.getMyAgent() + " is avoiding " + "Agent" + agt + " from the " + rightSide);
        }

        Vector2d directionToTarget = new Vector2d(agt.getCurrentPosition());
        directionToTarget.sub(wm.getMyAgent().getCurrentPosition());
        directionToTarget.normalize();
        //cos(theta), where theta is the angle between vector(directionToTarget) and vector (myAgent's velocity)
        double ahead = Geometry.sameDirection(directionToTarget, startVel);
        //ahead> 0 means angle <90, means, myAgent still not cross target agt yet
        if (ahead > 0) {
            if (frameFromLastDecision >= T) {
                violateExpectation = true;
                return;
            }
          //here, the finishCurrentStrategy condition can be more restricted as once there is enuf for space already moving towards its goal, no need to further deviate
           Point2d targetGoalPos = agt.getGoal();
           Point2d myGoal = wm.getMyAgent().getGoal();
           
           Point2d targetCurrentPos = agt.getCurrentPosition();
           Point2d myCurrentPos = wm.getMyAgent().getCurrentPosition();
                      
           Vector2d myToGoal = new Vector2d(myGoal);
           myToGoal.sub(myCurrentPos);
           
           Vector2d targetToGoal = new Vector2d(targetGoalPos);
           targetToGoal.sub(targetCurrentPos);
           
           double distanceToLine = 0;
           double myDistToTargetLine = 0;
           double targetDistToMyLine = 0;
           
           double slopeMyLine = (targetCurrentPos.x-myCurrentPos.x)*(myGoal.x-myCurrentPos.x) + (targetCurrentPos.y-myCurrentPos.y)*(myGoal.y-myCurrentPos.y);
           slopeMyLine = slopeMyLine / Math.pow(myToGoal.length(), 2);
           
           double crossOnMyLine_X = myCurrentPos.x + slopeMyLine * (myGoal.x-myCurrentPos.x);
           double crossOnMyLine_Y = myCurrentPos.y + slopeMyLine * (myGoal.y-myCurrentPos.y);
           
           targetDistToMyLine = Math.pow((crossOnMyLine_X-targetCurrentPos.x), 2) + Math.pow((crossOnMyLine_Y-targetCurrentPos.y), 2);
           targetDistToMyLine = Math.sqrt(targetDistToMyLine);
           
           
           double slopeTargetLine = (myCurrentPos.x-targetCurrentPos.x)*(targetGoalPos.x-targetCurrentPos.x) + (myCurrentPos.y-targetCurrentPos.y)*(targetGoalPos.y-targetCurrentPos.y);
           slopeTargetLine = slopeTargetLine / Math.pow(targetToGoal.length(), 2);
           
           double crossOnTargetLine_X = targetCurrentPos.x + slopeTargetLine * (targetGoalPos.x-targetCurrentPos.x);
           double crossOnTargetLine_Y = targetCurrentPos.y + slopeTargetLine * (targetGoalPos.y-targetCurrentPos.y);
           
           myDistToTargetLine = Math.pow((crossOnTargetLine_X-myCurrentPos.x), 2) + Math.pow((crossOnTargetLine_Y-myCurrentPos.y), 2);
           myDistToTargetLine = Math.sqrt(myDistToTargetLine);
           
           distanceToLine =  Math.min(targetDistToMyLine, myDistToTargetLine);
           double personalSpaceRadius = Math.max(wm.getMyAgent().getRadius() * (1+wm.getMyAgent().getPersonalSpaceFactor()), agt.getRadius()*(1+agt.getPersonalSpaceFactor()));
           if(distanceToLine>= personalSpaceRadius){
                finishCurrentStrategy = true;
                return;
            }      
            Vector2d deviatedVel = approachSide(agt, left);
            deviatedVel.scale(wm.getMyAgent().getPreferredSpeed()* 0.9);
            selectedVelocity = new Vector2d(deviatedVel);
        } else {
            finishCurrentStrategy = true;
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
        this.finishCurrentStrategy = true;
    }

    public void execute(STRATEGY selectedStrategy, RVOAgent target, boolean left, int T, Vector2d startVel, Point2d startPos) {
        this.target = target;
        switch (selectedStrategy) {
            case MOVE:
                //follow the rough preferredVel towatds its goal
                if(frameFromLastDecision>=T){
                    finishCurrentStrategy=true;
                }else{
                    selectedVelocity = wm.getMyAgent().getPrefVelocity();
                    frameFromLastDecision++;
                    System.out.println("moving");
                }
                break;
            case FOLLOW:
                System.out.println("executing follow steering strategy now");
                follow(this.target, left);
                frameFromLastDecision++;
                break;
            case OVERTAKE:
                System.out.println("executing overtake steering strategy now");
                overtake(this.target, left, T, startVel, startPos);
                frameFromLastDecision++;
                break;
            case AVOID:
                System.out.println("executing avoiding steering strategy now");
                avoid(this.target, left, T, startVel, startPos);
                frameFromLastDecision++;
                break;
            //SUDDENSLOW is one example of instinctive reaction, where is full of obstacles in front and no other strategy can be executed
            case INSTINCTIVERACTION:
                System.out.println("executing instinctive reaction - stop the Agent emergently");
                wm.getMyAgent().getVelocity().scale(0); //scale the velocity to 0
                finishCurrentStrategy = true;
                break;
        }
    }

    /*
    public void updateState() {
    updateVelocity(); //update velocity for the agent
    frameFromLastDecision++;
    //where to update the position of the agent according to their velocity????

    if (wm.getDecision().needNewDicison()) {
    wm.getDecision().selectNewStrategy();//verify stratey and  execute selected strategy accordingly
    }
    //TODO: hunan: I am not sure whether need to call the function again to contiuously execute current strategy if no new decision is needed, or will it automatically execute the current strategy in the next frame
    //else executeStrategy(currentAction,target,this.decision.left,this.decision.instructedTime);
    }
     */
    
    private void initPreferredGaps() {
        //for controlling gaps when follow and overtake is performed
        preferGap = (wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() * 1 + 2) * (1+wm.getMyAgent().getPersonalSpaceFactor()) * wm.getMyAgent().getRadius(); //random represent the other agent's personal space
    }

}
