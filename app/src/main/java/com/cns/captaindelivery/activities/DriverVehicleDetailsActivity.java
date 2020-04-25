package com.cns.captaindelivery.activities;

import android.Manifest;
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
import com.whiteelephant.monthpicker.MonthPickerDialog;

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

public class DriverVehicleDetailsActivity extends _BaseActivity implements View.OnClickListener {
    private static final int    PERMISSIONS_REQUEST_STORAGE = 10;

    private static final int    SELECT_PICTURE = 0;
    private static final int    TAKE_PICTURE = 1;

    ApiInterface apiInterface;

    String m_strBrand, m_strModel, m_strColor, m_strInteriorColor;
    int m_nYear = 0;

    String m_strPhotoPath = "";
    File mFile = null;

    ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_vehicle_detail);

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
        findViewById(R.id.btnContinue).setOnClickListener(this);
        findViewById(R.id.layoutEditYear).setOnClickListener(this);
        findViewById(R.id.editYear).setOnClickListener(this);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ||ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DriverVehicleDetailsActivity.this,
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
                    Toast.makeText(DriverVehicleDetailsActivity.this, R.string.msg_err_permission, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layoutBtnTakePhoto:
            case R.id.imgPhoto:
            case R.id.imgBtnTakePhoto:
                new AlertDialog.Builder(DriverVehicleDetailsActivity.this)
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
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverVehicleDetailsActivity.this);
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
            case R.id.layoutEditYear:
            case R.id.editYear:
                MonthPickerDialog.Builder builder1 = new MonthPickerDialog.Builder(DriverVehicleDetailsActivity.this, new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        m_nYear = selectedYear;
                        ((EditText)findViewById(R.id.editYear)).setText(String.valueOf(m_nYear));
                    }
                }, m_nYear==0?Calendar.getInstance().get(Calendar.YEAR):m_nYear, 0);

                builder1.showYearOnly()
                        .setYearRange(1900, Calendar.getInstance().get(Calendar.YEAR))
                        .build()
                        .show();
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
                    String selectAbpath = Utils.getPathFromUri(DriverVehicleDetailsActivity.this, data.getData());
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
            Toast.makeText(DriverVehicleDetailsActivity.this, R.string.msg_err_take_photo, Toast.LENGTH_SHORT).show();
            return false;
        }

        EditText editBrand = findViewById(R.id.editBrand);
        EditText editModel = findViewById(R.id.editModel);
        EditText editYear = findViewById(R.id.editYear);
        EditText editColor = findViewById(R.id.editColor);
        EditText editInteriorColor = findViewById(R.id.editInteriorColor);

        editBrand.setError(null);
        editModel.setError(null);
        editYear.setError(null);
        editColor.setError(null);
        editInteriorColor.setError(null);

        m_strBrand = editBrand.getText().toString();
        m_strModel = editModel.getText().toString();
        m_strColor = editColor.getText().toString();
        m_strInteriorColor = editColor.getText().toString();

        if (TextUtils.isEmpty(m_strBrand)) {
            editBrand.setError(getString(R.string.error_field_required));
            editBrand.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(m_strModel)) {
            editModel.setError(getString(R.string.error_field_required));
            editModel.requestFocus();
            return false;
        }
        if (m_nYear == 0) {
            editYear.setError(getString(R.string.error_field_required));
            return false;
        }
        if (TextUtils.isEmpty(m_strColor)) {
            editColor.setError(getString(R.string.error_field_required));
            editColor.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(m_strInteriorColor)) {
            editInteriorColor.setError(getString(R.string.error_field_required));
            editInteriorColor.requestFocus();
            return false;
        }

        return true;
    }

    private void procPostVehicleDetails (){
        mFile = new File(m_strPhotoPath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), mFile);
        Map<String, RequestBody> mapParams = new HashMap<>();
        mapParams.put("user_id", RequestBody.create(MediaType.parse("text/plain"), PreferenceManager.getUserId()));
        mapParams.put("brand", RequestBody.create(MediaType.parse("text/plain"), m_strBrand));
        mapParams.put("model", RequestBody.create(MediaType.parse("text/plain"), m_strModel));
        mapParams.put("year", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(m_nYear)));
        mapParams.put("color", RequestBody.create(MediaType.parse("text/plain"), m_strColor));
        mapParams.put("interior_color", RequestBody.create(MediaType.parse("text/plain"), m_strInteriorColor));

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverVehicleDetailsActivity.this, null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<BaseResult> call = apiInterface.doPostVehicleDetails(mapParams, reqFile);
        call.enqueue(new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {
                m_dlgWait.dismiss();
                BaseResult baseResult = response.body();
                Toast.makeText(DriverVehicleDetailsActivity.this, baseResult.getMessage(), Toast.LENGTH_SHORT).show();
                if (baseResult.getStatus() == 1){
                    Intent intent = new Intent(DriverVehicleDetailsActivity.this, DriverVehicleTypeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverVehicleDetailsActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procDeleteAccount (){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());

        final ProgressDialog m_dlgWait = ProgressDialog.show(DriverVehicleDetailsActivity.this, null, null);
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
                    Intent intent = new Intent(DriverVehicleDetailsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                m_dlgWait.dismiss();
                Toast.makeText(DriverVehicleDetailsActivity.this, R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
