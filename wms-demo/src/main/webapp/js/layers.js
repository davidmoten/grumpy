
function addLayers(map) {


    ///////////////////////////////////////////
    // setup the custom wms layer
    ///////////////////////////////////////////

    var wmsUrl = "wms";
    
    var layer1 = new OpenLayers.Layer.WMS( "Custom WMS Layer",
                wmsUrl, 
                {layers: 'Custom',transparent: "true", format: "image/png",styles:"Standard"},
                {gutter:15,singleTile:true, visibility:true,opacity: 0.5,animationEnabled: false});
                
    map.addLayer(layer1);
    
    ///////////////////////////////////////////
    // add the Darkness layer
    ///////////////////////////////////////////
    var layer2 = new OpenLayers.Layer.WMS( "Darkness",
                wmsUrl, 
                {layers: 'Darkness',transparent: "true", format: "image/png",styles:"Standard"},
                {gutter:15,singleTile:true, visibility:false,opacity: 0.5,animationEnabled: true});
                
    map.addLayer(layer2);
    
    ////////////////////////////////////////////////////
    // setup getFeatureInfo on click for all layers
    ////////////////////////////////////////////////////s
    
    var click = new OpenLayers.Control.WMSGetFeatureInfo({
                url: wmsUrl, 
                title: 'Identify features by clicking',
                layers: [layer1],
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
