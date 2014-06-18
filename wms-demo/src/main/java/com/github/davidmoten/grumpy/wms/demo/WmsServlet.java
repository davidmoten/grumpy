package com.github.davidmoten.grumpy.wms.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.grumpy.wms.Capabilities;
import com.github.davidmoten.grumpy.wms.CapabilitiesLayer;
import com.github.davidmoten.grumpy.wms.WmsServletRequestProcessor;
import com.github.davidmoten.grumpy.wms.layer.darkness.DarknessLayer;

public class WmsServlet extends HttpServlet {

    private static final long serialVersionUID = 1518113833457077766L;

    private final WmsServletRequestProcessor processor;

    public WmsServlet() {

        Capabilities cap = Capabilities
                .builder()
                .serviceName("CustomOgc")
                .serviceTitle("Custom OGC Services")
                .serviceAbstract(
                        "Custom OGC WMS services including Custom, Fiddle and Darkness layers")
                .imageFormat("image/png")
                .infoFormat("text/html")
                .layer(CapabilitiesLayer.builder().name("Custom").title("Custom WMS Layer")
                        .queryable().opaque().style("plain").crs("EPSG:4326").crs("EPSG:3857")
                        .build())
                // build caps
                .build();

        // initialize the request processor
        processor = WmsServletRequestProcessor.builder()
        // capabilities
                .capabilities(cap)
                // or use
                // .capabilitiesFromClasspath("/wms-capabilities.xml")
                // set image cache size
                .imageCache(200)
                // add custom layer as cached
                .addCachedLayer("Custom", new CustomLayer())
                // add darkness, not cached
                .addLayer("Darkness", new DarknessLayer())
                // add fiddles layer
                .addLayer("Fiddle", new FiddleLayer())
                // build it up
                .build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        // use the processor to handle requests
        processor.doGet(req, resp);
    }

}
