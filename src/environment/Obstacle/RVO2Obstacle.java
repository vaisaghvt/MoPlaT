/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.Obstacle;


import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * RVO2Obstacle
 *
 * @author michaellees
 * Created: Dec 1, 2010
 *
 * Copyright michaellees 
 *
 * Description:
 *
 *  Implementation of RVO2Obstacle according to RVO2 - this should be standardized
 *  if possible between RVO2 and RVO1.1
 */
public class RVO2Obstacle extends RVOObstacle{

    boolean isConvex;
    Point2d point;


    RVO2Obstacle nextObstacle;
    RVO2Obstacle prevObstacle;

    public RVO2Obstacle() {
        isConvex = true;
        point = new Point2d();
  
    }

    public RVO2Obstacle(boolean isConvex, Point2d point) {
        this.isConvex = isConvex;
        this.point = point;
    }

    public boolean isConvex() {
        return isConvex;
    }

    public void setIsConvex(boolean isConvex) {
        this.isConvex = isConvex;
    }

    public Point2d getPoint() {
        return point;
    }

    public void setPoint(Point2d point) {
        this.point = point;
    }

  
    
    public RVO2Obstacle getNext() {
        return nextObstacle;
    }

    public void setNext(RVO2Obstacle obstacle){
        nextObstacle = obstacle;
    }


    public RVO2Obstacle getPrev() {
        return prevObstacle;
    }

    public void setPrev(RVO2Obstacle obstacle){
        prevObstacle = obstacle;
    }

    public boolean equals(RVO2Obstacle obs){
        if(obs.getPoint().getX() == this.getPoint().getX() && obs.getPoint().getY() == this.getPoint().getY()){
            return true;
        }
        return false;
    }
}
