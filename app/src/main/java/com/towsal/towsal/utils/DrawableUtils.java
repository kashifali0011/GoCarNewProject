package com.towsal.towsal.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.applandeo.materialcalendarview.CalendarUtils;
import com.applandeo.materialcalendarview.utils.DayColorsUtils;
import com.towsal.towsal.R;

/**
 * Class for changing drawable properties
 * */
public final class DrawableUtils {

    public static Drawable getCircleDrawableWithText(Context context, String string) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.sample_circle);
        Drawable text = CalendarUtils.getDrawableText(context, string, null, android.R.color.white, 12);

        Drawable[] layers = {background, text};
        return new LayerDrawable(layers);
    }

    public static Drawable getDayCircle(Context context, @ColorRes int borderColor, @ColorRes int fillColor) {
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.calendar_day_background);
        if (drawable != null) {
            drawable.setStroke(15, DayColorsUtils.parseColor(context, borderColor));
            drawable.setColor(DayColorsUtils.parseColor(context, fillColor));
        }
        return drawable;
    }

    private DrawableUtils() {
    }
}