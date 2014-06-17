package com.github.davidmoten.grumpy.wms.layer.darkness;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to help with time-based calculations in Astronomy
 * 
 * @author Steven Ring
 * 
 */
public final class TimeUtil {

	private static final double MILLISEC_PER_DAY = TimeUnit.DAYS.toMillis(1);
	public static final double BASE_JD = 2440587.5;

	/**
	 * Constructor to prevent inheritance
	 */
	private TimeUtil() {
	}

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

}