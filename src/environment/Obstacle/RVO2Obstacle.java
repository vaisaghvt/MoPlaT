/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.Obstacle;


import app.RVOGui;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.vecmath.Point2d;
import sim.portrayal.DrawInfo2D;

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
 *  These obstacles as per RVO2 requirements
 */
public class RVO2Obstacle extends RVOObstacle{

    boolean convex;
    Point2d point;


    RVO2Obstacle nextObstacle;
    RVO2Obstacle prevObstacle;

    public RVO2Obstacle() {
         point = new Point2d();
     }


    public boolean isConvex() {
        return convex;
    }

    public void setConvex(boolean isConvex) {
        this.convex = isConvex;
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
    
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
         graphics.setColor(Color.black);

        graphics.drawLine((int)(point.getX()*RVOGui.scale),
                (int)(point.getY()*RVOGui.scale),
                (int)(nextObstacle.getPoint().getX()*RVOGui.scale),
                (int)(nextObstacle.getPoint().getY()*RVOGui.scale));
    }
}
