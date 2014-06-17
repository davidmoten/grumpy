package com.github.davidmoten.grumpy.wms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caches images keyed on the {@link WmsRequest}.
 * 
 * @author dxm
 * 
 */
public class ImageCache {

    private static Logger log = LoggerFactory.getLogger(ImageCache.class);

    private static int DEFAULT_SIZE = 250;// 50MB at 200K per image

    /**
     * Records the keys and the order they went into the cache so we can trim
     * the cache if needed.
     */
    private volatile List<String> keys = new ArrayList<String>();

    private volatile Set<String> layers = new HashSet<String>();

    private volatile Map<String, byte[]> cache = new ConcurrentHashMap<String, byte[]>();

    private final int maxSize;

    /**
     * Constructor.
     */
    public ImageCache() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param size
     *            the maximum number of elements in the cache
     */
    public ImageCache(int size) {
        this.maxSize = size;
    }

    /**
     * Factory method. Returns a new {@link ImageCache} of given maximum size.
     * 
     * @param size
     * @return
     */
    public static ImageCache create(int size) {
        return new ImageCache(size);
    }

    /**
     * Clears the cache for the given layer name.
     * 
     * @param layerName
     */
    // TOOD improve this
    public void clear(String layerName) {
        synchronized (this) {
            log.info("clearing cache for layer " + layerName);
            for (String key : cache.keySet()) {
                if (key.contains(layerName))
                    remove(key);
            }
        }
    }

    private void remove(String key) {
        cache.remove(key);
        log.info("removed cache entry " + key);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        synchronized (this) {
            cache.clear();
        }
    }

    /**
     * Enables/disables a layer for caching.
     * 
     * @param layerName
     * @param enabled
     */
    public void setEnabled(String layerName, boolean enabled) {
        synchronized (this) {
            if (enabled)
                layers.add(layerName);
            else
                layers.remove(layerName);
        }
    }

    private static String getKey(WmsRequest request) {
        StringBuffer s = new StringBuffer();
        for (String name : request.getParameterNames())
            // make sure we exclude the _OLSALT parameter which changes with
            // every request
            if (!name.startsWith("_"))
                add(s, name, request.getParam(name));
        return s.toString();
    }

    private static void add(StringBuffer s, String name, Object value) {
        s.append(name);
        s.append("=");
        s.append(String.valueOf(value));
        s.append(";");
    }

    /**
     * Get the bytes of the image returned by a {@link WmsRequest}. Returns null
     * if no corresponding image exists in the cache.
     * 
     * @param request
     * @return
     */
    public byte[] get(WmsRequest request) {
        synchronized (this) {
            log.info("cache size=" + cache.size());
            return cache.get(getKey(request));
        }
    }

    /**
     * Sets the cached image for the request.
     * 
     * @param request
     * @param image
     */
    public synchronized void put(WmsRequest request, byte[] image) {
        synchronized (this) {
            String key = getKey(request);
            // make sure it's the last on the list of keys so won't be dropped
            // from cache
            keys.remove(key);
            keys.add(key);
            if (keys.size() > maxSize)
                remove(keys.get(0));
            if (maxSize > 0 && layers.containsAll(request.getLayers())) {
                cache.put(key, image);
                log.info("cached image with key=" + key);
            }
        }
    }

    /**
     * Flags the given layer as a layer to be cached.
     * 
     * @param layerName
     * @return
     */
    public ImageCache add(String layerName) {
        setEnabled(layerName, true);
        return this;
    }

}