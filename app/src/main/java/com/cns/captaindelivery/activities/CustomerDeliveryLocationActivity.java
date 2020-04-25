package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.ResultGeocode;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerDeliveryLocationActivity extends _BaseFragmentActivity implements OnMapReadyCallback {
    private static final int DEFAULT_ZOOM = 16;

    private GoogleMap mMap;

    LatLng m_latLngSrc = null;
    LatLng m_latLngDest = null;

    String m_strDstAddr = "", m_strDstAddrDetail = "";

    ApiInterface googleApiInfterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_delivery_location);

        double dLatSrc = Double.valueOf(getIntent().getStringExtra(GlobalConst.KEY_LAT));
        double dLngSrc = Double.valueOf(getIntent().getStringExtra(GlobalConst.KEY_LNG));
        if (getIntent().hasExtra(GlobalConst.KEY_PLACE_ADDR)) {
            String strFullAddr = getIntent().getStringExtra(GlobalConst.KEY_PLACE_ADDR);
            String[] arrAddrs = strFullAddr.split("\n");
            m_strDstAddr = arrAddrs[0];
            if (arrAddrs.length>1)
                m_strDstAddrDetail = arrAddrs[1];
        }

        String strLatDst = getIntent().getStringExtra(GlobalConst.KEY_LAT_DEST);
        String strLngDst = getIntent().getStringExtra(GlobalConst.KEY_LNG_DEST);
        if (strLatDst!=null && strLngDst!=null && strLatDst.length() > 0 && strLngDst.length() > 0){
            Double dLatDst = Double.valueOf(strLatDst);
            Double dLngDst = Double.valueOf(strLngDst);
            m_latLngDest = new LatLng(dLatDst, dLngDst);
        }

        m_latLngSrc = new LatLng(dLatSrc, dLngSrc);

        googleApiInfterface = ApiClient.getGooglePlaceClient().create(ApiInterface.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (m_latLngDest == null) {
            String strCurrentLat = PreferenceManager.getCurrentLat();
            String strCurrentLng = PreferenceManager.getCurrentLng();
            if (strCurrentLat.length()>0 && strCurrentLng.length() > 0){
                m_latLngDest = new LatLng(Double.valueOf(strCurrentLat), Double.valueOf(strCurrentLng));
                showPlaceOnMap(DEFAULT_ZOOM);
                procGetAddrFromLatLng(m_latLngDest);
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_latLngSrc, DEFAULT_ZOOM));
            }
        } else {
            showPlaceOnMap(DEFAULT_ZOOM);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                m_latLngDest = latLng;
                showPlaceOnMap(0);
                procGetAddrFromLatLng(m_latLngDest);
            }
        });
    }

    private void initView(){

        findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.imgBtnPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_latLngDest == null) {
                    Toast.makeText(CustomerDeliveryLocationActivity.this, R.string.msg_unknown_place, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (m_strDstAddr.length() == 0) {
                    Toast.makeText(CustomerDeliveryLocationActivity.this, R.string.msg_unknown_place, Toast.LENGTH_SHORT).show();
                    return;
                }
                m_strDstAddrDetail = ((EditText)findViewById(R.id.editAddrDetail)).getText().toString().trim();
                if (m_strDstAddrDetail.length() == 0) {
                    Toast.makeText(CustomerDeliveryLocationActivity.this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(GlobalConst.KEY_PLACE_ADDR, m_strDstAddr+"\n"+m_strDstAddrDetail);
                bundle.putString(GlobalConst.KEY_LAT_DEST, String.valueOf(m_latLngDest.latitude));
                bundle.putString(GlobalConst.KEY_LNG_DEST, String.valueOf(m_latLngDest.longitude));
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final PlacesAutocompleteTextView mAutocomplete = findViewById(R.id.places_autocomplete);
        mAutocomplete.setOnPlaceSelectedListener(new OnPlaceSelectedListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mAutocomplete.getDetailsFor(place, new DetailsCallback() {
                    @Override
                    public void onSuccess(PlaceDetails placeDetails) {
                        m_latLngDest = new LatLng(placeDetails.geometry.location.lat, placeDetails.geometry.location.lng);
                        m_strDstAddr = placeDetails.formatted_address;
                        showPlaceOnText();
                        showPlaceOnMap(0);
                        Utils.hide_keyboard(CustomerDeliveryLocationActivity.this);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
            }
        });
        showPlaceOnText();
        showPlaceOnMap(0);
    }


    private void showPlaceOnText(){
        ((TextView)findViewById(R.id.txtAddr)).setText(m_strDstAddr);
        if (m_strDstAddrDetail.length()>0)
            ((TextView)findViewById(R.id.editAddrDetail)).setText(m_strDstAddrDetail);

        if (m_latLngDest != null) {
            double dDistance = Utils.getDistanceInMeters(m_latLngSrc, m_latLngDest);
            int nKmTemp = (int) (dDistance / 100);
            double dKm = (double) nKmTemp / 10;
            ((TextView) findViewById(R.id.txtDistance)).setText("Accurate to " + String.valueOf(dKm) + " km ");
        }
    }

    private void showPlaceOnMap(int nZoomLevel){
        if (mMap != null && m_latLngDest != null){
            mMap.addMarker(new MarkerOptions().position(m_latLngDest).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_2)));
            if (nZoomLevel == 0)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(m_latLngDest));
            else
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_latLngDest, nZoomLevel));
        }
    }

    private void procGetAddrFromLatLng (LatLng latLng){
        final ProgressDialog m_dlgWait = ProgressDialog.show(CustomerDeliveryLocationActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        StringBuilder urlString = new StringBuilder("api/geocode/json?");

        urlString.append("&latlng=");
        urlString.append(latLng.latitude);
        urlString.append(",");
        urlString.append(latLng.longitude);
        urlString.append("&sensor=false&key=" + getString(R.string.google_places_key));

        Call<ResultGeocode> call = googleApiInfterface.doReverseGeocode(urlString.toString());
        call.enqueue(new Callback<ResultGeocode>() {
            @Override
            public void onResponse(Call<ResultGeocode> call, retrofit2.Response<ResultGeocode> response) {
                m_dlgWait.dismiss();

                ResultGeocode resultGeocode = response.body();

                m_strDstAddr = "";
                if (resultGeocode.getStatus().equals(GlobalConst.OK)) {
                    if (resultGeocode.getResults().size()>0) {
                        m_strDstAddr = resultGeocode.getResults().get(0).getFormatted_address();
                    }
                }
                showPlaceOnText();
                if (m_strDstAddr.length() == 0)
                    Toast.makeText(CustomerDeliveryLocationActivity.this, R.string.msg_unknown_place, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<ResultGeocode> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(CustomerDeliveryLocationActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
