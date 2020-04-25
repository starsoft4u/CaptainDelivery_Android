package com.cns.captaindelivery.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.adapters.CustomerPlaceHistoryAdapter;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.InfoHistoryPlace;
import com.cns.captaindelivery.models.ResultPlaceHistory;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerHistoryFragment extends Fragment {
    private List<InfoHistoryPlace> m_lstData;
    private RecyclerView mRecyclerView;
    private CustomerPlaceHistoryAdapter mAdapter;
    private Button button,his_btn,nearby_btn;
    private TextView mTxtNoData;

    ApiInterface apiInterface;

    public static CustomerHistoryFragment newInstance() {
        CustomerHistoryFragment fragment = new CustomerHistoryFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout._fragment_list, container, false);
        initXml();
        mRecyclerView = view.findViewById(R.id.recylerview);
        mTxtNoData = view.findViewById(R.id.txtNoData);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        procGetHistoryPackages();
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.imagesearch).setVisibility(View.GONE);
        getActivity().findViewById(R.id.layoutTabbarHome).setVisibility(View.GONE);
        getActivity().findViewById(R.id.btnTabHistory).setSelected(false);
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(R.string.title_home);
        getActivity().findViewById(R.id.layoutTabbarHome).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.imagesearch).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.btnTabHistory).setSelected(true);
    }


    private void procGetHistoryPackages(){
        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("customer_id", PreferenceManager.getCustomerId());
        Call<ResultPlaceHistory> call = apiInterface.doGetPlaceHistory(jsonObject);
        call.enqueue(new Callback<ResultPlaceHistory>() {
            @Override
            public void onResponse(Call<ResultPlaceHistory> call, retrofit2.Response<ResultPlaceHistory> response) {
                m_dlgWait.dismiss();

                ResultPlaceHistory resultPackageList = response.body();

                if (resultPackageList.getStatus() == 1) {
                    m_lstData = resultPackageList.getData();
                    if (m_lstData.size() > 0) {
                        mAdapter = new CustomerPlaceHistoryAdapter(getActivity(), m_lstData);
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
            public void onFailure(Call<ResultPlaceHistory> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });


    }



}
