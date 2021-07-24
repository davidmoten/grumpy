package com.github.davidmoten.grumpy.wms;

public interface HasLayerFeatures {
    
    /**
     * Returns features about the WMS layer including styles, supported CRS and
     * the default name of the layer.
     * 
     * @return
     */
    LayerFeatures getFeatures();
    
}
