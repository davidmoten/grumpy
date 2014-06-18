package com.github.davidmoten.grumpy.wms.reduction;

import static com.github.davidmoten.grumpy.core.Position.position;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.util.Bounds;

public class BoundsSamplerCorners implements BoundsSampler {

    @Override
    public List<Position> sample(Bounds bounds) {
        List<Position> list = new ArrayList<>();
        list.add(position(bounds.getMin().lat(), bounds.getMin().lon()));
        list.add(position(bounds.getMin().lat(), bounds.getMax().lon()));
        list.add(position(bounds.getMax().lat(), bounds.getMax().lon()));
        list.add(position(bounds.getMax().lat(), bounds.getMin().lon()));
        return list;
    }

}
