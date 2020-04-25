package com.cns.captaindelivery.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;

import com.cns.captaindelivery.activities.DriverMainActivity;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.ResultPersonalInfo;
import com.cns.captaindelivery.utils.BitmapUtils;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.utils.Utils;
import com.cns.captaindelivery.widgets.CircleImageView;
import com.cns.captaindelivery.widgets.WorkaroundMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

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

public class DriverProfileFragment extends Fragment  {
    private static final int DEFAULT_ZOOM = 16;

    private static final int    SELECT_PICTURE = 0;
    private static final int    TAKE_PICTURE = 1;

    ApiInterface apiInterface;

    private GoogleMap mMap;
    Marker mMarker;

    View mRootView;
    CircleImageView mImgPhoto;
    EditText mEditUsername;
    EditText mEditCountry, mEditCity, mEditRegion;

    String m_strPhotoUrl;
    String m_strUserName;
    String m_strCountry, m_strCity, m_strRegion;
    LatLng m_latlngAddr = null;

    String m_strPhotoPath = "";
    String m_strScaledPhotoPath = "";
    File mFile = null;

    boolean m_bEditMode = false;

    ScrollView mScrollView;

    public static DriverProfileFragment newInstance() {
        DriverProfileFragment fragment = new DriverProfileFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =inflater.inflate(R.layout.fragment_customer_profile, container, false);
        initXml();

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        loadUserInfo();
        initView();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);
                if (m_latlngAddr != null) {
                    mMarker = mMap.addMarker(new MarkerOptions().position(m_latlngAddr).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_latlngAddr, DEFAULT_ZOOM));
                }
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (!m_bEditMode)   return;
                        if (mMarker != null)
                            mMarker.remove();
                        m_latlngAddr = latLng;
                        addMarkerAndUpdateAddr();
                    }
                });

                mScrollView = mRootView.findViewById(R.id.scrollView); //parent scrollview in xml, give your scrollview id value
                ((WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                        .setListener(new WorkaroundMapFragment.OnTouchListener() {
                            @Override
                            public void onTouch()
                            {
                                mScrollView.requestDisallowInterceptTouchEvent(true);
                            }
                        });
            }
        });

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.imgBtnEdit).setVisibility(View.GONE);
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(getString(R.string.title_profile));

        getActivity().findViewById(R.id.imgBtnEdit).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.imgBtnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(getString(R.string.title_edit_profile));
                m_bEditMode = true;
                refreshViewByMode();
            }
        });
    }

    private void loadUserInfo(){
        m_strPhotoUrl = PreferenceManager.getUserPhoto();
        m_strUserName = PreferenceManager.getUserName();

        m_strCountry = PreferenceManager.getCountry();
        m_strCity = PreferenceManager.getCity();
        m_strRegion = PreferenceManager.getRegion();
        String strLat = PreferenceManager.getAddrLat();
        String strLng = PreferenceManager.getAddrLng();
        if (strLat.length()>0 && strLng.length()>0){
            m_latlngAddr = new LatLng(Double.valueOf(strLat), Double.valueOf(strLng));
        }
    }
    public void initView(){
        mImgPhoto = mRootView.findViewById(R.id.imgPhoto);
        mEditUsername = mRootView.findViewById(R.id.editUserName);
        mEditCountry = mRootView.findViewById(R.id.editCountry);
        mEditCity = mRootView.findViewById(R.id.editCity);
        mEditRegion = mRootView.findViewById(R.id.editStreet);

        if (m_strPhotoUrl.length()>0)
            Picasso.get().load(m_strPhotoUrl).error(R.drawable.no_image).into(mImgPhoto);
        mEditUsername.setText(m_strUserName);
        if (PreferenceManager.getRole() == GlobalConst.ROLE_CUSTOMER)
            mEditUsername.setHint(R.string.lbl_customer_name);
        else
            mEditUsername.setHint(R.string.lbl_driver_name);

        ((EditText)mRootView.findViewById(R.id.editEmail)).setText(PreferenceManager.getUserEmail());
        ((EditText)mRootView.findViewById(R.id.editCode)).setText(PreferenceManager.getCountryCode());
        ((EditText)mRootView.findViewById(R.id.editPhoneNumber)).setText(PreferenceManager.getPhone());

        showAddrInfo();

        mImgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!m_bEditMode)   return;
                new AlertDialog.Builder(getActivity())
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
        });


        mRootView.findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()){
                    procUpdateDriverProfile();
                }
            }
        });

        m_bEditMode = false;
        refreshViewByMode();
    }

    private void addMarkerAndUpdateAddr(){
        mMarker = mMap.addMarker(new MarkerOptions().position(m_latlngAddr).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)));
        m_strCountry = ""; m_strCity = ""; m_strRegion = "";
        showAddrInfo();
        getAddressFromLocation(m_latlngAddr.latitude, m_latlngAddr.longitude);
    }

    private void showAddrInfo(){
        mEditCountry.setText(m_strCountry);
        mEditCity.setText(m_strCity);
        mEditRegion.setText(m_strRegion);
    }

    public void getAddressFromLocation(final double latitude, final double longitude) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        m_strCountry = address.getCountryName();
                        m_strCity = address.getLocality();
                        m_strRegion = address.getThoroughfare();
                        try {
                            getActivity().runOnUiThread(new Runnable() {
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

    public void pickPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select"), SELECT_PICTURE);
    }

    public void capturePhoto(){
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "Photo_" + ts+"_";
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (mFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),"com.cns.captaindelivery.fileprovider", mFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        }catch (Exception e){
            e.printStackTrace();;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            Bitmap bitmap = null;
            switch (requestCode){
                case SELECT_PICTURE:
                    String selectAbpath = Utils.getPathFromUri(getActivity(), data.getData());
                    //Picasso.with(this).load(new File(selectAbpath)).noFade().into(mImgPhoto);
                    m_strPhotoPath = selectAbpath;
                    bitmap = BitmapUtils.getSafeDecodeBitmap(m_strPhotoPath, 400);
                    if (bitmap != null) {
                        mImgPhoto.setImageBitmap(bitmap);
                        m_strScaledPhotoPath = BitmapUtils.saveSacledImage(getActivity(), bitmap);
                    }

                    break;
                case TAKE_PICTURE:
                    if (mFile.exists()){
                        //Picasso.with(this).load(mFile).noFade().into(mImgPhoto);
                        m_strPhotoPath = mFile.getAbsolutePath();
                    }
                    bitmap = BitmapUtils.getSafeDecodeBitmap(m_strPhotoPath, 400);
                    if (bitmap != null) {
                        mImgPhoto.setImageBitmap(bitmap);
                        m_strScaledPhotoPath = BitmapUtils.saveSacledImage(getActivity(), bitmap);
                    }
                    break;
            }
        }
    }


    private boolean checkValidation() {

        mEditUsername.setError(null);
        mEditCountry.setError(null);
        mEditCity.setError(null);
        mEditRegion.setError(null);

        m_strUserName = mEditUsername.getText().toString();
        m_strCountry = mEditCountry.getText().toString();
        m_strCity = mEditCity.getText().toString();
        m_strRegion = mEditRegion.getText().toString();

        if (TextUtils.isEmpty(m_strUserName)) {
            mEditUsername.setError(getString(R.string.error_field_required));
            mEditUsername.requestFocus();
            return  false;
        }

        if (TextUtils.isEmpty(m_strCountry)) {
            mEditCountry.setError(getString(R.string.error_field_required));
            mEditCountry.requestFocus();
            return  false;
        }
        if (TextUtils.isEmpty(m_strCity)) {
            mEditCity.setError(getString(R.string.error_field_required));
            mEditCity.requestFocus();
            return  false;
        }
        if (TextUtils.isEmpty(m_strRegion)) {
            mEditRegion.setError(getString(R.string.error_field_required));
            mEditRegion.requestFocus();
            return  false;
        }

        return true;
    }

    private void refreshViewByMode(){
        getActivity().findViewById(R.id.imgBtnEdit).setVisibility(m_bEditMode?View.GONE:View.VISIBLE);
        mRootView.findViewById(R.id.btnUpdate).setVisibility(m_bEditMode?View.VISIBLE:View.GONE);

        mEditUsername.setEnabled(m_bEditMode);
        mEditCountry.setEnabled(m_bEditMode);
        mEditCity.setEnabled(m_bEditMode);
        mEditRegion.setEnabled(m_bEditMode);
        if (m_bEditMode)
            mEditUsername.requestFocus();
    }

    private void procUpdateDriverProfile(){
        RequestBody reqFile = null;

        if (m_strScaledPhotoPath.length()>0) {
            File file = new File(m_strScaledPhotoPath);
            reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        }
        Map<String, RequestBody> mapParams = new HashMap<>();
        mapParams.put("user_id", RequestBody.create(MediaType.parse("text/plain"), PreferenceManager.getUserId()));
        mapParams.put("name", RequestBody.create(MediaType.parse("text/plain"), m_strUserName));

        mapParams.put("lat", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(m_latlngAddr.latitude)));
        mapParams.put("lng", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(m_latlngAddr.longitude)));
        mapParams.put("country", RequestBody.create(MediaType.parse("text/plain"), m_strCountry));
        mapParams.put("city", RequestBody.create(MediaType.parse("text/plain"), m_strCity));
        mapParams.put("region_street", RequestBody.create(MediaType.parse("text/plain"), m_strRegion));

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultPersonalInfo> call = apiInterface.doUpdateDriverProfile(mapParams, reqFile);
        call.enqueue(new Callback<ResultPersonalInfo>() {
            @Override
            public void onResponse(Call<ResultPersonalInfo> call, retrofit2.Response<ResultPersonalInfo> response) {
                m_dlgWait.dismiss();
                ResultPersonalInfo resultPersonalInfo = response.body();
                Toast.makeText(getActivity(), resultPersonalInfo.getMessage(), Toast.LENGTH_SHORT).show();
                if (resultPersonalInfo.getStatus() == 1){
                    PreferenceManager.setUserName(m_strUserName);

                    PreferenceManager.setAddrLat(String.valueOf(m_latlngAddr.latitude));
                    PreferenceManager.setAddrLng(String.valueOf(m_latlngAddr.longitude));
                    PreferenceManager.setCountry(m_strCountry);
                    PreferenceManager.setCity(m_strCity);
                    PreferenceManager.setRegion(m_strRegion);

                    if (resultPersonalInfo.getImage().length()>0)
                        PreferenceManager.setUserPhoto(resultPersonalInfo.getImage());
                    ((DriverMainActivity)getActivity()).refreshDrawer();

                    m_bEditMode = false;
                    refreshViewByMode();
                }
            }
            @Override
            public void onFailure(Call<ResultPersonalInfo> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }




}
