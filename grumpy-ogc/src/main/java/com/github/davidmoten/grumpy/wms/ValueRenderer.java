package com.github.davidmoten.grumpy.wms;

import java.awt.Graphics2D;

import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.util.Bounds;

public interface ValueRenderer<T> {
    void render(Graphics2D g, Projector projector, Bounds geoBounds, final T t);
}
