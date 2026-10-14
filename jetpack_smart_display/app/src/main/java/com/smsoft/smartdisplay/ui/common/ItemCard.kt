package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

/**
 * The card of the sensor, timer and alarm lists: [top] (the value and its own controls), then
 * [bottom] (the title or the days) with the pencil and the trash at its end in edit mode.
 *
 * A tap on the card does nothing and a long press toggles edit mode: editing goes through the
 * pencil only (a tap opened the editor, on the timer cards even outside edit mode).
 *
 * The pencil and the trash take no height: centred on the bottom line, they reach into the space
 * above it and the padding below it, so no card changes its height or its layout when edit mode
 * toggles. [rowSpacing] keeps them away from the controls in [top]: a slip off a button there
 * must not land on the trash, which deletes at once.
 *
 * The end padding is 6 dp: with the 10 dp a 48 dp icon box leaves beside its glyph, the trailing
 * glyphs end 16 dp from the edge, as the text starts 16 dp from the other one. Text in [top] and
 * [bottom] that reaches the end adds [ITEM_TEXT_END_INSET].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    editMode: Boolean,
    editDescription: String,
    onEnableModification: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    rowSpacing: Dp = ITEM_ROW_SPACING,
    top: @Composable () -> Unit,
    bottom: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onEnableModification
            ),
        elevation = 8.dp,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 6.dp, bottom = 16.dp)
        ) {
            top()
            Spacer(
                modifier = Modifier.height(rowSpacing)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1F)
                        .padding(end = if (editMode) 0.dp else ITEM_TEXT_END_INSET)
                ) {
                    bottom()
                }
                if (editMode) {
                    Row(
                        modifier = Modifier
                            .height(0.dp)
                            .wrapContentHeight(unbounded = true)
                    ) {
                        // The whole 48 dp box is the target, the glyph sits in its first 38 dp
                        Icon(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(role = Role.Button, onClick = onEdit)
                                .padding(end = 10.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = editDescription
                        )
                        Icon(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(role = Role.Button, onClick = onDelete),
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        }
    }
}

// Text that reaches the card's end keeps 16 dp from the edge, like the trailing glyphs
val ITEM_TEXT_END_INSET = 10.dp

// Between the top and the bottom line of a card
val ITEM_ROW_SPACING = 10.dp
