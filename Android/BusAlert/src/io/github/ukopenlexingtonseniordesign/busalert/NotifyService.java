package io.github.ukopenlexingtonseniordesign.busalert;

//import io.github.ukopenlexingtonseniordesign.busalert.Map.HTMLTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
  
/*
 * This service is started when an Alarm has been raised
 * 
 * We pop a notification into the status bar for the user to click on
 * When the user clicks the notification a new activity is opened
 * 
 */
public class NotifyService extends Service {
	String departTimes;
	public static String stopSelected;
    /*
     * Class for clients to access
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }
 
    // Unique id to identify the notification.
    private static final int NOTIFICATION = 123;
    // Name of an intent extra we can use to identify if this service was started to create a notification  
    public static final String INTENT_NOTIFY = "com.blundell.tut.service.INTENT_NOTIFY";
    // The system notification manager
    private NotificationManager mNM;
 
    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
         
        showNotification();
         
        // We don't care if this service is stopped as we have already delivered our notification
        return START_NOT_STICKY;
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
 
    // This is the object that receives interactions from clients
    private final IBinder mBinder = new ServiceBinder();
 
    /**
     * Creates a notification and shows it in the OS drag-down status bar
     */
    private void showNotification() {
    	//get ETA for appropriate bus stop -- route independent (will show all incoming buses).
    	String htmlTag = stopSelected;
    	HTMLTask htmlTask = new HTMLTask();
	    htmlTask.execute(new String[] { htmlTag });
	    
	    // This is the ETA information to send to the user
        CharSequence ETAinfo = "Failed to Retrieve Times";
		try {
			ETAinfo = htmlTask.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
 
		//Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        
        builder.setContentTitle("Your Bus Notificaiton")
    	.setStyle(new NotificationCompat.BigTextStyle().bigText(ETAinfo))
        .setSmallIcon(R.drawable.alert_dark_frame);
    	Notification notification = builder.build();

        // Clear the notification when it is pressed
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
         
        // Send the notification to the system.
        mNM.notify(NOTIFICATION, notification);
         
        // Stop the service when we are finished
        stopSelf();
    }
	
	//Used in getting and parsing the departure times for a stop
	private class HTMLTask extends AsyncTask<String, String, String> {
	    private String url = "http://realtime.lextran.com/InfoPoint/departures.aspx?stopid=";
	        
		@Override
		protected String doInBackground(String... stop) {
			String html = null;
			//given route name and stop name I can go to route xml and get html tag which is unique stop number to give URL below.
			
			url = url + stop[0];
	        try {	        	
	            // defaultHttpClient
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
		        
	    	//Now parse the html and get out the times of departures for the stop
	    	departTimes = Helpers.parseDepartTimes(html);
			
			return departTimes;
		}
	       
	    //Callback method
	    @Override
	    protected void onPostExecute(String inHtml) {}
	}
	
}