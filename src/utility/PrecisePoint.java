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

    @Override
    public String toString() {
        return "PrecisePoint{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.x != null ? this.x.hashCode() : 0);
        hash = 29 * hash + (this.y != null ? this.y.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrecisePoint other = (PrecisePoint) obj;
        if (this.x.compareTo(other.x)!=0){
            return false;
        }
        if (this.y.compareTo(other.y)!=0) {
            return false;
        }
        return true;
    }
    
    //@hunan added
    public void scale(double n){
        setX(this.getX()* n);
        setY(this.getY()* n);
    }
    
    //@hunan added
    public void add(Point2d toPoint) {
        setX(this.getX() + toPoint.getX());
        setY(this.getY() + toPoint.getY());
    }
   
}
