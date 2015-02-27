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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.property.Property;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.GeoEventListener;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class GeoNamesWikipediaProcessor extends GeoEventProcessorBase implements GeoEventProducer, EventUpdatable
{
	/**
	 * Initialize the i18n Bundle Logger
	 * 
	 * See {@link BundleLogger} for more info.
	 */

	GeoEventListener									listener;
	private static final BundleLogger	LOGGER					= BundleLoggerFactory.getLogger(GeoNamesWikipediaProcessor.class);

	private long											lastReport			= 0;
	private int												maxMessageRate	= 500;
	private boolean										printedWarning;

	Properties												prop						= new Properties();
	private Object										propertyLock		= new Object();
	private String										geoNamesUsername;
	private double										geoNamesWikipediaRadius;
	private int												geoNamesWikipediaMaxRows;
	private double										geoNamesNearbyOSMPOIsRadius;
	private int												geoNamesNearbyOSMPOIsMaxRows;
	private GeoEventCreator						geoEventCreator;
	private GeoEventProducer					geoEventProducer;
	private Messaging									messaging;

	protected GeoNamesWikipediaProcessor(GeoEventProcessorDefinition definition) throws ComponentException
	{
		super(definition);
	}

	@Override
	public void setId(String id)
	{
		super.setId(id);
		EventDestination evtDest = new EventDestination(id + ":event");
		geoEventProducer = messaging.createGeoEventProducer(evtDest);
	}

	@Override
	public void afterPropertiesSet()
	{
		synchronized (propertyLock)
		{
			geoNamesUsername = "krgorton";
			if (hasProperty(GeoNamesWikipediaProcessorDefinition.GEONAMES_USERNAME_PROPERTY))
			{
				geoNamesUsername = (String) getProperty(GeoNamesWikipediaProcessorDefinition.GEONAMES_USERNAME_PROPERTY).getValue();
				if (geoNamesUsername == "")
					geoNamesUsername = "krgorton";
			}
			geoNamesWikipediaRadius = 10.0;
			if (hasProperty(GeoNamesWikipediaProcessorDefinition.GEONAMES_WIKIPEDIARADIUS_PROPERTY))
			{
				geoNamesWikipediaRadius = (double) getProperty(GeoNamesWikipediaProcessorDefinition.GEONAMES_WIKIPEDIARADIUS_PROPERTY).getValue();
			}
			geoNamesWikipediaMaxRows = 10;
			if (hasProperty(GeoNamesWikipediaProcessorDefinition.GEONAMES_WIKIPEDIAMAXROWS_PROPERTY))
			{
				geoNamesWikipediaMaxRows = (int) getProperty(GeoNamesWikipediaProcessorDefinition.GEONAMES_WIKIPEDIAMAXROWS_PROPERTY).getValue();
			}
			geoNamesNearbyOSMPOIsRadius = 1.0;

		}
	}

	@Override
	public GeoEvent process(GeoEvent geoEvent) throws Exception, MalformedURLException, JSONException
	{
		try
		{

			Point point = (Point) geoEvent.getGeometry().getGeometry();
			double lon = point.getX();
			double lat = point.getY();
			int wkid = geoEvent.getGeometry().getSpatialReference().getID();

			// fetch nearby Wikipedia articles via geoNames web service
			String geoNamesLang = "en"; // lang : language code (around 240 languages) (default = en)
			URL geoNamesWikipediaURL = new URL("http://api.geonames.org/findNearbyWikipediaJSON?lat=" + Double.toString(lat) + "&lng=" + Double.toString(lon) + "&username=" + geoNamesUsername + "&lang=" + geoNamesLang + "&radius=" + Double.toString(geoNamesWikipediaRadius) + "&maxRows=" + Integer.toString(geoNamesWikipediaMaxRows));
			String responseWikipedia = getReverseGeocode(geoNamesWikipediaURL);
			createWikipediaGeoEvent(geoEvent, responseWikipedia);

		}
		catch (MessagingException e)
		{
			LOGGER.error("EVENT_SEND_FAILURE", e);
		}
		return null;
	}

	private String getReverseGeocode(URL url)
	{
		String output = "";

		try
		{

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200)
			{
				String errorString = "Failed : HTTP error code : " + conn.getResponseCode();
				throw new RuntimeException(errorString);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String line;

			while ((line = br.readLine()) != null)
			{
				output += line;
			}

			conn.disconnect();

		}
		catch (MalformedURLException e)
		{

			e.printStackTrace();

		}
		catch (IOException e)
		{

			e.printStackTrace();

		}

		return output;
	}

	private void createWikipediaGeoEvent(GeoEvent geoEvent, String jsonResponse) throws JSONException, MessagingException
	{
		JSONObject jsonObj = new JSONObject(jsonResponse);
		JSONArray geonamesSONArray = jsonObj.getJSONArray("geonames");
		for (int i = 0; i < geonamesSONArray.length(); i++)
		{
			JSONObject geoname = geonamesSONArray.getJSONObject(i);
			GeoEvent geonameGeoEvent = null;
			String incidentId = "none";
			String inGeoEventName = geoEvent.getGeoEventDefinition().getName();
			if (inGeoEventName.equals("incident"))
				incidentId = (String) geoEvent.getField("id");

			if (geoEventCreator != null)
			{
				try
				{
					geonameGeoEvent = geoEventCreator.create("GeoNamesWikipedia", definition.getUri().toString());

					geonameGeoEvent.setField(0, geoEvent.getTrackId());
					geonameGeoEvent.setField(1, geoEvent.getStartTime());
					geonameGeoEvent.setField(2, incidentId);
					try
					{
						geonameGeoEvent.setField(3, geoname.getString("summary"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(3, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(4, Double.parseDouble(geoname.getString("distance")));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(4, Double.NaN);
					}
					try
					{
						geonameGeoEvent.setField(5, geoname.getDouble("rank"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(5, Double.NaN);
					}
					try
					{
						geonameGeoEvent.setField(6, geoname.getString("title"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(6, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(7, geoname.getString("wikipediaUrl"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(7, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(8, geoname.getDouble("elevation"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(8, Double.NaN);
					}
					try
					{
						geonameGeoEvent.setField(9, geoname.getString("countryCode"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(9, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(10, geoname.getDouble("lng"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(10, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(11, geoname.getString("feature"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(11, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(12, geoname.getDouble("geoNameId"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(12, Double.NaN);
					}
					try
					{
						geonameGeoEvent.setField(13, geoname.getString("lang"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(13, "unavailable");
					}
					try
					{
						geonameGeoEvent.setField(14, geoname.getDouble("lat"));
					}
					catch (Exception ex)
					{
						geonameGeoEvent.setField(14, "unavailable");
					}
					MapGeometry pt = null;
					try
					{
						pt = new MapGeometry(new Point(geoname.getDouble("lng"), geoname.getDouble("lat"), geoname.getDouble("elevation")), SpatialReference.create(4326));
					}
					catch (Exception ex)
					{
						pt = new MapGeometry(new Point(geoname.getDouble("lng"), geoname.getDouble("lat")), SpatialReference.create(4326));
					}
					try
					{
						geonameGeoEvent.setGeometry(pt);
					}
					catch (Exception ex)
					{
						LOGGER.debug("Failed to set geometry");
					}
					geonameGeoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
					geonameGeoEvent.setProperty(GeoEventPropertyName.OWNER_ID, getId());
					geonameGeoEvent.setProperty(GeoEventPropertyName.OWNER_URI, definition.getUri());

				}
				catch (FieldException error)
				{
					geonameGeoEvent = null;
					LOGGER.error("GEOEVENT_CREATION_ERROR", error.getMessage());
					LOGGER.info(error.getMessage(), error);
				}
				catch (MessagingException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try
			{
				send(geonameGeoEvent);
			}
			catch (Exception ex)
			{
				LOGGER.error("GEOEVENTPRODUCER_SEND_ERROR (Wikipedia)", ex.getMessage());
			}
		}
	}

	@Override
	public void send(GeoEvent geoEvent) throws MessagingException
	{
		if (geoEventProducer != null && geoEvent != null)
		{
			geoEventProducer.send(geoEvent);
			// LOGGER.info("Sent: " + geoEvent.toString());
		}
	}

	@Override
	public EventDestination getEventDestination()
	{
		return (geoEventProducer != null) ? geoEventProducer.getEventDestination() : null;
	}

	@Override
	public void disconnect()
	{
		if (geoEventProducer != null)
			geoEventProducer.disconnect();
	}

	@Override
	public boolean isConnected()
	{
		return (geoEventProducer != null) ? geoEventProducer.isConnected() : false;
	}

	@Override
	public String getStatusDetails()
	{
		return (geoEventProducer != null) ? geoEventProducer.getStatusDetails() : "";
	}

	@Override
	public void setup() throws MessagingException
	{
		;
	}

	@Override
	public void init() throws MessagingException
	{
		;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		;
	}

	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
		geoEventCreator = messaging.createGeoEventCreator();
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(definition.getName());
		sb.append("/");
		sb.append(definition.getVersion());
		sb.append("[");
		for (Property p : getProperties())
		{
			sb.append(p.getDefinition().getPropertyName());
			sb.append(":");
			sb.append(p.getValue());
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public List<EventDestination> getEventDestinations()
	{
		return (geoEventProducer != null) ? Arrays.asList(geoEventProducer.getEventDestination()) : new ArrayList<EventDestination>();
	}
}
