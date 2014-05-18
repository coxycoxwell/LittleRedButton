/*
 * 
 * Bogazici University
 * MS in Software Engineering
 * SWE 599 - Project
 * 
 * Mustafa Goksu GURKAS
 * ID: 2011719225
 * 
 * */

package tr.edu.boun.swe599.littleredbutton.maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import tr.edu.boun.swe599.littleredbutton.R;

import android.app.Dialog;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowOnMapActivity extends FragmentActivity implements LocationListener {

	// Displays the user's current GPS Location coordinates and nearest places on GoogleMaps
    GoogleMap mGoogleMap;    
    
    double mLatitude=0;
    double mLongitude=0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);        
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        }else { // Google Play Services are available
            // Getting reference to the SupportMapFragment
            SupportMapFragment fragment = ( SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            // Getting Google Map
            mGoogleMap = fragment.getMap();
            // Enabling MyLocation in Google Map
            mGoogleMap.setMyLocationEnabled(true);
 
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);
            if(location!=null){
                    onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
            
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location="+mLatitude+","+mLongitude);
            sb.append("&radius=1000");
            sb.append("&types=hospital|police");
            sb.append("&sensor=true");
            sb.append("&key=AIzaSyBvQDC1eJxcXusIsdmOBufjiZeKcGg8ONg");
            // Creating a new non-ui thread task to download Google place json data 
            PlacesTask placesTask = new PlacesTask();                                    
            // Invokes the "doInBackground()" method of the class PlaceTask
            placesTask.execute(sb.toString());
        }        
    }
    
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
                URL url = new URL(strUrl);                
                urlConnection = (HttpURLConnection) url.openConnection();                
                // Connecting to url 
                urlConnection.connect();                
                // Reading data from url 
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer sb  = new StringBuffer();
                String line = "";
                while( ( line = br.readLine())  != null){
                        sb.append(line);
                }
                data = sb.toString();
                br.close();
        }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
        }finally{
                iStream.close();
                urlConnection.disconnect();
        }
        return data;
    }         
    
    /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String>{
        String data = null;
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                 Log.d("Background Task",e.toString());
            }
            return data;
        }
        
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){            
            ParserTask parserTask = new ParserTask();
            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }
    
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
        JSONObject jObject;
        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {
            List<HashMap<String, String>> places = null;            
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();
        
            try{
                jObject = new JSONObject(jsonData[0]);
                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);
            }catch(Exception e){
                    Log.d("Exception",e.toString());
            }
            return places;
        }
        
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){            
            // Clears all the existing markers 
            mGoogleMap.clear();
            
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(mLatitude, mLongitude);
            markerOptions.position(latLng);
            markerOptions.title("You are here!");                
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mGoogleMap.addMarker(markerOptions);

            for(int i=0;i<list.size();i++){
                // Creating a marker
                markerOptions = new MarkerOptions();
                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);
                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));                
                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));
                // Getting name
                String name = hmPlace.get("place_name");
                // Getting types
                String types = hmPlace.get("types");
                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");
                latLng = new LatLng(lat, lng);
                // Setting the position for the marker
                markerOptions.position(latLng);
                // Setting the title for the marker. 
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);                
                
                if(types.contains("hospital"))
                	markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                else if(types.contains("police"))
                	markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                
                // Placing a marker on the touched position
                mGoogleMap.addMarker(markerOptions);            
            }        
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}