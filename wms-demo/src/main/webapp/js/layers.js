var map;

function init() {

    ///////////////////////////////////////////
    // setup the base map with google layers
    ///////////////////////////////////////////
    
    map = new OpenLayers.Map('map', {
        projection: 'EPSG:3857',
        layers: [
            new OpenLayers.Layer.Google(
                "Google Physical",
                {type: google.maps.MapTypeId.TERRAIN}
            ),
            new OpenLayers.Layer.Google(
                "Google Streets", // the default
                {numZoomLevels: 20}
            ),
            new OpenLayers.Layer.Google(
                "Google Hybrid",
                {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
            ),
            new OpenLayers.Layer.Google(
                "Google Satellite",
                {type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}
            )
        ],
        center: new OpenLayers.LonLat(149.1,-35.3)
            // Google.v3 uses web mercator as projection, so we have to
            // transform our coordinates
            .transform('EPSG:4326', 'EPSG:3857'),
        zoom: 6,
        zoomMethod: null
    });
    
    map.addControl(new OpenLayers.Control.LayerSwitcher());

    ///////////////////////////////////////////
    // setup the custom wms layer
    ///////////////////////////////////////////

    var customWmsUrl = "http://localhost:8080/wms-demo/wms";
    
    var layer = new OpenLayers.Layer.WMS( "Custom WMS Layer",
                customWmsUrl, 
                {layers: 'Custom',transparent: "true", format: "image/png",styles:"Standard"},
                {gutter:15,singleTile:true, visibility:true,opacity: 0.5,animationEnabled: false});
                
    map.addLayer(layer);
    
    ////////////////////////////////////////////////////
    // setup getFeatureInfo on click for custom layer
    ////////////////////////////////////////////////////s
    
    var click = new OpenLayers.Control.WMSGetFeatureInfo({
                url: customWmsUrl, 
                title: 'Identify features by clicking',
                layers: [layer],
                queryVisible: true
            })
    click.events.register("getfeatureinfo", this, showInfo);
    map.addControl(click);
    click.activate();
    
}

function showInfo(event) {
    map.addPopup(new OpenLayers.Popup.FramedCloud(
        "chicken", 
        map.getLonLatFromPixel(event.xy),
        null,
        event.text,
        null,
        true
    ));
};