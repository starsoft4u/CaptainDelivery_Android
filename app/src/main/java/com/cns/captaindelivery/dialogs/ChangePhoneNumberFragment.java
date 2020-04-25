package com.cns.captaindelivery.dialogs;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class ChangePhoneNumberFragment extends DialogFragment {
    View mRootView;

    ApiInterface apiInterface;

    String m_strOldPhoneNumber, m_strNewPhoneNumber;

    String m_strCountryCode;

    public static ChangePhoneNumberFragment newInstance(String strPhone) {
        ChangePhoneNumberFragment frag = new ChangePhoneNumberFragment();
        Bundle args = new Bundle();
        args.putString(GlobalConst.KEY_PHONE_NUMBER, strPhone);
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
        mRootView = inflater.inflate(R.layout.fragment_change_phone_number, container, false);
        m_strOldPhoneNumber = getArguments().getString(GlobalConst.KEY_PHONE_NUMBER, "");


        initView();

        return mRootView;
    }

    private void initView(){
        TextView txtTitle = mRootView.findViewById(R.id.txtDlgTitle);
        if (m_strOldPhoneNumber.length()>0){
            txtTitle.setText(R.string.title_change_phone_number);
            mRootView.findViewById(R.id.layoutOldPhone).setVisibility(View.VISIBLE);
            ((EditText)mRootView.findViewById(R.id.editOldPhoneNumber)).setText(
                    PreferenceManager.getCountryCode()+" "+m_strOldPhoneNumber);
        } else {
            txtTitle.setText(R.string.title_add_phone_number);
            mRootView.findViewById(R.id.layoutOldPhone).setVisibility(View.GONE);
        }

        final EditText editCodeView =mRootView.findViewById(R.id.editCode);
        editCodeView.setText("+");
        Selection.setSelection(editCodeView.getText(), editCodeView.getText().length());

        editCodeView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().contains("+")){
                    editCodeView.setText("+");
                    Selection.setSelection(editCodeView.getText(), editCodeView.getText().length());

                }

            }
        });

        editCodeView.requestFocus();

        mRootView.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()){
                    procChangePhoneNumber();
                }
            }
        });
    }

    private boolean checkValidation() {
        EditText editCodeView =  mRootView.findViewById(R.id.editCode);
        EditText editNewPhoneNumberView =  mRootView.findViewById(R.id.editNewPhoneNumber);

        editCodeView.setError(null);
        editNewPhoneNumberView.setError(null);

        m_strCountryCode = editCodeView.getText().toString();
        if (TextUtils.isEmpty(m_strCountryCode)) {
            editCodeView.setError(getString(R.string.error_field_required));
            editCodeView.requestFocus();
            return  false;
        }

        m_strNewPhoneNumber = editNewPhoneNumberView.getText().toString();
        if (TextUtils.isEmpty(m_strNewPhoneNumber)) {
            editNewPhoneNumberView.setError(getString(R.string.error_field_required));
            editNewPhoneNumberView.requestFocus();
            return  false;
        }

        if (m_strNewPhoneNumber.equals(m_strOldPhoneNumber)){
            Toast.makeText(getActivity(), R.string.msg_same_phone, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void procChangePhoneNumber(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());
        jsonObject.addProperty("new_phone", m_strNewPhoneNumber);
        jsonObject.addProperty("country_code", m_strCountryCode);

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doChangePhoneNumber(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();

                BaseResult baseResult = response.body();
                Toast.makeText(getActivity(), baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                if (baseResult.getStatus() == 1) {
                    PreferenceManager.setPhone(m_strNewPhoneNumber);
                    PreferenceManager.setCountryCode(m_strCountryCode);
                    procRequestVerifyCode();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void procRequestVerifyCode (){
        dismiss();
        /*
        JsonObject jsonObject = new JsonObject();
        String strCountryCode = PreferenceManager.getCountryCode();
        if (strCountryCode.startsWith("+")){
            strCountryCode = strCountryCode.substring(1);
        }
        jsonObject.addProperty("via", "sms");
        jsonObject.addProperty("country_code", strCountryCode);
        jsonObject.addProperty("phone_number", PreferenceManager.getPhone());

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultRequestPhoneVerify> call = ((VerifyPhoneActivity)getActivity()).apiInterfaceTwilio.doRequestPhoneVerifyCode(jsonObject);
        call.enqueue(new Callback<ResultRequestPhoneVerify>() {
            @Override
            public void onResponse(Call<ResultRequestPhoneVerify> call, retrofit2.Response<ResultRequestPhoneVerify> response) {
                m_dlgWait.dismiss();
               if (response.body() != null) {
                    ResultRequestPhoneVerify resultRequestPhoneVerify = response.body();
                    Toast.makeText(getActivity(), resultRequestPhoneVerify.getMessage(), Toast.LENGTH_SHORT).show();
                    if (resultRequestPhoneVerify.isSuccess()){
                        dismiss();
                    }
                } else {
                    Converter<ResponseBody, ResultRequestPhoneVerify> errorConverter =
                            ApiClient.getPhoneVerifyClient().responseBodyConverter(ResultRequestPhoneVerify.class, new Annotation[0]);
                    try {
                        ResultRequestPhoneVerify error = errorConverter.convert(response.errorBody());
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.msg_err_failed, Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onFailure(Call<ResultRequestPhoneVerify> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

}
