package kg.ruslansupataev.customdatepicker.date_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kg.ruslansupataev.customdatepicker.date_picker.usecases.*

class DatePickerViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
           modelClass.isAssignableFrom(DatePickerViewModel::class.java) -> {
               DatePickerViewModel(
                   getYears = GetYearsUseCase(),
                   getMonths = GetMonthsUseCase(),
                   getDays = GetDaysUseCase(),
                   getHours = GetHoursUseCase(),
                   getMinutes = GetMinutesUseCase()
               ) as T
           }
           else -> { super.create(modelClass) }
        }
    }
}