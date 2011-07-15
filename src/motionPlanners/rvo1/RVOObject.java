package motionPlanners.rvo1;


import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;


/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Nov 22, 2010
 *
 * 
 *
 * Description:
 *
 */
public class RVOObject {

    public Point2d position;
    public Vector2d velocity;
    public double radius;
    public boolean isAgent;

    public RVOObject(boolean isAgent, Point2d position,
            double radius, Vector2d velocity) {
        super();
        this.isAgent = isAgent;
        this.position = position;
        this.radius = radius;
        this.velocity = velocity;
    }
}


