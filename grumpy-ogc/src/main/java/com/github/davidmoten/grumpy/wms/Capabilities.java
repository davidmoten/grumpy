package com.github.davidmoten.grumpy.wms;

import java.util.ArrayList;
import java.util.List;

public class Capabilities {
    private final String serviceName;
    private final String serviceTitle;
    private final String serviceAbstract;
    private final Integer serviceMaxWidth;
    private final Integer serviceMaxHeight;
    private final List<String> imageFormats;
    private final List<String> infoFormats;
    private final List<CapabilitiesLayer> layers;

    private Capabilities(String serviceName, String serviceTitle, String serviceAbstract,
            Integer serviceMaxWidth, Integer serviceMaxHeight, List<String> imageFormats,
            List<String> infoFormats, List<CapabilitiesLayer> layers) {
        if (serviceName == null)
            throw new RuntimeException("serviceName cannot be null");
        this.serviceName = serviceName;
        this.serviceTitle = serviceTitle;
        this.serviceAbstract = serviceAbstract;
        this.serviceMaxWidth = serviceMaxWidth;
        this.serviceMaxHeight = serviceMaxHeight;
        this.imageFormats = imageFormats;
        this.infoFormats = infoFormats;
        this.layers = layers;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceAbstract() {
        return serviceAbstract;
    }

    public Integer getServiceMaxWidth() {
        return serviceMaxWidth;
    }

    public Integer getServiceMaxHeight() {
        return serviceMaxHeight;
    }

    public List<String> getImageFormats() {
        return imageFormats;
    }

    public List<String> getInfoFormats() {
        return infoFormats;
    }

    public List<CapabilitiesLayer> getLayers() {
        return layers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String serviceName;
        private String serviceTitle;
        private String serviceAbstract;
        private Integer serviceMaxWidth = 2000;
        private Integer serviceMaxHeight = 2000;
        private List<String> imageFormats = new ArrayList<String>();
        private List<String> infoFormats = new ArrayList<String>();
        private List<CapabilitiesLayer> layers = new ArrayList<CapabilitiesLayer>();

        private Builder() {
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder serviceTitle(String serviceTitle) {
            this.serviceTitle = serviceTitle;
            return this;
        }

        public Builder serviceAbstract(String serviceAbstract) {
            this.serviceAbstract = serviceAbstract;
            return this;
        }

        public Builder serviceMaxWidth(Integer serviceMaxWidth) {
            this.serviceMaxWidth = serviceMaxWidth;
            return this;
        }

        public Builder serviceMaxHeight(Integer serviceMaxHeight) {
            this.serviceMaxHeight = serviceMaxHeight;
            return this;
        }

        public Builder imageFormats(List<String> imageFormats) {
            this.imageFormats = imageFormats;
            return this;
        }

        public Builder imageFormat(String imageFormat) {
            this.imageFormats.add(imageFormat);
            return this;
        }

        public Builder infoFormats(List<String> infoFormats) {
            this.infoFormats = infoFormats;
            return this;
        }

        public Builder infoFormat(String infoFormat) {
            this.infoFormats.add(infoFormat);
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

        public Builder layerFeatures(LayerFeatures layerFeatures) {
            this.layers.add(CapabilitiesLayer.from(layerFeatures).build());
            return this;
        }
        
        public Builder layerFeatures(Layer layer) {
            return layerFeatures(layer.getFeatures());
        }

        public Capabilities build() {
            if (serviceTitle == null)
                serviceTitle = serviceName;
            if (serviceAbstract == null)
                serviceAbstract = serviceName;
            return new Capabilities(serviceName, serviceTitle, serviceAbstract, serviceMaxWidth,
                    serviceMaxHeight, imageFormats, infoFormats, layers);
        }
    }

}
