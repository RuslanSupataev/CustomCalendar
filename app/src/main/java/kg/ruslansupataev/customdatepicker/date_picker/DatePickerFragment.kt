package kg.ruslansupataev.customdatepicker.date_picker

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kg.ruslansupataev.customdatepicker.R
import kg.ruslansupataev.customdatepicker.databinding.FragmentDatePickerBinding
import kg.ruslansupataev.customdatepicker.date_picker.models.RentalDateModel
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType
import kg.ruslansupataev.customdatepicker.date_picker.recyclerview.DateAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class DatePickerFragment : Fragment() {

    companion object {
        private const val RENTAL_ID_KEY = "RENTAL_ID_KEY"
        private const val SELECTING_TYPE_KEY = "SELECTING_TYPE_KEY"

        fun getInstance(rentalId: Int, type: SelectingType): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.arguments = bundleOf(
                RENTAL_ID_KEY to rentalId,
                SELECTING_TYPE_KEY to type
            )
            return fragment
        }
    }

    var onClosed: () -> Unit = {}

    private var _binding: FragmentDatePickerBinding? = null
    private val binding: FragmentDatePickerBinding
        get() = _binding!!

    private var rentalId by Delegates.notNull<Int>()
    private lateinit var type: SelectingType
    private lateinit var viewModel: DatePickerViewModel

    private val adapter: DateAdapter by lazy {
        getAdapterInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVarsFromArgs()
        initViewModel()
        initListeners()
        initRv()
    }

    private fun initRv() {
        binding.rvDates.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initListeners() {
        binding.run {
            vDateSection.setOnClickListener {
                viewModel.handleSideEvent(DatePickerEvent.SelectFromScratch(rentalId))
            }
            cvCheckout.setOnClickListener {
                viewModel.handleSideEvent(DatePickerEvent.Checkout)
            }
            ivBack.setOnClickListener {
                adapter.currentRange = null
                viewModel.handleSideEvent(DatePickerEvent.Back)
            }
        }
    }

    private fun onDateClicked(date: RentalDateModel) {
        viewModel.handleSideEvent(DatePickerEvent.SelectDate(date, rentalId))
    }

    private fun getAdapterInstance(): DateAdapter {
        return DateAdapter(
            this::onDateClicked,
            viewModel.state.value.selectedRange,
            type
        )
    }

    private fun finishFragment(isFinished: Boolean) {
        if (isFinished) {
            Toast.makeText(requireContext(), "Fragment is finished", Toast.LENGTH_SHORT).show()
            onClosed()
        }
    }

    private fun setProgress(isProgress: Boolean) {
        binding.progressBar.visibility = if (isProgress) View.VISIBLE else View.GONE
    }

    private fun handleCurrentType() {
        val type = viewModel.state.value.currentCalendarType ?: return
        adapter.currentType = type
        binding.rvDates.post {
            TransitionManager.beginDelayedTransition(binding.rvDates)
            (binding.rvDates.layoutManager as GridLayoutManager).spanCount = when (type) {
                SelectingType.YEARS -> 6
                SelectingType.MONTHS -> 4
                SelectingType.DAYS -> 7
                SelectingType.HOURS -> 6
                SelectingType.MINUTES -> 6
            }
        }
    }

    private fun setSelectedRange(range: Pair<RentalDateModel?, RentalDateModel?>?) {
        if (range == null) return
        val oldRange = adapter.currentRange
        adapter.currentRange = range
        adapter.updateRangeFromOld(oldRange)
        Toast.makeText(requireContext(), "f: ${range.first?.value} s: ${range.second?.value}", Toast.LENGTH_SHORT).show()
    }

    private fun setTitle(title: String?) {
        val visibility = if (title.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.run {
            ivExpandDate.visibility = visibility
            tvDate.visibility = visibility
            vDateSection.visibility = visibility
            tvDate.text = title
        }
    }

    private fun initIsCheckoutAvailable(isAvailable: Boolean) {
        val color: Int
        val checkoutTitle: Int

        if (isAvailable) {
            color = Color.parseColor("#007AFF")
            checkoutTitle = R.string.checkout
        } else {
            color = Color.parseColor("#C4C4C4")
            checkoutTitle = R.string.select_rental_time
        }

        binding.tvCheckoutTitle.setText(checkoutTitle)
        binding.cvCheckout.setCardBackgroundColor(color)
    }

    private fun initCheckoutSectionVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.run {
            clCheckoutSection.visibility = visibility
            vCheckoutDivisionLine.visibility = visibility
        }
    }

    private fun setDates(data: List<RentalDateModel>) {
        adapter.submitList(data)
        handleCurrentType()
    }

    private fun showError(errorMessage: String) {
        if (errorMessage.isNotEmpty()) {
            AlertDialog
                .Builder(requireContext())
                .setMessage(errorMessage)
                .setPositiveButton("ok", null)
                .create()
                .show()
        }
    }

    private fun setSelectedRangeTitle(range: Pair<String?, String?>?) {
        val firstMsg = range?.first
        val secondMsg = range?.second ?: ""

        if (firstMsg != null) {
            binding.tvCheckoutSectionTitle.text = getString(R.string.from_to, firstMsg, secondMsg)
        }
    }

    private fun submitResult(result: Pair<Long, Long>?) {
        if (result != null) {
            val fromTime = getDate(result.first, "dd.MM.yyyy HH:mm")
            val toTime = getDate(result.second, "dd.MM.yyyy HH:mm")

            AlertDialog
                .Builder(requireContext())
                .setTitle("Selected Time")
                .setMessage("from : $fromTime\nto : $toTime")
                .setPositiveButton("ok", null)
                .create()
                .show()
        }
    }

    private fun rippleItems(items: List<RentalDateModel>) {
        if (items.isNotEmpty()) adapter.rippleItems(items)
    }

    private fun setObserves(state: StateFlow<DatePickerState>) {
        state.partialListener(this::finishFragment) { it.isFinished }
        state.partialListener(this::setProgress) { it.isProgress }
        state.partialListener(this::setSelectedRange) { it.selectedRange }
        state.partialListener(this::setTitle) { it.title }
        state.partialListener(this::initIsCheckoutAvailable) { it.isCheckoutAvailable }
        state.partialListener(this::initCheckoutSectionVisibility) { it.checkoutSectionVisibility }
        state.partialListener(this::setDates) { it.dates }
        state.partialListener(this::showError) { it.errorMessage }
        state.partialListener(this::setSelectedRangeTitle) { it.selectedRangeInStrings }
        state.partialListener(this::submitResult) { it.result }
        state.partialListener(this::rippleItems) { it.itemsToRipple }
    }

    private fun initViewModel() {
        // initialize viewModel
        viewModel =
            DatePickerViewModelFactory()
                .create(DatePickerViewModel::class.java)

        // set listeners on out state's changes
        setObserves(viewModel.state)

        // initial commands
        viewModel.handleSideEvent(DatePickerEvent.GotType(type, rentalId))
    }

    private fun initVarsFromArgs() {
        rentalId = arguments?.getInt(RENTAL_ID_KEY)
            ?: throw RuntimeException("rental's id should be provided")
        type = arguments?.getSerializable(SELECTING_TYPE_KEY) as? SelectingType
            ?: throw RuntimeException("selecting type should be provided")
    }

    /**
     * [partialListener] is a extension function on [StateFlow]
     * Using to set some listener on changing some property in the state.
     * high-order function calling in [Dispatchers.Main] thread
     *
     * @param T is property you need to listen
     * @param block is high-order function which is called on change property-you-need
     * @param transform is high-order function which called to figure out which property you need.
     * Within param value which is [DatePickerState] and returning [T] which needs to be property of [DatePickerState]
     */
    private inline fun <T> StateFlow<DatePickerState>.partialListener(
        crossinline block: (T) -> Unit,
        crossinline transform: suspend (value: DatePickerState) -> T
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            map(transform)
                .distinctUntilChanged()
                .collect {
                    block(it)
                }
        }
    }
}