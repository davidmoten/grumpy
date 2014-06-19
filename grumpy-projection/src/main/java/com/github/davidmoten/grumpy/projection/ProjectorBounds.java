package com.github.davidmoten.grumpy.projection;

import java.util.ArrayList;
import java.util.List;

public class ProjectorBounds {
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    private final String srs;

    public String getSrs() {
        return srs;
    }

    public ProjectorBounds(String srs, double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.srs = srs;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getSizeX() {
        return this.maxX - this.minX;
    }

    public double getSizeY() {
        return maxY - minY;
    }

    public List<ProjectorBounds> splitHorizontally() {
        List<ProjectorBounds> list = new ArrayList<ProjectorBounds>();
        list.add(new ProjectorBounds(srs, minX, minY, minX + getSizeX() / 2, maxY));
        list.add(new ProjectorBounds(srs, minX + getSizeX() / 2, minY, maxX, maxY));
        return list;
    }

    public List<ProjectorBounds> splitVertically() {
        List<ProjectorBounds> list = new ArrayList<ProjectorBounds>();
        list.add(new ProjectorBounds(srs, minX, minY, maxX, minY + getSizeY() / 2));
        list.add(new ProjectorBounds(srs, minX, minY + getSizeY() / 2, maxX, maxY));
        return list;
    }

    public List<ProjectorBounds> quarter() {
        List<ProjectorBounds> list = new ArrayList<ProjectorBounds>();
        for (ProjectorBounds b : splitHorizontally())
            list.addAll(b.splitVertically());
        return list;
    }

    @Override
    public String toString() {
        return "ProjectorBounds [srs=" + srs + ", minX=" + minX + ", minY=" + minY + ", maxX="
                + maxX + ", maxY=" + maxY + "]";
    }

}
