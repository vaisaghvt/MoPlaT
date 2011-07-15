package motionPlanners.rvo1;

import agent.RVOAgent;
import environment.Obstacle.RVO1Obstacle;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d; //from Java3d - make sure installed on system
import javax.vecmath.Vector2d;
import motionPlanners.VelocityCalculator;
import sim.util.Bag;

/**
 * RVOVelocityCalculator
 *
 * @author michaellees
 * Created: Nov 16, 2010
 *
 * Copyright michaellees
 *
 *
 * Description: A class to perform the RVO calculation based on monte-carlo
 * style sampling. This is based on the implementation done for COSMOS by
 * Muzhou Xiong. He based this on  the van der berg paper and the available van der
 * berg code.
 *
 */
public abstract class RVOBase implements VelocityCalculator{



    //RVO specific parameters
    protected int NOOfCandidatAngle = 36;
    protected int NOOfCandidatMagnitude = 10;

    protected double Saftey_Factor = 7.5;

    /**
     * A boolean used to indicate the occurence of a collision
     */
    protected boolean collision = false;
     
        
    
    /**
     * get the intersection between a line and a circle
     *
     * @param p
     *            the point in the line
     * @param _x
     *            direction x of the line
     * @param _y
     *            direction y of the line
     * @param center
     *            central point of the circle
     * @param r
     *            radius of the circle
     * @return
     */
    protected static List<Vector2d> lineCircleIntersection(Point2d p, double _x,
            double _y, Point2d center, double r) {
        List<Vector2d> vec = new ArrayList<Vector2d>();

        if (Math.abs(_x) < 0.001) {
            double E = center.y * center.y - r * r + (p.x - center.x)
                    * (p.x - center.x);
            double delta = 4 * center.y * center.y - 4 * E;
            if (delta < 0) {
                // no intersection
            } else if (delta == 0) {
                double y = 2 * center.y / 2;
                double x = p.x;
                vec.add(new Vector2d(x, y));

            } else {
                double y1 = (2 * center.y + Math.sqrt(delta)) / 2;
                double y2 = (2 * center.y - Math.sqrt(delta)) / 2;
                Vector2d p1 = new Vector2d(p.x, y1);
                Vector2d p2 = new Vector2d(p.x, y2);
                vec.add(p1);
                vec.add(p2);
            }
        } else {
            double k = _y / _x;
            double b = p.y - k * p.x;
            double A = b - center.y;
            double B = 1 + k * k;
            double C = 2 * (k * A - center.x);
            double D = center.x * center.x + A * A - r * r;
            double delta = C * C - 4 * B * D;
            if (delta < 0) {
                // no intersection
            } else if (delta == 0) {
                double x = (-1) * C / (2 * B);
                double y = k * x + b;
                vec.add(new Vector2d(x, y));
            } else {
                double x1 = ((-1) * C + Math.sqrt(delta)) / (2 * B);
                double x2 = ((-1) * C - Math.sqrt(delta)) / (2 * B);
                Vector2d p1 = new Vector2d(x1, k * x1 + b);
                Vector2d p2 = new Vector2d(x2, k * x2 + b);
                vec.add(p1);
                vec.add(p2);
            }
        }
        return vec;
    }

  

    protected boolean detectCollision(RVOAgent me, RVOAgent tempAgt) {
        double distance = tempAgt.getCurrentPosition().distance(me.getCurrentPosition());
        return (distance < (me.getRadius() + tempAgt.getRadius()));
    }

    protected boolean detectCollision(RVOAgent me, RVO1Obstacle tempObs) {
        double distance = tempObs.getPosition().distance(me.getCurrentPosition());
        return (distance < (me.getRadius() + tempObs.getSize() * 0.707));
    }

    protected double getMinTimeToCollision(RVOAgent me, List<RVOObject> objectsAround,
            Vector2d vCandidate, boolean collision,double timeStep) {

        double minimumCollisionTime = Double.POSITIVE_INFINITY;

        double maxSpeed = me.getMaxSpeed();
        
        
        for (RVOObject tempObject : objectsAround) {
            Vector2d vRelative = new Vector2d(vCandidate);// This is the relative velocity used for time to collision

            if (tempObject.isAgent == true) {
                vRelative.scale(2);
                vRelative.sub(tempObject.velocity);
                vRelative.sub(me.getVelocity());
            } else {
              //  vRelative = vCandidate;
            }

            double time = timeTocollision(me.getCurrentPosition(), vRelative,
                    tempObject.position, me.getRadius()
                    + tempObject.radius + me.getRadius() * me.getPersonalSpaceFactor(), collision);

            double collisionTimeToTemp;


            if (collision == true) {
                //TODO: vvt: what is this supposed to be doing?
                collisionTimeToTemp = -Math.ceil(time / timeStep);
                collisionTimeToTemp -= vCandidate.dot(vCandidate) / (maxSpeed * maxSpeed);
             //   collisionTimeToTemp = 0;

            } else {
                collisionTimeToTemp = time;


            }
            if (collisionTimeToTemp < minimumCollisionTime) {
                minimumCollisionTime = collisionTimeToTemp;
            }
        }// for (Agent neighborAround : neighbors)

        return minimumCollisionTime;
    }

