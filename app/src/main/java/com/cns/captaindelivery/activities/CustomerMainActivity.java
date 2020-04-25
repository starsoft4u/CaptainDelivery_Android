package com.cns.captaindelivery.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.fragments.CustomerActiveOrderFragment;
import com.cns.captaindelivery.fragments.CustomerHistoryFragment;
import com.cns.captaindelivery.fragments.CustomerHomeFragment;
import com.cns.captaindelivery.fragments.CustomerInactiveOrderFragment;
import com.cns.captaindelivery.fragments.CustomerNearbyFragment;
import com.cns.captaindelivery.fragments.CustomerNotiFragment;
import com.cns.captaindelivery.fragments.CustomerProfileFragment;
import com.cns.captaindelivery.fragments.CustomerSearchFragment;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

public class CustomerMainActivity extends _BaseActivity {
    private static final int    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 11;

    public static final int     REQ_NEW_ORDER = 22;

    private String TAG="customermainactivity";
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
                    selectedFragment = CustomerHomeFragment.newInstance();
                    break;
                case R.id.order_bottom:
                    selectedFragment = CustomerActiveOrderFragment.newInstance();
                    break;
                    /*
                case R.id.chat_bottom:
                    selectedFragment = CustomerChatHistoryFragment.newInstance();
                    break;
                    */
                case R.id.profile_bottom:
                    selectedFragment = CustomerProfileFragment.newInstance();
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
        setContentView(R.layout.activity_customer_main);
        BottomNavigationViewNew bnve = (BottomNavigationViewNew) findViewById(R.id.navigation);
        bnve.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        refreshDrawer();

        initAllowedPlaceTypes();

        //first display
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        boolean bFromNotification = getIntent().getBooleanExtra(GlobalConst.KEY_FROM_NOFIFICATION, false);
        if (bFromNotification) {
            findViewById(R.id.noti_bottom).performClick();
        } else {
            transaction.replace(R.id.cus_content, CustomerHomeFragment.newInstance()).addToBackStack("tag").commit();
            findViewById(R.id.btnTabAll).setSelected(true);
        }



        setupToolbar();

        clikListener();

        initFCMToken();
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

    private void initFCMToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d("Kang", "FCM Token : "+token==null?"null":"");

