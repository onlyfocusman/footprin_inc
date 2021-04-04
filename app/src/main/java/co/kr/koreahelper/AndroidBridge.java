package co.kr.koreahelper;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AndroidBridge {

    private final Context mContext;


    public AndroidBridge(Context context){
        this.mContext = context;
    }

    @JavascriptInterface
    public void callAndroid(final String arg){

        String str = "";
        str = arg;
        str = arg;
        int a = 3;

        ((WebViewDemo)mContext).setMyPositionFromAppToWeb();

        ((WebViewDemo)mContext).setStartMyLocationUpdate();

    }

    @JavascriptInterface
    public void setLoginStayData(String sessWeb_Id){
        String str = sessWeb_Id;
        ((WebViewDemo)mContext).setPrefDataFromWeb(sessWeb_Id);
    }

    @JavascriptInterface
    public String getFcmMobileToken(){
        String fcmTk = ((WebViewDemo)mContext).getMobileFcmToken();
        return fcmTk;
    }
}
