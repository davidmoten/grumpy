package com.github.davidmoten.grumpy.wms.layer;

public class Bounds {
    private final LatLon a;
    private final LatLon b;

    public Bounds(LatLon a, LatLon b) {
        super();
        this.a = a;
        this.b = b;
    }

    public LatLon getMin() {
        return a;
    }

    public LatLon getMax() {
        return b;
    }

}
