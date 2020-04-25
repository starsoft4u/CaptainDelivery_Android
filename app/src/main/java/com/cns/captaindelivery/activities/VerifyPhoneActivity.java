package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.dialogs.ChangePhoneNumberFragment;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.models.ResultRequestPhoneVerify;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;

public class VerifyPhoneActivity extends _BaseActivity {

    ApiInterface apiInterface;
    public ApiInterface apiInterfaceTwilio;

    String m_strCode;

    boolean m_bFromProfile = false;

    String m_strCountryCodeForCheck, m_strPhoneForCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        m_bFromProfile = getIntent().getBooleanExtra(GlobalConst.KEY_FROM_PROFILE, false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        apiInterfaceTwilio = ApiClient.getPhoneVerifyClient().create(ApiInterface.class);

        if (checkExistingPhone()){
            //procRequestVerifyCode();
        }

        if (m_bFromProfile){
            m_strCountryCodeForCheck = getIntent().getStringExtra(GlobalConst.KEY_CODE);
            m_strPhoneForCheck = getIntent().getStringExtra(GlobalConst.KEY_PHONE);
        }
    }

    private void initView(){
        if (m_bFromProfile)
            findViewById(R.id.imgBtnBack).setVisibility(View.GONE);
        else {
            findViewById(R.id.imgBtnBack).setVisibility(View.VISIBLE);
            findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VerifyPhoneActivity.this);
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


        if (m_bFromProfile)
            ((TextView)findViewById(R.id.txtChangeNumber)).setText(R.string.btn_cancel);
        else
            ((TextView)findViewById(R.id.txtChangeNumber)).setText(R.string.btn_change_number);

        findViewById(R.id.ripTxtChangeNumber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_bFromProfile){
                    finish();
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    ChangePhoneNumberFragment dlgAlert = ChangePhoneNumberFragment.newInstance(PreferenceManager.getPhone());
                    dlgAlert.show(fm, "fragment_change_phone");
                }
            }
        });


        ((TextView)findViewById(R.id.txtResendCode)).setText(R.string.btn_send_code);
        findViewById(R.id.ripTxtResendCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.txtResendCode)).setText(R.string.btn_resend_code);
                if (m_bFromProfile)
                    procCheckPhoneBeforeUpdate();
                else
                    procRequestVerifyCode();
            }
        });

        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()){
                    procCheckVerifyCode();
                }
            }
        });
    }

    private boolean checkExistingPhone(){
        String strPhone = PreferenceManager.getPhone();
        if (strPhone == null || strPhone.length() == 0){
            FragmentManager fm = getSupportFragmentManager();
            ChangePhoneNumberFragment dlgAlert = ChangePhoneNumberFragment.newInstance("");
            dlgAlert.show(fm, "fragment_change_phone");
            return false;
        }
        return true;
    }

    private boolean checkValidation(){
        EditText editCodeView = findViewById(R.id.editCode);
        editCodeView.setError(null);
        m_strCode = editCodeView.getText().toString();

        if (TextUtils.isEmpty(m_strCode)) {
            editCodeView.setError(getString(R.string.error_field_required));
            editCodeView.requestFocus();
            return  false;
        }

        return true;
    }

    private void procRequestVerifyCode (){
        JsonObject jsonObject = new JsonObject();
        String strCountryCode = m_bFromProfile?m_strCountryCodeForCheck:PreferenceManager.getCountryCode();
        if (strCountryCode.startsWith("+")){
            strCountryCode = strCountryCode.substring(1);
        }
        jsonObject.addProperty("via", "sms");
        jsonObject.addProperty("country_code", strCountryCode);
        jsonObject.addProperty("phone_number", m_bFromProfile?m_strPhoneForCheck:PreferenceManager.getPhone());

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyPhoneActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultRequestPhoneVerify> call = apiInterfaceTwilio.doRequestPhoneVerifyCode(jsonObject);
        call.enqueue(new Callback<ResultRequestPhoneVerify>() {
            @Override
            public void onResponse(Call<ResultRequestPhoneVerify> call, retrofit2.Response<ResultRequestPhoneVerify> response) {
                m_dlgWait.dismiss();

                if (response.body() != null) {
                    ResultRequestPhoneVerify resultRequestPhoneVerify = response.body();
                    Toast.makeText(VerifyPhoneActivity.this, resultRequestPhoneVerify.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Converter<ResponseBody, ResultRequestPhoneVerify> errorConverter =
                            ApiClient.getPhoneVerifyClient().responseBodyConverter(ResultRequestPhoneVerify.class, new Annotation[0]);
                    try {
                        ResultRequestPhoneVerify error = errorConverter.convert(response.errorBody());
                        Toast.makeText(VerifyPhoneActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ResultRequestPhoneVerify> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procCheckVerifyCode (){
        String strCountryCode = m_bFromProfile?m_strCountryCodeForCheck:PreferenceManager.getCountryCode();
        if (strCountryCode.startsWith("+")){
            strCountryCode = strCountryCode.substring(1);
        }

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyPhoneActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultRequestPhoneVerify> call = apiInterfaceTwilio.doCheckPhoneVerifyCode(strCountryCode, m_bFromProfile?m_strPhoneForCheck:PreferenceManager.getPhone(), m_strCode);
        call.enqueue(new Callback<ResultRequestPhoneVerify>() {
            @Override
            public void onResponse(Call<ResultRequestPhoneVerify> call, retrofit2.Response<ResultRequestPhoneVerify> response) {
                m_dlgWait.dismiss();
                if (response.body() != null) {
                    ResultRequestPhoneVerify resultRequestPhoneVerify = response.body();
                    Toast.makeText(VerifyPhoneActivity.this, resultRequestPhoneVerify.getMessage(), Toast.LENGTH_SHORT).show();
                    if (resultRequestPhoneVerify.isSuccess()){
                        if (m_bFromProfile) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            procPostVerifyStatus();
                        }
                    }
                } else {
                    Converter<ResponseBody, ResultRequestPhoneVerify> errorConverter =
                            ApiClient.getPhoneVerifyClient().responseBodyConverter(ResultRequestPhoneVerify.class, new Annotation[0]);
                    try {
                        ResultRequestPhoneVerify error = errorConverter.convert(response.errorBody());
                        Toast.makeText(VerifyPhoneActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ResultRequestPhoneVerify> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procPostVerifyStatus (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("phone", PreferenceManager.getPhone());
        jsonObject.addProperty("phone_verify_code", m_strCode);

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyPhoneActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doPostPhoneVerifyStatus(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                Toast.makeText(VerifyPhoneActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                if (baseResult.getStatus() == 1){
                    Intent intent = new Intent(VerifyPhoneActivity.this, PersonalInformationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procCheckPhoneBeforeUpdate(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("country_code", m_strCountryCodeForCheck);
        jsonObject.addProperty("phone", m_strPhoneForCheck);

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyPhoneActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doCheckPhoneBeforeUpdateProfile(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();

                BaseResult baseResult = response.body();

                if (baseResult.getStatus() == 1) {
                    procRequestVerifyCode();
                } else {
                    Toast.makeText(VerifyPhoneActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyPhoneActivity.this, null, null);
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
                    Intent intent = new Intent(VerifyPhoneActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyPhoneActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
