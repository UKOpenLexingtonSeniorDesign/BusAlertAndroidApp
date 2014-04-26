package io.github.ukopenlexingtonseniordesign.busalert;

import android.app.Activity;
import android.app.AlertDialog;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener; 

//Page that is responsible for setting and creating the notification.
public class Alert2 extends Activity {
    private ScheduleClient scheduleClient;		// This is a handle so that we can call methods on our service
    private TimePicker picker;					// This is the date picker used to select the date for our notification
    private String stopSelected;
    String routeSelected;
    String stopID;
	ArrayList<HashMap<String, String>> stops;
	
	//Values used to parse the xml with
	static final String KEY_ROUTE = "route";
	static final String KEY_STOPS = "stops";
	static final String KEY_STOP = "stop";
	static final String KEY_SEGMENTS = "segments";
	static final String KEY_LNG = "lng";
	static final String KEY_LABEL = "label";
	static final String KEY_LAT = "lat";
	static final String KEY_HTML = "html";
	
	//HashMap of Routes
	private HashMap<String, Integer> routeID = new HashMap<String, Integer>();
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert2);
         
        // Create a new service client and bind our activity to this service
        scheduleClient = new ScheduleClient(this); // changed from just (this)
        scheduleClient.doBindService();
 
        // Get a reference to our date picker
        picker = (TimePicker) findViewById(R.id.scheduleTimePicker);
      		
  		//Get the RouteList
  		RouteMap rm = new RouteMap();
  		HashMap<String, Integer> routeID = rm.getRoutes();
  		
  		//Create a List<String> to use to fill the route spinner
  		List<String> route_list = new ArrayList<String>();
  		for (String s : routeID.keySet()) {
  			route_list.add(s);
  		}
  		
  		//Fill route spinner using built in android functions that populate the spinner from a list
  		Spinner route_spinner = (Spinner) findViewById(R.id.route_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, route_list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        route_spinner.setAdapter(dataAdapter);
		route_spinner.setOnItemSelectedListener(new MyItemSelectedListener());
    }
    
    //This is the onClick called from the XML to set a new notification 
    public void onDateSelectedButtonClick(View v){
        //send dialog to user saying notification has been set
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    	// Setting Dialog Message
    	alertDialog.setMessage("Notification Set!");
    	alertDialog.show();
 
        //Use android's built in calendar object and our picker to get the time for our notification
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
        c.set(Calendar.MINUTE, picker.getCurrentMinute());
        
        //start new HTML task to find the unique stop ID of the selected stop
        HTMLTask task = new HTMLTask();     
        
        //Get the integer of the route from the hashMap
        RouteMap routemap = new RouteMap();
        HashMap<String, Integer> routes = routemap.getRoutes();
		int routeInt = routes.get(routeSelected);		
	    task.execute(new Integer[] { routeInt });
	    
	    //Ask our service to set an alarm for that time, this activity talks to the client that talks to the service
        scheduleClient.setAlarmForNotification(c);
    }
     
    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if(scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onStop();
    }
    
    public void fillBusStopSpinner(ArrayList<HashMap<String, String>> inStops)
    {
    	//Loop over stops and add the bus stop labels into the list
    	List<String> list = new ArrayList<String>();
    	
    	for (int i = 0; i < inStops.size(); i++) {
    		if (inStops.get(i) != null) {
    			list.add(inStops.get(i).get("label"));
    		}
    	}

    	//Now fill the spinner with the list
        Spinner stop_spinner = (Spinner) findViewById(R.id.stop_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        stop_spinner.setAdapter(dataAdapter);
        
        //Create a listener for the object
        stop_spinner.setOnItemSelectedListener(new MyStopSelectedListener());
    }
	
    //Object listener
    public class MyStopSelectedListener implements OnItemSelectedListener {
    	String selected;	
	
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        stopSelected = parent.getItemAtPosition(pos).toString();
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	        // Do nothing.
	    }
	}
    
    //Object listener
	public class MyItemSelectedListener implements OnItemSelectedListener {
		
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        routeSelected = parent.getItemAtPosition(pos).toString();
	        
	        //Create map of routes to get id
	        RouteMap routemap = new RouteMap();
	        HashMap<String, Integer> routes = routemap.getRoutes();
	        
	        //Now call the XML getter
			XMLTask task = new XMLTask();
			int routeInt = routes.get(routeSelected);		//Get the integer of the route from the hashMap
		    task.execute(new Integer[] { routeInt });		//Get stops for selected route
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	        // Do nothing.
	    }
	}
	
	//Task that queries LexTran for the stops of a particular route, parses the information, then saves it.
	protected class XMLTask extends AsyncTask<Integer, String, String> {
        String xml = null;
        String url = "http://realtime.lextran.com/InfoPoint/map/GetRouteXml.ashx?RouteID=";
		
	    @Override
	    protected void onPreExecute() {
	        // Do stuff
	        // For example showing a Dialog to give some feedback to the user.
	    }

		@Override
		protected String doInBackground(Integer... route) {
			url = url + route[0];
	        try {	        	
	            /*
	             * You 'query' lextran by visiting a certain URL tailored to the route you want to look at.
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
		
	        //return the information for the route to the onPostExecute method
	        return xml;
		}
	    
		// The `xml` that you returned will be passed to this method
	    @Override
	    protected void onPostExecute(String xml) {
	    	//Initialize the list we will use to store the stops for the route
		    stops = new ArrayList<HashMap<String, String>>();
	    	Document doc = MainActivity.getDomElement(xml);		//Tool for parsing xml
	    	
	    	//get each stop
	    	NodeList nl = doc.getElementsByTagName(KEY_STOP);    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	    		//Holds the data for this stop, maps the longitude to lng, latitude to lat, etc. all formatted as strings.
	            HashMap<String, String> map = new HashMap<String, String>();	
	            Element e = (Element) nl.item(i);
	            
	            //Fill out the HashMap that holds the data for this stop
	            map.put(KEY_LNG, e.getAttribute(KEY_LNG));
	            map.put(KEY_LABEL, e.getAttribute(KEY_LABEL));
	            map.put(KEY_LAT, e.getAttribute(KEY_LAT));
	            map.put(KEY_HTML, e.getAttribute(KEY_HTML));
	            
	            // adding HashMap to list of stops
	            stops.add(map);
	        }	    	
	    	
	    	//Now set the bus stop spinner
	    	fillBusStopSpinner(stops);
	    }
	}
	
	//getting the html tag
	private class HTMLTask extends AsyncTask<Integer, String, String> {
        private String html = null;
        private String url = "http://realtime.lextran.com/InfoPoint/map/GetRouteXml.ashx?RouteID=";
        
		@Override
		protected String doInBackground(Integer... route) {
			url = url + route[0];
	        try {	        	
	            /*
	             * You 'query' lextran by visiting a certain URL tailored to the route you want to look at.
	             * We append the routeID to the URL and then use android's built in http methods to connect
	             * to the page and grab the HTML info for the route.
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
		
	        //return html
	        return html;
		}
        
	    //Callback method
	    @Override
	    protected void onPostExecute(String html) {
	        // The html that you returned will be passed to this method.	   	
	    	Document doc = MainActivity.getDomElement(html);
	    	NodeList nl = doc.getElementsByTagName(KEY_STOP);
	    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	            Element e = (Element) nl.item(i);

	            //If we have found the stop that is currently selected, save its HTML tag
	            if(e.getAttribute(KEY_LABEL).equals(stopSelected)){
	            	//this is the right stop, save the hmtl tag
	            	NotifyService.stopSelected = e.getAttribute(KEY_HTML);
	            }     
	        }
	    }
	}	
}
