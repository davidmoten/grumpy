package com.github.davidmoten.grumpy.wms.app;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.grumpy.wms.Capabilities;
import com.github.davidmoten.grumpy.wms.WmsServletRequestProcessor;
import com.github.davidmoten.grumpy.wms.layer.darkness.DarknessLayer;

public class WmsServlet extends HttpServlet {
    private static final long serialVersionUID = 1518113833457077766L;

    private static final String SERVICE_NAME = "Grumpy";
    private static final String SERVICE_TITLE = "Grumpy";
    private static final String SERVICE_ABSTRACT = "Grumpy WMS layers including Darkness layer";

    private final WmsServletRequestProcessor processor;

    public WmsServlet() {

        // instantiate the layers
        DarknessLayer darkness = new DarknessLayer();

        // setup the capabilities of the service which will extract features
        // from the layers to fill in defaults for the layer fields in generated
        // capabilities.xml
        Capabilities cap = Capabilities.builder()
        // set service name
                .serviceName(SERVICE_NAME)
                // set service title
                .serviceTitle(SERVICE_TITLE)
                // set service abstract
                .serviceAbstract(SERVICE_ABSTRACT)
                // add image format
                .imageFormat("image/png")
                // add info format
                .infoFormat("text/html")
                // add darkness layer
                .layer(darkness)
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
                // add darkness, not cached
                .addLayer("Darkness", darkness)
                // build it up
                .build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // use the processor to handle requests
        processor.doGet(req, resp);
    }

}
