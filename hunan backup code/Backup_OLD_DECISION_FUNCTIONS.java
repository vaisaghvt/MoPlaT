    /**
     * currently, just a simple function to MODEL phase-by-phase pattern-matching process
     *
     * @param strategyID
     * @param t1
     * @return
     */
    private boolean patternMatching(int strategyID, int t1) {
        boolean success = false; //initial value
        switch (strategyID) {
            //in principle, this case 0 can never be achieved
            case 0:
                System.out.println("No need to match pattern");
                success = false;
                break;
            //pattern matching for avoiding
            case -1:
                if (targetIndex < 0) {
                    System.out.println("no potential target for avoiding");
                    success = false;
                } else {
                    System.out.println("Need to match pattern for side-avoiding");
                    success = verifySpace_phase1(t1);
                }
                break;
            //pattern matching for overtaking phase 1 (catching up)
            case 1:
                System.out.println("Need to match pattern for phase 1 of overtaking");
                if (targetIndex < 0) {
                    System.out.println("no potential target for overtaking");
                    success = false;
                } else {
                    success = verifySpace_phase1(t1);
                }
                break;
            //pattern matching for both phase 1 and phase 2 in overtaking
            case 2:
                System.out.println("Need to match pattern for both phase 1 and phase 2 of overtaking");
                if (targetIndex < 0) {
                    System.out.println("no potential target for overtaking");
                    success = false;
                } else {
                    success = verifySpace_phase1n2(t1);
                }
                break;
        }
        return success;
    }


