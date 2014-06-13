package com.github.davidmoten.grumpy.wms.layer.shadow;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Utility class to help with time-based calculations in Astronomy
 * 
 * @author smr
 * 
 */
public final class Time {

    /**
     * Constructor to prevent inheritance
     */
    private Time() {
    }

    public static final double MILLISEC_PER_DAY = 86400000.0;
    public static final double BASE_JD = 2440587.5;

    /**
     * Calculate the Julian day number corresponding to the time provided
     * 
     * @param time
     *            , the time for which the Julian Day number is required. If
     *            null, the current time will be used.
     * 
     * @return - the Julian day number corresponding to the supplied time
     */
    public static double getJulianDayNumber(Calendar time) {

        if (time == null) {
            time = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        }

        double jd = BASE_JD + time.getTimeInMillis() / MILLISEC_PER_DAY;

        return jd;
    }

    public static void main(String[] args) {

        double jd = Time.getJulianDayNumber(null);
        System.out.println("Julian day number now is: " + jd);
    }

}