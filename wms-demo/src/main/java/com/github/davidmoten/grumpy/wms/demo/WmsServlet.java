package com.github.davidmoten.grumpy.wms.demo;

import static com.github.davidmoten.grumpy.wms.WmsGetCapabilitiesProviderFromClasspath.fromClasspath;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.grumpy.wms.ImageCache;
import com.github.davidmoten.grumpy.wms.Layers;
import com.github.davidmoten.grumpy.wms.LayersBuilder;
import com.github.davidmoten.grumpy.wms.WmsGetCapabilitiesProvider;
import com.github.davidmoten.grumpy.wms.WmsServletRequestProcessor;

public class WmsServlet extends HttpServlet {

	private static final long serialVersionUID = 1518113833457077766L;

	private final WmsServletRequestProcessor processor;

	public WmsServlet() {

		// get capabilities xml from the classpath
		WmsGetCapabilitiesProvider capabilities = fromClasspath("/wms-capabilities.xml");

		// add a single layer
		Layers layers = LayersBuilder.builder()
		// add our custom layer (the name should match the name in
		// capabilities.xml)
				.add("Custom", new CustomLayer())
				// build the layers
				.build();

		// create and configure the cache for max 200 images
		ImageCache imageCache = ImageCache.create(200).add("Custom");

		// initialize the request processor
		processor = new WmsServletRequestProcessor(capabilities, layers,
				imageCache);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// use the processor to handle requests
		processor.doGet(req, resp);
	}

}
