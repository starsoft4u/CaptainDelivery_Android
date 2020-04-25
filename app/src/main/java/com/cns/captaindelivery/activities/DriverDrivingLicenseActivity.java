package com.cns.captaindelivery.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.utils.BitmapUtils;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.utils.Utils;
import com.google.gson.JsonObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class DriverDrivingLicenseActivity extends _BaseActivity implements View.OnClickListener {
    private static final int    PERMISSIONS_REQUEST_STORAGE = 10;

    private static final int    SELECT_PICTURE = 0;
    private static final int    TAKE_PICTURE = 1;

    ApiInterface apiInterface;

    String m_strLicenseNumber, m_strIssuedOn, m_strExpiryDate;

    String m_strPhotoPath = "";
    File mFile = null;

    ImageView mImageView;

    Calendar m_calendarIssuedOn = Calendar.getInstance();
    Calendar m_calendarExpirayDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_driving_license);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initView();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        checkPermission();
    }

    private void initView(){

        mImageView = findViewById(R.id.imgPhoto);

        findViewById(R.id.layoutBtnTakePhoto).setOnClickListener(this);
        findViewById(R.id.imgBtnTakePhoto).setOnClickListener(this);
        findViewById(R.id.imgPhoto).setOnClickListener(this);
        findViewById(R.id.imgBtnBack).setOnClickListener(this);
        findViewById(R.id.imgBtnBack).setVisibility(View.VISIBLE);
        findViewById(R.id.btnContinue).setOnClickListener(this);

        findViewById(R.id.editIssuedOn).setOnClickListener(this);
        findViewById(R.id.layoutEditIssuedOn).setOnClickListener(this);
        findViewById(R.id.editExpiryDate).setOnClickListener(this);
        findViewById(R.id.layoutEditExpiryDate).setOnClickListener(this);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ||ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DriverDrivingLicenseActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(DriverDrivingLicenseActivity.this, R.string.msg_err_permission, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        String myFormat = "yyyy-MM-dd";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myFormat);

        switch (v.getId()){
            case R.id.layoutBtnTakePhoto:
            case R.id.imgPhoto:
            case R.id.imgBtnTakePhoto:
                new AlertDialog.Builder(DriverDrivingLicenseActivity.this)
                        .setItems(R.array.choose_photo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        dialogInterface.dismiss();
                                        pickPhoto();
                                        break;
                                    case 1:
                                        dialogInterface.dismiss();
                                        capturePhoto();
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.btnContinue:
                if (checkValidation()){
                    procPostVehicleDetails();
                }
                break;
            case R.id.imgBtnBack:
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverDrivingLicenseActivity.this);
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
                break;
            case R.id.layoutEditIssuedOn:
            case R.id.editIssuedOn:
                new DatePickerDialog(DriverDrivingLicenseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        m_calendarIssuedOn.set(Calendar.YEAR, year);
                        m_calendarIssuedOn.set(Calendar.MONTH, month);
                        m_calendarIssuedOn.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        ((EditText)findViewById(R.id.editIssuedOn)).setText(simpleDateFormat.format(m_calendarIssuedOn.getTime()));
                    }
                }, m_calendarIssuedOn.get(Calendar.YEAR), m_calendarIssuedOn.get(Calendar.MONTH),m_calendarIssuedOn.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.layoutEditExpiryDate:
            case R.id.editExpiryDate:
                new DatePickerDialog(DriverDrivingLicenseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        m_calendarExpirayDate.set(Calendar.YEAR, year);
                        m_calendarExpirayDate.set(Calendar.MONTH, month);
                        m_calendarExpirayDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        ((EditText)findViewById(R.id.editExpiryDate)).setText(simpleDateFormat.format(m_calendarExpirayDate.getTime()));
                    }
                }, m_calendarExpirayDate.get(Calendar.YEAR), m_calendarExpirayDate.get(Calendar.MONTH),m_calendarExpirayDate.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }
    public void pickPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select"), SELECT_PICTURE);
    }

    public void capturePhoto(){
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "Photo_" + ts+"_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (mFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.cns.captaindelivery.fileprovider", mFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        }catch (Exception e){
            e.printStackTrace();;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Bitmap bitmap = null;
            switch (requestCode){
                case SELECT_PICTURE:
                    String selectAbpath = Utils.getPathFromUri(DriverDrivingLicenseActivity.this, data.getData());
                    //Picasso.with(this).load(new File(selectAbpath)).noFade().into(mImgPhoto);
                    m_strPhotoPath = selectAbpath;
                    bitmap = BitmapUtils.getSafeDecodeBitmap(m_strPhotoPath, 800);
                    if (bitmap != null) {
                        findViewById(R.id.layoutBtnTakePhoto).setVisibility(View.GONE);
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageBitmap(bitmap);
                    }
                    break;
                case TAKE_PICTURE:
                    if (mFile.exists()){
                        //Picasso.with(this).load(mFile).noFade().into(mImgPhoto);
                        m_strPhotoPath = mFile.getAbsolutePath();
                    }
                    bitmap = BitmapUtils.getSafeDecodeBitmap(m_strPhotoPath, 800);
                    if (bitmap != null) {
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageBitmap(bitmap);
                    }
                    mFile = null;
                    break;
            }
        }
    }

    private boolean checkValidation() {
        if (TextUtils.isEmpty(m_strPhotoPath)) {
            Toast.makeText(DriverDrivingLicenseActivity.this, R.string.msg_err_take_photo, Toast.LENGTH_SHORT).show();
            return false;
        }

        EditText editView;

        editView= findViewById(R.id.editLicenseNumber);  editView.setError(null);
        m_strLicenseNumber = editView.getText().toString();
        if (TextUtils.isEmpty(m_strLicenseNumber)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editIssuedOn);  editView.setError(null);
        m_strIssuedOn = editView.getText().toString();
        if (TextUtils.isEmpty(m_strIssuedOn)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }

        editView= findViewById(R.id.editExpiryDate);  editView.setError(null);
        m_strExpiryDate = editView.getText().toString();
        if (TextUtils.isEmpty(m_strExpiryDate)) {
            editView.setError(getString(R.string.error_field_required));
            editView.requestFocus();
            return false;
        }
        return true;
    }

    private void procPostVehicleDetails (){
        mFile = new File(m_strPhotoPath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), mFile);
        Map<String, RequestBody> mapParams = new HashMap<>();
        mapParams.put("user_id", RequestBody.create(MediaType.parse("text/plain"), PreferenceManager.getUserId()));
        mapParams.put("licence_number", RequestBody.create(MediaType.parse("text/plain"), m_strLicenseNumber));
        mapParams.put("issued_on", RequestBody.create(MediaType.parse("text/plain"), m_strIssuedOn));
        mapParams.put("expiry_date", RequestBody.create(MediaType.parse("text/plain"), m_strExpiryDate));


        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverDrivingLicenseActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doPostDrivingLicense(mapParams, reqFile);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();

                if (baseResult.getStatus() == 1){
                    Toast.makeText(DriverDrivingLicenseActivity.this, R.string.msg_activate_before, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DriverDrivingLicenseActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DriverDrivingLicenseActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverDrivingLicenseActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverDrivingLicenseActivity.this, null, null);
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
                    Intent intent = new Intent(DriverDrivingLicenseActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverDrivingLicenseActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
