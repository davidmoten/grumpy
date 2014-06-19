package com.github.davidmoten.grumpy.projection;

public class ProjectorTarget {

    private final int width;
    private final int height;

    public ProjectorTarget(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "ProjectorTarget [width=" + width + ", height=" + height + "]";
    }
}