                        // Log and toast
                        /*
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(CustomerMainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        */
                    }
                });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_NEW_ORDER) {
            if (resultCode == RESULT_OK) {
                findViewById(R.id.order_bottom).performClick();
            }
        }
    }

    public void clikListener(){

        findViewById(R.id.imagesearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.cus_content, CustomerSearchFragment.newInstance()).addToBackStack("tag").commit();
            }
        });
        findViewById(R.id.btnTabHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) return;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.cus_content, CustomerHistoryFragment.newInstance()).addToBackStack("tag").commit();
            }
        });
        findViewById(R.id.btnTabAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) return;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.cus_content, CustomerHomeFragment.newInstance()).addToBackStack("tag").commit();
            }
        });
        findViewById(R.id.btnTabNearby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) return;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.cus_content, CustomerNearbyFragment.newInstance()).addToBackStack("tag").commit();
            }
        });
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
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMainActivity.this);
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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(CustomerMainActivity.this);
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
        Intent intent=new Intent(CustomerMainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startGPSService(){
        /*
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(CustomerMainActivity.this, GPSService.class));
                //Toast.makeText(CustomerMainActivity.this, "Tracking Started (1)..", Toast.LENGTH_SHORT).show();
            } else {

                startService(new Intent(CustomerMainActivity.this, GPSService.class));
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
            stopService(new Intent(CustomerMainActivity.this, GPSService.class));
        }
        */
    }

    private void initAllowedPlaceTypes(){
        GlobalConst.ALLOWED_PLACE_TYPES.clear();
        GlobalConst.ALLOWED_PLACE_TYPES.add("art_gallery");
        GlobalConst.ALLOWED_PLACE_TYPES.add("bakery");
        GlobalConst.ALLOWED_PLACE_TYPES.add("bar");
        GlobalConst.ALLOWED_PLACE_TYPES.add("beauty_salon");
        GlobalConst.ALLOWED_PLACE_TYPES.add("bicycle_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("book_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("cafe");
        GlobalConst.ALLOWED_PLACE_TYPES.add("car_dealer");
        GlobalConst.ALLOWED_PLACE_TYPES.add("car_rental");
        GlobalConst.ALLOWED_PLACE_TYPES.add("car_repair");
        GlobalConst.ALLOWED_PLACE_TYPES.add("car_wash");
        GlobalConst.ALLOWED_PLACE_TYPES.add("casino");
        GlobalConst.ALLOWED_PLACE_TYPES.add("cemetery");
        GlobalConst.ALLOWED_PLACE_TYPES.add("clothing_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("convenience_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("department_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("electrician");
        GlobalConst.ALLOWED_PLACE_TYPES.add("electronics_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("florist");
        GlobalConst.ALLOWED_PLACE_TYPES.add("furniture_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("gas_station");
        GlobalConst.ALLOWED_PLACE_TYPES.add("hair_care");
        GlobalConst.ALLOWED_PLACE_TYPES.add("hardware_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("home_goods_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("insurance_agency");
        GlobalConst.ALLOWED_PLACE_TYPES.add("jewelry_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("laundry");
        GlobalConst.ALLOWED_PLACE_TYPES.add("library");
        GlobalConst.ALLOWED_PLACE_TYPES.add("liquor_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("locksmith");
        GlobalConst.ALLOWED_PLACE_TYPES.add("lodging");
        GlobalConst.ALLOWED_PLACE_TYPES.add("meal_delivery");
        GlobalConst.ALLOWED_PLACE_TYPES.add("meal_takeaway");
        GlobalConst.ALLOWED_PLACE_TYPES.add("movie_rental");
        GlobalConst.ALLOWED_PLACE_TYPES.add("movie_theater");
        GlobalConst.ALLOWED_PLACE_TYPES.add("moving_company");
        GlobalConst.ALLOWED_PLACE_TYPES.add("night_club");
        GlobalConst.ALLOWED_PLACE_TYPES.add("painter");
        GlobalConst.ALLOWED_PLACE_TYPES.add("park");
        GlobalConst.ALLOWED_PLACE_TYPES.add("parking");
        GlobalConst.ALLOWED_PLACE_TYPES.add("pet_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("pharmacy");
        GlobalConst.ALLOWED_PLACE_TYPES.add("physiotherapist");
        GlobalConst.ALLOWED_PLACE_TYPES.add("plumber");
        GlobalConst.ALLOWED_PLACE_TYPES.add("post_office");
        GlobalConst.ALLOWED_PLACE_TYPES.add("real_estate_agency");
        GlobalConst.ALLOWED_PLACE_TYPES.add("restaurant");
        GlobalConst.ALLOWED_PLACE_TYPES.add("roofing_contractor");
        GlobalConst.ALLOWED_PLACE_TYPES.add("rv_park");
        GlobalConst.ALLOWED_PLACE_TYPES.add("shoe_store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("shopping_mall");
        GlobalConst.ALLOWED_PLACE_TYPES.add("spa");
        GlobalConst.ALLOWED_PLACE_TYPES.add("stadium");
        GlobalConst.ALLOWED_PLACE_TYPES.add("storage");
        GlobalConst.ALLOWED_PLACE_TYPES.add("store");
        GlobalConst.ALLOWED_PLACE_TYPES.add("supermarket");
        GlobalConst.ALLOWED_PLACE_TYPES.add("taxi_stand");
        GlobalConst.ALLOWED_PLACE_TYPES.add("train_station");
        GlobalConst.ALLOWED_PLACE_TYPES.add("transit_station");
        GlobalConst.ALLOWED_PLACE_TYPES.add("travel_agency");
        GlobalConst.ALLOWED_PLACE_TYPES.add("veterinary_care");
    }
}
