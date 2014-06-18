package com.github.davidmoten.grumpy.wms.layer.darkness;

import static com.github.davidmoten.grumpy.core.Position.position;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.FeatureUtil;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.grumpy.projection.ProjectorTarget;
import com.github.davidmoten.grumpy.util.Bounds;
import com.github.davidmoten.grumpy.util.LatLon;
import com.github.davidmoten.grumpy.wms.Layer;
import com.github.davidmoten.grumpy.wms.LayerFeatures;
import com.github.davidmoten.grumpy.wms.RendererUtil;
import com.github.davidmoten.grumpy.wms.WmsRequest;
import com.github.davidmoten.grumpy.wms.WmsUtil;
import com.github.davidmoten.grumpy.wms.layer.darkness.SunUtil.Twilight;

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
        ProjectorBounds b = request.getBounds();
        Position min = FeatureUtil.convertToLatLon(b.getMinX(), b.getMinY(), request.getCrs());
        Position max = FeatureUtil.convertToLatLon(b.getMaxX(), b.getMaxY(), request.getCrs());
        Bounds bounds = new Bounds(new LatLon(min.getLat(), min.getLon()), new LatLon(max.getLat(),
                max.getLon()));
        render(g, projector, bounds, request.getWidth(), request.getHeight(), request.getStyles());
    }

    /**
     * Render the Earth's shadow onto the supplied graphics context
     * 
     * @param g
     *            - the graphics context used for rendering
     * @param projector
     *            - the projection used to map from the geo-spatial world onto
     *            the graphics context
     * @param bounds
     *            - the geo-spatial bounding box of the region to be rendered
     * @param width
     *            - of the graphics area in pixels
     * @param height
     *            - of the graphics area in pixels
     */
    private void render(Graphics2D g, Projector projector, Bounds bounds, int width, int height,
            List<String> styles) {

        Position subSolarPoint = SunUtil.getSubSolarPoint();
        renderSubSolarPoint(g, subSolarPoint, projector, subSolarImage, styles);
        renderTwilight(g, subSolarPoint, projector, bounds);
    }

    private static void renderSubSolarPoint(Graphics2D g, Position subSolarPoint,
            Projector projector, BufferedImage subSolarImage, List<String> styles) {

        LatLon latLon = new LatLon(subSolarPoint.getLat(), subSolarPoint.getLon());
        Point point = projector.toPoint(latLon.lat(), latLon.lon());
        int size = SUB_SOLAR_POINT_SIZE_PIXELS;
        if (styles.contains(STYLE_PLAIN)) {
            Ellipse2D spot = new Ellipse2D.Double();
            g.setColor(Color.YELLOW);
            spot.setFrame(point.x - size / 2, point.y - size / 2, size, size);
            g.fill(spot);
        } else
            g.drawImage(subSolarImage, point.x - size / 2, point.y - size / 2, size, size, null);
    }

    private static BufferedImage loadSubSolarPointImage() {
        try {
            return ImageIO.read(DarknessLayer.class.getResourceAsStream("/sunny.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void renderTwilight(Graphics2D g, Position subSolarPoint, Projector projector,
            Bounds geoBounds) {

        ProjectorTarget t = projector.getTarget();
        Rectangle xyBounds = new Rectangle(0, 0, t.getWidth(), t.getHeight());
        renderTwilightRegion(g, subSolarPoint, projector, geoBounds, xyBounds);
    }

    private static void renderTwilightRegion(Graphics2D g, Position subSolarPoint,
            Projector projector, Bounds geoBounds, Rectangle xyBounds) {

        // check if we need to divide the region
        boolean regionDivisible = xyBounds.height > 1 || xyBounds.width > 1;

        final Twilight regionUniformTwilightValue;
        if (!regionDivisible) {
            // region is indivisible, so choose any corner for the twilight
            // value
            regionUniformTwilightValue = SunUtil.getTwilight(subSolarPoint, new Position(geoBounds
                    .getMin().lat(), geoBounds.getMin().lon()));
        } else {
            // get the twilight value for the region if common to all sample
            // points in the region (if no common value returns null)
            regionUniformTwilightValue = SunUtil.getUniformTwilight(geoBounds, subSolarPoint);
        }

        if (regionUniformTwilightValue != null) {
            // shade the region to represent the twilight
            shadeRegion(g, projector, geoBounds, regionUniformTwilightValue);
        } else {
            // region is a mix of twilight conditions and is divisible
            // so divide into sub regions ... 2 or 4
            // but only if we can

            renderTwilightOnSplitRegions(g, subSolarPoint, projector, xyBounds);
        }
    }

    private static void renderTwilightOnSplitRegions(Graphics2D g, Position subSolarPoint,
            Projector projector, Rectangle xyBounds) {
        // split region
        final Rectangle[] rectangles = splitRectangles(xyBounds);

        // now render each region
        for (Rectangle rect : rectangles) {
            Position min = projector.toPosition(rect.x, rect.y + rect.height);
            Position max = projector.toPosition(rect.x + rect.width, rect.y);
            Bounds bounds = new Bounds(new LatLon(min.getLat(), min.getLon()), new LatLon(
                    max.getLat(), max.getLon()));
            renderTwilightRegion(g, subSolarPoint, projector, bounds, rect);
        }
    }

    private static void shadeRegion(Graphics2D g, Projector projector, Bounds geoBounds,
            final Twilight twilight) {
        if (twilight != Twilight.DAYLIGHT) {

            List<Position> box = new ArrayList<Position>();
            box.add(position(geoBounds.getMin().lat(), geoBounds.getMin().lon()));
            box.add(position(geoBounds.getMin().lat(), geoBounds.getMax().lon()));
            box.add(position(geoBounds.getMax().lat(), geoBounds.getMax().lon()));
            box.add(position(geoBounds.getMax().lat(), geoBounds.getMin().lon()));
            box.add(position(geoBounds.getMin().lat(), geoBounds.getMin().lon()));

            // use multiple paths to handle boundary weirdness
            List<GeneralPath> path = RendererUtil.toPath(projector, box);

            // fill the region
            g.setColor(shades.get(twilight));
            RendererUtil.fill(g, path);
        }
    }

    private static Rectangle[] splitRectangles(Rectangle xyBounds) {
        final Rectangle[] rectangles;

        if (xyBounds.width > 1 && xyBounds.height > 1) {

            // divide into 4 sub regions

            rectangles = new Rectangle[4];
            int halfWidth = xyBounds.width / 2;
            int halfHeight = xyBounds.height / 2;
            rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, halfWidth, halfHeight);
            rectangles[1] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y, xyBounds.width
                    - halfWidth, halfHeight);
            rectangles[2] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y + halfHeight,
                    xyBounds.width - halfWidth, xyBounds.height - halfHeight);
            rectangles[3] = new Rectangle(xyBounds.x, xyBounds.y + halfHeight, halfWidth,
                    xyBounds.height - halfHeight);

        } else if (xyBounds.height > 1) {

            // divide into two vertically

            rectangles = new Rectangle[2];

            int halfHeight = xyBounds.height / 2;
            rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, 1, halfHeight);
            rectangles[1] = new Rectangle(xyBounds.x, xyBounds.y + halfHeight, 1, xyBounds.height
                    - halfHeight);

        } else {

            // divide into two horizontally

            rectangles = new Rectangle[2];
            int halfWidth = xyBounds.width / 2;
            rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, halfWidth, 1);
            rectangles[1] = new Rectangle(xyBounds.x + halfWidth, xyBounds.y, xyBounds.width
                    - halfWidth, 1);
        }
        return rectangles;
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