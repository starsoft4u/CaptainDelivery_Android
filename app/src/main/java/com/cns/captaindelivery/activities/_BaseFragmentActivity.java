package com.cns.captaindelivery.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.cns.captaindelivery.R;

import java.util.Calendar;

public class _BaseFragmentActivity extends FragmentActivity {

    private static final int MSG_TIMER_EXPIRED = 1;
    private static final int BACKEY_TIMEOUT = 2000;
    private boolean mIsBackKeyPressed = false;
    private long mCurrentTimeInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBackPressed() {

        if (isTaskRoot()) {
            procFinalFinish();
        } else {
            finish();
        }

    }

    private void procFinalFinish(){
        if(mIsBackKeyPressed == false){
            mIsBackKeyPressed = true;

            mCurrentTimeInMillis = Calendar.getInstance().getTimeInMillis();

            Toast.makeText(this, R.string.msg_exit, Toast.LENGTH_SHORT).show();
            mExitTimerHander.sendEmptyMessageDelayed(MSG_TIMER_EXPIRED, BACKEY_TIMEOUT);
        } else {
            mIsBackKeyPressed = false;

            if(Calendar.getInstance().getTimeInMillis() <= (mCurrentTimeInMillis + (BACKEY_TIMEOUT))){
                finish();
            }
        }
    }

    private Handler mExitTimerHander = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_TIMER_EXPIRED:
                {
                    mIsBackKeyPressed = false;
                }
                break;
            }
        }
    };
}
