package kg.ruslansupataev.customdatepicker.date_picker.usecases

import kg.ruslansupataev.customdatepicker.date_picker.generateId
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kotlinx.coroutines.flow.flow

class GetHoursUseCase {
    private val random = java.util.Random()

    operator fun invoke(
        rentalId: Int,
        yearId: Int,
        monthId: Int,
        dayId: Int
    ) = flow {
        emit(getMockedHours())
    }

    private fun getMockedHours(): List<RentalDateModel> {
        val list = mutableListOf<RentalDateModel>()
        RentalDateModel(
            id = generateId(1),
            value = "${12}AM",
            isAvailable = false,
            isBooked = random.nextInt(4) == 2
        )
        for (i in 1..12) {
            list.add(
                RentalDateModel(
                    id = generateId(i + 1),
                    value = "${i}AM",
                    isAvailable = i > 6,
                    isBooked = random.nextInt(4) == 2
                )
            )
        }
        for (i in 1..11) {
            list.add(
                RentalDateModel(
                    id = generateId(i * 2),
                    value = "${i}PM",
                    isAvailable = i < 5,
                    isBooked = random.nextInt(4) == 2
                )
            )
        }
        return list
    }

}