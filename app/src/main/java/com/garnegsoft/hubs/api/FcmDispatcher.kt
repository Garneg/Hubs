package com.garnegsoft.hubs.api

import android.os.Bundle
import android.util.Log

class FcmDispatcher {
	companion object {
		fun dispatchExtras(
			handleUrl: (url: String) -> Unit,
			extras: Bundle,
		) {
			if (extras.getString("type") == null || extras.getString("type") == "") {
				Log.e("FcmDispatcher", "Msg cannot be dispatched without 'type' field")
				return
			}
			
			val type = extras.getString("type")!!
			
			when (type) {
				"url" -> {
					if (extras.getString("url") == null) {
						Log.e(
							"FcmDispatcher",
							"It seems that type of msg was url but actual url wasn't provided"
						)
					} else {
						handleUrl(extras.getString("url")!!)
					}
				}
				
				else -> {
					Log.i("FcmDispatcher", "Unknown type of msg was provided: $type")
				}
			}
		}
	}
}