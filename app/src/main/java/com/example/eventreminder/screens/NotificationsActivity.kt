package com.example.eventreminder.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.*
import com.example.eventreminder.R
import com.example.eventreminder.databinding.ActivityNotificationsBinding
import com.example.eventreminder.model.Event
import com.example.eventreminder.utils.Constants
import com.example.eventreminder.workmanager.NotificationWorker
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private val PREFERENCE_NAME = "EVENT_SHAREDPREFERENCE"
    private var currentEvent: Event? = null
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale.US)
    private val ranges = Constants.ranges
    private val weekDays = Constants.weekDays
    private val days = Constants.days
    private var rangesIndex = 0
    private var weekDayIndex = 0
    private var daysIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications)

        val sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)

        currentEvent =
            if (intent.extras?.getSerializable("event") == null) null else intent.extras?.getSerializable(
                "event"
            ) as Event

        val isChecked = sharedPreferences.getBoolean(currentEvent?.uid.toString(), false)
        binding.notificationSwitch.isChecked = isChecked

        setDefaultValues()
        setListeners()
    }

    private fun setDefaultValues() {
        binding.notificationDayTv.text = days[daysIndex]
        binding.notificationWeekdayTv.text = weekDays[weekDayIndex]
        binding.notificationRangeTv.text = ranges[rangesIndex]
    }

    private fun setListeners() {
        binding.notificationSaveBtn.setOnClickListener { saveButtonClick() }
        binding.prevRangeImgBtn.setOnClickListener { decreaseRange() }
        binding.nextRangeImgBtn.setOnClickListener { increaseRange() }
        binding.prevWeekdayImgBtn.setOnClickListener { decreaseWeekday() }
        binding.nextWeekdayImgBtn.setOnClickListener { increaseWeekday() }
        binding.prevDayImgBtn.setOnClickListener { decreaseDay() }
        binding.nextDayImgBtn.setOnClickListener { increaseDay() }
        binding.cancelNotificationImageBtn.setOnClickListener { finish() }
    }

    private fun decreaseRange() {
        if (rangesIndex == 0) {
            rangesIndex = ranges.size - 1
        } else {
            rangesIndex--
        }

        if (rangesIndex == 0) {
            enableDates()
        } else {
            disableDates()
        }
        binding.notificationRangeTv.text = ranges[rangesIndex]
    }

    private fun decreaseWeekday() {
        if (rangesIndex == 0) {
            if (weekDayIndex == 0) {
                weekDayIndex = weekDays.size - 1
            } else {
                weekDayIndex--
            }
            binding.notificationWeekdayTv.text = weekDays[weekDayIndex]
        }
    }

    private fun decreaseDay() {
        if (rangesIndex == 0) {
            if (daysIndex == 0) {
                daysIndex = days.size - 1
            } else {
                daysIndex--
            }
            binding.notificationDayTv.text = days[daysIndex]
        }
    }

    private fun increaseRange() {
        if (rangesIndex == ranges.size - 1) {
            rangesIndex = 0
        } else {
            rangesIndex++
        }

        if (rangesIndex == 0) {
            enableDates()
        } else {
            disableDates()
        }
        binding.notificationRangeTv.text = ranges[rangesIndex]
    }

    private fun increaseWeekday() {
        if (rangesIndex == 0) {
            if (weekDayIndex == weekDays.size - 1) {
                weekDayIndex = 0
            } else {
                weekDayIndex++
            }
            binding.notificationWeekdayTv.text = weekDays[weekDayIndex]
        }
    }

    private fun increaseDay() {
        if (rangesIndex == 0) {
            if (daysIndex == days.size - 1) {
                daysIndex = 0
            } else {
                daysIndex++
            }
            binding.notificationDayTv.text = days[daysIndex]
        }
    }

    private fun disableDates() {
        binding.notificationWeekdayTv.isEnabled = false
        binding.notificationDayTv.isEnabled = false
        binding.prevDayImgBtn.isEnabled = false
        binding.nextDayImgBtn.isEnabled = false
        binding.prevWeekdayImgBtn.isEnabled = false
        binding.nextWeekdayImgBtn.isEnabled = false
        binding.prevDayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_left_disabled)
        binding.nextDayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_right_disabled)
        binding.prevWeekdayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_left_disabled)
        binding.nextWeekdayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_right_disabled)

    }

    private fun enableDates() {
        binding.notificationWeekdayTv.isEnabled = true
        binding.notificationDayTv.isEnabled = true
        binding.prevDayImgBtn.isEnabled = true
        binding.nextDayImgBtn.isEnabled = true
        binding.prevWeekdayImgBtn.isEnabled = true
        binding.nextWeekdayImgBtn.isEnabled = true
        binding.prevDayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_left)
        binding.nextDayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_right)
        binding.prevWeekdayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_left)
        binding.nextWeekdayImgBtn.setImageResource(R.drawable.ic_baseline_chevron_right)
    }

    private fun saveButtonClick() {

        val isNotificationEnabled = binding.notificationSwitch.isChecked
        val editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit()
        editor.putBoolean(currentEvent?.uid.toString(), isNotificationEnabled)
        editor.apply()

        if (isNotificationEnabled) {
            if (!isValidDate()) {
                showToast(
                    "Sorry, we can't time travel to the past. Please set a valid date",
                    Toast.LENGTH_SHORT
                )
                return
            }

            if (isEventExpiredForSelectedDate()) {
                showToast(
                    """The event "${currentEvent?.title}" would have occurred for the selected date """,
                    Toast.LENGTH_SHORT
                )
                return
            }

            showToast("Notification scheduled", Toast.LENGTH_LONG)
            scheduleNotification()
        } else {
            cancelNotification()
        }

        finish()
    }

    private fun showToast(message: String, duration: Int) {
        Toast.makeText(
            this,
            message,
            duration
        ).show()
    }

    private fun isValidDate(): Boolean {
        val currentDay = LocalDate.now().dayOfMonth
        val selectedDay = binding.notificationDayTv.text.toString()

        return selectedDay.toInt() <= currentDay
    }

    private fun scheduleNotification() {
        cancelNotification()

        val data = Data.Builder()
            .putString("event_title", currentEvent?.title)
            .putString("event_date", currentEvent?.date)
            .putInt("id", currentEvent?.uid!!)
            .build()

        val scheduleNotification: WorkRequest = if (rangesIndex == 0) {
            val initialDelay = getInitialDelayForOneTimeWork()
            OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(initialDelay, TimeUnit.HOURS)
                .addTag(currentEvent?.uid.toString())
                .setInputData(data)
                .build()

        } else {
            val initialDelay = getInitialDelayForPeriodicWork()
            PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.HOURS)
                .addTag(currentEvent?.uid.toString())
                .setInputData(data)
                .build()
        }



        WorkManager.getInstance(this).enqueue(scheduleNotification)
    }

    private fun getInitialDelayForOneTimeWork(): Long {
        val currentYear = LocalDate.now().year
        val currentMonth = LocalDate.now().month
        val currentDay = binding.notificationDayTv.text.toString()

        val dateString = "$currentDay-$currentMonth-$currentYear"
        val formattedDate = LocalDate.parse(dateString, formatter)
        val diff = Duration.between(LocalDate.now().atStartOfDay(), formattedDate.atStartOfDay())
            .plusHours(12)

        return diff.toHours()
    }

    private fun getInitialDelayForPeriodicWork(): Long {
        var initialDelay: Long = 0
        if (rangesIndex == 1) {
            val tomorrow = LocalDate.now().plusDays(1)
            val today = LocalDate.now()
            val diff = Duration.between(today.atStartOfDay(), tomorrow.atStartOfDay()).plusHours(12)

            initialDelay = diff.toHours()
        } else if (rangesIndex == 2) {
            val oneWeekFromNow = LocalDate.now().plusDays(7)
            val today = LocalDate.now()
            val diff =
                Duration.between(today.atStartOfDay(), oneWeekFromNow.atStartOfDay()).plusHours(12)

            initialDelay = diff.toHours()
        }

        return initialDelay
    }

    private fun isEventExpiredForSelectedDate(): Boolean {
        var expired = false
        val eventDate = currentEvent?.date
        val formattedEventDate = LocalDate.parse(eventDate, formatter)

        when (rangesIndex) {
            0 -> {
                val year = LocalDate.now().year
                val month = LocalDate.now().month
                val day = binding.notificationDayTv.text.toString()
                val dateString = "$day-$month-$year"
                val formattedDate = LocalDate.parse(dateString, formatter)

                val diff = Duration.between(
                    formattedDate.atStartOfDay(),
                    formattedEventDate.atStartOfDay()
                )
                expired = diff.isNegative

            }
            1 -> {
                val tomorrow = LocalDate.now().plusDays(1)

                val diff =
                    Duration.between(tomorrow.atStartOfDay(), formattedEventDate.atStartOfDay())
                expired = diff.isNegative

            }
            2 -> {
                val oneWeekFromNow = LocalDate.now().plusDays(7)

                val diff = Duration.between(
                    oneWeekFromNow.atStartOfDay(),
                    formattedEventDate.atStartOfDay()
                )
                expired = diff.isNegative
            }
        }

        return expired
    }

    private fun cancelNotification() {
        WorkManager.getInstance(this).cancelAllWorkByTag(currentEvent?.uid.toString())
    }

    private fun testNotification() {
        val data = Data.Builder()
            .putString("event_title", currentEvent?.title)
            .putString("event_date", currentEvent?.date)
            .putInt("id", currentEvent?.uid!!)
            .build()

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(request)
        finish()
    }
}