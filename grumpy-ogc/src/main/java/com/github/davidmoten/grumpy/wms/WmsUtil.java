package com.github.davidmoten.grumpy.wms;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.lite.RendererUtilities;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.FeatureUtil;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.grumpy.projection.ProjectorTarget;
import com.github.davidmoten.grumpy.util.Bounds;
import com.github.davidmoten.grumpy.util.LatLon;

public class WmsUtil {

    public static List<Color> getColorFromStyles(List<String> styles) {
        List<Color> colors = new ArrayList<Color>();
        for (String style : styles) {
            Field field;
            try {
                field = Color.class.getField(style);
                Color color = (Color) field.get(null);
                colors.add(color);
            } catch (SecurityException e) {
                // ignore
            } catch (NoSuchFieldException e) {
                // ignore
            } catch (IllegalArgumentException e) {
                // ignore
            } catch (IllegalAccessException e) {
                // ignore;
            }
        }
        return colors;
    }

    public static Projector getProjector(WmsRequest request) {
        ProjectorTarget target = new ProjectorTarget(request.getWidth(), request.getHeight());
        return new Projector(request.getBounds(), target);
    }

    public static double calculateScale(WmsRequest request) {
        ProjectorBounds b = request.getBounds();
        ReferencedEnvelope envelope = new ReferencedEnvelope(b.getMinX(), b.getMaxX(), b.getMinY(), b.getMaxY(),
                FeatureUtil.getCrs(request.getCrs()));
        double scale = RendererUtilities.calculateOGCScale(envelope, request.getWidth(), Collections.emptyMap());
        return scale;
    }

    public static Bounds toBounds(WmsRequest request) {
        ProjectorBounds b = request.getBounds();
        Position min = FeatureUtil.convertToLatLon(b.getMinX(), b.getMinY(), request.getCrs());
        Position max = FeatureUtil.convertToLatLon(b.getMaxX(), b.getMaxY(), request.getCrs());
        return new Bounds(new LatLon(min.getLat(), min.getLon()), new LatLon(max.getLat(), max.getLon()));
    }

}
