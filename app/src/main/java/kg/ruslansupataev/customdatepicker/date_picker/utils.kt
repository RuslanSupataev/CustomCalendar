package kg.ruslansupataev.customdatepicker.date_picker

import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

fun generateId(baseId: Int): Int {
    val random = Random(baseId.hashCode().toLong())
    return random.nextInt(baseId)
}

fun isPrime(num: Int): Boolean {
    var flag = false
    for (i in 2..num / 2) {
        if (num % i == 0) {
            flag = true
            break
        }
    }
    return !flag
}

fun getRandomSelectingType(): SelectingType {
    val values = SelectingType.values()
    return values[(Math.random() * values.size).toInt()]
}


// taking date (January 2022) and converting in into 01.2022
fun convertYearAndMonthFormat(input: String): String {
    val locale = Locale.getDefault()
    val inputPattern = "MMMM yyyy"
    val outputPattern = "MM.yyyy"
    val inputFormatter = SimpleDateFormat(inputPattern, locale)
    val outputFormatter = SimpleDateFormat(outputPattern, locale)

    val date = inputFormatter.parse(input)
    return outputFormatter.format(date)
}

// takes a string representing a day of the month (1 to 31) and adds a leading zero if it's less than 10
fun formatDay(day: String): String {
    return if (day.toInt() < 10) {
        "0$day"
    } else {
        day
    }
}

// converting pair such as "2022"-to"2023" into time in ms
fun convertYearRangeToTimestamp(range: Pair<String, String?>): Pair<Long, Long?> {
    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("UTC")
    calendar.set(Calendar.YEAR, range.first.toInt())
    calendar.set(Calendar.MONTH, Calendar.JANUARY)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTimestamp = calendar.timeInMillis

    val secondInRange = range.second
    val endTimestamp = if (secondInRange != null) {
        calendar.set(Calendar.YEAR, secondInRange.toInt())
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        calendar.timeInMillis
    } else {
        null
    }

    return Pair(startTimestamp, endTimestamp)
}

// converting pair such as "January 2022"-to"May 2023" into time in ms
fun convertMonthRangeToTimestamp(range: Pair<String, String?>): Pair<Long, Long?> {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("UTC")
    calendar.time = formatter.parse(range.first) as Date
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTimestamp = calendar.timeInMillis

    val secondInRange = range.second
    val endTimestamp = if (secondInRange != null) {
        calendar.time = formatter.parse(secondInRange) as Date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        calendar.timeInMillis
    } else {
        null
    }
    return Pair(startTimestamp, endTimestamp)
}

// converting pair such as "1 January 2022"-to"13 January 2023" into time in ms
fun convertDayRangeToTimestamp(range: Pair<String, String?>): Pair<Long, Long?> {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("UTC")
    calendar.time = formatter.parse(range.first) as Date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTimestamp = calendar.timeInMillis

    val secondInRange = range.second
    val endTimestamp = if (secondInRange != null) {
        calendar.time = formatter.parse(secondInRange) as Date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        calendar.timeInMillis
    } else {
        null
    }

    return Pair(startTimestamp, endTimestamp)
}

fun hasAMorPM(time: String): Boolean {
    return time.contains("AM") || time.contains("PM")
}

// converting pair such as "09:00AM 1 January 2022"-to"10:00PM 1 January 2023" into time in ms
fun convertTimeRangeToTimestamp(input: Pair<String, String?>): Pair<Long, Long?> {
    val locale = Locale.getDefault()
    val inputPattern =
        if (hasAMorPM(input.first) && input.first.contains(":")) "hh:mmaa dd MMMM yyyy"
        else if (hasAMorPM(input.first)) "hhaa dd MMMM yyyy"
        else "hh:mm dd MMMM yyyy"
    Log.e("olololo", "convertTimeRangeToTimestamp: input pattern is $inputPattern")
    val inputFormatter = SimpleDateFormat(inputPattern, locale)

    val firstDate = inputFormatter.parse(input.first)
    val firstTime = firstDate.time

    val secondTime = if (input.second != null) {
        val secondDate = inputFormatter.parse(input.second)
        secondDate.time
    } else {
        null
    }

    return Pair(firstTime, secondTime)
}

// comparing months like "January" and "May" and return true if first month is more near to end of year, otherwise false
fun compareMonths(month1: String, month2: String, locale: Locale = Locale.getDefault()): Boolean {
    val formatter = SimpleDateFormat("MMMM", locale)
    val calendar = Calendar.getInstance()

    calendar.timeZone = TimeZone.getTimeZone("UTC")

    calendar.time = formatter.parse(month1) as Date
    val month1Timestamp = calendar.timeInMillis
    calendar.time = formatter.parse(month2) as Date
    val month2Timestamp = calendar.timeInMillis
    return month1Timestamp > month2Timestamp
}

// comparing times like "12:00" and "13:43"
fun compareTimes(time1: String, time2: String): Boolean {
    val locale = Locale.getDefault()
    val inputPattern =
        if (hasAMorPM(time1) && time1.contains(":")) "hh:mmaa"
        else if (hasAMorPM(time1)) "hhaa"
        else "HH:mm"
    val formatter = SimpleDateFormat(inputPattern, locale)
    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("UTC")

    calendar.time = formatter.parse(time1) as Date
    val time1Timestamp = calendar.timeInMillis
    calendar.time = formatter.parse(time2) as Date
    val time2Timestamp = calendar.timeInMillis

    return time1Timestamp > time2Timestamp
}

fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun getHourOfDateOf(model: RentalDateModel): Pair<String, String?> {
    return if (model.value.toIntOrNull() == null) {
        val regex = "(\\d+)([APM]+)".toRegex()
        val matchResult = regex.find(model.value)
        val hour = matchResult?.groupValues?.get(1)?.toInt() ?: 0
        val period = matchResult?.groupValues?.get(2) ?: ""

        Pair(hour.toString(), period)
    } else {
        Pair(model.value, null)
    }
}

fun View.animateRippleEffect(duration: Long = 100) {
    val animation = TranslateAnimation(0f, 10f, 0f, 0f)
    animation.duration = duration
    animation.repeatMode = Animation.REVERSE
    animation.repeatCount = 2
    startAnimation(animation)
}