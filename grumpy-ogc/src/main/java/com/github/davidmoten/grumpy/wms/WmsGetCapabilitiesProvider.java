package com.github.davidmoten.grumpy.wms;

import javax.servlet.http.HttpServletRequest;

public interface WmsGetCapabilitiesProvider {
	String getCapabilities(HttpServletRequest request);
}
