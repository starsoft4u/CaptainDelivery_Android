package com.cns.captaindelivery.receivers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.cns.captaindelivery.utils.NotificationClass;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        double latitude = Double.valueOf(intent.getStringExtra("latitude"));
        //speedspeedspeed
        double speed = Double.valueOf(intent.getStringExtra("speed"));
        double altitude = Double.valueOf(intent.getStringExtra("altitude"));


        System.out.println("broadcast latitude:" + latitude);
        System.out.println("broadcast speed:" + speed);
        System.out.println("broadcast altitude:" + altitude);


        //  double longitude = Double.valueOf(intent.getStringExtra("longitude"));
        //  Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

    }
}