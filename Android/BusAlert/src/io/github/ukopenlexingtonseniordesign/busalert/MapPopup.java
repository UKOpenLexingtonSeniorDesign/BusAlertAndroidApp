package io.github.ukopenlexingtonseniordesign.busalert;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

//Custom popup for when a person clicks on a bus stop on the map
public class MapPopup implements InfoWindowAdapter {
	private LayoutInflater inflater;
	
	public MapPopup(LayoutInflater inInflater) {
		inflater = inInflater;
	}
	
	@Override
	public View getInfoWindow(Marker marker) {
	  return(null);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
	  View popup = inflater.inflate(R.layout.popup, null);

	  //Build the string we want and use one TextView to display it. The text to
	  //display will be set as the snippet of the marker.
	  TextView tv = (TextView)popup.findViewById(R.id.SnippetText);
	  tv.setText(marker.getSnippet());

	  return(popup);
	}
}
