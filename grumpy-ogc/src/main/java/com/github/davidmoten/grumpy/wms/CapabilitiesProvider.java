package com.github.davidmoten.grumpy.wms;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides the response to the WMS GetCapabilities request.
 */
public interface CapabilitiesProvider {
    String getCapabilities(HttpServletRequest request);
}
