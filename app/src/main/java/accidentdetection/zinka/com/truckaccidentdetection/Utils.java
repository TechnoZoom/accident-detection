package accidentdetection.zinka.com.truckaccidentdetection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static void startAlarm(Context context, Class alarmClass, int timeInSeconds) {

        Intent alarmIntent = new Intent(context, alarmClass);
        boolean alarmRunning = (PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmRunning) {
            long currentTime = SystemClock.elapsedRealtime();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, currentTime, TimeUnit.SECONDS.toMillis(timeInSeconds), pendingIntent);
        }
    }
}
