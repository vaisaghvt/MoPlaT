/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import agent.clustering.ClusteredAgent;
import app.PropertySet;
import app.PropertySet.Model;
import app.RVOGui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import utility.Line;
import motionPlanners.rvo2.RVO_2_1;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
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
public class AgentPortrayal extends SimplePortrayal2D {

    public static boolean SHOW_ORCA_LINES;
    public static boolean SHOW_TRAILS;
    public static boolean SHOW_VELOCITY;
    public static boolean SHOW_PERCEPTION;
    public static boolean SHOW_STP;
//    public static boolean SHOW_PERCEIVED_STP;
//    public static boolean SHOW_PROTOTYPICAL_STP;
    ArrayList<Double2D> points; // this is the list of points that will be painted in the trail
    //  public Paint paint;
    private boolean trails;
    private double scale;
    protected double radius;
    protected double offset = 0.0;  // used only by CircledPortrayal2D
    private Color trailColor = new Color(0.0f, 1.0f, 0.0f, 0.2f); // no effect?
    private Color agentColor = new Color(0.0f, 0.0f, 1.0f, 1.0f); // no effect?
    private float trailLineWidth = 1.5f;
    private float agentLineWidth = 5.0f;
    private boolean showOrcaLines;
    private boolean showVelocity;
    private boolean showPerception;
    private boolean showSTP;
//    private boolean showPerceivedSTP;
//    private boolean showPrototypicalSTP;
    float alpha = 0.0f; //for fade-in fade-out effect

    //TODO: when are each of these portrayals used.. why do i have two with entirely different parameters??
    public AgentPortrayal() {
        radius = RVOAgent.RADIUS;
        scale = RVOGui.scale;
        showOrcaLines = SHOW_ORCA_LINES;
        trails = SHOW_TRAILS;
        showVelocity = SHOW_VELOCITY;
        points = new ArrayList<Double2D>();

        showPerception = SHOW_PERCEPTION;
        showSTP = SHOW_STP;
    }

    public void setColor(Color col) {
        agentColor = col;
//        trailColor = new Color(col.getRed(), col.getGreen(), col.getBlue(), (int) ((255-col.getTransparency())*1));
        trailColor = col;
    }

    public void addPoint(Double2D pt) {
        points.add(pt);

    }

    // assumes the graphics already has its color set
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

        RVOAgent me = ((RVOAgent) this);
        addPoint(new Double2D(me.getCurrentPosition().getX() * scale,
                me.getCurrentPosition().getY() * scale));


        //draw orcaLines for RVO2
        if (((RVOAgent) this).getVelocityCalculator() instanceof RVO_2_1) {

            RVO_2_1 rvo2 = (RVO_2_1) ((RVOAgent) this).getVelocityCalculator();
            if (showOrcaLines) {
                for (Line l : rvo2.getOrcaLines()) {
                    Point2d end = l.getEndPoint();
                    Point2d start = l.getStartPoint();
                    end.add(me.getCurrentPosition());
                    end.scale(scale);
                    start.add(me.getCurrentPosition());
                    start.scale(scale);
                    graphics.setColor(Color.CYAN);

                    graphics.setStroke(new BasicStroke(2.5f));
                    graphics.drawLine((int) start.x, (int) start.y, (int) end.x, (int) end.y);

                }
            }
        }

        //draw trail

        //for the scenario to test dense crowd with directional move
//        if(PropertySet.PBMSCENARIO == 1){
//            if(me.getPrefDirection().x>0)    
//                this.setColor(Color.BLACK);
//            else if(me.getPrefDirection().x<0)
//                this.setColor(Color.green);
//        }  

        double startx = me.getCurrentPosition().getX() * scale;
        double starty = me.getCurrentPosition().getY() * scale;
        
        double endx = 0, endy = 0;
        int countNumOfStepDraw = 40;   //used to display trail only for the last 50 steps

        if (trails && points.size() >= countNumOfStepDraw) { // if trails need to be drawn...
            final BasicStroke stroke;

            stroke = new BasicStroke(this.trailLineWidth);

            graphics.setStroke(stroke);

            for (int i = 0; i < countNumOfStepDraw; i++) {
                graphics.setPaint(new Color(trailColor.getRed(), trailColor.getGreen(), trailColor.getBlue(), (int) (255 * (i + 1) / countNumOfStepDraw)));

                Double2D pt = points.get(points.size() - (countNumOfStepDraw - i));
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
        }


        final double width = 2 * radius * scale + offset;
        final double height = 2 * radius * scale + offset;

        final double PSwidth = 2 * radius * (((RVOAgent) this).getPersonalSpaceFactor() + 1) * scale + offset;
        final double PSheight = 2 * radius * (((RVOAgent) this).getPersonalSpaceFactor() + 1) * scale + offset;

        if (this instanceof ClusteredAgent) {
            graphics.setColor(Color.BLACK);
            ClusteredAgent tempAgent = (ClusteredAgent) this;
            final double clusteredWidth = 2 * tempAgent.getRadius() * scale + offset;
            final double clusteredHeight = 2 * tempAgent.getRadius() * scale + offset;
            graphics.drawOval(
                    (int) Math.round(tempAgent.getCurrentPosition().x * scale - clusteredWidth / 2.0),
                    (int) Math.round(tempAgent.getCurrentPosition().y * scale - clusteredHeight / 2.0),
                    (int) clusteredWidth, (int) clusteredHeight);
            graphics.drawLine(
                    (int) Math.round(tempAgent.getCurrentPosition().x * scale + offset),
                    (int) Math.round(tempAgent.getCurrentPosition().y * scale + offset),
                    (int) Math.round((tempAgent.getCurrentPosition().x + tempAgent.getRadius()) * scale + offset),
                    (int) Math.round((tempAgent.getCurrentPosition().y) * scale + offset));

            return;
        }


        graphics.setPaint(agentColor);

        graphics.fillOval(
                (int) Math.round(me.getCurrentPosition().getX() * scale - width / 2.0),
                (int) Math.round(me.getCurrentPosition().getY() * scale - height / 2.0),
                (int) width, (int) height);
        graphics.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));

        //draw the personal space of agents
        graphics.drawOval(
                (int) Math.round(me.getCurrentPosition().getX() * scale - PSwidth / 2.0),
                (int) Math.round(me.getCurrentPosition().getY() * scale - PSheight / 2.0),
                (int) PSwidth, (int) PSheight);
        graphics.setStroke(new BasicStroke(1.0f));

        //Draw Current velocity of the agent
        if (showVelocity) {
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

    public void toggleShowVelocity() {
        showVelocity = !showVelocity;
    }

    public void toggleShowOrcaLines() {
        showOrcaLines = !showOrcaLines;
    }

    public void togglePerception() {
        showPerception = !showPerception;
    }

    public void toggleSTP() {
        showSTP = !showSTP;
    }

    public double getRadius() {
        return radius;
    }
}
