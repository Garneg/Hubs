package com.garnegsoft.hubs.ui.navigation

import android.app.Activity
import androidx.navigation.NavController



fun NavController.navigateBack(): Boolean {
    (context as? Activity)?.let { parentActivity ->
        if (parentActivity.intent.data != null && this.previousBackStackEntry == null) {
            parentActivity.finish()
        }
    }
    return popBackStack()
}