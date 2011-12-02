package environment;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import environment.Obstacle.RVO2Obstacle;
import environment.Obstacle.RVOObstacle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
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
            ArrayList<RVO2Obstacle> obstacles = new ArrayList<RVO2Obstacle>();
            
            RVO2Obstacle rvo2Obstacle;
            
            
            
            

            for (int i = 0; i < obstacle.getVertices().size(); i++) {
                rvo2Obstacle = new RVO2Obstacle();
                rvo2Obstacle.setPoint(obstacle.getVertices().get(i));
                obstacles.add(rvo2Obstacle);
            }
            for(int i = 0; i < obstacles.size(); i++){
                if(i==0){
                    obstacles.get(i).setPrev(obstacles.get(obstacles.size()-1));
                }else{
                    obstacles.get(i).setPrev(obstacles.get(i-1));
                }
                
                if(i==obstacles.size()-1){
                    obstacles.get(i).setNext(obstacles.get(0));
                }else{
                    obstacles.get(i).setNext(obstacles.get(i+1));
                }
                
                obstacleSpace.setObjectLocation(obstacles.get(i), new Double2D(
                    obstacles.get(i).getPoint().getX(),
                    obstacles.get(i).getPoint().getY()));
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

        Bag initialObstacleList = findObstacles(me.getCurrentPosition(), RVOAgent.SENSOR_RANGE * me.getRadius());

       



        return initialObstacleList;


    }

    private Bag findObstacles(Point2d currentPosition, double radius) {
        Bag obstacles = obstacleSpace.getObjectsExactlyWithinDistance(new Double2D(currentPosition.x, currentPosition.y), radius);
        return obstacles;
    }

    public Bag findNeighbours(Point2d currentPosition, double radius) {
        Bag neighbours = agentSpace.getObjectsExactlyWithinDistance(new Double2D(currentPosition.x, currentPosition.y), radius);
        return neighbours;
    }

    public static double calcDistanceToLineSegment(Point2d a, Point2d b, Point2d c) {
        
        Vector2d cMinusA = new Vector2d(c);
        cMinusA.sub(a);
        
        
        
        Vector2d bMinusA = new Vector2d(b);
        bMinusA.sub(a);
        
        
        double multipliedDistance = cMinusA.dot(bMinusA);
        
        double r = multipliedDistance  / absSq(bMinusA);

        if (r < 0.0f) {
            return absSq(cMinusA);
        } else if (r > 1.0f) {
            Vector2d cMinusB = new Vector2d(c);
            cMinusB.sub(b);
            return absSq(cMinusB);
        } else {
            bMinusA.scale(r);
            bMinusA.add(a);
            Vector2d finalResult = new Vector2d(c);
            finalResult.sub(bMinusA);
            return absSq(finalResult);
        }
    }
    
    private static double absSq(Vector2d bMinusA) {
        return bMinusA.dot(bMinusA);
    }

}
