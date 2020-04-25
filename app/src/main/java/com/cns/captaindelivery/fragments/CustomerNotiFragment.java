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
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.adapters.CustomerNotificationAdapter;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.InfoNotification;
import com.cns.captaindelivery.models.ResultNotification;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerNotiFragment extends Fragment {
    private List<InfoNotification> m_lstData;
    private CustomerNotificationAdapter mAdapter;

    private RecyclerView mRecyleView;
    private TextView mTxtNoData;

    ApiInterface apiInterface;


    public static CustomerNotiFragment newInstance() {
        CustomerNotiFragment fragment = new CustomerNotiFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout._fragment_list, container, false);
        initXml();

        mRecyleView =view.findViewById(R.id.recylerview);
        mTxtNoData = view.findViewById(R.id.txtNoData);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        procGetNofications();

        return view;
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(R.string.notifications);
    }

    @Override
    public void onResume() {
        super.onResume();
//        mAdapter = new CustomerNotiAdapter (getListData());
//        mRecyleView.setAdapter(mAdapter);
    }

    private void procGetNofications(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", PreferenceManager.getUserId());
        jsonObject.addProperty("is_driver", PreferenceManager.getRole()== GlobalConst.ROLE_DRIVER?1:0);

        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        Call<ResultNotification> call = apiInterface.doGetNotifications(jsonObject);
        call.enqueue(new Callback<ResultNotification>() {
            @Override
            public void onResponse(Call<ResultNotification> call, retrofit2.Response<ResultNotification> response) {
                m_dlgWait.dismiss();

                ResultNotification resultNotification = response.body();

                if (resultNotification.getStatus() == 1) {
                    m_lstData = resultNotification.getData();
                    if (m_lstData.size() > 0) {
                        mAdapter = new CustomerNotificationAdapter(getActivity(), m_lstData);
                        mRecyleView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mRecyleView.setItemAnimator(new DefaultItemAnimator());
                        mRecyleView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                        mRecyleView.setAdapter(mAdapter);
                        mRecyleView.setVisibility(View.VISIBLE);
                        mTxtNoData.setVisibility(View.GONE);
                        return;
                    }
                }
                mRecyleView.setVisibility(View.GONE);
                mTxtNoData.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFailure(Call<ResultNotification> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }





}
