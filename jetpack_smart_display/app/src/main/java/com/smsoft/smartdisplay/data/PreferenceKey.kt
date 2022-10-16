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
    HAND_SHOW_SECOND_HAND_JET("handShowSecondHandJet", R.string.hand_show_second_hand),
    DIGIT_FONT_CLOCKVIEW("digitFontClockView", R.string.digit_font),
    DIGIT_STYLE_CLOCKVIEW("digitStyleClockView", R.string.digit_style),
    DIGIT_SHOW_HOURS_CLOCKVIEW("digitShowHoursClockView", R.string.digit_show_hours),
    DIGIT_SHOW_MINUTES_CLOCKVIEW("digitShowMinutesClockView", R.string.digit_show_minutes),
    DIGIT_SHOW_DEGREES_CLOCKVIEW("digitShowDegreesClockView", R.string.digit_show_degrees),
    DIGIT_DISPOSITION_CLOCKVIEW("digitDispositionClockView", R.string.digit_disposition),
    DIGIT_STEP_CLOCKVIEW("digitStepClockView", R.string.digit_step),
    DEGREE_TYPE_CLOCKVIEW("degreeTypeClockView", R.string.degree_type),
    DEGREE_STEP_CLOCKVIEW("degreeStepClockView", R.string.degree_step),
    SHOW_CENTER_CLOCKVIEW("showCenterClockView", R.string.show_center),
    SHOW_SECOND_HAND_CLOCKVIEW("showSecondHandClockView", R.string.hand_show_second_hand),
    DIGIT_FONT_CLOCKVIEW2("digitFontClockView2", R.string.digit_font),
    DIGIT_STYLE_CLOCKVIEW2("digitStyleClockView2", R.string.digit_style),
    OUTER_RIM_WIDTH_CLOCKVIEW2("outerRimWidthClockView2", R.string.outer_rim_width),
    INNER_RIM_WIDTH_CLOCKVIEW2("innerRimWidthClockView2", R.string.inner_rim_width),
    THICK_MARKER_WIDTH_CLOCKVIEW2("thickMarkerWidthClockView2", R.string.thick_marker_width),
    THIN_MARKER_WIDTH_CLOCKVIEW2("thinMarkerWidthClockView2", R.string.thin_marker_width),
    DIGIT_TEXT_SIZE_CLOCKVIEW2("digitTextSizeClockView2", R.string.digit_text_size),
    HOUR_HAND_WIDTH_CLOCKVIEW2("hourHandWidthClockView2", R.string.hour_hand_width),
    MINUTE_HAND_WIDTH_CLOCKVIEW2("minuteHandWidthClockView2", R.string.minute_hand_width),
    SECOND_HAND_WIDTH_CLOCKVIEW2("secondHandWidthClockView2", R.string.second_hand_width),
    CENTER_CIRCLE_RADIUS_CLOCKVIEW2("centerCircleRadiusClockView2", R.string.center_circle_radius)
    ;
}
