
function addLayers(map) {


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
