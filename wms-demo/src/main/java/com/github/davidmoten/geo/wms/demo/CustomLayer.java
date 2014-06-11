package com.github.davidmoten.geo.wms.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.geo.Position;
import com.github.davidmoten.geo.projection.Projector;
import com.github.davidmoten.geo.wms.Layer;
import com.github.davidmoten.geo.wms.RendererUtil;
import com.github.davidmoten.geo.wms.WmsRequest;
import com.github.davidmoten.geo.wms.WmsUtil;

public class CustomLayer implements Layer {

	private static final Logger log = LoggerFactory
			.getLogger(CustomLayer.class);

	private static final String CANBERRA = "Canberra";
	private static final double CANBERRA_LAT = -35.3075;
	private static final double CANBERRA_LON = 149.1244;

	@Override
	public void render(Graphics2D g, WmsRequest request) {
		Projector projector = WmsUtil.getProjector(request);

		// draw a border around Canberra and shade it.
		List<Position> list = new ArrayList<Position>();
		list.add(Position.create(CANBERRA_LAT - 2, CANBERRA_LON - 4));
		list.add(Position.create(CANBERRA_LAT + 2, CANBERRA_LON - 4));
		list.add(Position.create(CANBERRA_LAT + 2, CANBERRA_LON + 4));
		list.add(Position.create(CANBERRA_LAT - 2, CANBERRA_LON + 4));
		list.add(Position.create(CANBERRA_LAT - 2, CANBERRA_LON - 4));

		// join the positions using great circle paths
		Shape shape = RendererUtil.getPath(projector, list);
		g.setColor(Color.white);

		// fill the box with white
		g.fill(shape);

		// draw border in blue
		g.setColor(Color.BLUE);
		g.draw(shape);

		// label Canberra
		Point p = projector.toPoint(CANBERRA_LAT, CANBERRA_LON);
		g.setColor(Color.RED);
		g.setFont(g.getFont().deriveFont(24.0f).deriveFont(Font.BOLD));
		g.drawString(CANBERRA, p.x + 5, p.y);

	}

	@Override
	public String getInfo(Date time, WmsRequest request, Point point,
			Format format) {
		return "<html><p>Some information about the point you clicked on</p></html>";
	}

}
