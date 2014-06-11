package com.github.davidmoten.geo.wms.demo;

import static com.github.davidmoten.geo.wms.WmsGetCapabilitiesProviderFromClasspath.fromClasspath;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.geo.wms.ImageCache;
import com.github.davidmoten.geo.wms.Layers;
import com.github.davidmoten.geo.wms.LayersBuilder;
import com.github.davidmoten.geo.wms.WmsGetCapabilitiesProvider;
import com.github.davidmoten.geo.wms.WmsServletRequestProcessor;

public class WmsServlet extends HttpServlet {

    private static final long serialVersionUID = 1518113833457077766L;

    private final WmsServletRequestProcessor processor;

    public WmsServlet() {

        // get capabilities xml from the classpath
        WmsGetCapabilitiesProvider getCapabilitiesProvider = fromClasspath("/wms-capabilities.xml");

        // add a single layer
        Layers layers = LayersBuilder
        // get a builder
                .builder()
                // add our custom layer (the name should match the name in
                // capabilities.xml
                .add("Custom", new CustomLayer())
                // build the Layers
                .build();

        // create and configure the cache
        ImageCache imageCache = new ImageCache();

        // initialize the request processor
        processor = new WmsServletRequestProcessor(getCapabilitiesProvider, layers, imageCache);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        // use the processor to handle requests
        processor.doGet(req, resp);
    }

}
