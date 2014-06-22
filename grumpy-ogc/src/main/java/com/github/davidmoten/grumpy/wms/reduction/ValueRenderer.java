package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.github.davidmoten.grumpy.projection.Projector;

public interface ValueRenderer<T> {
    void render(Graphics2D g, Projector projector, Rectangle region, final T t);
}
