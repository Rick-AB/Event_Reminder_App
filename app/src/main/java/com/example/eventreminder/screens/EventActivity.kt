package com.example.eventreminder.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.eventreminder.R
import com.example.eventreminder.database.EventDatabase
import com.example.eventreminder.databinding.ActivityEventBinding
import com.example.eventreminder.model.Event
import com.example.eventreminder.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class EventActivity : AppCompatActivity(), View.OnClickListener {
    private var TAG = "com.example.eventreminder.screens"
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale.US)
    private lateinit var binding: ActivityEventBinding
    private var dayIndex = 0
    private var monthIndex = 0
    private var yearIndex = 0
    private var months = Constants.months
    private var years = Constants.years
    private var days = Constants.days
    private var currentEvent: Event? = null
    private lateinit var db: EventDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event)

        db = EventDatabase.getInstance(this)

        currentEvent =
            if (intent.extras?.getSerializable("event") == null) null else intent.extras?.getSerializable(
                "event"
            ) as Event

        Log.d(TAG, "onCreate: $currentEvent")

        if (currentEvent != null) {
            setDataToViews()
        } else {
            setDefaultValues()
        }
        setListeners()
    }

    private fun setDefaultValues() {
        binding.setEventDayTv.text = days[dayIndex]
        binding.setEventMonthTv.text = months[monthIndex]
        binding.setEventYearTv.text = years[yearIndex]
    }

    private fun setDataToViews() {
        binding.eventTitleEt.setText(currentEvent?.title)
        val day = getDateValue(currentEvent?.date, "day")
        binding.setEventDayTv.text = day
        dayIndex = days.indexOf(day)

        val month = getDateValue(currentEvent?.date, "month")
        binding.setEventMonthTv.text = month
        monthIndex = months.indexOf(month)

        val year = getDateValue(currentEvent?.date, "year")
        binding.setEventYearTv.text = year
        yearIndex = years.indexOf(year)
    }

    private fun getDateValue(date: String?, type: String): String? {
        val arr = date?.split("-")
        val value = when (type) {
            "day" -> arr?.get(0)
            "month" -> arr?.get(1)
            "year" -> arr?.get(2)
            else -> ""
        }
        return value
    }

    private fun setListeners() {
        binding.eventSaveBtn.setOnClickListener { onSaveClick() }
        binding.nextDayImgBtn.setOnClickListener { v -> onClick(v) }
        binding.nextMonthImgBtn.setOnClickListener { v -> onClick(v) }
        binding.nextYearImgBtn.setOnClickListener { v -> onClick(v) }
        binding.prevDayImgBtn.setOnClickListener { v -> onClick(v) }
        binding.prevMonthImgBtn.setOnClickListener { v -> onClick(v) }
        binding.prevYearImgBtn.setOnClickListener { v -> onClick(v) }
        binding.cancelEventImageBtn.setOnClickListener { finish() }
    }

    private fun onSaveClick() {
        if (isValidEventDate()) {
            if (currentEvent != null) {
                updateEvent()
            } else {
                addEvent()
            }
        } else {
            showToast(
                "Umm, seems like the event you're looking forward to is already behind you",
                Toast.LENGTH_LONG
            )
        }

    }

    private fun isValidEventDate(): Boolean {
        val today: LocalDate = LocalDate.now()

        val selectedDay = binding.setEventDayTv.text
        val selectedMonth = binding.setEventMonthTv.text
        val selectedYear = binding.setEventYearTv.text
        val dateString = "$selectedDay-$selectedMonth-$selectedYear"
        val formattedDate = LocalDate.parse(dateString, formatter)

        val diff = Duration.between(today.atStartOfDay(), formattedDate.atStartOfDay())
        return !diff.isNegative
    }

    private fun showToast(message: String, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }

    private fun addEvent() {
        lifecycleScope.launch(Dispatchers.IO) {
            val title = binding.eventTitleEt.text.toString()
            val date = "${days[dayIndex]}-${months[monthIndex]}-${years[yearIndex]}"

            val event = Event(0, title, date)
            db.eventDao().addEvent(event)
        }

        Toast.makeText(this, "Event added", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun updateEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            val title = binding.eventTitleEt.text.toString()
            val date = "${days[dayIndex]}-${months[monthIndex]}-${years[yearIndex]}"
            currentEvent?.title = title
            currentEvent?.date = date

            currentEvent?.let { db.eventDao().updateEvent(it) }
        }

        Toast.makeText(this, "Event updated", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.next_day_img_btn -> {
                increaseDay()
            }
            R.id.prev_day_img_btn -> {
                decreaseDay()
            }
            R.id.next_month_img_btn -> {
                increaseMonth()
            }
            R.id.prev_month_img_btn -> {
                decreaseMonth()
            }
            R.id.next_year_img_btn -> {
                increaseYear()
            }
            R.id.prev_year_img_btn -> {
                decreaseYear()
            }
        }
    }

    private fun increaseDay() {
        if (dayIndex == days.size - 1) {
            dayIndex = 0
        } else {
            dayIndex++
        }
        binding.setEventDayTv.text = days[dayIndex]
    }

    private fun decreaseDay() {
        if (dayIndex == 0) {
            dayIndex = days.size - 1
        } else {
            dayIndex--
        }
        binding.setEventDayTv.text = days[dayIndex]
    }

    private fun increaseMonth() {
        if (monthIndex == months.size - 1) {
            monthIndex = 0
        } else {
            monthIndex++
        }
        binding.setEventMonthTv.text = months[monthIndex]
        updateDaysList()
    }

    private fun decreaseMonth() {
        if (monthIndex == 0) {
            monthIndex = months.size - 1
        } else {
            monthIndex--
        }
        binding.setEventMonthTv.text = months[monthIndex]
        updateDaysList()
    }

    private fun increaseYear() {
        if (yearIndex == years.size - 1) {
            yearIndex = 0
        } else {
            yearIndex++
        }
        binding.setEventYearTv.text = years[yearIndex]
    }

    private fun decreaseYear() {
        if (yearIndex == 0) {
            yearIndex = years.size - 1
        } else {
            yearIndex--
        }
        binding.setEventYearTv.text = years[yearIndex]
    }

    private fun updateDaysList() {
        val endIndex = Constants.monthsToDaysMap.getOrDefault(months[monthIndex], 30)
        if (dayIndex > endIndex) {
            dayIndex = endIndex
        }
        days = Constants.days.subList(0, (endIndex + 1))
        binding.setEventDayTv.text = days[dayIndex]
    }
}