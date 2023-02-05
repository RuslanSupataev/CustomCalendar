package kg.ruslansupataev.customdatepicker.date_picker

import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType

data class DatePickerState(
    val isFinished: Boolean,
    val isProgress: Boolean,
    val type: SelectingType?,
    val selectedRange: Pair<RentalDateModel?, RentalDateModel?>?,
    val title: String?,
    val isCheckoutAvailable: Boolean,
    val checkoutSectionVisibility: Boolean,
    val previous: DatePickerState?,
    val currentCalendarType: SelectingType?,
    val dates: List<RentalDateModel>,
    val errorMessage: String,
    val selectedRangeInStrings: Pair<String?, String?>?,
    // result range of time in ms | startTimeMs and endTimeMs
    val result: Pair<Long, Long>?,
    val itemsToRipple: List<RentalDateModel>
)

sealed interface DatePickerEvent {
    data class SelectDate(val date: RentalDateModel, val rentalId: Int): DatePickerEvent
    data class GotType(val type: SelectingType, val rentalId: Int): DatePickerEvent
    data class SelectFromScratch(val rentalId: Int): DatePickerEvent
    object Checkout: DatePickerEvent
    object Back: DatePickerEvent
}