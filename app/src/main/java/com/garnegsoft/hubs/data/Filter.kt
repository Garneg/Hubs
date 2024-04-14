package com.garnegsoft.hubs.data

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