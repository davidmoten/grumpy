package com.github.davidmoten.grumpy.wms;

import javax.servlet.http.HttpServletRequest;

public class CapabilitiesProviderEmpty implements CapabilitiesProvider {

    private final CapabilitiesProvider provider = CapabilitiesProviderFromClasspath
            .fromClasspath("/wms-capabilities-empty.xml");

    @Override
    public String getCapabilities(HttpServletRequest request) {
        return provider.getCapabilities(request);
    }

}
