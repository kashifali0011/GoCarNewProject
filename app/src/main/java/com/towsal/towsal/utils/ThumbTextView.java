package com.towsal.towsal.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatTextView;

/**
* Custom text view class
* */
public class ThumbTextView extends AppCompatTextView {

    private final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private int width = 0;

    public ThumbTextView(Context context) {
        super(context);
    }

    public ThumbTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Attaching text view with seekbar
     */
    public void attachToSeekBar(SeekBar seekBar) {
        String content = getText().toString();
        if (TextUtils.isEmpty(content) || seekBar == null)
            return;
        float contentWidth = this.getPaint().measureText(content);
        int realWidth = width - seekBar.getPaddingLeft() - seekBar.getPaddingRight();
        int maxLimit = (int) (width - contentWidth - seekBar.getPaddingRight());
        int minLimit = seekBar.getPaddingLeft();
        float percent = (float) (1.0 * seekBar.getProgress() / seekBar.getMax());
        int left = minLimit + (int) (realWidth * percent - contentWidth / 2.0);
        left = left <= minLimit ? minLimit : Math.min(left, maxLimit);
        lp.setMargins(left, 0, 0, 0);
        if (seekBar.getProgress() >= 20) {
            lp.setMargins(left - minLimit, 0, 0, 0);
        } else {
            lp.setMargins(left, 0, 0, 0);
        }
        setLayoutParams(lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (width == 0)
            width = MeasureSpec.getSize(widthMeasureSpec);
    }
}
