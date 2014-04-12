package io.github.ukopenlexingtonseniordesign.busalert;

import android.app.Activity;
//import io.github.ukopenlexingtonseniordesign.busalert.Alert.Hour1SelectedListener;
//import io.github.ukopenlexingtonseniordesign.busalert.Alert.Minute1SelectedListener;
//import io.github.ukopenlexingtonseniordesign.busalert.Alert.XMLTask;

import android.app.AlertDialog;
import io.github.ukopenlexingtonseniordesign.busalert.Alert.MyItemSelectedListener;
//import io.github.ukopenlexingtonseniordesign.busalert.Map.Stop;

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
 
//import com.blundell.tut.R;
//import com.blundell.tut.service.ScheduleClient;
 
public class Alert2 extends Activity{
	// This is a handle so that we can call methods on our service
    private ScheduleClient scheduleClient;
    // This is the date picker used to select the date for our notification
    private TimePicker picker;
    private String stopSelected;
    String routeSelected;
    String stopID;
	ArrayList<HashMap<String, String>> stops;
	static final String KEY_ROUTE = "route";
	static final String KEY_STOPS = "stops";
	static final String KEY_STOP = "stop";
	static final String KEY_SEGMENTS = "segments";
	static final String KEY_LNG = "lng";
	static final String KEY_LABEL = "label";
	static final String KEY_LAT = "lat";
	static final String KEY_HTML = "html";
	HashMap<String, Integer> routeID = new HashMap<String, Integer>();
 
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
      		List<String> route_list = new ArrayList<String>();
      		for (String s : routeID.keySet()) {
      			route_list.add(s);
      		}
      		//Fill route spinner
      		Spinner route_spinner = (Spinner) findViewById(R.id.route_spinner);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, route_list);
            dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            route_spinner.setAdapter(dataAdapter);
    		route_spinner.setOnItemSelectedListener(new MyItemSelectedListener());
    }
     
    /**
     * This is the onClick called from the XML to set a new notification 
     */
    public void onDateSelectedButtonClick(View v){
        //send dialog to user saying notification has been set
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    	// Setting Dialog Message
    	alertDialog.setMessage("Notification Set!");
    	alertDialog.show();
 
        Calendar c = Calendar.getInstance();
        //c.set(year, month, day);
        c.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
        c.set(Calendar.MINUTE, picker.getCurrentMinute());
        //c.set(Calendar.SECOND, 0);
        // Ask our service to set an alarm for that time, this activity talks to the client that talks to the service
        
        //start new XML task to find the uniue stop ID of the selected stop
        XMLTask2 task = new XMLTask2();
		int routeInt = routeID.get(routeSelected);		//Get the integer of the route from the hashMap
	    task.execute(new Integer[] { routeInt });
        //NotifyService.stopSelected = stopID; // needs to be the unique stop ID
        scheduleClient.setAlarmForNotification(c);
        // Notify the user what they just did
        //Toast.makeText(this, "Notification set for: "+ day +"/"+ (month+1) +"/"+ year, Toast.LENGTH_SHORT).show();
    }
     
    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if(scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onStop();
    }
	
    public void setStops(ArrayList<HashMap<String, String>> inStops) {
    	stops = inStops;
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

        Spinner stop_spinner = (Spinner) findViewById(R.id.stop_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        stop_spinner.setAdapter(dataAdapter);
        stop_spinner.setOnItemSelectedListener(new MyStopSelectedListener());
    }
	
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
    
	public class MyItemSelectedListener implements OnItemSelectedListener {
		
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
	    	
		    stops = new ArrayList<HashMap<String, String>>();
	    	Document doc = getDomElement(xml);
	    	//get each stop
	    	NodeList nl = doc.getElementsByTagName(KEY_STOP);    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	            // creating new HashMap
	            HashMap<String, String> map = new HashMap<String, String>();
	            Element e = (Element) nl.item(i);
	            
	            //CHANGES NEEDED HERE?
	            map.put(KEY_LNG, e.getAttribute(KEY_LNG));
	            map.put(KEY_LABEL, e.getAttribute(KEY_LABEL));
	            map.put(KEY_LAT, e.getAttribute(KEY_LAT));
	            map.put(KEY_HTML, e.getAttribute(KEY_HTML));
	            
	            // adding HashList to ArrayList
	            stops.add(map);
	        }	    	
	    	
	    	//Now set the bus stop spinner
	    	fillBusStopSpinner(stops);
	    	
	    	//Save the values in the main thread after all processing is finished
	    	setStops(stops);
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
	
	//getting the html tag
	private class XMLTask2 extends AsyncTask<Integer, String, String> {
        private String xml = null;
        private String url = "http://realtime.lextran.com/InfoPoint/map/GetRouteXml.ashx?RouteID=";
        
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
	        // The `xml` that you returned will be passed to this method.	
	    	
	    	
	    	Document doc = getDomElement(xml);
	    	NodeList nl = doc.getElementsByTagName(KEY_STOP);
	    	
	    	for (int i = 0; i < nl.getLength(); i++) {
	            Element e = (Element) nl.item(i);

	            if(e.getAttribute(KEY_LABEL).equals(stopSelected)){
	            	//this is the right stop, save the hmtl tag
	            	NotifyService.stopSelected = e.getAttribute(KEY_HTML);
	            }
	            
	        }
	    }
	
	}
	
}
