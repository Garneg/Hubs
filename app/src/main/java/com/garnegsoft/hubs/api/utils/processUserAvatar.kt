package com.garnegsoft.hubs.api.utils


/**
 * Replaces uncorrect host of habrastorage and returns url to placeholder avatar in case avatarUrl is null
 */
fun processUserAvatar(avatarUrl: String?, alias: String): String {
    return if (avatarUrl == null || avatarUrl == "") {
        placeholderAvatarUrl(alias)
    } else {
        avatarUrl.replace("//habrastorage", "https://hsto")
    }
}

