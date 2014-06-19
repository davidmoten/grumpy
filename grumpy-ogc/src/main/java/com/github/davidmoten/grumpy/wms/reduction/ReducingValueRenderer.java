package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
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
            Projector projector, Rectangle region, RectangleSampler sampler,
            ValueRenderer<T> regionRenderer) {
        // split region
        final List<Rectangle> regions = splitRegion(region);

        // now render each region
        for (Rectangle subRegion : regions) {
            renderRegion(g, function, projector, subRegion, sampler, regionRenderer);
        }
    }

    private static List<Rectangle> splitHorizontally(Rectangle region) {
        List<Rectangle> list = new ArrayList<Rectangle>();
        int halfWidth = region.width / 2;
        list.add(new Rectangle(region.x, region.y, halfWidth, region.height));
        list.add(new Rectangle(region.x + halfWidth, region.y, region.width - halfWidth,
                region.height));
        return list;
    }

    private static List<Rectangle> splitVertically(Rectangle region) {
        List<Rectangle> list = new ArrayList<Rectangle>();
        int halfHeight = region.height / 2;
        list.add(new Rectangle(region.x, region.y, region.width, halfHeight));
        list.add(new Rectangle(region.x, region.y + halfHeight, region.width, region.height
                - halfHeight));
        return list;
    }

    private static List<Rectangle> quarter(Rectangle region) {
        List<Rectangle> list = new ArrayList<Rectangle>();
        for (Rectangle r : splitHorizontally(region))
            list.addAll(splitVertically(r));
        return list;
    }

    private static List<Rectangle> splitRegion(Rectangle region) {
        if (region.width > 1 && region.height > 1)
            return quarter(region);
        else if (region.height > 1)
            return splitVertically(region);
        else
            return splitHorizontally(region);
    }

    private static <T> T getUniformSampledValue(Projector projector, Rectangle region,
            RectangleSampler sampler, Function<Position, T> function) {

        T regionT = null;

        List<Point> points = sampler.sample(region, projector);

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
