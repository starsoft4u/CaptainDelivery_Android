package com.cns.captaindelivery.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.models.ResultPersonalInfo;
import com.cns.captaindelivery.utils.BitmapUtils;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.utils.Utils;
import com.cns.captaindelivery.widgets.CircleImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class PersonalInformationActivity extends _BaseFragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final int    PERMISSIONS_REQUEST_STORAGE = 10;
    private static final int    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 11;

    private static final int    SELECT_PICTURE = 0;
    private static final int    TAKE_PICTURE = 1;

    private static final int DEFAULT_ZOOM = 15;

    boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;

    ApiInterface apiInterface;

    LatLng mLatLng = null;
    Marker mMarker = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    String m_strCountry, m_strCity, m_strStreet;

    String m_strPhotoPath = "";
    String m_strScaledPhotoPath = "";
    File mFile = null;

    CircleImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_personal_information);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        //Toast.makeText(this, R.string.msg_address_map_hint, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMarker != null)
                    mMarker.remove();
                mLatLng = latLng;
                addMarkerAndUpdateAddr();
            }
        });
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void initView(){
        mImageView = findViewById(R.id.imgPhoto);

        findViewById(R.id.imgBtnBack).setVisibility(View.VISIBLE);
        findViewById(R.id.imgBtnBack).setOnClickListener(this);
        findViewById(R.id.imgBtnBack).setOnClickListener(this);
        findViewById(R.id.layoutBtnTakePhoto).setOnClickListener(this);
        findViewById(R.id.imgBtnTakePhoto).setOnClickListener(this);
        findViewById(R.id.imgPhoto).setOnClickListener(this);
        findViewById(R.id.btnContinue).setOnClickListener(this);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ||ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PersonalInformationActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_STORAGE);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                }
                updateLocationUI();
                break;
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showTakePhotoKindDlg();
                } else {
                    Toast.makeText(PersonalInformationActivity.this, R.string.msg_err_permission, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }


    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted ) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            if(mLatLng == null){
                                Log.e("kang", "TTTTEEEESSSSS");
                                Location location = (Location)task.getResult();
                                if (location == null)
                                    return;
                                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM));
                                addMarkerAndUpdateAddr();
                            }

                        } else {
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void addMarkerAndUpdateAddr(){
        mMarker = mMap.addMarker(new MarkerOptions().position(mLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)));
        m_strCountry = ""; m_strCity = ""; m_strStreet = "";
        showAddrInfo();
        getAddressFromLocation(mLatLng.latitude, mLatLng.longitude);
    }

    private void showAddrInfo(){
        ((EditText)findViewById(R.id.editCountry)).setText(m_strCountry);
        ((EditText)findViewById(R.id.editCity)).setText(m_strCity);
        ((EditText)findViewById(R.id.editStreet)).setText(m_strStreet);
    }

    public void getAddressFromLocation(final double latitude, final double longitude) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(PersonalInformationActivity.this, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        m_strCountry = address.getCountryName();
                        m_strCity = address.getLocality();
                        //m_strStreet = address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "";
                        m_strStreet = address.getThoroughfare();
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showAddrInfo();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    Log.e("Location Address Loader", "Unable connect to Geocoder", e);
                }
            }
        };
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layoutBtnTakePhoto:
            case R.id.imgPhoto:
            case R.id.imgBtnTakePhoto:
                if (checkPermission()){
                    showTakePhotoKindDlg();
                }
                break;
            case R.id.btnContinue:
                if (checkValidation()){
                    procPostPersonalInformation();
                }
                break;
            case R.id.imgBtnBack:
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonalInformationActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.msg_delete_account)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                procDeleteAccount();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
    }

    private void showTakePhotoKindDlg(){
        new AlertDialog.Builder(PersonalInformationActivity.this)
                .setItems(R.array.choose_photo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                dialogInterface.dismiss();
                                pickPhoto();
                                break;
                            case 1:
                                dialogInterface.dismiss();
                                capturePhoto();
                                break;
                        }
                    }
                })
                .show();
    }

    public void pickPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select"), SELECT_PICTURE);
    }

    public void capturePhoto(){
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "Photo_" + ts+"_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (mFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.cns.captaindelivery.fileprovider", mFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Bitmap bitmap = null;
            switch (requestCode){
                case SELECT_PICTURE:
                    String selectAbpath = Utils.getPathFromUri(PersonalInformationActivity.this, data.getData());
                    //Picasso.with(this).load(new File(selectAbpath)).noFade().into(mImgPhoto);
                    m_strPhotoPath = selectAbpath;
                    bitmap = BitmapUtils.getSafeDecodeBitmap(m_strPhotoPath, 400);
                    if (bitmap != null) {
                        findViewById(R.id.layoutBtnTakePhoto).setVisibility(View.GONE);
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageBitmap(bitmap);
                        m_strScaledPhotoPath = BitmapUtils.saveSacledImage(PersonalInformationActivity.this, bitmap);
                    }

                    break;
                case TAKE_PICTURE:
                    if (mFile.exists()){
                        //Picasso.with(this).load(mFile).noFade().into(mImgPhoto);
                        m_strPhotoPath = mFile.getAbsolutePath();
                    }
                    bitmap = BitmapUtils.getSafeDecodeBitmap(m_strPhotoPath, 400);
                    if (bitmap != null) {
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageBitmap(bitmap);
                        m_strScaledPhotoPath = BitmapUtils.saveSacledImage(PersonalInformationActivity.this, bitmap);
                    }
                    break;
            }
        }
    }



    private boolean checkValidation() {
        if (TextUtils.isEmpty(m_strScaledPhotoPath)) {
            Toast.makeText(PersonalInformationActivity.this, R.string.msg_err_take_photo, Toast.LENGTH_SHORT).show();
            return  false;
        }

        EditText editCountryView =  findViewById(R.id.editCountry);
        EditText editCityView =  findViewById(R.id.editCity);
        EditText editStreetView = findViewById(R.id.editStreet);

        editCountryView.setError(null);
        editCityView.setError(null);
        editStreetView.setError(null);

        m_strCountry = editCountryView.getText().toString();
        m_strCity = editCityView.getText().toString();
        m_strStreet = editStreetView.getText().toString();

        if (TextUtils.isEmpty(m_strCountry)) {
            editCountryView.setError(getString(R.string.error_field_required));
            editCountryView.requestFocus();
            return  false;
        }
        if (TextUtils.isEmpty(m_strCity)) {
            editCityView.setError(getString(R.string.error_field_required));
            editCityView.requestFocus();
            return  false;
        }

        if (TextUtils.isEmpty(m_strStreet)) {
            editStreetView.setError(getString(R.string.error_field_required));
            editStreetView.requestFocus();
            return  false;
        }


        return true;
    }

    private void procPostPersonalInformation (){
        File file = new File(m_strScaledPhotoPath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        Map<String, RequestBody> mapParams = new HashMap<>();
        mapParams.put("user_id", RequestBody.create(MediaType.parse("text/plain"), PreferenceManager.getUserId()));
        mapParams.put("lat", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(mLatLng.latitude)));
        mapParams.put("lng", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(mLatLng.longitude)));
        mapParams.put("country", RequestBody.create(MediaType.parse("text/plain"), m_strCountry));
        mapParams.put("city", RequestBody.create(MediaType.parse("text/plain"), m_strCity));
        mapParams.put("region_street", RequestBody.create(MediaType.parse("text/plain"), m_strStreet));

        final ProgressDialog m_dlgWait = ProgressDialog.show(PersonalInformationActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultPersonalInfo> call;
        if (PreferenceManager.getRole() == GlobalConst.ROLE_DRIVER)
            call = apiInterface.doPostPersonalInformationForDriver(mapParams, reqFile);
        else
            call = apiInterface.doPostPersonalInformationForCustomer(mapParams, reqFile);
        call.enqueue(new Callback<ResultPersonalInfo>() {
            @Override
            public void onResponse(Call<ResultPersonalInfo> call, retrofit2.Response<ResultPersonalInfo> response) {
                m_dlgWait.dismiss();
                ResultPersonalInfo resultPersonalInfo = response.body();
                Toast.makeText(PersonalInformationActivity.this, resultPersonalInfo.getMessage(), Toast.LENGTH_SHORT).show();
                if (resultPersonalInfo.getStatus() == 1){
                    PreferenceManager.setAddrLat(String.valueOf(mLatLng.latitude));
                    PreferenceManager.setAddrLng(String.valueOf(mLatLng.longitude));
                    PreferenceManager.setCountry(m_strCountry);
                    PreferenceManager.setCity(m_strCity);
                    PreferenceManager.setRegion(m_strStreet);
                    PreferenceManager.setUserPhoto(resultPersonalInfo.getImage());

                    Intent intent;
                    if (PreferenceManager.getRole() == GlobalConst.ROLE_DRIVER) {
                        PreferenceManager.setDriverId(resultPersonalInfo.getExt_id());
                        intent = new Intent(PersonalInformationActivity.this, DriverVehicleDetailsActivity.class);
                    } else {
                        PreferenceManager.setCustomerId(resultPersonalInfo.getExt_id());
                        intent = new Intent(PersonalInformationActivity.this, CustomerMainActivity.class);
                    }

                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResultPersonalInfo> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(PersonalInformationActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(PersonalInformationActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doDeleteAccount(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                if (baseResult.getStatus() == 1){
                    PreferenceManager.resetAll();
                    Intent intent = new Intent(PersonalInformationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(PersonalInformationActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
