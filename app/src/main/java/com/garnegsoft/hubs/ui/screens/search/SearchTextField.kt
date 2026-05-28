package com.garnegsoft.hubs.ui.screens.search

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly


@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    onValueChange: (String) -> Unit
) {
    val showClearAllButton = remember(value) { value.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colors.surface)
            .heightIn(min = 48.dp),
       verticalAlignment = Alignment.CenterVertically

    ) {
        Box(
            modifier = Modifier.weight(1f).padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                modifier = modifier,
                value = value,
                enabled = enabled,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = MaterialTheme.colors.onBackground, fontSize = 18.sp),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colors.secondary)
            )
            if (value.isEmpty()) {
                Text(
                    text = "Введите запрос или ссылку",
                    color = MaterialTheme.colors.secondary.copy(0.5f)
                )
            }
        }

        AnimatedVisibility(
            visible = showClearAllButton,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            IconButton(
                onClick = {
                    onValueChange("")
                }
            ) {
                Icon(
                    tint = MaterialTheme.colors.secondary,
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "clear input"
                )
            }
        }
    }
}