package environment;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import environment.Obstacle.RVO2Obstacle;
import environment.Obstacle.RVOObstacle;

import javax.vecmath.Point2d;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * RVOSpace
 *
 * @author michaellees
 * Created: Nov 24, 2010
 *
 * Copyright michaellees
 *
 * Description:
 *
 * This class defines the environment. It has two layers : an agent layer with
 * all the agents on it and an obstacleSpace layer with all the obstacles on it. 
 */
public class RVOSpace {

    protected double gridDimension;
    public static double xRealSize;
    public static double yRealSize;
    /**
     * This is the space were all the agents are stored
     */
    protected Continuous2D agentSpace;
    /**
     * This space contains the obstacle information
     */
    protected Continuous2D obstacleSpace;
    /**
     * This is the model for local reference
     */
    protected RVOModel rvoModel;

    public RVOSpace(int xSize, int ySize, double gridSize, RVOModel rm) {

        gridDimension = gridSize;

        xRealSize = xSize * gridDimension;
        yRealSize = ySize * gridDimension;


        agentSpace = new Continuous2D(gridDimension, xRealSize, yRealSize);
        obstacleSpace = new Continuous2D(gridDimension, xRealSize, yRealSize);
        rvoModel = rm;



    }

    
    public Continuous2D getCurrentAgentSpace() {
        return agentSpace;
    }

    public RVOModel getRvoModel() {
        return rvoModel;
    }

    /**
     * Analogous to obstacle space
     * @return obstacleSpace
     */
    public Continuous2D getGeographySpace() {
        return obstacleSpace;
    }

    public void addNewObstacle(RVOObstacle obstacle) {

        if (PropertySet.MODEL == PropertySet.MODEL.RVO2) {
            //If RVO2 is the model then the RVO2 obstacle requires obstacles to
            //broken down

            RVO2Obstacle rvo2Obstacle, first;

            rvo2Obstacle = new RVO2Obstacle(); //initialise to empty obstacles
            first = rvo2Obstacle;
            rvo2Obstacle.setNext(new RVO2Obstacle()); // This is to enable the proper setting of links between obstacles

            for (int i = 0; i < obstacle.getVertices().size(); i++) {
                // Each vertex/ edge has a corresponding obstacle

                if (i == obstacle.getVertices().size() - 1) {
                    rvo2Obstacle.getNext().setPrev(rvo2Obstacle);
                    rvo2Obstacle = rvo2Obstacle.getNext();

                    //For the last obstacle point, the circular references, need to be set
                    first.setPrev(rvo2Obstacle); // Set First point to point to last element
                    rvo2Obstacle.setNext(first); // set last point to point to first element

                } else if (i != 0) {
                    rvo2Obstacle.getNext().setPrev(rvo2Obstacle);
                    rvo2Obstacle = rvo2Obstacle.getNext();

                    rvo2Obstacle.setNext(new RVO2Obstacle());
                }

                //Set the current rvo2obstacle to the first point of the RVOObstacle
                rvo2Obstacle.setPoint(obstacle.getVertices().get(i));

                //Add the point to vertex list
                rvo2Obstacle.addVertex(rvo2Obstacle.getPoint());

                if (i == obstacle.getVertices().size() - 1) {
                    rvo2Obstacle.addVertex(first.getPoint());
                }
                if (i > 0) {
                    rvo2Obstacle.getPrev().addVertex(rvo2Obstacle.getPoint());
                }


                obstacleSpace.setObjectLocation(rvo2Obstacle, 
                        new Double2D(
                            obstacle.getVertices().get(0).x, 
                            obstacle.getVertices().get(0).y));
            }

        } else {
            obstacleSpace.setObjectLocation(
                    obstacle, 
                    new Double2D(
                        obstacle.getVertices().get(0).x, 
                        obstacle.getVertices().get(0).y));
        }


    }

    public void updatePositionOnMap(RVOAgent agent, double x, double y) {
        //TODO: vvt: check whether the agent was created on an existing obstacle
        agent.setCurrentPosition(x, y);
        agentSpace.setObjectLocation(agent, new Double2D(x, y));
    }

    public Bag senseNeighbours(RVOAgent me) {
        return findNeighbours(me.getCurrentPosition(), RVOAgent.SENSOR_RANGE * me.getRadius());
    }

    public Bag findNeighbours(Double2D currentPosition, double radius) {
        Bag neighbours = agentSpace.getObjectsExactlyWithinDistance(currentPosition, radius);
        return neighbours;
    }

    public Bag senseObstacles(RVOAgent me) {

        return findObstacles(me.getCurrentPosition(), RVOAgent.SENSOR_RANGE * me.getRadius());
    }

    private Bag findObstacles(Point2d currentPosition, double radius) {
        Bag obstacles = obstacleSpace.getObjectsExactlyWithinDistance(new Double2D(currentPosition.x, currentPosition.y), radius);
        return obstacles;
    }

    public Bag findNeighbours(Point2d currentPosition, double radius) {
        Bag neighbours = agentSpace.getObjectsExactlyWithinDistance(new Double2D(currentPosition.x, currentPosition.y), radius);
        return neighbours;
    }
}
