package io.github.ukopenlexingtonseniordesign.busalert;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

//Place for helper functions
public class Helpers {
    //Helper function that will be used in Alert2.java and Map.java
	//Java pre-built helper tools that are used to parse XML.
	static public Document getDomElement(String xml){
		Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            //Set the input to our xml string
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
            
        	//return DOM
            return doc;
	}
	
	//Helper function used to parse LexTran HTML docs
	//Take in the html info for a stop and parse out the routes and times that busses will be arriving
    static public String parseDepartTimes(String inHtml) {    	
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
