package co.kr.koreahelper;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class GpsTracker extends Service implements LocationListener {
    private final Context mContext;
    private WebViewDemo webViewDemo;

    Location location;
    double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    //private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final long MIN_TIME_BW_UPDATES = 1000;

    protected LocationManager locationManager;

    public GpsTracker(Context context){
        this.mContext = context;
        webViewDemo = (WebViewDemo)this.mContext;
        getLocation();
        ((WebViewDemo)mContext).setMyPositionToMain(location);
    }
    public Location getLocation(){
        try{
            locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



            if(!isGPSEnabled && !isNetworkEnabled){
                //둘중 하나라도 안된다면 권한 요청한다.

            }else{
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
                if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
                    //둘다 되면
                }else{
                    return null;
                }

                LocationListener locationListener = new MyLocationListener(mContext);

                if(isGPSEnabled){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    if(locationManager != null){
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if(isNetworkEnabled){
                    if(location == null){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                        if(locationManager != null){
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if(location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            Log.d("@@@", ""+e.toString());
        }

        return location;
    }

    @Override
    public void onProviderEnabled(String provider){

    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){

    }
    @Override
    public void onLocationChanged(Location location){
        this.latitude = location.getLatitude();
        this.longitude = location.getLatitude();
        //locationManager.removeUpdates(this);

        //((WebViewDemo)mContext).setMyPositionToMain(location);

    }
    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }
}
