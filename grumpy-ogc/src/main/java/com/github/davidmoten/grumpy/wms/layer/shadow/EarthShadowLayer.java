package com.github.davidmoten.grumpy.wms.layer.shadow;

import static com.github.davidmoten.grumpy.wms.RendererUtil.draw;
import static com.github.davidmoten.grumpy.wms.RendererUtil.getCircle;
import static com.github.davidmoten.grumpy.wms.RendererUtil.getPath;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.FeatureUtil;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.grumpy.wms.Layer;
import com.github.davidmoten.grumpy.wms.WmsRequest;
import com.github.davidmoten.grumpy.wms.WmsUtil;
import com.github.davidmoten.grumpy.wms.layer.shadow.Sun.Twilight;

public class EarthShadowLayer implements Layer {

    private static final Logger log = LoggerFactory.getLogger(EarthShadowLayer.class);

    private static HashMap<Twilight, Color> shades = new HashMap<Twilight, Color>();

    static {
        shades.put(Twilight.NIGHT, Color.BLACK);
        shades.put(Twilight.ASTRONOMICAL, new Color(50, 50, 50));
        shades.put(Twilight.NAUTICAL, new Color(100, 100, 100));
        shades.put(Twilight.CIVIL, new Color(150, 150, 150));
        shades.put(Twilight.DAYLIGHT, Color.WHITE);
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
    private void render(Graphics g, Projector projector, Bounds bounds, int width, int height) {

        Graphics2D g2d = (Graphics2D) g;
        Position subSolarPoint = Sun.getPosition(null);

        Color color = g.getColor();
        renderSubSolarPoint(g2d, subSolarPoint, projector);
        renderTwilight(g2d, subSolarPoint, projector, bounds);
        g.setColor(color);

    }

    private void renderSubSolarPoint(Graphics2D g, Position subSolarPoint, Projector projector) {

        Ellipse2D spot = new Ellipse2D.Double();

        g.setColor(Color.YELLOW);
        LatLon latLon = new LatLon(subSolarPoint.getLat(), subSolarPoint.getLon());
        Point point = projector.toPoint(latLon.lat(), latLon.lon());
        spot.setFrame(point.x - 10, point.y - 10, 20.0, 20.0);
        g.fill(spot);

        log.info("rendering circle");
        g.setColor(Color.RED);
        draw(g, getPath(projector, getCircle(subSolarPoint, 1000, 20)));

        log.info("rendered circle");
    }

    private void renderTwilight(Graphics2D g, Position subSolarPoint, Projector projector,
            Bounds geoBounds) {

        Point min = projector.toPoint(geoBounds.getMin().lat(), geoBounds.getMin().lon());
        Point max = projector.toPoint(geoBounds.getMax().lat(), geoBounds.getMax().lon());
        Rectangle xyBounds = new Rectangle(min.x, max.y, max.x - min.x, min.y - max.y);

        renderTwilightRegion(g, subSolarPoint, projector, geoBounds, xyBounds);

    }

    private void renderTwilightRegion(Graphics2D g, Position subSolarPoint, Projector projector,
            Bounds geoBounds, Rectangle xyBounds) {

        // g.setColor(Color.CYAN);
        // g.draw(xyBounds);

        // check if we need to divide the region

        Twilight twilight = null;

        if (xyBounds.height == 1 && xyBounds.width == 1) {

            // region is indivisible, so fill it

            twilight = Sun.getTwilight(subSolarPoint, new Position(geoBounds.getMin().lat(),
                    geoBounds.getMin().lon()));
        } else {
            twilight = Sun.getRegionTwilight(geoBounds, subSolarPoint);
        }

        if (twilight != null) {

            // shade the region to represent the twilight

            if (twilight != Twilight.DAYLIGHT) {
                g.setColor(shades.get(twilight));
                g.fill(xyBounds);
            }

        } else {

            // region is a mix of twilight conditions and is divisble
            // so divide into sub regions ... 2 or 4
            // but only if we can

            Rectangle[] rectangles = null;

            if (xyBounds.width > 1 && xyBounds.height > 1) {

                // divide into 4 sub regions

                rectangles = new Rectangle[4];
                rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, xyBounds.width / 2,
                        xyBounds.height / 2);
                rectangles[1] = new Rectangle(xyBounds.x + rectangles[0].width, xyBounds.y,
                        xyBounds.width - rectangles[0].width, rectangles[0].height);
                rectangles[2] = new Rectangle(rectangles[1].x, xyBounds.y + rectangles[1].height,
                        rectangles[1].width, xyBounds.height - rectangles[1].height);
                rectangles[3] = new Rectangle(xyBounds.x, xyBounds.y + rectangles[0].height,
                        rectangles[0].width, rectangles[2].height);

            } else if (xyBounds.height > 1) {

                // divide into two vertically

                rectangles = new Rectangle[2];

                rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, 1, xyBounds.height / 2);
                rectangles[1] = new Rectangle(xyBounds.x, xyBounds.y + rectangles[0].height, 1,
                        xyBounds.height - rectangles[0].height);

            } else {

                // divide into two horizontally

                rectangles = new Rectangle[2];

                rectangles[0] = new Rectangle(xyBounds.x, xyBounds.y, xyBounds.width / 2, 1);
                rectangles[1] = new Rectangle(xyBounds.x + rectangles[0].width, xyBounds.y,
                        xyBounds.width - rectangles[0].width, 1);

            }

            // now render each region

            for (Rectangle rect : rectangles) {
                Position min = projector.toPosition(rect.x, rect.y + rect.height);
                Position max = projector.toPosition(rect.x + rect.width, rect.y);

                Bounds bounds = new Bounds(new LatLon(min.getLat(), min.getLon()), new LatLon(
                        max.getLat(), max.getLon()));
                renderTwilightRegion(g, subSolarPoint, projector, bounds, rect);
            }
        }
    }

    @Override
    public void render(Graphics2D g, WmsRequest request) {
        Projector projector = WmsUtil.getProjector(request);
        ProjectorBounds b = request.getBounds();
        Position min = FeatureUtil.convertToLatLon(b.getMinX(), b.getMinY(), request.getCrs());
        Position max = FeatureUtil.convertToLatLon(b.getMaxX(), b.getMaxY(), request.getCrs());
        Bounds bounds = new Bounds(new LatLon(min.getLat(), min.getLon()), new LatLon(max.getLat(),
                max.getLon()));
        render(g, projector, bounds, request.getWidth(), request.getHeight());
    }

    @Override
    public String getInfo(Date time, WmsRequest request, Point point, String format) {
        return null;
    }

}