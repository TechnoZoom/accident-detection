package accidentdetection.zinka.com.truckaccidentdetection;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface RetrofitApis {

    @POST(ServerConfig.SEND_SENSOR_DATA)
    Call<ResponseBody> sendSensorData(@Body JsonObject requestJsonObject);

    @POST(ServerConfig.SEND_ACCIDENT_DATA)
    Call<ResponseBody> sendAccidentData(@Body JsonObject requestJsonObject);

    @GET(ServerConfig.CONFIG)
    Call<ConfigResponse> getConfig();

    @POST(ServerConfig.GPS)
    Call<ResponseBody> sendGPS(@Body JsonObject requestJsonObject);

}
