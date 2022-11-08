package com.smsoft.smartdisplay.ui.screen.clock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.clock.clockview.*
import com.smsoft.smartdisplay.ui.composable.clock.clockview2.*
import com.smsoft.smartdisplay.ui.composable.clock.clockview2.DigitStyle
import com.smsoft.smartdisplay.ui.composable.clock.clockview2.Font
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock.*
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock2.DEFAULT_ANIMATION_DURATION_DC2
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock2.DEFAULT_FONT_SIZE_DC2
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock2.DEFAULT_SHADOW_RADIUS_DC2
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock2.DEFAULT_SHOW_SECONDS_DC2
import com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock.*
import com.smsoft.smartdisplay.ui.composable.clock.jetalarm.*
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.DEFAULT_PADDING_FC
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.DEFAULT_REVERSE_ROTATION_FC
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.DEFAULT_TEXT_SIZE_FC
import com.smsoft.smartdisplay.ui.composable.clock.rectangular.*
import com.smsoft.smartdisplay.utils.getParamFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class ClockViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val TIMER_INTERVAL = 100L //100ms

    private val uiStatePrivate = MutableStateFlow(getTime())
    val uiState = uiStatePrivate.asStateFlow()

    private var isTimerStarted = true
    val dataStore = userPreferencesRepository.dataStore

    class UserPreferencesRepository @Inject constructor(
        val dataStore: DataStore<Preferences>
    )

    val clockType = getParamFlow(
        dataStore = dataStore,
        defaultValue = ClockType.getDefault().id
    ) { preferences -> ClockType.getById(preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)] ?: ClockType.getDefault().id) }

    val primaryColor = getParamFlow(
        dataStore = dataStore,
        defaultValue = null
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PRIMARY_COLOR.key)] }

    val secondaryColor = getParamFlow(
        dataStore = dataStore,
        defaultValue = null
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.SECONDARY_COLOR.key)] }

    val isShowSecondsDC2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECONDS_DC2
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_SECONDS_DC2.key)] }

    val fontSizeDC2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_FONT_SIZE_DC2
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.FONT_SIZE_DC2.key)] }

    val shadowRadiusDC2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHADOW_RADIUS_DC2
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.SHADOW_RADIUS_DC2.key)] }

    val animationDurationDC2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_ANIMATION_DURATION_DC2
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.ANIMATION_DURATION_DC2.key)] }

    val isShowSecondsDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECONDS_DC
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_SECONDS_DC.key)] }

    val isShowDateDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_DATE_DC
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_DATE_DC.key)] }

    val spaceHeightDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SPACE_HEIGHT_DC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.SPACE_HEIGHT_DC.key)] }

    val timeFontResDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DigitFont.getDefault().font
    ) { preferences -> DigitFont.getById((preferences[stringPreferencesKey(PreferenceKey.TIME_FONT_DC.key)] ?: DigitFont.getDefaultId())).font }

    val timeFontSizeDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_TIME_FONT_SIZE_DC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TIME_FONT_SIZE_DC.key)] }

    val dateFontResDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DigitFont.getDefault().font
    ) { preferences -> DigitFont.getById((preferences[stringPreferencesKey(PreferenceKey.DATE_FONT_DC.key)] ?: DigitFont.getDefaultId())).font }

    val dateFontSizeDC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DATE_FONT_SIZE_DC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DATE_FONT_SIZE_DC.key)] }

    val dotStyleMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DotStyle.getDefault()
    ) { preferences -> DotStyle.getById(preferences[stringPreferencesKey(PreferenceKey.DOT_STYLE_MC.key)] ?: DotStyle.getDefaultId()) }

    val isShowSecondsMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECONDS_MC
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_SECONDS_MC.key)] }

    val dotRadiusRoundMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_RADIUS_ROUND_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_RADIUS_ROUND_MC.key)] }

    val dotSpacingRoundMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_SPACING_ROUND_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_SPACING_ROUND_MC.key)] }

    val dotRadiusRoundSecMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_RADIUS_ROUND_SEC_MC.key)] }

    val dotSpacingRoundSecMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_SPACING_ROUND_SECONDS_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_SPACING_ROUND_SEC_MC.key)] }

    val dotRadiusSquareMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_RADIUS_SQUARE_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_RADIUS_SQUARE_MC.key)] }

    val dotSpacingSquareMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_SPACING_SQUARE_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_SPACING_SQUARE_MC.key)] }

    val dotRadiusSquareSecMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_RADIUS_SQUARE_SEC_MC.key)] }

    val dotSpacingSquareSecMC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DOT_SPACING_SQUARE_SEC_MC.key)] }

    val reverseRotationFC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_REVERSE_ROTATION_FC
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.REVERSE_ROTATION_FC.key)] }

    val paddingFC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_PADDING_FC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.PADDING_FC.key)] }

    val fontSizeFC = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_TEXT_SIZE_FC
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.FONT_SIZE_FC.key)] }

    val borderRadiusJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_BORDER_RADIUS_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.BORDER_RADIUS_JA.key)] }

    val borderThicknessJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_BORDER_THICKNESS_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.BORDER_THICKNESS_JA.key)] }

    val secondsHandLengthJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_SECONDS_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_SECONDS_JA.key)] }

    val minutesHandLengthJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_MINUTES_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_MINUTES_JA.key)] }

    val secondsHandWidthJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_SECONDS_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_SECONDS_JA.key)] }

    val minutesHandWidthJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_MINUTES_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_MINUTES_JA.key)] }

    val hoursHandLengthJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_HOURS_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_HOURS_JA.key)] }

    val hoursHandWidthJA = getParamFlow(
            dataStore = dataStore,
            defaultValue = DEFAULT_HAND_WIDTH_HOURS_JA
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_HOURS_JA.key)] }

    val showSecondHandJA = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECOND_HAND_JA
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.HAND_SHOW_SECOND_HAND_JA.key)] }

    val fontResAR = getParamFlow(
        dataStore = dataStore,
        defaultValue = com.smsoft.smartdisplay.data.Font.getDefault()
    ) { preferences ->
        com.smsoft.smartdisplay.data.Font.getById(
            preferences[stringPreferencesKey(PreferenceKey.DIGIT_FONT_AR.key)]
                ?: com.smsoft.smartdisplay.data.Font.getDefault().toString()
        ).font
    }

    val fontSizeAR = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DIGIT_FONT_SIZE_AR
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DIGIT_FONT_SIZE_AR.key)] }

    val minutesHandLengthAR = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_MINUTES_AR
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_MINUTES_AR.key)] }

    val minutesHandWidthAR = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_MINUTES_AR
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_MINUTES_AR.key)] }

    val hoursHandLengthAR = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_HOURS_AR
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_HOURS_AR.key)] }

    val hoursHandWidthAR = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_HOURS_AR
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_HOURS_AR.key)] }

    val fontCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = Font.getDefault().font
    ) { preferences -> Font.getById((preferences[stringPreferencesKey(PreferenceKey.DIGIT_FONT_CV2.key)] ?: Font.getDefaultId())).font }

    val digitStyleCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DigitStyle.getDefault()
    ) { preferences ->
        DigitStyle.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_STYLE_CV2.key)] ?: DigitStyle.getDefaultId())
    }

    val digitTextSizeCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_DIGIT_TEXT_SIZE_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.DIGIT_TEXT_SIZE_CV2.key)] }

    val outerRimWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_OUTER_RIM_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.OUTER_RIM_WIDTH_CV2.key)] }

    val innerRimWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_INNER_RIM_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.INNER_RIM_WIDTH_CV2.key)] }

    val thickMarkerWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_THICK_MARKER_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.THICK_MARKER_WIDTH_CV2.key)] }

    val thinMarkerWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_THIN_MARKER_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.THIN_MARKER_WIDTH_CV2.key)] }

    val hourHandWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_HOUR_HAND_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.HOUR_HAND_WIDTH_CV2.key)] }

    val minuteHandWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_MINUTE_HAND_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MINUTE_HAND_WIDTH_CV2.key)] }

    val secondHandWidthCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SECOND_HAND_WIDTH_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.SECOND_HAND_WIDTH_CV2.key)] }

    val centerCircleRadiusCV2 = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_CENTER_CIRCLE_RADIUS_CV2
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.CENTER_CIRCLE_RADIUS_CV2.key)] }

    val fontCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = com.smsoft.smartdisplay.ui.composable.clock.clockview.Font.getDefault().font
    ) { preferences ->
        com.smsoft.smartdisplay.ui.composable.clock.clockview.Font.getById((preferences[stringPreferencesKey(PreferenceKey.DIGIT_FONT_CV.key)] ?: com.smsoft.smartdisplay.ui.composable.clock.clockview.Font.getDefaultId())).font }

    val digitStyleCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = com.smsoft.smartdisplay.ui.composable.clock.clockview.DigitStyle.getDefault()
    ) { preferences -> com.smsoft.smartdisplay.ui.composable.clock.clockview.DigitStyle.getById(preferences[stringPreferencesKey(
        PreferenceKey.DIGIT_STYLE_CV.key)] ?: com.smsoft.smartdisplay.ui.composable.clock.clockview.DigitStyle.getDefaultId()) }

    val showHoursValuesCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_HOURS_CV
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.DIGIT_SHOW_HOURS_CV.key)] }

    val showMinutesValuesCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_MINUTES_CV
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.CV.key)] }

    val showDegreesCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_DEGREES_CV
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.DIGIT_SHOW_DEGREES_CV.key)] }

    val digitDispositionCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DigitDisposition.getDefault()
    ) { preferences -> DigitDisposition.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_DISPOSITION_CV.key)] ?: DigitDisposition.getDefaultId()) }

    val digitStepCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DigitStep.getDefault()
    ) { preferences -> DigitStep.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_STEP_CV.key)] ?: DigitStep.getDefaultId()) }

    val degreesTypeCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DegreeType.getDefault()
    ) { preferences -> DegreeType.getById(preferences[stringPreferencesKey(PreferenceKey.DEGREE_TYPE_CV.key)] ?: DegreeType.getDefaultId()) }

    val degreesStepCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DegreesStep.getDefault()
    ) { preferences -> DegreesStep.getById(preferences[stringPreferencesKey(PreferenceKey.DEGREE_STEP_CV.key)] ?: DegreesStep.getDefaultId()) }

    val showCenterCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_CENTER_CV
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_CENTER_CV.key)] }

    val showSecondsHandCV = getParamFlow(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECOND_HAND_CV
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_SECOND_HAND_CV.key)] }

    val digitStyleND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DigitStyle.getDefault()
    ) { preferences -> AnalogClockConfig.DigitStyle.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_STYLE_ND.key)] ?: AnalogClockConfig.DigitStyle.getDefaultId()) }

    val digitPositionND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_DIGIT_POSITION_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DIGIT_POSITION_ND.key)] }

    val highlightQuarterOfHourND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_DIGIT_EMP_QUARTERS_ND
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.DIGIT_EMP_QUARTERS_ND.key)] }

    val fontUriND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.Font.getDefault()
    ) { preferences ->
        AnalogClockConfig.Font.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_FONT_ND.key)] ?: AnalogClockConfig.Font.getDefault()).font
    }
    val fontSizeND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_DIGIT_FONT_SIZE_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DIGIT_FONT_SIZE_ND.key)] }

    val handStyleND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.HandStyle.getDefault()
    ) { preferences -> AnalogClockConfig.HandStyle.getById(preferences[stringPreferencesKey(PreferenceKey.HAND_STYLE_ND.key)] ?: AnalogClockConfig.HandStyle.getDefaultStyle()) }

    val handLengthMinutesND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_HAND_LEN_MIN_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_MINUTES_ND.key)] }

    val handLengthHoursND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_HAND_LEN_HOURS_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_HOURS_ND.key)] }

    val handWidthMinutesND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_HAND_WIDTH_MIN_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_MINUTES_ND.key)] }

    val handWidthHoursND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_HAND_WIDTH_HOURS_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_HOURS_ND.key)] }

    val showSecondHandND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_SHOW_SECOND_HAND_ND
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.HAND_SHOW_SECOND_HAND_ND.key)] }

    val tickStyleMinutesND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.TickStyle.getDefault()
    ) { preferences -> AnalogClockConfig.TickStyle.getById(preferences[stringPreferencesKey(PreferenceKey.TICK_STYLE_MINUTES_ND.key)] ?: AnalogClockConfig.TickStyle.getDefaultStyle()) }

    val tickStartMinutesND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_TICK_START_MINUTES_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_START_MINUTES_ND.key)] }

    val tickLengthMinutesND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_TICK_LEN_MINUTES_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_LEN_MINUTES_ND.key)] }

    val tickStyleHoursND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.TickStyle.CIRCLE
    ) { preferences -> AnalogClockConfig.TickStyle.getById(preferences[stringPreferencesKey(PreferenceKey.TICK_STYLE_HOURS_ND.key)] ?: AnalogClockConfig.TickStyle.CIRCLE.style) }

    val tickStartHoursND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_TICK_START_HOURS_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_START_HOURS_ND.key)] }

    val tickLengthHoursND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_TICK_LEN_HOURS_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_LEN_HOURS_ND.key)] }

    val innerCircleRadiusND = getParamFlow(
        dataStore = dataStore,
        defaultValue = AnalogClockConfig.DEFAULT_INNER_CIRCLE_RADIUS_ND
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.INNER_CIRCLE_RADIUS_ND.key)] }

    fun onStart() {
        fixedRateTimer(
            name= "default",
            daemon = false,
            initialDelay = 0L,
            period = TIMER_INTERVAL
        ) {

            val cal = Calendar.getInstance()
            uiStatePrivate.update {
                it.copy(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH),
                    day = cal.get(Calendar.DATE),
                    dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
                    hour = cal.get(Calendar.HOUR_OF_DAY),
                    minute = cal.get(Calendar.MINUTE),
                    second = cal.get(Calendar.SECOND),
                    milliSecond = cal.get(Calendar.MILLISECOND)
                )
            }

            if (!isTimerStarted) {
                cancel()
            }
        }
    }

    fun onStop() {
        isTimerStarted = false
    }

    private fun getTime(): ClockUiState {
        val cal = Calendar.getInstance()
        return ClockUiState(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH),
            day = cal.get(Calendar.DATE),
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            second = cal.get(Calendar.SECOND),
            milliSecond = cal.get(Calendar.MILLISECOND)
        )
    }
}

data class ClockUiState(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val milliSecond: Int
)