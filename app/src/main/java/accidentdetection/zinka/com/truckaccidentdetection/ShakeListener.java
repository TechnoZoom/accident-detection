package accidentdetection.zinka.com.truckaccidentdetection;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorListener
{
    private static final double FORCE_THRESHOLD = 15;
    private static final int TIME_THRESHOLD = 75;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 150;
    private static final int SHAKE_COUNT = 1;


    private SensorManager mSensorMgr;
    private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;

    private AccelerationChangeListener accelerationChangeListener;

    public interface OnShakeListener
    {
        public void onShake();
    }

    public ShakeListener(Context context)
    {
        mContext = context;
        resume();
    }

    public void setOnShakeListener(OnShakeListener listener)
    {
        mShakeListener = listener;
    }

    public void setOnAccChangeListener(AccelerationChangeListener listener)
    {
        accelerationChangeListener = listener;
    }

    public void resume() {
        mSensorMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            mSensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void pause() {
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            mSensorMgr = null;
        }
    }

    public void onAccuracyChanged(int sensor, int accuracy) { }

    public void onSensorChanged(int sensor, float[] values)
    {
        if (sensor != SensorManager.SENSOR_ACCELEROMETER) return;
        long now = System.currentTimeMillis();
/*
        if(accelerationChangeListener != null) {
            accelerationChangeListener.onAccelChange(values[SensorManager.DATA_X], values[SensorManager.DATA_Y],
                    values[SensorManager.DATA_Z]);
        }*/

        if ((now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD) {
            long diff = now - mLastTime;
            double oldNetAcc = Math.sqrt(Math.pow(Math.abs(mLastX),2) + Math.pow(Math.abs(mLastZ),2));
            double newAcc =  Math.sqrt(Math.pow(values[SensorManager.DATA_X], 2)  + Math.pow(values[SensorManager.DATA_Z],2));
            double netDiff = newAcc - oldNetAcc;
            float xDiff = values[SensorManager.DATA_X] - mLastX;
            //Log.d("XAcccDiff", String.valueOf(values[SensorManager.DATA_X] - mLastX));



            float speed = Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000;
            if (netDiff > FORCE_THRESHOLD ) {
                //if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener != null) {
                        Intent intent = new Intent(mContext,FusedLocationService.class);
                        intent.putExtra(SensorConstants.X_ACCELERATION, values[SensorManager.DATA_X]);
                        intent.putExtra(SensorConstants.Y_ACCELERATION, values[SensorManager.DATA_Y]);
                        intent.putExtra(SensorConstants.Z_ACCELERATION, values[SensorManager.DATA_Z]);
                        accelerationChangeListener.onAccelChange(values[SensorManager.DATA_X], values[SensorManager.DATA_Y],
                                values[SensorManager.DATA_Z], "High Acc");
                        //mContext.startService(intent);
                        //playSound();
                        mShakeListener.onShake();
                    }
               // }
                mLastForce = now;
            }

            else if(netDiff < (-FORCE_THRESHOLD)) {
                accelerationChangeListener.onAccelChange(values[SensorManager.DATA_X], values[SensorManager.DATA_Y],
                        values[SensorManager.DATA_Z], "low Acc");
                mShakeListener.onShake();
            }

            else {

                accelerationChangeListener.onAccelChange(values[SensorManager.DATA_X], values[SensorManager.DATA_Y],
                        values[SensorManager.DATA_Z], null);
            }
            mLastTime = now;
            mLastX = values[SensorManager.DATA_X];
            mLastY = values[SensorManager.DATA_Y];
            mLastZ = values[SensorManager.DATA_Z];
        }
    }

    public interface AccelerationChangeListener {
        void onAccelChange(float accX, float accY, float accZ, String type);
    }


}