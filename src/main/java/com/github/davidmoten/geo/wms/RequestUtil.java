package com.github.davidmoten.geo.wms;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.github.davidmoten.geo.wms.MissingMandatoryParameterException;

public class RequestUtil {

	private static final String COMMA = ",";

	public static List<String> getList(HttpServletRequest request,
			String parameter, boolean mandatory) {
		String[] items = new String[] {};
		if (request.getParameter(parameter) != null)
			items = request.getParameter(parameter).split(COMMA);
		return Arrays.asList(items);
	}

	public static String getParameter(HttpServletRequest request,
			String parameter, boolean mandatory)
			throws MissingMandatoryParameterException {

		String s = request.getParameter(parameter);
		if (s == null && mandatory)
			throw new MissingMandatoryParameterException(parameter);
		return s;
	}

}
