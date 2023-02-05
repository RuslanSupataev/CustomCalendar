package kg.ruslansupataev.customdatepicker.date_picker.models

data class RentalDateModel(
    val id: Int,
    val value: String,
    val isAvailable: Boolean,
    val isBooked: Boolean
)

fun RentalDateModel.compareTo(date: RentalDateModel, type: SelectingType) =
    type.compareTwoRentalDates(this, date)