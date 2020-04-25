package com.cns.captaindelivery.api;

import com.cns.captaindelivery.models.ResultCheckEmail;
import com.cns.captaindelivery.models.ResultNotification;
import com.cns.captaindelivery.models.ResultForgotPassword;
import com.cns.captaindelivery.models.ResultGeocode;
import com.cns.captaindelivery.models.ResultGooglePlaceDetail;
import com.cns.captaindelivery.models.ResultGooglePlaceList;
import com.cns.captaindelivery.models.ResultLogin;
import com.cns.captaindelivery.models.ResultOrderDetail;
import com.cns.captaindelivery.models.ResultOrderList;
import com.cns.captaindelivery.models.ResultPackageList;
import com.cns.captaindelivery.models.ResultPersonalInfo;
import com.cns.captaindelivery.models.ResultPlaceHistory;
import com.cns.captaindelivery.models.ResultRequestPhoneVerify;
import com.cns.captaindelivery.models.ResultSignup;
import com.google.gson.JsonObject;

import com.cns.captaindelivery.models.BaseResult;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface  ApiInterface {


    @POST("/App/login_request")
    Call<ResultLogin> doLogin(@Body JsonObject jsonObject);

    @POST("/App/forgot_password")
    Call<ResultForgotPassword> doForgotPassword(@Body JsonObject jsonObject);

    @POST("/App/reset_password")
    Call<BaseResult> doResetPassword(@Body JsonObject jsonObject);

    @POST("/App/register_request")
    Call<ResultSignup> doSignup(@Body JsonObject jsonObject);

    @POST("/App/delete_request")
    Call<BaseResult> doDeleteAccount(@Body JsonObject jsonObject);

    @POST("/App/change_email")
    Call<BaseResult> doChangeEmail(@Body JsonObject jsonObject);

    @POST("/App/verify_request")
    Call<BaseResult> doSendEmailVerifyRequest(@Body JsonObject jsonObject);

    @POST("/App/email_verify")
    Call<BaseResult> doCheckEmailVerifyCode(@Body JsonObject jsonObject);

    @POST("/App/change_phonenumber")
    Call<BaseResult> doChangePhoneNumber(@Body JsonObject jsonObject);

    @POST("/App/phone_verify")
    Call<BaseResult> doPostPhoneVerifyStatus(@Body JsonObject jsonObject);

    @Multipart
    @POST("/App/personal_info_driver")
    Call<ResultPersonalInfo> doPostPersonalInformationForDriver(@PartMap Map<String, RequestBody> map , @Part("image\"; filename=\"driver.jpg\" ") RequestBody file);

    @Multipart
    @POST("/App/personal_info_customer")
    Call<ResultPersonalInfo> doPostPersonalInformationForCustomer(@PartMap Map<String, RequestBody> map , @Part("image\"; filename=\"customer.jpg\" ") RequestBody file);

    @Multipart
    @POST("/App/vehicle_info_driver")
    Call<BaseResult> doPostVehicleDetails(@PartMap Map<String, RequestBody> map , @Part("vehicle_image\"; filename=\"vehicle.jpg\" ") RequestBody file);

    @POST("/App/vehicle_info_driver")
    Call<BaseResult> doPostVehicleType(@Body JsonObject jsonObject);

    @POST("/App/vehicle_info_driver")
    Call<BaseResult> doPostVehicleOwnership(@Body JsonObject jsonObject);

    @Multipart
    @POST("/App/vehicle_info_driver")
    Call<BaseResult> doPostDrivingLicense(@PartMap Map<String, RequestBody> map , @Part("licence_image\"; filename=\"vehicle.jpg\" ") RequestBody file);

    @POST("/App/all_package")
    Call<ResultPackageList> doAllPackages();

    @POST("/App/get_notifications")
    Call<ResultNotification> doGetNotifications(@Body JsonObject jsonObject);

    @Multipart
    @POST("/App/update_customer_profile")
    Call<ResultPersonalInfo> doUpdateCustomerProfile(@PartMap Map<String, RequestBody> map , @Part("image\"; filename=\"customer.jpg\" ") RequestBody file);

    @POST("/App/add_new_order")
    Call<BaseResult> doAddNewOrder(@Body JsonObject jsonObject);

    @POST("/App/get_customer_active_orders")
    Call<ResultOrderList> doGetCustomerActiveOrders(@Body JsonObject jsonObject);

    @POST("/App/get_customer_inactive_orders")
    Call<ResultOrderList> doGetCustomerInactiveOrders(@Body JsonObject jsonObject);

    @POST("/App/getOrderDetail")
    Call<ResultOrderDetail> doGetOrderDetail(@Body JsonObject jsonObject);

    @POST("/App/get_place_history")
    Call<ResultPlaceHistory> doGetPlaceHistory(@Body JsonObject jsonObject);

    @Multipart
    @POST("/App/update_driver_profile")
    Call<ResultPersonalInfo> doUpdateDriverProfile(@PartMap Map<String, RequestBody> map , @Part("image\"; filename=\"driver.jpg\" ") RequestBody file);

    @POST("/App/check_email_before_update_profile")
    Call<ResultCheckEmail> doCheckEmailBeforeUpdateProfile(@Body JsonObject jsonObject);

    @POST("/App/check_phone_before_update_profile")
    Call<BaseResult> doCheckPhoneBeforeUpdateProfile(@Body JsonObject jsonObject);

    @POST("/App/update_driver_location")
    Call<BaseResult> doUpdateDriverLocation(@Body JsonObject jsonObject);


    //Twilio

    @Headers("X-Authy-API-Key: cpfasOFQW9KRjX7UxQnoABesj11QDeZK")
    @POST("/protected/json/phones/verification/start")
    Call<ResultRequestPhoneVerify> doRequestPhoneVerifyCode(@Body JsonObject jsonObject);

    @Headers("X-Authy-API-Key: cpfasOFQW9KRjX7UxQnoABesj11QDeZK")
    @GET("/protected/json/phones/verification/check")
    Call<ResultRequestPhoneVerify> doCheckPhoneVerifyCode(@Query("country_code") String strCountryCode, @Query("phone_number") String strPhoneNumber, @Query("verification_code") String strVerificationCode);

    //Google Places
    @GET
    Call<ResultGooglePlaceList> doGooglePlaceSearch(@Url String url);

    @GET
    Call<ResultGooglePlaceDetail> doGooglePlaceDetail(@Url String url);

    @GET
    Call<ResultGeocode> doReverseGeocode(@Url String url);
}
