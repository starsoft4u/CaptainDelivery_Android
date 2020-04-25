package com.cns.captaindelivery.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.PreferenceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DriverHomeFragment extends Fragment  {
    private static final int DEFAULT_ZOOM = 12;

    private static int m_nReloadHandler = 1;
    private static long	m_lReloadDelayTime = 2000;				//ms

    ApiInterface apiInterface;

    private GoogleMap mMap;
    Marker mMarker;

    View mRootView;
    LatLng m_latlngMe = null;

    public static DriverHomeFragment newInstance() {
        DriverHomeFragment fragment = new DriverHomeFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =inflater.inflate(R.layout.fragment_driver_home, container, false);
        initXml();

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        initView();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (!showCurrentLocation())
                    mHandler.sendEmptyMessageDelayed(m_nReloadHandler, m_lReloadDelayTime);
            }
        });

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(m_nReloadHandler);
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(getString(R.string.title_home));
    }

    public void initView(){
    }

    private boolean showCurrentLocation(){
        String strLat = PreferenceManager.getCurrentLat();
        String strLng = PreferenceManager.getCurrentLng();
        if (strLat.length()>0 && strLng.length()>0){
            m_latlngMe = new LatLng(Double.valueOf(strLat), Double.valueOf(strLng));
            mMarker = mMap.addMarker(new MarkerOptions().position(m_latlngMe).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_latlngMe, DEFAULT_ZOOM));
            return true;
        } else {
            return false;
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (!showCurrentLocation())
                mHandler.sendEmptyMessageDelayed(m_nReloadHandler, m_lReloadDelayTime);
        }
    };


}
