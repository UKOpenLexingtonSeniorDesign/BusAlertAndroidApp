package io.github.ukopenlexingtonseniordesign.busalert;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

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

//how do we overlay the parsed data onto the map? 
//Am I even parsing the right info here? 
//Should we be using the KML from the "segments" element information?  
public class Alert extends Activity{
	private String routeSelected;
	ArrayList<HashMap<String, String>> stops;
	static final String KEY_ROUTE = "route";
	static final String KEY_STOPS = "stops";
	static final String KEY_STOP = "stop";
	static final String KEY_SEGMENTS = "segments";
	static final String KEY_LNG = "lng";
	static final String KEY_LABEL = "label";
	static final String KEY_LAT = "lat";
	static final String KEY_HTML = "html";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert);
		
	    //FIX!!!: Using a temporary fix to fill the route spinner (hard coded xml). Will not be able to track a new route that Lextran adds.
		Spinner spinner = (Spinner) findViewById(R.id.route_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.route_array, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyItemSelectedListener());
	}
	
    public void setStops(ArrayList<HashMap<String, String>> inStops) {
    	stops = inStops;
    	int foo = 5;
    }
	
	public class MyItemSelectedListener implements OnItemSelectedListener {
		
		//Callback function
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        routeSelected = parent.getItemAtPosition(pos).toString();
	        
	        //Now call the XML getter
			XMLTask task = new XMLTask();
			int routeInt = Integer.parseInt(routeSelected);		//Cast to int
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
	    	
		    //not sure if putting the xml information in an arraylist of hashmaps is the best way to do this
		    ArrayList<HashMap<String, String>> stops = new ArrayList<HashMap<String, String>>();
	    	Document doc = getDomElement(xml);
	    	//get each stop
	    	NodeList nl = doc.getElementsByTagName(KEY_STOPS);    	
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
	    	
	    	int foo = 5;
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


