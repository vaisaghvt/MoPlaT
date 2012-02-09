package environment;

import utility.PrecisePoint;
import agent.RVOAgent;
import app.PropertySet;
import app.PropertySet.Model;
import app.RVOModel;
import environment.Obstacle.RVO2Obstacle;
import environment.Obstacle.RVOObstacle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import utility.Geometry;

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
    protected List<RVO2Obstacle> obstacleList = new ArrayList<RVO2Obstacle>();
    protected RVOModel rvoModel;
    private List<Point2d> roadMap;

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

        if (PropertySet.MODEL == Model.RVO2 || PropertySet.MODEL == Model.SocialForce) {
            //If RVO2 is the model then the RVO2 obstacle requires obstacles to
            //broken down
            ArrayList<RVO2Obstacle> obstacles = new ArrayList<RVO2Obstacle>();

            RVO2Obstacle rvo2Obstacle;

            obstacle.setVertices(makeRightOrder(obstacle.getVertices()));
            for (int i = 0; i < obstacle.getVertices().size(); i++) {
                rvo2Obstacle = new RVO2Obstacle();
                rvo2Obstacle.setPoint(obstacle.getVertices().get(i));
                obstacles.add(rvo2Obstacle);
            }
            for (int i = 0; i < obstacles.size(); i++) {
                if (i == 0) {
                    obstacles.get(i).setPrev(obstacles.get(obstacles.size() - 1));
                } else {
                    obstacles.get(i).setPrev(obstacles.get(i - 1));
                }

                if (i == obstacles.size() - 1) {
                    obstacles.get(i).setNext(obstacles.get(0));
                } else {
                    obstacles.get(i).setNext(obstacles.get(i + 1));
                }
                // System.out.println(obstacles.get(i).getPoint());
                if (obstacle.getVertices().size() == 2) {
                    obstacles.get(i).setConvex(true);
                } else {
                    obstacles.get(i).setConvex(
                            Geometry.leftOf(obstacles.get(i).getPrev().getPoint(),
                            obstacles.get(i).getPoint(),
                            obstacles.get(i).getNext().getPoint()));

                }
                this.obstacleList.add(obstacles.get(i));
                obstacleSpace.setObjectLocation(obstacles.get(i), new Double2D(
                        obstacles.get(i).getPoint().getX(),
                        obstacles.get(i).getPoint().getY()));
            }

        } else {
            for (Point2d vertex : obstacle.getVertices()) {
                obstacleSpace.setObjectLocation(
                        obstacle,
                        new Double2D(
                        vertex.getX(),
                        vertex.getY()));
            }
        }


    }

    public void updatePositionOnMap(RVOAgent agent, double x, double y) {
        //TODO: vvt: check whether the agent was created on an existing obstacle
        agent.setCurrentPosition(x, y);
        agentSpace.setObjectLocation(agent, new Double2D(x, y));
    }

    public Bag senseNeighbours(RVOAgent me) {
        double sensorRange = RVOAgent.SENSOR_RANGE;
        Bag neighbours = findNeighbours(me.getCurrentPosition(), sensorRange * me.getRadius());
//        Vector2d unitAgentDirection =  new Vector2d(me.getPrefVelocity());
//        unitAgentDirection.normalize();
//        for(int i=0;i<neighbours.size();i++){
//            RVOAgent agentNeighbour = (RVOAgent) neighbours.get(i);
//            Vector2d neighbourDirection = new Vector2d(agentNeighbour.getCurrentPosition());
//            neighbourDirection.sub(me.getCurrentPosition());
//            neighbourDirection.normalize();
//            double angleRadians = neighbourDirection.angle(unitAgentDirection);   
//            if((Double.compare(angleRadians, (Math.PI/2.0))>0
//                    && Double.compare(angleRadians, (3.0*Math.PI/2.0))<0)){
//                neighbours.remove(i);
//            }
//        }
//        while(neighbours.size()>10){
//            sensorRange *= 0.9;
//            neighbours= findNeighbours(me.getCurrentPosition(), sensorRange * me.getRadius());
//        }
        return neighbours;

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

   
    public boolean visibleFrom(Point2d goal, Point2d position) {
        Point2d p1 = new Point2d(position.getX(), position.getY());
        Point2d p2 = new Point2d(goal.getX(), goal.getY());
        for (RVO2Obstacle obstacle : this.obstacleList) {
            if (Geometry.lineSegmentIntersectionTest(p1, p2,
                    obstacle.getPoint(), obstacle.getNext().getPoint())) {
                return false;
            }
        }
        return true;
    }
    
    private ArrayList<Point2d> makeRightOrder(ArrayList<Point2d> points){
        
        int sum =0;
        for(int i=0;i< points.size();i++){
            Point2d currentPoint = points.get(i);
            Point2d nextPoint = points.get((i+1)%points.size());
            double x1 = currentPoint.getX();
            double x2 = nextPoint.getX();
            double y1 = currentPoint.getY();
            double y2 = nextPoint.getY();
            sum+= ( x2-x1) * (y2+y1);
            
        }
//        System.out.println(sum);
        if(sum<0){
         
            return points;
        }else {
         
            Collections.reverse(points);
            return points;
            
        }
        
        
    }
}
