package com.towsal.towsal.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.towsal.towsal.R;

/**
 * Custom seek bar thumb with top text seek bar
 */
public class ThumbTextSeekBar extends LinearLayout {

    //    public ThumbTextView tvThumb;
    public View thumbView;
    public SeekBar seekBar;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;

    public ThumbTextSeekBar(Context context) {
        super(context);
        init();
    }

    public ThumbTextSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Init block fow this class in which we initialize all fileds and views
     */
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_thumb_text_seekbar, this);
        thumbView = LayoutInflater.from(getContext()).inflate(R.layout.layout_seekbar_thumb, null, false);
        setOrientation(LinearLayout.VERTICAL);
//        tvThumb = findViewById(R.id.tvThumb);
        seekBar = findViewById(R.id.sbProgress);

        seekBar.setThumb(getThumb());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null)
                    onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null)
                    onSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (onSeekBarChangeListener != null)
                    onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
//                tvThumb.attachToSeekBar(seekBar);

            }
        });

    }

    /**
     * Function for attaching seek bar change listener
     */
    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener l) {
        this.onSeekBarChangeListener = l;
    }

    public Drawable getThumb() {
        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        thumbView.layout(0, 0, thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight());
        thumbView.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

}
