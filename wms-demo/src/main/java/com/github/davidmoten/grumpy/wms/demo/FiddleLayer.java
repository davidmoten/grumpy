package com.github.davidmoten.grumpy.wms.demo;

import static com.github.davidmoten.grumpy.core.Position.position;
import static com.github.davidmoten.grumpy.wms.WmsUtil.getProjector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.util.Date;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.wms.Layer;
import com.github.davidmoten.grumpy.wms.LayerFeatures;
import com.github.davidmoten.grumpy.wms.RendererUtil;
import com.github.davidmoten.grumpy.wms.WmsRequest;
import com.github.davidmoten.grumpy.wms.WmsUtil;
import com.github.davidmoten.grumpy.wms.reduction.RectangleSampler;
import com.github.davidmoten.grumpy.wms.reduction.RectangleSamplerCorners;
import com.github.davidmoten.grumpy.wms.reduction.Reducer;
import com.github.davidmoten.grumpy.wms.reduction.ValueRenderer;
import com.google.common.base.Function;

public class FiddleLayer implements Layer {

    private final LayerFeatures features;

    public FiddleLayer() {
        features = LayerFeatures.builder().name("Fiddle").crs("EPSG:4326").crs("EPSG:3857").build();
    }

    @Override
    public void render(Graphics2D g, WmsRequest request) {
        Position centre = position(35, -40);
        Projector projector = getProjector(request);

        int radiusKm = 8000;

        Function<Position, Boolean> function = createValueFunction(centre, radiusKm);
        ValueRenderer<Boolean> valueRenderer = createValueRenderer();
        RectangleSampler sampler = new RectangleSamplerCorners();
        Reducer.render(g, function, projector, sampler, valueRenderer);

    }

    private ValueRenderer<Boolean> createValueRenderer() {
        return new ValueRenderer<Boolean>() {
            @Override
            public void render(Graphics2D g, Projector projector, Rectangle region, Boolean t) {
                if (t) {
                    List<Position> positions = WmsUtil.getBorder(projector, region);
                    List<GeneralPath> shapes = RendererUtil.toPath(projector, positions);
                    g.setColor(Color.LIGHT_GRAY);
                    RendererUtil.fill(g, shapes);
                }
            }
        };
    }

    private Function<Position, Boolean> createValueFunction(final Position centre,
            final double radiusKm) {
        return new Function<Position, Boolean>() {
            @Override
            public Boolean apply(Position p) {
                return centre.getDistanceToKm(p) <= radiusKm;
            }
        };
    }

    @Override
    public String getInfo(Date time, WmsRequest request, Point point, String mimeType) {
        return null;
    }

    @Override
    public LayerFeatures getFeatures() {
        return features;
    }

}
