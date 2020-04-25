package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class DriverVehicleOwnershipActivity extends _BaseActivity  {
    ApiInterface apiInterface;

    String m_strFullAddress, m_strCountry, m_strCity, m_strCompanyName;
    String m_strRegistrationNo, m_strVinNumber, m_strIgnitionKeyNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_vehicle_ownership);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

    }

    private void initView(){
        ((EditText)findViewById(R.id.editFullName)).setText(PreferenceManager.getUserName());
        ((EditText)findViewById(R.id.editEmailAddress)).setText(PreferenceManager.getUserEmail());
        ((EditText)findViewById(R.id.editPhoneNumber)).setText(PreferenceManager.getPhone());

        findViewById(R.id.btnContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()){
                    procPostVehicleOwnership();
                }
            }
        });

        findViewById(R.id.imgBtnBack).setVisibility(View.VISIBLE);
        findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverVehicleOwnershipActivity.this);
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
    }

    private boolean checkValidation() {
        EditText editView;

        editView= findViewById(R.id.editFullAddress);  editView.setError(null);
        m_strFullAddress = editView.getText().toString();
        if (TextUtils.isEmpty(m_strFullAddress)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editCountry);  editView.setError(null);
        m_strCountry = editView.getText().toString();
        if (TextUtils.isEmpty(m_strCountry)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editCity);  editView.setError(null);
        m_strCity = editView.getText().toString();
        if (TextUtils.isEmpty(m_strCity)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editCompanyName);  editView.setError(null);
        m_strCompanyName = editView.getText().toString();
        if (TextUtils.isEmpty(m_strCompanyName)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editRegistrationNo);  editView.setError(null);
        m_strRegistrationNo = editView.getText().toString();
        if (TextUtils.isEmpty(m_strRegistrationNo)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editVinNumber);  editView.setError(null);
        m_strVinNumber = editView.getText().toString();
        if (TextUtils.isEmpty(m_strVinNumber)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editIgnitionKeyNumber);  editView.setError(null);
        m_strIgnitionKeyNumber = editView.getText().toString();
        if (TextUtils.isEmpty(m_strIgnitionKeyNumber)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }
        return true;
    }

    private void procPostVehicleOwnership (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());
        jsonObject.addProperty("full_address", m_strFullAddress);
        jsonObject.addProperty("company_name", m_strCompanyName);
        jsonObject.addProperty("owner_country", m_strCountry);
        jsonObject.addProperty("owner_city", m_strCity);
        jsonObject.addProperty("registeration_vehicle", m_strRegistrationNo);
        jsonObject.addProperty("vin_number", m_strVinNumber);
        jsonObject.addProperty("ignition_key_number", m_strIgnitionKeyNumber);

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverVehicleOwnershipActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doPostVehicleOwnership(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult resultVehicleType = response.body();
                if (resultVehicleType.getStatus() == 1) {
                    Toast.makeText(DriverVehicleOwnershipActivity.this, resultVehicleType.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DriverVehicleOwnershipActivity.this, DriverDrivingLicenseActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DriverVehicleOwnershipActivity.this, resultVehicleType.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverVehicleOwnershipActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverVehicleOwnershipActivity.this, null, null);
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
                    Intent intent = new Intent(DriverVehicleOwnershipActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverVehicleOwnershipActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
