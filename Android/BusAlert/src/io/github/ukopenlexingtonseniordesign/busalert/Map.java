package io.github.ukopenlexingtonseniordesign.busalert;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;










import com.google.android.gms.maps.MapView;
import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class Map extends Activity{	
	private HashMap<String, Integer> routeID = new HashMap<String, Integer>();		//HashMap that maps the route names to their IDs
	private ArrayList<Stop> stopsList = new ArrayList<Stop>();	//Array list to hold stop info for route
    String departTimes;
    Stop currentStop;
	private String routeSelected;
	static final String KEY_STOP = "stop";
	static final String KEY_LNG = "lng";
	static final String KEY_LAT = "lat";
	static final String KEY_LABEL = "label";
	static final String KEY_HTML = "html";
	private static GoogleMap map;
	private final LatLng LEXINGTON = new LatLng(38.042053, -84.502550);
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		//Fill the route id hashmap
		RouteMap rm = new RouteMap();
		routeID = rm.getRoutes();
		
		//Create a List<String> to use to fill the route spinner
		List<String> map_route_list = new ArrayList<String>();
		for (String s : routeID.keySet()) {
			map_route_list.add(s);
		}
		
		super.onCreate(savedInstanceState);
		
		//Get a reference to our map
		setContentView(R.layout.map);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(LEXINGTON, 11));	//Set the default zoom
		map.setInfoWindowAdapter(new MapPopup(getLayoutInflater()));
		
		//Define a new markerclicklistener to the map
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

		//Method that is called when a map marker is clicked
		@Override
		public boolean onMarkerClick(Marker arg0) {
			//The snippet holds the html tag of the stop
			String oldSnippet = arg0.getSnippet();
			String htmlTag = arg0.getSnippet();
			
		 	//Get the estimated arrivals for this stop
			HTMLTask htmlTask = new HTMLTask();
		    htmlTask.execute(new String[] { htmlTag });		//Get stops for selected route
		    
		    String times;
		    try {
				times = htmlTask.get();					//Get the string that is the times
				arg0.setSnippet(times);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
		    //Display the popup over the marker
		    arg0.showInfoWindow();
		    
		    //Change the snippet back to the html identifier. The info windows displayed are actually converted to flat images, so this shouldn't change the view.
		    arg0.setSnippet(oldSnippet);			    
		    
			return true;
		}
		});
		
		// Create an ArrayAdapter using the string array and a default spinner layout
        Spinner map_route_spinner = (Spinner) findViewById(R.id.map_route_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, map_route_list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        map_route_spinner.setAdapter(dataAdapter);
		map_route_spinner.setOnItemSelectedListener(new RouteItemSelectedListener());
	}
	
	//Listener for the route spinner
	public class RouteItemSelectedListener implements OnItemSelectedListener {
			
			//Callback function
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        routeSelected = parent.getItemAtPosition(pos).toString();
		        
		        //Now call the XML getter in order to get the stops for the selected route
				XMLTask task = new XMLTask();
				int routeInt = routeID.get(routeSelected);		//Get the integer of the route from the hashMap
			    task.execute(new Integer[] { routeInt });		//Get stops for selected route
		    }
	
		    public void onNothingSelected(AdapterView<?> parent) {
		        // Do nothing.
		    }
	}
	
	//Use to get the stops, lats, and longs for the selected route
	private class XMLTask extends AsyncTask<Integer, String, String> {
        private String xml = null;
        private String url = "http://realtime.lextran.com/InfoPoint/map/GetRouteXml.ashx?RouteID=";
        
		@Override
		protected String doInBackground(Integer... route) {
			url = url + route[0];
	        try {	        	
	            /*
	             * You 'query' lextran by visiting a certain URL tailed to the route you want to look at.
	             * We append the routeID to the URL and then use android's built in http methods to connect
	             * to the page and grab the XML info for the route.
	        	*/
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpPost httpPost = new HttpPost(url);
	 
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            xml = EntityUtils.toString(httpEntity);
	 
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		
	        //return xml
	        return xml;
		}
        
	    //Callback method
	    @Override
	    protected void onPostExecute(String xml) {
	        // The `xml` that you returned will be passed to this method.	
	    	//To Get Route Stop Coords
	    	String lng;
	    	String lat;
	    	String label;
	    	String html;
	    	
	    	//Clear the current stops vector
	    	stopsList.clear();
	    	
	    	//Java helper objects for parsing
	    	Document doc = MainActivity.getDomElement(xml);
	    	NodeList nl = doc.getElementsByTagName(KEY_STOP);
	    	
	    	//Iterate over each node and store the data for the stop
	    	for (int i = 0; i < nl.getLength(); i++) {
	            Element e = (Element) nl.item(i);
	            lng = e.getAttribute(KEY_LNG);
	            lat = e.getAttribute(KEY_LAT);
	            label = e.getAttribute(KEY_LABEL);
	            html = e.getAttribute(KEY_HTML);
	            
	            //Take this data, create a LatLgn object, create a stop, then add to our list of stops
	            stopsList.add(new Stop(label, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), html));
	        }
	    	
	    	//For drawing the stop markers
	    	drawStopsOnMap(stopsList);
	    }
		
		public void drawStopsOnMap(ArrayList<Stop> inStops) {			
			//Find the map that is on our page
	    	 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    	 
	    	 //Now draw on the map
	    	 if (map != null) {
	    		 
	    		 //Reset map
	    		 map.clear();
	    		 
	    		 for (Stop stop : inStops) {
	    			 currentStop = stop;
	    			 
	    			 //Create marker for stop
	    			 map.addMarker(new MarkerOptions()
	    			 		.position(stop.getLatLng())
	    			 		.title(stop.getName())
	    			 		.snippet(stop.getHtmlTag())		//Make the snippet the html tag so we can get it later
	    			 		.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))	//Set custom icon
	    					 );
	    		 }
	    	 }   
		}
	}
	
	//Used in getting and parsing the departure times for a stop
	private class HTMLTask extends AsyncTask<String, String, String> {
        private String url = "http://realtime.lextran.com/InfoPoint/departures.aspx?stopid=";
        
		@Override
		protected String doInBackground(String... stop) {
			String html = null;
			
			url = url + stop[0];
	        try {	        	
	            /*
	             * You 'query' lextran by visiting a certain URL tailored to the data you want to look at.
	             * We append the stop ID to the URL and then use android's built in http methods to connect
	             * to the page and grab the HTML info for the stop.
	        	*/
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpPost httpPost = new HttpPost(url);
	 
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            html = EntityUtils.toString(httpEntity);
	 
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	    	//Now parse the html and get out the times of departures for the stop
	    	departTimes = parseDepartTimes(html);
			
			return departTimes;
		}
        
	    //Callback method
	    @Override
	    protected void onPostExecute(String inHtml) {}
	}
	
	//Data structure used to represent a stop
	private class Stop {
		private LatLng latlng;
		private String htmlTag;
		private String name;
		
		Stop(String inName, LatLng inLatLng, String inHTMLTag) {
			latlng = inLatLng;
			htmlTag = inHTMLTag;
			name = inName;
		}
		
		public LatLng getLatLng() {
			return latlng;
		}
		
		public String getHtmlTag() {
			return htmlTag;
		}
		
		public String getName() {
			return name;
		}
	}
	
	//Take in the html info for a stop and parse out the routes and times that busses will be arriving
    private String parseDepartTimes(String inHtml) {    	
    	StringBuilder toReturn = new StringBuilder("");					//Initialize the return string
    	toReturn.append("     Estimated Times of Departure     \n");
    	
    	int index = 0;
    	int routeIndex = 0;
    	int routeEnd = 0;
    	while (index != -1) {
    		//First find the beginning and the ending indices of this route
    		routeIndex = inHtml.indexOf("<div class='routeName'>", routeEnd);
    		routeIndex = routeIndex + 23;											//Advance index past the routename tag
    		int routeClose = inHtml.indexOf("</div>", routeIndex);					//Find the closing tag of the routename
    		String routeName = "\n" + inHtml.substring(routeIndex, routeClose) + ": ";
    		toReturn.append(routeName + "\n");		//Add the routename to the builder
    		routeIndex = routeClose;												//Advance the index past the name of the route
    		
    		routeEnd = inHtml.indexOf("<div class='routeName'>", routeIndex);	//Either finds the beginning of the next route, or -1 as the EOF
    		
    		//If we are the last route of the file then the routeEnd will be -1. Thus this while loop will skip and we will do the operation once more outside a loop
    		routeIndex = inHtml.indexOf("<div class='departure'>", routeIndex);
    		while (routeIndex < routeEnd && routeIndex != -1) {
    			routeIndex = routeIndex + 23;										//Advance past the departure tag
    			String time = inHtml.substring(routeIndex, routeIndex + 8);
    			
    			//Check if the time is the done label
    			if (time.equals("Done</di")) {
    				time = "Done";
    			}
    			
    			toReturn.append("  " + time + "  ");
    			
    			routeIndex = inHtml.indexOf("<div class='departure'>", routeIndex);
    		}
    		
    		//Ending condition
    		if (routeEnd == -1) {
    			index = -1;
    			
    			//Now add the departures for the final route
    			routeIndex = inHtml.indexOf("<div class='departure'>", routeIndex);
        		while (routeIndex != -1) {
        			routeIndex = routeIndex + 23;										//Advance past the departure tag
        			String time = inHtml.substring(routeIndex, routeIndex + 8);
        			
        			//Check if the time is the done label
        			if (time.equals("Done</di")) {
        				time = "Done";
        			}
        			
        			toReturn.append("  " + time + "  ");
        			
        			routeIndex = inHtml.indexOf("<div class='departure'>", routeIndex);
        		}
    		}
    	}
    	
    	return toReturn.toString();
    }
}
