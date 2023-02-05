package kg.ruslansupataev.customdatepicker.date_picker.models

import kg.ruslansupataev.customdatepicker.date_picker.compareMonths
import kg.ruslansupataev.customdatepicker.date_picker.compareTimes

enum class SelectingType {
    MINUTES, HOURS, DAYS, MONTHS, YEARS
}

// return true if first one is more
fun SelectingType.compareTwoRentalDates(first: RentalDateModel, second: RentalDateModel): Boolean {
    return when (this) {
        SelectingType.YEARS -> first.value.toInt() > second.value.toInt()
        SelectingType.MONTHS -> compareMonths(first.value, second.value)
        SelectingType.DAYS -> first.value.toInt() > second.value.toInt()
        SelectingType.HOURS -> compareTimes(first.value, second.value)
        SelectingType.MINUTES -> compareTimes(first.value, second.value)
        else -> throw RuntimeException("new values in ${javaClass.name} should implement comparing func")
    }
}