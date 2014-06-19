package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.wms.WmsUtil;
import com.google.common.base.Function;

public class ReducingValueRenderer {

    public static <T> void renderRegion(Graphics2D g, Function<Position, T> function,
            Projector projector, RectangleSampler sampler, ValueRenderer<T> regionRenderer) {
        Rectangle region = WmsUtil.toTargetRectangle(projector);
        renderRegion(g, function, projector, region, sampler, regionRenderer);
    }

    private static <T> void renderRegion(Graphics2D g, Function<Position, T> function,
            Projector projector, Rectangle region, RectangleSampler sampler,
            ValueRenderer<T> regionRenderer) {

        // check if we need to divide the region
        boolean regionDivisible = region.height > 1 || region.width > 1;

        final T regionUniformValue;
        if (!regionDivisible) {
            // region is indivisible, so choose any corner for the twilight
            // value
            regionUniformValue = function.apply(projector.toPosition(region.getMinX(),
                    region.getMinY()));
        } else {
            // get the function value for the region if common to all sample
            // points in the region (if no common value returns null)
            regionUniformValue = getUniformSampledValue(projector, region, sampler, function);
        }

        if (regionUniformValue != null) {
            // render the region
            regionRenderer.render(g, projector, region, regionUniformValue);
        } else {
            // region is a mix of values and is divisible
            // so divide into sub regions ... 2 or 4
            // but only if we can

            splitRegionAndRender(g, function, projector, region, sampler, regionRenderer);
        }
    }

    private static <T> void splitRegionAndRender(Graphics2D g, Function<Position, T> function,
            Projector projector, Rectangle xyBounds, RectangleSampler sampler,
            ValueRenderer<T> regionRenderer) {
        // split region
        final Rectangle[] rectangles = splitRectangles(xyBounds);

        // now render each region
        for (Rectangle rect : rectangles) {
            renderRegion(g, function, projector, rect, sampler, regionRenderer);
        }
    }

    private static Rectangle[] splitRectangles(Rectangle xyBounds) {
        final Rectangle[] rectangles;

        if (xyBounds.width > 1 && xyBounds.height > 1) {

            // divide into 4 sub regions

            rectangles = new Rectangle[4];
            int halfWidth = xyBounds.width / 2;
            int halfHeight = xyBounds.height / 2;
            rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, halfWidth, halfHeight);
            rectangles[1] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y, xyBounds.width
                    - halfWidth, halfHeight);
            rectangles[2] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y + halfHeight,
                    xyBounds.width - halfWidth, xyBounds.height - halfHeight);
            rectangles[3] = new Rectangle(xyBounds.x, xyBounds.y + halfHeight, halfWidth,
                    xyBounds.height - halfHeight);

        } else if (xyBounds.height > 1) {

            // divide into two vertically

            rectangles = new Rectangle[2];

            int halfHeight = xyBounds.height / 2;
            rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, 1, halfHeight);
            rectangles[1] = new Rectangle(xyBounds.x, xyBounds.y + halfHeight, 1, xyBounds.height
                    - halfHeight);

        } else {

            // divide into two horizontally

            rectangles = new Rectangle[2];
            int halfWidth = xyBounds.width / 2;
            rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, halfWidth, 1);
            rectangles[1] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y, xyBounds.width
                    - halfWidth, 1);
        }
        return rectangles;
    }

    private static <T> T getUniformSampledValue(Projector projector, Rectangle region,
            RectangleSampler sampler, Function<Position, T> function) {

        T regionT = null;

        List<Point> points = sampler.sample(region);

        for (Point point : points) {
            Position p = projector.toPosition(point.x, point.y);
            T t = function.apply(p);
            if (regionT == null) {
                regionT = t;
            } else if (!regionT.equals(t)) {
                return null;
            }
        }
        return regionT;
    }
}
