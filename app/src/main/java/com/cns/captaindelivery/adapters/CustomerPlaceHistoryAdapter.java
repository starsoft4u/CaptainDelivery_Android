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
import com.cns.captaindelivery.models.InfoHistoryPlace;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomerPlaceHistoryAdapter extends RecyclerView.Adapter<CustomerPlaceHistoryAdapter.MyViewHolder>{
    private List<InfoHistoryPlace> m_lstData;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title,subtitle;
        public ImageView p_image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.store_title);
            subtitle= (TextView) view.findViewById(R.id.subtitle);
            p_image=(ImageView)view.findViewById(R.id.p_image);

        }
    }

    public CustomerPlaceHistoryAdapter(Context context, List<InfoHistoryPlace> lstData) {
        this.context=context;
        this.m_lstData = lstData;
    }

    public void setListData(List<InfoHistoryPlace> lstData){
        this.m_lstData = lstData;
    }
    @Override
    public CustomerPlaceHistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_search_gif, parent, false);
        return new CustomerPlaceHistoryAdapter.MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(CustomerPlaceHistoryAdapter.MyViewHolder holder, int position) {
       final InfoHistoryPlace infoHistoryPlace = m_lstData.get(position);
        holder.title.setText(infoHistoryPlace.getSrc_name());
        holder.subtitle.setText(infoHistoryPlace.getSrc_addr());
        Picasso.get().load(infoHistoryPlace.getSrc_placeicon()).error(R.drawable.no_image).into(holder.p_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerShopInfoActivity.class);
                intent.putExtra(GlobalConst.KEY_PLACE_ID, infoHistoryPlace.getSrc_placeid());
                intent.putExtra(GlobalConst.KEY_PLACE_NAME, infoHistoryPlace.getSrc_name());
                ((CustomerMainActivity)context).startActivityForResult(intent, CustomerMainActivity.REQ_NEW_ORDER);
            }
        });
    }

    @Override
    public int getItemCount() {
        return m_lstData.size();
    }
}
