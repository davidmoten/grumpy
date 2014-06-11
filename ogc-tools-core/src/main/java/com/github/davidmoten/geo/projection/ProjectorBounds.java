package com.github.davidmoten.geo.projection;

public class ProjectorBounds {
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private final String srs;

	public String getSrs() {
		return srs;
	}

	public ProjectorBounds(String srs, double minX, double minY, double maxX,
			double maxY) {
		super();
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.srs = srs;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	@Override
	public String toString() {
		return "ProjectorBounds [srs=" + srs + ", minX=" + minX + ", minY="
				+ minY + ", maxX=" + maxX + ", maxY=" + maxY + "]";
	}

}
