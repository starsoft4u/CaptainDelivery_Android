package com.cns.captaindelivery.fragments;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.adapters.CustomerHomeSearchAdapter;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.InfoGooglePlace;
import com.cns.captaindelivery.models.ResultGooglePlaceList;
import com.cns.captaindelivery.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerHomeFragment extends Fragment {
    private List<InfoGooglePlace> m_lstData = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CustomerHomeSearchAdapter mAdapter;
    private Button button,his_btn,nearby_btn;
    private TextView mTxtNoData;

    private FusedLocationProviderClient fusedLocationClient;

    String m_strLat, m_strLng;

    ApiInterface apiInterface;
    ApiInterface googleApiInfterface;

    String m_strNextPageToken = "";
    boolean m_bFirstLoad = true;

    public static CustomerHomeFragment newInstance() {
        CustomerHomeFragment fragment = new CustomerHomeFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout._fragment_list, container, false);
        initXml();
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recylerview);
        mTxtNoData = view.findViewById(R.id.txtNoData);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        googleApiInfterface = ApiClient.getGooglePlaceClient().create(ApiInterface.class);

        m_lstData.clear();
        m_strLat = PreferenceManager.getCurrentLat();
        m_strLng = PreferenceManager.getCurrentLng();
        if (m_strLat.length()>0 && m_strLng.length()>0) {
            procGetNearbyPlaces("");
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    m_strLat = String.valueOf(location.getLatitude());
                                    m_strLng = String.valueOf(location.getLongitude());
                                } else {
                                    Toast.makeText(getActivity(), R.string.msg_cannot_get_location, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(getActivity(), R.string.msg_err_permission, Toast.LENGTH_SHORT).show();
            }

        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.imagesearch).setVisibility(View.GONE);
        getActivity().findViewById(R.id.layoutTabbarHome).setVisibility(View.GONE);
        getActivity().findViewById(R.id.btnTabAll).setSelected(false);
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(R.string.title_home);
        getActivity().findViewById(R.id.layoutTabbarHome).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.imagesearch).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.btnTabAll).setSelected(true);
    }


    private void procGetNearbyPlaces (String pageToken){
        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        StringBuilder urlString = new StringBuilder("api/place/nearbysearch/json?");

        urlString.append("&location=");
        urlString.append(m_strLat);
        urlString.append(",");
        urlString.append(m_strLng);
        urlString.append("&radius=10000");
        //urlString.append("&types=restaurant");
        urlString.append("&sensor=false&key=" + getString(R.string.google_places_key));
        if (pageToken!=null && pageToken.length()>0) {
            urlString.append("&pagetoken=");
            urlString.append(pageToken);
        }

        Call<ResultGooglePlaceList> call = googleApiInfterface.doGooglePlaceSearch(urlString.toString());
        call.enqueue(new Callback<ResultGooglePlaceList>() {
            @Override
            public void onResponse(Call<ResultGooglePlaceList> call, retrofit2.Response<ResultGooglePlaceList> response) {
                m_dlgWait.dismiss();

                ResultGooglePlaceList resultGooglePlaceList = response.body();

                boolean bCheckType;
                if (resultGooglePlaceList.getStatus().equals(GlobalConst.OK)) {
                    m_strNextPageToken = resultGooglePlaceList.getNext_page_token();
                    if (resultGooglePlaceList.getResults() != null) {
                        for (InfoGooglePlace infoGooglePlace : resultGooglePlaceList.getResults()) {
                            if (infoGooglePlace.getTypes() == null) {
                                m_lstData.add(infoGooglePlace);
                            } else {
                                bCheckType = false;
                                for (String strType : infoGooglePlace.getTypes()){
                                    if (GlobalConst.ALLOWED_PLACE_TYPES.contains(strType)) {
                                        bCheckType = true;
                                        break;
                                    }
                                }
                                if (bCheckType)
                                    m_lstData.add(infoGooglePlace);
                            }
                        }
                    }
                } else {
                    m_strNextPageToken = "";
                }

                if (m_lstData.size()>0) {
                    if (m_bFirstLoad) {
                        mAdapter = new CustomerHomeSearchAdapter(getActivity(), m_lstData);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                        mRecyclerView.setAdapter(mAdapter);

                        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);

                                if (!recyclerView.canScrollVertically(1)) {
                                    Log.e("knag", "Scroll Endless");
                                    if (m_strNextPageToken != null && m_strNextPageToken.length() > 0) {
                                        procGetNearbyPlaces(m_strNextPageToken);
                                    }
                                }
                            }
                        });
                        m_bFirstLoad = false;
                    } else {
                        mAdapter.setListData(m_lstData);
                        mAdapter.notifyDataSetChanged();
                    }

                    mRecyclerView.setVisibility(View.VISIBLE);
                    mTxtNoData.setVisibility(View.GONE);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mTxtNoData.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<ResultGooglePlaceList> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }



/*
    private void procGetAllPackages (){
        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultPackageList> call = apiInterface.doAllPackages();
        call.enqueue(new Callback<ResultPackageList>() {
            @Override
            public void onResponse(Call<ResultPackageList> call, retrofit2.Response<ResultPackageList> response) {
                m_dlgWait.dismiss();

                ResultPackageList resultPackageList = response.body();

                if (resultPackageList.getStatus() == 1) {
                    m_lstData = resultPackageList.getData();
                    if (m_lstData.size() > 0) {
                        mAdapter = new CustomerHomeSearchAdapter(getActivity(), m_lstData);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                        mRecyclerView.setAdapter(mAdapter);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mTxtNoData.setVisibility(View.GONE);
                        return;
                    }
                }
                mRecyclerView.setVisibility(View.GONE);
                mTxtNoData.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFailure(Call<ResultPackageList> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
*/
}
