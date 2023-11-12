package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun LogoutConfirmDialog(
	show: Boolean,
	onDismiss: () -> Unit,
	onProceed: () -> Unit,
) {
	if (show) {
		Dialog(
			properties = DialogProperties(true, true),
			onDismissRequest = onDismiss
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(26.dp))
					.background(MaterialTheme.colors.surface)
					.padding(20.dp)
			) {
				Column(modifier = Modifier.height(IntrinsicSize.Min)) {
					Text(
						text = "Вы хотите выйти?",
						color = MaterialTheme.colors.onSurface,
						style = MaterialTheme.typography.subtitle1
					)
//					Spacer(modifier = Modifier.height(12.dp))
//					Box(modifier = Modifier
//						.weight(1f)
//						.verticalScroll(rememberScrollState())) {
//						Text(text = "")
//					}
					Spacer(modifier = Modifier.height(12.dp))
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.End
					) {
						TextButton(
							onClick = onDismiss,
							contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
						) {
							Text(text = "Отмена")
						}
						Spacer(modifier = Modifier.width(4.dp))
						Button(onClick = onProceed, elevation = null) {
							Text(text = "Выйти")
						}
					}
				}
			}
		}
	}
}