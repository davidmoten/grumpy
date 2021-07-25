package com.github.davidmoten.grumpy.wms;

import java.util.ArrayList;
import java.util.List;

public final class CapabilitiesLayer {
    
    private final String name;
    private final String title;
    private final boolean queryable;
    private final boolean opaque;
    private final List<String> crs;
    private final List<String> styles;
    private final List<CapabilitiesLayer> layers;

    private CapabilitiesLayer(String name, String title, boolean queryable, boolean opaque,
            List<String> crs, List<String> styles, List<CapabilitiesLayer> layers) {
        this.name = name;
        this.title = title;
        this.crs = crs;
        this.styles = styles;
        this.layers = layers;
        this.queryable = queryable;
        this.opaque = opaque;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public boolean isOpaque() {
        return opaque;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getCrs() {
        return crs;
    }

    public List<String> getStyles() {
        return styles;
    }

    public List<CapabilitiesLayer> getLayers() {
        return layers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(LayerFeatures layer) {
        return new Builder().layerFeatures(layer);
    }

    public static class Builder {

        private String name;
        private String title;
        private List<String> crs = new ArrayList<String>();
        private List<String> styles = new ArrayList<String>();
        private List<CapabilitiesLayer> layers = new ArrayList<CapabilitiesLayer>();
        private Boolean queryable = null;
        private boolean opaque = true;
        private LayerFeatures layerFeatures;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder queryable(boolean value) {
            this.queryable = value;
            return this;
        }

        public Builder opaque(boolean value) {
            this.opaque = value;
            return this;
        }

        public Builder queryable() {
            return queryable(true);
        }

        public Builder opaque() {
            return opaque(true);
        }

        public Builder crs(List<String> crs) {
            this.crs = crs;
            return this;
        }

        public Builder crs(String crs) {
            this.crs.add(crs);
            return this;
        }

        public Builder styles(List<String> styles) {
            this.styles = styles;
            return this;
        }

        public Builder style(String style) {
            this.styles.add(style);
            return this;
        }

        public Builder layerFeatures(LayerFeatures layerFeatures) {
            this.layerFeatures = layerFeatures;
            return this;
        }

        public Builder layers(List<CapabilitiesLayer> layers) {
            this.layers = layers;
            return this;
        }

        public Builder layer(CapabilitiesLayer layer) {
            this.layers.add(layer);
            return this;
        }

        public CapabilitiesLayer build() {
            if (layerFeatures != null) {
                styles.addAll(layerFeatures.getStyles());
                crs.addAll(layerFeatures.getCrs());
                if (name == null)
                    name = layerFeatures.getName();
                if (queryable == null)
                    queryable = layerFeatures.isQueryable();
            }
            if (title == null)
                title = name;
            if (queryable == null)
                queryable = false;
            return new CapabilitiesLayer(name, title, queryable, opaque, crs, styles, layers);
        }
    }

}
