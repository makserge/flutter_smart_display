package com.arbelkilani.clock

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ClockView(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int
) {
   // val theme = AnalogicalThemeBuilder()
        //.setShowBorder(true)
        //        .setBorderColor(R.color.blue)
     //   .setShowProgress(true)
       // .build()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            Clock(context).apply {
              //  clockType = ClockType.analogical
         //       setAnalogicalTheme(theme)
                setShowSecondsNeedle(true)
            }
        },
        update = { view ->
            view.setTime(
                isAM = true,
                hour = hour,
                minute = minute,
                second = second
            )
        }
    )
}
