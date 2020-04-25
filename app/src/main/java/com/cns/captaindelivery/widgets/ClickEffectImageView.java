package com.cns.captaindelivery.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ClickEffectImageView extends ImageView {

    boolean m_bOutOf = false;

    private Rect rect;    // Variable rect to hold the bounds of the view

    public ClickEffectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

/*		setClickable(true);
		setEnabled(true);
		setFocusable(true);
		setFocusableInTouchMode(true);*/
    }

    /* (non-Javadoc)
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //overlay is black with transparency of 0x77 (119)
                this.getDrawable().setColorFilter(0x50000000,android.graphics.PorterDuff.Mode.SRC_ATOP);
                this.invalidate();
                rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
                m_bOutOf = false;
                //Log.e("test", "Down");
                return true;

            case MotionEvent.ACTION_CANCEL:
                //clear the overlay
                //Log.e("test", "CANCEL");
                this.getDrawable().clearColorFilter();
                this.invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                //clear the overlay
                if (m_bOutOf)  	return false;

                if(!rect.contains(getLeft() + (int) event.getX(), getTop() + (int) event.getY())){
                    this.getDrawable().clearColorFilter();
                    this.invalidate();
                    m_bOutOf = true;
                    //Log.e("test", "OUTSIDE");
                } else {
                    //Log.e("test", "INSIDE");
                }
                return true;
            case MotionEvent.ACTION_UP:
                //clear the overlay
                //Log.e("test", "Up");
                this.getDrawable().clearColorFilter();
                this.invalidate();
                if (m_bOutOf == false)
                    performClick();
                return true;
        }

        return false;
    }
}