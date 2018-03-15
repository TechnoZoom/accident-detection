package accidentdetection.zinka.com.truckaccidentdetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class SyncingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, FusedLocationService.class));
    }
}
