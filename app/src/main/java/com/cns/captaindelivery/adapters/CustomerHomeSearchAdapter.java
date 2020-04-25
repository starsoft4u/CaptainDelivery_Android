package com.cns.captaindelivery.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.activities.CustomerMainActivity;
import com.cns.captaindelivery.activities.CustomerShopInfoActivity;
import com.cns.captaindelivery.fragments.CustomerOrderFragment;
import com.cns.captaindelivery.models.InfoGooglePlace;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomerHomeSearchAdapter extends RecyclerView.Adapter<CustomerHomeSearchAdapter.MyViewHolder>{
    private List<InfoGooglePlace> m_lstData;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title,subtitle,distance;
        public ImageView p_image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.store_title);
            subtitle= (TextView) view.findViewById(R.id.subtitle);
            p_image=(ImageView)view.findViewById(R.id.p_image);

        }
    }

    public CustomerHomeSearchAdapter(Context context, List<InfoGooglePlace> lstData) {
        this.context=context;
        this.m_lstData = lstData;
    }

    public void setListData(List<InfoGooglePlace> lstData){
        this.m_lstData = lstData;
    }
    @Override
    public CustomerHomeSearchAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_search_gif, parent, false);
        return new CustomerHomeSearchAdapter.MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(CustomerHomeSearchAdapter.MyViewHolder holder, int position) {
       final InfoGooglePlace infoGooglePlace = m_lstData.get(position);
        holder.title.setText(infoGooglePlace.getName());
        String strSubInfo = infoGooglePlace.getVicinity();
        if (strSubInfo==null)   strSubInfo = infoGooglePlace.getFormatted_address();

        holder.subtitle.setText(strSubInfo);
        Picasso.get().load(infoGooglePlace.getIcon()).error(R.drawable.no_image).into(holder.p_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerShopInfoActivity.class);
                intent.putExtra(GlobalConst.KEY_PLACE_ID, infoGooglePlace.getPlace_id());
                intent.putExtra(GlobalConst.KEY_PLACE_NAME, infoGooglePlace.getName());
                ((CustomerMainActivity)context).startActivityForResult(intent, CustomerMainActivity.REQ_NEW_ORDER);
                /*
                //Toast.makeText(context,"click listener",Toast.LENGTH_SHORT).show();
                String p_id=infoGooglePlace.getP_id();
                String p_image=infoGooglePlace.getP_image();
                String p_name=infoGooglePlace.getP_name();
                String p_location=infoGooglePlace.getP_location();
                fragmentJump(p_id,p_image,p_name,p_location);
                */
            }
        });
    }
    public void fragmentJump(String p_id,String p_image,String p_name,String p_location){
//        CustomerOrderFragment orderFragment=CustomerOrderFragment.newInstance();
//        switchContent(R.id.cus_content,orderFragment);
        Fragment mFragment = new CustomerOrderFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString("selected_package_id", p_id);
        mBundle.putString("selected_package_image",p_image);
        mBundle.putString("selected_package_name",p_name);
        mBundle.putString("selected_package_location",p_location);
        mFragment.setArguments(mBundle);
        switchContent(R.id.cus_content, mFragment);

    }
    public void switchContent(int id, Fragment fragment) {
        if (context == null)
            return;
        if (context instanceof CustomerMainActivity) {
            CustomerMainActivity mainActivity = (CustomerMainActivity) context;
            Fragment frag = fragment;
            mainActivity.switchContent(id, frag);
        }
    }

    @Override
    public int getItemCount() {
        return m_lstData.size();
    }
}
