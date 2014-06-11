package com.github.davidmoten.geo.wms.demo;

import static com.github.davidmoten.geo.wms.WmsGetCapabilitiesProviderFromClasspath.fromClasspath;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.geo.wms.ImageCache;
import com.github.davidmoten.geo.wms.Layer;
import com.github.davidmoten.geo.wms.Layers;
import com.github.davidmoten.geo.wms.WmsGetCapabilitiesProvider;
import com.github.davidmoten.geo.wms.WmsServletRequestProcessor;

public class WmsServlet extends HttpServlet {

	private static final long serialVersionUID = 1518113833457077766L;

	private final WmsServletRequestProcessor processor;

	public WmsServlet() {
		WmsGetCapabilitiesProvider getCapabilitiesProvider = fromClasspath("/wms-capabilities.xml");
		processor = new WmsServletRequestProcessor(getCapabilitiesProvider,
				createLayers(), new ImageCache());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processor.doGet(req, resp);
	}

	private Layers createLayers() {
		return new Layers() {

			private final Layer layer = new CustomLayer();

			@Override
			public Layer getLayer(String layerName) {
				return layer;
			}
		};
	}

}
