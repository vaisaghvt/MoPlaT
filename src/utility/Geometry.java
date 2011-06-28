/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

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
    public static double angleBetweenWSign(Vector2d v1, Vector2d v2){
        return Math.atan2(v2.y,v2.x) - Math.atan2(v1.y,v1.x); //-pi to pi
    }
}
