package com.github.davidmoten.grumpy.wms.demo;

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

    private static final String SERVICE_TITLE = "Custom OGC Services";
    private static final String SERVICE_NAME = "CustomOGC";
    private static final String SERVICE_ABSTRACT = "Custom OGC WMS services including Custom, Fiddle and Darkness layers";

    private final WmsServletRequestProcessor processor;

    public WmsServlet() {

        // instantiate the layers
        CustomLayer custom = new CustomLayer();
        DarknessLayer darkness = new DarknessLayer();
        FiddleLayer fiddle = new FiddleLayer();

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
                // add custom layer
                .layer(custom)
                // add darkness layer
                .layer(darkness)
                // add fiddle layer
                .layer(fiddle)
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
                .addCachedLayer("Custom", custom)
                // add darkness, not cached
                .addLayer("Darkness", darkness)
                // add fiddles layer
                .addLayer("Fiddle", fiddle)
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
