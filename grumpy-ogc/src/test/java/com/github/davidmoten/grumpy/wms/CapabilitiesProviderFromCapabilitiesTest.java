package com.github.davidmoten.grumpy.wms;

import org.junit.Test;

public class CapabilitiesProviderFromCapabilitiesTest {

    @Test
    public void test() {
        Capabilities cap = Capabilities //
                .builder() //
                .serviceName("CustomOgc") //
                .serviceTitle("Custom OGC") //
                .serviceAbstract(
                        "Custom OGC WMS services including Custom, Fiddle and Darkness layers") //
                .serviceBaseUrl("https://base/wms") //
                .serviceMaxHeight(2000) //
                .serviceMaxWidth(2000) //
                .imageFormat("image/png") //
                .imageFormat("image/jpeg") //
                .infoFormat("text/html") //
                .layer(CapabilitiesLayer.builder().name("Custom").title("Custom WMS Layer")
                        .queryable(true).opaque(true).style("plain").crs("EPSG:4326")
                        .crs("EPSG:3857").build()).build();
        CapabilitiesProviderFromCapabilities p = new CapabilitiesProviderFromCapabilities(cap);
        System.out.println(p.getCapabilities(null));
    }
}
