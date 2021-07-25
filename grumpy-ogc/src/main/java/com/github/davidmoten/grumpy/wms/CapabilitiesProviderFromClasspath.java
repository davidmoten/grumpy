package com.github.davidmoten.grumpy.wms;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

public final class CapabilitiesProviderFromClasspath implements CapabilitiesProvider {

    private final String resource;

    public CapabilitiesProviderFromClasspath(String resource) {
        this.resource = resource;
    }

    public static CapabilitiesProvider fromClasspath(String resource) {
        return new CapabilitiesProviderFromClasspath(resource);
    }

    @Override
    public String getCapabilities(HttpServletRequest request) {
        try (InputStream in = CapabilitiesProviderFromClasspath.class
                .getResourceAsStream(resource)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
