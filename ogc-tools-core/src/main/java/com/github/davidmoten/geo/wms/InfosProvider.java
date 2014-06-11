package com.github.davidmoten.geo.wms;

import java.awt.Point;
import java.util.Date;
import java.util.Map;

import com.github.davidmoten.geo.wms.InfoProvider.Format;

public interface InfosProvider {
	Map<String, String> getInfos(Date time, WmsRequest request, Point point,
			Format format);
}
