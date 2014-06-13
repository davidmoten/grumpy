package com.github.davidmoten.grumpy.core;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.round;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.awt.Polygon;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

/**
 * Can use commons-math3 FastMath for most trig functions exception for
 * atan,atan2 (see <a
 * href="https://issues.apache.org/jira/browse/MATH-740">here</a>).
 * 
 */
public class Position {
	private final double lat;
	private final double lon;
	private final double alt;
	private static final double radiusEarthKm = 6371.01;
	private static final double circumferenceEarthKm = 2.0 * PI * radiusEarthKm;

	/**
	 * @param lat
	 *            in degrees
	 * @param lon
	 *            in degrees
	 */
	public Position(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		this.alt = 0.0;
	}

	/**
	 * @param lat
	 *            in degrees
	 * @param lon
	 *            in degrees
	 * @param alt
	 *            in metres
	 */
	public Position(double lat, double lon, double alt) {
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
	}

	public static Position create(double lat, double lon) {
		return new Position(lat, lon);
	}

	public static Position position(double lat, double lon) {
		return create(lat, lon);
	}

	public static Position create(double lat, double lon, double alt) {
		return new Position(lat, lon, alt);
	}

	public final double getLat() {
		return lat;
	}

	public final double getLon() {
		return lon;
	}

	public final double getAlt() {
		return alt;
	}

	@Override
	public final String toString() {
		return "[" + lat + "," + lon + "]";
	}

	/**
	 * Predicts position travelling along a great circle arc based on the
	 * Haversine formula.
	 * 
	 * From http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param distanceKm
	 * @param courseDegrees
	 * @return
	 */
	public final Position predict(double distanceKm, double courseDegrees) {
		assertWithMsg(alt == 0.0, "Predictions only valid for Earth's surface");
		double dr = distanceKm / radiusEarthKm;
		double latR = toRadians(lat);
		double lonR = toRadians(lon);
		double courseR = toRadians(courseDegrees);
		double lat2Radians = asin(sin(latR) * cos(dr) + cos(latR) * sin(dr)
				* cos(courseR));
		double lon2Radians = atan2(sin(courseR) * sin(dr) * cos(latR), cos(dr)
				- sin(latR) * sin(lat2Radians));
		double lon3Radians = mod(lonR + lon2Radians + PI, 2 * PI) - PI;
		return new Position(FastMath.toDegrees(lat2Radians),
				FastMath.toDegrees(lon3Radians));
	}

	public static double toDegrees(double degrees, double minutes,
			double seconds) {
		return degrees + minutes / 60.0 + seconds / 3600.0;
	}

	/**
	 * From http://williams.best.vwh.net/avform.htm (Latitude of point on GC).
	 * 
	 * @param position
	 * @param longitudeDegrees
	 * @return
	 */
	public Double getLatitudeOnGreatCircle(Position position,
			double longitudeDegrees) {
		double lonR = toRadians(longitudeDegrees);
		double lat1R = toRadians(lat);
		double lon1R = toRadians(lon);
		double lat2R = toRadians(position.getLat());
		double lon2R = toRadians(position.getLon());

		double sinDiffLon1RLon2R = sin(lon1R - lon2R);
		if (abs(sinDiffLon1RLon2R) < 0.00000001) {
			return null;
		} else {
			double cosLat1R = cos(lat1R);
			double cosLat2R = cos(lat2R);
			double numerator = sin(lat1R) * cosLat2R * sin(lonR - lon2R)
					- sin(lat2R) * cosLat1R * sin(lonR - lon1R);
			double denominator = cosLat1R * cosLat2R * sinDiffLon1RLon2R;
			double radians = atan(numerator / denominator);
			return FastMath.toDegrees(radians);
		}
	}

	public static class LongitudePair {
		private final double lon1, lon2;

		public LongitudePair(double lon1, double lon2) {
			this.lon1 = lon1;
			this.lon2 = lon2;
		}

		public double getLon1() {
			return lon1;
		}

		public double getLon2() {
			return lon2;
		}

		@Override
		public String toString() {
			return "LongitudePair [lon1=" + lon1 + ", lon2=" + lon2 + "]";
		}

	}

