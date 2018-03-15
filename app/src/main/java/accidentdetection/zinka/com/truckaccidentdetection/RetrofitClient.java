package accidentdetection.zinka.com.truckaccidentdetection;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {

    //TODO:Add base url
    public static final String BASE_URL = "http://13.229.47.113:8080/services/";
    private static Retrofit mRetrofit = null;

    public static Retrofit getInstance() {
        if (mRetrofit == null) {
            mRetrofit = buildRetrofit();
        }
        return mRetrofit;
    }
    private static Retrofit buildRetrofit() {

        OkHttpClient.Builder httpClient;
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(70, TimeUnit.SECONDS)
                    .readTimeout(70, TimeUnit.SECONDS)
                    .writeTimeout(70, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit;
    }
}
