package com.github.davidmoten.grumpy.projection;

import com.github.davidmoten.grumpy.core.Position;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses GeoTools and JTS libraries to perform transformations between coordinate
 * reference systems.
 *
 * <p>
 * Set {@code Projector.forceXY = false} before instantiating any Projector if
 * you don't want the system property "org.geotools.referencing.forceXY" to be
 * set to truen (it will happen on first instantiation of a Projector by
 * default).
 *
 * @author dxm
 */
public class Projector {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    public static volatile boolean forceXY = true;

    private final ProjectorTarget target;
    private final ProjectorBounds bounds;

    private final MathTransform transform;
    private final GeometryFactory geometryFactory;

    public Projector(ProjectorBounds bounds, ProjectorTarget target) {
        // perform one time initialization in a thread safe way
        if (initialized.compareAndSet(false, true)) {
            if (forceXY)
                System.setProperty("org.geotools.referencing.forceXY", "true");
        }
        this.target = target;
        this.bounds = bounds;
        try {
            transform = CRS.findMathTransform(FeatureUtil.getCrs(FeatureUtil.EPSG_4326),
                    FeatureUtil.getCrs(bounds.getSrs()));

        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
        geometryFactory = new GeometryFactory();
    }

    public ProjectorBounds getBounds() {
        return bounds;
    }

    public ProjectorTarget getTarget() {
        return target;
    }

    public org.locationtech.jts.geom.Point getGeometryPointInSrs(double lat, double lon) {
        Coordinate coordinate = new Coordinate(lon, lat);
		org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coordinate);
        try {
            return (org.locationtech.jts.geom.Point) JTS.transform(point, transform);
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }

    public org.locationtech.jts.geom.Point getGeometryPointInSrsRelativeTo(double lat,
                                                                           double lon, double relativeLat, double relativeLon, double relativeX, double relativeY) {

        double diffLon1 = lon - relativeLon;
        double diffLon2 = lon - relativeLon + 360;
        if (Math.abs(diffLon1) > Math.abs(diffLon2))
            lon = lon + 360;
        double sign = Math.signum(lon - relativeLon);

		org.locationtech.jts.geom.Point point = getGeometryPointInSrs(lat, lon);
        double periodAtLat = periodAtLat(lat);
        double x = point.getX();
        // makes the assumption that increasing lon = increasing X
        // which is probably valid for most common projections
        // TODO determine when invalid or handle
        if (sign >= 0) {
            while (x - periodAtLat >= relativeX)
                x -= periodAtLat;
            while (x < relativeX)
                x += periodAtLat;
        } else {
            while (x >= relativeX)
                x -= periodAtLat;
            while (x + periodAtLat < relativeX)
                x += periodAtLat;
        }

        return createPoint(x, point.getY());
    }

    public org.locationtech.jts.geom.Point createPoint(double x, double y) {
        return geometryFactory.createPoint(new Coordinate(x, y));
    }

    public double periodAtLat(double lat) {
        return getGeometryPointInSrs(lat, 180).getX() - getGeometryPointInSrs(lat, -180).getX();
    }

    public Point2D.Double getTargetPoint(org.locationtech.jts.geom.Point point) {
        double proportionX = (point.getX() - bounds.getMinX())
                / (bounds.getMaxX() - bounds.getMinX());
        double proportionY = (bounds.getMaxY() - point.getY())
                / (bounds.getMaxY() - bounds.getMinY());
        double x = proportionX * target.getWidth();
        double y = proportionY * target.getHeight();
        return new Point2D.Double(x, y);
    }

    public org.locationtech.jts.geom.Point getFirstXAfter(Projector projector, double lat,
                                                            double lon, double x) {
        org.locationtech.jts.geom.Point point = projector.getGeometryPointInSrs(lat, lon);
        double x2 = point.getX();
        double periodX = periodAtLat(lat);
        while (x2 - periodX >= x)
            x2 -= periodX;
        while (x2 + periodX < x)
            x2 += periodX;
        return createPoint(x2, point.getY());
    }

    public Point toPoint(double lat, double lon) {
        Point2D point2D = toPoint2D(lat, lon);
        Point p = new Point();
        p.x = (int) Math.round(point2D.getX());
        p.y = (int) Math.round(point2D.getY());
        return p;
    }

    public Point2D.Double toPointInSrs(double lat, double lon) {
        org.locationtech.jts.geom.Point point = getGeometryPointInSrs(lat, lon);
        return new Point2D.Double(point.getX(), point.getY());
    }

    public Point2D.Double toPoint2D(double lat, double lon) {
        Coordinate coordinate = new Coordinate(lon, lat);
        org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coordinate);
        try {
            point = (org.locationtech.jts.geom.Point) JTS.transform(point, transform);

            double proportionX;
            if (point.getX() > bounds.getMaxX() || point.getX() < bounds.getMinX()) {
                // assume the maxX occurs at longitude 180 (true for EPSG 3857
                // spherical mercator) but maybe not true for other projections?
                Coordinate c = new Coordinate(180, 0);
                org.locationtech.jts.geom.Point pt = (org.locationtech.jts.geom.Point) JTS
                        .transform(geometryFactory.createPoint(c), transform);
                double maximumX = pt.getX();
                if (point.getX() > bounds.getMaxX())
                    proportionX = (point.getX() - 2 * maximumX - bounds.getMinX())
                            / (bounds.getMaxX() - bounds.getMinX());
                else
                    proportionX = (point.getX() + 2 * maximumX - bounds.getMinX())
                            / (bounds.getMaxX() - bounds.getMinX());
            } else {
                proportionX = (point.getX() - bounds.getMinX())
                        / (bounds.getMaxX() - bounds.getMinX());
            }
            double proportionY = (bounds.getMaxY() - point.getY())
                    / (bounds.getMaxY() - bounds.getMinY());
            Point2D.Double point2D = new Point2D.Double(proportionX * target.getWidth(),
                    proportionY * target.getHeight());
            return point2D;
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }

    public Position toPosition(double targetX, double targetY) {
        double proportionX = targetX / target.getWidth();
        double proportionY = targetY / target.getHeight();
        double x = proportionX * (bounds.getMaxX() - bounds.getMinX()) + bounds.getMinX();
        double y = bounds.getMaxY() - proportionY * (bounds.getMaxY() - bounds.getMinY());
        Coordinate coordinate = new Coordinate(x, y);
        org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coordinate);
        try {
            point = (org.locationtech.jts.geom.Point) JTS.transform(point, transform.inverse());
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
        return new Position(point.getY(), point.getX());
    }

    public Position toPositionFromSrs(double x, double y) {
        return FeatureUtil.convertToLatLon(x, y, bounds.getSrs());
    }

    @Override
    public String toString() {
        return "ProjectorImpl [target=" + target + ", bounds=" + bounds + "]";
    }

}
