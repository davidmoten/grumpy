package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class RectangleUtil {

    public static List<Point> corners(Rectangle region) {
        List<Point> list = new ArrayList<>();
        list.add(new Point(region.x, region.y));
        list.add(new Point(region.x, region.y + region.height));
        list.add(new Point(region.x + region.width, region.y));
        list.add(new Point(region.x + region.width, region.y + region.height));
        return list;
    }

}
