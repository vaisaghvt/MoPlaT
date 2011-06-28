/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.Obstacle;

import app.RVOGui;
import environment.geography.Obstacle;
import java.awt.Color;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import sim.portrayal.Portrayal;


/**
 *
 * @author Vaisagh
 */
public class RVOObstacle extends ObstaclePortrayal {

 //   ArrayList<Point2d> vertices;


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
