package com.github.davidmoten.grumpy.wms.demo;

import static com.github.davidmoten.grumpy.core.Position.position;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Date;

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
	private final ArrayList<Position> box;

	public CustomLayer() {
		// prepare a box around Canberra
		box = new ArrayList<Position>();
		box.add(position(CANBERRA_LAT - 2, CANBERRA_LON - 4));
		box.add(position(CANBERRA_LAT + 2, CANBERRA_LON - 4));
		box.add(position(CANBERRA_LAT + 2, CANBERRA_LON + 4));
		box.add(position(CANBERRA_LAT - 2, CANBERRA_LON + 4));
		box.add(position(CANBERRA_LAT - 2, CANBERRA_LON - 4));
	}

	@Override
	public void render(Graphics2D g, WmsRequest request) {

		RendererUtil.useAntialiasing(g);

		Projector projector = WmsUtil.getProjector(request);

		// get the box around Canberra as a shape
		Shape shape = RendererUtil.getPath(projector, box);
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

		// if user clicks within Canberra box then return some info, otherwise
		// return blank string

		Projector projector = WmsUtil.getProjector(request);
		Position position = projector.toPosition(point.x, point.y);

		if (position.isWithin(box))
			return "<div style=\"width:250px\">"
					+ "<p>Canberra is the capital city of Australia. With a population of 381,488, it is Australia's largest inland city and the eighth-largest city overall.</p>"
					+ "<img src=\"http://international.cit.edu.au/__data/assets/image/0006/27636/Canberra-Aerial-view-of-lake.jpg\" width=\"200\"/>"
					+ "</div>";
		else
			return "";
	}

}
