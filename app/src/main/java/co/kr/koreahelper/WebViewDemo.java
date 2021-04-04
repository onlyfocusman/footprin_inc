package co.kr.koreahelper;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.io.File;
import java.util.List;

public class WebViewDemo extends AppCompatActivity {

    public static Context context_main;
    public static String tokenFromFcmManager = "";

    private SharedPreferences sharedPreferences;
    private String webId = "";

    public double latitude;
    public double longitude;

    public WebView mWebView;
    public boolean mStarted = false;
    private WebSettings mWebSettings;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;


    private static final String TAG = "footprin_alert";

    public final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    public final static int FILECHOOSER_LOLLIPOP_REQ_CODE= 2002;
    public ValueCallback<Uri> filePathCallbakNormal;
    public ValueCallback<Uri[]> filePathCallbakLollipop;
    private Uri cameraImageUri = null;


    private GpsTracker gpsTracker = null;

    private static final int GPS_ENABLE_REQUEST_CODE = 2003;
    private static final int UPDATE_INTERVAL_MS = 10000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 5000;
    private static final int PERMISSIONS_REQUEST_CODE = 100;




    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private static final String [] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int INITIAL_REQUEST = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        checkRuntimePermission();

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        if(!checkLocationServicesStatus()){
            showDialogLocationServiceSetting();
        }

        context_main = this;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean accCrsLocation = canAccessCoarseLocation();
            boolean accFneLocation = canAccessFineLocation();
            boolean accNotiPolicy = canAccessNotification();
            boolean accReadExtnal = canAccessReadExtnal();
            boolean accWriteExtnal = canAccessWriteExtnal();
            if(!accCrsLocation || !accFneLocation || !accNotiPolicy || !accReadExtnal || !accWriteExtnal){
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }

