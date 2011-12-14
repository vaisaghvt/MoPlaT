package motionPlanners.pbm;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import utility.Geometry;

public class PbmVelocity {

    public double magnitude;
    private double theta; //theta = 0~ 360 in degree with regard to normal coordinate system

    public double getTheta() {
        return theta;
    }
    public double speed_x;  //speed is in the unit of #of unit in applet per simulation frame
    public double speed_y;

    public PbmVelocity(Vector2d vel) {
        setSpeed2(vel.getX(), vel.getY());
    }

//  //for testing the coordinate system
//    public static void main(String[] args) {
//        PbmVelocity vel = new PbmVelocity(new Vector2d(0,-1));
//        System.out.println("Theta:" + vel.theta + " mag " + vel.magnitude);
//    }
    
//    private static void rotate2d(Vector2d v, double angle) {
//        double newx = v.x * Math.cos(angle) - v.y * Math.sin(angle);
//        double newy = v.x * Math.sin(angle) + v.y * Math.cos(angle);
//        v.x = newx;
//        v.y = newy;
//    }
//
////rotate vector2d v around point p clockwise by angle
//    private static void rotate2dAroundaPoint(Vector2d v, Point2d p, double angle){
//        double newx = (v.x-p.x) * Math.cos(angle) - (v.y-p.y) * Math.sin(angle) + p.x;
//        double newy = (v.y-p.y) * Math.cos(angle) + (v.x-p.x) * Math.sin(angle) + p.y;
//        v.x = newx;
//        v.y = newy;
//    }
//    /**
//     * for helper function testing
//     * @param args
//     */
//    public static void main(String args[]){
//        Vector2d v = new Vector2d(1,0);
//        Point2d p = new Point2d(1,1);
//        v.add(p);
//        double angle = Math.PI / 4;
////        PbmVelocity.rotate2d(v,angle);
//        PbmVelocity.rotate2dAroundaPoint(v, p, angle);
//        System.out.print("new vector is " + v.x + ", " + v.y );
//    }
//    
    
    public PbmVelocity(double mag, double theta) {
        setSpeed1(mag, theta);
    }

    private void setSpeed1(double mag, double theta) {
        magnitude = mag; //mag>0
        this.theta = theta;  //theta from 0 to 360
        speed_x = magnitude * Math.cos(theta * Math.PI / 180);
        speed_y = magnitude * Math.sin((-1) * theta * Math.PI / 180);  //because the coordinate system is inverse with x axis with javaApplet coordinate system
    }
    
    //theta between 0-360 in degree
    public void setSpeed2(double dx, double dy) {
        speed_x = dx;
        speed_y = dy;
        Vector2d xAxis = new Vector2d(1, 0);
        theta = Geometry.angleBetweenWSign(xAxis, new Vector2d(dx, dy* (-1)));
        if (theta < 0) {
            theta += Math.PI * 2;
        }
        theta = theta * 180 / Math.PI;
        magnitude = Math.sqrt(speed_x * speed_x + speed_y * speed_y);
    }
}
