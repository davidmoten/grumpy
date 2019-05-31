package com.github.davidmoten.grumpy.wms;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmurty.utils.XMLBuilder;

public class CapabilitiesProviderFromCapabilities implements CapabilitiesProvider {

    private static final Logger log = LoggerFactory
            .getLogger(CapabilitiesProviderFromCapabilities.class);

    private final Capabilities capabilities;

    public CapabilitiesProviderFromCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;

    }

    @Override
    public String getCapabilities(HttpServletRequest request) {
        String template = getTemplate();
        template = template.replace("${serviceName}", capabilities.getServiceName());
        template = template.replace("${serviceTitle}", capabilities.getServiceTitle());
        template = template.replace("${serviceAbstract}", capabilities.getServiceAbstract());
        template = template.replace("${serviceMaxWidth}", capabilities.getServiceMaxWidth() + "");
        template = template.replace("${serviceMaxHeight}", capabilities.getServiceMaxHeight() + "");
        template = template.replace("${imageFormats}", formats(capabilities.getImageFormats()));
        template = template.replace("${infoFormats}", formats(capabilities.getInfoFormats()));
        template = template.replace("${layers}", layers(capabilities.getLayers()));
        log.info("capabilities=\n" + template);
        return template;
    }

    private String layers(List<CapabilitiesLayer> layers) {
        StringBuilder s = new StringBuilder();
        for (CapabilitiesLayer layer : layers) {
            s.append(layer(layer));
            s.append("\n\n");
        }
        return s.toString();
    }

    private String layer(CapabilitiesLayer layer) {
        try {
            XMLBuilder xml = XMLBuilder.create("Layer");
            if (layer.isQueryable())
                xml = xml.a("queryable", "1");
            if (layer.isOpaque())
                xml = xml.a("opaque", "1");
            xml = xml.element("Name").text(layer.getName()).up()
            // add title
                    .element("Title").text(layer.getTitle()).up();
            for (String crs : layer.getCrs()) {
                xml = xml.element("CRS").text(crs).up();
            }

            xml = xml.e("EX_GeographicBoundingBox") //
                    .element("westBoundLongitude").text("-180").up() //
                    .element("eastBoundLongitude").text("180").up() //
                    .element("southBoundLatitude").text("-90").up() //
                    .element("northBoundLatitude").text("90").up() //
                    .up();
            // wms 1.3.0 expects lat, long order in coordinates
            xml = xml.e("BoundingBox") //
                    .a("CRS", "EPSG:4326") //
                    .a("minx", "-90") //
                    .a("miny", "-180") //
                    .a("maxx", "90") //
                    .a("maxy", "180") //
                    .up();
//             xml = xml.e("MaxScaleDenominator").text("500000000").up();
            for (String style : layer.getStyles()) {
                xml = xml.element("Style").element("Name").text(style).up().up();
            }
            return xml.asString();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (FactoryConfigurationError e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private String formats(List<String> formats) {
        StringBuilder s = new StringBuilder();
        for (String format : formats) {
            s.append("<Format>" + format + "</Format>");
        }
        return s.toString();
    }

    private static String getTemplate() {
        try {
            return IOUtils.toString(CapabilitiesProviderFromCapabilities.class
                    .getResourceAsStream("/wms-capabilities-template.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
