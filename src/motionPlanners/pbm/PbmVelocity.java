package motionPlanners.pbm;

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

    public static void main(String[] args) {

        PbmVelocity vel = new PbmVelocity(new Vector2d(-1,1));
        System.out.println("Theta:" + vel.theta + " mag " + vel.magnitude);
    }

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
