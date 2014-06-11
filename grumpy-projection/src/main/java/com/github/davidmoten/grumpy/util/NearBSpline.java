package com.github.davidmoten.grumpy.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

/**
 * A shape that is almost a B-spline curve but not necessarily exactly. If the
 * shape has <i>n</i> points, the following things are guaranteed:
 * <ul>
 * <li><i>n</i> == 2: the shape is a straight line,
 * <li><i>n</i> == 3: the shape is a quadratic curve,
 * <li><i>n</i> == 4: the shape is a cubic curve,
 * <li><i>n</i> >= 5: the shape consists of segments, which are joined together
 * so that the first derivate is continuous at intermittent points
 * </ul>
 * 
 */
public class NearBSpline implements Shape, Cloneable {
    /** holds the combination of curve and line segments */
    private GeneralPath path;

    /** the last segment added to the path */
    private Shape lastPart;

    /** the individual segments of the path */
    private Shape[] segments;

    /**
     * Creates a new, empty b-spline
     */
    public NearBSpline() {
    }

    /**
     * Creates a new b-spline with the given control points
     * 
     * @param points
     *            the control points for the curve
     */
    public NearBSpline(Point2D[] points) {
        setLine(points);
    }

    /**
     * Sets the curve using control points given in integer precision
     * 
     * @param points
     *            the control points for the curve
     */
    public void setLine(int... points) {
        double[] pd = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            pd[i] = points[i];
        }
        setLine(pd);
    }

    /**
     * Sets the curve using control points given in double precision
     * 
     * @param points
     *            the control points for the curve
     */
    public void setLine(double... points) {
        path = new GeneralPath();
        path.moveTo((float) points[0], (float) points[1]);
        segments = new Shape[points.length / 2 - 1];

        for (int i = 2; i < points.length;) {
            switch (points.length - i) {
            case 2:
                lastPart = new Line2D.Float((float) path.getCurrentPoint().getX(), (float) path.getCurrentPoint()
                        .getY(), (float) points[i], (float) points[i + 1]);
                path.append(lastPart, true);
                segments[i / 2 - 1] = lastPart;
                i += 2;
                break;
            case 4:
                lastPart = new QuadCurve2D.Float((float) path.getCurrentPoint().getX(), (float) path.getCurrentPoint()
                        .getY(), (float) points[i], (float) points[i + 1], (float) points[i + 2], (float) points[i + 3]);
                path.append(lastPart, true);
                segments[i / 2 - 1] = lastPart;
                segments[i / 2] = lastPart;
                i += 4;
                break;
            case 6:
                lastPart = new CubicCurve2D.Double(path.getCurrentPoint().getX(), path.getCurrentPoint().getY(),
                        points[i], points[i + 1], points[i + 2], points[i + 3], points[i + 4], points[i + 5]);
                path.append(lastPart, true);
                segments[i / 2 - 1] = lastPart;
                segments[i / 2] = lastPart;
                segments[i / 2 + 1] = lastPart;
                i += 6;
                break;
            default: // use two points and add one extra between 2nd and 3rd
                float x = (float) (points[i + 2] + points[i + 4]) / 2F;
                float y = (float) (points[i + 3] + points[i + 5]) / 2F;

                lastPart = new CubicCurve2D.Double(path.getCurrentPoint().getX(), path.getCurrentPoint().getY(),
                        points[i], points[i + 1], points[i + 2], points[i + 3], x, y);

                path.append(lastPart, true);
                segments[i / 2 - 1] = lastPart;
                segments[i / 2] = lastPart;

                i += 4;
            }
        }
    }

    /**
     * Sets the curve using control points given as Point2D objects
     * 
     * @param points
     *            the control points for the curve
     */
    public void setLine(Point2D[] points) {
        double[] pd = new double[points.length * 2];
        for (int i = 0; i < points.length; i++) {
            Point2D p = points[i];
            pd[i * 2] = p.getX();
            pd[i * 2 + 1] = p.getY();
        }
        setLine(pd);
    }

    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    public Rectangle2D getBounds2D() {
        return path.getBounds2D();
    }

    public boolean contains(double x, double y) {
        return path.contains(x, y);
    }

    public boolean contains(Point2D p) {
        return path.contains(p);
    }

    public boolean intersects(double x, double y, double w, double h) {
        return path.intersects(x, y, w, h);
    }

    public boolean intersects(Rectangle2D r) {
        return path.intersects(r);
    }

    public boolean contains(double x, double y, double w, double h) {
        return path.contains(x, y, w, h);
    }

    public boolean contains(Rectangle2D r) {
        return path.contains(r);
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return path.getPathIterator(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return path.getPathIterator(at, flatness);
    }

    /**
     * Returns the last piece of this curve, which should be the most likely
     * thing to intercept with rectangles at the end.
     * 
     * @return a Line2D, QuadCurve2D, or CubicCurve2D object
     */
    public Shape getLastPart() {
        return lastPart;
    }

    /**
     * Return the segment of the path that should be adjacent to the control
     * point at the given index.
     * 
     * @param i
     *            the index of the control point
     * @return the path segment adjacent to the control point (a Line2D,
     *         QuadCurve2D, or CubicCurve2D object)
     */
    public Shape getSegment(int i) {
        return segments[i];
    }

    public GeneralPath getPath() {
        return path;
    }
}
