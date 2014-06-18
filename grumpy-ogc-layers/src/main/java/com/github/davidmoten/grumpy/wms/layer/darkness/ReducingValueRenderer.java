package com.github.davidmoten.grumpy.wms.layer.darkness;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.util.Bounds;
import com.github.davidmoten.grumpy.util.LatLon;
import com.github.davidmoten.grumpy.wms.layer.darkness.SunUtil.Twilight;
import com.google.common.base.Function;

public class ReducingValueRenderer {

	public static <T> void renderRegion(Graphics2D g,
			Function<Position, T> function, Projector projector,
			Bounds geoBounds, Rectangle xyBounds,
			ValueRenderer<T> regionRenderer) {

		// check if we need to divide the region
		boolean regionDivisible = xyBounds.height > 1 || xyBounds.width > 1;

		final T regionUniformValue;
		if (!regionDivisible) {
			// region is indivisible, so choose any corner for the twilight
			// value
			regionUniformValue = function.apply(new Position(geoBounds.getMin()
					.lat(), geoBounds.getMin().lon()));
		} else {
			// get the function value for the region if common to all sample
			// points in the region (if no common value returns null)
			regionUniformValue = getUniformSampledValue(geoBounds, function);
		}

		if (regionUniformValue != null) {
			// render the region
			regionRenderer.render(g, projector, geoBounds,
					regionUniformValue);
		} else {
			// region is a mix of values and is divisible
			// so divide into sub regions ... 2 or 4
			// but only if we can

			splitRegionAndRender(g, function, projector, xyBounds,
					regionRenderer);
		}
	}

	private static <T> void splitRegionAndRender(Graphics2D g,
			Function<Position, T> function, Projector projector,
			Rectangle xyBounds, ValueRenderer<T> regionRenderer) {
		// split region
		final Rectangle[] rectangles = splitRectangles(xyBounds);

		// now render each region
		for (Rectangle rect : rectangles) {
			Position min = projector.toPosition(rect.x, rect.y + rect.height);
			Position max = projector.toPosition(rect.x + rect.width, rect.y);
			Bounds bounds = new Bounds(new LatLon(min.getLat(), min.getLon()),
					new LatLon(max.getLat(), max.getLon()));
			renderRegion(g, function, projector, bounds, rect, regionRenderer);
		}
	}

	private static Rectangle[] splitRectangles(Rectangle xyBounds) {
		final Rectangle[] rectangles;

		if (xyBounds.width > 1 && xyBounds.height > 1) {

			// divide into 4 sub regions

			rectangles = new Rectangle[4];
			int halfWidth = xyBounds.width / 2;
			int halfHeight = xyBounds.height / 2;
			rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, halfWidth,
					halfHeight);
			rectangles[1] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y,
					xyBounds.width - halfWidth, halfHeight);
			rectangles[2] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y
					+ halfHeight, xyBounds.width - halfWidth, xyBounds.height
					- halfHeight);
			rectangles[3] = new Rectangle(xyBounds.x, xyBounds.y + halfHeight,
					halfWidth, xyBounds.height - halfHeight);

		} else if (xyBounds.height > 1) {

			// divide into two vertically

			rectangles = new Rectangle[2];

			int halfHeight = xyBounds.height / 2;
			rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, 1, halfHeight);
			rectangles[1] = new Rectangle(xyBounds.x, xyBounds.y + halfHeight,
					1, xyBounds.height - halfHeight);

		} else {

			// divide into two horizontally

			rectangles = new Rectangle[2];
			int halfWidth = xyBounds.width / 2;
			rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, halfWidth, 1);
			rectangles[1] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y,
					xyBounds.width - halfWidth, 1);
		}
		return rectangles;
	}

	/**
	 * Computes the twilight condition which prevails across the entire
	 * specified region by testing sample points. If one of the sample points
	 * differs in twilight value from the others then null is returned otherwise
	 * the common {@link Twilight} value is returned.
	 * 
	 * @param region
	 *            of interest
	 * @param subSolarPoint
	 *            -- point on the earth's surface where the sun is on the zenith
	 * @return the regional twilight or null if different twilights prevail
	 * 
	 */
	public static <T> T getUniformSampledValue(Bounds region,
			Function<Position, T> function) {

		T regionT = null;

		Position[] positions = new Position[4];

		positions[0] = new Position(region.getMin().lat(), region.getMin()
				.lon());
		positions[1] = new Position(region.getMax().lat(), region.getMin()
				.lon());
		positions[2] = new Position(region.getMax().lat(), region.getMax()
				.lon());
		positions[3] = new Position(region.getMin().lat(), region.getMax()
				.lon());

		for (Position p : positions) {
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
