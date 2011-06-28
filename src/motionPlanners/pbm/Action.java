package motionPlanners.pbm;

import agent.RVOAgent;
import ec.util.MersenneTwisterFast;
import javax.vecmath.Vector2d;
import javax.vecmath.Point2d;
import motionPlanners.pbm.WorkingMemory.STRATEGY;
import utility.Geometry;

/*
 * Define realistic data
 * Attention range = 2.5m
 * Body size = 50cm (diameter)
 * Average speed = 0.4m/s
 * Frame = 300ms (according each frame is one step)
 */
public class Action {
//        public static enum AggressiveLevel {
//            AGREEABLE, AGRESSIVE, VERYAGRESSIVE
//        }
    //commitment to the preferred speed when it is high, this factor affects both in overtaking and avoiding

//    public static final int MOVE = 0;
//    public static final int FOLLOW = 1;
//    public static final int OVERTAKE = 2;
//    //public static final int AVOID = 3;
    //parameters for action execution
    private RVOAgent target;
    //define speed
    /**
     * relative vector to modify the currentSpeed.
     * Used to modify currentspeed in order to execute motion.
     */
    //TODO-------------this velocity may not be useful any more, consider to delete after compilation succeeds
    //private PbmVelocity steerVelocity;
    /**
     * The selected speed is calculated by the system at each time step.
     * It is defined as the currentSpeed + steerSpeed.
     * Once the selectedSpeed is finalised, at the next timestep the
     * currentSpeed = selectedSpeed.
     */
    private PbmVelocity selectedVelocity;
    /**
     * Maximum speed of agent, fixed at initialization
     */
    //public final PbmVelocity maxSpeed;
    /**
     * preferred speed is a aggregated parameter of higher level cognitive
     * process, reflect current preferred strategy
     */
    private PbmVelocity preferredVelocity; //
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
    public boolean finishCurrentStrategy = false;
    /**
     * For this mechanism we set a maximum speed of 4 m/s
     */
    private double maxSpeed = 1.8; //approximate max walking speed for pedestrians is around 350ft/min, according to Young's work
    WorkingMemory wm;
    /**
     * Relative velocity difference necessary in order to trigger completion of
     * strategy (e.g., follow). In order for follow to be considered complete
     * the velocity of the two agents involved must have less than 3% difference
     */
    public double velocityDiff = 0.05;

    public Action(WorkingMemory wm) {

        this.wm = wm;

////        steerVelocity = new PbmVelocity(0, 0);
//        Vector2d testV = wm.getMyAgent().getPrefVelocity();
//        PbmVelocity pbmV = new PbmVelocity(testV);
//        System.out.println("Theta:" + pbmV.theta + " mag " + pbmV.magnitude);

        preferredVelocity = new PbmVelocity(wm.getMyAgent().getPrefVelocity()); //can use magnitude * 1.x and angle * 1.x to represent a range for preferred speed
        selectedVelocity = new PbmVelocity(wm.getMyAgent().getVelocity());
        if (preferredVelocity.magnitude < 0.8) {
            wm.setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.SLOW);
        } else if (preferredVelocity.magnitude > 1.4) {
            wm.setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.FAST);
        } else {
            wm.setPs_discrete(WorkingMemory.PreferredSpeed_Discrete.MID);
        }
        frameFromLastDecision = 0;
        violateExpectation = false;
        finishCurrentStrategy = false;
        initPreferredGaps();
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getPreferGap() {
        return preferGap;
    }

    public PbmVelocity getPreferredVelocity() {
        return preferredVelocity;
    }

    public PbmVelocity getSelectedVelocity() {
        return selectedVelocity;
    }

