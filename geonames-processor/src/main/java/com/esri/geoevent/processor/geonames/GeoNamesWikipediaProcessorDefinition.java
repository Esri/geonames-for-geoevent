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

public class GeoNamesWikipediaProcessorDefinition extends GeoEventProcessorDefinitionBase
{

	protected static final String	GEONAMES_USERNAME_PROPERTY					= "geoNamesUsername";
	protected static final String	GEONAMES_WIKIPEDIARADIUS_PROPERTY		= "geoNamesWikiRadius";
	protected static final String	GEONAMES_WIKIPEDIAMAXROWS_PROPERTY	= "geoNamesWikiMaxRows";

	public GeoNamesWikipediaProcessorDefinition()
	{
		try
		{

			PropertyDefinition geoNamesUsernameProperty = new PropertyDefinition(GEONAMES_USERNAME_PROPERTY, PropertyType.String, "", "GeoNames Username (optional)", "If you purchased GeoNames Premium services, enter your username. If blank, a more limited free account will be used.", false, false);
			propertyDefinitions.put(GEONAMES_USERNAME_PROPERTY, geoNamesUsernameProperty);

			PropertyDefinition geoNamesWikipediaRadiusProperty = new PropertyDefinition(GEONAMES_WIKIPEDIARADIUS_PROPERTY, PropertyType.Double, 10, "GeoNames Wikipedia Search Radius (Km)", "The distance from the geoevent's location within which to search for Wikipedia articles. Max for a free account = 20km, max for a premium account = 150km.", true, false);
			propertyDefinitions.put(GEONAMES_WIKIPEDIARADIUS_PROPERTY, geoNamesWikipediaRadiusProperty);

			PropertyDefinition geoNamesWikipediaMaxRowsProperty = new PropertyDefinition(GEONAMES_WIKIPEDIAMAXROWS_PROPERTY, PropertyType.Integer, 10, "GeoNames Wikipedia Max Rows", "The maximum number of Wikipedia articles to return. Max for a free account = 500, max for a premium account = 2000.", true, false);
			propertyDefinitions.put(GEONAMES_WIKIPEDIAMAXROWS_PROPERTY, geoNamesWikipediaMaxRowsProperty);

			GeoEventDefinition ged = new DefaultGeoEventDefinition();
			List<FieldDefinition> fds = new ArrayList<FieldDefinition>();

			ged = new DefaultGeoEventDefinition();
			ged.setName("GeoNamesWikipedia");
			fds = new ArrayList<FieldDefinition>();
			fds.add(new DefaultFieldDefinition("trackId", FieldType.String, "TRACK_ID"));
			fds.add(new DefaultFieldDefinition("timestamp", FieldType.Date, "TIME_START"));
			fds.add(new DefaultFieldDefinition("incidentId", FieldType.String));
			fds.add(new DefaultFieldDefinition("summary", FieldType.String));
			fds.add(new DefaultFieldDefinition("distance", FieldType.Double));
			fds.add(new DefaultFieldDefinition("rank", FieldType.Double));
			fds.add(new DefaultFieldDefinition("title", FieldType.String));
			fds.add(new DefaultFieldDefinition("wikipediaUrl", FieldType.String));
			fds.add(new DefaultFieldDefinition("elevation", FieldType.Double));
			fds.add(new DefaultFieldDefinition("countryCode", FieldType.String));
			fds.add(new DefaultFieldDefinition("lng", FieldType.Double));
			fds.add(new DefaultFieldDefinition("feature", FieldType.String));
			fds.add(new DefaultFieldDefinition("geoNameId", FieldType.Double));
			fds.add(new DefaultFieldDefinition("lang", FieldType.String));
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
		return "GeoNamesWikipediaProcessor";
	}

	@Override
	public String getDomain()
	{
		return "geonames.wikipedia.processor";
	}

	@Override
	public String getVersion()
	{
		return "10.3.0";
	}

	@Override
	public String getLabel()
	{
		/**
		 * Note: by using the ${myBundle-symbolic-name.myProperty} notation, the framework will attempt to replace the
		 * string with a localized string in your properties file.
		 */
		// return "${reversegeocode.gep.reversegeocode-processor.PROCESSOR_LABEL}";
		return "GeoNames Wikipedia Lookup";
	}

	@Override
	public String getDescription()
	{
		/**
		 * Note: by using the ${myBundle-symbolic-name.myProperty} notation, the framework will attempt to replace the
		 * string with a localized string in your properties file.
		 */
		// return "${reversegeocode.gep.reversegeocode-processor.PROCESSOR_DESC}";
		return "Calls the GeoNames web service and fetches the georeferenced Wikipedia articles near the GeoEvent.";
	}
}
