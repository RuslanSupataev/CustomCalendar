package kg.ruslansupataev.customdatepicker.date_picker.usecases

import kg.ruslansupataev.customdatepicker.date_picker.generateId
import kg.ruslansupataev.customdatepicker.date_picker.isPrime
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kotlinx.coroutines.flow.flow

class GetMonthsUseCase {
    private val months =
        listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

    operator fun invoke(rentalId: Int, yearId: Int) = flow {
        emit(getMockedMonths(yearId))
    }

    private fun getMockedMonths(yearId: Int): List<RentalDateModel> {
        val list = mutableListOf<RentalDateModel>()

        for (i in 0..months.lastIndex) {
            list.add(
                RentalDateModel(
                    id = generateId(i + 1),
                    value = months[i],
                    isAvailable = !isPrime(i),
                    isBooked = i == 1 || i == 3
                )
            )
        }
        return list
    }


}