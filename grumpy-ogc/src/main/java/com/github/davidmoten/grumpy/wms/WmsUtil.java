package com.github.davidmoten.grumpy.wms;

import java.awt.Color;
import java.awt.Rectangle;
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

    public static double getScale(WmsRequest request) {
        ProjectorBounds b = request.getBounds();
        ReferencedEnvelope envelope = new ReferencedEnvelope(b.getMinX(), b.getMaxX(), b.getMinY(),
                b.getMaxY(), FeatureUtil.getCrs(request.getCrs()));
        return RendererUtilities.calculateOGCScale(envelope, request.getWidth(),
                Collections.emptyMap());
    }

    public static Rectangle toTargetRectangle(Projector projector) {
        ProjectorTarget t = projector.getTarget();
        return new Rectangle(0, 0, t.getWidth(), t.getHeight());
    }

    public static List<Position> getBorder(Projector projector, Rectangle region) {
        List<Position> box = new ArrayList<Position>();
        box.add(projector.toPosition(region.x, region.y));
        box.add(projector.toPosition(region.x, region.y + region.height));
        box.add(projector.toPosition(region.x + region.width, region.y + region.height));
        box.add(projector.toPosition(region.x + region.width, region.y));
        box.add(projector.toPosition(region.x, region.y));
        return box;
    }

}
