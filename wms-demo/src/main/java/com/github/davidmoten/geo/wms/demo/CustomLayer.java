package com.github.davidmoten.geo.wms.demo;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import com.github.davidmoten.geo.wms.Layer;
import com.github.davidmoten.geo.wms.WmsRequest;

public class CustomLayer implements Layer {

	@Override
	public void render(Graphics g, WmsRequest request) {
		g.setFont(g.getFont().deriveFont(12.0f));
		g.drawString("Hello world", 100, 100);
	}

	@Override
	public String getInfo(Date time, WmsRequest request, Point point,
			Format format) {
		return "<html><p>Some information about the point you clicked on</p></html>";
	}

}
