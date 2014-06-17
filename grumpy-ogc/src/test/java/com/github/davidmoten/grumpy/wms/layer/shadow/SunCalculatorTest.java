package com.github.davidmoten.grumpy.wms.layer.shadow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Assert;

import com.github.davidmoten.grumpy.wms.layer.shadow.SunCalculator.SunCalculatorResult;

public class SunCalculatorTest {

    // @Test
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
            Assert.assertEquals(c.getTime().getTime(),
                    SunCalculator.convertJulianDateToCalendar(2454908.61756).getTime().getTime(),
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
                System.out.println(i + "\t" + a.getHourAngleDegrees() + "\t"
                        + a.getRise().getTime() + "|" + a.getTransit().getTime() + "|"
                        + calendar.getTime() + "|" + a.getSet().getTime());
            }
        }

        // not working till have sorted out the set and rise for canberra (seem
        // to be out by 12 hours)

        // TODO wonder what this is about? leave commented out
        Assert.fail();

    }

}
