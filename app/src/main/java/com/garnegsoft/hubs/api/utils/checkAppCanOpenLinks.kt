package com.garnegsoft.hubs.api.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri


fun checkAppCanOpenLinks(context: Context): Boolean {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://habr.com"))
    val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    resolveInfoList.forEach { info ->
        if (info.activityInfo.packageName == context.packageName) return true
    }

    return false
}
