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
import java.util.List;

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
import com.google.android.gms.maps.MapFragment;










import com.google.android.gms.maps.MapView;
import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class Map extends Activity{
	HashMap<String, Integer> routeID = new HashMap<String, Integer>();
	private String routeSelected;
	private String kmlToOverlay;
	static final String KEY_INFO = "info";
	static final String KEY_KML = "trace_kml_url";
	static final String KEY_COORD = "coordinates";
	static final String KEY_LINE_STRING = "LineString";
	static GoogleMap map;
	
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
		
	    //FIX!!!: Using a temporary fix to fill the route spinner (hard coded xml). Will not be able to track a new route that Lextran adds.
		// Create an ArrayAdapter using the string array and a default spinner layout
        Spinner map_route_spinner = (Spinner) findViewById(R.id.map_route_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, map_route_list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        map_route_spinner.setAdapter(dataAdapter);
		map_route_spinner.setOnItemSelectedListener(new RouteItemSelectedListener());
	}
	
	public void setKML(String inKML) {
		kmlToOverlay = inKML;
	}
	
	public class RouteItemSelectedListener implements OnItemSelectedListener {
			
			//Callback function
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        routeSelected = parent.getItemAtPosition(pos).toString();
		        
		        //Now call the XML getter
				XMLTask task = new XMLTask();
				int routeInt = routeID.get(routeSelected);		//Get the integer of the route from the hashMap
			    task.execute(new Integer[] { routeInt });		//Get stops for selected route
		    }
	
		    public void onNothingSelected(AdapterView<?> parent) {
		        // Do nothing.
		    }
	}
	
	private class XMLTask extends AsyncTask<Integer, String, String> {
        String xml = null;
        String url = "http://realtime.lextran.com/InfoPoint/map/GetRouteXml.ashx?RouteID=";
		
	    @Override
	    protected void onPreExecute() {
	        // Do stuff
	        // For example showing a Dialog to give some feedback to the user.
	    }

	    @Override
	    protected void onPostExecute(String xml) {
	        // If you have created a Dialog, here is the place to dismiss it.
	        // The `xml` that you returned will be passed to this method
	    	
		    String kml_url = "http://realtime.lextran.com";
		    
	    	Document doc = getDomElement(xml);
	    	//get each stop
	    	NodeList nl = doc.getElementsByTagName(KEY_INFO);
	    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	            Element e = (Element) nl.item(i);
	            kml_url = kml_url + e.getAttribute(KEY_KML);
	               
	        }	    	
	    	
	    	//		******* Do Something With KML Here ********
	    	//Google KML Tutorial:  https://developers.google.com/kml/documentation/kml_tut
	    	//In Android: 			http://stackoverflow.com/questions/3109158/how-to-draw-a-path-on-a-map-using-kml-file/3109723#3109723
	    	//Another good in Android example: http://tw.tonytuan.org/2009/06/android-driving-direction-route-path.html
	    	//Or we could just show route as PolyLine: https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/Polyline
	    	
	    	//Parse KML for coordinates, give PolyLine list of coordinates
	    	 
	    	//not sure this is correct, haven't tested. 

	    	/*
	    	String myCoordinates = "";
	    	NavigationDataSet myNavDataSet = GetNavigationDataSet(kml_url);
	    	ArrayList<Placemark> myPlacemarks = myNavDataSet.getPlacemarks();
	    	for( Placemark p : myPlacemarks){
	    		if(p.getCoordinates() != null && myCoordinates==""){
	    			myCoordinates = p.getCoordinates();
	    		}
	    	}
	    	*/

	    	// get the kml (XML) doc. And parse it to get the coordinates(direction route). 
	   
	    	
	    	//then would need to parse myCoordinates string and pass to Polyline
	    	
	    	
	    	
	    	//Save the values in the main thread after all processing is finished
	    	setKML(kml_url);
	    }

		@Override
		protected String doInBackground(Integer... stops) {
			
			url = url + stops[0];
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
	
	
	
	
	
	
	
	
	
	
	
	
}
