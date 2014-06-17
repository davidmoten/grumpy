package com.github.davidmoten.grumpy.wms.layer.shadow;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * From http://en.wikipedia.org/wiki/Sunrise_equation and
 * http://users.electromagnetic.net/bu/astro/iyf-calc.php
 * 
 * @author dxm
 * 
 */
public class SunCalculator {

    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long JULIAN_1970 = 2440588;

    public static SunCalculatorResult calculate(GregorianCalendar calendar, double latitudeDegrees,
            double longitudeDegrees) {
        // convert gregorian to julian date
        calendar.setGregorianChange(new Date(Long.MAX_VALUE));
        // note sign change of longitudeDegrees because the value is
        // longitude east
        double jDate = calendar.getTimeInMillis() * 1.0 / MILLIS_PER_DAY + JULIAN_1970;
        double nStar = (jDate - 2451545 - 0.0009 + longitudeDegrees / 360);
        long n = Math.round(nStar);

        double jStar = 2451545 + 0.0009 - longitudeDegrees / 360 + n;
        double m = (357.5291 + 0.98560028 * (jStar - 2451545)) % 360;
        double mRadians = Math.toRadians(m);
        double c = 1.9148 * Math.sin(mRadians) + 0.02 * Math.sin(mRadians * 2) + 0.0003
                * Math.sin(3 * mRadians);
        double lambda = (m + 102.9372 + c + 180) % 360;
        double lambdaRadians = Math.toRadians(lambda);
        double jTransit = jStar + 0.0053 * Math.sin(mRadians) - 0.0069 * Math.sin(lambdaRadians);
        double declinationRadians = Math.asin(Math.sin(lambdaRadians)
                * Math.sin(Math.toRadians(23.45)));
        double latitudeRadians = Math.toRadians(latitudeDegrees);
        double numerator = Math.sin(Math.toRadians(-0.83)) - Math.sin(latitudeRadians)
                * Math.sin(declinationRadians);
        double denominator = Math.cos(latitudeRadians) * Math.cos(declinationRadians);
        double hourAngleRadians = Math.acos(numerator / denominator);
        double hourAngleDegrees = Math.toDegrees(hourAngleRadians);
        double declinationDegrees = Math.toDegrees(declinationRadians);
        double jSet = 2451545 + 0.0009 + (hourAngleDegrees - longitudeDegrees) / 360 + n + 0.0053
                * Math.sin(mRadians) - 0.0069 * Math.sin(2 * lambdaRadians);
        double jRise = jTransit - (jSet - jTransit);

        SunCalculatorResult result = new SunCalculatorResult();
        result.setjDate(jDate);
        result.setjStar(jStar);
        result.setnStar(nStar);
        result.setN(n);
        result.setM(m);
        result.c = c;
        result.setLambda(lambda);
        result.setjTransit(jTransit);
        result.setHourAngleDegrees(hourAngleDegrees);
        result.setDeclinationDegrees(declinationDegrees);
        result.setjSet(jSet);
        result.setjRise(jRise);
        return result;
    }

    public static class SunCalculatorResult {
        private double jRise;
        private double jSet;
        private double jStar;
        private double jDate;
        private double nStar;
        private long n;
        private double m;
        double c;
        private double lambda;
        private double jTransit;
        private double hourAngleDegrees;
        private double declinationDegrees;

        public final Calendar getRise() {
            return convertJulianDateToCalendar(getjRise());
        }

        public final Calendar getSet() {
            return convertJulianDateToCalendar(getjSet());
        }

        public final Calendar getTransit() {
            return convertJulianDateToCalendar(getjTransit());
        }

        @Override
        public final String toString() {
            return "hourAngleDegrees=" + getHourAngleDegrees() + ", declinationDegrees="
                    + getDeclinationDegrees();
        }

        public final void setjRise(double jRise) {
            this.jRise = jRise;
        }

        public final double getjRise() {
            return jRise;
        }

        public final void setjSet(double jSet) {
            this.jSet = jSet;
        }

        public final double getjSet() {
            return jSet;
        }

        public final void setjStar(double jStar) {
            this.jStar = jStar;
        }

        public final double getjStar() {
            return jStar;
        }

        public final void setjDate(double jDate) {
            this.jDate = jDate;
        }

        public final double getjDate() {
            return jDate;
        }

        public final void setnStar(double nStar) {
            this.nStar = nStar;
        }

        public final double getnStar() {
            return nStar;
        }

        public final void setN(long n) {
            this.n = n;
        }

        public final long getN() {
            return n;
        }

        public final void setM(double m) {
            this.m = m;
        }

        public final double getM() {
            return m;
        }

        public final void setLambda(double lambda) {
            this.lambda = lambda;
        }

        public final double getLambda() {
            return lambda;
        }

        public final void setjTransit(double jTransit) {
            this.jTransit = jTransit;
        }

        public final double getjTransit() {
            return jTransit;
        }

        public final void setHourAngleDegrees(double hourAngleDegrees) {
            this.hourAngleDegrees = hourAngleDegrees;
        }

        public final double getHourAngleDegrees() {
            return hourAngleDegrees;
        }

        public final void setDeclinationDegrees(double declinationDegrees) {
            this.declinationDegrees = declinationDegrees;
        }

        public final double getDeclinationDegrees() {
            return declinationDegrees;
        }

    }

    static Calendar convertJulianDateToCalendar(double julianDateDays) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setGregorianChange(new Date(Long.MAX_VALUE));
        calendar.setTimeInMillis(Math.round((julianDateDays - JULIAN_1970) * MILLIS_PER_DAY));
        return calendar;
    }

}