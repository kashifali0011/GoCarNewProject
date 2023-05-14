package com.applandeo.materialcalendarview.utils

import android.graphics.Typeface
import android.view.View
import com.applandeo.materialcalendarview.R
import kotlinx.android.synthetic.main.calendar_view.view.*


/**
 * Created by Applandeo Team.
 */

internal fun View.setAbbreviationsLabels(color: Int, firstDayOfWeek: Int) {
    val labels = getAbbreviationsTextViews()

    val abbreviations = context.resources.getStringArray(R.array.material_calendar_day_abbreviations_array)

    labels.forEachIndexed { index, label ->
        label.text = abbreviations[(index + firstDayOfWeek - 1) % 7]

        if (color != 0) {
            label.setTextColor(color)
        }
    }
}

internal fun View.setAbbreviationsLabelsSize(size: Float) {
    val labels = getAbbreviationsTextViews()
    val maxTextSize = resources.getDimensionPixelSize(R.dimen.text_size_max)
    labels.forEachIndexed { _, label ->
        if (size > 0.0 && size <= maxTextSize) {
            label.textSize = size
        }
    }
}

private fun View.getAbbreviationsTextViews() = listOf(
        mondayLabel,
        tuesdayLabel,
        wednesdayLabel,
        thursdayLabel,
        fridayLabel,
        saturdayLabel,
        sundayLabel)

internal fun View.setTypeface(typeface: Typeface?) {
    if (typeface == null) return
    getAbbreviationsTextViews().forEach { label ->
        label.typeface = typeface
    }
}


internal fun View.setAbbreviationsBarColor(color: Int) {
    if (color == 0) return
    this.abbreviationsBar.setBackgroundColor(color)
}

internal fun View.setPagesColor(color: Int) {
    if (color == 0) return
    this.calendarViewPager.setBackgroundColor(color)
}

internal fun View.setAbbreviationsBarVisibility(visibility: Int) {
    this.abbreviationsBar.visibility = visibility
}
