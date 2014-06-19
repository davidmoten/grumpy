package com.github.davidmoten.grumpy.wms.reduction;

import static com.github.davidmoten.grumpy.wms.reduction.RectangleUtil.quarter;
import static com.github.davidmoten.grumpy.wms.reduction.RectangleUtil.splitHorizontally;
import static com.github.davidmoten.grumpy.wms.reduction.RectangleUtil.splitVertically;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.wms.WmsUtil;
import com.google.common.base.Function;

public class Reducer {

    public static <T> void render(Graphics2D g, Function<Position, T> function,
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
            // region is indivisible, so choose any corner for the
            // representative value
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

        T firstT = null;

        List<Point> points = sampler.sample(region, projector);

        for (Point point : points) {
            Position p = projector.toPosition(point.x, point.y);
            T t = function.apply(p);
            if (firstT == null) {
                firstT = t;
            } else if (!firstT.equals(t)) {
                return null;
            }
        }
        return firstT;
    }
}
