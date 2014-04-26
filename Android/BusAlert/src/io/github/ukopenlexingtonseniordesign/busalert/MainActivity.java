package io.github.ukopenlexingtonseniordesign.busalert;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

//The main menu for our app
public class MainActivity extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //click listeners for all the buttons
        View mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(this);
        View alertButton = findViewById(R.id.alert2_button);
        alertButton.setOnClickListener(this);
        View aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
    }
    
    //This decides the action the app takes when one of the buttons is pressed. It 
    //determines which button was pressed and then lunches the appropriate activity.
    public void onClick(View v){
    	switch (v.getId()){
        case R.id.map_button:
    		Intent map = new Intent(this, Map.class);
    		startActivity(map);
            break;
        case R.id.alert2_button:
    		Intent alert2 = new Intent(this, Alert2.class);
    		startActivity(alert2);
            break;
    	case R.id.about_button:
    		Intent about = new Intent(this, About.class);
    		startActivity(about);
    		break;
        case R.id.exit_button:
            finish();
            break;
    	}
    }
    
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

} 