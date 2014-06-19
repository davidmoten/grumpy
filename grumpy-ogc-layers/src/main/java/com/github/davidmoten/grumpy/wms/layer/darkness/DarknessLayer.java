package com.github.davidmoten.grumpy.wms.layer.darkness;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.wms.Layer;
import com.github.davidmoten.grumpy.wms.LayerFeatures;
import com.github.davidmoten.grumpy.wms.RendererUtil;
import com.github.davidmoten.grumpy.wms.WmsRequest;
import com.github.davidmoten.grumpy.wms.WmsUtil;
import com.github.davidmoten.grumpy.wms.layer.darkness.SunUtil.Twilight;
import com.github.davidmoten.grumpy.wms.reduction.RectangleSampler;
import com.github.davidmoten.grumpy.wms.reduction.RectangleSamplerCorners;
import com.github.davidmoten.grumpy.wms.reduction.Reducer;
import com.github.davidmoten.grumpy.wms.reduction.ValueRenderer;
import com.google.common.base.Function;

/**
 * Splits the visible region into rectangles recursively till all sampled points
 * in each rectangle have the same {@link Twilight} value. Once the rectangle
 * has a uniform {@link Twilight} value it is filled with the shade
 * corresponding to the {@link Twilight} value.
 * 
 * @author Steven Ring
 * @author Dave Moten
 */
public class DarknessLayer implements Layer {

    private static final String STYLE_PLAIN = "plain";
    private static final int SUB_SOLAR_POINT_SIZE_PIXELS = 30;
    private static final Map<Twilight, Color> shades = createShades();
    private final BufferedImage subSolarImage;
    private final LayerFeatures features;

    public DarknessLayer() {
        subSolarImage = loadSubSolarPointImage();
        features = LayerFeatures.builder().name("Darkness").style(STYLE_PLAIN).crs("EPSG:4326")
                .crs("EPSG:3857").build();
    }

    @Override
    public void render(Graphics2D g, WmsRequest request) {
        Projector projector = WmsUtil.getProjector(request);
        Position subSolarPoint = SunUtil.getSubSolarPoint();
        renderSubSolarPoint(g, subSolarPoint, projector, subSolarImage, request.getStyles());
        renderTwilight(g, subSolarPoint, projector);
    }

    private static void renderSubSolarPoint(Graphics2D g, Position subSolarPoint,
            Projector projector, BufferedImage subSolarImage, List<String> styles) {

        Point point = projector.toPoint(subSolarPoint.getLat(), subSolarPoint.getLon());
        int size = SUB_SOLAR_POINT_SIZE_PIXELS;
        if (styles.contains(STYLE_PLAIN)) {
            fillCircle(g, point, size);
        } else
            g.drawImage(subSolarImage, point.x - size / 2, point.y - size / 2, size, size, null);
    }

    private static void fillCircle(Graphics2D g, Point point, int size) {
        Ellipse2D spot = new Ellipse2D.Double();
        g.setColor(Color.YELLOW);
        spot.setFrame(point.x - size / 2, point.y - size / 2, size, size);
        g.fill(spot);
    }

    private static BufferedImage loadSubSolarPointImage() {
        try {
            return ImageIO.read(DarknessLayer.class.getResourceAsStream("/sunny.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void renderTwilight(Graphics2D g, final Position subSolarPoint,
            Projector projector) {

        Function<Position, Twilight> function = createValueFunction(subSolarPoint);
        ValueRenderer<Twilight> valueRenderer = createValueRenderer();
        RectangleSampler sampler = new RectangleSamplerCorners();
        Reducer.render(g, function, projector, sampler, valueRenderer);
    }

    private static Function<Position, Twilight> createValueFunction(final Position subSolarPoint) {
        return new Function<Position, Twilight>() {
            @Override
            public Twilight apply(Position p) {
                return SunUtil.getTwilight(subSolarPoint, p);
            }
        };
    }

    private static ValueRenderer<Twilight> createValueRenderer() {
        return new ValueRenderer<Twilight>() {
            @Override
            public void render(Graphics2D g, Projector projector, Rectangle region, Twilight t) {
                renderBounds(g, projector, region, t);
            }
        };
    }

    private static void renderBounds(Graphics2D g, Projector projector, Rectangle region,
            final Twilight twilight) {
        if (twilight != Twilight.DAYLIGHT) {

            List<Position> box = WmsUtil.getBorder(projector, region);

            // use multiple paths to handle boundary weirdness
            List<GeneralPath> path = RendererUtil.toPath(projector, box);

            // fill the region
            g.setColor(shades.get(twilight));
            RendererUtil.fill(g, path);
        }
    }

    private static Map<Twilight, Color> createShades() {
        Map<Twilight, Color> shades = new HashMap<Twilight, Color>();
        shades.put(Twilight.NIGHT, Color.BLACK);
        shades.put(Twilight.ASTRONOMICAL, new Color(50, 50, 50));
        shades.put(Twilight.NAUTICAL, new Color(100, 100, 100));
        shades.put(Twilight.CIVIL, new Color(150, 150, 150));
        shades.put(Twilight.DAYLIGHT, Color.WHITE);
        return shades;
    }

    @Override
    public String getInfo(Date time, WmsRequest request, Point point, String format) {
        return null;
    }

    @Override
    public LayerFeatures getFeatures() {
        return features;
    }

}