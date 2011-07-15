/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Dec 1, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class Line {

    public Point2d point;
    public Vector2d direction;

    private Point2d startPoint;
    private Point2d endPoint;

    public Line(Line line) {
        this.point = new Point2d(line.point);
        this.direction = new Vector2d(line.direction);
    }

    public Line() {
        this.point = new Point2d();
        this.direction = new Vector2d();
    }
    
    public Point2d getEndPoint() {
        if(endPoint == null)
            this.calculatePoints();
        return endPoint;
    }

    public Point2d getStartPoint() {
        if(startPoint == null)
            this.calculatePoints();
        return startPoint;
    }

    

    public void calculatePoints(){
        endPoint = new Point2d(direction);
        endPoint.scale(100);

        startPoint = new Point2d(direction);
        startPoint.scale(100);
        startPoint.negate();

       // endPoint.add(point);
       startPoint.add(point);
  
    }
}
