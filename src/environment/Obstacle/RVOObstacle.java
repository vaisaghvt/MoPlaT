
package environment.Obstacle;

import environment.geography.Obstacle;
import java.util.ArrayList;
import javax.vecmath.Point2d;


/**
 *
 * @author Vaisagh
 * 
 * This class extends ObstaclePortrayal and gives a description of a general 
 * RVOObstacle which is used to create obstacles from an XML file
 * 
 * 
 */
public class RVOObstacle extends ObstaclePortrayal {

    public RVOObstacle() {
        vertices = new ArrayList<Point2d>();
    }

    public RVOObstacle(Obstacle obstacle) {
        this();
        for (int i = 0; i < obstacle.getVertices().size(); i++) {
            vertices.add(new Point2d(obstacle.getVertices().get(i).getX(), obstacle.getVertices().get(i).getY()));
        }
    }

    /**
     * Returns the actual list reference. Be careful when editing
     * @return
     */
    public ArrayList<Point2d> getVertices() {
        return vertices;
    }

    public void addVertex(Point2d vertex) {
        vertices.add(vertex);

    }

  
}
