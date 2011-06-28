/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package motionPlanners.rvo;

import motionPlanners.rvo.ObjectTemp;
import agent.RVOAgent;
import environment.Obstacle.RVO1Obstacle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.vecmath.Point2d; //from Java3d - make sure installed on system
import javax.vecmath.Vector2d;
import motionPlanners.VelocityCalculator;
import sim.util.Bag;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Dec 1, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class RVO_1_Standard extends RVOBase{
  /**
     * Calculate new velocity without kinematic constraints. Here the velocity
     * sampling is done around the MaXSpeed circle and vCand is not relative to current velocity.
     * This is similar to the original RVO 1.1, however regular sampling around
     * the maxSpeed circle is used rather than random sampling with in the
     * maxSpeed.
     *
     * @param me
     * @param neighbors
     * @param obses
     * @param preferredVelocity
     * @param timeStep
     * @param NOOfCandidatAngle
     * @param NOOfCandidatMagnitude
     * @param Saftey_Factor
     * @return
     */
    public Vector2d calculateVelocity(RVOAgent me,
            Bag neighbors, Bag obses, Vector2d preferredVelocity, double timeStep) {


        this.timeStep = timeStep;

        /**
         * TODO: mhl, why the hell is this here??
         */
        Vector2d vCand = null;

        /**
         *
         */
        Vector2d selectedVelocity = new Vector2d(preferredVelocity);
        //TODO: WHY IS THIS COMMENTED OUT!?
        // Point3D preferredVelocity = goal.substract(me.position).normalize()
        // .multiply(preferredSpeed);

        double minPenalty = Double.MAX_VALUE;
        double selectedTc = 0;
        double selectedDv = 0;
     

        Vector<ObjectTemp> objectsAround = getObjectsAround(me,
                neighbors, obses);

        //Check preferred velocity first.
        double tc = this.getMinTimeToCollision(me, objectsAround, preferredVelocity, collision);
        double penalty = Saftey_Factor / tc; //note that preferred velocity has zero deviation.

        double prefPen = penalty;

        if (penalty < minPenalty) {
            minPenalty = penalty;
            selectedVelocity.x = preferredVelocity.x;
            selectedVelocity.y = preferredVelocity.y;
        }


        if (objectsAround.size() > 0 && tc < Double.POSITIVE_INFINITY) {

//            //check zero velocity as special case
//            Vector2d zeroVelocity = new Vector2d(0, 0);
//            tc = this.getMinTimeToCollision(me, objectsAround, zeroVelocity, collision);
//            penalty = Saftey_Factor / tc + preferredVelocity.length(); //note that preferred velocity has zero deviation.
//
//            if (penalty < minPenalty) {
//                minPenalty = penalty;
//                selectedVelocity.x = 0;
//                selectedVelocity.y = 0;
//            }

            //get a set of candidate angles and magnitudes
            ArrayList<Double> candidateAngles = generateCandidateAngles(NOOfCandidatAngle);
            ArrayList<Double> candidateMagnitudes = generateCandidateMagnitudes(NOOfCandidatMagnitude, maxSpeed);

            for (Double angle : candidateAngles) {
                for (Double magnitude : candidateMagnitudes) {

                    vCand = new Vector2d(magnitude
                            * Math.cos(angle), magnitude
                            * Math.sin(angle));


                    double distanceOfVels = 0;

                    if (collision == true) {
                        distanceOfVels = 0;
                    } else {
                        Vector2d distVec = new Vector2d(vCand);
                        distVec.sub(preferredVelocity);
                        distanceOfVels = distVec.length();
                    }

                    tc = this.getMinTimeToCollision(me, objectsAround, vCand, collision);
                    penalty = Saftey_Factor / tc + distanceOfVels;

                    if (penalty < minPenalty) {
                        minPenalty = penalty;
                        selectedTc = tc;
                        selectedDv = distanceOfVels;
                        selectedVelocity.x = vCand.x;
                        selectedVelocity.y = vCand.y;
                    }
                }
            }

            System.out.println("Selected: " + selectedVelocity.x + " " + selectedVelocity.y + " tc " + selectedTc + " dv " + selectedDv + " p " + minPenalty);
        }
        return selectedVelocity;

    }
}
