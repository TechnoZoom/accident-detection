package accidentdetection.zinka.com.truckaccidentdetection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ShakeService extends Service implements ShakeListener.OnShakeListener, ShakeListener.AccelerationChangeListener {
    private ShakeListener mShaker;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeListener.AccelerationChangeListener accelerationChangeListener;
    public int check;
    private int accCount;
    private int sensCount;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onCreate() {

        super.onCreate();
        this.mSensorManager = ((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(this);
        mShaker.setOnAccChangeListener(this);
        Toast.makeText(ShakeService.this, "Service is created!", Toast.LENGTH_LONG).show();
        Log.d(getPackageName(), "Created the Service!");
        check=1;
    }
    @Override
    public void onShake() {
        if(check==1) {
            Toast.makeText(ShakeService.this, "SHAKEN!", Toast.LENGTH_LONG).show();
            final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(1500);
            playSound();
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }
    public void onDestroy(){
        super.onDestroy();
        check=0;
        Log.d(getPackageName(),"Service Destroyed.");
    }

    private void playSound() {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.proud);
        mp.start();

    }


    @Override
    public void onAccelChange(float accX, float accY, float accZ, String type) {
        // Log.d("locTesting", "onAccelChange");
     /*   this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;*/
        //if (location != null) {
        //Log.d("locTesting", "onAccelChangeLocNotNull");
        sendResult(accX, accY, accZ, type);
        //}
    }


    private void sendResult(double accX, double accY, double accZ, String type) {

        RetrofitApis retrofitApis = RetrofitClient.getInstance().create(RetrofitApis.class);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gps_id", Utils.getDeviceId(this));
        jsonObject.addProperty("accelerometer_id", Utils.getDeviceId(this));
        jsonObject.addProperty("accelerometer_id", Utils.getDeviceId(this));
        jsonObject.addProperty("x_acc", accX);
        jsonObject.addProperty("y_acc", accY);
        jsonObject.addProperty("z_acc", accZ);
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("date", System.currentTimeMillis());
        if(type !=null) {
            jsonObject.addProperty("counter", ++accCount);

        }
        else {
            jsonObject.addProperty("counter", ++sensCount);

        }
        Call call;
        if (type != null) {
            call = retrofitApis.sendAccidentData(jsonObject);
            Call call2 = retrofitApis.sendSensorData(jsonObject);

            call2.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });


        } else {
            call = retrofitApis.sendSensorData(jsonObject);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });




        /*Intent intent = new Intent(LOCATION_SERVICE_RESULT);
        if (location != null) {
            intent.putExtra(LOCATION_LAT, location.getLatitude());
            intent.putExtra(LOCATION_LONG, location.getLongitude());
            intent.putExtra(ACCURACY, location.getAccuracy());
        }
        localBroadcastManager.sendBroadcast(intent);*/
    }

}
