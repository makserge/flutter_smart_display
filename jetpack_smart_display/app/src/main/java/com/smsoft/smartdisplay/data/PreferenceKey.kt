package com.smsoft.smartdisplay.data

import com.smsoft.smartdisplay.R

enum class PreferenceKey(val key: String, val title: Int) {
    PRIMARY_COLOR("primaryColor", R.string.primary_color),
    SECONDARY_COLOR("secondaryColor", R.string.secondary_color),
    CLOCK_TYPE("clockType", R.string.clock_type),
    DIGIT_STYLE("digitStyle", R.string.digit_style),
    DIGIT_POSITION("digitPosition", R.string.digit_position),
    DIGIT_EMP_QUARTERS("digitEmpQuarters", R.string.digit_emp_quarters),
    DIGIT_FONT("digitFont", R.string.digit_font),
    DIGIT_FONT_SIZE("digitFontSize", R.string.digit_font_size),
    HAND_STYLE("handStyle", R.string.hand_style),
    HAND_LENGTH_MINUTES("handLengthMinutes", R.string.hand_length_minutes),
    HAND_LENGTH_HOURS("handLengthHours", R.string.hand_length_hours),
    HAND_WIDTH_MINUTES("handWidthMinutes", R.string.hand_width_minutes),
    HAND_WIDTH_HOURS("handWidthHours", R.string.hand_width_hours),
    HAND_SHOW_SECOND_HAND("handShowSecondHand", R.string.hand_show_second_hand),
    TICK_STYLE_MINUTES("tickStyleMinutes", R.string.tick_style_minutes),
    TICK_START_MINUTES("tickStartMinutes", R.string.tick_start_minutes),
    TICK_LEN_MINUTES("tickLengthMinutes", R.string.tick_length_minutes),
    TICK_STYLE_HOURS("tickStyleHours", R.string.tick_style_hours),
    TICK_START_HOURS("tickStartHours", R.string.tick_start_hours),
    TICK_LEN_HOURS("tickLengthHours", R.string.tick_length_hours),
    INNER_CIRCLE_RADIUS("innerCircleRadius", R.string.inner_circle_radius),
    DIGIT_FONT_RECT("digitFontRect", R.string.digit_font),
    DIGIT_FONT_SIZE_RECT("digitFontSizeRect", R.string.digit_font_size),
    HAND_LENGTH_MINUTES_RECT("handLengthMinutesRect", R.string.hand_length_minutes),
    HAND_LENGTH_HOURS_RECT("handLengthHoursRect", R.string.hand_length_hours),
    HAND_WIDTH_MINUTES_RECT("handWidthMinutesRect", R.string.hand_width_minutes),
    HAND_WIDTH_HOURS_RECT("handWidthHoursRect", R.string.hand_width_hours),
    BORDER_RADIUS_JET("borderRadiusJet", R.string.border_radius),
    BORDER_THICKNESS_JET("borderThicknessJet", R.string.border_thickness),
    HAND_LENGTH_SECONDS_JET("handLengthSecondsJet", R.string.hand_length_seconds),
    HAND_LENGTH_MINUTES_JET("handLengthMinutesJet", R.string.hand_length_minutes),
    HAND_LENGTH_HOURS_JET("handLengthHoursJet", R.string.hand_length_hours),
    HAND_WIDTH_SECONDS_JET("handWidthSecondsJet", R.string.hand_width_seconds),
    HAND_WIDTH_MINUTES_JET("handWidthMinutesJet", R.string.hand_width_minutes),
    HAND_WIDTH_HOURS_JET("handWidthHoursJet", R.string.hand_width_hours),
    HAND_SHOW_SECOND_HAND_JET("handShowSecondHandJet", R.string.hand_show_second_hand);
}
