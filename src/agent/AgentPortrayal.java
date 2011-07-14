/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import agent.clustering.ClusteredAgent;
import app.RVOGui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import motionPlanners.rvo.Line;
import motionPlanners.rvo.RVO_2_1;
import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Nov 29, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:This class which is a parent of RVOAgent describes the things that
 * are drawn including the Agent itself, it's trail, it's velocity or in special
 * cases: the ORCA lines, etc. based on flags that are set.
 * For any changes to be made to what is drawn on the pallete, change the code here.
 *
 */
public class AgentPortrayal extends SimplePortrayal2D {

    ArrayList<Double2D> points; // this is the list of points that will be painted in the trail
    //  public Paint paint;
    private boolean trails;
    public boolean filled;
    public double scale;
    public double radius;
    protected double offset = 0.0;  // used only by CircledPortrayal2D
    private Color trailColor = new Color(0.0f, 1.0f, 0.0f, 0.2f); // no effect?
    private Color agentColor = new Color(0.0f, 0.0f, 1.0f, 1.0f); // no effect?
    private float trailLineWidth = 1.5f;
    private float agentLineWidth = 5.0f;

    //TODO: when are each of these portrayals used.. why do i have two with entirely different parameters??
    public AgentPortrayal(double radius, boolean trails) {
        this.radius = radius;
        this.trails = trails;
        scale = RVOGui.SCALE;
        filled = false;
        points = new ArrayList<Double2D>();

    }

    public void setColor(Color col) {
        agentColor = col;
        trailColor = new Color(col.getRed(), col.getGreen(), col.getBlue(), (int) ((255 - col.getTransparency()) * 0.4));

    }

    public void addPoint(Double2D pt) {

        points.add(pt);

    }

    // assumes the graphics already has its color set
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

        RVOAgent me = ((RVOAgent) this);
        addPoint(new Double2D(info.draw.x, info.draw.y));
        graphics.setPaint(trailColor);

        //draw orcaLines for RVO2
        if (((RVOAgent) this).getRvoCalc() instanceof RVO_2_1) {

            RVO_2_1 rvo2 = (RVO_2_1) ((RVOAgent) this).getRvoCalc();
            if (rvo2.showLines) {
                for (Line l : rvo2.getOrcaLines()) {
                    Point2d end = l.getEndPoint();
                    Point2d start = l.getStartPoint();
                    end.add(me.getCurrentPosition());
                    end.scale(scale);
                    start.add(me.getCurrentPosition());
                    start.scale(scale);

                    graphics.drawLine((int) start.x, (int) start.y, (int) end.x, (int) end.y);

                }
            }
        }


        final double width = 2 * radius * scale + offset;
        final double height = 2 * radius * scale + offset;

  


        if (this instanceof ClusteredAgent) {
            graphics.setColor(Color.BLACK);
            ClusteredAgent tempAgent = (ClusteredAgent) this;
            final double clusteredWidth = 2 * tempAgent.getRadius() * scale + offset;
            final double clusteredHeight = 2 * tempAgent.getRadius() * scale + offset;
            graphics.drawOval(
                    (int) Math.round(tempAgent.getCentre().x * scale + offset - clusteredWidth / 2.0), 
                    (int) Math.round(tempAgent.getCentre().y * scale + offset - clusteredHeight / 2.0), 
                    (int) clusteredWidth, (int) clusteredHeight);
            graphics.drawLine(
                    (int) Math.round(tempAgent.getCentre().x * scale + offset), 
                    (int) Math.round(tempAgent.getCentre().y * scale + offset), 
                    (int) Math.round((tempAgent.getCentre().x + tempAgent.getRadius()) * scale + offset), 
                    (int) Math.round((tempAgent.getCentre().y) * scale + offset));
  
            return;
        }

        double startx = -1;
        double starty = -1;
        double endx = 0, endy = 0;
        if (trails && points.size() > 1) { // if trails need to be drawn...
            final BasicStroke stroke = new BasicStroke(this.trailLineWidth);
            graphics.setStroke(stroke);
            for (Double2D pt : points) {
                if (startx == -1) {
                    startx = pt.x;
                    starty = pt.y;
                    continue;
                }

                endx = pt.x;
                endy = pt.y;
                graphics.drawLine(
                        (int) Math.round(startx), (int) Math.round(starty), 
                        (int) Math.round(endx), (int) Math.round(endy));

                startx = endx;
                starty = endy;

            }
            final BasicStroke stroke2 = new BasicStroke(agentLineWidth);
            graphics.setStroke(stroke2);
        } else {

            startx = info.draw.x;
            starty = info.draw.y;

        }

        
        graphics.setPaint(agentColor);
        graphics.fillOval(
                (int) Math.round(startx - width / 2.0), 
                (int) Math.round(starty - height / 2.0), 
                (int) width, (int) height);
        graphics.setStroke(new BasicStroke(1.0f));

        //Draw Current velocity of the agent
        if (me.SHOW_VELOCITY) {
            graphics.drawLine((int) Math.round((startx)),
                    (int) Math.round((starty)),
                    (int) Math.round((me.getVelocity().x) * scale + startx),
                    (int) Math.round((me.getVelocity().y) * scale + starty));
        }

    }

    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        return new AgentInspector(super.getInspector(wrapper, state), wrapper, state);
    }

    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
        final double SLOP = 1.0;  // need a little extra diameter to hit circles
        double diameter = radius * 2.0;
        final double width = range.draw.width * diameter;
        final double height = range.draw.height * diameter;

        Ellipse2D.Double ellipse = new Ellipse2D.Double(
                range.draw.x - width / 2 - SLOP,
                range.draw.y - height / 2 - SLOP,
                width + SLOP * 2,
                height + SLOP * 2);
        return (ellipse.intersects(range.clip.x, range.clip.y, range.clip.width, range.clip.height));
    }

    public void toggleTrails() {
        trails = !trails;
    }
}