//    public PbmVelocity getSteerVelocity() {
//        return steerVelocity;
//    }
    /**
     * Updates the selected velocity 
     */
    public Vector2d getVelocity() {
//        Vector2d newVelocity = new Vector2d(selectedVelocity.speed_x, selectedVelocity.speed_y);
//
//        if (newVelocity.length() > maxSpeed) {
//            newVelocity.normalize();
//            newVelocity.scale(maxSpeed);
//        }
//
//        //update total speed
//        selectedVelocity.setSpeed2(newVelocity.x, newVelocity.y);

        //set the velocity inside the agent
        return new Vector2d(selectedVelocity.speed_x, selectedVelocity.speed_y);
    }

    /**
     * Execution of Follow steering strategy by setting the velocity in locomotion level
     * follow a particular agent
     * approaching the agent from the left or right side of the target depends on the current relative position of the two agents
     */
    public void follow(RVOAgent agt, boolean left, int T) {
        //Set target for follow
        target = agt;
        String fromLeft = "right";
        if (left) {
            fromLeft = "left";
        }
        System.out.println("agent" + this.wm.getMyAgent() + " is following " + "agent" + agt + " from its " + fromLeft);

        if (agt.getCurrentPosition().distance(this.wm.myAgent.getCurrentPosition()) <= preferGap
                && Math.abs(agt.getVelocity().length() - this.wm.myAgent.getVelocity().length()) / Math.max(agt.getVelocity().length(), this.wm.myAgent.getVelocity().length()) <= velocityDiff
                && agt.getVelocity().length() >= this.wm.myAgent.getVelocity().length()) {
            finishCurrentStrategy = true;
            return;
        }
        boolean blocked = false;
        Vector2d DirectionToTarget = new Vector2d(agt.getCurrentPosition());
        DirectionToTarget.sub(wm.getMyAgent().getCurrentPosition());
        DirectionToTarget.normalize();

        RVOAgent[] currentRow_agt = new RVOAgent[wm.getVision().getSpacepattern_agt()[0][0].length];
        int[] currentFrontRow = wm.getVision().getCurrentView_1stRow(DirectionToTarget, currentRow_agt);
        int midIndex = currentFrontRow.length / 2;
        if (currentFrontRow[midIndex] != 0 && currentRow_agt[midIndex] != target) {
            blocked = true;
        }
        //Check expectancies according to time.
        if (frameFromLastDecision > T || blocked) {
            violateExpectation = true;
            return;
        }

        //execute necessary sub strategies
        Vector2d destinationToMove = approachlane(agt, left); //change direction of velocity (returns position to move to)
        adjustSpeedFollow(agt, destinationToMove);//change magnitude of velocity
    }

    //used in follow()
    //implement lane approaching behavior, assume steering speed in Y only during this phase
    //not based on "pattern", not taken into account of the temporal factors for now, but can be improved later
    private Vector2d approachlane(RVOAgent agt, boolean left) {
        double randomAngle = 0;
        //use the updated information of agt all the way during following
        Vector2d locationToMove = new Vector2d(agt.getVelocity());
        locationToMove.normalize();
        locationToMove.negate();
        locationToMove.scale(preferGap);
        MersenneTwisterFast random = wm.getMyAgent().getMySpace().getRvoModel().random;
        if (left) {
            randomAngle = random.nextDouble() * (Math.PI / 4);
        } else {
            randomAngle = random.nextDouble() * (Math.PI / 4) * (-1);
        }
//        rotate2d(locationToMove, randomAngle);
        //rotate the vector around agt's position clokcwise by randomAngle
        rotate2d(locationToMove, randomAngle);
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
    private static void rotate2d(Vector2d v, double angle) {
        double newx = v.x * Math.cos(angle) - v.y * Math.sin(angle);
        double newy = v.x * Math.sin(angle) + v.y * Math.cos(angle);
        v.x = newx;
        v.y = newy;
    }

//    //rotate vector2d v around point p clockwise by angle
//    private void rotate2dAroundaPoint(Vector2d v, Point2d p, double angle){
//        double newx = (v.x-p.x) * Math.cos(angle) - (v.y-p.y) * Math.sin(angle) + p.x;
//        double newy = (v.y-p.y) * Math.cos(angle) + (v.x-p.x) * Math.sin(angle) + p.y;
//        v.x = newx;
//        v.y = newy;
//    }
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
     * Sets speed for follow?
     * @param agt
     * @param pointToMove
     */
    private void adjustSpeedFollow(RVOAgent agt, Vector2d pointToMove) {
        //follow speed control
        pointToMove.sub(this.wm.getMyAgent().getCurrentPosition());
        pointToMove.normalize();

        //new Speed is proportional to relative speed difference
        //assume target is slower than this agent, otherwise follow doesn't make sense

        double approachSpeed = wm.getMyAgent().getVelocity().length() + 0.5 * (agt.getVelocity().length() - wm.getMyAgent().getVelocity().length());
        pointToMove.scale(approachSpeed);

        selectedVelocity.setSpeed2(pointToMove.x, pointToMove.y);
//        this.steerVelocity.setSpeed2(0, 0);

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
//        boolean phase2Finished = false;

        //TODO: check whether front is blocked by anyone

        if (ahead >= 0) {//this agent is behind the target and trying to catch up
//            if (frameFromLastDecision >= T) {
//                violateExpectation = true;
//                return;
//            }
            //execute catching up behavior in phase 1 of overtaking
            CatchUp(agt, left, T);

        } else if (ahead > -0.6) {
             Vector2d passAheadVel = new Vector2d(startVel);
            passAheadVel.scale(1.2);
                 selectedVelocity.setSpeed2(passAheadVel.x, passAheadVel.y);
        }
        else{
//            //condition to check whether phase 2 is finished, phase2Finished will be checked and modified accordingly in PassAhead()
//            if (!phase2Finished) {
//                //in this condition, the ahead is almost zero,but <0, means myAgent is ahead of target and will further pass ahead the target for some distance
//                RVOAgent[] currentRow_agt = new RVOAgent[wm.getVision().getSpacepattern_agt()[0][0].length];
//                int[] currentRow = this.wm.vision.getCurrentView_1stRow(wm.getMyAgent().getVelocity(), currentRow_agt);
//                if (currentRow[currentRow.length / 2] != 0) {
//                    violateExpectation = true;
//                    return;
//                }
//                phase2Finished = PassAhead(agt);//pass && ahead
//            } else {
//                //TODO: refine condition for steering back, so as for the whole overtaking strategy
//                //currently: condition for the overtaking strategy to be finsihed by some threshold in time
//                //rough approximation that the total 3 phases time cost = 2 * Time cost for phase 1
//                if (frameFromLastDecision >= 2 * T) {
//                    finishCurrentStrategy = true;
//                    return;
//                }
//                RVOAgent[] currentRow_agt = new RVOAgent[wm.getVision().getSpacepattern_agt()[0][0].length];
//                int[] currentRow = this.wm.vision.getCurrentView_1stRow(wm.getMyAgent().getVelocity(), currentRow_agt);
//                if (left) {
//                    if (currentRow[currentRow.length / 2 + 1] != 0) {
//                        violateExpectation = true;
//                        return;
//                    }
//                } else {
//                    if (currentRow[currentRow.length / 2 - 1] != 0) {
//                        violateExpectation = true;
//                        return;
//                    }
//                }
//                double p_resumePath = this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble();
//                if (p_resumePath <= 0.5) {
//                    finishCurrentStrategy = true;
//                    return;
//                } //TODO: check whether the original path blocked could be simple by just using the isObstacle() function in RVOSpace class
//                else if (!this.isOrgPathBlocked()) { //%50 probability of going back to original path
//                    steerBack(startPos, startVel, left);
//                }
//            }

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
        rotate2d(locationToMove, angle);
        if (wm.getDecision().getCurrentStrategy() == STRATEGY.OVERTAKE) {
            locationToMove.scale(1.5 * (this.wm.getMyAgent().getRadius() * this.wm.getMyAgent().getPersonalSpaceFactor() + agt.getRadius() * agt.getPersonalSpaceFactor()) + (2 * this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() + 1) * 0 * this.wm.getMyAgent().getRadius());
        } else if (wm.getDecision().getCurrentStrategy() == STRATEGY.AVOID) {
            locationToMove.scale(1.5 * (this.wm.getMyAgent().getRadius() * this.wm.getMyAgent().getPersonalSpaceFactor() + agt.getRadius() * agt.getPersonalSpaceFactor()) + (2 * this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() + 1) * 1 * this.wm.getMyAgent().getRadius());
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
        if (this.wm.decision.calculateTTA(this.wm.getMyAgent(), agt) < T) {
            //if we're ahead of schedule then ok
           
        } else if (this.wm.decision.calculateTTA(this.wm.getMyAgent(), agt) > T) {
            // speed up
            catchUpVelocity.normalize();
            //inrease the speed at most by 30% each step, this is already much
//            catchUpVelocity.scale(wm.getMyAgent().getVelocity().length() * (1 + this.wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble()* 0.1));
            //TODO: make mechanics more realistic
            catchUpVelocity.scale(wm.getMyAgent().getVelocity().length());
        }
        selectedVelocity.setSpeed2(catchUpVelocity.x, catchUpVelocity.y);
//        this.steerVelocity.setSpeed2(0, 0);
    }

    /**
     * The second phase of overtake, moves ahead for a certain
     * distance prior to cutting in front.
     *
     * @param agt
     */
    private boolean PassAhead(RVOAgent agt) {
        boolean phase2Finished = false;
        MersenneTwisterFast random = wm.getMyAgent().getMySpace().getRvoModel().random;
        Vector2d directionToMe = new Vector2d(wm.myAgent.getCurrentPosition());
        directionToMe.sub(agt.getCurrentPosition());

        int numberOfPeopleAheadtoPass = 3 + random.nextInt(3);

        if (Geometry.sameDirection(directionToMe, agt.getVelocity()) > 0
                && directionToMe.length() > (agt.getRadius() * (numberOfPeopleAheadtoPass + 2) * 2)) {
            //In this case we are in front of the target agent and at least a
            //distance of between 3 and 6 people away. Once we have reached this
            //we have completed the second phase of overtake
            wm.getMyAgent().findPrefVelocity();
            Vector2d newPrefVel = new Vector2d(wm.getMyAgent().getPrefVelocity());
            selectedVelocity.setSpeed2(newPrefVel.x, newPrefVel.y); //when phase 2 finishes, set myAgent's velocity to be its preferredVelocity
//            this.steerVelocity.setSpeed2(0, 0);
            phase2Finished = true;
        } else {
//            phase2Finished = false;
            //Otherwise we need to execute the pass ahead strategy, which is to align velocity
            //with the target agent, plus some slight random variation to the target velocity to make myAgent move faster to pass ahead the target
            Vector2d passVel = new Vector2d(agt.getVelocity().x * random.nextDouble() * 0.02, agt.getVelocity().y * random.nextDouble() * 0.02);
            passVel.scale(1 + random.nextDouble() * 0.1);
            selectedVelocity.setSpeed2(passVel.x, passVel.y);
//            this.steerVelocity.setSpeed2(0, 0);
        }
        return phase2Finished;
    }

    /**
     * Final phase of overtake (after PassAhead), 50% chance of being called.
     *
     * Moves in front of the target agent at some random distance.
     *
     * @param agt
     * @param left
     */
    private void steerBack(Point2d startPosbefOvertake, Vector2d startPosDirection, boolean left) {
        //need to make use of intersection of two lines to specify the aiming position for steerBack
        MersenneTwisterFast random = wm.getMyAgent().getMySpace().getRvoModel().random;
        double randomSteerAngle = Math.PI / 4; //default steering angle is 45 degree
        if (wm.getDecision().getCurrentStrategy() == STRATEGY.OVERTAKE) {
            randomSteerAngle = (random.nextDouble() * 1 / 12 + 1 / 6) * Math.PI;    //rotate clockwise rotate angle from my current velocity direction range from 30 to 45 degree
        } else if (wm.getDecision().getCurrentStrategy() == STRATEGY.AVOID) {
            randomSteerAngle = (random.nextDouble() * 1 / 6 + 1 / 6) * Math.PI; //steering direction can be range from 30 to 60 degree
        }
        //first, get the current position and velocity direction
        Point2d myCurrentPosition = new Point2d(wm.getMyAgent().getCurrentPosition());
        Vector2d steerBackDirection = new Vector2d(wm.getMyAgent().getVelocity());
        steerBackDirection.normalize();
        //rotate the vector of my current velocity clockwise by a random steering angle
        if (left) {
            rotate2d(steerBackDirection, randomSteerAngle);
        } else {
            rotate2d(steerBackDirection, randomSteerAngle * (-1));
        }
//        steerBackDirection.add(myCurrentPosition);

        //given two points and the vector representing the direction at the points, two lines can be identified, pass in the function to check the intersection point
        Point2d intersectPoint = line2lineIntersection(myCurrentPosition, steerBackDirection, startPosbefOvertake, startPosDirection);
        if (intersectPoint == null) {
            System.out.println("exception occurs during steering back, steering back is not executed");
            return;
        }
        //1. to set the direction of the velocity during steering back
        Vector2d DestinationToSteerBack = new Vector2d(intersectPoint);
        DestinationToSteerBack.sub(myCurrentPosition);
        DestinationToSteerBack.normalize();
        //2. to set the speed of the velocity during steering back
        //currently, we set the steering back speed as the preferred speed, can add in more variation in the future
        DestinationToSteerBack.scale(wm.getMyAgent().getPrefVelocity().length());
        selectedVelocity.setSpeed2(DestinationToSteerBack.x, DestinationToSteerBack.y);
//        this.steerVelocity.setSpeed2(0, 0);
    }

    /*
     * To return the intersection point of two lines, not segment
     * startPosbefOvertake
     */
    private Point2d line2lineIntersection(Point2d point1, Vector2d direction1, Point2d point2, Vector2d direction2) {
        //get the tangent, slope of the two lines
        double slope1 = Math.tan(direction1.y / direction1.x);
        double slope2 = Math.tan(direction2.y / direction2.x);

        //get equation for line 1 and line 2 - defined by point-slope formulae
        // point.y = slope * point.x + c
        double c1 = point1.y - slope1 * point1.x;
        double c2 = point2.y - slope2 * point2.x;

        //check whether 2 lines are parallel to each other
        //if parellel or overlap onto each other, then there is no intersection
        if (slope1 == slope2) {
            return null;
        }
        double newX = (c2 - c1) / (slope1 - slope2);
        double newY = newX * slope1 + c1;
        Point2d intersectionPoint = new Point2d(newX, newY);
        return intersectionPoint;
    }

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
        if (ahead > 0) {//agents getting closer
            wm.getMyAgent().findPrefVelocity();
            Vector2d directionToGoal = new Vector2d(wm.getMyAgent().getPrefVelocity());
            double cosineAlfa = Math.abs(Geometry.sameDirection(startVel, directionToGoal));
            Point2d goalPos = wm.getMyAgent().getGoal();
            Vector2d toGoal = new Vector2d(goalPos);
            toGoal.sub(wm.getMyAgent().getCurrentPosition());
            double distanceToGoal = toGoal.length();
            //in this condition, the path towards goal is clear, avoiding can be considered as finished
            if (distanceToGoal * Math.sqrt(1 - cosineAlfa * cosineAlfa) >= 1.2 * wm.getMyAgent().getRadius() * wm.getMyAgent().getPersonalSpaceFactor()) {
                System.out.println("succesfully avoided oncoming agent");
                double p_resumePath = wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble();
                if (p_resumePath <= 0.5) {
                    finishCurrentStrategy = true;
                    return;
                } else if (!isOrgPathBlocked()) {
                    steerBack(startPos, startVel, left);
                } else {
                    finishCurrentStrategy = true;
                    System.out.println("trying to resume to original course but failed due to the blockage on the way");
                    return;
                }
            }
            //function to deviate the velocity to the side of target agent in order to avoid collision
            approachToTarget(agt, left, startVel);
        } else {
            finishCurrentStrategy = true;
            return;
        }
    }

    /**
     * this method is called in avoid steering strategy to deviate the velocity slightly to the selected side of the target
     * in order to avoid the oncoming agent
     */
    private void approachToTarget(RVOAgent agt, boolean left, Vector2d startVel) {
        Vector2d avoidingVelocity = new Vector2d(approachSide(agt, left));
        avoidingVelocity.sub(wm.getMyAgent().getCurrentPosition());
        avoidingVelocity.normalize();
//        double cosAngle =  Geometry.sameDirection(avoidingVelocity, startVel);
        //normally, during side-avoiding in face-to-face avoiding situations, speed may reduce slightly to make sure the other part has time to react to the oncoming agent
        avoidingVelocity.scale(wm.getMyAgent().getVelocity().length());
        selectedVelocity.setSpeed2(avoidingVelocity.x, avoidingVelocity.y);
//        this.steerVelocity.setSpeed2(0, 0);
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
//                selectedVelocity.setSpeed2(this.wm.getMyAgent().getVelocity().x, this.wm.getMyAgent().getVelocity().y);
                wm.getMyAgent().findPrefVelocity();
                Vector2d newPrefVel = new Vector2d(wm.getMyAgent().getPrefVelocity());
                selectedVelocity.setSpeed2(newPrefVel.x, newPrefVel.y);
                break;
            case FOLLOW:
                System.out.println("executing follow steering strategy now");
                follow(this.target, left, T);
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
            case SUDDENSLOW:
                System.out.println("executing instinctive reaction - suddenly slow down the speed");
                Vector2d suddenSlowDown = wm.getMyAgent().getVelocity();
                //cut speed by half
                suddenSlowDown.scale(0.5);
                selectedVelocity.setSpeed2(suddenSlowDown.x, suddenSlowDown.y);
                break;
            case SIDESLIDING:
                System.out.println("executing instinctive reaction - side slidng");
                slideaside(left);
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
        preferGap = (wm.getMyAgent().getMySpace().getRvoModel().random.nextDouble() * 2 + 3) * wm.getMyAgent().getPersonalSpaceFactor() * wm.getMyAgent().getRadius(); //random represent the other agent's personal space
    }

    private boolean isOrgPathBlocked() {
        boolean blocked = false;
        RVOAgent[] currentRow_agt = new RVOAgent[wm.getVision().getSpacepattern_agt()[0][0].length];
        int[] currentRow = wm.getVision().getCurrentView_1stRow(wm.getMyAgent().getVelocity(), currentRow_agt);
        if (wm.getDecision().isLeft()) {
            if (currentRow[currentRow.length / 2 + 1] != 0) {
                blocked = true;
            }
        } else {
            if (currentRow[currentRow.length / 2 - 1] != 0) {
                blocked = true;
            }
        }
        return blocked;
    }

    void setFinishedStrategy(boolean b) {
        this.finishCurrentStrategy = b;
    }
}
