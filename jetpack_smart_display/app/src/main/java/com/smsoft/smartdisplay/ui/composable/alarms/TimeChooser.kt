package com.smsoft.smartdisplay.ui.composable.alarms

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun TimeChooser(
    modifier: Modifier,
    time: Int,
    onChange: (time: Int) -> Unit
) {
    val hours = time / 60
    val minutes = time - (hours * 60)
    val dialog = TimePickerDialog(
        LocalContext.current,
        { _, newHour, newMinute -> onChange(newHour * 60 + newMinute) },
        hours,
        minutes,
        true
    )
    Text(
        modifier = Modifier
            .padding(start = 10.dp)
            .clickable(onClick = {
                dialog.show()
            }),
        text = "%d:%02d".format(hours, minutes),
        style = MaterialTheme.typography.h5,
        color = MaterialTheme.colors.primary
    )

}