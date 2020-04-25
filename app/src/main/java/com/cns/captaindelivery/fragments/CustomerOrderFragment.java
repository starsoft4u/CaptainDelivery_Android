package com.cns.captaindelivery.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cns.captaindelivery.R;

public class CustomerOrderFragment extends Fragment {
    public static CustomerOrderFragment newInstance() {
        CustomerOrderFragment fragment = new CustomerOrderFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout._fragment_list, container, false);
        initXml();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.layoutTabbarOrder).setVisibility(View.GONE);
    }

    public void initXml(){
        ((TextView)getActivity().findViewById(R.id.tvTitle)).setText(R.string.title_order);
        getActivity().findViewById(R.id.layoutTabbarOrder).setVisibility(View.VISIBLE);
    }
}
