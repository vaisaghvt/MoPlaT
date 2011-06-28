package motionPlanners.rvo;

import motionPlanners.rvo.ObjectTemp;
import agent.RVOAgent;
import java.util.ArrayList;
import java.util.Vector;
import javax.vecmath.Vector2d;
import sim.util.Bag;

/**
 * RVO_1_WithAccel
 *
 * @author michaellees
 * Created: Nov 30, 2010
 *
 * Copyright michaellees
 *
 * Description:
 *
 * This is an alternative implementation of RVO sampling.
 *
 * In this case the candidate velocities are sampled around the current velocity
 * in a range equal to the maximum accleration of the agent. Care has to be taken
 * when specifying safetyFactor, timeStep and max acceleration. The relationship
 * between all these isn't clear to me...
 *
 */
public class RVO_1_WithAccel extends RVOBase{

     /**
     * Calculate new velocity using acceleration. Here sampling is done around
     * current velocity according to acceleration constraints. vCand is then
     * addition to current velocity
     *
     * @param me
     * @param neighbors
     * @param obses
     * @param preferredVelocity
     * @param timeStep
     * @param NOOfCandidatAngle
     * @param NOOfCandidatMagnitude
     * @param Saftey_Factor
     * @param maxAccel
     * @return
     */

    /**
     * The radius around the preferred velocity at which to sample.
     */
    private static double SAMPLE_RADIUS = 1.3;
    @Override
    public Vector2d calculateVelocity(RVOAgent me,
            Bag neighbors, Bag obses, Vector2d preferredVelocity, double timeStep) {



        //ger current velocity
        Vector2d currentVelocity = me.getVelocity();

        this.timeStep = timeStep;


        Vector2d vCand = null;

        /**
         *
         */
        Vector2d selectedVelocity = new Vector2d(currentVelocity);

        double minPenalty = Double.MAX_VALUE;

        Vector<ObjectTemp> objectsAround = getObjectsAround(me,
                neighbors, obses);

        //Check current velocity first.
        double tc = this.getMinTimeToCollision(me, objectsAround, currentVelocity, collision);
        double penalty = Saftey_Factor / tc; //note that preferred velocity has zero deviation.

        double prefPen = penalty;

        if (penalty < minPenalty) {
            minPenalty = penalty;
            selectedVelocity.x = currentVelocity.x;
            selectedVelocity.y = currentVelocity.y;
        }

        if (objectsAround.size() > 0) {

            ArrayList<Double> candidateAngles = generateCandidateAngles(NOOfCandidatAngle);
            ArrayList<Double> candidateMagnitudes = generateCandidateMagnitudes(NOOfCandidatMagnitude, SAMPLE_RADIUS);

            for (Double angle : candidateAngles) {
                for (Double magnitude : candidateMagnitudes) {

                    vCand = new Vector2d(me.getVelocity().getX() + magnitude
                            * Math.cos(angle), me.getVelocity().getY() + magnitude
                            * Math.sin(angle));
                    if (vCand.length() > maxSpeed) {
                        continue;
                    }

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
                        selectedVelocity.x = vCand.x;
                        selectedVelocity.y = vCand.y;
                    }
                }
            }
        }
        return selectedVelocity;

    }


}
