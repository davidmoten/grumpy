package com.github.davidmoten.grumpy.wms.layer.darkness;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.util.Bounds;

/**
 * @author Steven Ring
 *
 */
public final class Sun {

    private static Logger log = LoggerFactory.getLogger(Sun.class);
    private static final double EARTH_RADIUS_KM = 6378.0;

    /**
     * Constructor to prevent inheritance
     */
    private Sun() {
    }

    public static enum Twilight {
        NIGHT, ASTRONOMICAL, NAUTICAL, CIVIL, DAYLIGHT
    }

    /**
     * Return the twilight condition for a point which is a given great circle
     * distance from the current sub solar point.
     * 
     * 
     * @param sunDistanceRadians
     *            - from the current positon to the sub solar point
     * @return The twilight condition
     */
    public static Twilight getTwilight(double sunDistanceRadians) {

        double altDegrees = 90.0 - Math.toDegrees(sunDistanceRadians);
        if (altDegrees >= 0.0) {
            return Twilight.DAYLIGHT;
        } else if (altDegrees >= -6.0) {
            return Twilight.CIVIL;
        } else if (altDegrees >= -12.0) {
            return Twilight.NAUTICAL;
        } else if (altDegrees >= -18.0) {
            return Twilight.ASTRONOMICAL;
        }

        return Twilight.NIGHT;
    }

    public static Twilight getTwilight(Position subSolarPoint, Position somePosition) {
        double distKm = somePosition.getDistanceToKm(subSolarPoint);
        double distRads = distKm / EARTH_RADIUS_KM;
        return getTwilight(distRads);
    }

    /**
     * Compute the twilight condition which prevails across the entire specified
     * region
     * 
     * @param region
     *            of interest
     * @param subSolarPoint
     *            -- point on the earth's surface where the sun is on the zenith
     * @return the regional twilight or null if different twilights prevail
     * 
     */
    public static Twilight getRegionUniformTwilightValue(Bounds region, Position subSolarPoint) {

        Twilight regionTwilight = null;

        Position[] corners = new Position[8];

        double midLat = (region.getMin().lat() + region.getMax().lat()) / 2;
        double midLon = (region.getMin().lon() + region.getMax().lon()) / 2;
        corners[0] = new Position(region.getMin().lat(), region.getMin().lon());
        corners[1] = new Position(region.getMax().lat(), region.getMin().lon());
        corners[2] = new Position(region.getMax().lat(), region.getMax().lon());
        corners[3] = new Position(region.getMin().lat(), region.getMax().lon());
        corners[4] = new Position(midLat, region.getMax().lon());
        corners[5] = new Position(midLat, region.getMin().lon());
        corners[6] = new Position(region.getMin().lat(), midLon);
        corners[7] = new Position(region.getMax().lat(), midLon);

        for (Position corner : corners) {
            double distKm = corner.getDistanceToKm(subSolarPoint);
            double distRads = distKm / EARTH_RADIUS_KM;
            Twilight tl = getTwilight(distRads);
            if (regionTwilight == null) {
                regionTwilight = tl;
            } else if (regionTwilight != tl) {
                return null;
            }
        }
        return regionTwilight;
    }

    public static Position getPosition() {
        return getPosition(null);
    }

    public static Position getPosition(Calendar time) {

        // convert time to Julian Day Number

        double jd = Time.getJulianDayNumber(time);
        // Julian centuries since Jan 1, 2000, 12:00 UTC

        double T = (jd - 2451545.0) / 36525;

        // mean anomaly, degree
        double M = 357.52910 + 35999.05030 * T - 0.0001559 * T * T - 0.00000048 * T * T * T;

        // mean longitude, degree
        double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T * T;

        double DL = (1.914600 - 0.004817 * T - 0.000014 * T * T) * Math.sin(Math.toRadians(M))
                + (0.019993 - 0.000101 * T) * Math.sin(Math.toRadians(2.0 * M)) + 0.000290
                * Math.sin(Math.toRadians(3.0 * M));

        // true longitude, degree
        double L = L0 + DL;

        // obliquity eps of ecliptic in degrees:
        double eps = 23.0 + 26.0 / 60.0 + 21.448 / 3600.0
                - (46.8150 * T + 0.00059 * T * T - 0.001813 * T * T * T) / 3600.0;

        double X = Math.cos(Math.toRadians(L));
        double Y = Math.cos(Math.toRadians(eps)) * Math.sin(Math.toRadians(L));
        double Z = Math.sin(Math.toRadians(eps)) * Math.sin(Math.toRadians(L));
        double R = Math.sqrt(1.0 - Z * Z);

        double delta = Math.toDegrees(Math.atan(Z / R)); // in degrees
        double p = Y / (X + R);
        double ra = Math.toDegrees(Math.atan(p));
        double RA = (24.0 / 180.0) * ra; // in hours

        // sidereal time (in hours)

        double theta0 = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * T * T - T
                * T * T / 38710000.0;
        double sidTime = (theta0 % 360) / 15.0;

        // lon and lat of sun

        double sunHADeg = ((sidTime - RA) * 15.0) % 360.0;
        double lon = 0.0;
        if (sunHADeg < 180.0) {
            lon = -sunHADeg;
        } else {
            lon = 360.0 - sunHADeg;
        }
        double lat = delta;

        log.info("Sidereal time is " + sidTime + ", Sun RA/Dec is " + RA + "/" + delta
                + ", subSolar lat/long is " + lat + "/" + lon);

        return new Position(lat, lon);

    }

}