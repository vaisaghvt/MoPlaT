package motionPlanners.rvo;


import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Nov 22, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class ObjectTemp {

    public Point2d position;
    public Vector2d velocity;
    public double radius;
    public boolean isAgent;

    public ObjectTemp(boolean isAgent, Point2d position,
            double radius, Vector2d velocity) {
        super();
        this.isAgent = isAgent;
        this.position = position;
        this.radius = radius;
        this.velocity = velocity;
    }
}


