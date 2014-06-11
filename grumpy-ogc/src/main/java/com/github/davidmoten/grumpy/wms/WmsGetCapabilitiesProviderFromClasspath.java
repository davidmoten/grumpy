package com.github.davidmoten.grumpy.wms;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

public class WmsGetCapabilitiesProviderFromClasspath implements WmsGetCapabilitiesProvider {

    private final String resource;

    public WmsGetCapabilitiesProviderFromClasspath(String resource) {
        this.resource = resource;
    }

    public static WmsGetCapabilitiesProvider fromClasspath(String resource) {
        return new WmsGetCapabilitiesProviderFromClasspath(resource);
    }

    @Override
    public String getCapabilities(HttpServletRequest request) {
        try {
            return IOUtils.toString(WmsGetCapabilitiesProviderFromClasspath.class.getResourceAsStream(resource));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