/**
     * Main method called to calculate new strategy. Strategy selection is
     * based on two things:
     * 1. The input from the vision system (pattern)
     * 2. The high  level cognitive strategy selection (i.e., preferred strategy).
     */
    private void selectNewStrategy() {
        System.out.println("start decision on which strategy to select at current situation");
        //1. form 3D array to represent the SA at current frame

        //call grouping function (personal space is considered here)
        //return column index of the POTENTIAL target at the edge of the center group next to the available space
        targetIndex = spatialGrouping(wm, 0);

        if (targetIndex == -1 && targetGroup == 2) {
            System.out.println("case 1. No space to execute steering strategy, resolve to instinctive reactions"); //maybe slow down
            wm.getMyAgent().setColor(Color.yellow);
            this.setCurrentStrategy(STRATEGY.SUDDENSLOW);
        } else if (targetIndex == -1 && targetGroup == 0) {
            System.out.println("case 2. No one is blocking myAgent's way in front");
            this.setCurrentStrategy(STRATEGY.MOVE);
            wm.getAction().finishCurrentStrategy = true;
        } else {
            //expect how many frames from now on can me rich the boundary of the first attention range
            System.out.println("target is " + targetAgent + "   " + targetAgent.getId() + " " + targetAgent.getVelocity());
            int t1 = calculateTTA(myAgent, targetAgent);
            System.out.println("time to reach the perceived space is " + t1 + "frames");
            //2. mimic the CHECKUP TABLE against experience for "steering strategy verification"
            //This part is only for agents whose preferred speed is fast
            if (wm.ps_discrete != WorkingMemory.PreferredSpeed_Discrete.SLOW) {
//                //the center in front is not blocked
//                if (targetGroup == 0) {
//                    this.setCurrentStrategy(STRATEGY.MOVE);
//                    System.out.println("myAgent: no obstacle blocked my way in front, move as usual");
//                    //   wm.getAction().updateState(); //call default action, no strategy is to be executed
//                    //if no strategy is selected at current frame, in the next frame, it will make decision again
//                    wm.getAction().finishCurrentStrategy = true;
//                } //the front center is blocked and there is someone coming towards myAgent, which creates higher threat for collision than those moving in same direction
                if (targetGroup == -1) {
                    if (wm.getMyAgent().getCommitementLevel() != WorkingMemory.strategymatchingCommitment.HIGHCOMMITMENT) {
                        boolean matchforavoid = patternMatching(-1, t1);
                        if (matchforavoid) {
                            System.out.println("case 3-1. Pattern-Matched successful for Avoiding");
                            this.setCurrentStrategy(STRATEGY.AVOID);
                            //record the current position and velocity of myAgent for steering back and checking "ahead"
                            //create a copy of myAgent's current position for the use of steerback() function
                            startPosbefExe = new Point2d(wm.getMyAgent().getCurrentPosition());
                            startPosVel = new Vector2d(wm.getMyAgent().getVelocity());
                            startPosVel.normalize();
                            //      wm.getAction().execute(STRATEGY.AVOID, targetAgent, left, instructedTime);
                        } else {
                            //instinctive reactions(slideaside or stop) when no space on either side of the avoiding target
                            System.out.println("case 3-1. Pattern didnt successfully matched for Avoiding, executing Sidesliding");
                            this.setCurrentStrategy(STRATEGY.SIDESLIDING);
                        }
                    } else { //commitToHIghSpeed is HIGH
                        this.setCurrentStrategy(STRATEGY.MOVE);
//                        wm.getAction().finishCurrentStrategy = true;
                        //in this case, the goal of "AVOID" never can achive, so new decision needs to be made next frame
                    }
                } //the front center is blocked by a gourp of agents with same direction as myAgent
                else if (targetGroup == 1) {
                    if (wm.getMyAgent().getCommitementLevel() != WorkingMemory.strategymatchingCommitment.HIGHCOMMITMENT) {
                        //check spacepattern for both phase 1 and phase 2
                        //the agent is committed to more levels in the pattern-matching, it is more cautious
                        boolean matchforovertake_1n2 = patternMatching(2, t1);
                        if (matchforovertake_1n2) {
                            //within verifybyPhase(2), should set side and instructedTime accordingly
                            System.out.println("case 4-1. Patternlevels-Matched both 2 phases successful for Overtaking (high commitment level for agent)");
                            this.setCurrentStrategy(STRATEGY.OVERTAKE);
                            //create a copy of myAgent's current position for the use of steerback() function
                            startPosbefExe = new Point2d(wm.getMyAgent().getCurrentPosition());
                            startPosVel = new Vector2d(wm.getMyAgent().getVelocity());
                            startPosVel.normalize();
                        } else {
                            System.out.println("case 4-2. Pattern-Matched both 2 phases failed for Overtaking, executing following instead (high commitment for agent)");
                            this.setCurrentStrategy(STRATEGY.FOLLOW);
                            wm.getAction().finishCurrentStrategy = true; //prompt the agent to make new decision next frame
                        }
                    } else {
                        //commitment level to high speed is high, which means less requirement on environment, as long as phase 1 got space, will start to overtake
                        boolean matchforovertake_1 = patternMatching(1, t1);
                        if (matchforovertake_1) {
                            System.out.println("case 5-1. Pattern-Matched phase 1 successful for Overtaking,start executing Overtaking (low commitment of strategy planning for agent)");
                            this.setCurrentStrategy(STRATEGY.OVERTAKE);
                            startPosbefExe = new Point2d(wm.getMyAgent().getCurrentPosition());
                            startPosVel = new Vector2d(wm.getMyAgent().getVelocity());
                            startPosVel.normalize();
                        } else {
                            this.setCurrentStrategy(STRATEGY.FOLLOW);
                            System.out.println("case 5-2. Pattern-Matched phase 1 failed for Overtaking,start executing Following instead (low commitment of strategy planning for agent)");
                            wm.getAction().finishCurrentStrategy = true;
                        }
                    }
                }
            } //for agents, whose preferred speed is slow
            else {
                //TODO: here, actually no much steering strategy for agents whoes preferredVelocity is slow
                //For future work, can consider "active follow" here, not as the backup strategy for overtaking.
                //in "active follow", 2 things to do
                /*
                 * 1. grouping of agents according to 1) position (2d array in single frame) 2) velocity similarity (number of frames to maintain the similar group along different frames (3d))
                 * 2. choice of group to follow depends on 1) direction deviation from current velocity
                 *    2) speed change from current velocity 3) direction deviation from current goal
                 */
            }
        }
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
        double relativeRadius = myAgt.getRadius() + targetAgt.getRadius();
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

    /**
     * calculate TTA based on the maximum speed of Agent a
     *
     * @param a
     * @param b
     * @return
     */
    protected int calculateTTA_MaxSpeed(RVOAgent a, RVOAgent b) {
        int frameNum = Integer.MAX_VALUE; //agent a can never collide with agent b
        Vector2d relativeS = new Vector2d(a.getVelocity());
        relativeS.normalize();
        relativeS.scale(wm.getAction().getMaxSpeed());
        relativeS.sub(b.getVelocity());
        double distanceToMove = Integer.MAX_VALUE; //if the relative speed vector doesnt cut the relative circle

        Point2d p1 = a.getCurrentPosition();
        Point2d p2 = b.getCurrentPosition();
        Vector2d pa2pb = new Vector2d(p2);
        pa2pb.sub(p1);
        double relativeDistance = pa2pb.length();

        //TODO, shall we use radius or radius*personalSpaceFactor here?
        double relativeRadius = a.getRadius() + b.getRadius();
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
            frameNum = (int) ((distanceToMove / relativeS.length()) / PropertySet.TIMESTEP);
            return frameNum;
        }
    }

    /**
     * Return the target agent index. target is selected from a group, which is 
     * based on the array and personal space set parameter "left", to indicate
     * from which side of the current moving direction gonna this agent execute 
     * the strategy also set the groupCenter to indicate whether the center 
     * group is 0 or -1 or 1(corresponding to different preferred strategy)
     *
     * @param wm
     * @param frame
     * @return
     */
    //  for group center (focus of the pattern)
        /*
     * xxxx1-11xxxxxx
     * xxxx-111xxxxxx
     * xxxx-10-1xxxxx
     * assumptions here:
     * 1. -1 is more important
     * 2. continuous 0, including the midIndex, need to meet threshold: myspace/bodysize
     */
    /*
     * here, it just check briefly whether the first row at the current frame got available space that meets the personal space
     * later when this is done, more detailed check involving time factor will be carried out in verifybyPhase() function
     */
    //grouping agents for the first row in current frame based on direction and personal space
    private int spatialGrouping(WorkingMemory wm, int frame) {

        Point2d spaceIndecies = new Point2d(0, wm.getVision().spacepattern[0][0].length - 1);

        /*
         * previously,  check all 3 levels of attention for possible target, now only check the first level, because targets far away
         * from the first attention range is too dynamic to monitor, leave it to the pattern-matching process
         */
        //    int tempRow [] = new int[wm.getVision().spacepattern[frame][0].length];
        //   for(int x = 0; x <wm.getVision().spacepattern[frame][0].length; x++)
        //      tempRow[x] = wm.getVision().spacepattern[frame][0][x];
        //get the space indecies, in the array, which meets 2 requirements
        //1. got at least threshold number of continuous 0s
        //2. the space is nearest to the midIndex
        spaceIndecies = getSpaceIndecies(wm.getVision().spacepattern[frame][0], threshold);

        if (spaceIndecies.x == -1) {
            System.out.println("for the first attention level, there is no available space observed");
            //targetGroup ==2 means no available space in front.
            targetGroup = 2;
            return -1;
        } else {
            if (spaceIndecies.x <= midIndex && spaceIndecies.y >= midIndex) {
                System.out.println("for the first attention level, there is no obstacle observed");
                //if there is available space in center front
                targetGroup = 0;
                return -1;
            } else if (spaceIndecies.y < midIndex) {
                left = true;
                targetAgent = wm.getVision().spacepattern_agt[frame][0][(int) spaceIndecies.y + 1];
                //targetGroup ==1 means there is space for overtaking the target group
                if (wm.getVision().spacepattern[frame][0][(int) (spaceIndecies.y + 1)] == 1) {
                    targetGroup = 1;
                } //targetGroup ==-1 means there is space for avoiding the target group
                else {
                    targetGroup = -1;
                }
                return (int) (spaceIndecies.y + 1);
            } else {
                left = false;
                targetAgent = wm.getVision().spacepattern_agt[frame][0][(int) spaceIndecies.x - 1];
                if (wm.getVision().spacepattern[frame][0][(int) (spaceIndecies.x - 1)] == 1) {
                    targetGroup = 1;
                } else {
                    targetGroup = -1;
                }
                return (int) (spaceIndecies.x - 1);
            }
        }
    }

    /**
     * Takes an array and finds the set of continuous zeros nearest to the
     * midpoint of the array which is at least of length th
     *
     * @param array
     * @param th (threshold)
     * @return
     */
    public Point2d getSpaceIndecies(final int[] array, int th) {
        int midPoint = array.length / 2;
        int curLeft = -1;
        int curRight = -1;
        int minDistance = Integer.MAX_VALUE;

        int leftStartI = midPoint;
        boolean foundLeftStart = false;
        int leftCount = 0;

        for (int i = 0; i < array.length; i++) {
            if (!foundLeftStart) {//no start point yet
                if (array[i] == 0) {
                    leftStartI = i;
                    leftCount = 0;
                    foundLeftStart = true;
                }
            } else {
                //next start point

                if (array[i] == 0 && i != array.length - 1) { //still zeros and not at end of line
                    leftCount++;
                } else {
                    if (i == array.length - 1 && array[i] == 0) {
                        leftCount++; //special case of zero at the end, need to add the extra zero count
                    }
                    if (leftCount >= th - 1) {
                        if (leftStartI <= midPoint && leftStartI + leftCount - 1 >= midPoint) {
                            return new Point2d(leftStartI, leftStartI + leftCount);//pass over middle
                        }
                        if (Math.abs(leftStartI - midPoint) < minDistance || Math.abs(leftStartI + leftCount - midPoint) < minDistance) {
                            curLeft = leftStartI;
                            curRight = leftStartI + leftCount;
                            minDistance = Math.min(Math.abs(curLeft - midPoint), Math.abs(curRight - midPoint));
                        }
                    }
                    foundLeftStart = false;
                }
            }


        }
        return new Point2d(curLeft, curRight);
    }

