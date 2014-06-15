package com.github.davidmoten.grumpy;

import java.awt.geom.Point2D.Double;

import org.junit.Test;

import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.grumpy.projection.ProjectorTarget;
import com.vividsolutions.jts.geom.Point;

public class ProjectorTest {

    @Test
    public void testTransformWrapping() {
        ProjectorTarget target = new ProjectorTarget(300, 200);
        ProjectorBounds bounds = new ProjectorBounds("EPSG:3857", 18924313.4349, -4865942.2795, -18924313.4349,
                -3503549.8435);
        Projector projector = new Projector(bounds, target);
        Point p = projector.getGeometryPointInSrs(-35, 140);
        Double point = projector.getTargetPoint(p);
        System.out.println("x1=" + p.getX() + " point=" + point);
        Point p2 = projector.getGeometryPointInSrsRelativeTo(-35, 141, -35, 140, p.getX(), p.getY());
        System.out.println("x2=" + p2.getX());
        double x3 = p.getX() - projector.periodAtLat(-35);
        Double point3 = projector.getTargetPoint(projector.createPoint(x3, p.getY()));
        System.out.println("x3=" + x3 + " point=" + point3);
        double x4 = p.getX() + projector.periodAtLat(-35);
        Double point4 = projector.getTargetPoint(projector.createPoint(x4, p.getY()));
        System.out.println("x4=" + x4 + " point=" + point4);
    }

}
