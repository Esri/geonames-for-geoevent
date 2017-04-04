# geonames-for-geoevent

ArcGIS GeoEvent Server sample OpenStreetMap and Wikipedia GeoNames Lookup Processors.

The GeoNames Lookup Processors contain two sample processors called GeoNames OpenStreetMap Lookup Processor and GeoNames Wikipedia Lookup Processor. These processors can be added to a GeoEvent Service to perform the processing described below.

The GeoNames OpenStreetMap Lookup Processor takes the geometry of the incoming GeoEvent and looks up the nearest OpenStreetMap (OSM) Points of Interest using the GeoNames web service. The search is constrained by a user-defined search radius and user-defined maximum row count to be returned.

The GeoNames Wikipedia Lookup Processor takes the geometry of the incoming GeoEvent and looks up the nearest georeferenced Wikipedia articles using the GeoNames web service. The search is constrained by a user-defined search radius and user-defined maximum row count to be returned.

![App](geonames-for-geoevent.png?raw=true)

## Features
* GeoNames OpenStreetMap Lookup Processor
* GeoNames Wikipedia Lookup Processor

## Instructions

Building the source code:

1. Make sure Maven and ArcGIS GeoEvent Server SDK are installed on your machine.
2. Run 'mvn install -Dcontact.address=[YourContactEmailAddress]'

Installing the built jar files:

1. Copy the *.jar files under the 'target' sub-folder(s) into the [ArcGIS-GeoEvent-Server-Install-Directory]/deploy folder.

## Requirements

* ArcGIS GeoEvent Server.
* ArcGIS GeoEvent Server SDK.
* Java JDK 1.7 or greater.
* Maven.

## Resources

* [Download the connector's tutorial](https://www.arcgis.com/home/item.html?id=d9db42537c74437da84288930ced6c81) from the ArcGIS GeoEvent Gallery
* [ArcGIS GeoEvent Server Resources](http://links.esri.com/geoevent)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2015 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt?raw=true) file.

[](ArcGIS, GeoEvent, Processor)
[](Esri Tags: ArcGIS GeoEvent Server)
[](Esri Language: Java)
