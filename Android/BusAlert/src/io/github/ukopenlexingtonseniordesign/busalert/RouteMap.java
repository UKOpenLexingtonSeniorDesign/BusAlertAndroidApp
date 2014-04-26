package io.github.ukopenlexingtonseniordesign.busalert;

import java.util.HashMap;

/*Structure to hold the HashMap we use to map the route names to their IDs. For future implementations this
 * could be implemented dynamically by querying LexTran.*/
public class RouteMap {
	private static HashMap<String, Integer> routeID = new HashMap<String, Integer>();
	
	public RouteMap() {
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
	}
	
	public HashMap<String, Integer> getRoutes() {
		return routeID;
	}
}
