package com.github.davidmoten.geo.wms;

public enum InfoFormat {
	XML("text/xml"), GML("application/vnd.ogc.gml"), HTML("text/html");

	private final String mimeType;

	private InfoFormat(String mimeType) {
		this.mimeType = mimeType;
	}

	public static InfoFormat decode(String mimeType) {
		for (InfoFormat format : InfoFormat.values()) {
			if (format.mimeType.equals(mimeType))
				return format;
		}
		throw new Error(InfoFormat.class.getName()
				+ " does not exist with mimeType: " + mimeType);
	}

	@Override
	public String toString() {
		return mimeType;
	}
}