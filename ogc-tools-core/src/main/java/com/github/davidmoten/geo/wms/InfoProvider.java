package com.github.davidmoten.geo.wms;

import java.awt.Point;
import java.util.Date;

public interface InfoProvider {
	public static enum Format {
		XML("text/xml"), GML("application/vnd.ogc.gml"), HTML("text/html");

		private String mimeType;

		private Format(String mimeType) {
			this.mimeType = mimeType;
		}

		public static Format decode(String mimeType) {
			for (Format format : Format.values()) {
				if (format.mimeType.equals(mimeType))
					return format;
			}
			throw new Error(Format.class.getName()
					+ " does not exist with mimeType: " + mimeType);
		}

		@Override
		public String toString() {
			return mimeType;
		}
	}

	String getInfo(Date time, WmsRequest request, Point point, Format format);
}
