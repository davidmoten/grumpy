package com.github.davidmoten.geo.wms;

public class ProjectorTarget {
	@Override
	public String toString() {
		return "ProjectorTarget [width=" + width + ", height=" + height + "]";
	}

	private int width;
	private int height;

	public ProjectorTarget(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
