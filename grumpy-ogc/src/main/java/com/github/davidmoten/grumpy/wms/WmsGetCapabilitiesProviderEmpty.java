package com.github.davidmoten.grumpy.wms;

import javax.servlet.http.HttpServletRequest;

public class WmsGetCapabilitiesProviderEmpty implements WmsGetCapabilitiesProvider {

    private final WmsGetCapabilitiesProvider provider = WmsGetCapabilitiesProviderFromClasspath
            .fromClasspath("/wms-capabilities-empty.xml");

    @Override
    public String getCapabilities(HttpServletRequest request) {
        return provider.getCapabilities(request);
    }

}
