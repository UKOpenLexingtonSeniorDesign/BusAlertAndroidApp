package io.github.ukopenlexingtonseniordesign.busalert;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Bundle;

/*				***READ THIS***
 * The order of calls is important to make sure we don't get a null pointer exception. We start with the OnSelectListener of the spinner,
 * once that completes we execute the AsyncTask from the listener callback function to retrieve the XML info, on the postExecute
 * function of the AsyncTask we call the method in Alert that sets the ArrayList of maps that is parsed. Now we have the info we need
 * in the main function. 
 */



  
public class Alert extends Activity{
	private String routeSelected;
	private String hour1Selected;
	private String minute1Selected;
	private String hour2Selected;
	private String minute2Selected;
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
		List<String> route_list = new ArrayList<String>();
		for (String s : routeID.keySet()) {
			route_list.add(s);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert);
		
		//Fill the first hour spinner
		Spinner hour1_spinner = (Spinner) findViewById(R.id.hour1_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> hour1_adapter = ArrayAdapter.createFromResource(this,
					R.array.hour_list, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		hour1_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		hour1_spinner.setAdapter(hour1_adapter);
		hour1_spinner.setOnItemSelectedListener(new Hour1SelectedListener());
				
		//Fill the first minute spinner
		Spinner minute1_spinner = (Spinner) findViewById(R.id.minute1_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> minute1_adapter = ArrayAdapter.createFromResource(this,
			        R.array.minute_list, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		minute1_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		minute1_spinner.setAdapter(minute1_adapter);
		minute1_spinner.setOnItemSelectedListener(new Minute1SelectedListener());
		
		//Fill the first hour spinner
		Spinner hour2_spinner = (Spinner) findViewById(R.id.hour2_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> hour2_adapter = ArrayAdapter.createFromResource(this,
					R.array.hour_list, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		hour2_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		hour2_spinner.setAdapter(hour1_adapter);
		hour2_spinner.setOnItemSelectedListener(new Hour2SelectedListener());
				
		//Fill the first minute spinner
		Spinner minute2_spinner = (Spinner) findViewById(R.id.minute2_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> minute2_adapter = ArrayAdapter.createFromResource(this,
			        R.array.minute_list, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		minute2_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		minute2_spinner.setAdapter(minute1_adapter);
		minute2_spinner.setOnItemSelectedListener(new Minute2SelectedListener());
		
	    //FIX!!!: Using a temporary fix to fill the route spinner (hard coded xml). Will not be able to track a new route that Lextran adds.
		// Create an ArrayAdapter using the string array and a default spinner layout
        Spinner route_spinner = (Spinner) findViewById(R.id.route_spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, route_list);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        route_spinner.setAdapter(dataAdapter);
		route_spinner.setOnItemSelectedListener(new MyItemSelectedListener());
	}
	
	//confused on what this function does. Seems like it's taking "stops" and setting it to itself
    public void setStops(ArrayList<HashMap<String, String>> inStops) {
    	stops = inStops;
    }
    
    public void setHour1(String inHour) {
    	hour1Selected = inHour;
    }
    
    public void setMinute1(String inMinute) {
    	minute1Selected = inMinute;
    }
    
    public void setHour2(String inHour) {
    	hour2Selected = inHour;
    }
    
    public void setMinute2(String inMinute) {
    	minute2Selected = inMinute;
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
	
	public class Hour1SelectedListener implements OnItemSelectedListener {
		
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        String hour = parent.getItemAtPosition(pos).toString();
	        
	        //Now save the input
	        setHour1(hour);	        
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	        // Do nothing.
	    }
	}
	
	public class Minute1SelectedListener implements OnItemSelectedListener {
		
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        String minute = parent.getItemAtPosition(pos).toString();
	        
	        //Now save the input
	        setMinute1(minute);	        
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	        // Do nothing.
	    }
	}
	
public class Hour2SelectedListener implements OnItemSelectedListener {
		
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        String hour = parent.getItemAtPosition(pos).toString();
	        
	        //Now save the input
	        setHour2(hour);	        
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	        // Do nothing.
	    }
	}
	
	public class Minute2SelectedListener implements OnItemSelectedListener {
		
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        String minute = parent.getItemAtPosition(pos).toString();
	        
	        //Now save the input
	        setMinute2(minute);	        
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
}


