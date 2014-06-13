package com.github.davidmoten.grumpy.wms;

import static com.github.davidmoten.grumpy.core.Position.position;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Test;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.grumpy.projection.ProjectorTarget;

public class RendererUtilTest {

    @Test
    public void testGetPath() {
        ProjectorTarget target = new ProjectorTarget(300, 200);
        ProjectorBounds bounds = new ProjectorBounds("EPSG:3857", 18924313.4349, -4865942.2795, -18924313.4349,
                -3503549.8435);
        Projector projector = new Projector(bounds, target);
        List<Position> list = RendererUtil.joinPixels(projector, 20, asList(position(-35, 175), position(-35, -175)));
        System.out.println(list);
    }
}
