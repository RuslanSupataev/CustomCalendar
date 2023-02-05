package kg.ruslansupataev.customdatepicker.date_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType
import kg.ruslansupataev.customdatepicker.date_picker.models.compareTo
import kg.ruslansupataev.customdatepicker.date_picker.usecases.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DatePickerViewModel(
    private val getYears: GetYearsUseCase,
    private val getMonths: GetMonthsUseCase,
    private val getDays: GetDaysUseCase,
    private val getHours: GetHoursUseCase,
    private val getMinutes: GetMinutesUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<DatePickerState> = MutableStateFlow(
        DatePickerState(
            isFinished = false,
            isProgress = true,
            type = null,
            selectedRange = null,
            title = null,
            isCheckoutAvailable = false,
            checkoutSectionVisibility = false,
            previous = null,
            currentCalendarType = null,
            dates = listOf(),
            errorMessage = "",
            selectedRangeInStrings = null,
            result = null,
            itemsToRipple = listOf()
        )
    )

    val state: StateFlow<DatePickerState>
        get() = _state

    private var currentYear: RentalDateModel? = null
    private var currentMonth: RentalDateModel? = null
    private var currentDay: RentalDateModel? = null
    private var currentHour: RentalDateModel? = null

    fun handleSideEvent(event: DatePickerEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is DatePickerEvent.GotType -> gotType(type = event.type, rentalId = event.rentalId)
                is DatePickerEvent.SelectDate -> dateSelected(date = event.date, event.rentalId)
                is DatePickerEvent.Back -> back()
                is DatePickerEvent.Checkout -> checkout()
                is DatePickerEvent.SelectFromScratch -> startFromScratch(event.rentalId)
            }
        }
    }

    private suspend fun dateSelected(date: RentalDateModel, rentalId: Int) {
        val type = state.value.type
        if (type == state.value.currentCalendarType && type != null) {
            val currentRange = state.value.selectedRange
            val currentFirst = currentRange?.first
            val currentSecond = currentRange?.second
            val newRange =
                when {
                    currentRange == null -> {
                        Pair(date, null)
                    }
                    currentFirst != null && currentSecond == null && currentFirst == date  -> {
                        Pair(null, null)
                    }
                    date == currentFirst -> {
                        Pair(currentSecond, null)
                    }
                    date == currentSecond -> {
                        Pair(currentFirst, null)
                    }
                    currentFirst != null
                            && currentFirst.compareTo(date, type) -> {
                        Pair(date, currentSecond)
                    }
                    currentFirst != null
                            && date.compareTo(currentFirst, type) -> {
                        Pair(currentFirst, date)
                    }
                    else -> {
                        Pair(date, currentSecond)
                    }
                }
            val blockedDates = mutableListOf<RentalDateModel>()
            if (newRange.second != null) {
                val firstIndex = state.value.dates.indexOf(newRange.first)
                val secondIndex = state.value.dates.indexOf(newRange.second)

                for (i in firstIndex..secondIndex) {
                    val iDate = state.value.dates[i]
                    if (iDate.isBooked || !iDate.isAvailable) {
                        blockedDates.add(iDate)
                    }
                }
            }

            if (blockedDates.isEmpty())
                selectTime(newRange)
            else _state.update { it.copy(itemsToRipple = blockedDates) }

        } else {
            someDateSelected(date, rentalId)
        }
    }

    private fun selectTime(range: Pair<RentalDateModel?, RentalDateModel?>) {
        val secondDate = range.second
        val firstDate = range.first
        var selectedRangeInString: Pair<String?, String?>? = null

        if (firstDate != null) {
            selectedRangeInString =
                if (state.value.currentCalendarType == SelectingType.HOURS
                    || state.value.currentCalendarType == SelectingType.MINUTES
                ) {
                    if (secondDate == null) {
                        Pair(firstDate.value, null)
                    } else {
                        val format = if (hasAMorPM(secondDate.value)) "hh:mmaa (dd.MM.yyyy)"
                        else "HH:mm (dd.MM.yyyy)"
                        val timeInMs = convertDateToTimeInMs(secondDate, null)?.first
                        if (timeInMs != null) {
                            Pair(firstDate.value, getDate(timeInMs, format))
                        } else {
                            Pair(firstDate.value, null)
                        }
                    }
                } else {
                    val convertedDatesPair = convertDateToTimeInMs(firstDate, range.second)
                    if (convertedDatesPair != null) {
                        val first = getDate(convertedDatesPair.first, "dd.MM.yyyy")
                        val secondDateMs = convertedDatesPair.second
                        val second =
                            if (secondDateMs == null) "" else getDate(secondDateMs, "dd.MM.yyyy")
                        Pair(first, second)
                    } else {
                        null
                    }
                }
        }
        _state.update {
            it.copy(
                selectedRange = range,
                isCheckoutAvailable = range.second != null,
                selectedRangeInStrings = selectedRangeInString
            )
        }
    }

    private fun convertDateToTimeInMs(
        date: RentalDateModel,
        secondDate: RentalDateModel?
    ): Pair<Long, Long?>? {
        var secondValue: String? = null
        return when (state.value.currentCalendarType) {
            SelectingType.YEARS -> {
                if (secondDate != null) {
                    secondValue = secondDate.value
                }
                convertYearRangeToTimestamp(Pair(date.value, secondValue))
            }
            SelectingType.MONTHS -> {
                currentYear?.let {
                    if (secondDate != null) {
                        secondValue = secondDate.value + " " + it.value
                    }
                    convertMonthRangeToTimestamp(Pair(date.value + " " + it.value, secondValue))
                }
            }
            SelectingType.DAYS -> {
                currentYear?.value?.let { year ->
                    currentMonth?.value?.let { month ->
                        if (secondDate != null) {
                            secondValue = "${secondDate.value} $month $year"
                        }
                        convertDayRangeToTimestamp(Pair("${date.value} $month $year", secondValue))
                    }
                }
            }
            SelectingType.HOURS -> {
                currentYear?.value?.let { year ->
                    currentMonth?.value?.let { month ->
                        currentDay?.value?.let { day ->
                            if (secondDate != null) {
                                secondValue = "${secondDate.value} $day $month $year"
                            }
                            convertTimeRangeToTimestamp(
                                Pair(
                                    "${date.value} $day $month $year",
                                    secondValue
                                )
                            )
                        }
                    }
                }
            }
            SelectingType.MINUTES -> {
                currentYear?.value?.let { year ->
                    currentMonth?.value?.let { month ->
                        currentDay?.value?.let { day ->
                            val firstMin = "${date.value} $day $month $year"
                            if (secondDate != null)
                                secondValue = "${secondDate.value} $day $month $year"
                            convertTimeRangeToTimestamp(Pair(firstMin, secondValue))
                        }
                    }
                }
            }
            null -> {
                null
            }
        }
    }

    private fun checkout() {
        val selectedRange =
            state.value.selectedRange ?: throw RuntimeException("range should be selected")

        val first = selectedRange.first ?: throw RuntimeException("start range should be selected")
        val second = selectedRange.second ?: throw RuntimeException("end range should be selected")

        val dateRangeMs: Pair<Long, Long?>? = convertDateToTimeInMs(first, second)

        if (dateRangeMs?.second != null) {
            _state.update { it.copy(result = dateRangeMs as Pair<Long, Long>) }
        }
    }

    private suspend fun someDateSelected(date: RentalDateModel, rentalId: Int) {
        _state.update { it.copy(isProgress = true) }

        var dates: List<RentalDateModel> = listOf()
        var currentCalendarType = SelectingType.YEARS
        val previousState = state.value
        var title: String? = state.value.title

        when (state.value.currentCalendarType) {
            SelectingType.YEARS -> {
                getMonths(rentalId, date.id).collect {
                    currentYear = date
                    dates = it
                    currentCalendarType = SelectingType.MONTHS
                    title = date.value
                }
            }
            SelectingType.MONTHS -> {
                getDays(rentalId, currentYear!!.id, date.id).collect {
                    currentMonth = date
                    dates = it
                    currentCalendarType = SelectingType.DAYS
                    title = convertYearAndMonthFormat(date.value + " " + title)
                }
            }
            SelectingType.DAYS -> {
                getHours(rentalId, currentYear!!.id, currentMonth!!.id, date.id).collect { data ->
                    val hours =
                        if (state.value.type == SelectingType.HOURS) {
                            data.map {
                                it.copy(
                                    value =
                                    if (it.value.contains("AM") || it.value.contains("PM"))
                                        it.value
                                    else "${it.value}:00"
                                )
                            }
                        } else data
                    currentDay = date
                    dates = hours
                    currentCalendarType = SelectingType.HOURS
                    title = formatDay(date.value) + "." + title
                }
            }
            SelectingType.HOURS -> {
                val hourOfDate = getHourOfDateOf(date)

                getMinutes(
                    rentalId,
                    currentYear!!.id,
                    currentMonth!!.id,
                    currentDay!!.id,
                    date.id,
                    hourOfDate.first.toInt(),
                    hourOfDate.second
                ).collect {
                    currentHour = date
                    dates = it
                    currentCalendarType = SelectingType.MINUTES
                }
            }
            SelectingType.MINUTES -> {}
            null -> {
                throw RuntimeException("type has not been provided")
            }
        }



        _state.update {
            it.copy(
                dates = dates,
                currentCalendarType = currentCalendarType,
                previous = previousState,
                title = title,
                checkoutSectionVisibility = currentCalendarType == it.type,
                isProgress = false
            )
        }
    }

    private fun back() {
        if (state.value.previous == null) {
            _state.update { it.copy(isFinished = true) }
        } else {
            _state.update { state.value.previous!!.copy(isProgress = false) }
        }
    }

    private suspend fun startFromScratch(rentalId: Int) {
        _state.update { it.copy(isProgress = true) }
        getYears(rentalId).collect { years ->
            _state.update {
                it.copy(
                    currentCalendarType = SelectingType.YEARS,
                    dates = years,
                    title = null,
                    isCheckoutAvailable = false,
                    checkoutSectionVisibility = false,
                    isProgress = false
                )
            }
        }
    }

    private suspend fun gotType(type: SelectingType, rentalId: Int) {
        if (type == state.value.type) return
        _state.update { it.copy(isProgress = true) }
        when (type) {
            SelectingType.YEARS -> {
                getYears(rentalId).collect { years ->
                    _state.update {
                        it.copy(
                            type = type,
                            currentCalendarType = type,
                            dates = years,
                            isProgress = false,
                            checkoutSectionVisibility = true
                        )
                    }
                }
            }
            SelectingType.MONTHS -> {
                fetchMonthByYear(rentalId).collect { months ->
                    _state.update {
                        it.copy(
                            type = type,
                            currentCalendarType = type,
                            dates = months,
                            isProgress = false,
                            checkoutSectionVisibility = true
                        )
                    }
                }
            }
            SelectingType.DAYS -> {
                fetchDays(rentalId).collect { days ->
                    _state.update {
                        it.copy(
                            type = type,
                            currentCalendarType = type,
                            dates = days,
                            isProgress = false,
                            checkoutSectionVisibility = true
                        )
                    }
                }
            }
            SelectingType.HOURS -> {
                fetchDays(rentalId).collect { data ->
                    _state.update {
                        it.copy(
                            type = type,
                            currentCalendarType = SelectingType.DAYS,
                            dates = data,
                            isProgress = false
                        )
                    }
                }
            }
            SelectingType.MINUTES -> {
                fetchDays(rentalId).collect { days ->
                    _state.update {
                        it.copy(
                            type = type,
                            currentCalendarType = SelectingType.DAYS,
                            dates = days,
                            isProgress = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchMonthByYear(rentalId: Int): Flow<List<RentalDateModel>> = flow {
        getYears(rentalId).collect { list ->
            var year: RentalDateModel? = null
            list.forEach {
                if (!it.isBooked && it.isAvailable) {
                    year = it
                    return@forEach
                }
            }
            if (year == null) {
                _state.update {
                    it.copy(
                        errorMessage = "year has not been provided from API",
                        isProgress = false
                    )
                }
            } else {
                getMonths(rentalId, year!!.id).collect { months ->
                    _state.update { it.copy(title = year?.value) }
                    currentYear = year
                    emit(months)
                }
            }
        }
    }

    private suspend fun fetchDays(rentalId: Int) = flow {
        fetchMonthByYear(rentalId).collect { months ->
            var month: RentalDateModel? = null
            months.forEach {
                if (!it.isBooked && it.isAvailable) {
                    month = it
                    return@forEach
                }
            }
            if (month == null || currentYear == null) {
                _state.update {
                    it.copy(
                        errorMessage = "month has not been provided from API",
                        isProgress = false
                    )
                }
            } else {
                getDays(
                    rentalId = rentalId,
                    yearId = currentYear!!.id,
                    monthId = month!!.id
                ).collect { list ->
                    _state.update { it.copy(title = convertYearAndMonthFormat(month?.value + " " + it.title)) }
                    currentMonth = month
                    emit(list)
                }
            }
        }
    }
}