package io.github.ukopenlexingtonseniordesign.busalert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


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

} 