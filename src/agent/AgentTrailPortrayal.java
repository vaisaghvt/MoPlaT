/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import agent.clustering.ClusteredAgent;
import app.RVOGui;
import environment.RVOSpace;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import motionPlanners.rvo.Line;
import motionPlanners.rvo.RVO_2_1;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
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
public class AgentTrailPortrayal extends SimplePortrayal2D {

    ArrayList<Double2D> points; // this is the list of points that will be painted in the trail
    //  public Paint paint;
    public double scale;
    public boolean filled;
    public double radius;
    protected double offset = 0.0;  // used only by CircledPortrayal2D
    private boolean trails;
    private Color trailColor = new Color(0.0f, 1.0f, 0.0f, 0.2f); // no effect?
    private Color agentColor = new Color(0.0f, 0.0f, 1.0f, 1.0f); // no effect?
    private float trailLineWidth = 1.5f;
    private float agentLineWidth = 5.0f;

    public AgentTrailPortrayal(double radius, boolean trails) {
        this.radius = radius;
        this.trails = trails;
        //  paint = trailColor;
        scale = RVOGui.SCALE;
        filled = false;
        points = new ArrayList<Double2D>();

    }

    public AgentTrailPortrayal(Paint paint, double scale, boolean filled) {
        //  this.paint = paint;
        this.scale = scale;
        this.filled = filled;

    }

    public void setColor(Color col) {
        agentColor = col;
        this.trailColor = new Color(col.getRed(), col.getGreen(), col.getBlue(), (int) ((255 - col.getTransparency()) * 0.4));

    }

    public void addPoint(Double2D pt) {
        if (trails) {
            points.add(pt);
        }


    }

    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

        RVOAgent me = ((RVOAgent) this);
        this.addPoint(new Double2D(info.draw.x, info.draw.y));
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


        Rectangle2D.Double draw = info.draw;
        final double width = 2 * radius * scale + offset;
        final double height = 2 * radius * scale + offset;

        // we are doing a simple draw, so we ignore the info.clip


        if (this instanceof ClusteredAgent) {
            graphics.setColor(Color.BLACK);
            ClusteredAgent tempAgent = (ClusteredAgent) this;
            final double clusteredWidth = 2 * tempAgent.getRadius() * scale + offset;
            final double clusteredHeight = 2 * tempAgent.getRadius() * scale + offset;
            graphics.drawOval((int) Math.round(tempAgent.getCentre().x*scale+offset - clusteredWidth / 2.0), (int) Math.round(tempAgent.getCentre().y*scale+offset - clusteredHeight / 2.0), (int) clusteredWidth, (int) clusteredHeight);
            graphics.drawLine((int) Math.round(tempAgent.getCentre().x*scale+offset), (int) Math.round(tempAgent.getCentre().y*scale+offset),(int) Math.round((tempAgent.getCentre().x+tempAgent.getRadius())*scale+offset), (int) Math.round((tempAgent.getCentre().y)*scale+offset));
         //   graphics.drawString(tempAgent.getCentre().x+","+tempAgent.getCentre().y, (int)(tempAgent.getCentre().x*scale), (int)(tempAgent.getCentre().y*scale));
            return;
        }

        double startx = -1;
        double starty = -1;
        double endx = 0, endy = 0;
        if (trails && points.size() > 1) {
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
                graphics.drawLine((int) Math.round(startx), (int) Math.round(starty), (int) Math.round(endx), (int) Math.round(endy));

                startx = endx;
                starty = endy;

            }
            final BasicStroke stroke2 = new BasicStroke(this.agentLineWidth);
            graphics.setStroke(stroke2);
        } else {

            startx = info.draw.x;
            starty = info.draw.y;

        }

        graphics.setPaint(agentColor);
        graphics.fillOval((int)Math.round(startx - width / 2.0), (int)Math.round(starty - height / 2.0), (int) width, (int) height);
        graphics.setStroke(new BasicStroke(1.0f));

            //Draw Current velocity of the agent
        if (me.showVelocity) {
            graphics.drawLine((int) Math.round((startx)),
                    (int) Math.round((starty)),
                    (int) Math.round((me.getVelocity().x) * scale+startx),
                    (int) Math.round((me.getVelocity().y) * scale+starty));
        }

    }
}
