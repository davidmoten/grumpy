package com.github.davidmoten.grumpy.wms.reduction;

import static com.github.davidmoten.grumpy.core.Position.position;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.util.Bounds;
import com.github.davidmoten.grumpy.util.LatLon;

/**
 * Samples bounds by creating a grid starting at top left corner of
 * min(maxSizeKm,widthKm) by min(maxSizeKm, heightKm). Always includes bottom
 * and right edge points as well.
 */
public class BoundsSamplerMaxSize implements BoundsSampler {

    private final double maxSizeKm;

    public BoundsSamplerMaxSize(double maxSizeKm) {
        this.maxSizeKm = maxSizeKm;
    }

    @Override
    public List<Position> sample(Bounds bounds) {

        LatLon min = bounds.getMin();
        LatLon max = bounds.getMax();
        Position topLeft = position(max.lat(), min.lon());
        Position bottomLeft = position(min.lat(), min.lon());
        Position topRight = position(max.lat(), max.lon());

        double xDistanceKm = topLeft.getDistanceToKm(topRight);
        double yDistanceKm = topLeft.getDistanceToKm(bottomLeft);

        double longDiff = Position.longitudeDiff(min.lon(), max.lon());
        double longStep = Math.min(1, maxSizeKm / xDistanceKm) * longDiff;
        double latStep = Math.min(1, maxSizeKm / yDistanceKm) * (max.lat() - min.lat());

        List<Position> list = new ArrayList<Position>();

        double lat = min.lat();

        while (lat < max.lat()) {
            addLat(min, max, longDiff, longStep, list, lat);
            lat += latStep;
        }
        addLat(min, max, longDiff, longStep, list, max.lat());

        return list;
    }

    private void addLat(LatLon min, LatLon max, double longDiff, double longStep,
            List<Position> list, double lat) {
        double lon = min.lon();
        while (Position.longitudeDiff(min.lon(), lon) <= longDiff) {
            list.add(position(lat, lon));
            lon += longStep;
        }
        list.add(position(lat, max.lon()));
    }
}
