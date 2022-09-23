package com.escapeindustries.dotmatrix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MatrixDisplay(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    hour: Int,
    minute: Int,
    second: Int
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MatrixDisplayView(
            modifier = modifier,
            color = color,
            hour = hour,
            minute = minute,
            second = second
        )
        /*
        AndroidView(
            modifier = modifier,
            factory = { context ->
                MatrixDisplayView(context).apply {
                    setPrimaryColor(color)
                }
            },
            update = { view ->
                view.setTime(
                      hour = hour,
                      minute = minute,
                      second = second
                )
            }
        )

         */
    }
}