package com.github.davidmoten.grumpy.wms.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.grumpy.wms.WmsServletRequestProcessor;
import com.github.davidmoten.grumpy.wms.layer.shadow.EarthShadowLayer;

public class WmsServlet extends HttpServlet {

    private static final long serialVersionUID = 1518113833457077766L;

    private final WmsServletRequestProcessor processor;

    public WmsServlet() {
        // initialize the request processor
        processor = WmsServletRequestProcessor.builder()
        // capabilities
                .capabilitiesFromClasspath("/wms-capabilities.xml")
                // set image cache size
                .imageCache(200)
                // add custom layer as cached
                .addCachedLayer("Custom", new CustomLayer())
                // add darkness
                .addLayer("Darkness", new EarthShadowLayer())
                // build it up
                .build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // use the processor to handle requests
        processor.doGet(req, resp);
    }

}
