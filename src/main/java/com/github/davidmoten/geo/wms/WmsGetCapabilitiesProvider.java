package com.github.davidmoten.geo.wms;

import javax.servlet.http.HttpServletRequest;

public interface WmsGetCapabilitiesProvider {
	// doesn't do much but it is useful to have an explicit class for this for
	// specification to guice injector module
	String getCapabilities(HttpServletRequest request);
}