	/**
	 * Returns null if no crossing of latitude otherwise return two longitude
	 * candidates. From http://williams.best.vwh.net/avform.htm (Crossing
	 * parallels).
	 * 
	 * @param position
	 * @param latitudeDegrees
	 * @return
	 */
	// TODO add unit test
	public LongitudePair getLongitudeOnGreatCircle(Position position,
			double latitudeDegrees) {
		double lat3 = toRadians(latitudeDegrees);
		double lat1 = toRadians(lat);
		double lon1 = toRadians(lon);
		double lat2 = toRadians(position.getLat());
		double lon2 = toRadians(position.getLon());
		double l12 = lon1 - lon2;
		double sinLat1 = sin(lat1);
		double cosLat2 = cos(lat2);
		double cosLat3 = cos(lat3);
		double cosLat1 = cos(lat1);
		double sinL12 = sin(l12);
		double A = sinLat1 * cosLat2 * cosLat3 * sinL12;
		double B = sinLat1 * cosLat2 * cosLat3 * cos(l12) - cosLat1 * sin(lat2)
				* cosLat3;
		double C = cosLat1 * cosLat2 * sin(lat3) * sinL12;
		double longitude = atan2(B, A);
		double v = sqrt(sqr(A) + sqr(B));
		if (abs(C) >= v) {
			// not found!
			return null;
		} else {
			double dlon = acos(C / v);
			double lonCandidate1Degrees = to180(FastMath.toDegrees(lon1 + dlon
					+ longitude));
			double lonCandidate2Degrees = to180(FastMath.toDegrees(lon1 - dlon
					+ longitude));
			return new LongitudePair(lonCandidate1Degrees, lonCandidate2Degrees);
		}
	}

	private double sqr(double d) {
		return d * d;
	}

	/**
	 * Return an array of Positions representing the earths limb (aka: horizon)
	 * as viewed from this Position in space. This position must have altitude >
	 * 0
	 * 
	 * The array returned will have the specified number of elements (radials).
	 * 
	 * 
	 * This method is useful for the calculation of satellite footprints or the
	 * position of the Earth's day/night terminator.
	 * 
	 * 
	 * This formula from Aviation Formula by Ed Williams
	 * (http://williams.best.vwh.net/avform.htm)
	 * 
	 * @param radials
	 *            the number of radials to calculated (evenly spaced around the
	 *            circumference of the circle
	 * 
	 * @return An array of radial points a fixed distance from this point
	 *         representing the Earth's limb as viewed from this point in space.
	 * 
	 */
	public final Position[] getEarthLimb(int radials) {

		Position[] result = new Position[radials];

		double radialDegrees = 0.0;
		double incDegrees = 360.0 / radials;
		double quarterEarthKm = circumferenceEarthKm / 4.0;
		Position surfacePosition = new Position(this.lat, this.lon, 0.0);

		// Assert( this.alt>0.0, "getEarthLimb() requires Position a positive
		// altitude");
		for (int i = 0; i < radials; i++) {

			// TODO: base the distance on the altitude above the Earth

			result[i] = surfacePosition.predict(quarterEarthKm, radialDegrees);
			radialDegrees += incDegrees;
		}

		return result;
	}

	/**
	 * returns distance between two WGS84 positions according to Vincenty's
	 * formula from Wikipedia
	 * 
	 * @param position
	 * @return
	 */
	public final double getDistanceToKm(Position position) {
		double lat1 = toRadians(lat);
		double lat2 = toRadians(position.lat);
		double lon1 = toRadians(lon);
		double lon2 = toRadians(position.lon);
		double deltaLon = lon2 - lon1;
		double cosLat2 = cos(lat2);
		double cosLat1 = cos(lat1);
		double sinLat1 = sin(lat1);
		double sinLat2 = sin(lat2);
		double cosDeltaLon = cos(deltaLon);
		double top = sqrt(sqr(cosLat2 * sin(deltaLon))
				+ sqr(cosLat1 * sinLat2 - sinLat1 * cosLat2 * cosDeltaLon));
		double bottom = sinLat1 * sinLat2 + cosLat1 * cosLat2 * cosDeltaLon;
		double distance = radiusEarthKm * atan2(top, bottom);
		return abs(distance);
	}

