package com.github.davidmoten.grumpy.wms;

import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.grumpy.projection.ProjectorTarget;
import org.junit.Test;
import org.locationtech.jts.geom.Point;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D.Double;
import java.util.List;

import static com.github.davidmoten.grumpy.core.Position.position;

public class RendererUtilTest {

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

    @Test
    public void testGetCircle() {
        ProjectorTarget target = new ProjectorTarget(300, 200);
        // 14288114.828624,-6061227.593083,18907357.32131,-2348222.5076192
        ProjectorBounds bounds = new ProjectorBounds("EPSG:3857", 14288114.828624, -6061227.593083, 18907357.32131,
                -2348222.5076192);
        Projector projector = new Projector(bounds, target);
        List<GeneralPath> paths = RendererUtil.toPath(projector,
                RendererUtil.getCircle(position(-35.3075, 149.1244), 400, 36));
        for (GeneralPath path : paths) {
            System.out.println("Path");
            PathIterator it = path.getPathIterator(null);
            double[] values = new double[6];
            while (!it.isDone()) {
                int code = it.currentSegment(values);
                if (code == PathIterator.SEG_MOVETO) {
                    System.out.println("moveto " + values[0] + "," + values[1]);
                } else if (code == PathIterator.SEG_LINETO) {
                    System.out.println("lineto " + values[0] + "," + values[1]);
                } else {
                    System.out.println("code=" + code);
                }
                it.next();
            }
        }
    }
}
