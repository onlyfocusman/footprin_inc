package co.kr.koreahelper.Model;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitService {

    @POST("/base/portal/member/makeFcmToken.do")
    Call<Result> call_result(@Header("Content-Type") String Content_type, @Path("how") String how, @Body Map<String, Object> params);
}
