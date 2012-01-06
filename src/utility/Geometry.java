/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Geometry
 *
 * @author michaellees
 * Created: Dec 20, 2010
 *
 * Copyright michaellees 
 *
 * Description:
 *
 */
public class Geometry {

    public static float EPSILON;

    //returns the cos(theta) of the two vectors
    public static double sameDirection(Vector2d v1, Vector2d v2) {
        return v1.dot(v2) / (v1.length() * v2.length()); //0 - pi
    }

    /**
     * returns angle from v1 to v2
     * @param v1
     * @param v2
     * @return angle between v1 and v2 in 0-pi
     */
    public static double angleBetween(Vector2d v1, Vector2d v2) {
        return Math.acos(sameDirection(v1, v2)); //0-pi
    }

    /**
     *
     * @param v1
     * @param v2
     * @return an angle from v1 to v2 in -pi to pi
     */
    public static double angleBetweenWSign(Vector2d v1, Vector2d v2) {
        return Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x); //-pi to pi
    }

    public static double det(Vector2d a, Vector2d b) {
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    public static boolean lineSegmentIntersectionTest(Point2d p1, Point2d p2, Point2d p3, Point2d p4) {
        double x1 = p1.getX();
        double x2 = p2.getX();
        double x3 = p3.getX();
        double x4 = p4.getX();
        double y1 = p1.getY();
        double y2 = p2.getY();
        double y3 = p3.getY();
        double y4 = p4.getY();

        double denom = ((y4 - y3) * (x2 - x1)) - ((x4 - x3) * (y2 - y1));
        if (denom == 0) {
            return false;
        } else {
            double num1 = ((x4 - x3) * (y1 - y3)) - ((y4 - y3) * (x1 - x3));
            double num2 = ((x2 - x1) * (y1 - y3)) - ((y2 - y1) * (x1 - x3));
            double ua = num1 / denom;
            double ub = num2 / denom;
            if (Double.compare(ua, -EPSILON) > 0
                    && Double.compare(ua - 1, EPSILON) < 0
                    && Double.compare(ub, -EPSILON) > 0
                    && Double.compare(ub - 1, EPSILON) < 0) {
                return true;
            } else {
                return false;
            }
        }


    }

    public static double calcDistanceToLineSegment(Point2d a, Point2d b, Point2d c) {

        Vector2d cMinusA = new Vector2d(c);
        cMinusA.sub(a);



        Vector2d bMinusA = new Vector2d(b);
        bMinusA.sub(a);


        double multipliedDistance = cMinusA.dot(bMinusA);

        double r = multipliedDistance / absSq(bMinusA);

        if (r < 0.0f) {
            return absSq(cMinusA);
        } else if (r > 1.0f) {
            Vector2d cMinusB = new Vector2d(c);
            cMinusB.sub(b);
            return absSq(cMinusB);
        } else {
            bMinusA.scale(r);
            bMinusA.add(a);
            Vector2d finalResult = new Vector2d(c);
            finalResult.sub(bMinusA);
            return absSq(finalResult);
        }
    }

    public static double absSq(Vector2d bMinusA) {
        return bMinusA.dot(bMinusA);
    }

    public static boolean leftOf(Point2d a, Point2d b, Point2d c) {
        Vector2d aMinusC = new Vector2d(a);
        aMinusC.sub(c);

        Vector2d bMinusA = new Vector2d(b);
        bMinusA.sub(a);


        if (Double.compare(Geometry.det(aMinusC, bMinusA), 0.0f) > 0) {

            return true;
        } else {

            return false;
        }
    }
}
