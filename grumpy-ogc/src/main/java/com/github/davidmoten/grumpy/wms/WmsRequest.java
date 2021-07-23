package com.github.davidmoten.grumpy.wms;

import static com.github.davidmoten.util.servlet.RequestUtil.getList;
import static com.github.davidmoten.util.servlet.RequestUtil.getParameter;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;

import com.github.davidmoten.grumpy.projection.FeatureUtil;
import com.github.davidmoten.grumpy.projection.ProjectorBounds;
import com.github.davidmoten.util.servlet.RequestUtil;

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
    private final Date time;

    public WmsRequest(List<String> layers, List<String> queryLayers, List<String> styles,
            ProjectorBounds bounds, String format, int width, int height, boolean transparent,
            Color backgroundColor, String version, String infoFormat,
            Map<String, String> parameters, Date time) {
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
        this.time = time;
    }

    public WmsRequest(HttpServletRequest request) throws MissingMandatoryParameterException {

        this(getList(request, "LAYERS", true), getList(request, "QUERY_LAYERS", false),
                getList(request, "STYLES", true),
                getBounds(getEffectiveCrs(request), getParameter(request, "BBOX", true),
                        getVersion(request)),
                getParameter(request, "FORMAT", true),
                Integer.parseInt(getParameter(request, "WIDTH", true)),
                Integer.parseInt(getParameter(request, "HEIGHT", true)), isTransparent(request),
                getBackgroundColor(request), getVersion(request),
                getParameter(request, "INFO_FORMAT", false), getParameters(request),
                getTime(request));
    }

    public WmsRequest(String layers, String queryLayers, String styles, String crs, String srs,
            String bbox, String format, String width, String height, String transparent,
            String bgColor, String version, String infoFormat, Map<String, String> parameters,
            String time) {
        this(list(checkNotBlank(layers, "LAYERS")), //
                list(queryLayers), //
                list(checkNotBlank(styles, "STYLES")), //
                getBounds(getEffectiveCrs(srs, crs), //
                        checkNotBlank(bbox, "BBOX"), //
                        version), //
                checkNotBlank(format, "FORMAT"), //
                parseInt(width, "WIDTH"), //
                parseInt(height, "HEIGHT"), //
                "true".equalsIgnoreCase(transparent), //
                getColor(bgColor, "BGCOLOR"), //
                checkNotBlank(version, "VERSION"), //
                infoFormat, //
                parameters, //
                getTime(time));
    }

    private static int parseInt(String s, String name) {
        try {
            return Integer.parseInt(checkNotBlank(s, name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " is not a valid integer: " + s);
        }
    }

    private static String checkNotBlank(String s, String name) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " parameter must be present");
        }
        return s;
    }

    private static List<String> list(String value) {
        if (value == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(value.split(","));
        }
    }

    private static Date getTime(HttpServletRequest request)
            throws MissingMandatoryParameterException {
        String s = getParameter(request, "TIME", false);
        return getTime(s);
    }

    private static Date getTime(String s) {
        if (s != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_ISO_8601);
            try {
                return sdf.parse(s);
            } catch (ParseException e) {
                throw new IllegalArgumentException("TIME invalid format: " + e.getMessage());
            }
        } else
            return null;
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
        return new WmsRequest(layers, queryLayers, styles, bounds, format, w, h, transparent,
                backgroundColor, version, infoFormat, parameters, time);
    }

    public String getVersion() {
        return version;
    }

    public WmsRequest modifyStyles(List<String> newStyles) {
        return new WmsRequest(layers, queryLayers, newStyles, bounds, format, width, height,
                transparent, backgroundColor, version, infoFormat, parameters, time);
    }

    public WmsRequest modifyBounds(ProjectorBounds newBounds) {
        return new WmsRequest(layers, queryLayers, styles, newBounds, format, width, height,
                transparent, backgroundColor, version, infoFormat, parameters, time);
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

    private static boolean isTransparent(HttpServletRequest request)
            throws MissingMandatoryParameterException {
        return "true".equalsIgnoreCase(getParameter(request, "TRANSPARENT", false));
    }

    private static String getVersion(HttpServletRequest request)
            throws MissingMandatoryParameterException {
        return getParameter(request, "VERSION", false);
    }

    private static Color getBackgroundColor(HttpServletRequest request)
            throws MissingMandatoryParameterException {
        String s = getParameter(request, "BGCOLOR", false);
        return getColor(s, "BGCOLOR");
    }

    private static Color getColor(String s, String name) {
        try {
            if (s != null)
                return new Color(Integer.valueOf(s.substring(2), 16));
            else
                return Color.white;
        } catch (Throwable e) {
            throw new IllegalArgumentException("Invalid color format for " + name);
        }
    }

    private static String getEffectiveCrs(HttpServletRequest request)
            throws MissingMandatoryParameterException {
        String srs = request.getParameter("SRS");
        String crs = RequestUtil.getParameter(request, "CRS", false);
        return getEffectiveCrs(srs, crs);
    }

    private static String getEffectiveCrs(String srs, String crs) {
        return ObjectUtils.firstNonNull(crs, srs, FeatureUtil.EPSG_4326);
    }

    @Override
    public String toString() {
        return "WmsRequest [layers=" + layers + ", queryLayers=" + queryLayers + ", styles="
                + styles + ", bounds=" + bounds + ", format=" + format + ", width=" + width
                + ", height=" + height + ", transparent=" + transparent + ", backgroundColor="
                + backgroundColor + ", version=" + version + ", infoFormat=" + infoFormat
                + ", parameters=" + parameters + "]";
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
        double minLong;
        double minLat;
        double maxLong;
        double maxLat;
        /**
         * there is some confusion about order of bbox parameters. The OpenGIS WMS 1.3.0
         * spec says: http://portal.opengeospatial.org/files/?artifact_id=14416,
         * 7.3.3.6: The value of the BBOX parameter in a GetMap request is a list of
         * comma-separated real numbers (see 6.5) in the form "minx,miny,maxx,maxy".
         * These values specify the minimum X, minimum Y, maximum X, and maximum Y
         * values of a region in the Layer CRS of the request.
         * 
         * Yet, the GeoServer doco says:
         * 
         * http://docs.geoserver.org/stable/en/user/services/wms/basics.html
         *
         * Apparently the bbox parameter order is now that determined by the CRS
         * definition. EPSG:3857 for instance has this in its WKT: AXIS [X, "EAST"]
         * AXIS[Y,"NORTH"]. Still can't suss it but getting closer!
         * 
         **/

        try {
            if (("1.1.1".equals(version) || ("1.1.0".equals(version)) || !("CRS:84".equals(crs)))) {
                minLong = Double.parseDouble(items[0]);
                minLat = Double.parseDouble(items[1]);
                maxLong = Double.parseDouble(items[2]);
                maxLat = Double.parseDouble(items[3]);
            } else {
                // this is the order for CRS:84 but EPSG:4326 apparently not
                minLat = Double.parseDouble(items[0]);
                minLong = Double.parseDouble(items[1]);
                maxLat = Double.parseDouble(items[2]);
                maxLong = Double.parseDouble(items[3]);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    "BBOX invalid, should be decimal,decimal,decimal,decimal");
        }
        return new ProjectorBounds(crs, minLong, minLat, maxLong, maxLat);
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
