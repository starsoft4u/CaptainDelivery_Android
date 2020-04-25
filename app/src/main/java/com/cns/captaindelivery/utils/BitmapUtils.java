package com.cns.captaindelivery.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BitmapUtils {

    public synchronized static Bitmap getSafeDecodeBitmap(String strFilePath, int maxSize) {
        try {
            if (strFilePath == null)
                return null;
            // Max image size
            int IMAGE_MAX_SIZE = maxSize;

            File file = new File(strFilePath);
            if (file.exists() == false) {
                //DEBUG.SHOW_ERROR(TAG, "[ImageDownloader] SafeDecodeBitmapFile : File does not exist !!");
                return null;
            }

            BitmapFactory.Options bfo 	= new BitmapFactory.Options();
            bfo.inJustDecodeBounds 		= true;

            BitmapFactory.decodeFile(strFilePath, bfo);

            if (IMAGE_MAX_SIZE > 0)
                if(bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE) {
                    bfo.inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE
                            / (double) Math.max(bfo.outHeight, bfo.outWidth)) / Math.log(0.5)));
                }
            bfo.inJustDecodeBounds = false;
            bfo.inPurgeable = true;
            bfo.inDither = true;

            final Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, bfo);

            int degree = GetExifOrientation(strFilePath);

            return GetRotatedBitmap(bitmap, degree);
        }
        catch(OutOfMemoryError ex)
        {
            ex.printStackTrace();

            return null;
        }
    }

    public synchronized static int GetExifOrientation(String filepath) 	{
        int degree = 0;
        ExifInterface exif = null;

        try    {
            exif = new ExifInterface(filepath);
        } catch (IOException e)  {
            Log.e("superman", "cannot read exif");
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }

        return degree;
    }

    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) 	{
        if ( degrees != 0 && bitmap != null )     {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }

        return bitmap;
    }

    public static final Bitmap zoomBitmapByMin(Bitmap bm, int minValue) {

        try {
            int width = bm.getWidth();
            int height = bm.getHeight();

            float aspect = (float) width / height;
            float scaleWidth ;
            float scaleHeight ;

            if (width < height) {
                scaleWidth = minValue;
                scaleHeight = scaleWidth / aspect;
            } else {
                scaleHeight = minValue;
                scaleWidth = scaleHeight * aspect;
            }

            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(scaleWidth / width, scaleHeight / height);
            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    matrix, true);
			/* bm.recycle(); */
            if (resizedBitmap != bm) {
                bm.recycle();
                bm = null;
            }
            return resizedBitmap;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String encodeTobase64(Bitmap image)	{
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) 	{
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, String filepath) {
        if (bitmap == null)
            return false;
        if (filepath == null)
            return false;

        OutputStream outStream = null;

        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
            file = new File(filepath);
        }
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String saveSacledImage(Context context, Bitmap bitmap){
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "scaled_" + ts + "_";
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = File.createTempFile(imageFileName, ".jpg", storageDir);
            if (file != null) {
                if (BitmapUtils.saveBitmapToFile(bitmap, file.getAbsolutePath())){
                    return file.getAbsolutePath();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
