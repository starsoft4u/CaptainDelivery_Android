package com.cns.captaindelivery.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class Utils {

    public static String getFileName(URL extUrl) {
        String filename = "";
        String path = extUrl.getPath();
        String[] pathContents = path.split("[\\\\/]");
        if (pathContents != null) {
            int pathContentsLength = pathContents.length;
            System.out.println("Path Contents Length: " + pathContentsLength);
            for (int i = 0; i < pathContents.length; i++) {
                System.out.println("Path " + i + ": " + pathContents[i]);
            }
            String lastPart = pathContents[pathContentsLength - 1];
            String[] lastPartContents = lastPart.split("\\.");
            if (lastPartContents != null && lastPartContents.length > 1) {
                int lastPartContentLength = lastPartContents.length;
                System.out
                        .println("Last Part Length: " + lastPartContentLength);
                String name = "";
                for (int i = 0; i < lastPartContentLength; i++) {
                    System.out.println("Last Part " + i + ": "
                            + lastPartContents[i]);
                    if (i < (lastPartContents.length - 1)) {
                        name += lastPartContents[i];
                        if (i < (lastPartContentLength - 2)) {
                            name += ".";
                        }
                    }
                }
                String extension = lastPartContents[lastPartContentLength - 1];
                filename = name + "." + extension;
                System.out.println("Name: " + name);
                System.out.println("Extension: " + extension);
                System.out.println("Filename: " + filename);
            }
        }
        return filename;
    }

    public static String getPathFromUri(Context context, Uri uri){
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        return filePath;
    }

    public static Bitmap getRoundedShape(Bitmap bitmap, int width, int height) {
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);
        paint.setDither(true);
        paint.setShader(shader);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        return circleBitmap;
    }


    /**
     * Print hash key
     */
    public static void printHashKey(Context context) {
        try {
            String TAG = "needman";
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e(TAG, "keyHash: " + keyHash);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    /*
    public static double getDistanceInMiles(LatLng pos1, LatLng pos2){
        double distance = SphericalUtil.computeDistanceBetween(pos1, pos2);
        final double meterToMiles = 0.000621371D;
        return distance * meterToMiles;
    }
*/
    public static double getDistanceInMeters(LatLng pos1, LatLng pos2){
        double distance = SphericalUtil.computeDistanceBetween(pos1, pos2);
        return distance;
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * To check which of current locations got by GPS and Network is best.
     * @param location : last location
     * @param currentBestLocation
     * @return
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static ComponentName isServiceExisted(Context context, String className)	{
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> serviceList =
                activityManager.getRunningServices(Integer.MAX_VALUE);

        if(!(serviceList.size() > 0))
        {
            return null;
        }

        for(int i = 0; i < serviceList.size(); i++)
        {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;

            if(serviceName.getClassName().equals(className))
            {
                return serviceName;
            }
        }
        return null;
    }



    public static void getHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String strHashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("Kang", "HashKey = "+strHashKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static boolean downloadFile_with_Get(String url, String fileFullPath)   {
        if (url.equals("")) return false;

        int Read;
        int readByte = 0 ;
        double perByte = 0 ;
        int prevperByte = 0;

        try {
            File file = new File(fileFullPath);

            if (file.exists()) {
                file.delete();
                file = new File(fileFullPath);
            }

            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();

            int len = conn.getContentLength();
            InputStream is = conn.getInputStream();
            //byte[] raster = new byte[is.available() + 1];
            byte[] raster = new byte[20480];

            FileOutputStream fos = new FileOutputStream(file);

            for (;;) {
                Read = is.read(raster);
                if (Read <= 0) 	break;

                readByte += Read;
                perByte =  (double)readByte / (double)len * 100.0 ;

                if ( (int)perByte != prevperByte) 	prevperByte = (int)perByte ;

                fos.write(raster,0, Read);
            }

            is.close();
            fos.close();
            conn.disconnect();
            if (raster != null) raster = null;
        } catch (Exception e) {
            File file = new File(fileFullPath);
            if (file.exists())	file.delete();
            return false;
        }
        return true;
    }

    public static void hide_keyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();

            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e){

        }
    }

    public static void show_keyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        } catch (Exception e){

        }
    }
}
