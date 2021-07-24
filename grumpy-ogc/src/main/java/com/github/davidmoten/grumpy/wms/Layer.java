package com.github.davidmoten.grumpy.wms;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Date;

/**
 * Renders or displays info about positions on a WMS layer.
 */
public interface Layer extends HasLayerFeatures {

    /**
     * Render some information onto the supplied graphics context
     * 
     * @param g
     *            - the graphics context used for rendering
     * @param bounds
     *            - the geo-spatial bounding box of the region to be rendered
     * @param width
     *            - of the graphics area in pixels
     * @param height
     *            - of the graphics area in pixels
     */
    void render(Graphics2D g, WmsRequest request);

    /**
     * Returns info about the given point on the layer formatted as per the
     * requested mimeType.
     * 
     * @param time
     * @param request
     * @param point
     * @param mimeType
     * @return
     */
    String getInfo(Date time, WmsRequest request, Point point, String mimeType);

}
