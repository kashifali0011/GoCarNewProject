package com.towsal.towsal.views.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * Custom scroll view
 * */
public class ScrollViewForNesting extends ScrollView {
    private final int DIRECTION_VERTICAL = 0;
    private final int DIRECTION_HORIZONTAL = 1;
    private final int DIRECTION_NO_VALUE = -1;

    private final int mTouchSlop;
    private int mGestureDirection;

    private float mDistanceX;
    private float mDistanceY;
    private float mLastX;
    private float mLastY;

    public ScrollViewForNesting(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public ScrollViewForNesting(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScrollViewForNesting(Context context) {
        this(context,null);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDistanceY = mDistanceX = 0f;
                mLastX = ev.getX();
                mLastY = ev.getY();
                mGestureDirection = DIRECTION_NO_VALUE;
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                mDistanceX += Math.abs(curX - mLastX);
                mDistanceY += Math.abs(curY - mLastY);
                mLastX = curX;
                mLastY = curY;
                break;
        }

        return super.onInterceptTouchEvent(ev) && shouldIntercept();
    }


    private boolean shouldIntercept(){
        if((mDistanceY > mTouchSlop || mDistanceX > mTouchSlop) && mGestureDirection == DIRECTION_NO_VALUE){
            if(Math.abs(mDistanceY) > Math.abs(mDistanceX)){
                mGestureDirection = DIRECTION_VERTICAL;
            }
            else{
                mGestureDirection = DIRECTION_HORIZONTAL;
            }
        }

        return mGestureDirection == DIRECTION_VERTICAL;
    }
}