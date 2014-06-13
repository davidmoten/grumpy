package com.github.davidmoten.grumpy.wms;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.projection.ProjectorTarget;

public class WmsUtil {

	public static List<Color> getColorFromStyles(List<String> styles) {
		List<Color> colors = new ArrayList<Color>();
		for (String style : styles) {
			Field field;
			try {
				field = Color.class.getField(style);
				Color color = (Color) field.get(null);
				colors.add(color);
			} catch (SecurityException e) {
				// ignore
			} catch (NoSuchFieldException e) {
				// ignore
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (IllegalAccessException e) {
				// ignore;
			}
		}
		return colors;
	}

	public static Projector getProjector(WmsRequest request) {
		ProjectorTarget target = new ProjectorTarget(request.getWidth(),
				request.getHeight());
		return new Projector(request.getBounds(), target);
	}

}
