package com.garnegsoft.hubs.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.garnegsoft.hubs.ui.common.HubsTopAppBar


@Composable
fun WidgetSettingsScreen(modifier: Modifier = Modifier) {
    BottomSheetScaffold(
        topBar = {
            HubsTopAppBar(
                title = {
                    Text(text = "Настройки виджета")
                }
            )
        },
        sheetContent = {

        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
        ) {

        }
    }
}