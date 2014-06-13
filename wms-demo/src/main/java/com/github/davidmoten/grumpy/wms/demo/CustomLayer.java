package com.github.davidmoten.grumpy.wms.demo;

import static com.github.davidmoten.grumpy.core.Position.position;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.wms.Layer;
import com.github.davidmoten.grumpy.wms.RendererUtil;
import com.github.davidmoten.grumpy.wms.WmsRequest;
import com.github.davidmoten.grumpy.wms.WmsUtil;

public class CustomLayer implements Layer {

	private static final String CANBERRA = "Canberra";
	private static final double CANBERRA_LAT = -35.3075;
	private static final double CANBERRA_LON = 149.1244;

	@Override
	public void render(Graphics2D g, WmsRequest request) {

		RendererUtil.useAntialiasing(g);

		Projector projector = WmsUtil.getProjector(request);
		// draw a border around Canberra and shade it.
		List<Position> list = new ArrayList<Position>();
		list.add(position(CANBERRA_LAT - 2, CANBERRA_LON - 4));
		list.add(position(CANBERRA_LAT + 2, CANBERRA_LON - 4));
		list.add(position(CANBERRA_LAT + 2, CANBERRA_LON + 4));
		list.add(position(CANBERRA_LAT - 2, CANBERRA_LON + 4));
		list.add(position(CANBERRA_LAT - 2, CANBERRA_LON - 4));

		// join the positions using great circle paths
		Shape shape = RendererUtil.getPath(projector, list);
		g.setColor(Color.white);

		// fill the box with white
		// transparency is deferred to the wms client framework
		g.fill(shape);

		// draw border in blue
		g.setColor(Color.blue);
		g.draw(shape);

		// label Canberra
		Point p = projector.toPoint(CANBERRA_LAT, CANBERRA_LON);
		g.setColor(Color.RED);
		g.setFont(g.getFont().deriveFont(24.0f).deriveFont(Font.BOLD));
		g.drawString(CANBERRA, p.x + 5, p.y);

	}

	@Override
	public String getInfo(Date time, WmsRequest request, Point point,
			String mimeType) {
		return "<p>Some information about the point you clicked on</p>";
	}

}
