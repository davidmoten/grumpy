package com.github.davidmoten.geo.wms;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayerManager {

    private static Logger log = LoggerFactory.getLogger(LayerManager.class);

    private final Layers layers;

    private final ExecutorService executor;

    public LayerManager(Layers layers) {
        this.layers = layers;
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String name : gEnv.getAvailableFontFamilyNames())
            log.debug(name);
        log.info("constructed");
        executor = Executors.newFixedThreadPool(30);
    }

    private static final ImageObserver noActionImageObserver = new ImageObserver() {
        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }
    };

    private static final boolean DRAW_IN_PARALLEL = true;

    public BufferedImage getImage(WmsRequest request) {
        MyGraphics graphics = createGraphics(request);
        Graphics2D g = graphics.graphics;

        log.info("painting image with layers");
        // paint the image
        paintImage(request, g);
        // release resources
        g.dispose();
        log.info("image finished");
        return graphics.image;
    }

    private static class MyGraphics {
        public MyGraphics(BufferedImage image, Graphics2D graphics) {
            super();
            this.image = image;
            this.graphics = graphics;
        }

        BufferedImage image;
        Graphics2D graphics;
    }

    private void prepareGraphics(Graphics2D g) {
        RendererUtil.useAntialiasing(g);
    }

    private MyGraphics createGraphics(WmsRequest request) {
        log.info("creating buffered image");
        BufferedImage image = new BufferedImage(request.getWidth(), request.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        // set rendering options
        prepareGraphics(g);

        // paint the background with transparency as required
        paintBackground(request, g);

        log.info("image ready");
        return new MyGraphics(image, g);
    }

    private void paintImage(final WmsRequest request, Graphics2D g) {

        log.info("painting layers " + request.getLayers());

        if (DRAW_IN_PARALLEL) {
            paintImageParallel(request, g);
        } else {
            // using only a single g2d seems to help with IE8 png transparency
            // bug (fixed in IE9).
            for (final String layerName : request.getLayers()) {
                paintLayer(g, layerName, layers, request);
            }
        }
    }

    private void paintImageParallel(WmsRequest request, Graphics2D g) {
        // create future for each worker (layer)
        List<Future<BufferedImage>> futures = new ArrayList<Future<BufferedImage>>();

        for (final String layerName : request.getLayers()) {
            // create a worker for layer
            Callable<BufferedImage> worker = createWorker(layers, layerName, request);
            // start the worker
            Future<BufferedImage> submit = executor.submit(worker);
            // record the worker in a list so we can paint the images in
            // order later
            futures.add(submit);
        }
        // wait for each image to complete in turn then draw it to the
        // everything graphics object
        for (Future<BufferedImage> future : futures) {
            drawImage(g, future);
        }
    }

    private Callable<BufferedImage> createWorker(final Layers layers, final String layerName, final WmsRequest request) {
        return new Callable<BufferedImage>() {
            @Override
            public BufferedImage call() throws Exception {
                final MyGraphics graphics = createGraphics(request);
                paintLayer(graphics.graphics, layerName, layers, request);
                return graphics.image;
            }
        };
    }

    private static void paintLayer(Graphics2D g, String layerName, Layers layers, WmsRequest request) {
        log.info("painting " + layerName);
        final Layer layer = layers.getLayer(layerName);
        if (layer != null) {
            layer.render(g, request);
            log.info("finished painting " + layerName);
        } else
            log.warn("no paintImage implementation for layer: " + layerName);
    }

    private void drawImage(Graphics2D g, Future<BufferedImage> future) {
        try {
            BufferedImage image = future.get();
            g.drawImage(image, 0, 0, noActionImageObserver);
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void paintBackground(WmsRequest request, Graphics2D g) {
        g.setColor(request.getBackgroundColor());
        g.setBackground(request.getBackgroundColor());
        if (request.isTransparent())
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        else
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
        g.fillRect(0, 0, 100, 100);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public Map<String, String> getInfos(Date time, WmsRequest request, Point point, InfoFormat format) {
        Map<String, String> map = new HashMap<String, String>();
        for (String layerName : request.getLayers()) {
            Layer layer = layers.getLayer(layerName);
            if (layer != null) {
                String info = layer.getInfo(time, request, point, format);
                if (info != null)
                    map.put(layerName, info);
            } else
                log.warn("no getInfo implementation for layer: " + layerName);
        }
        return map;
    }

}
