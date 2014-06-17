package com.github.davidmoten.grumpy.wms.demo;

import static com.github.davidmoten.grumpy.core.Position.position;
import static com.github.davidmoten.grumpy.wms.RendererUtil.draw;
import static com.github.davidmoten.grumpy.wms.RendererUtil.getCircle;
import static com.github.davidmoten.grumpy.wms.RendererUtil.toPath;
import static com.github.davidmoten.grumpy.wms.WmsUtil.getProjector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Date;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.wms.Layer;
import com.github.davidmoten.grumpy.wms.WmsRequest;

public class FiddleLayer implements Layer {

    @Override
    public void render(Graphics2D g, WmsRequest request) {
        Projector projector = getProjector(request);
        List<Position> positions = getCircle(position(-35, 149), 10000, 360);
        List<GeneralPath> paths = toPath(projector, positions);
        g.setColor(Color.BLUE);
        draw(g, paths);
    }

    @Override
    public String getInfo(Date time, WmsRequest request, Point point, String mimeType) {
        return null;
    }

}
