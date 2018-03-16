package accidentdetection.zinka.com.truckaccidentdetection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sendSensorDataInforeground();
        //scheduleSensorDataSendingAlarm();
        //startService(new Intent(getApplicationContext(), ShakeService.class));
        getConfig();
        //startServices();
    }


    private void scheduleSensorDataSendingAlarm() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, SyncingReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.currentThreadTimeMillis() + Config.SYNCING_INTERVAL,
                Config.SYNCING_INTERVAL, pi);
    }

    private void sendSensorDataInforeground() {

        final Handler handler = new Handler();
        final int delay = 2000;

        handler.postDelayed(new Runnable(){
            public void run(){
                startService(new Intent(MainActivity.this, FusedLocationService.class));
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void getConfig() {

        RetrofitApis retrofitApis = RetrofitClient.getInstance().create(RetrofitApis.class);
        showProgressDialog("Loading Config");
        Call<ConfigResponse> call = retrofitApis.getConfig();
        call.enqueue(new Callback<ConfigResponse>() {
            @Override
            public void onResponse(Call<ConfigResponse> call, Response<ConfigResponse> response) {
                ConfigResponse configResponse = response.body();
                SharedPreferences sharedPreferences = getSharedPreferences(StorageConstants.CONFIG_PREFS,
                        MODE_PRIVATE);
                dismissProgressDialog();
                sharedPreferences.edit().putLong(StorageConstants.ACCIDENT_THRESHOLD,configResponse.getAccident_threshold()).apply();
                sharedPreferences.edit().putLong(StorageConstants.HARD_ACC_THRESHOLD,configResponse.getAcc_threshold()).apply();
                sharedPreferences.edit().putLong(StorageConstants.HARSH_BRAKE_THRESHOLD,configResponse.getBreak_threshold()).apply();
                Toast.makeText(getApplicationContext(),"Config Loaded",Toast.LENGTH_SHORT).show();
                startServices();
            }

            @Override
            public void onFailure(Call<ConfigResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error Loading Config",Toast.LENGTH_SHORT).show();
                dismissProgressDialog();

            }
        });

    }

    private void startServices() {

        //startService(new Intent(MainActivity.this, FusedLocationService.class));
        sendSensorDataInforeground();
        //scheduleSensorDataSendingAlarm();
        startService(new Intent(MainActivity.this, ShakeService.class));
    }
}
