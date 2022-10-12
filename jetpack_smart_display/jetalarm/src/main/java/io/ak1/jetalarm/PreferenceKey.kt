package io.ak1.jetalarm

enum class PreferenceKey(val key: String, val title: Int) {
    BORDER_RADIUS_JET("borderRadiusJet", R.string.border_radius),
    BORDER_THICKNESS_JET("borderThicknessJet", R.string.border_thickness),
    PRIMARY_COLOR("primaryColor", R.string.primary_color),
    SECONDARY_COLOR("secondaryColor", R.string.secondary_color),
    HAND_LENGTH_SECONDS_JET("handLengthSecondsJet", R.string.hand_length_seconds),
    HAND_LENGTH_MINUTES_JET("handLengthMinutesJet", R.string.hand_length_minutes),
    HAND_LENGTH_HOURS_JET("handLengthHoursJet", R.string.hand_length_hours),
    HAND_WIDTH_SECONDS_JET("handWidthSecondsJet", R.string.hand_width_seconds),
    HAND_WIDTH_MINUTES_JET("handWidthMinutesJet", R.string.hand_width_minutes),
    HAND_WIDTH_HOURS_JET("handWidthHoursJet", R.string.hand_width_hours),
    HAND_SHOW_SECOND_HAND_JET("handShowSecondHandJet", R.string.hand_show_second_hand);
}
