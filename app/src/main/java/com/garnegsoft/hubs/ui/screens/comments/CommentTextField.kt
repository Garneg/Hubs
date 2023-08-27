package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R


@Composable
fun EnterCommentTextField(
	focusRequester: FocusRequester,
	onSend: (text: String) -> Unit
) {
	Row(
		verticalAlignment = Alignment.Bottom, modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colors.surface)
			.padding(4.dp)
	) {
		var commentTextFieldValue by remember { mutableStateOf(TextFieldValue()) }
		Box(
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = 8.dp)
		) {
			BasicTextField(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 12.dp)
					.heightIn(max = 140.dp)
					.focusRequester(focusRequester),
				value = commentTextFieldValue,
				onValueChange = {
					commentTextFieldValue = it
				},
				cursorBrush = SolidColor(MaterialTheme.colors.secondary),
				textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface)
			)
			if (commentTextFieldValue.text.isEmpty()) {
				Row(modifier = Modifier.align(Alignment.CenterStart)) {
					Text(
						text = "Комментарий",
						color = MaterialTheme.colors.onSurface.copy(0.4f)
					)
					Spacer(modifier = Modifier.width(4.dp))
					Icon(
						tint = MaterialTheme.colors.onSurface.copy(0.4f),
						painter = painterResource(id = R.drawable.markdown),
						contentDescription = ""
					)
				}
				
			}
		}
		
		IconButton(
			enabled = commentTextFieldValue.text.isNotBlank(),
			onClick = {
				onSend(commentTextFieldValue.text)
				commentTextFieldValue = TextFieldValue()
			}
		) {
			val iconTint by animateColorAsState(
				targetValue = if (commentTextFieldValue.text.isNotBlank()) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface.copy(
					0.4f
				)
			)
			Icon(
				tint = iconTint,
				painter = painterResource(id = R.drawable.send),
				contentDescription = "send comment"
			)
		}
	}
}