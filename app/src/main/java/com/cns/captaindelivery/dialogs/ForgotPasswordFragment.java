package com.cns.captaindelivery.dialogs;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.models.ResultForgotPassword;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class ForgotPasswordFragment extends DialogFragment {

    View mRootView;

    ApiInterface apiInterface;

    String m_strEmail, m_strCode, m_strPassword1, m_strPassword2;

    int m_nUserId;
    String m_strReturnedCode;


    EditText mEditEmail, mEditCode, mEditPassword1, mEditPassword2;
    View mLayoutEmail, mLayoutCode, mLayoutPassword1, mLayoutPassword2;
    Button mButton;
    TextView mTextview;

    public final int DLG_STATUS_EMAIL = 0;
    public final int DLG_STATUS_CODE = 1;
    public final int DLG_STATUS_PASSWORD = 2;
    int m_nDlgStatus = DLG_STATUS_EMAIL;

    public static ForgotPasswordFragment newInstance(String strEmail) {
        ForgotPasswordFragment frag = new ForgotPasswordFragment();
        Bundle args = new Bundle();
        args.putString(GlobalConst.KEY_EMAIL, strEmail);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        m_strEmail = getArguments().getString(GlobalConst.KEY_EMAIL, "");

        mEditEmail = mRootView.findViewById(R.id.editEmail);
        mEditCode = mRootView.findViewById(R.id.editCode);
        mEditPassword1 = mRootView.findViewById(R.id.editPassword1);
        mEditPassword2 = mRootView.findViewById(R.id.editPassword2);
        mLayoutEmail = mRootView.findViewById(R.id.layoutEditEmail);
        mLayoutCode = mRootView.findViewById(R.id.layoutEditCode);
        mLayoutPassword1 = mRootView.findViewById(R.id.layoutPassword1);
        mLayoutPassword2 = mRootView.findViewById(R.id.layoutPassword2);
        mButton = mRootView.findViewById(R.id.btnSubmit);
        mTextview = mRootView.findViewById(R.id.txtDesc);

        m_nDlgStatus = DLG_STATUS_EMAIL;
        updateView();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (m_nDlgStatus){
                    case DLG_STATUS_EMAIL:
                        if (checkValidationEmail()){
                            procForgotPassword();
                        }
                        break;
                    case DLG_STATUS_CODE:
                        doCheckCode();
                        break;
                    case DLG_STATUS_PASSWORD:
                        if (checkValidationPassword()){
                            procResetPassword();
                        }
                        break;
                }
            }
        });
        return mRootView;
    }

    private void updateView(){

        switch (m_nDlgStatus){
            case DLG_STATUS_EMAIL:
                mLayoutEmail.setVisibility(View.VISIBLE);
                mLayoutCode.setVisibility(View.GONE);
                mLayoutPassword1.setVisibility(View.GONE);
                mLayoutPassword2.setVisibility(View.GONE);
                mTextview.setText(R.string.lbl_forgot_password_email);
                break;
            case DLG_STATUS_CODE:
                mLayoutEmail.setVisibility(View.GONE);
                mLayoutCode.setVisibility(View.VISIBLE);
                mLayoutPassword1.setVisibility(View.GONE);
                mLayoutPassword2.setVisibility(View.GONE);
                mTextview.setText(R.string.lbl_forgot_password_code);
                break;
            case DLG_STATUS_PASSWORD:
                mLayoutEmail.setVisibility(View.GONE);
                mLayoutCode.setVisibility(View.GONE);
                mLayoutPassword1.setVisibility(View.VISIBLE);
                mLayoutPassword2.setVisibility(View.VISIBLE);
                mTextview.setText(R.string.lbl_forgot_password_password);
                break;
        }
    }
    private boolean checkValidationEmail() {
        mEditEmail.setError(null);

        m_strEmail = mEditEmail.getText().toString();
        if (TextUtils.isEmpty(m_strEmail)) {
            mEditEmail.setError(getString(R.string.error_field_required));
            mEditEmail.requestFocus();
            return  false;
        }
        if (m_strEmail.contains("@") == false) {
            mEditEmail.setError(getString(R.string.error_invalid_email));
            mEditEmail.requestFocus();
            return  false;
        }
        return true;
    }

    private boolean checkValidationPassword() {
        mEditPassword1.setError(null);
        mEditPassword2.setError(null);

        m_strPassword1 = mEditPassword1.getText().toString();
        if (TextUtils.isEmpty(m_strPassword1)) {
            mEditPassword1.setError(getString(R.string.error_field_required));
            mEditPassword1.requestFocus();
            return  false;
        }
        m_strPassword2 = mEditPassword2.getText().toString();
        if (m_strPassword1.equals(m_strPassword2) ==  false) {
            Toast.makeText(getActivity(), R.string.msg_forgot_password_password, Toast.LENGTH_SHORT).show();
            mEditPassword2.requestFocus();
            return  false;
        }
        return true;
    }

    private void doCheckCode(){
        m_strCode = mEditCode.getText().toString();
        if (m_strCode.equals(m_strReturnedCode)){
            m_nDlgStatus = DLG_STATUS_PASSWORD;
            updateView();
        } else {
            Toast.makeText(getActivity(), R.string.msg_forgot_password_code, Toast.LENGTH_SHORT).show();
        }
    }
    private void procForgotPassword (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", m_strEmail);

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultForgotPassword> call = apiInterface.doForgotPassword(jsonObject);
        call.enqueue(new Callback<ResultForgotPassword>() {
            @Override
            public void onResponse(Call<ResultForgotPassword> call, retrofit2.Response<ResultForgotPassword> response) {
                m_dlgWait.dismiss();

                ResultForgotPassword resultForgotPassword = response.body();
                Toast.makeText(getActivity(), resultForgotPassword.getMessage(), Toast.LENGTH_SHORT).show();
                if (resultForgotPassword.getStatus() == 1) {
                    m_strReturnedCode = resultForgotPassword.getCode();
                    m_nUserId = resultForgotPassword.getUser_id();
                    m_nDlgStatus = DLG_STATUS_CODE;
                    updateView();
                }
            }
            @Override
            public void onFailure(Call<ResultForgotPassword> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procResetPassword (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", m_strEmail);
        jsonObject.addProperty("password", m_strPassword1);
        jsonObject.addProperty("user_id", m_nUserId);

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doResetPassword(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();

                BaseResult baseResult = response.body();
                Toast.makeText(getActivity(), baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                if (baseResult.getStatus() == 1) {
                    dismiss();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