	/**
	 * Returns a great circle bearing in degrees in the range 0 to 360.
	 * 
	 * @param position
	 * @return
	 */
	public final double getBearingDegrees(Position position) {
		double lat1 = toRadians(lat);
		double lat2 = toRadians(position.lat);
		double lon1 = toRadians(lon);
		double lon2 = toRadians(position.lon);
		double dLon = lon2 - lon1;
		double sinDLon = sin(dLon);
		double cosLat2 = cos(lat2);
		double y = sinDLon * cosLat2;
		double x = cos(lat1) * sin(lat2) - sin(lat1) * cosLat2 * cos(dLon);
		double course = FastMath.toDegrees(atan2(y, x));
		if (course < 0)
			course += 360;
		return course;
	}

	/**
	 * returns difference in degrees in the range -180 to 180
	 * 
	 * @param bearing1
	 *            degrees between -360 and 360
	 * @param bearing2
	 *            degrees between -360 and 360
	 * @return
	 */
	public static double getBearingDifferenceDegrees(double bearing1,
			double bearing2) {
		if (bearing1 < 0)
			bearing1 += 360;
		if (bearing2 > 180)
			bearing2 -= 360;
		double result = bearing1 - bearing2;
		if (result > 180)
			result -= 360;
		return result;
	}

	/**
	 * calculates the distance of a point to the great circle path between p1
	 * and p2.
	 * 
	 * Formula from: http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public final double getDistanceKmToPath(Position p1, Position p2) {
		double d = radiusEarthKm
				* asin(sin(getDistanceToKm(p1) / radiusEarthKm)
						* sin(toRadians(getBearingDegrees(p1)
								- p1.getBearingDegrees(p2))));
		return abs(d);
	}

	public static String toDegreesMinutesDecimalMinutesLatitude(double lat) {
		long degrees = round(signum(lat) * floor(abs(lat)));
		double remaining = abs(lat - degrees);
		remaining *= 60;
		String result = abs(degrees) + "" + (char) 0x00B0
				+ new DecimalFormat("00.00").format(remaining) + "'"
				+ (lat < 0 ? "S" : "N");
		return result;
	}

	public static String toDegreesMinutesDecimalMinutesLongitude(double lon) {
		long degrees = round(signum(lon) * floor(abs(lon)));
		double remaining = abs(lon - degrees);
		remaining *= 60;
		String result = abs(degrees) + "" + (char) 0x00B0
				+ new DecimalFormat("00.00").format(remaining) + "'"
				+ (lon < 0 ? "W" : "E");
		return result;
	}

	private static double mod(double y, double x) {

		x = abs(x);
		int n = (int) (y / x);
		double mod = y - x * n;
		if (mod < 0) {
			mod += x;
		}
		return mod;
	}

	public static void assertWithMsg(boolean assertion, String msg) {
		if (!assertion)
			throw new RuntimeException("Assertion failed: " + msg);

	}

	/**
	 * Returns a position along a path according to the proportion value
	 * 
	 * @param position
	 * @param proportion
	 *            is between 0 and 1 inclusive
	 * @return
	 */

	public final Position getPositionAlongPath(Position position,
			double proportion) {

		if (proportion >= 0 && proportion <= 1) {

			// Get bearing degrees for course
			double courseDegrees = this.getBearingDegrees(position);

			// Get distance from position arg and this objects location
			double distanceKm = this.getDistanceToKm(position);

			// Predict the position for a proportion of the course
			// where this object is the start position and the arg
			// is the destination position.
			Position retPosition = this.predict(proportion * distanceKm,
					courseDegrees);

			return retPosition;
		} else
			throw new RuntimeException(
					"Proportion must be between 0 and 1 inclusive");
	}

