package com.github.davidmoten.grumpy.util;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LatLon [lat=");
        builder.append(lat);
        builder.append(", lon=");
        builder.append(lon);
        builder.append("]");
        return builder.toString();
    }

}
