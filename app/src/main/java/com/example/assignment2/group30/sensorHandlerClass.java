package com.example.assignment2.group30;

/**
 * Created by ayan_ on 3/5/2018.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class sensorHandlerClass extends Service implements SensorEventListener {

    private SensorManager accelManage;
    int index = 0;
    Boolean serviceFlag = true;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // TODO Auto-generated method stub
        Sensor mySensor = sensorEvent.sensor;
//        Log.d("flag", serviceFlag + "");

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && serviceFlag) {
            index++;
            sendMessageToActivity(sensorEvent.values[2], sensorEvent.values[1], sensorEvent.values[0]);
        } else {
            accelManage.unregisterListener(this);
        }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCreate() {
        Log.d("Start S", "started");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //k = 0;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        serviceFlag = false;
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


    private void sendMessageToActivity(float z, float y, float x) {
        Intent intent = new Intent("accel-values");
        // You can also include some extra data.
        //Log.d("message", "sent " + z);
        Bundle b = new Bundle();
        b.putFloat("z", z);
        b.putFloat("y", y);
        b.putFloat("x", x);
        intent.putExtras(b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
