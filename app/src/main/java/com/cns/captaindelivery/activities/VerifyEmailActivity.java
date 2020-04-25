package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.dialogs.ChangeEmailFragment;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.models.ResultCheckEmail;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class VerifyEmailActivity extends _BaseActivity {

    ApiInterface apiInterface;

    //String m_strDigit1, m_strDigit2, m_strDigit3, m_strDigit4;
    String m_strCode;

    String m_strEmailForCheck, m_strCodeForCheck;

    boolean m_bFromProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        m_bFromProfile = getIntent().getBooleanExtra(GlobalConst.KEY_FROM_PROFILE, false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (m_bFromProfile) {
            m_strEmailForCheck = getIntent().getStringExtra(GlobalConst.KEY_EMAIL);
            procCheckEmailBeforeUpdate();
        } else {
            procSendEmailVerifyRequest();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(VerifyEmailActivity.this);
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
            ((TextView)findViewById(R.id.txtChangeEmail)).setText(R.string.btn_cancel);
        else
            ((TextView)findViewById(R.id.txtChangeEmail)).setText(R.string.btn_change_email);

        findViewById(R.id.ripTxtChangeEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_bFromProfile){
                    finish();
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    ChangeEmailFragment dlgAlert = ChangeEmailFragment.newInstance(PreferenceManager.getUserEmail());
                    dlgAlert.show(fm, "fragment_change_email");
                }
            }
        });

        findViewById(R.id.ripTxtResendCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!m_bFromProfile)
                    procSendEmailVerifyRequest();
                else
                    procCheckEmailBeforeUpdate();
            }
        });

        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    if (m_bFromProfile){
                        if (m_strCode.equals(m_strCodeForCheck)){
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        procCheckVerifyCode();
                    }
                }
            }
        });

        final PinEntryEditText pinEntry = (PinEntryEditText) findViewById(R.id.txt_pin_entry);
        if (pinEntry != null) {
            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    m_strCode = str.toString();
                }
            });
        }
    }

    private boolean checkValidation(){
        /*
        m_strDigit1 = ((EditText)findViewById(R.id.editDigit1)).getText().toString();
        m_strDigit2 = ((EditText)findViewById(R.id.editDigit2)).getText().toString();
        m_strDigit3 = ((EditText)findViewById(R.id.editDigit3)).getText().toString();
        m_strDigit4 = ((EditText)findViewById(R.id.editDigit4)).getText().toString();

        m_strCode = m_strDigit1+m_strDigit2+m_strDigit3+m_strDigit4;
        */

        if (m_strCode.length() != 4){
            Toast.makeText(VerifyEmailActivity.this, R.string.msg_err_input, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void procSendEmailVerifyRequest (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", PreferenceManager.getUserEmail());

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyEmailActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doSendEmailVerifyRequest(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                Toast.makeText(VerifyEmailActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyEmailActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procCheckVerifyCode (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", PreferenceManager.getUserEmail());
        jsonObject.addProperty("email_verify_code", m_strCode);

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyEmailActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doCheckEmailVerifyCode(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                Toast.makeText(VerifyEmailActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                if (baseResult.getStatus() == 1){
                    Intent intent = new Intent(VerifyEmailActivity.this, VerifyPhoneActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyEmailActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procCheckEmailBeforeUpdate(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", m_strEmailForCheck);

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyEmailActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultCheckEmail> call = apiInterface.doCheckEmailBeforeUpdateProfile(jsonObject);
        call.enqueue(new Callback<ResultCheckEmail>() {
            @Override
            public void onResponse(Call<ResultCheckEmail> call, retrofit2.Response<ResultCheckEmail> response) {
                m_dlgWait.dismiss();

                ResultCheckEmail resultCheckEmail = response.body();
                Toast.makeText(VerifyEmailActivity.this, resultCheckEmail.getMessage(), Toast.LENGTH_SHORT).show();
                if (resultCheckEmail.getStatus() == 1) {
                    m_strCodeForCheck = resultCheckEmail.getVerify_code();
                }
            }
            @Override
            public void onFailure(Call<ResultCheckEmail> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(VerifyEmailActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(VerifyEmailActivity.this, null, null);
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
                    Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(VerifyEmailActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
