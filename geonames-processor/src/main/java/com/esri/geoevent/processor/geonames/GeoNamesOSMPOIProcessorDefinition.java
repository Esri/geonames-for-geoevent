/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
*/

package com.esri.geoevent.processor.geonames;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class GeoNamesOSMPOIProcessorDefinition extends GeoEventProcessorDefinitionBase
{

	protected static final String	GEONAMES_USERNAME_PROPERTY				= "geoNamesUsername";
	protected static final String	GEONAMES_OSMPOIsRADIUS_PROPERTY		= "geoNamesOSMPOIsRadius";
	protected static final String	GEONAMES_OSMPOIsMAXROWS_PROPERTY	= "geoNamesOSMPOIsMaxRows";

	public GeoNamesOSMPOIProcessorDefinition()
	{
		try
		{
			PropertyDefinition geoNamesUsernameProperty = new PropertyDefinition(GEONAMES_USERNAME_PROPERTY, PropertyType.String, "", "GeoNames Username (optional)", "If you purchased GeoNames Premium services, enter your username. If blank, a more limited free account will be used.", false, false);
			propertyDefinitions.put(GEONAMES_USERNAME_PROPERTY, geoNamesUsernameProperty);

			PropertyDefinition geoNamesOSMPOIsRadiusProperty = new PropertyDefinition(GEONAMES_OSMPOIsRADIUS_PROPERTY, PropertyType.Double, 1, "GeoNames OpenStreetmap POI Search Radius (Km)", "The distance from the geoevent's location within which to search for OpenStreetmap Points of Interest. Max for a free account = 1, max for a premium account = 3.", true, false);
			propertyDefinitions.put(GEONAMES_OSMPOIsRADIUS_PROPERTY, geoNamesOSMPOIsRadiusProperty);

			PropertyDefinition geoNamesOSMPOIsMaxRowsProperty = new PropertyDefinition(GEONAMES_OSMPOIsMAXROWS_PROPERTY, PropertyType.Integer, 50, "GeoNames OpenStreetmap POI Max Rows", "The maximum number of OpenStreetmap Points of Interest to return. Max for a free account = 50, max for a premium account = 150.", true, false);
			propertyDefinitions.put(GEONAMES_OSMPOIsMAXROWS_PROPERTY, geoNamesOSMPOIsMaxRowsProperty);

			GeoEventDefinition ged = new DefaultGeoEventDefinition();
			ged.setName("GeoNamesOSMPOI");
			List<FieldDefinition> fds = new ArrayList<FieldDefinition>();
			fds.add(new DefaultFieldDefinition("trackId", FieldType.String, "TRACK_ID"));
			fds.add(new DefaultFieldDefinition("timestamp", FieldType.Date, "TIME_START"));
			fds.add(new DefaultFieldDefinition("incidentId", FieldType.String));
			fds.add(new DefaultFieldDefinition("typeName", FieldType.String));
			fds.add(new DefaultFieldDefinition("distance", FieldType.Double));
			fds.add(new DefaultFieldDefinition("name", FieldType.String));
			fds.add(new DefaultFieldDefinition("lng", FieldType.Double));
			fds.add(new DefaultFieldDefinition("typeClass", FieldType.String));
			fds.add(new DefaultFieldDefinition("lat", FieldType.Double));
			fds.add(new DefaultFieldDefinition("geometry", FieldType.Geometry, "GEOMETRY"));
			ged.setFieldDefinitions(fds);
			geoEventDefinitions.put(ged.getName(), ged);

		}
		catch (PropertyException ex)
		{
			;
		}
		catch (ConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getName()
	{
		return "GeoNamesOSMPOIProcessor";
	}

	@Override
	public String getDomain()
	{
		return "geonames.osmpoi.processor";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.processor.geonames-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.processor.geonames-processor.PROCESSOR_DESC}";
	}
}
