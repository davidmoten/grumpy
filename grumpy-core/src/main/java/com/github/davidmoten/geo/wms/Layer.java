package com.github.davidmoten.geo.wms;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Date;

public interface Layer {

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
    public void render(Graphics2D g, WmsRequest request);

    String getInfo(Date time, WmsRequest request, Point point, InfoFormat format);

}
