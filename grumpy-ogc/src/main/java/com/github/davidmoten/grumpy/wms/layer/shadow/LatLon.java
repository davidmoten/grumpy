package com.github.davidmoten.grumpy.wms.layer.shadow;

public class LatLon {

    private final double lat;
    private final double lon;

    public LatLon(double lat, double lon) {
        super();
        this.lat = lat;
        this.lon = lon;
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }

}