//    public static void main(String args[]) {
//        int[] test = {0, 0, 1, 0, 0, 0, 0, 0, 0};
//        //  Decision d = new Decision();
//        Point2d test2 = Decision.getSpaceIndecies(test, 2);
//        System.out.println("Index: " + test2);
//
//    }
   

    //
    /**
     * in the function, need to perform pattern-matching for phase 1 overtaking
     * or side-avoiding need to set instructedTime according to the matching condition
     *
     * @param t
     * @return
     */
    private boolean verifySpace_phase1(int t) {
        boolean success1 = false;
        int T = 0;

        //though in verifyStrategy(), first row has been checked for 001 or 00-1 pattern
        //still need to recheck here coz from current position of me to the boundary of attention_multi[0] takes t frames
        //during these t frames, targetIndex may change already, that is why target agent was retrieved before this function

        //pf == 20 currently
        if (t > wm.getVision().getPf()) {
            t = wm.getVision().getPf(); //decision can only be made based on information within prediction
        }

        //here, need to think whether the 00-1 and 001 patterns need to last for T or, need to appear at t
        /*
         * currently, I think should be lasting for T, because during this period, if any information changed
         * on the focus area (001 or 00-1), then new decision should be made, coz it is the closest area that can cost highest level of threat to myAgent
         */

        for (int i = 0; i < t; i++) {
            //along different slices of 2d array, try to find how many frames from now has the pattern 001 or 00-1 last
            for (int j = 0; j < wm.getVision().spacepattern[0][0].length; j++) {
                //1. since the column index in the array can be changed for the target agent, so we need to find this agent first using the agent's reference itself
                if (wm.getVision().spacepattern_agt[i][0][j] == targetAgent) {
                    //if previously, during selction of target in spatialGrouping(), the available space is on the left of the target
                    if (left) {
                        int checksum = 0;
                        if (j - threshold >= 0) {
                            for (int k = j - 1; k > j - threshold - 1; k--) {
                                checksum += Math.abs(wm.getVision().spacepattern[i][0][k]);
                            }
                            if (checksum == 0) {
                                T++;
                            }
                            //here, condition can be even stronger by specifying that me.vision.spacepattern[i][1][k]!=-1
                            //but may be needed (consider reference from back rows) or not (anyway, this 00 needs to be considered along time t, so if currently, there is -1 at the back row, it is most probably 00 wont exist in next time frame)
                        }
                    } else {
                        int checksum = 0;
                        if (j + threshold <= wm.getVision().spacepattern[0][0].length - 1) {
                            for (int l = j + 1; l < j + threshold + 1; l++) {
                                checksum += Math.abs(wm.getVision().spacepattern[i][0][l]);
                            }
                            if (checksum == 0) {
                                T++;
                            }
                        }
                    }
                    break;
                } else {
                    continue;
                }
            }
        }
        if (T == 0) {
            success1 = false;
        } //if existing space pattern lasts longer than the period of time when myAgent can get to the position with the max speed
        else if (T >= calculateTTA_MaxSpeed(myAgent, targetAgent)) {
            if (T >= t) {
                instructedTime = t;
            } else {
                instructedTime = T; //me needs to increase speed according to the shorter instructed time T to reach the 0 at first row in the 2d array
            }
            success1 = true;
            System.out.println("space is available for phase 1: catching up for " + instructedTime + " frames");
        } else {
            if (T >= 20) {
                success1 = true;
                instructedTime = T;
            } else {
                success1 = false; //even with max speed, me still cannot reach the available space within period of T from now on
                System.out.println("space for phase 1: catching up is only available for " + T + " frames, myAgent cannot make it");
            }
        }
        return success1;
    }

    /**
     *
     *
     * @param t
     * @return
     */
    private boolean verifySpace_phase1n2(int t) {
        boolean success1n2 = false;
//        boolean success1 = verifySpace_phase1(t);
//
//        if (success1) {
//            //  here, if success1 is successful, assume me will occupy that space, where will becomes my vision center in the array
//            //  targetIndex_phase2 is no much use, but need to get the return value from spatialGrouping(). it will be -1 if the center of the 1st array at frame[instructedTime] is 0
//            int targetIndex_phase2 = spatialGrouping(wm, instructedTime); //generate new "targetGroup", "left", "targetIndex" for phase 2 (phase 1 is believed to finish within instructedTime)
//            if (targetIndex_phase2 == -1 && targetGroup == 0) {
//                success1n2 = true;
//                System.out.println("space also available for phase 2: by-passing");
//            } else {
//                System.out.println("space not available for phase 2: by-passing");
//            }
//        }
        /*
         * Need to rewrite these part, basically, it is doing something similar to verifySpace_phase1
         * because if wannt verifySpace both for phase 1 and phase 2, the spatial patterns can be extended from only first row in each frame
         * to two rows in each frame, e.g, not only 001 or 00-1 needs to be existing along t frames, but also there should be "0" in the second row of array
         * behind either of the "0"s in the first row
         */
        int T = 0;

        if (t > wm.getVision().getPf()) {
            t = wm.getVision().getPf(); //decision can only be made based on information within prediction
        }

        //here, need to think whether the 00-1 and 001 patterns need to last for T or, need to appear at t
        /*
         * currently, I think should be lasting for T, because during this period, if any information changed
         * on the focus area (001 or 00-1), then new decision should be made, coz it is the closest area that can cost highest level of threat to myAgent
         */

        for (int i = 0; i < t; i++) {
            //along different slices of 2d array, try to find how many frames from now has the pattern 001 or 00-1 last
            for (int j = 0; j < wm.getVision().spacepattern[0][0].length; j++) {
                //1. since the column index in the array can be changed for the target agent, so we need to find this agent first using the agent's reference itself
                if (wm.getVision().spacepattern_agt[i][0][j] == targetAgent) {
                    if (left) {
                        int checksum_1stRow = 0;
                        int checkbit_2ndRow = 1;
                        if (j - threshold >= 0) {
                            //to check whether 001 or 00-1 pattern exist in the first row in frame i
                            for (int k = j - 1; k > j - threshold - 1; k--) {
                                checksum_1stRow += Math.abs(wm.getVision().spacepattern[i][0][k]); //for threshold number of columns, all need to be 0
                                //to check whether 0 exist at the second row in frame i behind the 0 in the first row
                                //maybe later, can add in third row for most experienced person
                                checkbit_2ndRow *= Math.abs(wm.getVision().spacepattern[i][1][k]); //within threshold number of columns, as long as 1 columns is 0 in the second row
                            }

                            if (checksum_1stRow == 0 && checkbit_2ndRow == 0) {
                                T++;
                            }
                            //here, condition can be even stronger by specifying that me.vision.spacepattern[i][1][k]!=-1
                            //but may be needed (consider reference from back rows) or not (anyway, this 00 needs to be considered along time t, so if currently, there is -1 at the back row, it is most probably 00 wont exist in next time frame)
                        }
                    } else {
                        int checksum_1stRow = 0;
                        int checkbit_2ndRow = 1;
                        if (j + threshold <= wm.getVision().spacepattern[0][0].length - 1) {
                            for (int l = j + 1; l < j + threshold + 1; l++) {
                                checksum_1stRow += Math.abs(wm.getVision().spacepattern[i][0][l]);
                                checkbit_2ndRow *= Math.abs(wm.getVision().spacepattern[i][1][l]); //within threshold number of columns, as long as 1 columns is 0 in the second row

                            }
                            if (checksum_1stRow == 0 && checkbit_2ndRow == 0) {
                                T++;
                            }
                        }
                    }
                    break;
                } else {
                    continue;
                }
            }
        }
        if (T == 0) {
            success1n2 = false;
        } //if existing space pattern lasts longer than the period of time when myAgent can get to the position with the max speed
        else if (T >= calculateTTA_MaxSpeed(myAgent, targetAgent)) {
            if (T >= t) {
                instructedTime = t;
            } else {
                instructedTime = T; //me needs to increase speed according to the shorter instructed time T to reach the 0 at first row in the 2d array
            }
            success1n2 = true;
            System.out.println("space is available for phase 1: catching up for " + instructedTime + " frames");
        } else {
            if (T >= 20) {
                success1n2 = true;
                instructedTime = T;
            } else {
                success1n2 = false; //even with max speed, me still cannot reach the available space within period of T from now on
                System.out.println("space for phase 1: catching up is only available for " + T + " frames, myAgent cannot make it");
            }
        }

        return success1n2;
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