       /**
     * get the time to collison for body moving from p to p2 with a relative velocity v
     *
     * @param p
     *            my location
     * @param v
     *            my velocity relative to object
     * @param p2
     *            center of object i'm trying to avoid
     * @param radius
     *            combined radius of my radius plus obect radius
     * @param collision
     *            flag indicating whether collision has occured
     * @return time to collision
     */
    protected double timeTocollision(Point2d p, Vector2d v, Point2d p2,
            double radius, boolean collision) {

        Vector2d ba = new Vector2d(p2);
        ba.sub(p);

        double sq_diam = radius * radius;

        double time;

        //determinant not supplied by Vector2d so...
        //double vDetBa = v.det(ba);
        double vDetBa = v.x * ba.y - v.y * ba.x;


        double discr = sq_diam * v.dot(v) - vDetBa * vDetBa;

        if (discr > 0) {
            if (collision) {
                time = (v.dot(ba) + Math.sqrt(discr)) / v.dot(v);

                if (time < 0) {
                    time = Double.NEGATIVE_INFINITY;

                }
            } else {
                time = (v.dot(ba) - Math.sqrt(discr)) / v.dot(v);
                if (time < 0) {
                    time = Double.POSITIVE_INFINITY;//MAX_VALUE;
                }
            }
        } else {
            if (collision) {
                time = Double.NEGATIVE_INFINITY;

            } else {
                time = Double.POSITIVE_INFINITY;//MAX_VALUE;

            }
        }
        return time;

    }

    protected ArrayList<Double> generateCandidateMagnitudes(int numOfCandidateMagnitudes, double maxRadius) {
        ArrayList<Double> ret = new ArrayList<Double>(numOfCandidateMagnitudes);

        for (int j = 0; j < numOfCandidateMagnitudes; j++) {
            double radius = maxRadius * ((double) j + 1) / ((double) numOfCandidateMagnitudes);
            ret.add(radius);
        }

        return ret;
    }

    protected ArrayList<Double> generateCandidateAngles(int numOfCandidateAngles) {
        ArrayList<Double> ret = new ArrayList<Double>(numOfCandidateAngles);

        for (int n = 0; n < numOfCandidateAngles; n++) {
            double angle = 2 * Math.PI * n / numOfCandidateAngles;
            ret.add(angle);
        }

        return ret;
    }

    protected List<RVOObject> getObjectsAround(RVOAgent me,
            Bag neighbors, Bag obses) {

        List<RVOObject> objectsAround = new ArrayList<RVOObject>();

        collision = false;
        //new Vector<ObjectTemp>();

        for (Object obj : neighbors) {

            RVOAgent tempAgt = (RVOAgent) obj;
            if (tempAgt == me) {//don't consider self
                continue;
            }

            //check to see if we collide with this agent
            boolean collisionWithThis = detectCollision(me, tempAgt);

            //if we have a collision only put colliding objects in the list
            if (collisionWithThis) {
                if (!collision) {
                    //our first collision we clear previous agents and from now on
                    //only add agents with collision. Essentially if there is a collision
                    //the objectsAround should contain all agents which it collided with
                    //but no more.
                    objectsAround.clear();
                    collision = true;
                }
                objectsAround.add(new RVOObject(true,
                        tempAgt.getCurrentPosition(), tempAgt.getRadius(),
                        tempAgt.getVelocity()));

            } else if (!collision) {
                objectsAround.add(new RVOObject(true,
                        tempAgt.getCurrentPosition(), tempAgt.getRadius(),
                        tempAgt.getVelocity()));
            }
        }

        if (obses != null) {
            for (Object obj : obses) {
                RVO1Obstacle tempObs = (RVO1Obstacle) obj;
                boolean collisionWithThis = detectCollision(me, tempObs);
                if (collisionWithThis) {
                    if (!collision) {
                        //our first collision we clear previous agents and from now on
                        //only add agents with collision. Essentially if there is a collision
                        //the objectsAround should contain all agents which it collided with
                        //but no more.
                        objectsAround.clear();
                        collision = true;
                    }
                    objectsAround.add(new RVOObject(false, tempObs.getPosition(), tempObs.getSize() * 0.707, null));

                } else if (!collision) {

                    objectsAround.add(new RVOObject(false, tempObs.getPosition(), tempObs.getSize() * 0.707, null));
                }

            }
        }
        return objectsAround;
    }
}

