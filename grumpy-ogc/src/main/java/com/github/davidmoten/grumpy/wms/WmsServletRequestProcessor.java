package com.github.davidmoten.grumpy.wms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    private final WmsGetCapabilitiesProvider capabilitiesProvider;

    private final ImageCache imageCache;

    private final LayerManager layerManager;

    private final ImageWriter imageWriter;

    /**
     * Constructor.
     * 
     * @param capabilitiesProvider
     * @param layers
     * @param imageCache
     * @param imageWriter
     */
    public WmsServletRequestProcessor(WmsGetCapabilitiesProvider capabilitiesProvider, Layers layers,
            ImageCache imageCache, ImageWriter imageWriter) {
        this.capabilitiesProvider = capabilitiesProvider;
        this.imageCache = imageCache;
        this.imageWriter = imageWriter;
        this.layerManager = new LayerManager(layers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private WmsGetCapabilitiesProvider capabilitiesProvider = new WmsGetCapabilitiesProviderEmpty();
        private ImageCache imageCache = new ImageCache();
        private Layers layers;
        private final LayersBuilder layersBuilder = LayersBuilder.builder();
        private final List<String> layersToCache = new ArrayList<String>();
        private ImageWriter imageWriter = new ImageWriterDefault();
        private Integer imageCacheSize;

        private Builder() {
        }

        public Builder capabilities(WmsGetCapabilitiesProvider capabilitiesProvider) {
            this.capabilitiesProvider = capabilitiesProvider;
            return this;
        }

        public Builder capabilitiesFromClasspath(String resource) {
            this.capabilitiesProvider = WmsGetCapabilitiesProviderFromClasspath.fromClasspath(resource);
            return this;
        }

        public Builder imageCache(int size) {
            this.imageCacheSize = size;
            return this;
        }

        public Builder addCachedLayer(String name, Layer layer) {
            return addLayer(name, layer, true);
        }

        public Builder addLayer(String name, Layer layer) {
            return addLayer(name, layer, false);
        }

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
            return new WmsServletRequestProcessor(capabilitiesProvider, layers, imageCache, imageWriter);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                    + new DecimalFormat("0.000").format((System.currentTimeMillis() - t) / 1000.0) + "s");
        }
    }

    private void handleException(Exception e) throws ServletException {
        if (e.getClass().getName().contains("ClientAbortException") || e.getMessage() != null
                && e.getMessage().contains("Broken pipe") || e.getCause() instanceof java.net.SocketException) {
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

    private void writeCapabilities(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/xml");
        String capabilities = capabilitiesProvider.getCapabilities(request);
        response.getOutputStream().write(capabilities.getBytes());

    }

    private void writeImage(HttpServletRequest request, HttpServletResponse response)
            throws MissingMandatoryParameterException, IOException {
        log.info("getting image");
        WmsRequest wmsRequest = new WmsRequest(request);
        byte[] bytes = null;
        response.setContentType(wmsRequest.getFormat());

        // check the cache for the bytes of the image converted to the
        // appropriate format. Note that the critical bottleneck is
        // ImageIO.write rather than the layerManager.getImage call
        bytes = imageCache.get(wmsRequest);

        // if cacheImage=false then don't use cache
        if ("false".equals(request.getParameter("cacheImage")))
            bytes = null;

        if (bytes == null) {
            log.info("image cache empty");

            BufferedImage image = null;
            // dynamic layers should clear the imageCache in a separate thread
            // (for example, using a quartz job)
            image = layerManager.getImage(wmsRequest);
            // Note that we write the image to memory first to avoid this JRE
            // bug:
            // http://bugs.sun.com/bugdatabase/view_bug.do;jsessionid=dc84943191e06dffffffffdf200f5210dd319?bug_id=6967419
            // which is commented on further in JIRA ER-95
            log.info("writing image to memory for layers " + wmsRequest.getLayers());
            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            String imageType = wmsRequest.getFormat().substring(wmsRequest.getFormat().indexOf('/') + 1);
            // This call is slow!!
            long t = System.currentTimeMillis();
            imageWriter.writeImage(image, byteOs, imageType);
            log.info("ImageIoWriteTimeMs=" + (System.currentTimeMillis() - t));
            bytes = byteOs.toByteArray();
            imageCache.put(wmsRequest, bytes);
        } else
            log.info("obtained image from cache for layers " + wmsRequest.getLayers());

        log.info("writing image to http output stream for layers " + wmsRequest.getLayers());
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
        log.info("imageSizeK=" + new DecimalFormat("0.000").format(bytes.length / 1000.0) + " for layers "
                + wmsRequest.getLayers());
    }

    private void writeFeatureInfo(HttpServletRequest request, HttpServletResponse response)
            throws MissingMandatoryParameterException, IOException {
        log.info("getting feature info");
        int i = getI(request);
        int j = getJ(request);
        WmsRequest wmsRequest = new WmsRequest(request);
        response.setContentType(wmsRequest.getInfoFormat());
        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        Map<String, String> infos = layerManager.getInfos(new Date(), wmsRequest, new Point(i, j),
                wmsRequest.getInfoFormat());

        for (Entry<String, String> entry : infos.entrySet()) {
            log.debug(entry.getKey() + "=" + entry.getValue());
            bos.write(("<p>" + entry.getKey() + "</p>").getBytes());
            bos.write(entry.getValue().getBytes());
        }
        bos.close();
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
