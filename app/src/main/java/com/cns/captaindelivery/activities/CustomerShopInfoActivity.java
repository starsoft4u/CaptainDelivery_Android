package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.InfoGooglePlaceDetail;
import com.cns.captaindelivery.models.ResultGooglePlaceDetail;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerShopInfoActivity extends _BaseFragmentActivity implements OnMapReadyCallback {
    final int REQ_NEW_ORDER = 21;
    private static final int DEFAULT_ZOOM = 16;

    String m_strPlaceId;
    String m_strPlaceName;
    InfoGooglePlaceDetail m_placeDetail;

    private GoogleMap mMap;

    ApiInterface apiInterface;

    LatLng m_latLngMe = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_shop_info);

        m_strPlaceId = getIntent().getStringExtra(GlobalConst.KEY_PLACE_ID);
        m_strPlaceName = getIntent().getStringExtra(GlobalConst.KEY_PLACE_NAME);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initView();
        apiInterface = ApiClient.getGooglePlaceClient().create(ApiInterface.class);

        procGetPlaceDetails();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String strLat = PreferenceManager.getCurrentLat();
        String strLng = PreferenceManager.getCurrentLng();
        if (strLat.length()>0 && strLng.length()>0){
            m_latLngMe = new LatLng(Double.valueOf(strLat), Double.valueOf(strLng));
            mMap.addMarker(new MarkerOptions().position(m_latLngMe).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_you)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_latLngMe, DEFAULT_ZOOM));
        }
    }

    private void initView(){
        ((TextView)findViewById(R.id.txtStoreName)).setText(m_strPlaceName);

        findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.imgBtnNewOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerShopInfoActivity.this, CustomerNewOrderActivity.class);
                intent.putExtra(GlobalConst.KEY_PLACE_ID, m_strPlaceId);
                intent.putExtra(GlobalConst.KEY_PLACE_NAME, m_placeDetail.getName());
                intent.putExtra(GlobalConst.KEY_PLACE_ADDR, m_placeDetail.getFormatted_address());
                intent.putExtra(GlobalConst.KEY_PLACE_ICON, m_placeDetail.getIcon());
                intent.putExtra(GlobalConst.KEY_LAT, String.valueOf(m_placeDetail.getGeometry().getLocation().getLat()));
                intent.putExtra(GlobalConst.KEY_LNG, String.valueOf(m_placeDetail.getGeometry().getLocation().getLng()));
                startActivityForResult(intent, REQ_NEW_ORDER);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_NEW_ORDER){
            if (resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void showShopInfo(){
        if (m_placeDetail == null)  return;
        ((TextView)findViewById(R.id.txtStoreInfo)).setText(m_placeDetail.getFormatted_address());
        LatLng latLng = new LatLng(m_placeDetail.getGeometry().getLocation().getLat(), m_placeDetail.getGeometry().getLocation().getLng());
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_shop)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
        if (m_latLngMe != null){
            Double dDistance = Utils.getDistanceInMeters(latLng, m_latLngMe);
            int nKmTemp = (int)(dDistance / 100);
            Double dKm = (double)nKmTemp / 10;
            ((TextView)findViewById(R.id.txtDistance)).setText(String.valueOf(dKm)+"km Away");
        }

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String strWeekDay =new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        if (m_placeDetail.getOpening_hours() != null){
            for (String strOpen: m_placeDetail.getOpening_hours().getWeekday_text()) {
                if (strOpen.startsWith(strWeekDay)) {
                    ((TextView)findViewById(R.id.txtOpenTime)).setText(strOpen.substring(strWeekDay.length()+1));
                    break;
                }
            }
        } else {
            ((TextView)findViewById(R.id.txtOpenTime)).setText(R.string.lbl_unknown);
        }

    }

    private void procGetPlaceDetails (){
        StringBuilder urlString = new StringBuilder("api/place/details/json?");
        urlString.append("&placeid=");
        urlString.append(m_strPlaceId);
        urlString.append("&key=" + getString(R.string.google_places_key));

        final ProgressDialog m_dlgWait = ProgressDialog.show(CustomerShopInfoActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);


        Call<ResultGooglePlaceDetail> call = apiInterface.doGooglePlaceDetail(urlString.toString());
        call.enqueue(new Callback<ResultGooglePlaceDetail>() {
            @Override
            public void onResponse(Call<ResultGooglePlaceDetail> call, retrofit2.Response<ResultGooglePlaceDetail> response) {
                m_dlgWait.dismiss();
                ResultGooglePlaceDetail resultGooglePlaceDetail = response.body();

                if (resultGooglePlaceDetail.getStatus().equals(GlobalConst.OK)){
                    m_placeDetail = resultGooglePlaceDetail.getResult();
                    showShopInfo();
                } else {
                    Toast.makeText(CustomerShopInfoActivity.this, resultGooglePlaceDetail.getError_message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResultGooglePlaceDetail> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(CustomerShopInfoActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
