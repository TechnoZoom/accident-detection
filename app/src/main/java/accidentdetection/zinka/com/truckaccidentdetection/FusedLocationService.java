package accidentdetection.zinka.com.truckaccidentdetection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FusedLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener /*SensorListener*/ /*ShakeListener.AccelerationChangeListener*/ {

    private boolean latestLocCallDone;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private int UPDATE_INTERVAL = 12000;
    private final int UPDATE_FAST_INTERVAL = 6000;
    public static final String LOCATION_SERVICE_RESULT = "com.service.location.result";
    public static final String LOCATION_LAT = "latitude";
    public static final String LOCATION_LONG = "longitude";
    public static final String ACCURACY = "accuracy";
    public static final String LOCATION_FETCHING_ERROR = "error";
    private float accX, accY, accZ;
    private boolean dataSent = false;
    private Location location;
    private ShakeListener shakeListener;

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private boolean accidentHappened;


    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shakeListener = new ShakeListener(this);
        //shakeListener.setOnAccChangeListener(this);
        latestLocCallDone = false;
        dataSent = false;
        Log.d("locTesting", "--------------------");
        Log.d("locTesting", "onStartCommand");
        buildGoogleApiClient();

        location = null;
        accY = 0;
        accX = 0;
        accZ = 0;
        accidentHappened = intent.hasExtra(SensorConstants.X_ACCELERATION);
        if (accidentHappened) {
            accX = intent.getFloatExtra(SensorConstants.X_ACCELERATION, 0);
            accY = intent.getFloatExtra(SensorConstants.Y_ACCELERATION, 0);
            accZ = intent.getFloatExtra(SensorConstants.Z_ACCELERATION, 0);
            shakeListener.setOnAccChangeListener(null);

        }
        return Service.START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("locTesting", "onDest");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d("locTesting", "removLoc");
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    FusedLocationService.this);
        }
        location = null;
        accY = 0;
        accX = 0;
        accZ = 0;
        shakeListener = null;
        accidentHappened = false;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_FAST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (IllegalStateException se) {
            latestLocCallDone = true;
            sendLocationFetchingError("Some error occurred ! Make Sure your GPS Location is enabled");
            stopLocationSendingService();
        }
    }

    private void stopLocationSendingService() {
        if (latestLocCallDone) {
            stopSelf();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("locTesting", "onLocChanged");
        if (location != null && !latestLocCallDone) {
            Log.d("locTesting", "onLocChanged :- " + "--" + location.getExtras() + "----- " + "speed:- " + String.valueOf(location.getSpeed() + "-----" + "accuracy:- " + location.getAccuracy() + "-----altitude:- " + location.getAltitude() + "------" + location.getLatitude()) + "  --- " + location.getLongitude());
            latestLocCallDone = true;
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, FusedLocationService.this);
            } else {
                mGoogleApiClient.connect();
            }
            this.location = location;
            if (accZ != 0 || accX != 0 || accY != 0) {
                sendResult(location);
            }

        }
    }

    private void sendResult(Location location) {
        if (dataSent) {
            Log.d("locTesting", "sendResultDataSentTrue");

            return;
        }
        Log.d("locTesting", "sendResultDataSentFalse");

        dataSent = true;
        RetrofitApis retrofitApis = RetrofitClient.getInstance().create(RetrofitApis.class);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gps_id", Utils.getDeviceId(this));
        jsonObject.addProperty("accelerometer_id", Utils.getDeviceId(this));
        jsonObject.addProperty("accelerometer_id", Utils.getDeviceId(this));
        jsonObject.addProperty("x_acc", accX);
        jsonObject.addProperty("y_acc", accY);
        jsonObject.addProperty("z_acc", accZ);
        if(location != null) {
            jsonObject.addProperty("latitude", location.getLatitude());
            jsonObject.addProperty("longitude", location.getLongitude());
        }
        jsonObject.addProperty("date", System.currentTimeMillis());
        Call call;
        if (accidentHappened) {
            call = retrofitApis.sendAccidentData(jsonObject);
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


        stopLocationSendingService();


        /*Intent intent = new Intent(LOCATION_SERVICE_RESULT);
        if (location != null) {
            intent.putExtra(LOCATION_LAT, location.getLatitude());
            intent.putExtra(LOCATION_LONG, location.getLongitude());
            intent.putExtra(ACCURACY, location.getAccuracy());
        }
        localBroadcastManager.sendBroadcast(intent);*/
    }

    private void sendLocationFetchingError(String message) {
        Intent intent = new Intent(LOCATION_SERVICE_RESULT);
        intent.putExtra(LOCATION_FETCHING_ERROR, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    /*@Override
    public void onSensorChanged(int sensor, float[] values) {


    }

    @Override
    public void onAccuracyChanged(int i, int i1) {

    }*/


   /* @Override
    public void onAccelChange(float accX, float accY, float accZ) {
        // Log.d("locTesting", "onAccelChange");
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        //if (location != null) {
            //Log.d("locTesting", "onAccelChangeLocNotNull");
        sendResult(location);
        //}
    }*/
}
