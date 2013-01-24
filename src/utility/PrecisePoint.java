/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.math.BigDecimal;
import java.math.MathContext;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import static app.PropertySet.HIGH_PRECISION;

/**
 *
 * This is a replacement for Point2d with added precision. It helps in extremely
 * dense scenarios at very high scales where math errors are likely to result in
 * overlapping agents
 *
 * @author vaisagh
 */
public class PrecisePoint {

    private double x;
    private double y;
    private Point2d pointValue;
    private Vector2d vectorValue;
    private BigDecimal bigY;
    private BigDecimal bigX;

    public PrecisePoint(double x, double y) {
        if (HIGH_PRECISION) {
            this.bigX = new BigDecimal(x, MathContext.DECIMAL32);
            this.bigY = new BigDecimal(y, MathContext.DECIMAL32);
        } else {
            this.x = x;
            this.y = y;
        }
        pointValue = null;
        vectorValue = null;
    }

    public PrecisePoint() {
        if (HIGH_PRECISION) {
            this.bigX = new BigDecimal("0.000");
            this.bigY = new BigDecimal("0.000");
        } else {
            this.x = 0;
            this.y = 0;
        }
        pointValue = null;
        vectorValue = null;
    }

    public final double getX() {
        if (HIGH_PRECISION) {
            return bigX.doubleValue();
        } else {
            return x;
        }
    }

    public void setX(double x) {
        if (HIGH_PRECISION) {
            this.bigX = new BigDecimal(x, MathContext.DECIMAL32);
        } else {
            this.x = x;
        }
        pointValue = null;
        vectorValue = null;
    }

    public final double getY() {
        if (HIGH_PRECISION) {
            return bigY.doubleValue();
        } else {
            return y;
        }
    }

    public void setY(double y) {
        if (HIGH_PRECISION) {
            this.bigY = new BigDecimal(y, MathContext.DECIMAL32);
        } else {
            this.y = y;
        }
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
        int hash = 7;
        if (!HIGH_PRECISION) {
            hash = 13 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
            hash = 13 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        } else {
            hash = 47 * hash + (this.bigY != null ? this.bigY.hashCode() : 0);
            hash = 47 * hash + (this.bigX != null ? this.bigX.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!HIGH_PRECISION) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PrecisePoint other = (PrecisePoint) obj;
            if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
                return false;
            }
            if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
                return false;
            }
            return true;
        } else {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PrecisePoint other = (PrecisePoint) obj;
            if (this.bigY != other.bigY && (this.bigY == null || !this.bigY.equals(other.bigY))) {
                return false;
            }
            if (this.bigX != other.bigX && (this.bigX == null || !this.bigX.equals(other.bigX))) {
                return false;
            }
            return true;
        }
    }

    //@hunan added
    public void scale(double n) {
        setX(this.getX() * n);
        setY(this.getY() * n);
    }

    //@hunan added
    public void add(Point2d toPoint) {
        setX(this.getX() + toPoint.getX());
        setY(this.getY() + toPoint.getY());
    }
}
