grumpy
=============

[OGC WMS](http://www.opengeospatial.org/standards/wms) Server in java allowing custom rendering of WMS layers using projection utilities and Graphics2D.

Status: *released to Maven Central*, proper handling of longitude wrapping regions and lines happening now (nearly finished and will be released as 0.2 by 21 June 2014). 

[Release Notes](RELEASE_NOTES.md)

Continuous integration with Jenkins for this project is [here](https://xuml-tools.ci.cloudbees.com/). <a href="https://xuml-tools.ci.cloudbees.com/"><img  src="http://web-static-cloudfront.s3.amazonaws.com/images/badges/BuiltOnDEV.png"/></a>

Features
----------
* Great circle navigation utilities in ```grumpy-core```
* Map projection utilities in ```grumpy-projection```
* WMS Server utilities in ```grumpy-ogc```
* Create a lightweight WMS server (about 12MB war)
* Supports BBOX parameters handled differently by WMS 1.1.1 and 1.3
* Handles boundary discontinuities with ```RendererUtil``` methods

Demonstration
-----------------
Try it [live](http://grumpy.xuml-tools.cloudbees.net/) on cloudbees. 

```
cd wms-demo
mvn jetty:run
```

Go to [http://localhost:8080](http://localhost:8080/wms-demo) with a browser.

And at the map link you will see this:

<img src="https://raw.githubusercontent.com/davidmoten/grumpy/master/src/docs/demo.png"/>

This demonstrates a custom filled shape and some text that is placed with transparency over the position of Canberra on the map. Notice that the borders are great circle paths.

How is it done? Easy!

Getting started
-------------------
To make your own WMS service add this dependency to the pom.xml of your war project:
```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>grumpy-ogc</artifactId>
  <version>0.1</version>
</dependency>
```

How to make your own WMS
---------------------------
Using a war project (you could just copy the ```wms-demo``` project and change its artifact and group id, remove the parent reference as well):

###Create a layer:

See [CustomLayer.java](https://github.com/davidmoten/grumpy/blob/master/wms-demo/src/main/java/com/github/davidmoten/grumpy/wms/demo/CustomLayer.java) for how to render a layer using a ```Projector``` and a ```RendererUtil```.

###Define the service capabilities:

See [wms-capabilities.xml](https://github.com/davidmoten/grumpy/blob/master/wms-demo%2Fsrc%2Fmain%2Fresources%2Fwms-capabilities.xml) which should conform to the OGC WMS 1.3 schema.

###Create a servlet to serve the layer and capabilities:

See [WmsServlet.java](https://github.com/davidmoten/grumpy/blob/master/wms-demo%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgithub%2Fdavidmoten%2Fgrumpy%2Fwms%2Fdemo%2FWmsServlet.java) for how an ```HttpServlet``` is created to server WMS requests. You do need to register this servlet against a url of course in [web.xml](https://github.com/davidmoten/grumpy/blob/master/wms-demo/src/main/webapp/WEB-INF/web.xml).

From this point you have a working WMS service against the url for the ```WmsServlet```!

###View the WMS:
An example WMS client is included in ```wms-demo``` in [map.jsp](https://github.com/davidmoten/grumpy/blob/master/wms-demo%2Fsrc%2Fmain%2Fwebapp%2Fmap.jsp) and the custom layer is referenced in [layers.js](https://github.com/davidmoten/grumpy/blob/master/wms-demo/src/main/webapp/js/layers.js).

```wms-demo``` project uses [OpenLayers](http://openlayers.org/) javascript libraries and google maps v3 to display the world and the custom layer using the Spherical Mercator projection (EPSG 3857). See OpenLayers [documentation](http://docs.openlayers.org/) and [examples](http://openlayers.org/dev/examples/) to play with this client as you see fit.

How to build
----------------
```
git clone https://github.com/davidmoten/grumpy.git
cd grumpy
mvn clean install
```

Why Grumpy?
---------------
The project name was chosen at random and is no hint at the disposition of the primary developer! I'm very happy to receive contributions on this project. Just raise an issue.
