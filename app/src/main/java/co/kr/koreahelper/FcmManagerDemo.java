package co.kr.koreahelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import co.kr.koreahelper.Api.RetrofitClient;
import co.kr.koreahelper.Model.Result;
import co.kr.koreahelper.Model.RetrofitService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FcmManagerDemo extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s){
        super.onNewToken(s);
        Log.d("FCM_TEST(New token) : ", s);
        Log.d("FCM_TEST(New token) : ", s);
        Log.d("FCM_TEST(New token) : ", s);
        Log.d("FCM_TEST(New token) : ", s);

        final String ss = s;


        ((WebViewDemo)WebViewDemo.context_main).setMobileFcmPushTokenAsPref(s);

        ((WebViewDemo)WebViewDemo.context_main).tokenFromFcmManager = s;
        ((WebViewDemo)WebViewDemo.context_main).mWebView.post(new Runnable() {
            @Override
            public void run() {
                ((WebViewDemo)WebViewDemo.context_main).mWebView.loadUrl("javascript:setMobileFcmToken('"+ss+"')");
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title"); //firebase에서 보낸 메세지의 title
        String message = remoteMessage.getData().get("message");
        String test = remoteMessage.getData().get("test");

        Intent intent = new Intent(this, WebViewDemo.class);
        intent.putExtra("test",test);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channel = "channel";
            String channel_nm = "channelname";

            NotificationManager notichannel = (android.app.NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channelMessage = new NotificationChannel(channel, channel_nm,NotificationManager.IMPORTANCE_HIGH);
            channelMessage.setDescription("channeldescription");
            channelMessage.enableLights(true);

            channelMessage.enableVibration(true);
            channelMessage.setShowBadge(true);
            channelMessage.setVibrationPattern(new long[]{1000,1000});
            notichannel.createNotificationChannel(channelMessage);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channel)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setChannelId(channel)
                    .setAutoCancel(true)
                    .setFullScreenIntent(pendingIntent, true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(9999, notificationBuilder.build());
        }else{
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9999, notificationBuilder.build());

        }
    }

    public void setMyPrivateFcmToken(String tk){
        Map<String, Object> map = new HashMap<>();
        map.put("how", "setMyFcmToken");
        map.put("myfcm_token", tk);

        RetrofitService retrofitService = RetrofitClient.getClient(getApplicationContext()).create(RetrofitService.class);
        Call<Result> call_result = retrofitService.call_result("application/json", map.get("how").toString(), map);

        call_result.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }
}
