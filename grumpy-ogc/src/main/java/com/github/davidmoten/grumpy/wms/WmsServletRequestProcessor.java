package com.github.davidmoten.grumpy.wms;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

/**
 * Processes a WMS {@link HttpServletRequest} and returns a
 * {@link HttpServletResponse}.
 */
public class WmsServletRequestProcessor {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WmsServletRequestProcessor.class);

    private static final String REQUEST_GET_MAP = "GetMap";
    private static final String PARAMETER_REQUEST = "REQUEST";
    private static final Object REQUEST_GET_CAPABILITIES = "GetCapabilities";
    private static final Object REQUEST_GET_FEATURE_INFO = "GetFeatureInfo";

    private final CapabilitiesProvider capabilitiesProvider;

    private WmsRequestProcessor processor;

    /**
     * Constructor.
     * 
     * @param capabilitiesProvider
     * @param layers
     * @param imageCache
     * @param imageWriter
     */
    public WmsServletRequestProcessor(CapabilitiesProvider capabilitiesProvider,
            WmsRequestProcessor processor) {
        this.capabilitiesProvider = capabilitiesProvider;
        this.processor = processor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CapabilitiesProvider capabilitiesProvider = new CapabilitiesProviderEmpty();
        private ImageCache imageCache = new ImageCache();
        private Layers layers;
        private final LayersBuilder layersBuilder = LayersBuilder.builder();
        private final List<String> layersToCache = new ArrayList<String>();
        private ImageWriter imageWriter = new ImageWriterDefault();
        private Integer imageCacheSize;

        private Builder() {
        }

        public Builder capabilities(CapabilitiesProvider capabilitiesProvider) {
            this.capabilitiesProvider = capabilitiesProvider;
            return this;
        }

        public Builder capabilities(Capabilities capabilities) {
            this.capabilitiesProvider = new CapabilitiesProviderFromCapabilities(capabilities);
            return this;
        }

        public Builder capabilitiesFromClasspath(String resource) {
            this.capabilitiesProvider = CapabilitiesProviderFromClasspath.fromClasspath(resource);
            return this;
        }

        public Builder imageCache(int size) {
            this.imageCacheSize = size;
            return this;
        }

        public Builder addCachedLayer(Layer layer) {
            return addCachedLayer(layer.getFeatures().getName(), layer);
        }

        /**
         * Adds the layer with cacheable images when generated and uses the given name
         * for the layer for WMS calls (this will override the layer name defined in
         * {#link {@link Layer#getFeatures()}.
         * 
         * @param name  override name for layer
         * @param layer layer to add with cacheable images
         * @return this
         */
        public Builder addCachedLayer(String name, Layer layer) {
            return addLayer(name, layer, true);
        }

        public Builder addLayer(Layer layer) {
            return addLayer(layer.getFeatures().getName(), layer);
        }

        /**
         * Adds the layer where generated images are non-cacheable and uses the given
         * name for the layer for WMS calls (this will override the layer name defined
         * in {#link {@link Layer#getFeatures()}.
         * 
         * @param name  override name for layer
         * @param layer layer to add with cacheable images
         * @return this
         */
        public Builder addLayer(String name, Layer layer) {
            return addLayer(name, layer, false);
        }

        /**
         * Adds the layer where generated images are cacheable according to parameter
         * {@code cache} and uses the given name for the layer for WMS calls (this will
         * override the layer name defined in {#link {@link Layer#getFeatures()}.
         * 
         * @param name  override name for layer
         * @param layer layer to add with cacheable images
         * @param cache if and only if true images will be cached up to the max image
         *              cache size
         * @return this
         */
        public Builder addLayer(String name, Layer layer, boolean cache) {
            layersBuilder.add(name, layer);
            if (cache)
                layersToCache.add(name);
            return this;
        }

        public Builder layers(Layers layers) {
            this.layers = layers;
            return this;
        }

        public Builder imageWriter(ImageWriter imageWriter) {
            this.imageWriter = imageWriter;
            return this;
        }

        public WmsServletRequestProcessor build() {
            if (imageCacheSize != null)
                imageCache = new ImageCache(imageCacheSize);
            for (String layer : layersToCache)
                imageCache.add(layer);
            if (layers == null)
                layers = layersBuilder.build();
            WmsRequestProcessor processor = new WmsRequestProcessor(layers, imageCache,
                    imageWriter);
            return new WmsServletRequestProcessor(capabilitiesProvider, processor);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long t = System.currentTimeMillis();
        try {
            log.info("httpGetUrl=" + request.getRequestURL() + "?" + request.getQueryString());
            log.info("requestedByIP = ip " + request.getRemoteAddr());
            String req = request.getParameter(PARAMETER_REQUEST);
            setNoCacheParameters(response);
            if (REQUEST_GET_CAPABILITIES.equals(req)) {
                writeCapabilities(request, response);
            } else if (REQUEST_GET_MAP.equals(req)) {
                writeImage(request, response);
            } else if (REQUEST_GET_FEATURE_INFO.equals(req)) {
                writeFeatureInfo(request, response);
            } else
                throw new UnknownParameterException("Unrecognized REQUEST parameter: " + req);
            // flush everything so timer below is realistic for delivery to
            // client
            response.getOutputStream().flush();
        } catch (UnknownParameterException e) {
            log.warn(e.getMessage());
            throw new ServletException(e);
        } catch (MissingMandatoryParameterException e) {
            log.warn(e.getMessage(), e);
            throw new ServletException(e);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.info("requestTimeSeconds="
                    + new DecimalFormat("0.000").format((System.currentTimeMillis() - t) / 1000.0)
                    + "s");
        }
    }

    private void handleException(Exception e) throws ServletException {
        if (e.getClass().getName().contains("ClientAbortException")
                || e.getMessage() != null && e.getMessage().contains("Broken pipe")
                || e.getCause() instanceof java.net.SocketException) {
            String s = e.getMessage();
            if (s == null)
                s = e.getClass().getName();
            log.warn(e.getMessage());
        } else {
            log.error(e.getClass().getName());
            log.error(e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    private void writeCapabilities(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/xml");
        String capabilities = capabilitiesProvider.getCapabilities(request);
        response.getOutputStream().write(capabilities.getBytes());

    }

    private void writeImage(HttpServletRequest request, HttpServletResponse response)
            throws MissingMandatoryParameterException, IOException {
        log.info("getting image");
        WmsRequest wmsRequest = new WmsRequest(request);
        OutputStream out = response.getOutputStream();
        response.setContentType(wmsRequest.getFormat());
        boolean cacheImage = "true".equalsIgnoreCase(request.getParameter("cacheImage"));

        processor.writeImage(wmsRequest, cacheImage, out);
    }

    private void writeFeatureInfo(HttpServletRequest request, HttpServletResponse response)
            throws MissingMandatoryParameterException, IOException {
        log.info("getting feature info");
        int i = getI(request);
        int j = getJ(request);
        WmsRequest wmsRequest = new WmsRequest(request);
        response.setContentType(wmsRequest.getInfoFormat());
        processor.writeFeatureInfo(i, j, wmsRequest, response.getOutputStream());
    }

    private int getJ(HttpServletRequest request) {
        if (request.getParameter("J") != null)
            return Math.round(Float.parseFloat(request.getParameter("J")));
        else
            // GAIA uses x, y instead of spec I,J!
            return Math.round(Float.parseFloat(request.getParameter("Y")));

    }

    private int getI(HttpServletRequest request) {
        if (request.getParameter("J") != null)
            return Math.round(Float.parseFloat(request.getParameter("I")));
        else
            // GAIA uses x, y instead of spec I,J!
            return Math.round(Float.parseFloat(request.getParameter("X")));

    }

    private void setNoCacheParameters(HttpServletResponse response) {
        // Set to expire far in the past.
        response.setHeader("Expires", "-1");

        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
    }

}
