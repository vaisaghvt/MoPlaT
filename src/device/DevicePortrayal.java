/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package device;

//import agent.*;
//import agent.clustering.ClusteredAgent;
//import app.PropertySet;
//import app.PropertySet.Model;
import app.RVOGui;
//import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
//import javax.vecmath.Point2d;
//import utility.Line;
//import motionPlanners.rvo2.RVO_2_1;
//import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
//import sim.portrayal.Inspector;
//import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

/**
 *
 *
 * @author michaellees Created: Nov 29, 2010
 *
 *
 *
 * Description: This class which is a parent of RVOAgent describes the things
 * that are drawn including the Agent itself, it's trail, it's velocity or in
 * special cases: the ORCA lines, etc. based on flags that are set. For any
 * changes to be made to what is drawn on the pallete, change the code here.
 *
 */
public class DevicePortrayal extends SimplePortrayal2D {
    ArrayList<Double2D> points; // this is the list of points that will be painted in the trail
    //  public Paint paint;

    private double scale;
    protected double radius;
    protected double offset = 0.0;  // used only by CircledPortrayal2D
//    private Color trailColor = new Color(0.0f, 1.0f, 0.0f, 0.2f); // no effect?
    private Color deviceColor = new Color(0.0f, 0.0f, 1.0f, 1.0f); // no effect?

    //TODO: when are each of these portrayals used.. why do i have two with entirely different parameters??
    public DevicePortrayal() {
        radius = Device.RADIUS;
        scale = RVOGui.scale;

        points = new ArrayList<Double2D>();
    }

    public void setColor(Color col) {
        deviceColor = col;
    }
    
    public Color getColor() {
        return deviceColor;
    }

    public void addPoint(Double2D pt) {
        points.add(pt);

    }

    // assumes the graphics already has its color set
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

        Device me = ((Device) this);
        addPoint(new Double2D(me.getCurrentPosition().getX() * scale,
                me.getCurrentPosition().getY() * scale));

        final double width =  radius * scale + offset;
        final double height = radius * scale + offset;

     
        if(me.isDense())    {
            me.setColor(Color.RED);
        }
        else    {
            me.setColor(Color.GREEN);
        }
        graphics.setPaint(me.getColor());
        graphics.fillArc((int) Math.round(me.getCurrentPosition().getX() * scale - width / 2.0), (int) Math.round(me.getCurrentPosition().getY() * scale - height / 2.0), 
                (int) width, (int) height, 0, 360);
//        graphics.drawArc((int) Math.round(me.getCurrentPosition().getX() * scale - width / 2.0), (int) Math.round(me.getCurrentPosition().getY() * scale - height / 2.0), 
//                (int) width, (int) height, 0, 360);

        me.setColor(Color.yellow);
        if(me.hasForbiddenArea()){
            for(int i=0;i<4;i++){
                if(me.isQuardantForbidden(i)){
//                    System.out.println(i+":"+i*90+","+(i+1)*89);
                    graphics.drawArc((int) Math.round(me.getCurrentPosition().getX() * scale - (3*width) / 2.0), (int) Math.round(me.getCurrentPosition().getY() * scale - (3*height) / 2.0), 
                (int) width*3, (int) height*3, i*90, 90);

                }
            }
        }
    }
   

}
