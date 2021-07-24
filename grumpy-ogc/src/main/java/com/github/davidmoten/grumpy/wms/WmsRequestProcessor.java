package com.github.davidmoten.grumpy.wms;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

/**
 * Processes a WMS {@link HttpServletRequest} and returns a
 * {@link HttpServletResponse}.
 */
public class WmsRequestProcessor {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WmsRequestProcessor.class);

    private final ImageCache imageCache;

    private final LayerManager layerManager;

    private final ImageWriter imageWriter;

    /**
     * Constructor.
     * 
     * @param layers
     * @param imageCache
     * @param imageWriter
     */
    public WmsRequestProcessor(Layers layers, ImageCache imageCache, ImageWriter imageWriter) {
        this.imageCache = imageCache;
        this.imageWriter = imageWriter;
        this.layerManager = new LayerManager(layers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ImageCache imageCache = new ImageCache();
        private Layers layers;
        private final LayersBuilder layersBuilder = LayersBuilder.builder();
        private final List<String> layersToCache = new ArrayList<String>();
        private ImageWriter imageWriter = new ImageWriterDefault();
        private Integer imageCacheSize;

        private Builder() {
        }

        public Builder imageCache(int size) {
            this.imageCacheSize = size;
            return this;
        }
        
        public Builder addCachedLayer(Layer layer) {
            return addCachedLayer(layer.getFeatures().getName(), layer);
        }
        
        public Builder addCachedLayer(String name, Layer layer) {
            return addLayer(name, layer, true);
        }
        
        public Builder addLayer(Layer layer) {
            return addLayer(layer.getFeatures().getName(), layer);
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

        public WmsRequestProcessor build() {
            if (imageCacheSize != null)
                imageCache = new ImageCache(imageCacheSize);
            for (String layer : layersToCache)
                imageCache.add(layer);
            if (layers == null)
                layers = layersBuilder.build();
            return new WmsRequestProcessor(layers, imageCache, imageWriter);
        }
    }

    public void writeImage(WmsRequest wmsRequest, boolean cacheImage, OutputStream out)
            throws IOException {
        final byte[] bytes;
        if (cacheImage) {
            // check the cache for the bytes of the image converted to the
            // appropriate format. Note that the critical bottleneck is
            // ImageIO.write rather than the layerManager.getImage call
            bytes = imageCache.get(wmsRequest);
        } else {
            bytes = null;
        }
        final byte[] result;
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
            String imageType = wmsRequest.getFormat()
                    .substring(wmsRequest.getFormat().indexOf('/') + 1);
            // This call is slow!!
            long t = System.currentTimeMillis();
            imageWriter.writeImage(image, byteOs, imageType);
            log.info("ImageIoWriteTimeMs=" + (System.currentTimeMillis() - t));
            result = byteOs.toByteArray();
            imageCache.put(wmsRequest, result);
        } else {
            result = bytes;
            log.info("obtained image from cache for layers " + wmsRequest.getLayers());
        }

        log.info("writing image to http output stream for layers " + wmsRequest.getLayers());
        out.write(result);
        out.flush();
        log.info("imageSizeK=" + new DecimalFormat("0.000").format(result.length / 1000.0)
                + " for layers " + wmsRequest.getLayers());
    }

    public void writeFeatureInfo(int i, int j, WmsRequest wmsRequest, OutputStream out)
            throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(out);
        Map<String, String> infos = layerManager.getInfos(new Date(), wmsRequest, new Point(i, j),
                wmsRequest.getInfoFormat());
        for (Entry<String, String> entry : infos.entrySet()) {
            log.debug(entry.getKey() + "=" + entry.getValue());
            bos.write(("<p>" + entry.getKey() + "</p>").getBytes());
            bos.write(entry.getValue().getBytes());
        }
        bos.flush();
    }

}
