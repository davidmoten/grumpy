package com.github.davidmoten.geo.wms.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.geo.projection.Projector;
import com.github.davidmoten.geo.wms.Layer;
import com.github.davidmoten.geo.wms.WmsRequest;
import com.github.davidmoten.geo.wms.WmsUtil;

public class CustomLayer implements Layer {

	private static final Logger log = LoggerFactory
			.getLogger(CustomLayer.class);

	private static final String CANBERRA = "Canberra";
	private static final double CANBERRA_LAT = -35.3075;
	private static final double CANBERRA_LON = 149.1244;

	@Override
	public void render(Graphics g, WmsRequest request) {
		Projector projector = WmsUtil.getProjector(request);
		Point p = projector.toPoint(CANBERRA_LAT, CANBERRA_LON);
		g.setColor(Color.RED);
		g.setFont(g.getFont().deriveFont(12.0f));
		log.info("drawing string at " + p);
		g.drawString(CANBERRA, p.x + 5, p.y);
	}

	@Override
	public String getInfo(Date time, WmsRequest request, Point point,
			Format format) {
		return "<html><p>Some information about the point you clicked on</p></html>";
	}

}
