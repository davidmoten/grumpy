package com.github.davidmoten.geo.wms;

import static com.github.davidmoten.geo.wms.RequestUtil.getList;
import static com.github.davidmoten.geo.wms.RequestUtil.getParameter;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;

import com.github.davidmoten.geo.projection.FeatureUtil;
import com.github.davidmoten.geo.projection.ProjectorBounds;

public class WmsRequest {

    private static final String DATETIME_FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss";

    private List<String> layers = new ArrayList<String>();
    private List<String> queryLayers = new ArrayList<String>();
    private List<String> styles = new ArrayList<String>();
    private final ProjectorBounds bounds;
    private final String format;
    private final int width;
    private final int height;
    private final boolean transparent;
    private final Color backgroundColor;
    private final String version;
    private final String infoFormat;
    private final Map<String, String> parameters;
    private final String url;

    private final Date time;

    public WmsRequest(List<String> layers, List<String> queryLayers, List<String> styles, ProjectorBounds bounds,
            String format, int width, int height, boolean transparent, Color backgroundColor, String version,
            String infoFormat, Map<String, String> parameters, String url, Date time) {
        super();
        this.layers = layers;
        this.queryLayers = queryLayers;
        this.styles = styles;
        this.bounds = bounds;
        this.format = format;
        this.width = width;
        this.height = height;
        this.transparent = transparent;
        this.backgroundColor = backgroundColor;
        this.version = version;
        this.infoFormat = infoFormat;
        this.parameters = parameters;
        this.url = url;
        this.time = time;
    }

    public WmsRequest(HttpServletRequest request) throws MissingMandatoryParameterException {

        this(getList(request, "LAYERS", true), getList(request, "QUERY_LAYERS", false),
                getList(request, "STYLES", true), getBounds(getCrs(request), getParameter(request, "BBOX", true),
                        getVersion(request)), getParameter(request, "FORMAT", true), Integer.parseInt(getParameter(
                        request, "WIDTH", true)), Integer.parseInt(getParameter(request, "HEIGHT", true)),
                isTransparent(request), getBackgroundColor(request), getVersion(request), getParameter(request,
                        "INFO_FORMAT", false), getParameters(request), getUrl(request), getTime(request));
    }

    private static Date getTime(HttpServletRequest request) throws MissingMandatoryParameterException {
        String s = getParameter(request, "TIME", false);
        if (s != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_ISO_8601);
            try {
                return sdf.parse(s);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else
            return null;
    }

    private static String getUrl(HttpServletRequest request) {
        return request.getRequestURL() + "?" + request.getQueryString();
    }

    public String getUrl() {
        return url;
    }

    private static Map<String, String> getParameters(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            map.put(name, request.getParameter(name));
        }
        return map;
    }

    public WmsRequest modifySize(int w, int h) {
        return new WmsRequest(layers, queryLayers, styles, bounds, format, w, h, transparent, backgroundColor, version,
                infoFormat, parameters, url, time);
    }

    public String getVersion() {
        return version;
    }

    public WmsRequest modifyStyles(List<String> newStyles) {
        return new WmsRequest(layers, queryLayers, newStyles, bounds, format, width, height, transparent,
                backgroundColor, version, infoFormat, parameters, url, time);
    }

    public WmsRequest modifyBounds(ProjectorBounds newBounds) {
        return new WmsRequest(layers, queryLayers, styles, newBounds, format, width, height, transparent,
                backgroundColor, version, infoFormat, parameters, url, time);
    }

    public List<String> getQueryLayers() {
        if (queryLayers.size() == 0)
            return layers;
        else
            return queryLayers;
    }

    public String getParam(String name) {
        return parameters.get(name);
    }

    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    private static boolean isTransparent(HttpServletRequest request) throws MissingMandatoryParameterException {
        return "true".equalsIgnoreCase(getParameter(request, "TRANSPARENT", false));
    }

    private static String getVersion(HttpServletRequest request) throws MissingMandatoryParameterException {
        return getParameter(request, "VERSION", false);
    }

    private static Color getBackgroundColor(HttpServletRequest request) throws MissingMandatoryParameterException {
        String s = getParameter(request, "BGCOLOR", false);
        if (s != null)
            return new Color(Integer.valueOf(s.substring(2), 16));
        else
            return Color.white;
    }

    private static String getCrs(HttpServletRequest request) throws MissingMandatoryParameterException {
        String srs = RequestUtil.getParameter(request, "SRS", false);
        String crs = ObjectUtils.firstNonNull(RequestUtil.getParameter(request, "CRS", false), srs,
                FeatureUtil.EPSG_4326);
        return crs;
    }

    @Override
    public String toString() {
        return "WmsRequest [layers=" + layers + ", queryLayers=" + queryLayers + ", styles=" + styles + ", bounds="
                + bounds + ", format=" + format + ", width=" + width + ", height=" + height + ", transparent="
                + transparent + ", backgroundColor=" + backgroundColor + ", version=" + version + ", infoFormat="
                + infoFormat + ", parameters=" + parameters + ", url=" + url + "]";
    }

    /**
     * Expected to be in lat long (EPSG:4326).
     * 
     * @param s
     * @param version
     * @param srs
     * @return
     */
    private static ProjectorBounds getBounds(String crs, String s, String version) {
        if (s == null)
            return null;
        String[] items = s.split(",");
        // this is the order for EPSG:4326
        double minLat = Double.parseDouble(items[0]);
        double minLong = Double.parseDouble(items[1]);
        double maxLat = Double.parseDouble(items[2]);
        double maxLong = Double.parseDouble(items[3]);

        if ("1.1.1".equals(version)) {
            minLong = Double.parseDouble(items[0]);
            minLat = Double.parseDouble(items[1]);
            maxLong = Double.parseDouble(items[2]);
            maxLat = Double.parseDouble(items[3]);
        }
        ProjectorBounds bounds = new ProjectorBounds(crs, minLong, minLat, maxLong, maxLat);

        return bounds;
    }

    public List<String> getLayers() {
        return layers;
    }

    public List<String> getStyles() {
        return styles;
    }

    public String getCrs() {
        return bounds.getSrs();
    }

    public String getFormat() {
        return format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public ProjectorBounds getBounds() {
        return bounds;
    }

    public String getInfoFormat() {
        return infoFormat;
    }

    public Date getTime() {
        return time;
    }

}
