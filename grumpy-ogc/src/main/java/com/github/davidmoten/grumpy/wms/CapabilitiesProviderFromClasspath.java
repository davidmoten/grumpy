package com.github.davidmoten.grumpy.wms;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

public class CapabilitiesProviderFromClasspath implements CapabilitiesProvider {

    private final String resource;

    public CapabilitiesProviderFromClasspath(String resource) {
        this.resource = resource;
    }

    public static CapabilitiesProvider fromClasspath(String resource) {
        return new CapabilitiesProviderFromClasspath(resource);
    }

    @Override
    public String getCapabilities(HttpServletRequest request) {
        try {
            return IOUtils.toString(CapabilitiesProviderFromClasspath.class
                    .getResourceAsStream(resource));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
