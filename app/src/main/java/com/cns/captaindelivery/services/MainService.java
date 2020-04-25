package com.cns.captaindelivery.services;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.IPlaybackService;
import com.cns.captaindelivery.PreferenceManager;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.activities.DriverVehicleOwnershipActivity;
import com.cns.captaindelivery.activities.DriverVehicleTypeActivity;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.BaseResult;
import com.cns.captaindelivery.utils.Utils;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;


public class MainService extends Service {
    private double m_dLongitude = 0, m_dLatitude = 0;
	private boolean m_bLocationChanged = false;

	LocationManager m_locationManager = null;
	Location m_locationLast = null;
    String m_strCurProvider;

    public UploadScheduler m_uploadScheduler = null;

	ApiInterface apiInterface;
    private final IPlaybackService.Stub uploadServiceStub = new IPlaybackService.Stub() {
		@Override
		public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

		}
	};

    @Override
    public void onCreate() {
        super.onCreate();

		apiInterface = ApiClient.getClient().create(ApiInterface.class);

		setupLocationListeners();

		createUploadScheduleThread();


    }


	public void createUploadScheduleThread(){
		m_uploadScheduler = new UploadScheduler(this);
		final Thread uploadSchedulerThread = new Thread(m_uploadScheduler);
		uploadSchedulerThread.start();
	}

    @Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}


	@Override
	public void onDestroy() {
		try {
    		m_locationManager.removeUpdates(mGpsLocationListener);
    		m_locationManager.removeUpdates(mNetworkLocationListener);
    		
    	} catch (Exception e) {
    		
    	}
		
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		return uploadServiceStub;
	}



	/**
	 * Set up listeners for both GPS and wifi.
	 */
	private void setupLocationListeners(){
		if (m_locationManager != null)	return;

		if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			m_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

			try {
				m_locationLast = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (m_locationLast == null) {
					m_locationLast = m_locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
				if (m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					m_strCurProvider = LocationManager.GPS_PROVIDER;
				} else {
					m_strCurProvider = LocationManager.NETWORK_PROVIDER;
				}
				m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GlobalConst.GPS_MIN_TIME, GlobalConst.GPS_MIN_DISTANCE, mGpsLocationListener);
				m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GlobalConst.NETWORK_MIN_TIME, GlobalConst.NETWORK_MIN_DISTANCE, mNetworkLocationListener);
			} catch (Exception e){
				e.printStackTrace();
			}

			if (m_locationLast != null) {
				updateLocation(m_locationLast);
			}
		}
	}
	
	/**
	 * GPS Location Listener
	 */
	private final LocationListener mGpsLocationListener = new LocationListener() {
    	@Override
    	public void onLocationChanged(Location location) {
    		if (m_locationLast == null) {
    			m_locationLast = location;
    			updateLocation(location);
    		} else {
	    		if (Utils.isBetterLocation(location, m_locationLast) == true) {
					Log.d("LocationFactory.java", "Location Acquired: " + location.toString());
					m_locationLast = location;
					updateLocation(location);
				}
    		}
    	}
    	
    	@Override
    	public void onProviderDisabled(String provider) {
    		m_strCurProvider = LocationManager.NETWORK_PROVIDER;
    		
			Log.e("Service GPS", "Gps disabled");
    	}
    	
    	public void onProviderEnabled(String provider) {
    		m_strCurProvider = LocationManager.GPS_PROVIDER;
			Log.e("Service GPS", "Gps enabled");
    	}
    	
    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		
    	}
    };
    
    
    /**
     * Network Location Listener
     */
	private final LocationListener mNetworkLocationListener = new LocationListener() {
    	@Override
    	public void onLocationChanged(Location location) {
    		if (m_locationLast == null) {
    			m_locationLast = location;
    			updateLocation(location);
    		} else {
	    		if (Utils.isBetterLocation(location, m_locationLast)) {
					Log.d("LocationFactory.java", "Location Acquired: " + location.toString());
					m_locationLast = location;
					updateLocation(location);
				}
    		}
    	}
    	
    	@Override
    	public void onProviderDisabled(String provider) {
    		
    	}
    	
    	public void onProviderEnabled(String provider) {
    		
    	}
    	
    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		
    	}
    };
    
    /**
     * Update location information on local DB.
     * @param location : current location
     */
    private void updateLocation(Location location) {
		m_dLongitude = location.getLongitude();
		m_dLatitude = location.getLatitude();
		m_bLocationChanged = true;

		PreferenceManager.setCurrentLat(String.valueOf(location.getLatitude()));
		PreferenceManager.setCurrentLng(String.valueOf(location.getLongitude()));

    	Log.e("Location-Listener", "(" + location.getLatitude() + "," + location.getLongitude() + ")");
     }



	class UploadScheduler implements Runnable {

		Context mContext;
		boolean m_bRunning;
		private long _MINUTUE_5 = 1000L * 60L * 5L;

		public UploadScheduler(Context context){
			mContext = context;
			m_bRunning = true;
		}

		@Override
		public void run() {

			while(true){
				try{
					//if (m_bGoogleLocationListenerRunning == false)
					//	enableGoogleLocationListener();

					setupLocationListeners();

					procDriverLocationUpdate();

					Thread.sleep(_MINUTUE_5);			//proc cycle = 1 min

					Log.e("Kang", "Thread = " + String.valueOf(System.currentTimeMillis()));
				}catch(Exception e){
					Log.e("error", e.toString());
				}
			}
		}
	}


	private void procDriverLocationUpdate (){
    	if (PreferenceManager.getUserId().length() == 0)	return;
    	if (PreferenceManager.getRole() != GlobalConst.ROLE_DRIVER)	return;
		if (PreferenceManager.getCurrentLat().length() == 0)		return;
		if (PreferenceManager.getCurrentLng().length() == 0)		return;

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("user_id", PreferenceManager.getUserId());
		jsonObject.addProperty("cur_lat", PreferenceManager.getCurrentLat());
		jsonObject.addProperty("cur_lng", PreferenceManager.getCurrentLng());

		Call<BaseResult> call = apiInterface.doUpdateDriverLocation(jsonObject);
		call.enqueue(new Callback<BaseResult>() {
			@Override
			public void onResponse(Call<BaseResult> call, retrofit2.Response<BaseResult> response) {

			}

			@Override
			public void onFailure(Call<BaseResult> call, Throwable t) {

			}
		});
	}
}