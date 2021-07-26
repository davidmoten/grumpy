package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Rectangle;

import org.junit.Test;

public class BoundsSamplerMaxSizeTest {

    @Test
    public void testSample() {
        RectangleSampler b = new RectangleSamplerGrid();
        Rectangle r = new Rectangle(0, 0, 200, 300);
        System.out.println(b.sample(r, null));
    }

}
