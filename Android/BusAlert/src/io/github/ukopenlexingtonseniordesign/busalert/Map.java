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
	private HashMap<String, Integer> routeID = new HashMap<String, Integer>();
	private ArrayList<Stop> stopsList = new ArrayList<Stop>();	//Array list to hold stop info for route
    String departTimes;
    Stop currentStop;
	private String routeSelected;
	//static final String KEY_INFO = "info";
	//static final String KEY_KML = "trace_kml_url";
	static final String KEY_STOP = "stop";
	static final String KEY_LNG = "lng";
	static final String KEY_LAT = "lat";
	static final String KEY_LABEL = "label";
	static final String KEY_HTML = "html";
	//static final String KEY_LINE_STRING = "LineString";
	private static GoogleMap map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		//Fill the route id hashmap
		routeID.put("Woodhill", 1);
		routeID.put("Georgetown Road", 2);
		routeID.put("Tates Creek Road", 3);
		routeID.put("Newtown Pike", 4);
		routeID.put("Nicholasville Road", 5);
		routeID.put("North Broadway", 6);
		routeID.put("North Limestone", 7);
		routeID.put("Versailles Road", 8);
		routeID.put("Eastland", 9);
		routeID.put("Hamburg Pavillion", 10);
		routeID.put("Richmond Road", 11);
		routeID.put("Leestown Road", 12);
		routeID.put("South Broadway", 13);
		routeID.put("UK Commonwealth Stadium", 14);
		routeID.put("Red Mile", 15);
		routeID.put("BCTC Southland", 16);
		routeID.put("Northside Connector", 17);
		routeID.put("Centre Parkway Connector", 18);
		routeID.put("Masterson Station", 20);
		routeID.put("Keeneland Airport", 21);
		routeID.put("Nicholasville Express", 22);
		routeID.put("Trolley Blue Route", 24);
		routeID.put("Trolley Green Route", 25);
		routeID.put("Alexandria - UK Medical Center", 31);
		
		//Create a List<String> to use to fill the route spinner
		List<String> map_route_list = new ArrayList<String>();
		for (String s : routeID.keySet()) {
			map_route_list.add(s);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		map.setInfoWindowAdapter(new MapPopup(getLayoutInflater()));
		
		//Add a markerclicklistener to the map
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
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
				
			    arg0.showInfoWindow();
			    
			    //Change the snippet bag to the html identifier. The info windows displayed are actually converted to flat images, so this shouldn't change the view.
			    arg0.setSnippet(oldSnippet);			    
			    
				return true;
			}
			
		});
		
	    //FIX!!!: Using a temporary fix to fill the route spinner (hard coded xml). Will not be able to track a new route that Lextran adds.
		// Create an ArrayAdapter using the string array and a default spinner layout
        Spinner map_route_spinner = (Spinner) findViewById(R.id.map_route_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, map_route_list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        map_route_spinner.setAdapter(dataAdapter);
		map_route_spinner.setOnItemSelectedListener(new RouteItemSelectedListener());
	}
	
	/*
	public void setKML(String inKML) {
		kmlToOverlay = inKML;
	}
	*/
	
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
        private final LatLng LEXINGTON = new LatLng(38.042053, -84.502550);
        
		@Override
		protected String doInBackground(Integer... route) {
			url = url + route[0];
	        try {	        	
	            // defaultHttpClient
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
	        // If you have created a Dialog, here is the place to dismiss it.
	        // The `xml` that you returned will be passed to this method
		    
	    	//To Get KML
	    	/*
	    	String kml_url = "http://realtime.lextran.com"
	    	Document doc = getDomElement(xml);
	    	NodeList nl = doc.getElementsByTagName(KEY_INFO);
	    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	            Element e = (Element) nl.item(i);
	            kml_url = kml_url + e.getAttribute(KEY_KML);
	        }
	        */	    	
	    	
	    	//To Get Route Stop Coords
	    	String lng;
	    	String lat;
	    	String label;
	    	String html;
	    	
	    	Document doc = getDomElement(xml);
	    	NodeList nl = doc.getElementsByTagName(KEY_STOP);
	    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	            Element e = (Element) nl.item(i);
	            lng = e.getAttribute(KEY_LNG);
	            lat = e.getAttribute(KEY_LAT);
	            label = e.getAttribute(KEY_LABEL);
	            html = e.getAttribute(KEY_HTML);
	            
	            //Take this data, create a LatLgn object, create a stop, then add to our list of stops
	            stopsList.add(new Stop(label, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), html));
	        }
	    	
	    	//		******* Do Something With KML Here ********
	    	//Google KML Tutorial:  https://developers.google.com/kml/documentation/kml_tut
	    	//In Android: 			http://stackoverflow.com/questions/3109158/how-to-draw-a-path-on-a-map-using-kml-file/3109723#3109723
	    	//Another good in Android example: http://tw.tonytuan.org/2009/06/android-driving-direction-route-path.html
	    	//Or we could just show route as PolyLine: https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/Polyline
	    	
	    	//Parse KML for coordinates, give PolyLine list of coordinates
	    	//not sure this is correct, haven't tested. 
	    	
	    	//For route drawing
	    	//Hardcoded the coordinates of the routes into the RouteCoord class. Use it to get a list of the coords to draw. For the route drawing
	    	//RouteCoord routeCoord = new RouteCoord();	    	
	    	//ArrayList<ArrayList<LatLng>> routeCoordinates = routeCoord.buildCoordList(routeSelected);
	    	
	    	//For drawing the stop markers
	    	drawStopsOnMap(stopsList);
	    }
		
		/*
		public void drawRouteOnMap(ArrayList<ArrayList<LatLng>> inCoordinates) {
	    	//Read the coordinates for the selected route and add to polyLine
	    	//Find the map that is on our page
	    	 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    	 
	    	 //Now draw on the map
	    	 if (map != null) {
	    		 map.clear();
	    		 
	    		 //Add a polyline to the map for each line that we draw
	    		 for (int line = 0; line < inCoordinates.size(); line++) {
		    		 //Create the PolylineOptions that contains all of our coordinates as vertices
		    		 PolylineOptions polyOptions = new PolylineOptions();	    		 
		    		 polyOptions.addAll(inCoordinates.get(line));		//Add all our coordinates to the polyOptions
		    		 polyOptions.color(0xffdd4444);
		    		 
		    		 //Now add the polyline to the map
		    		 map.addPolyline(polyOptions);
	    		 }
	    		 
	    		 map.moveCamera(CameraUpdateFactory.newLatLngZoom(inCoordinates.get(0).get(0), 25));
	    	 }   
		}
		*/
		
		public void drawStopsOnMap(ArrayList<Stop> inStops) {			
			//Find the map that is on our page
	    	 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    	 
	    	 //Now draw on the map
	    	 if (map != null) {
	    		 map.clear();
	    		 
	    		 for (Stop stop : inStops) {
	    			 currentStop = stop;
	    			 
	    			 /*
	    			 //Get the estimated arrivals for this stop
	    			 String stopUrl = "http://realtime.lextran.com/InfoPoint/departures.aspx?stopid=";
	    			 HTMLTask htmlTask = new HTMLTask();
	 				 String stopHtml = stop.getHtmlTag();		//Get the integer of the route from the hashMap
	 			     htmlTask.execute(new String[] { stopHtml });		//Get stops for selected route
	    			 */
	    			 
	    			 //Create marker
	    			 map.addMarker(new MarkerOptions()
	    			 		.position(stop.getLatLng())
	    			 		.title(stop.getName())
	    			 		.snippet(stop.getHtmlTag())		//Make the snippet the html tag so we can get it later
	    			 		.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
	    					 );
	    		 }
	    		 
	    		 map.moveCamera(CameraUpdateFactory.newLatLngZoom(LEXINGTON, 12));	//Zoom to the first stop in keyset (random)
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
	            // defaultHttpClient
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
		
	        //return html as string
	        //return html;
	        
	    	//Now parse the html and get out the times of departures for the stop
	    	departTimes = parseDepartTimes(html);
	    
	    	/*		String is built in parseDepartTimes for the new version
			//Combine into one string so we can add to a Marker.snippet
			StringBuilder sb = new StringBuilder("");
			for (String s : departTimes) {
				sb.append(s + "\n");
			}
			*/
			
			return departTimes;
		}
        
	    //Callback method
	    @Override
	    protected void onPostExecute(String inHtml) {
	    	/*
	        // If you have created a Dialog, here is the place to dismiss it.
	        // The `html` that you returned will be passed to this method   	
	    	
	    	//Now parse the html and get out the times of departures for the stop
	    	departTimes = parseDepartTimes(inHtml);
	    	
	    	
	    	//Find the map that is on our page
	    	 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    	 
	    	 //Now draw on the map
	    	 if (map != null) {
	    		 map.clear();
	    		 
			     //Combine into one string so we can add to a Marker.snippet
			     StringBuilder sb = new StringBuilder("");
			     for (String s : departTimes) {
			    	 sb.append(s + "\n");
			     }
				 
				 //Create marker
				 map.addMarker(new MarkerOptions()
				 		.position(currentStop.getLatLng())
				 		.title(currentStop.getName())
				 		.snippet(sb.toString())
						 );
	    	 }
	    	 */
		}
	    
	    /*
	    private ArrayList<String> parseDepartTimes(String inHtml) {
	    	ArrayList<String> toReturn = new ArrayList<String>();
	    	
	    	int index = inHtml.indexOf("<div class='departure'>");
	    	while (index != -1) {
	    		index = index + 23;		//Advance index the number of chars that our search consists of
	    		String time = inHtml.substring(index, index + 8);
	    		toReturn.add(time);	//Add the time to our list
	    		index = index + 14;		//Advance the 14 chars for the length of the time and closing tag
	    		
	    		index = inHtml.indexOf("<div class='departure'>", index);	//Search for the next occurence
	    	}
	    	
	    	return toReturn;
	    }
	    */
	}
	
	public Document getDomElement(String xml){
		Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
 
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
                // return DOM
            return doc;
	}
	
	public static NavigationDataSet GetNavigationDataSet(String url) {
		int LOG_LEVEL = 3;
		String TAG = "NavigationDataSet";
        NavigationDataSet navigationDataSet = null;
        try
            { 
        	if(LOG_LEVEL <= Log.DEBUG)Log.d(TAG, "url[" + url + "]");
            final URL aUrl = new URL(url); 
            if(LOG_LEVEL <= Log.DEBUG)Log.d(TAG, "Connecting...");
            final URLConnection conn = aUrl.openConnection();
            //conn.setReadTimeout(15 * 1000);  // timeout for reading the google maps data: 15 secs
            conn.connect();
            
            if(LOG_LEVEL <= Log.DEBUG)Log.d(TAG, "Connected...");
            /* Get a SAXParser from the SAXPArserFactory. */
            SAXParserFactory spf = SAXParserFactory.newInstance(); 
            SAXParser sp = spf.newSAXParser(); 
 
            /* Get the XMLReader of the SAXParser we created. */
            XMLReader xr = sp.getXMLReader();
 
            /* Create a new ContentHandler and apply it to the XML-Reader*/ 
            NavigationSaxHandler navSax2Handler = new NavigationSaxHandler(); 
            xr.setContentHandler(navSax2Handler); 
            
            if(LOG_LEVEL <= Log.DEBUG)Log.d(TAG, "Parse Stream");
            /* Parse the xml-data from our URL. */ 
            xr.parse(new InputSource(aUrl.openStream()));
            
            if(LOG_LEVEL <= Log.DEBUG)Log.d(TAG, "Get data frm Parser");
            /* Our NavigationSaxHandler now provides the parsed data to us. */ 
            navigationDataSet = navSax2Handler.getParsedData(); 
 
        } catch (Exception e) {
        	if(LOG_LEVEL <= Log.ERROR)Log.e(TAG, "error getting route info", e);
            navigationDataSet = null;
        }   
 
        return navigationDataSet;
    }
	
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
	
    private String parseDepartTimes(String inHtml) {
    	//ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();    	
    	/*
  		//Old version that returned an array of strings as the times. New version just builds the string itself
    	int index = inHtml.indexOf("<div class='departure'>");
    	while (index != -1) {
    		index = index + 23;		//Advance index the number of chars that our search consists of
    		String time = inHtml.substring(index, index + 8);
    		toReturn.add(time);	//Add the time to our list
    		index = index + 14;		//Advance the 14 chars for the length of the time and closing tag
    		
    		index = inHtml.indexOf("<div class='departure'>", index);	//Search for the next occurence
    	}
    	*/
    	
    	StringBuilder toReturn = new StringBuilder("");					//Initialize the return string
    	toReturn.append("     Estimated Times of Departure     \n");
    	
    	/*
    	//Add the first route to the builder
    	int index = inHtml.indexOf("<div class='routeName'>");
    	index = index + 23;												//Advance index past the route tag
    	int endIndex = inHtml.indexOf("<", index);						//Find the index of the first character after the routename
    	toReturn.append(inHtml.substring(index, endIndex) + ": \n");	//Appends the route name to the string 	
    	
    	while (index != -1) {
    		index = inHtml.indexOf("<div class='departure'>", index);	//routename will allways be followed by the departure tag
    		index = index + 23;		//Advance index the number of chars that our search consists of
    		String time = inHtml.substring(index, index + 8);
    		toReturn.add(time);	//Add the time to our list
    		index = index + 14;		//Advance the 14 chars for the length of the time and closing tag
    		
    		index = inHtml.indexOf("<div class='routeName'>", index);	//Search for the next occurence
    	}
    	*/
    	
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
