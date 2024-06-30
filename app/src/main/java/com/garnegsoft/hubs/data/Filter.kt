package com.garnegsoft.hubs.data

import java.util.logging.Filter

interface Filter {
	fun toArgsMap(): Map<String, String>
	
	fun getTitle(): String
	
}

enum class FilterPeriod {
	Day,
	Week,
	Month,
	Year,
	AllTime
}