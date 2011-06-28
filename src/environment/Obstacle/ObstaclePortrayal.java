/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.Obstacle;

import app.RVOGui;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;

/**
 *
 * @author Vaisagh
 */
public class ObstaclePortrayal extends SimplePortrayal2D {

    ArrayList<Point2d> vertices = new ArrayList<Point2d>();

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        
        int xPoints[] = new int[vertices.size()];
        int yPoints[] = new int[vertices.size()];


        for (int j = 0; j < vertices.size(); j++) {
            double x = vertices.get(j).getX() * RVOGui.SCALE;
            double y = vertices.get(j).getY() * RVOGui.SCALE;
            xPoints[j] = (int) Math.round(x);
            yPoints[j] = (int) Math.round(y);
 
        }

        graphics.setColor(Color.red);
        graphics.fillPolygon(xPoints, yPoints, vertices.size());

        graphics.setColor(Color.black);

 graphics.drawPolygon(xPoints, yPoints, vertices.size());
        //return new sim.portrayal.simple.ShapePortrayal2D(xPoints,yPoints,new Color(0.5f, 0.5f, 1.0f);
    }
}
