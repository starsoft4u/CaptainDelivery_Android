package com.cns.captaindelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.MainActivity;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.dialogs.ForgotPasswordFragment;
import com.cns.captaindelivery.models.InfoUser;
import com.cns.captaindelivery.models.ResultLogin;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends _BaseActivity  {

    boolean m_bDriverChecked = true;

    String m_strEmail, m_strPassword;

    ApiInterface apiInterface;

    //FB
    CallbackManager mFacebookCallbackManager;
    private static final String EMAIL = "email";

    //Google+
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        initFacebook();
        initGooglePlus();
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

        findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidation())
                    return;
                procCommonLogin(GlobalConst.LOGIN_NORMAL);
            }
        });

        findViewById(R.id.ripTxtForgotPassword).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                ForgotPasswordFragment dlgAlert = ForgotPasswordFragment.newInstance(PreferenceManager.getUserEmail());
                dlgAlert.show(fm, "fragment_forgot_password");
            }
        });

        findViewById(R.id.ripTxtGotoSignup).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView textView = (TextView)findViewById(R.id.txtHavenotAccount);
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        findViewById(R.id.imgBtnFBLogin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                if (isLoggedIn){
                    doFacebookLogin(accessToken);
                } else {
                    findViewById(R.id.facebook_button).performClick();
                }
            }
        });

        findViewById(R.id.imgBtnGLogin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
                if (account == null) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    doGoogleLogin(account);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            doGoogleLogin(account);
        } catch (ApiException e) {
            Log.w("kang", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(LoginActivity.this, R.string.msg_err_login_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void initFacebook(){
        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));

        loginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                doFacebookLogin(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, "Facebook Login Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initGooglePlus(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private boolean checkValidation() {
        EditText editEmailView =  findViewById(R.id.editEmail);
        EditText editPasswordView =  findViewById(R.id.editPassword);

        editEmailView.setError(null);
        editPasswordView.setError(null);

        m_strEmail = editEmailView.getText().toString();
        m_strPassword = editPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(m_strEmail)) {
            editEmailView.setError(getString(R.string.error_field_required));
            focusView = editEmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(m_strPassword)) {
            editPasswordView.setError(getString(R.string.error_field_required));
            focusView = editPasswordView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            return false;
        }
        return true;
    }

    private void doGoogleLogin(GoogleSignInAccount googleSignInAccount){
        m_strEmail = googleSignInAccount.getEmail();
        m_strPassword = googleSignInAccount.getId();
        procCommonLogin(GlobalConst.LOGIN_GOOGLE);
    }

    private void doFacebookLogin(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.v("LoginActivity Response ", response.toString());
                        try {
                            m_strEmail = object.getString("email");
                            m_strPassword = object.getString("id");
                            procCommonLogin(GlobalConst.LOGIN_FACEBOOK);
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


    private void procCommonLogin(final String strLoginMode){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user", m_strEmail);
        jsonObject.addProperty("password", m_strPassword);
        jsonObject.addProperty("token", PreferenceManager.getFCMToken());

        final ProgressDialog m_dlgWait = ProgressDialog.show(LoginActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultLogin> call = apiInterface.doLogin(jsonObject);
        call.enqueue(new Callback<ResultLogin>() {
            @Override
            public void onResponse(Call<ResultLogin> call, retrofit2.Response<ResultLogin> response) {
                m_dlgWait.dismiss();
                ResultLogin resultLogin = response.body();
                if (resultLogin.getStatus() == 1) {
                    InfoUser infoUser = resultLogin.getData();
                    boolean bIsDriver;
                    if (infoUser.getAuth().equals("1")){
                        //driver
                        if (m_bDriverChecked) {
                            if (infoUser.getIs_active() >0) {
                                Toast.makeText(LoginActivity.this, resultLogin.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.msg_driver, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        PreferenceManager.setRole(GlobalConst.ROLE_DRIVER);
                        PreferenceManager.setDriverId(infoUser.getExt_id());
                        bIsDriver = true;
                    } else {
                        //customer
                        PreferenceManager.setRole(GlobalConst.ROLE_CUSTOMER);
                        PreferenceManager.setCustomerId(infoUser.getExt_id());
                        if (m_bDriverChecked ) {
                            Toast.makeText(LoginActivity.this, R.string.msg_customer, Toast.LENGTH_SHORT).show();
                            return;
                        } else
                            Toast.makeText(LoginActivity.this, resultLogin.getMessage(), Toast.LENGTH_SHORT).show();
                        bIsDriver = false;
                    }

                    PreferenceManager.setUserEmail(infoUser.getEmail());
                    PreferenceManager.setPhone(infoUser.getPhone());
                    PreferenceManager.setCountryCode(infoUser.getCountry_code());
                    PreferenceManager.setPassword(m_strPassword);
                    PreferenceManager.setLoginMethod(strLoginMode);
                    PreferenceManager.setUserId(infoUser.getUser_id());
                    PreferenceManager.setUserName(infoUser.getName());
                    PreferenceManager.setRate(infoUser.getRate());

                    PreferenceManager.setUserPhoto(infoUser.getImage());
                    PreferenceManager.setAddrLat(infoUser.getLat());
                    PreferenceManager.setAddrLng(infoUser.getLng());
                    PreferenceManager.setCountry(infoUser.getCountry());
                    PreferenceManager.setCity(infoUser.getCity());
                    PreferenceManager.setRegion(infoUser.getRegion_street());

                    Intent intent;
//                    if (infoUser.getEmail_verify_status().equals("1") == false){
//                        intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
//                    } else if (infoUser.getPhone_verify_status().equals("1") == false){
//                        intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
//                    } else if (infoUser.getSignup_step().length() == 0) {
//                        intent = new Intent(LoginActivity.this, PersonalInformationActivity.class);
//                    } else if (infoUser.getSignup_step().equals("personal_information")) {
//                        if (bIsDriver)
//                            intent = new Intent(LoginActivity.this, DriverVehicleDetailsActivity.class);
//                        else
//                            intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
//                    } else if (infoUser.getSignup_step().equals("vehicle_details")) {
//                        intent = new Intent(LoginActivity.this, DriverVehicleTypeActivity.class);
//                    } else if (infoUser.getSignup_step().equals("vehicle_type")) {
//                        intent = new Intent(LoginActivity.this, DriverVehicleOwnershipActivity.class);
//                    } else if (infoUser.getSignup_step().equals("vehicle_ownership")) {
//                        intent = new Intent(LoginActivity.this, DriverDrivingLicenseActivity.class);
//                    } else if (infoUser.getSignup_step().equals("driving_license")) {
//                        if (infoUser.getIs_active() >0) {
//                            intent = new Intent(LoginActivity.this, DriverMainActivity.class);
//                        } else {
//                            Toast.makeText(LoginActivity.this, R.string.msg_activate_before, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        intent = new Intent(LoginActivity.this, DriverMainActivity.class);
//                    } else {
//                        if (bIsDriver) {
//                            if (infoUser.getIs_active() >0) {
//                                intent = new Intent(LoginActivity.this, DriverMainActivity.class);
//                            } else {
//                                Toast.makeText(LoginActivity.this, R.string.msg_activate_before, Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                        else {
//                            intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
//                        }
//                    }
                    intent = new Intent(LoginActivity.this, DriverMainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.msg_err_login_failed, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResultLogin> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(LoginActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }




}

