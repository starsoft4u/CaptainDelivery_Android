package com.cns.captaindelivery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.InfoUser;
import com.cns.captaindelivery.models.ResultLogin;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class SplashActivity extends _BaseActivity {
    private static final int    PERMISSIONS_REQUEST_ALL = 11;
    private static final int  REQ_GPS_ENABLE = 21;

    ApiInterface apiInterface;

    private static int m_nWhatHandler = 1;
    private static long	m_lDelayTime = 3000;				//ms
    boolean m_bFinished = false;
    boolean m_bCheckedPermission = false;

    final int PAGE_LOGIN = 0;
    final int PAGE_DRIVER_MAIN = 1;
    final int PAGE_CUSTOMER_MAIN = 2;
    final int PAGE_VERIFY_EMAIL = 3;
    final int PAGE_VERIFY_PHONE = 4;
    final int PAGE_PERSONAL_INFORMATION = 5;
    final int PAGE_VEHICLE_DETAILS = 6;
    final int PAGE_VEHICLE_TYPE = 7;
    final int PAGE_VEHICLE_OWNERSHIP = 8;
    final int PAGE_DRIVING_LICENSE = 9;

    int m_nNextPage = PAGE_LOGIN;

    boolean m_bFromNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        m_bFromNotification = getIntent().getBooleanExtra(GlobalConst.KEY_FROM_NOFIFICATION, false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        m_bFinished = false;
        mHandler.sendEmptyMessageDelayed(m_nWhatHandler, m_lDelayTime);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        //refreshedToken = FirebaseInstanceId.getInstance().getToken();
        checkPermission();
        checkLogin();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)     {
            mHandler.removeMessages(m_nWhatHandler);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (m_bFinished && m_bCheckedPermission) {
                switch (m_nNextPage){
                    case PAGE_CUSTOMER_MAIN:
                        gotoCustomerMainPage();
                        break;
                    case PAGE_DRIVER_MAIN:
                        gotoDriverMainPage();
                        break;
                    case PAGE_VERIFY_EMAIL:
                        gotoVerifyEmailPage();
                        break;
                    case PAGE_VERIFY_PHONE:
                        gotoVerifyPhonePage();
                        break;
                    case PAGE_PERSONAL_INFORMATION:
                        gotoPersonalInformationPage();
                        break;
                    case PAGE_VEHICLE_DETAILS:
                        gotoVehicleDetailsPage();
                        break;
                    case PAGE_VEHICLE_TYPE:
                        gotoVehicleTypePage();
                        break;
                    case PAGE_VEHICLE_OWNERSHIP:
                        gotoVehicleOwnershipPage();
                        break;
                    case PAGE_DRIVING_LICENSE:
                        gotoDrivingLicensePage();
                        break;

                    default:
                        gotoLoginPage();
                }
            } else {
                mHandler.sendEmptyMessageDelayed(m_nWhatHandler, m_lDelayTime/2L);
            }
        }
    };

    private void checkLogin(){
        String login= PreferenceManager.getLoginMethod();

//        if(!login.equals("")) {
//            procCommonLogin(PreferenceManager.getRole());
//        }else {
            m_nNextPage = PAGE_LOGIN;
            m_bFinished = true;
//        }
    }

    private void procCommonLogin(final int nRole){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user", PreferenceManager.getUserEmail());
        jsonObject.addProperty("password", PreferenceManager.getPassword());
        jsonObject.addProperty("token", PreferenceManager.getFCMToken());
        Call<ResultLogin> call = apiInterface.doLogin(jsonObject);
        call.enqueue(new Callback<ResultLogin>() {
            @Override
            public void onResponse(Call<ResultLogin> call, retrofit2.Response<ResultLogin> response) {
                ResultLogin resultLogin = response.body();
                if (resultLogin == null) {
                    Toast.makeText(SplashActivity.this, R.string.msg_err_response, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultLogin.getStatus() == 1) {
                    InfoUser infoUser = resultLogin.getData();

                    if (infoUser.getEmail_verify_status().equals("1") == false){
                        m_nNextPage = PAGE_VERIFY_EMAIL;
                    } else if (infoUser.getPhone_verify_status().equals("1") == false){
                        m_nNextPage = PAGE_VERIFY_PHONE;
                    } else if (infoUser.getSignup_step().length() == 0) {
                        m_nNextPage = PAGE_PERSONAL_INFORMATION;
                    } else if (infoUser.getSignup_step().equals("personal_information")) {
                        if (nRole == GlobalConst.ROLE_DRIVER)
                            m_nNextPage = PAGE_VEHICLE_DETAILS;
                        else
                            m_nNextPage = PAGE_CUSTOMER_MAIN;
                    } else if (infoUser.getSignup_step().equals("vehicle_details")) {
                        m_nNextPage = PAGE_VEHICLE_TYPE;
                    } else if (infoUser.getSignup_step().equals("vehicle_type")) {
                        m_nNextPage = PAGE_VEHICLE_OWNERSHIP;
                    } else if (infoUser.getSignup_step().equals("vehicle_ownership")) {
                        m_nNextPage = PAGE_DRIVING_LICENSE;
                    } else if (infoUser.getSignup_step().equals("driving_license")) {
                        if (infoUser.getIs_active() == 1) {
                            m_nNextPage = PAGE_DRIVER_MAIN;
                        } else {
                            m_nNextPage = PAGE_LOGIN;
                        }
                    } else {
                        if (nRole == GlobalConst.ROLE_DRIVER) {
                            if (infoUser.getIs_active() == 1)
                                m_nNextPage = PAGE_DRIVER_MAIN;
                            else {
                                m_nNextPage = PAGE_LOGIN;
                            }
                        } else
                            m_nNextPage = PAGE_CUSTOMER_MAIN;
                    }

                } else {
                    m_nNextPage = PAGE_LOGIN;
                }
                m_bFinished = true;
            }

            @Override
            public void onFailure(Call<ResultLogin> call, Throwable t) {
                m_nNextPage = PAGE_LOGIN;
                m_bFinished = true;
                //Toast.makeText(SplashActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GPS_ENABLE){
            switch (m_nNextPage) {
                case PAGE_CUSTOMER_MAIN:
                    gotoCustomerMainPage();
                    break;
                case PAGE_DRIVER_MAIN:
                    gotoDriverMainPage();
                    break;
                case PAGE_VERIFY_EMAIL:
                    gotoVerifyEmailPage();
                    break;
                case PAGE_VERIFY_PHONE:
                    gotoVerifyPhonePage();
                    break;
                case PAGE_PERSONAL_INFORMATION:
                    gotoPersonalInformationPage();
                    break;
                case PAGE_VEHICLE_DETAILS:
                    gotoVehicleDetailsPage();
                    break;
                case PAGE_VEHICLE_TYPE:
                    gotoVehicleTypePage();
                    break;
                case PAGE_VEHICLE_OWNERSHIP:
                    gotoVehicleOwnershipPage();
                    break;
                case PAGE_DRIVING_LICENSE:
                    gotoDrivingLicensePage();
                    break;

                default:
                    gotoLoginPage();
            }
        }
    }

    private void gotoDriverMainPage(){
        /*
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQ_GPS_ENABLE);
        } else {
        */
            Intent intent = new Intent(SplashActivity.this, DriverMainActivity.class);
            intent.putExtra(GlobalConst.KEY_FROM_NOFIFICATION, m_bFromNotification);
            startActivity(intent);
            finish();
        //}
    }
    private void gotoCustomerMainPage(){
        /*
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQ_GPS_ENABLE);
        } else {
        */
            Intent intent = new Intent(SplashActivity.this, CustomerMainActivity.class);
            intent.putExtra(GlobalConst.KEY_FROM_NOFIFICATION, m_bFromNotification);
            startActivity(intent);
            finish();
        //}
    }
    private void gotoLoginPage(){
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoVerifyEmailPage(){
        Intent intent = new Intent(SplashActivity.this, VerifyEmailActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoVerifyPhonePage(){
        Intent intent = new Intent(SplashActivity.this, VerifyPhoneActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoPersonalInformationPage(){
        Intent intent = new Intent(SplashActivity.this, PersonalInformationActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoVehicleDetailsPage(){
        Intent intent = new Intent(SplashActivity.this, DriverVehicleDetailsActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoVehicleTypePage(){
        Intent intent = new Intent(SplashActivity.this, DriverVehicleTypeActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoVehicleOwnershipPage(){
        Intent intent = new Intent(SplashActivity.this, DriverVehicleOwnershipActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoDrivingLicensePage(){
        Intent intent = new Intent(SplashActivity.this, DriverDrivingLicenseActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_ALL);
        } else {
            m_bCheckedPermission = true;
            PreferenceManager preferenceManager = (PreferenceManager) getApplication();
            preferenceManager.startMainService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ALL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    m_bCheckedPermission = true;
                    PreferenceManager preferenceManager = (PreferenceManager) getApplication();
                    preferenceManager.startMainService();
                } else {
                    Toast.makeText(SplashActivity.this, R.string.msg_err_permission, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


}
