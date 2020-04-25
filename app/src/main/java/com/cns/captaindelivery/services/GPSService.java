package com.cns.captaindelivery.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.utils.NotificationClass;
import com.cns.captaindelivery.PreferenceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";


    // Binder given to clients
    // Registered callbacks
    private static final String LOGSERVICE = "#######";
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    Context c = this;
    NotificationClass nc = new NotificationClass();
    public static int PacketSize;
    public static int LocationInterval;
    public static int LocationFastestInterval;
    /// private static final int drawableIcon = R.drawable.cast_ic_notification_small_icon;
    public static PendingIntent contentIntent;
    public static String NotificationTxt;
    public static String NotificationTitle;
    public static int drawable_small = R.drawable.ic_launcher;

    @Override
    public void onCreate() {
        super.onCreate();
        // building Google Api Client at the startup
        buildGoogleApiClient();

        //calling and building notification for ANDROID O within 5 seconds of the Service Startup.
        ForegroundServiceInitialize();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP_FOREGROUND_SERVICE.equals(action)){
                stopForegroundService();
            }
        }
        return START_NOT_STICKY;
    }


    private void stopForegroundService() {
        Log.d("Kang", "Stop foreground service.");
        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //Inntizlizing ArrayList
//        TrackLoc = new ArrayList < > ();
        // We already have taken the permission from the user so this is redundant here...
        @SuppressLint("MissingPermission") Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        startLocationUpdate();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Has been Suspended..", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, connectionResult.getErrorMessage().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        try {

            Log.e("kang-locations : ", String.valueOf(location.getLatitude())+", "+String.valueOf(location.getLongitude()));

            PreferenceManager.setCurrentLat(String.valueOf(location.getLatitude()));
            PreferenceManager.setCurrentLng(String.valueOf(location.getLongitude()));

            Intent broadcastIntent = new Intent("com.highbryds.tracker");
            //  broadcastIntent.setAction(MainActivity.mBroadcastStringAction);
            broadcastIntent.putExtra("latitude", String.valueOf(location.getLatitude()));
            broadcastIntent.putExtra("longitude", String.valueOf(location.getLongitude()));
            broadcastIntent.putExtra("speed", String.valueOf(location.getSpeed()));
            broadcastIntent.putExtra("time", String.valueOf(location.getTime()));
            broadcastIntent.putExtra("altitude", String.valueOf(location.getAltitude()));

            sendBroadcast(broadcastIntent);


        } catch (NullPointerException e) {
            Toast.makeText(GPSService.this, "Location has been disabled...", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private void initLocationRequest() {
        //these are the parameters defined.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LocationInterval);
        mLocationRequest.setFastestInterval(LocationFastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        //Getting all the paramters.
        initLocationRequest();
        //Requesting Location Updates..
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void stopLocationUpdate() {
        //TO Remove Location Udpates
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void ForegroundServiceInitialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Creating Channel
            nc.createMainNotificationChannel(this);
            //building Notification.
            Notification.Builder notifi = new Notification.Builder(getApplicationContext(), nc.getMainNotificationId());
            notifi.setSmallIcon(drawable_small);
            notifi.setContentTitle(NotificationTitle);
            notifi.setContentText(NotificationTxt);
            notifi.setContentIntent(contentIntent);
            //getting notification object from notification builder.
            Notification n = notifi.build();

            int mNotificationId = 001;

            NotificationManager mNotificationManager =
                    (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(mNotificationId, n);

            //  startting foreground
            startForeground(1, n);


        } else {
            //for devices less than API Level 26
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(NotificationTitle)
                    .setContentText(NotificationTxt)
                    .setSmallIcon(drawable_small)
                    .setContentIntent(contentIntent)
                    .setOngoing(true).build();
            startForeground(1, notification);
        }


    }


}
