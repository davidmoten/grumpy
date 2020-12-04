package com.github.davidmoten.grumpy.projection;

import com.github.davidmoten.grumpy.core.Position;
import org.apache.commons.io.IOUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureUtil {
    public static final String EPSG_4326 = "EPSG:4326";
    public static final String EPSG_900913 = "EPSG:900913";
    public static final String EPSG_3857 = "EPSG:3857";
    /**
     * ARCGIS copy of 3857
     */
    public static final String EPSG_102100 = "EPSG:102100";

    private static Map<String, CoordinateReferenceSystem> crs = new ConcurrentHashMap<String, CoordinateReferenceSystem>();

    public static synchronized CoordinateReferenceSystem getCrs(String epsg) {
        try {
            if (crs.get(epsg) != null)
                return crs.get(epsg);

            if (epsg.equals(EPSG_900913)) {
                String wkt = IOUtils.toString(FeatureUtil.class
                        .getResourceAsStream("/epsg/EPSG_900913.txt"));
                crs.put(epsg, CRS.parseWKT(wkt));
            } else if (epsg.equals(EPSG_102100)) {
                String wkt = IOUtils.toString(FeatureUtil.class
                        .getResourceAsStream("/epsg/EPSG_102100.txt"));
                crs.put(epsg, CRS.parseWKT(wkt));
            } else
                crs.put(epsg, CRS.decode(epsg));
            return crs.get(epsg);
        } catch (FactoryException e) {
            throw new RuntimeException("could not load " + epsg, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Point createPoint(double lat, double lon, String srsName) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(lon, lat);
        Point point = geometryFactory.createPoint(coordinate);

        try {
            if (!srsName.equals(EPSG_4326)) {
                MathTransform transform = CRS.findMathTransform(getCrs(EPSG_4326), getCrs(srsName));
                point = (Point) JTS.transform(point, transform);
            }
            return point;

        } catch (NoSuchAuthorityCodeException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }

    public static Position convertToLatLon(double x, double y, String srsName) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(x, y);
        Point point = geometryFactory.createPoint(coordinate);

        try {
            if (!srsName.equals(EPSG_4326)) {
                MathTransform transform = CRS.findMathTransform(getCrs(EPSG_4326), getCrs(srsName));
                point = (Point) JTS.transform(point, transform.inverse());
            }
            return new Position(point.getY(), point.getX());
        } catch (NoSuchAuthorityCodeException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }

}
