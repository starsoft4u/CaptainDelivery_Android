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
import android.widget.EditText;
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

public class ChangeEmailFragment extends DialogFragment {

    View mRootView;

    ApiInterface apiInterface;

    String m_strOldEmail, m_strNewEmail;

    public static ChangeEmailFragment newInstance(String strEmail) {
        ChangeEmailFragment frag = new ChangeEmailFragment();
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
        mRootView = inflater.inflate(R.layout.fragment_change_email, container, false);
        m_strOldEmail = getArguments().getString(GlobalConst.KEY_EMAIL, "");

        ((EditText)mRootView.findViewById(R.id.editOldEmail)).setText(m_strOldEmail);
        mRootView.findViewById(R.id.editNewEmail).requestFocus();

        mRootView.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()){
                    procChangeEmail();
                }
            }
        });
        return mRootView;
    }

    private boolean checkValidation() {
        EditText editNewEmailView =  mRootView.findViewById(R.id.editNewEmail);

        editNewEmailView.setError(null);

        m_strNewEmail = editNewEmailView.getText().toString();
        if (TextUtils.isEmpty(m_strNewEmail)) {
            editNewEmailView.setError(getString(R.string.error_field_required));
            editNewEmailView.requestFocus();
            return  false;
        }
        if (m_strNewEmail.contains("@") == false) {
            editNewEmailView.setError(getString(R.string.error_invalid_email));
            editNewEmailView.requestFocus();
            return  false;
        }

        return true;
    }

    private void procChangeEmail (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());
        jsonObject.addProperty("old_email", m_strOldEmail);
        jsonObject.addProperty("new_email", m_strNewEmail);

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doChangeEmail(jsonObject);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();

                BaseResult baseResult = response.body();
                Toast.makeText(getActivity(), baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                if (baseResult.getStatus() == 1) {
                    PreferenceManager.setUserEmail(m_strNewEmail);
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
