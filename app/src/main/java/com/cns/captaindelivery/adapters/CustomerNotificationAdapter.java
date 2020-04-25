package com.cns.captaindelivery.adapters;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cns.captaindelivery.R;
import com.cns.captaindelivery.activities.CustomerMainActivity;
import com.cns.captaindelivery.fragments.CustomerDriverProfileFragment;
import com.cns.captaindelivery.models.InfoNotification;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerNotificationAdapter extends RecyclerView.Adapter<CustomerNotificationAdapter.MyViewHolder> {
    private List<InfoNotification> moviesList;
    private Context context;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView noti_time;
        ImageView driver_image;
        ImageView imgUnread;

        public MyViewHolder(View view) {
            super(view);
            description=(TextView)view.findViewById(R.id.description);
            noti_time=(TextView)view.findViewById(R.id.day);
            driver_image=(ImageView)view.findViewById(R.id.d_image);
            imgUnread=view.findViewById(R.id.imgUnread);
        }
    }

    public CustomerNotificationAdapter(Context context, List<InfoNotification> moviesList) {
        this.context=context;
        this.moviesList = moviesList;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cutomer_notification, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
       final InfoNotification infoNotification = moviesList.get(position);
        viewHolder.description.setText(infoNotification.getNoti_content());
        viewHolder.noti_time.setText(simpleDateFormat.format(infoNotification.getNoti_time()*1000L));
        Picasso.get().load(infoNotification.getImage()).placeholder(R.drawable.user).into(viewHolder.driver_image);
        if (infoNotification.getNoti_opened() == 1)
            viewHolder.imgUnread.setVisibility(View.GONE);
        else
            viewHolder.imgUnread.setVisibility(View.VISIBLE);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,"click listener",Toast.LENGTH_SHORT).show();
                /*
                String d_image=infoNotification.getDriver_image();
                String phone=infoNotification.getPhone();
                String d_name=infoNotification.getDriver_name();
                String d_rate=infoNotification.getRate();
                String d_token=infoNotification.getToken();
                String d_id=infoNotification.getDriver_id();
                String d_noti_id=infoNotification.getDriver_noti_id();
                String package_id=infoNotification.getPackage_id();

                fragmentJump(d_id,d_image,d_name,phone,d_rate,d_token,d_noti_id,package_id);
                */
            }
        });
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
    public void fragmentJump(String d_id,String d_image,String d_name,String phone,String d_rate,String token,String d_noti_id,String package_id){
        Fragment mFragment = new CustomerDriverProfileFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString("selected_d_noti_id", d_noti_id);
        mBundle.putString("selected_d_id", d_id);
        mBundle.putString("selected_d_image",d_image);
        mBundle.putString("selected_d_name",d_name);
        mBundle.putString("selected_d_rate",d_rate);
        mBundle.putString("selected_d_phone",phone);
        mBundle.putString("selected_d_token",token);
        mBundle.putString("selected_d_packge_id",package_id);
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

}
