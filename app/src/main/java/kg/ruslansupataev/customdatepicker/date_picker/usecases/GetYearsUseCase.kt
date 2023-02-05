package kg.ruslansupataev.customdatepicker.date_picker.usecases

import kg.ruslansupataev.customdatepicker.date_picker.generateId
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kotlinx.coroutines.flow.flow

class GetYearsUseCase {

    operator fun invoke(rentalId: Int) = flow {
        emit(getMockedYears())
    }

    private fun getMockedYears(): List<RentalDateModel> {
        val list = mutableListOf<RentalDateModel>()
        for (i in 0..10) {
            list.add(
                RentalDateModel(
                    id = generateId(i + 1),
                    value = (2022 + i).toString(),
                    isAvailable = i in 3..8,
                    isBooked = i == 5
                )
            )
        }
        return list
    }
}