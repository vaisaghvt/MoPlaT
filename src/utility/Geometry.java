/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import agent.RVOAgent;
import app.PropertySet;
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
     * @return an angle from v1 to v2 in -pi to pi, clockwise is positive
     */
    public static double angleBetweenWSign(Vector2d v1, Vector2d v2) {
        return Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x); //-pi to pi
    }
    
    /*
     * testing
     */
    public static void main(String args[]){
        Vector2d v1 = new Vector2d(1,0);
        Vector2d v2 = new Vector2d(0,1);
        double angleWSign = angleBetweenWSign(v1, v2);
        double sinAngle = Math.sin(angleWSign);
        System.out.println("Angle from V1 to V2 in degree is: "+angleWSign*180/Math.PI+" sine of Angle is "+sinAngle);
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

    public static double calcDistanceToLineSegment(Point2d p1, Point2d p2, Point2d p3) {

        final double xDelta = p2.getX() - p1.getX();
        final double yDelta = p2.getY() - p1.getY();

        if ((xDelta == 0) && (yDelta == 0)) {
            throw new IllegalArgumentException("p1 and p2 cannot be the same point");
        }

        final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        final Point2d closestPoint;
        if (u < 0) {
            closestPoint = p1;
        } else if (u > 1) {
            closestPoint = p2;
        } else {
            closestPoint = new Point2d(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
        }

        return closestPoint.distance(p3);
    }
    
    public static double calcTTC(Point2d p1, Vector2d v1, double r1, Point2d p2, Vector2d v2, double r2){
        double ttc = 9999;
      
        double r = r1+r2;
        Vector2d v = new Vector2d(v1);
        v.sub(v2);

        double slope = v.y/v.x;
        // line for relative velocity is y = slope(x-p1.x)+p1.y
        // circle for relative position is (x-p2.x)*(x-p2.x) + (y-p2.y)*(y-p2.y)= r*r
        //substitue y into the circle formula get: 
        double a = slope*slope + 1;
        
        double m = p1.y - p2.y - slope * p1.x;
        
        double b = slope * m - p2.x;
        b*=2;
        
        double c = m*m + p2.x*p2.x - r*r;
        
        double alpha = b*b - 4*a*c;
        
        if(alpha>=0){
            double intersectX1 = b*(-1)-Math.sqrt(alpha);
            intersectX1 /= 2*a;
            double intersectY1 = (intersectX1-p1.x)*slope + p1.y; 
            
            double intersectX2 = b*(-1)+Math.sqrt(alpha);
            intersectX2 /= 2*a;
            double intersectY2 = (intersectX2-p1.x)*slope + p1.y; 
            
            Point2d intersection1 = new Point2d(intersectX1,intersectY1);
            Point2d intersection2 = new Point2d(intersectX2,intersectY2);
            
            double d1=intersection1.distance(p1);
            double d2=intersection2.distance(p1);
           
            ttc =(Math.min(d1, d2) -RVOAgent.RADIUS)/v.length();
        }
        return ttc; //ttc in terms of time
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
    
        /*
     * rotate vector v clockwise by degree d and return the new vector
     */
    public static Vector2d helpRotate(Vector2d v, double d) {
        double radian = 0.0;
        radian = d * Math.PI / 180;
        double newx = (v.x * Math.cos(radian)) - (v.y * Math.sin(radian));
        double newy = (v.x * Math.sin(radian)) + (v.y * Math.cos(radian)); 
        return new Vector2d(newx,newy);
    }
}
