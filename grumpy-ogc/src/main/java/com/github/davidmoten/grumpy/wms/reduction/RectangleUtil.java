package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class RectangleUtil {

    public static List<Point> corners(Rectangle region) {
        List<Point> list = new ArrayList<Point>();
        list.add(new Point(region.x, region.y));
        list.add(new Point(region.x, region.y + region.height));
        list.add(new Point(region.x + region.width, region.y));
        list.add(new Point(region.x + region.width, region.y + region.height));
        return list;
    }

    public static List<Rectangle> splitHorizontally(Rectangle region) {
        List<Rectangle> list = new ArrayList<Rectangle>();
        int halfWidth = region.width / 2;
        list.add(new Rectangle(region.x, region.y, halfWidth, region.height));
        list.add(new Rectangle(region.x + halfWidth, region.y, region.width - halfWidth,
                region.height));
        return list;
    }

    public static List<Rectangle> splitVertically(Rectangle region) {
        List<Rectangle> list = new ArrayList<Rectangle>();
        int halfHeight = region.height / 2;
        list.add(new Rectangle(region.x, region.y, region.width, halfHeight));
        list.add(new Rectangle(region.x, region.y + halfHeight, region.width, region.height
                - halfHeight));
        return list;
    }

    public static List<Rectangle> quarter(Rectangle region) {
        List<Rectangle> list = new ArrayList<Rectangle>();
        for (Rectangle r : splitHorizontally(region))
            list.addAll(splitVertically(r));
        return list;
    }

}
