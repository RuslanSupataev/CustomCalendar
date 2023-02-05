package kg.ruslansupataev.customdatepicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import kg.ruslansupataev.customdatepicker.databinding.ActivityMainBinding
import kg.ruslansupataev.customdatepicker.date_picker.DatePickerFragment
import kg.ruslansupataev.customdatepicker.date_picker.getRandomSelectingType
import kg.ruslansupataev.customdatepicker.date_picker.models.SelectingType
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedType: SelectingType = SelectingType.YEARS
    private var fragment: DatePickerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Locale.setDefault(Locale("en","US"))
        selectedType = getRandomSelectingType()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setNewType(selectedType)
        initContentButtons()
        initStartButton()
    }

    private fun initStartButton() {
        binding.btnStart.setOnClickListener {
            binding.content.isVisible = false
            binding.frameLayout.isVisible = true

            initFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.frame_layout, fragment!!)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fragmentClosed() {
        fragment?.let {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()

            fragment = null
        }
        binding.content.isVisible = true
        binding.frameLayout.isVisible = false
    }

    private fun initFragment() {
        fragment = DatePickerFragment.getInstance(9292929, selectedType)
        fragment?.onClosed = this::fragmentClosed
    }

    private fun initContentButtons() {
        binding.run {
            checkboxYear.setOnClickListener { setNewType(SelectingType.YEARS) }
            checkboxMonth.setOnClickListener { setNewType(SelectingType.MONTHS) }
            checkboxDays.setOnClickListener { setNewType(SelectingType.DAYS) }
            checkboxHours.setOnClickListener { setNewType(SelectingType.HOURS) }
            checkboxMinutes.setOnClickListener { setNewType(SelectingType.MINUTES) }
        }
    }

    private fun setNewType(type: SelectingType) {
        selectedType = type
        binding.run {
            checkboxYear.isChecked = type == SelectingType.YEARS
            checkboxMonth.isChecked = type == SelectingType.MONTHS
            checkboxDays.isChecked = type == SelectingType.DAYS
            checkboxHours.isChecked = type == SelectingType.HOURS
            checkboxMinutes.isChecked = type == SelectingType.MINUTES
        }
    }
}