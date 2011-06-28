package environment.Obstacle;



import environment.geography.Obstacle;
import javax.vecmath.Point2d;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Nov 16, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:Obstacles used in RVO1
 *
 */
public class RVO1Obstacle extends RVOObstacle {

    private double size;
    private Point2d position;

    public RVO1Obstacle(Obstacle obstacle){
        super(obstacle);
        //.... Depending on what the requirements are... generate RVO1Obstacles from RVOObstacles
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Point2d getPosition() {
        return position;
    }

    public double getSize() {
        return size;
    }

    //TODO: Check what the requirements are for this class in terms of COSMOS
}
