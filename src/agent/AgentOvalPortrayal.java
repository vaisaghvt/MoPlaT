/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import sim.portrayal.*;
import java.awt.*;
import java.awt.geom.*;
import sim.display.*;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Nov 29, 2010
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class AgentOvalPortrayal {
    /*
    Copyright 2006 by Sean Luke and George Mason University
    Licensed under the Academic Free License version 3.0
    See the file "LICENSE" for more information
     */

    public Paint paint;
    public double scale;
    public boolean filled;
    protected double offset = 0.0;  // used only by CircledPortrayal2D

  



    public AgentOvalPortrayal() {
        this(Color.gray, 1.0, true);
    }

    public AgentOvalPortrayal(Paint paint) {
        this(paint, 1.0, true);
    }

    public AgentOvalPortrayal(double scale) {
        this(Color.gray, scale, true);
    }

    public AgentOvalPortrayal(Paint paint, double scale) {
        this(paint, scale, true);
    }

    public AgentOvalPortrayal(Paint paint, boolean filled) {
        this(paint, 1.0, filled);
    }

    public AgentOvalPortrayal(double scale, boolean filled) {
        this(Color.gray, scale, filled);
    }

    public AgentOvalPortrayal(Paint paint, double scale, boolean filled) {
        this.paint = paint;
        this.scale = scale;
        this.filled = filled;
    }
    Ellipse2D.Double preciseEllipse = new Ellipse2D.Double();
    // assumes the graphics already has its color set

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Rectangle2D.Double draw = info.draw;
        final double width = draw.width * scale + offset;
        final double height = draw.height * scale + offset;

        graphics.setPaint(paint);
        // we are doing a simple draw, so we ignore the info.clip

        if (info.precise) {
            preciseEllipse.setFrame(info.draw.x - width / 2.0, info.draw.y - height / 2.0, width, height);
            if (filled) {
                graphics.fill(preciseEllipse);
            } else {
                graphics.draw(preciseEllipse);
            }
            return;
        }

        final int x = (int) (draw.x - width / 2.0);
        final int y = (int) (draw.y - height / 2.0);
        int w = (int) (width);
        int h = (int) (height);

        // draw centered on the origin
        if (filled) {
            graphics.fillOval(x, y, w, h);
        } else {
            graphics.drawOval(x, y, w, h);
        }
    }

    /** If drawing area intersects selected area, add last portrayed object to the bag */
    public boolean hitObject(Object object, DrawInfo2D range) {
        final double SLOP = 1.0;  // need a little extra area to hit objects
        final double width = range.draw.width * scale;
        final double height = range.draw.height * scale;
        preciseEllipse.setFrame(range.draw.x - width / 2 - SLOP, range.draw.y - height / 2 - SLOP, width + SLOP * 2, height + SLOP * 2);
        return (preciseEllipse.intersects(range.clip.x, range.clip.y, range.clip.width, range.clip.height));
    }
}


