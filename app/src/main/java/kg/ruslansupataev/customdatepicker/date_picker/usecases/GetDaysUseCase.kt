package kg.ruslansupataev.customdatepicker.date_picker.usecases

import kg.ruslansupataev.customdatepicker.date_picker.generateId
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kotlinx.coroutines.flow.flow

class GetDaysUseCase {
    private val random = java.util.Random()

    operator fun invoke(rentalId: Int, yearId: Int, monthId: Int) = flow {
        val list = mutableListOf<RentalDateModel>()
        val titles = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
        titles.forEach {
            list.add(RentalDateModel(0, it, isAvailable = false, isBooked = false))
        }
        list.addAll(getMockedDays())

        emit(list)
    }

    private fun getMockedDays(): List<RentalDateModel> {
        val list = mutableListOf<RentalDateModel>()

        for (i in 1..randomRange()) {
            list.add(
                RentalDateModel(
                    id = generateId(i),
                    value = "$i",
                    isAvailable = !isWeekend(i),
                    isBooked = random.nextInt(4) == 2
                )
            )
        }
        return list
    }

    private fun randomRange(): Int {
        val randomNum = random.nextInt(3)
        return 29 + randomNum
    }

    private fun isWeekend(someInt: Int): Boolean {
        val dayOfWeek = (someInt - 1) % 7 + 1
        return dayOfWeek == 6 || dayOfWeek == 7
    }

}