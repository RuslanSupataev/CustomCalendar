package kg.ruslansupataev.customdatepicker.date_picker.recyclerview

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import kg.ruslansupataev.customdatepicker.databinding.ItemDateBinding
import kg.ruslansupataev.customdatepicker.date_picker.animateRippleEffect
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType
import kg.ruslansupataev.customdatepicker.date_picker.models.compareTo

class DateViewHolder(
    private val binding: ItemDateBinding,
    private val onClick: (date: RentalDateModel) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(
        date: RentalDateModel,
        selectedRange: Pair<RentalDateModel?, RentalDateModel?>,
        type: SelectingType,
        shouldRipple: Boolean
    ) {
        binding.run {
            if (shouldRipple) root.animateRippleEffect()
            tvDate.text =
                if (type == SelectingType.HOURS
                    && ":" !in date.value
                    && "AM" !in date.value
                    && "PM" !in date.value
                ) "${date.value}:00"
                else date.value

            root.setOnClickListener {
                if (date.isBooked) it.animateRippleEffect()
                else if (date.isAvailable) onClick(date)
            }
        }
        if (!trySetAsFirstInRange(date, selectedRange.first)) {
            trySetAsInRange(date, selectedRange.first, selectedRange.second, type)
        }
    }

    private fun trySetAsInRange(
        date: RentalDateModel,
        firstInRange: RentalDateModel?,
        secondInRange: RentalDateModel?,
        type: SelectingType
    ) {
        binding.cvContainer.setCardBackgroundColor(
            if (firstInRange != null
                && secondInRange != null
                && date.compareTo(firstInRange, type)
                && (secondInRange.compareTo(date, type) || date == secondInRange)
            ) {
                binding.tvDate.setTextColor(Color.WHITE)
                Color.parseColor("#007AFF")
            } else {
                binding.tvDate.setTextColor(getTextColor(date))
                Color.TRANSPARENT
            }
        )
    }

    private fun trySetAsFirstInRange(
        currentDate: RentalDateModel,
        firstInSelectedRange: RentalDateModel?
    ): Boolean {
        return if (firstInSelectedRange != null
            && currentDate.id == firstInSelectedRange.id
            && currentDate.value == firstInSelectedRange.value
        ) {
            binding.tvDate.setTextColor(Color.WHITE)
            binding.cvContainer.setCardBackgroundColor(Color.parseColor("#007AFF"))
            true
        } else {
            false
        }
    }

    private fun getTextColor(date: RentalDateModel): Int {
        return Color.parseColor(
            if (!date.isAvailable) "#818C99"
            else if (date.isBooked) "#D72C20"
            else "#34A853"
        )

    }

}