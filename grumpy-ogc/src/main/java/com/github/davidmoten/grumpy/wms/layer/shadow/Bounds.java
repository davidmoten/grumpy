package com.github.davidmoten.grumpy.wms.layer.shadow;

public class Bounds {
    private final LatLon min;
    private final LatLon max;

    public Bounds(LatLon min, LatLon max) {
        this.min = min;
        this.max = max;
    }

    public LatLon getMin() {
        return min;
    }

    public LatLon getMax() {
        return max;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Bounds [min=");
        builder.append(min);
        builder.append(", max=");
        builder.append(max);
        builder.append("]");
        return builder.toString();
    }

}
