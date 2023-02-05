package kg.ruslansupataev.customdatepicker.date_picker.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import kg.ruslansupataev.customdatepicker.databinding.ItemDateBinding
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType

class DateAdapter(
    private val onClick: (date: RentalDateModel) -> Unit,
    range: Pair<RentalDateModel?, RentalDateModel?>?,
    type: SelectingType
) : ListAdapter<RentalDateModel, DateViewHolder>(
    object : DiffUtil.ItemCallback<RentalDateModel>() {
        override fun areItemsTheSame(oldItem: RentalDateModel, newItem: RentalDateModel): Boolean =
            oldItem.id == newItem.id && oldItem.value == newItem.value

        override fun areContentsTheSame(
            oldItem: RentalDateModel,
            newItem: RentalDateModel
        ): Boolean = oldItem == newItem
    }
) {
    private var itemsToRipple: MutableList<RentalDateModel> = mutableListOf()
    var currentRange: Pair<RentalDateModel?, RentalDateModel?>? = range
    var currentType: SelectingType = type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DateViewHolder(
        ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onClick
    )

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = getItem(position)
        var shouldRipple = false
        for (i in 0..itemsToRipple.lastIndex) {
            if (date.id == itemsToRipple[i].id) {
                shouldRipple = true
                itemsToRipple = itemsToRipple.also {
                    it.removeAt(i)
                }
                break
            }
        }

        holder.onBind(
            date = date,
            selectedRange = currentRange ?: Pair(null, null),
            type = currentType,
            shouldRipple = shouldRipple
        )
    }

    fun updateRangeFromOld(
        old: Pair<RentalDateModel?, RentalDateModel?>?,
    ) {
        val modifiedIndexes = mutableSetOf<Int>()

        val oldLeftEdge = if (old?.first == null) 0
        else currentList.indexOf(old.first)

        val oldEndEdge = if (old?.second == null) oldLeftEdge
        else currentList.indexOf(old.second)

        val newLeftEdge = if (currentRange?.first == null) 0
        else currentList.indexOf(currentRange!!.first)

        val newEndEdge = if (currentRange?.second == null) newLeftEdge
        else currentList.indexOf(currentRange!!.second)

        val leftDifference = getModifiedRange(oldLeftEdge, newLeftEdge)
        val endDifference = getModifiedRange(oldEndEdge, newEndEdge)

        modifiedIndexes.addAll(leftDifference.first..leftDifference.second)
        modifiedIndexes.addAll(endDifference.first..endDifference.second)

        modifiedIndexes.forEach {
            notifyItemChanged(it)
        }
    }

    private fun getModifiedRange(x1: Int, x2: Int): Pair<Int, Int> {
        val i = x1 - x2
        return if (i > 0) Pair(x2, x2 + i)
        else Pair(x2 + i, x2)
    }

    fun rippleItems(data: List<RentalDateModel>) {
        if (data.isNotEmpty()) {
            itemsToRipple.addAll(data)
            val firstIndex = currentList.indexOf(data.first())
            val lastIndex = currentList.indexOf(data.last())
            for (i in firstIndex..lastIndex) {
                notifyItemChanged(i)
            }
        }
    }
}