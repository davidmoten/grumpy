package com.github.davidmoten.grumpy.wms.layer.shadow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

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
        double c = 1.9148 * Math.sin(mRadians) + 0.02 * Math.sin(mRadians * 2) + 0.0003 * Math.sin(3 * mRadians);
        double lambda = (m + 102.9372 + c + 180) % 360;
        double lambdaRadians = Math.toRadians(lambda);
        double jTransit = jStar + 0.0053 * Math.sin(mRadians) - 0.0069 * Math.sin(lambdaRadians);
        double declinationRadians = Math.asin(Math.sin(lambdaRadians) * Math.sin(Math.toRadians(23.45)));
        double latitudeRadians = Math.toRadians(latitudeDegrees);
        double numerator = Math.sin(Math.toRadians(-0.83)) - Math.sin(latitudeRadians) * Math.sin(declinationRadians);
        double denominator = Math.cos(latitudeRadians) * Math.cos(declinationRadians);
        double hourAngleRadians = Math.acos(numerator / denominator);
        double hourAngleDegrees = Math.toDegrees(hourAngleRadians);
        double declinationDegrees = Math.toDegrees(declinationRadians);
        double jSet = 2451545 + 0.0009 + (hourAngleDegrees - longitudeDegrees) / 360 + n + 0.0053 * Math.sin(mRadians)
                - 0.0069 * Math.sin(2 * lambdaRadians);
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
        private double c;
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
            return "hourAngleDegrees=" + getHourAngleDegrees() + ", declinationDegrees=" + getDeclinationDegrees();
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

    private static Calendar convertJulianDateToCalendar(double julianDateDays) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setGregorianChange(new Date(Long.MAX_VALUE));
        calendar.setTimeInMillis(Math.round((julianDateDays - JULIAN_1970) * MILLIS_PER_DAY));
        return calendar;
    }

    @Test
    public final void test() {

        // using http://users.electromagnetic.net/bu/astro/iyf-calc.php

        // julian date 2454909
        if (true) {
            GregorianCalendar calendar = new GregorianCalendar(2009, 2, 18, 14, 0);
            SunCalculatorResult r = SunCalculator.calculate(calendar, 39.040759, -77.04876);
            Assert.assertEquals(3364, r.getN());
            Assert.assertEquals(2454909.2149243, r.getjStar(), 0.0001);
            Assert.assertEquals(73.300271402933, r.getM(), 0.0001);
            Assert.assertEquals(1.8448581379758, r.c, 0.0001);
            Assert.assertEquals(358.08232954091, r.getLambda(), 0.0001);
            Assert.assertEquals(2454909.2204623, r.getjTransit(), 0.01);
            Assert.assertEquals(-0.76082424216907, r.getDeclinationDegrees(), 0.01);
            Assert.assertEquals(90.451651571029, r.getHourAngleDegrees(), 0.01);
            Assert.assertEquals(2454909.4717157, r.getjSet(), 0.01);
            Assert.assertEquals(2454908.9692066, r.getjRise(), 0.01);

            Calendar c = new GregorianCalendar(2009, 2, 18, 1, 49, 17);
            // assert equal within one second
            Assert.assertEquals(c.getTime().getTime(), convertJulianDateToCalendar(2454908.61756).getTime().getTime(),
                    1000);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSSZ");
            sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

            System.out.println(sdf.format(r.getRise().getTime()));
            System.out.println(sdf.format(calendar.getTime()));
            System.out.println(sdf.format(r.getSet().getTime()));
        }
        if (true) {

            GregorianCalendar calendar = new GregorianCalendar(2009, 2, 18, 14, 0);

            SunCalculatorResult r = SunCalculator.calculate(calendar, -35.283, 149.1333);

            SimpleDateFormat sdf = new SimpleDateFormat();

            System.out.println("rise=" + sdf.format(r.getRise().getTime()));
            System.out.println(" time=" + sdf.format(calendar.getTime()));
            System.out.println(" set=" + sdf.format(r.getSet().getTime()));
            System.out.println(" transit=" + sdf.format(r.getTransit().getTime()));

            for (int i = -180; i < 180; i += 10) {
                SunCalculatorResult a = SunCalculator.calculate(calendar, -35, i);
                System.out.println(i + "\t" + a.getHourAngleDegrees() + "\t" + a.getRise().getTime() + "|"
                        + a.getTransit().getTime() + "|" + calendar.getTime() + "|" + a.getSet().getTime());
            }
        }

        // not working till have sorted out the set and rise for canberra (seem
        // to be out by 12 hours)
        Assert.fail();

    }
}