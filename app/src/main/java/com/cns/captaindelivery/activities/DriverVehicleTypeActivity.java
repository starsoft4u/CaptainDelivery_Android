package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class DriverVehicleTypeActivity extends _BaseActivity  {
    ApiInterface apiInterface;

    final int CAPTAIN_MICRO = 1;
    final int CAPTAIN_MINI = 2;
    final int CAPTAIN_SEDAN = 3;
    int m_nCaptainType = 0;

    final int[] ARR_CAPTAIN_TYPES = {0, R.string.lbl_captain_micro, R.string.lbl_captain_mini, R.string.lbl_captain_sedan};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_vehicle_type);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    private void initView(){
        findViewById(R.id.layoutBtnCaptainMicro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_nCaptainType == CAPTAIN_MICRO)
                    return;
                m_nCaptainType = CAPTAIN_MICRO;
                showSelection();
            }
        });
        findViewById(R.id.layoutBtnCaptainMini).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_nCaptainType == CAPTAIN_MINI)
                    return;
                m_nCaptainType = CAPTAIN_MINI;
                showSelection();
            }
        });
        findViewById(R.id.layoutBtnCaptainSedan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_nCaptainType == CAPTAIN_SEDAN)
                    return;
                m_nCaptainType = CAPTAIN_SEDAN;
                showSelection();
            }
        });

        findViewById(R.id.imgBtnBack).setVisibility(View.VISIBLE);
        findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverVehicleTypeActivity.this);
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
            }
        });

        findViewById(R.id.btnContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_nCaptainType == 0){
                    Toast.makeText(DriverVehicleTypeActivity.this, R.string.msg_err_select_one, Toast.LENGTH_SHORT).show();
                    return;
                }
                procPostVehicleType();
            }
        });
    }

    private void showSelection(){
        findViewById(R.id.layoutCaptainMicroDetail).setVisibility(View.GONE);
        findViewById(R.id.imgLineCaptainMicro).setVisibility(View.GONE);
        findViewById(R.id.imgCheckCaptainMicro).setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.imgCaptainMicro)).setImageResource(R.drawable.ic_captain_micro_off);
        findViewById(R.id.layoutCaptainMiniDetail).setVisibility(View.GONE);
        findViewById(R.id.imgLineCaptainMini).setVisibility(View.GONE);
        findViewById(R.id.imgCheckCaptainMini).setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.imgCaptainMini)).setImageResource(R.drawable.ic_captain_mini_off);
        findViewById(R.id.layoutCaptainSedanDetail).setVisibility(View.GONE);
        findViewById(R.id.imgLineCaptainSedan).setVisibility(View.GONE);
        findViewById(R.id.imgCheckCaptainSedan).setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.imgCaptainSedan)).setImageResource(R.drawable.ic_captain_sedan_off);

        switch (m_nCaptainType){
            case CAPTAIN_MICRO:
                findViewById(R.id.layoutCaptainMicroDetail).setVisibility(View.VISIBLE);
                findViewById(R.id.imgLineCaptainMicro).setVisibility(View.VISIBLE);
                findViewById(R.id.imgCheckCaptainMicro).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.imgCaptainMicro)).setImageResource(R.drawable.ic_captain_micro_on);
                break;
            case CAPTAIN_MINI:
                findViewById(R.id.layoutCaptainMiniDetail).setVisibility(View.VISIBLE);
                findViewById(R.id.imgLineCaptainMini).setVisibility(View.VISIBLE);
                findViewById(R.id.imgCheckCaptainMini).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.imgCaptainMini)).setImageResource(R.drawable.ic_captain_mini_on);
                break;
            case CAPTAIN_SEDAN:
                findViewById(R.id.layoutCaptainSedanDetail).setVisibility(View.VISIBLE);
                findViewById(R.id.imgLineCaptainSedan).setVisibility(View.VISIBLE);
                findViewById(R.id.imgCheckCaptainSedan).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.imgCaptainSedan)).setImageResource(R.drawable.ic_captain_sedan_on);
                break;
        }
    }

    private void procPostVehicleType (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());
        jsonObject.addProperty("vehicle_type", getResources().getString(ARR_CAPTAIN_TYPES[m_nCaptainType]));

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverVehicleTypeActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doPostVehicleType(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                if (baseResult.getStatus() == 1) {
                    Toast.makeText(DriverVehicleTypeActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DriverVehicleTypeActivity.this, DriverVehicleOwnershipActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DriverVehicleTypeActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverVehicleTypeActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverVehicleTypeActivity.this, null, null);
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
                    Intent intent = new Intent(DriverVehicleTypeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverVehicleTypeActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
