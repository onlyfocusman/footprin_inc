package co.kr.koreahelper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;

public class MyLocationListener implements LocationListener {

    private final Context mContext;

    public MyLocationListener(Context context){
        this.mContext = context;
    }
    @Override
    public void onProviderEnabled(String provider){
        int a = 110;
    }
    @Override
    public void onProviderDisabled(String provider) {
        int a = 110;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        int a = 111;
    }
    @Override
    public void onLocationChanged(Location location){

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        int a = 110;
        //locationManager.removeUpdates(this);

        //((WebViewDemo)mContext).setMyPositionFromAppToWeb();
    }


}
