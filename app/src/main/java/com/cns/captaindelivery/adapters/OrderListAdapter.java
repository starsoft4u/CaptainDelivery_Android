package com.cns.captaindelivery.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.cns.captaindelivery.activities.CustomerNewOrderActivity;
import com.cns.captaindelivery.activities.CustomerShopInfoActivity;
import com.cns.captaindelivery.fragments.CustomerActiveOrderFragment;
import com.cns.captaindelivery.models.InfoHistoryPlace;
import com.cns.captaindelivery.models.InfoOrder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.MyViewHolder>{
    private List<InfoOrder> m_lstData;
    private Context context;
    private Fragment fragment;

    SimpleDateFormat fmtOut = new SimpleDateFormat("hh:mm a dd MMM yyyy");

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtStoreName,txtOrderNo, txtOrderDate;


        public MyViewHolder(View view) {
            super(view);
            txtStoreName = view.findViewById(R.id.txtStoreName);
            txtOrderNo= view.findViewById(R.id.txtOrderNo);
            txtOrderDate= view.findViewById(R.id.txtOrderDate);

        }
    }

    public OrderListAdapter(Context context, Fragment fragment, List<InfoOrder> lstData) {
        this.context=context;
        this.fragment = fragment;
        this.m_lstData = lstData;
    }

    public void setListData(List<InfoOrder> lstData){
        this.m_lstData = lstData;
    }
    @Override
    public OrderListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderListAdapter.MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(OrderListAdapter.MyViewHolder holder, int position) {
       final InfoOrder infoOrder = m_lstData.get(position);
        holder.txtStoreName.setText(infoOrder.getSrc_name());
        holder.txtOrderNo.setText("Order #"+infoOrder.getOrd_id());

        holder.txtOrderDate.setText("Ordered on "+fmtOut.format(new Date(infoOrder.getOrd_created()*1000L)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoOrder.getOrd_status() == 0){
                    //Pending status
                    Intent intent = new Intent(context, CustomerNewOrderActivity.class);
                    intent.putExtra(GlobalConst.KEY_ORDER_ID, infoOrder.getOrd_id());
                    fragment.startActivityForResult(intent, CustomerActiveOrderFragment.REQ_CHANGE_ORDER);
                }
                /*
                Intent intent = new Intent(context, CustomerShopInfoActivity.class);
                intent.putExtra(GlobalConst.KEY_PLACE_ID, infoOrder.getSrc_placeid());
                intent.putExtra(GlobalConst.KEY_PLACE_NAME, infoOrder.getSrc_name());
                ((CustomerMainActivity)context).startActivityForResult(intent, CustomerMainActivity.REQ_NEW_ORDER);
                */
            }
        });
    }

    @Override
    public int getItemCount() {
        return m_lstData.size();
    }
}
