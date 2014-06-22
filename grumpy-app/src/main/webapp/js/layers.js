
function addLayers(map) {


    ///////////////////////////////////////////
    // setup the custom wms layer
    ///////////////////////////////////////////

    var wmsUrl = "wms";
    
    ///////////////////////////////////////////
    // add the Darkness layer
    ///////////////////////////////////////////
    var layer2 = new OpenLayers.Layer.WMS( "Darkness",
                wmsUrl, 
                {layers: 'Darkness',transparent: "true", format: "image/png",styles:"Standard"},
                {gutter:15,singleTile:true, visibility:true,opacity: 0.5,animationEnabled: true});
                
    map.addLayer(layer2);
}

