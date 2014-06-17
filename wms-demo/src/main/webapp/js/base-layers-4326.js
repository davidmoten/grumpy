

function init(){
	var lon = 140;
	var lat = -35;
	var zoom = 5;
    var map = new OpenLayers.Map('map');
    var layer = new OpenLayers.Layer.WMS( "OpenLayers WMS",
            "http://vmap0.tiles.osgeo.org/wms/vmap0", {layers: 'basic'} );
    map.addLayer(layer);
    map.setCenter(new OpenLayers.LonLat(lon, lat), zoom);
    addLayers(map);
}