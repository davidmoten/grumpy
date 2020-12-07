package com.github.davidmoten.grumpy.wms;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;

public class RendererUtil {

    public static void useAntialiasing(Graphics2D g) {
        RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.addRenderingHints(renderHints);
    }

    public static List<GeneralPath> toPathGreatCircle(Projector projector, List<Position> positions) {
        return toPath(projector, Position.interpolateLongitude(positions));
    }

    public static List<GeneralPath> toPath(Projector projector, List<Position> positions) {
        List<GeneralPath> list = new ArrayList<GeneralPath>();

        if (positions.size() < 2)
            throw new RuntimeException("must provide at least two positions");

        list.add(createPath(projector, positions, 0));
        list.add(createPath(projector, positions, -projector.periodAtLat(0)));
        list.add(createPath(projector, positions, projector.periodAtLat(0)));

        return list;
    }

    private static List<org.locationtech.jts.geom.Point> getPathPoints(Projector projector,
            List<Position> positions, double deltaX) {
        List<org.locationtech.jts.geom.Point> list = new ArrayList<org.locationtech.jts.geom.Point>();
        Double firstPointLat = null;
        Double firstPointLon = null;
        org.locationtech.jts.geom.Point firstPoint = null;
        for (Position pos : positions) {
            Position p = pos.normalizeLongitude();
            if (firstPoint == null) {
                firstPoint = projector.getFirstXAfter(projector, p.getLat(), p.getLon(), projector
                        .getBounds().getMinX() + deltaX);
                firstPointLat = p.getLat();
                firstPointLon = p.getLon();
                list.add(firstPoint);
            } else {
                org.locationtech.jts.geom.Point point = projector
                        .getGeometryPointInSrsRelativeTo(p.getLat(), p.getLon(), firstPointLat,
                                firstPointLon, firstPoint.getX(), firstPoint.getY());
                list.add(point);
            }
        }
        return list;
    }

    private static GeneralPath createPath(Projector projector, List<Position> positions,
            double deltaX) {
        List<org.locationtech.jts.geom.Point> points = getPathPoints(projector, positions, deltaX);
        GeneralPath path = new GeneralPath();
        boolean first = true;
        for (org.locationtech.jts.geom.Point point : points) {
            Point2D.Double pt = projector.getTargetPoint(point);
            if (first) {
                path.moveTo(pt.x, pt.y);
                first = false;
            } else
                path.lineTo(pt.x, pt.y);
        }
        return path;
    }

    public static Point2D[] getPoints(Projector projector, List<Position> positions) {
        List<Point2D> points = new ArrayList<Point2D>();
        for (Position position : positions)
            points.add(projector.toPoint2D(position.getLat(), position.getLon()));
        return points.toArray(new Point2D[] {});
    }

    public static List<Position> getCircle(Position position, double radiusKm, double numPoints) {

        List<Position> positions = new ArrayList<Position>();
        for (int i = 0; i < numPoints; i++) {
            double bearing = 360.0 * i / numPoints;
            Position p = position.predict(radiusKm, bearing).normalizeLongitude();
            positions.add(p);
        }
        positions.add(positions.get(0));
        return positions;
    }

    public static void draw(Graphics2D g, List<? extends Shape> shapes) {
        for (Shape shape : shapes)
            g.draw(shape);
    }

    public static void fill(Graphics2D g, List<? extends Shape> shapes) {
        for (Shape shape : shapes)
            g.fill(shape);
    }

}
