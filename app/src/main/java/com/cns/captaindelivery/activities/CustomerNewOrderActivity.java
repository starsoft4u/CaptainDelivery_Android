package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.models.InfoOrderDetail;
import com.cns.captaindelivery.models.ResultOrderDetail;
import com.google.gson.JsonObject;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerNewOrderActivity extends _BaseActivity implements View.OnClickListener {
    final int REQ_DELIVERY_LOCATION = 22;

    ApiInterface apiInterface;

    int m_nOrderId;

    InfoOrderDetail m_infoOrderDetail = new InfoOrderDetail();
    Date m_deliveryTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutomer_new_order);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        m_nOrderId = getIntent().getIntExtra(GlobalConst.KEY_ORDER_ID, -1);
        if (m_nOrderId > 0){
            procGetOrderDetail();
        } else {
            m_infoOrderDetail.setSrc_name(getIntent().getStringExtra(GlobalConst.KEY_PLACE_NAME));
            m_infoOrderDetail.setSrc_addr(getIntent().getStringExtra(GlobalConst.KEY_PLACE_ADDR));
            m_infoOrderDetail.setSrc_placeid(getIntent().getStringExtra(GlobalConst.KEY_PLACE_ID));
            m_infoOrderDetail.setSrc_placeicon(getIntent().getStringExtra(GlobalConst.KEY_PLACE_ICON));
            m_infoOrderDetail.setSrc_lat(getIntent().getStringExtra(GlobalConst.KEY_LAT));
            m_infoOrderDetail.setSrc_lng(getIntent().getStringExtra(GlobalConst.KEY_LNG));
            showInfo();
        }
        initView();
    }

    private void initView(){
        findViewById(R.id.imgBtnBack).setOnClickListener(this);
        findViewById(R.id.layoutBtnDeliveryLocation).setOnClickListener(this);
        findViewById(R.id.layoutBtnDeliveryTime).setOnClickListener(this);
        findViewById(R.id.btnDone).setOnClickListener(this);
        if (m_nOrderId>0){
            ((Button)findViewById(R.id.btnDone)).setText(R.string.btn_update);
            findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
            findViewById(R.id.btnDelete).setOnClickListener(this);
        }
    }

    private void showInfo(){
        ((TextView)findViewById(R.id.txtStoreName)).setText(m_infoOrderDetail.getSrc_name());
        ((TextView)findViewById(R.id.txtStoreAddr)).setText(m_infoOrderDetail.getSrc_addr());
        Picasso.get().load(m_infoOrderDetail.getSrc_placeicon()).error(R.drawable.no_image).into((ImageView) findViewById(R.id.imgStoreIcon));

        if (m_infoOrderDetail.getDelivery_time()>0) {
            ((TextView)findViewById(R.id.txtDeliveryLocation)).setText(m_infoOrderDetail.getDst_addr());
            ((EditText) findViewById(R.id.editDesc)).setText(m_infoOrderDetail.getOrd_desc());
            m_deliveryTime = new Date();
            m_deliveryTime.setTime(m_infoOrderDetail.getDelivery_time()*1000L);
            showDeliveryDate();
        }
    }

    private void showDeliveryDate(){
        Toast.makeText(this, "Timestampe = "+m_deliveryTime.getTime(), Toast.LENGTH_SHORT).show();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String strFormattedDate = df.format(m_deliveryTime);
        ((TextView)findViewById(R.id.txtDeliveryTime)).setText(strFormattedDate);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBtnBack:
                finish();
                break;
            case R.id.layoutBtnDeliveryLocation:
                Intent intent = new Intent(CustomerNewOrderActivity.this, CustomerDeliveryLocationActivity.class);
                intent.putExtra(GlobalConst.KEY_LAT, m_infoOrderDetail.getSrc_lat());
                intent.putExtra(GlobalConst.KEY_LNG, m_infoOrderDetail.getSrc_lng());
                intent.putExtra(GlobalConst.KEY_LAT_DEST, m_infoOrderDetail.getDst_lat());
                intent.putExtra(GlobalConst.KEY_LNG_DEST, m_infoOrderDetail.getDst_lng());
                if (m_infoOrderDetail.getDst_addr()!=null && m_infoOrderDetail.getDst_addr().length()>0)
                    intent.putExtra(GlobalConst.KEY_PLACE_ADDR, m_infoOrderDetail.getDst_addr());

                startActivityForResult(intent, REQ_DELIVERY_LOCATION);

                break;
            case R.id.layoutBtnDeliveryTime:
                // Initialize
                SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                        getResources().getString(R.string.lbl_delivery_time),
                        getResources().getString(R.string.btn_ok),
                        getResources().getString(R.string.btn_cancel)
                );

                // Assign values
                dateTimeDialogFragment.startAtCalendarView();
                dateTimeDialogFragment.set24HoursMode(false);
                //dateTimeDialogFragment.setMinimumDateTime(new Date());
                dateTimeDialogFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.DECEMBER, 31).getTime());
                dateTimeDialogFragment.setDefaultDateTime(m_deliveryTime==null?new Date():m_deliveryTime);

                // Define new day and month format
                try {
                    dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
                } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
                    Log.e("kang", e.getMessage());
                }

                // Set listener
                dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Date date) {
                        m_deliveryTime = date;
                        showDeliveryDate();
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {

                    }
                });

                // Show
                dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");

                break;
            case R.id.btnDone:
                if (checkValidate()){
                    procAddNewOrder();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DELIVERY_LOCATION){
            if (resultCode == RESULT_OK){
                Bundle bundle = data.getExtras();
                m_infoOrderDetail.setDst_addr(bundle.getString(GlobalConst.KEY_PLACE_ADDR, ""));

                m_infoOrderDetail.setDst_lat(bundle.getString(GlobalConst.KEY_LAT_DEST, ""));
                m_infoOrderDetail.setDst_lng(bundle.getString(GlobalConst.KEY_LNG_DEST, ""));
                ((TextView)findViewById(R.id.txtDeliveryLocation)).setText(m_infoOrderDetail.getDst_addr());
            }
        }
    }

    private boolean checkValidate(){
        if (m_infoOrderDetail.getDst_lat()==null || m_infoOrderDetail.getDst_lng()==null || m_infoOrderDetail.getDst_lat().length()==0 && m_infoOrderDetail.getDst_lng().length()==0){
            Toast.makeText(CustomerNewOrderActivity.this, R.string.msg_delivery_location, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (m_deliveryTime == null){
            Toast.makeText(CustomerNewOrderActivity.this, R.string.msg_delivery_time, Toast.LENGTH_SHORT).show();
            return false;
        }

        String strDesc = ((EditText)findViewById(R.id.editDesc)).getText().toString().trim();
        if (strDesc.length() == 0){
            Toast.makeText(CustomerNewOrderActivity.this, R.string.msg_request_description, Toast.LENGTH_SHORT).show();
            return false;
        }
        m_infoOrderDetail.setOrd_desc(strDesc);

        return true;
    }

    private void procGetOrderDetail (){
        JsonObject jsonObject = new JsonObject();
        //	src_addr											ord_created
        jsonObject.addProperty("customer_id", PreferenceManager.getCustomerId());
        jsonObject.addProperty("ord_id", m_nOrderId);

        final ProgressDialog m_dlgWait = ProgressDialog.show(CustomerNewOrderActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultOrderDetail> call = apiInterface.doGetOrderDetail(jsonObject);
        call.enqueue(new Callback<ResultOrderDetail>() {
            @Override
            public void onResponse(Call<ResultOrderDetail> call, retrofit2.Response<ResultOrderDetail> response) {
                m_dlgWait.dismiss();
                ResultOrderDetail resultOrderDetail = response.body();
                if (resultOrderDetail.getStatus() == 1) {
                    m_infoOrderDetail = resultOrderDetail.getData();
                    showInfo();
                } else {
                    Toast.makeText(CustomerNewOrderActivity.this, resultOrderDetail.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResultOrderDetail> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(CustomerNewOrderActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procAddNewOrder (){
        JsonObject jsonObject = new JsonObject();
        //	src_addr											ord_created
        if (m_nOrderId>0)
            jsonObject.addProperty("ord_id", m_nOrderId);
        jsonObject.addProperty("customer_id", PreferenceManager.getCustomerId());
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());
        jsonObject.addProperty("src_lat", m_infoOrderDetail.getSrc_lat());
        jsonObject.addProperty("src_lng", m_infoOrderDetail.getSrc_lng());
        jsonObject.addProperty("src_name", m_infoOrderDetail.getSrc_name());
        jsonObject.addProperty("src_addr", m_infoOrderDetail.getSrc_addr());
        jsonObject.addProperty("src_placeid", m_infoOrderDetail.getSrc_placeid());
        jsonObject.addProperty("src_placeicon", m_infoOrderDetail.getSrc_placeicon());
        jsonObject.addProperty("dst_addr", m_infoOrderDetail.getDst_addr());
        jsonObject.addProperty("dst_lat", m_infoOrderDetail.getDst_lat());
        jsonObject.addProperty("dst_lng", m_infoOrderDetail.getDst_lng());

        jsonObject.addProperty("delivery_time", m_deliveryTime.getTime() / 1000L);
        jsonObject.addProperty("ord_desc", m_infoOrderDetail.getOrd_desc());

        final ProgressDialog m_dlgWait = ProgressDialog.show(CustomerNewOrderActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doAddNewOrder(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                if (baseResult.getStatus() == 1) {
                    Toast.makeText(CustomerNewOrderActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CustomerNewOrderActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(CustomerNewOrderActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
