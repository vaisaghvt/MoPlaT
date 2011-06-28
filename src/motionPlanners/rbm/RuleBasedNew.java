/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.rbm;

import agent.RVOAgent;
import java.util.ArrayList;
import java.util.Vector;
import javax.vecmath.Vector2d;
import motionPlanners.rvo.ObjectTemp;
import sim.util.Bag;
import motionPlanners.rvo.RVOBase;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Dec 1, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description: This doesn't use a safety factor. Threshold set for time to collision
 * and smaller to larger magnitudes considered.For each magnitude all angles are considered
 */
public class RuleBasedNew extends RVOBase {

    /**
     * Threshold for time to collision at which we stop sampling.
     */
    private double tcThreshold = 2.0; //(2 seconds)
    private double SAMPLE_RADIUS = 2;

    @Override
    public Vector2d calculateVelocity(RVOAgent me,
            Bag neighbors, Bag obses, Vector2d preferredVelocity, double timeStep) {

        this.timeStep = timeStep;

        Vector2d vCand = null;

        Vector2d selectedVelocity = new Vector2d(preferredVelocity);

        double minTc = Double.MAX_VALUE;

        Vector<ObjectTemp> objectsAround = getObjectsAround(me,neighbors, obses);

        if(collision){
            return getCollisionStopVelocity(me, objectsAround);
        }



        //Check current velocity first.
        double tc = this.getMinTimeToCollision(me, objectsAround, preferredVelocity, collision);

        //current velocity suitable so return
        if (tc > tcThreshold) {
            selectedVelocity.x = preferredVelocity.x;
            selectedVelocity.y = preferredVelocity.y;
            return selectedVelocity;
        }

        if (objectsAround.size() > 0) {
            ArrayList<Double> candidateAngles = generateCandidateAngles(NOOfCandidatAngle);
            ArrayList<Double> candidateMagnitudes = generateCandidateMagnitudes(NOOfCandidatMagnitude, SAMPLE_RADIUS);

            //In this case we check increasing magnitudes away from preferred velocity,
            // i.e., all angles for each magnitude in turn

            Vector2d initialVector;
            for (Double magnitude : candidateMagnitudes) {
                initialVector = new Vector2d(preferredVelocity);
                initialVector.scale(magnitude);
                initialVector.add(preferredVelocity, initialVector);
                for (Double angle : candidateAngles) {
                    vCand = rotate2d(initialVector, angle);

                    System.out.println("X: " + vCand.x + " Y: "+ vCand.y);
                    if (vCand.length() > maxSpeed) {
                        continue;
                    }
                    tc = this.getMinTimeToCollision(me, objectsAround, vCand, collision);

                    if (tc > tcThreshold) {
                        selectedVelocity.x = vCand.x;
                        selectedVelocity.y = vCand.y;
                        return selectedVelocity;
                    } else if (tc < minTc) {
                        minTc = tc;
                        selectedVelocity.x = vCand.x;
                        selectedVelocity.y = vCand.y;
                    }
                }
            }
        }
        return selectedVelocity;

    }

    /**
     * Rotates a given vector in 2d in counterclockwise direction about Z-axis
     *
     * @param v
     * @param angle
     * @return
     */
    private Vector2d rotate2d(Vector2d v, double angle) {
        return new Vector2d(v.x * Math.cos(angle) - v.y * Math.sin(angle), v.x * Math.sin(angle) + v.y * Math.cos(angle));
    }

    @Override
    //TODO: vvt: what is the point of adding a negative magnitude since the candidate angles already consider a 360 degree radius?
     protected ArrayList<Double> generateCandidateMagnitudes(int numOfCandidateMagnitudes, double maxRadius) {
        ArrayList<Double> ret = new ArrayList<Double>(numOfCandidateMagnitudes);

        for (int j = 0; j < numOfCandidateMagnitudes/2; j++) {
            double radius = -maxRadius * ((double) j + 1) / ((double) numOfCandidateMagnitudes);
            ret.add(radius);
            radius = maxRadius * ((double) j + 1) / ((double) numOfCandidateMagnitudes);
            ret.add(radius);
        }

        return ret;
    }


    @Override
    protected double getMinTimeToCollision(RVOAgent me, Vector<ObjectTemp> objectsAround, Vector2d vCand, boolean collision) {

        double minimumCollisionTime = Double.POSITIVE_INFINITY;

        for (ObjectTemp tempObject : objectsAround) {
            Vector2d vRelative = new Vector2d(vCand);

            if (tempObject.isAgent == true) {
                vRelative.scale(2);
                vRelative.sub(tempObject.velocity);
                vRelative.sub(me.getVelocity());
            } else {
              //  vRelative = vCand;
            }

            double time = timeTocollision(me.getCurrentPosition(), vRelative,
                    tempObject.position, me.getRadius()
                    + tempObject.radius + me.getRadius() * me.getPersonalSpaceFactor(), collision);

            double collisionTimeToTemp;


            if (collision == true) {
               collisionTimeToTemp=0;
            } else {
                collisionTimeToTemp = time;
            }
            if (collisionTimeToTemp < minimumCollisionTime) {
                minimumCollisionTime = collisionTimeToTemp;
            }
        }// for (Agent neighborAround : neighbors)

        return minimumCollisionTime;
    }

    private Vector2d getCollisionStopVelocity(RVOAgent me, Vector<ObjectTemp> collisionNeighbours) {
       //We assume that objectsAround will contain only those agents which we have collided with.

        Vector2d collStopV = new Vector2d(0,0);
        for(ObjectTemp n: collisionNeighbours){
           Vector2d posDiff = new Vector2d(me.getCurrentPosition());
           posDiff.sub(n.position);
           collStopV.add(posDiff);      
        }
        collStopV.scale(2); //Assume both agents avoid
        //collStopV.negate();
        return collStopV;
    }
}
