package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.ResultSignup;
import com.cns.captaindelivery.PreferenceManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;

public class SignupActivity extends _BaseActivity {

    ApiInterface apiInterface;

    String m_strEmail, m_strPassword, m_strPhoneNumber, m_strUserName, m_strCode;
    boolean m_bDriverChecked = true;


    //Google+
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    //FB
    CallbackManager mFacebookCallbackManager;
    private static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        initGooglePlus();
        initFacebook();
        initFCM();
    }

    private void initView(){

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioDriver) {
                    m_bDriverChecked = true;
                } else {
                    m_bDriverChecked = false;
                }
            }
        });
        m_bDriverChecked = true;
        findViewById(R.id.radioDriver).performClick();

        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()){
                    procCommonSignup(m_bDriverChecked);
                }
            }
        });

        TextView textView = (TextView)findViewById(R.id.txtHaveAccount);
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        findViewById(R.id.txtHaveAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.imgBtnGLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(SignupActivity.this);
                if (account == null) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    procGoogleSignup(m_bDriverChecked, account);
                }
            }
        });

        findViewById(R.id.imgBtnFBLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                if (isLoggedIn){
                    doFacebookSignin(accessToken);
                } else {
                    findViewById(R.id.facebook_button).performClick();
                }
            }
        });

        final EditText editCodeView =findViewById(R.id.editCode);
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

    }

    private void initFCM(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e("kang", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String strToken = task.getResult().getToken();
                        if (strToken != null && strToken.length() > 0){
                            PreferenceManager.setFCMToken(strToken);
                        }
                    }
                });
    }

    //-------Google--------
    private void initGooglePlus(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            procGoogleSignup(m_bDriverChecked, account);
        } catch (ApiException e) {
            Log.w("Kang", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(SignupActivity.this, R.string.msg_err_failed, Toast.LENGTH_SHORT).show();
        }
    }



    //FB------------------
    private void initFacebook(){
        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        loginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                doFacebookSignin(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(SignupActivity.this, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(SignupActivity.this, "Facebook Login Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkValidation() {
        EditText editEmailView =  findViewById(R.id.editEmail);
        EditText editPasswordView =  findViewById(R.id.editPassword);
        EditText editUsernameView = findViewById(R.id.editUserName);
        EditText editPhonenumberView = findViewById(R.id.editPhoneNumber);
        EditText editCodeView =findViewById(R.id.editCode);

        editEmailView.setError(null);
        editPasswordView.setError(null);
        editPhonenumberView.setError(null);
        editUsernameView.setError(null);
        editCodeView.setError(null);

        m_strUserName = editUsernameView.getText().toString();
        m_strEmail = editEmailView.getText().toString();
        m_strPassword = editPasswordView.getText().toString();
        m_strPhoneNumber = editPhonenumberView.getText().toString();
        m_strCode = editCodeView.getText().toString();

        if (TextUtils.isEmpty(m_strUserName)) {
            editUsernameView.setError(getString(R.string.error_field_required));
            editUsernameView.requestFocus();
            return  false;
        }
        if (TextUtils.isEmpty(m_strEmail)) {
            editEmailView.setError(getString(R.string.error_field_required));
            editEmailView.requestFocus();
            return  false;
        }
        if (m_strEmail.contains("@") == false) {
            editEmailView.setError(getString(R.string.error_invalid_email));
            editEmailView.requestFocus();
            return  false;
        }

        if (TextUtils.isEmpty(m_strPassword)) {
            editPasswordView.setError(getString(R.string.error_field_required));
            editPasswordView.requestFocus();
            return  false;
        }
        if (TextUtils.isEmpty(m_strCode)) {
            editCodeView.setError(getString(R.string.error_field_required));
            editCodeView.requestFocus();
            return  false;
        }
        if (TextUtils.isEmpty(m_strPhoneNumber)) {
            editPhonenumberView.setError(getString(R.string.error_field_required));
            editPhonenumberView.requestFocus();
            return  false;
        }

        return true;
    }

    private void procCommonSignup (final boolean bIsDriver){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", m_strUserName);
        jsonObject.addProperty("email", m_strEmail);
        jsonObject.addProperty("password", m_strPassword);
        jsonObject.addProperty("phone", m_strPhoneNumber);
        jsonObject.addProperty("country_code", m_strCode);
        jsonObject.addProperty("token", PreferenceManager.getFCMToken());
        jsonObject.addProperty("auth", bIsDriver?"1":"0");

        final ProgressDialog m_dlgWait = ProgressDialog.show(SignupActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultSignup> call = apiInterface.doSignup(jsonObject);
        call.enqueue(new Callback<ResultSignup>() {
            @Override
            public void onResponse(Call<ResultSignup> call, retrofit2.Response<ResultSignup> response) {
                m_dlgWait.dismiss();
                ResultSignup resultSignup = response.body();
                if (resultSignup.getStatus() == 1) {
                    PreferenceManager.setUserName(m_strUserName);
                    PreferenceManager.setUserId(resultSignup.getUser_id());
                    PreferenceManager.setUserEmail(m_strEmail);
                    PreferenceManager.setPassword(m_strPassword);
                    PreferenceManager.setLoginMethod(GlobalConst.LOGIN_NORMAL);
                    PreferenceManager.setCountryCode(m_strCode);
                    PreferenceManager.setPhone(m_strPhoneNumber);
                    PreferenceManager.setRole(bIsDriver? GlobalConst.ROLE_DRIVER:GlobalConst.ROLE_CUSTOMER);
                    Intent intent;
                    intent = new Intent(SignupActivity.this, VerifyEmailActivity.class);
//                    intent = new Intent(SignupActivity.this, PersonalInformationActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, resultSignup.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResultSignup> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(SignupActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procFacebookSignup (final boolean bIsDriver, final String strFBName, final String strFBEmail, final String strFBId){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", strFBName);
        jsonObject.addProperty("email", strFBEmail);
        jsonObject.addProperty("f_id", strFBId);
        jsonObject.addProperty("f_name", strFBName);
        jsonObject.addProperty("token", PreferenceManager.getFCMToken());
        jsonObject.addProperty("email_verify_status", "1");
        jsonObject.addProperty("auth", bIsDriver?"1":"0");

        final ProgressDialog m_dlgWait = ProgressDialog.show(SignupActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultSignup> call = apiInterface.doSignup(jsonObject);
        call.enqueue(new Callback<ResultSignup>() {
            @Override
            public void onResponse(Call<ResultSignup> call, retrofit2.Response<ResultSignup> response) {
                m_dlgWait.dismiss();
                ResultSignup resultSignup = response.body();
                if (resultSignup.getStatus() == 1) {
                    PreferenceManager.setUserName(strFBName);
                    PreferenceManager.setUserId(resultSignup.getUser_id());
                    PreferenceManager.setUserEmail(strFBEmail);
                    PreferenceManager.setPassword(strFBId);
                    PreferenceManager.setLoginMethod(GlobalConst.LOGIN_FACEBOOK);
                    PreferenceManager.setCountryCode("");
                    PreferenceManager.setPhone("");
                    PreferenceManager.setRole(bIsDriver? GlobalConst.ROLE_DRIVER:GlobalConst.ROLE_CUSTOMER);
                    Intent intent;
                    intent = new Intent(SignupActivity.this, VerifyPhoneActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, resultSignup.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResultSignup> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(SignupActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procGoogleSignup (final boolean bIsDriver, final GoogleSignInAccount googleSignInAccount){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", googleSignInAccount.getDisplayName());
        jsonObject.addProperty("email", googleSignInAccount.getEmail());
        jsonObject.addProperty("g_id", googleSignInAccount.getId());
        jsonObject.addProperty("g_email", googleSignInAccount.getEmail());
        jsonObject.addProperty("token", PreferenceManager.getFCMToken());
        jsonObject.addProperty("email_verify_status", "1");
        jsonObject.addProperty("auth", bIsDriver?"1":"0");

        final ProgressDialog m_dlgWait = ProgressDialog.show(SignupActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultSignup> call = apiInterface.doSignup(jsonObject);
        call.enqueue(new Callback<ResultSignup>() {
            @Override
            public void onResponse(Call<ResultSignup> call, retrofit2.Response<ResultSignup> response) {
                m_dlgWait.dismiss();
                ResultSignup resultSignup = response.body();
                if (resultSignup.getStatus() == 1) {
                    PreferenceManager.setUserName(googleSignInAccount.getDisplayName());
                    PreferenceManager.setUserId(resultSignup.getUser_id());
                    PreferenceManager.setUserEmail(googleSignInAccount.getEmail());
                    PreferenceManager.setPassword(googleSignInAccount.getId());
                    PreferenceManager.setLoginMethod(GlobalConst.LOGIN_GOOGLE);
                    PreferenceManager.setCountryCode("");
                    PreferenceManager.setPhone("");
                    PreferenceManager.setRole(bIsDriver? GlobalConst.ROLE_DRIVER:GlobalConst.ROLE_CUSTOMER);
                    Intent intent;
                    intent = new Intent(SignupActivity.this, VerifyPhoneActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, resultSignup.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResultSignup> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(SignupActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doFacebookSignin(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.v("LoginActivity Response ", response.toString());

                        try {
                            String strName = object.getString("name");
                            String strEmail = object.getString("email");
                            String strId = object.getString("id");

                            procFacebookSignup(m_bDriverChecked, strName, strEmail, strId);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
