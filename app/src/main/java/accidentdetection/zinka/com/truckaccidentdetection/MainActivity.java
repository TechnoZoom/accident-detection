package accidentdetection.zinka.com.truckaccidentdetection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sendSensorDataInforeground();
        //scheduleSensorDataSendingAlarm();
        startService(new Intent(getApplicationContext(), ShakeService.class));
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
}
