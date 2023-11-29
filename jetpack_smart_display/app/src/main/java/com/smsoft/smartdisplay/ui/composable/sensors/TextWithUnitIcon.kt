package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.utils.getIcon

@Composable
fun TextWithUnitIcon(
    modifier: Modifier,
    context: Context,
    text: String,
    unit: String,
    icon: String? = null,
    imageVector: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .defaultMinSize(
                minWidth = 90.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!icon.isNullOrEmpty()) {
            Icon(
                modifier = Modifier
                    .padding(
                        end = 3.dp
                    ),
                painter = painterResource(
                    id = getIcon(
                        context = context,
                        item = icon
                    )
                ),
                contentDescription = null
            )
        }
        if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier,
            text = text,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .padding(
                    end = 10.dp
                ),
            text = unit,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )
    }
}