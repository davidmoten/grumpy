package com.github.davidmoten.grumpy.wms;

import java.util.ArrayList;
import java.util.List;

public class LayerFeatures {
    private final List<String> styles;
    private final List<String> crs;
    private final String name;
    private final boolean queryable;

    private LayerFeatures(List<String> styles, List<String> crs, String name, boolean queryable) {
        if (name == null)
            throw new NullPointerException("name cannot be null");
        this.styles = styles;
        this.crs = crs;
        this.name = name;
        this.queryable = queryable;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public List<String> getStyles() {
        return styles;
    }

    public List<String> getCrs() {
        return crs;
    }

    public String getName() {
        return name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private List<String> styles = new ArrayList<>();
        private List<String> crs = new ArrayList<>();
        private boolean queryable = false;

        private Builder() {
        }

        public Builder styles(List<String> styles) {
            this.styles = styles;
            return this;
        }

        public Builder crs(List<String> crs) {
            this.crs = crs;
            return this;
        }

        public Builder style(String style) {
            this.styles.add(style);
            return this;
        }

        public Builder crs(String crs) {
            this.crs.add(crs);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder queryable(boolean value) {
            this.queryable = value;
            return this;
        }

        public LayerFeatures build() {
            return new LayerFeatures(styles, crs, name, queryable);
        }

        public Builder queryable() {
            return queryable(true);
        }
    }

}
