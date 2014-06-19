package com.github.davidmoten.grumpy.wms.reduction;

import org.junit.Test;

import com.github.davidmoten.grumpy.util.Bounds;
import com.github.davidmoten.grumpy.util.LatLon;

public class BoundsSamplerMaxSizeTest {

    @Test
    public void testSample() {
        BoundsSampler b = new BoundsSamplerMaxSize(100);
        Bounds bounds = new Bounds(new LatLon(-30, 149), new LatLon(-30, 154));
        System.out.println(b.sample(bounds));
    }

}
