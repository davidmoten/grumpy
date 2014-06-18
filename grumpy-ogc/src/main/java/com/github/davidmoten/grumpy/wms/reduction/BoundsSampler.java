package com.github.davidmoten.grumpy.wms.reduction;

import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.util.Bounds;

public interface BoundsSampler {

    List<Position> sample(Bounds bounds);

}