        setContentView(R.layout.activity_web_view_demo);
        mWebView = findViewById(R.id.wvLayout);

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback){
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });

        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        mWebSettings.setGeolocationEnabled(true);

        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setSupportZoom(true);
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setDisplayZoomControls(true);
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebSettings.setDomStorageEnabled(true);

        mWebSettings.setDefaultFontSize(14);

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon){
                Log.d(TAG, "(WEBVIEW)onPageStarted : " + url);
                super.onPageStarted(view, url, favicon);

            }
            @Override
            public void onPageFinished(WebView view, String url){
                super.onPageFinished(view, url);
                Log.d(TAG, "(WEBVIEW)onPageFinished");
                if(webId != "" && webId != null){
                    mWebView.loadUrl("javascript:loginCheckFromAppWithSharedPref('"+webId+"')");
                }
            }

        });

        //For Android 5.0+ 카메라 - input type="file" 태그를 선택 했을 때 반응 설정
        mWebView.setWebChromeClient(new WebChromeClient(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams){
                Log.d("MainActivity","5.0+");
                Log.d("MainActivity","5.0+");

                //Callback초기화(중요)
                if(filePathCallbakLollipop != null){
                    filePathCallbakLollipop.onReceiveValue(null);
                    filePathCallbakLollipop = null;
                }
                filePathCallbakLollipop = filePathCallback;
                boolean isCapture = fileChooserParams.isCaptureEnabled();
                runCamera(isCapture);
                return true;
            }

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback){
                callback.invoke(origin, true, false);
            }
        });
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        webId = sharedPreferences.getString("SAVE_LOGIN_DATA_USER_ID", "");

        mWebView.addJavascriptInterface(new AndroidBridge(context_main),"android2");

        //mWebView.loadUrl("https://192.168.35.129:8443/base/portal/mongo/loginView.do");

        mWebView.loadUrl("https://koreahelper.co.kr:8443/base/portal/mongo/loginView.do");
        //mWebView.loadUrl("https://koreahelper.co.kr:12334/kor/index.do");

        //gpsTracker = new GpsTracker(WebViewDemo.this);

        if(webId != "" && webId != null){
            mWebView.loadUrl("javascript:loginCheckFromAppWithSharedPref('"+webId+"')");
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
    }

    public void setMyPositionToMain(final Location location){

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if(mWebView != null && mStarted == true){
            mWebView.loadUrl("javascript:setMyPositionWebFromAndroid('"+this.latitude+"','"+this.longitude+"')");
        }

        //mWebView.loadUrl("javascript:setMyPositionWebFromAndroid('"+location.getLatitude()+"','"+location.getLongitude()+"')");

        /*mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:setMyPositionFromAppToWeb('"+location.getLatitude()+"','"+location.getLongitude()+"')");
            }
        });*/
    }
    public void setMyPositionFromAppToWeb(){
        //mWebView.loadUrl("javascript:setMyPositionWebFromAndroid('"+this.latitude+"','"+this.longitude+"')");
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:setMyPositionWebFromAndroid('"+latitude+"','"+longitude+"')");
            }
        });
        mStarted = true;
    }

    public void setStartMyLocationUpdate(){
        startLocationUpdates();
    }

    //액티비티가 종료될 때 결과를 받아서 파일을 전송할때 호출
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        switch (requestCode)
        {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성화 했는지 검사
                if(checkLocationServicesStatus()){
                    if(checkLocationServicesStatus()){
                        Log.d("@@@","onActivityResult : GPS 활성화 되어있음.");
                        checkRuntimePermission();
                        return;
                    }
                }
                break;
            case FILECHOOSER_NORMAL_REQ_CODE:
                if(resultCode == RESULT_OK){
                    if(filePathCallbakNormal == null) return;
                    Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                    //onReceiveValue 로 파일을 전송한다.
                    filePathCallbakNormal.onReceiveValue(result);
                    filePathCallbakNormal = null;
                }
                break;
            case FILECHOOSER_LOLLIPOP_REQ_CODE:
                if(resultCode == RESULT_OK){
                    if(filePathCallbakLollipop == null) return;
                    if(data == null)
                        data = new Intent();
                    if(data.getData() == null)
                        data.setData(cameraImageUri);

                    filePathCallbakLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    filePathCallbakLollipop = null;
                }else{
                    //resultCode에 RESULT_OK가 들어오지 않으면 null이 처리가 안되서 다시 input을 누르면 반응이없어서 추가함.
                    if(filePathCallbakLollipop != null){
                        filePathCallbakLollipop.onReceiveValue(null);
                        filePathCallbakLollipop = null;
                    }
                    if(filePathCallbakNormal != null){
                        filePathCallbakNormal.onReceiveValue(null);
                        filePathCallbakNormal = null;
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //카메라 기능 구현
    private void runCamera(boolean _isCapture){
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        File path = getFilesDir();
        File file = new File(path, "sample.png"); //카메라찍었을때 저장되는 파일명 아무거나로 지정함(일단)
        //File 객체의 URI를 얻는다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            String strpa = getApplicationContext().getPackageName();
            cameraImageUri = FileProvider.getUriForFile(this, strpa + ".fileprovider", file);
        }else{
            cameraImageUri = Uri.fromFile(file);
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        if(!_isCapture){
            //선택 팝업 카메라, 갤러리 둘다 띄우고 싶을때
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            String pickTitle = "사진을 가져올 방법을 선택하세요.";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
            //카메라 intent 포함시키기..
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intentCamera});
            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }else{
            //바로 카메라 실행
            startActivityForResult(intentCamera, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }
    }

    public void makeFcmToken(String tk){
        Log.d("진짜토큰??", tokenFromFcmManager);
        Log.d("진짜토큰??", tokenFromFcmManager);
        Log.d("진짜토큰??", tokenFromFcmManager);
        mWebView.loadUrl("javascript:setMobileFcmToken('"+tk+"')");
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showDialogLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(WebViewDemo.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱 사용을 위해서는 위치 서비스가 필요합니다.\n" + "위치설정 수정을 하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    void checkRuntimePermission(){
        //런타임 퍼미션 처리
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(WebViewDemo.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(WebViewDemo.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED
                && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            //위치값 모두 가져올 수 이뜸 ㅋㅋ
            int asdsad = 1;
        }else{
            //퍼미션 요청을 허용한 적이 없다면 여기서 다시 요청을 한다.

            //거부한적이 있는경우
            if(ActivityCompat.shouldShowRequestPermissionRationale(WebViewDemo.this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(WebViewDemo.this, "이 앱이 실행되려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(WebViewDemo.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }else{
                //거부한적이 없는경우 바로 퍼미션 요청을 진행
                ActivityCompat.requestPermissions(WebViewDemo.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }

        }
    }

    private void startLocationUpdates(){
        if(!checkLocationServicesStatus()){
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogLocationServiceSetting();
        }else{
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(WebViewDemo.this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(WebViewDemo.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "StartLocationUpdates : permission not allowed");
                return;
            }
            Log.d(TAG, "StartLocationUpdates : call mFusedLocationCliend.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            int asdf = 101020;
            asdf = asdf + 1;
        }
    }

    LocationCallback locationCallback = new LocationCallback(){
      @Override
      public void onLocationResult(LocationResult locationResult){
          super.onLocationResult(locationResult);

          List<Location> locationList = locationResult.getLocations();
          if(locationList.size() > 0){
              location = locationList.get(locationList.size() - 1);
              //location = locationList.get(0);

              double lat = location.getLatitude();
              double lon = location.getLongitude();

              latitude = lat;
              longitude = lon;

              setMyPositionFromAppToWeb();

          }
      }
    };

    public void setPrefDataFromWeb(String pref_SessionID){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SAVE_LOGIN_DATA_USER_ID", pref_SessionID);
        editor.apply();
    }

    public void setMobileFcmPushTokenAsPref(String tk){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SAVE_MOBILE_FCM_TOKEN", tk);
        editor.apply();
    }

    public String getMobileFcmToken(){
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        String fcmToken = sharedPreferences.getString("SAVE_MOBILE_FCM_TOKEN", "");
        return fcmToken;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessFineLocation(){
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessCoarseLocation(){
        return (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessNotification(){
        return (hasPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessReadExtnal(){
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessWriteExtnal(){
        return (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm){
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }
}
