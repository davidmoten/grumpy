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
            ),
            new OpenLayers.Layer.OSM( "OpenStreetMap")
        ],
        center: new OpenLayers.LonLat(149.1,-35.3)
            // Google.v3 uses web mercator as projection, so we have to
            // transform our coordinates
            .transform('EPSG:4326', 'EPSG:3857'),
        zoom: 6,
        zoomMethod: null
    });
    
    map.addControl(new OpenLayers.Control.LayerSwitcher());

    addLayers(map);    
}