	public final List<Position> getPositionsAlongPath(Position position,
			double maxSegmentLengthKm) {

		// Get distance from this to position
		double distanceKm = this.getDistanceToKm(position);

		List<Position> positions = new ArrayList<Position>();

		long numSegments = round(floor(distanceKm / maxSegmentLengthKm)) + 1;
		positions.add(this);
		for (int i = 1; i < numSegments; i++)
			positions.add(getPositionAlongPath(position, i
					/ (double) numSegments));
		positions.add(position);
		return positions;
	}

	public final Position to360() {
		double lat = this.lat;
		double lon = this.lon;
		if (lon < 0)
			lon += 360;
		return new Position(lat, lon);
	}

	/**
	 * normalize the lat lon values of this to ensure that no large longitude
	 * jumps are made from lastPosition (e.g. 179 to -180)
	 * 
	 * @param lastPosition
	 */
	public final Position ensureContinuous(Position lastPosition) {
		double lon = this.lon;
		if (abs(lon - lastPosition.lon) > 180) {
			if (lastPosition.lon < 0)
				lon -= 360;
			else
				lon += 360;
			return new Position(lat, lon);
		} else
			return this;

	}

	public final boolean isWithin(List<Position> positions) {
		Polygon polygon = new Polygon();
		for (Position p : positions) {
			polygon.addPoint(degreesToArbitraryInteger(p.lon),
					degreesToArbitraryInteger(p.lat));
		}
		int x = degreesToArbitraryInteger(this.lon);
		int y = degreesToArbitraryInteger(this.lat);
		return polygon.contains(x, y);
	}

	private int degreesToArbitraryInteger(double d) {
		return (int) round(d * 3600);
	}

	@Override
	public final boolean equals(Object o) {
		if (o == null)
			return false;
		else if (o instanceof Position) {
			Position p = (Position) o;
			return p.lat == lat && p.lon == lon;
		} else
			return false;
	}

	@Override
	public final int hashCode() {
		return (int) (lat + lon);
	}

	public final double getDistanceToPathKm(List<Position> positions) {
		if (positions.size() == 0)
			throw new RuntimeException("positions must not be empty");
		else if (positions.size() == 1)
			return this.getDistanceToKm(positions.get(0));
		else {
			Double distance = null;
			for (int i = 0; i < positions.size() - 1; i++) {
				double d = getDistanceToSegmentKm(positions.get(i),
						positions.get(i + 1));
				if (distance == null || d < distance)
					distance = d;
			}
			return distance;
		}
	}

	public final double getDistanceToSegmentKm(Position p1, Position p2) {
		return getDistanceToKm(getClosestIntersectionWithSegment(p1, p2));
	}

	public final Position getClosestIntersectionWithSegment(Position p1,
			Position p2) {
		if (p1.equals(p2))
			return p1;
		double d = getDistanceToKm(p1);
		double bearing1 = p1.getBearingDegrees(this);
		double bearing2 = p1.getBearingDegrees(p2);
		double bearingDiff = bearing1 - bearing2;
		double proportion = d * cos(toRadians(bearingDiff))
				/ p1.getDistanceToKm(p2);
		if (proportion < 0 || proportion > 1) {
			if (d < getDistanceToKm(p2))
				return p1;
			else
				return p2;
		} else
			return p1.getPositionAlongPath(p2, proportion);
	}

	/**
	 * @param path
	 * @param minDistanceKm
	 * @return
	 */
	public boolean isOutside(List<Position> path, double minDistanceKm) {
		if (isWithin(path))
			return false;
		else {
			double distance = getDistanceToPathKm(path);
			return distance >= minDistanceKm;
		}
	}

	/**
	 * Returns the difference between two longitude values. The returned value
	 * is always >=0.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double longitudeDiff(double a, double b) {
		a = to180(a);
		b = to180(b);
		if (a < b)
			return a - b + 360;
		else
			return a - b;
	}

	/**
	 * Converts an angle in degrees to range -180< x <= 180.
	 * 
	 * @param d
	 * @return
	 */
	public static double to180(double d) {
		if (d < 0)
			return -to180(abs(d));
		else {
			if (d > 180) {
				long n = round(floor((d + 180) / 360.0));
				return d - n * 360;
			} else
				return d;
		}
	}

	public Position normalizeLongitude() {
		return new Position(lat, to180(lon), alt);
	}
}
