package co.kr.koreahelper.Api;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static String BASE_URL;
    private static volatile Retrofit retrofit = null;

    public static Retrofit getClient(Context context){
        BASE_URL = "https://koreahelper.co.kr:12334/";
        if(retrofit == null){
            synchronized (Retrofit.class) {
                if(retrofit == null) {
                    retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create()).build();
                }
            }
        }
        return retrofit;
    }
}
