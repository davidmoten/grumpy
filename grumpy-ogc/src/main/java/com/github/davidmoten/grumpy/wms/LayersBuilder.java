package com.github.davidmoten.grumpy.wms;

import java.util.HashMap;
import java.util.Map;

public class LayersBuilder {

	public static LayersBuilder builder() {
		return new LayersBuilder();
	}

	final Map<String, Layer> map = new HashMap<String, Layer>();

	private LayersBuilder() {
		// private constructor
	}

	public LayersBuilder add(String name, Layer layer) {
		map.put(name, layer);
		return this;
	}

	public Layers build() {
		// make defensive copy
		final Map<String, Layer> m = new HashMap<String, Layer>(map);
		return new Layers() {

			@Override
			public Layer getLayer(String layerName) {
				Layer layer = m.get(layerName);
				// null return handled by LayerManager with a warning in the log
				return layer;
			}

		};
	}
}