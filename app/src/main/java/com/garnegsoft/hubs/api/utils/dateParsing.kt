package com.garnegsoft.hubs.api.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*


private val defaultInputFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
private val defaultOutputFormatter = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.US)
private val customOutputFormatter = SimpleDateFormat("HH:mm", Locale.US)

private val birthDateInputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val birthDateOutputFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)


private val MONTH_MAP = mapOf(
    1 to "янв",
    2 to "фев",
    3 to "мар",
    4 to "апр",
    5 to "мая",
    6 to "июн",
    7 to "июл",
    8 to "авг",
    9 to "сен",
    10 to "окт",
    11 to "ноя",
    12 to "дек",
)


fun formatTime(time: String): String {
    if (time.isBlank()){
        return ""
    }
    var result = String()
    
    val localedTime = defaultInputFormatter.parse(time.split('+')[0].replace('T', ' '))!!
    localedTime.time = localedTime.time + TimeZone.getDefault().getOffset(Date().time)
    
    var todayCallendar = Calendar.getInstance()
    var publishCalendar = Calendar.getInstance()
    publishCalendar.time = localedTime

    if (publishCalendar.get(Calendar.YEAR) == todayCallendar.get(Calendar.YEAR)
        && publishCalendar.get(Calendar.DAY_OF_YEAR) == todayCallendar.get(Calendar.DAY_OF_YEAR)
    ) {
        result = "Сегодня в ${customOutputFormatter.format(publishCalendar.time)}"
        return result
    }
    if (publishCalendar.get(Calendar.YEAR) == todayCallendar.get(Calendar.YEAR)
        && publishCalendar.get(Calendar.DAY_OF_YEAR) == todayCallendar.get(Calendar.DAY_OF_YEAR) - 1
    ) {
        result = "Вчера в ${customOutputFormatter.format(publishCalendar.time)}"
        return result
    }

    result = defaultOutputFormatter.format(localedTime)

    var month = result.split(" ")[1].split(".")[1].toInt()
    var mappedMonth = MONTH_MAP[month]
    var year = result.split(" ")[1].split(".")[2]
    result = "${result.split(" ")[1].split(".")[0].toInt()} $mappedMonth $year в ${result.split(" ")[0]}"

    if (publishCalendar.get(Calendar.YEAR) == todayCallendar.get(Calendar.YEAR)) {
        result = result.replace(" $year", "")
    }

    return result
}

fun formatBirthdate(date: String): String {
    val date = birthDateInputFormatter.parse(date)

    val birthDay = birthDateOutputFormatter.format(date).split('.')[0].toInt()
    val birthMonth = MONTH_MAP.get(birthDateOutputFormatter.format(date).split('.')[1].toInt())
    val birthYear = birthDateOutputFormatter.format(date).split('.')[2]

    return "$birthDay $birthMonth $birthYear"
}

fun formatFoundationDate(day: String?, month: String?, year: String?): String? {
    var result = String()

    day?.let{
        result += "${it.toInt()} "
    }
    month?.let {
        result += "${MONTH_MAP[it.toInt()]} "
    }
    year?.let {
        result += it.toInt().toString()
    }
    return result.ifEmpty { null }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    return customOutputFormatter.format(date)
}