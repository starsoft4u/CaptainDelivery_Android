package com.cns.captaindelivery.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.adapters.OrderListAdapter;
import com.cns.captaindelivery.api.ApiClient;
import com.cns.captaindelivery.api.ApiInterface;
import com.cns.captaindelivery.models.InfoOrder;
import com.cns.captaindelivery.models.ResultOrderList;
import com.cns.captaindelivery.PreferenceManager;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerInactiveOrderFragment extends Fragment {
    private List<InfoOrder> m_lstData;
    private RecyclerView mRecyclerView;
    private OrderListAdapter mAdapter;

    private TextView mTxtNoData;

    ApiInterface apiInterface;
    public static CustomerInactiveOrderFragment newInstance() {
        CustomerInactiveOrderFragment fragment = new CustomerInactiveOrderFragment();
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
        procGetListData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.layoutTabbarOrder).setVisibility(View.GONE);
        getActivity().findViewById(R.id.btnTabInactiveOrders).setSelected(false);
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(R.string.title_order);
        getActivity().findViewById(R.id.layoutTabbarOrder).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.btnTabInactiveOrders).setSelected(true);
    }

    private void procGetListData(){
        final ProgressDialog m_dlgWait = ProgressDialog.show(getActivity(), null, null);
        m_dlgWait.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_dlgWait.setContentView(R.layout.dlg_loader);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("customer_id", PreferenceManager.getCustomerId());
        Call<ResultOrderList> call = apiInterface.doGetCustomerInactiveOrders(jsonObject);
        call.enqueue(new Callback<ResultOrderList>() {
            @Override
            public void onResponse(Call<ResultOrderList> call, retrofit2.Response<ResultOrderList> response) {
                m_dlgWait.dismiss();

                ResultOrderList resultOrderList = response.body();

                if (resultOrderList.getStatus() == 1) {
                    m_lstData = resultOrderList.getData();
                    if (m_lstData.size() > 0) {
                        mAdapter = new OrderListAdapter(getActivity(), CustomerInactiveOrderFragment.this, m_lstData);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
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
            public void onFailure(Call<ResultOrderList> call, Throwable t) {
                m_dlgWait.dismiss();
                Log.e("kang", t.getMessage());
                Toast.makeText(getActivity(), R.string.msg_err_request, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
