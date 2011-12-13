/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.math.BigDecimal;
import java.math.MathContext;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 *
 * @author vaisagh
 */
public class PrecisePoint {

    private BigDecimal x;
    private BigDecimal y;
    private Point2d pointValue;
    private Vector2d vectorValue;

    public PrecisePoint(double x, double y) {
        this.x = new BigDecimal(x, MathContext.DECIMAL32);
        this.y = new BigDecimal(y, MathContext.DECIMAL32);
        pointValue = null;
        vectorValue = null;
    }

    public PrecisePoint() {
        this.x = new BigDecimal("0.000");
        this.y = new BigDecimal("0.000");
        pointValue = null;
        vectorValue = null;
    }

    public final double getX() {
        return x.doubleValue();
    }

    public void setX(double x) {
        this.x = new BigDecimal(x, MathContext.DECIMAL32);
        pointValue = null;
        vectorValue = null;
    }

    public final double getY() {
        return y.doubleValue();
    }

    public void setY(double y) {
        this.y = new BigDecimal(y, MathContext.DECIMAL32);
        pointValue = null;
        vectorValue = null;
    }

    public Point2d toPoint() {
        if (pointValue == null) {
            pointValue = new Point2d(this.getX(), this.getY());
        }
        return pointValue;
    }

    public Vector2d toVector() {
         if (vectorValue == null) {
            vectorValue = new Vector2d(this.getX(), this.getY());
        }
        return vectorValue;
    }
}
