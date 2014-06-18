package com.github.davidmoten.grumpy.wms.layer.darkness;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.util.Bounds;
import com.github.davidmoten.grumpy.wms.layer.darkness.SunUtil.Twilight;
import com.google.common.base.Function;

public class SamplingUtil {

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
