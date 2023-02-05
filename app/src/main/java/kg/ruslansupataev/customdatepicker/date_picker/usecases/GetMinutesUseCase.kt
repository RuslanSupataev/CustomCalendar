package kg.ruslansupataev.customdatepicker.date_picker.usecases

import kg.ruslansupataev.customdatepicker.date_picker.generateId
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kotlinx.coroutines.flow.flow

class GetMinutesUseCase {

    operator fun invoke(
        rentalId: Int,
        yearId: Int,
        monthId: Int,
        dayId: Int,
        hourId: Int,
        hourValue: Int,
        apOrPmOrNull: String?
    ) = flow {
        emit(getMockedMinutes(hourValue, apOrPmOrNull))
    }

    private fun getMockedMinutes(hourValue: Int, amOrPmOrNull: String?): List<RentalDateModel> {
        val list = mutableListOf<RentalDateModel>()
        val randomRange = randomRange()
        for (i in 1..59) {
            val hourStr =
                if (hourValue > 9) "$hourValue:"
                else "0$hourValue:"
            val minuteStr =
                if (i > 9) "$i"
                else "0$i"

            list.add(
                RentalDateModel(
                    id = generateId(i),
                    value = hourStr + minuteStr + (amOrPmOrNull ?: ""),
                    isAvailable = true,
                    isBooked = i in randomRange.first..randomRange.second
                )
            )
        }
        return list
    }

    private fun randomRange(): Pair<Int, Int> {
        val random = java.util.Random()
        val start = random.nextInt(60)
        val end = random.nextInt(60)
        return if (end >= start) Pair(start, end) else Pair(end, start)
    }
}