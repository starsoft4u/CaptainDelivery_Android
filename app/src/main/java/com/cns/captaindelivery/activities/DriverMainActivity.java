package com.cns.captaindelivery.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.fragments.CustomerActiveOrderFragment;
import com.cns.captaindelivery.fragments.CustomerInactiveOrderFragment;
import com.cns.captaindelivery.fragments.CustomerNotiFragment;
import com.cns.captaindelivery.fragments.DriverHomeFragment;
import com.cns.captaindelivery.fragments.DriverProfileFragment;
import com.cns.captaindelivery.services.GPSService;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.widgets.bottom_nav_service.BottomNavigationViewNew;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

public class DriverMainActivity extends _BaseActivity {
    private DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;

    private BottomNavigationViewNew.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationViewNew.OnNavigationItemSelectedListener() {
        @SuppressLint({"ResourceAsColor", "SetTextI18n", "NewApi"})
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.home_bottom:
                    selectedFragment = DriverHomeFragment.newInstance();
                    break;
                case R.id.order_bottom:
                    selectedFragment = CustomerActiveOrderFragment.newInstance();
                    break;
                case R.id.profile_bottom:
                    selectedFragment = DriverProfileFragment.newInstance();
                    break;

                case R.id.noti_bottom:
                    selectedFragment = CustomerNotiFragment.newInstance();

                    break;
                default:

            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            assert selectedFragment != null;
            transaction.replace(R.id.cus_content, selectedFragment).addToBackStack("tag").commit();
            return true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        BottomNavigationViewNew bnve = findViewById(R.id.navigation);
        bnve.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        refreshDrawer();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        boolean bFromNotification = getIntent().getBooleanExtra(GlobalConst.KEY_FROM_NOFIFICATION, false);
        if (bFromNotification)
            findViewById(R.id.noti_bottom).performClick();
        else
            transaction.replace(R.id.cus_content, DriverHomeFragment.newInstance()).addToBackStack("tag").commit();

        setupToolbar();

        clikListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "TEST", Toast.LENGTH_SHORT).show();
        boolean bFromNotification = intent.getBooleanExtra(GlobalConst.KEY_FROM_NOFIFICATION, false);
        if (bFromNotification)
            findViewById(R.id.noti_bottom).performClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGPSService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGPSService();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setupToolbar(){
        mDrawerLayout = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.app_name, R.string.app_name);
        //mDrawerLayout.setDrawerListener(mDrawerToggle);
        //mDrawerToggle.syncState();
    }

    public void refreshDrawer(){
        ((TextView)findViewById(R.id.txtMyName)).setText(PreferenceManager.getUserName());
        String strPhotoUrl = PreferenceManager.getUserPhoto();
        if (strPhotoUrl.length()>0)
            Picasso.get().load(strPhotoUrl).error(R.drawable.no_image).into((ImageView)findViewById(R.id.imgMyPhoto));
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        float fRate = PreferenceManager.getRate();
        ratingBar.setRating(fRate);
        ((TextView)findViewById(R.id.txtRate)).setText(String.format("(%.1f)", fRate));
    }


    public void clikListener(){

        findViewById(R.id.btnTabActiveOrders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) return;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.cus_content, CustomerActiveOrderFragment.newInstance()).addToBackStack("tag").commit();
            }
        });
        findViewById(R.id.btnTabInactiveOrders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) return;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.cus_content, CustomerInactiveOrderFragment.newInstance()).addToBackStack("tag").commit();
            }
        });

        findViewById(R.id.txtBtnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverMainActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Do you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String strLoginMode = PreferenceManager.getLoginMethod();
                                switch (strLoginMode){
                                    case GlobalConst.LOGIN_FACEBOOK:
                                        logoutFromFacebook();
                                        break;
                                    case GlobalConst.LOGIN_GOOGLE:
                                        logoutFromGoogle();
                                        break;
                                    default:
                                        gotoLogin();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
    public void switchContent(int id, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(id, fragment, fragment.toString());
        ft.addToBackStack(null);
        ft.commit();
    }

    public void logoutFromFacebook() {

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn == false) {
            gotoLogin();
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();
                gotoLogin();
            }
        }).executeAsync();
    }

    private void logoutFromGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(DriverMainActivity.this);
        if (account == null) {
            gotoLogin();
            return;
        }

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        gotoLogin();
                    }
                });
    }

    private void gotoLogin(){
        PreferenceManager.resetAll();
        Intent intent=new Intent(DriverMainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startGPSService(){
        /*
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(DriverMainActivity.this, GPSService.class));
                //Toast.makeText(CustomerMainActivity.this, "Tracking Started (1)..", Toast.LENGTH_SHORT).show();
            } else {

                startService(new Intent(DriverMainActivity.this, GPSService.class));
                //Toast.makeText(CustomerMainActivity.this, "Tracking Started (2)..", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.msg_err_permission, Toast.LENGTH_SHORT).show();
        }
        */
    }

    private void stopGPSService(){
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, GPSService.class);
            intent.setAction(GPSService.ACTION_STOP_FOREGROUND_SERVICE);
            startService(intent);
        } else {
            stopService(new Intent(DriverMainActivity.this, GPSService.class));
        }
        */
    }
}
