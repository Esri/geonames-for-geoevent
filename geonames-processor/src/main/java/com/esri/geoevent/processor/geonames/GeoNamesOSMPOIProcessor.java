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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Point2D;
import com.esri.core.geometry.Point3D;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.property.*;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.property.Property;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
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

import org.geonames.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoNamesOSMPOIProcessor extends GeoEventProcessorBase implements GeoEventProducer, EventUpdatable
{
  /**
   * Initialize the i18n Bundle Logger
   * 
   * See {@link BundleLogger} for more info.
   */


	GeoEventListener listener;
	private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(GeoNamesOSMPOIProcessor.class);
  
	private long lastReport = 0;
	private int maxMessageRate = 500;
	private boolean printedWarning;
	
	Properties prop = new Properties();
	private Object propertyLock = new Object();
	private String geoNamesUsername;
	private double geoNamesNearbyOSMPOIsRadius;
	private int geoNamesNearbyOSMPOIsMaxRows;	
	private GeoEventCreator geoEventCreator;
	private GeoEventProducer geoEventProducer;
	private Messaging messaging;
	  
	
	protected GeoNamesOSMPOIProcessor(GeoEventProcessorDefinition definition) throws ComponentException
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
			if (hasProperty(GeoNamesOSMPOIProcessorDefinition.GEONAMES_USERNAME_PROPERTY))	      
			{	    	
				geoNamesUsername = (String) getProperty(GeoNamesOSMPOIProcessorDefinition.GEONAMES_USERNAME_PROPERTY).getValue();
				if (geoNamesUsername == "")
					geoNamesUsername = "krgorton";
			}	
			geoNamesNearbyOSMPOIsRadius = 1.0;
			if (hasProperty(GeoNamesOSMPOIProcessorDefinition.GEONAMES_OSMPOIsRADIUS_PROPERTY))	      
			{	    	
				geoNamesNearbyOSMPOIsRadius = (double) getProperty(GeoNamesOSMPOIProcessorDefinition.GEONAMES_OSMPOIsRADIUS_PROPERTY).getValue();
			}
			geoNamesNearbyOSMPOIsMaxRows = 50;
			if (hasProperty(GeoNamesOSMPOIProcessorDefinition.GEONAMES_OSMPOIsMAXROWS_PROPERTY))	      
			{	    	
				geoNamesNearbyOSMPOIsMaxRows = (int) getProperty(GeoNamesOSMPOIProcessorDefinition.GEONAMES_OSMPOIsMAXROWS_PROPERTY).getValue();
			}  	      
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
	
		//fetch nearby OpenStreetmap Points of Interest via geoNames web service
		URL geoNamesNearbyOSMPOIsURL = new URL("http://api.geonames.org/findNearbyPOIsOSMJSON?lat=" + Double.toString(lat) + "&lng=" + Double.toString(lon) + "&username=" + geoNamesUsername + "&radius=" + Double.toString(geoNamesNearbyOSMPOIsRadius) + "&maxRows=" + Integer.toString(geoNamesNearbyOSMPOIsMaxRows));		
		String responseOSMPOI = getReverseGeocode(geoNamesNearbyOSMPOIsURL);
		createOSMPOIGeoEvent(geoEvent,responseOSMPOI);
		
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
		
		try {
			 
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
	 
			if (conn.getResponseCode() != 200) {
				String errorString = "Failed : HTTP error code : "+ conn.getResponseCode();
				throw new RuntimeException(errorString);
			}
	 
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
	 
			String line;
			//System.out.println("Output from Server .... \n");
			
			while ((line = br.readLine()) != null) {
				output+=line;
			}
	 
			conn.disconnect();
	 
		  } catch (MalformedURLException e) {
	 
			e.printStackTrace();
	 
		  } catch (IOException e) {
	 
			e.printStackTrace();
	 
		  }
		
		return output;
	}
	
	
	
	private void createOSMPOIGeoEvent(GeoEvent geoEvent, String jsonResponse) throws JSONException, MessagingException
	{
		JSONObject jsonObj = new JSONObject(jsonResponse);			
		JSONArray geonamesSONArray = jsonObj.getJSONArray("poi");	
		//LOGGER.info("OSMPOI response: " + jsonResponse);
		for (int i=0;i<geonamesSONArray.length();i++)
		  {
			  JSONObject geoname = geonamesSONArray.getJSONObject(i);
			  GeoEvent geonameGeoEvent = null; 	
			  String incidentId = "none";
			  String inGeoEventName = geoEvent.getGeoEventDefinition().getName();
			  if (inGeoEventName.equals("incident"))
			  	incidentId = (String)geoEvent.getField("id");
		
			  if (geoEventCreator != null)
			  {
				  try
				  {
					  geonameGeoEvent = geoEventCreator.create("GeoNamesOSMPOI", definition.getUri().toString());						
					  geonameGeoEvent.setField(0, geoEvent.getTrackId());						
					  geonameGeoEvent.setField(1, geoEvent.getStartTime());						
					  geonameGeoEvent.setField(2, incidentId);						
					  try{geonameGeoEvent.setField(3, geoname.getString("typeName"));}catch(Exception ex){}					
					  try{geonameGeoEvent.setField(4, Double.parseDouble(geoname.getString("distance")));}catch(Exception ex){}				
					  try{geonameGeoEvent.setField(5, geoname.getString("name"));}catch(Exception ex){}					
					  try{geonameGeoEvent.setField(6, Double.parseDouble(geoname.getString("lng")));}catch(Exception ex){}				
					  try{geonameGeoEvent.setField(7, geoname.getString("typeClass"));}catch(Exception ex){}			
					  try{geonameGeoEvent.setField(8, Double.parseDouble(geoname.getString("lat")));}catch(Exception ex){}	
					  MapGeometry pt2D = new MapGeometry(new Point(Double.parseDouble(geoname.getString("lng")),Double.parseDouble(geoname.getString("lat"))), SpatialReference.create(4326));
					  try{geonameGeoEvent.setGeometry(pt2D);}catch(Exception ex){LOGGER.debug("Failed to set geometry");}
					  geonameGeoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
					  geonameGeoEvent.setProperty(GeoEventPropertyName.OWNER_ID, getId());
					  geonameGeoEvent.setProperty(GeoEventPropertyName.OWNER_URI, definition.getUri());
				  }
				  catch (FieldException error)
				  {
					  geonameGeoEvent = null;
					  LOGGER.error("GEOEVENT_CREATION_ERROR", error.getMessage());
					  LOGGER.info(error.getMessage(), error);
				  } catch (MessagingException e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  }
			  }
			  
			  try
			  {
				  send(geonameGeoEvent);
			  }
			  catch(Exception ex)
			  {
				LOGGER.error("GEOEVENTPRODUCER_SEND_ERROR (OSM POI)",ex.getMessage());
			  }
		  }
	}

	@Override
	public void send(GeoEvent geoEvent) throws MessagingException {
		if (geoEventProducer != null && geoEvent != null)
		{
		    geoEventProducer.send(geoEvent);
		  	//LOGGER.info("Sent: " + geoEvent.toString());	
		}
	}
	
	@Override
	public EventDestination getEventDestination() {
		return (geoEventProducer != null) ? geoEventProducer.getEventDestination() : null;
	}
	
	@Override
	public void disconnect() {
		if (geoEventProducer != null)
      geoEventProducer.disconnect();		
	}
	
	@Override
	public boolean isConnected() {
		return (geoEventProducer != null) ? geoEventProducer.isConnected() : false;
	}
	
	@Override
	public String getStatusDetails() {
		return (geoEventProducer != null) ? geoEventProducer.getStatusDetails() : "";
	}
	
	@Override
	public void setup() throws MessagingException {
		;
	}
	
	@Override
	public void init() throws MessagingException {
		;		
	}
	
	@Override
	public void update(Observable o, Object arg) {
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
	public List<EventDestination> getEventDestinations() {
		return (geoEventProducer != null) ? Arrays.asList(geoEventProducer.getEventDestination()) : new ArrayList<EventDestination>();
	}
}