package com.cns.captaindelivery.utils;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class NotificationClass {
    private String CHANNEL_ID = "123";
    private String CHANNEL_NAME = "Your human readable notification channel name";
    private String CHANNEL_DESCRIPTION = "description";

    @RequiresApi(Build.VERSION_CODES.O)
    public void createMainNotificationChannel(Context c) {
        String id = CHANNEL_ID;
        String name = CHANNEL_NAME;
        String description = CHANNEL_DESCRIPTION;
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription (description);
        mChannel.enableLights(true);
        mChannel.setLightColor( Color.RED);
        mChannel.enableVibration(true);
        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        // NotificationManager mNotificationManager = c.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        mNotificationManager.createNotificationChannel(mChannel);

    }

    @RequiresApi(Build.VERSION_CODES.O)
    public String getMainNotificationId() {
        return CHANNEL_ID;
    }

}
