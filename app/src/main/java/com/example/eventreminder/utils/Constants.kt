package com.example.eventreminder.utils

import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Constants {
    companion object {
        val days = listOf(
            "01",
            "02",
            "03",
            "04",
            "05",
            "06",
            "07",
            "08",
            "09",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23",
            "24",
            "25",
            "26",
            "27",
            "28",
            "29",
            "30",
            "31",
        )
        val months = listOf(
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
        val years = listOf("2021", "2022", "2023", "2024", "2025")
        val monthsToDaysMap = hashMapOf(
            Pair("January", 30),
            Pair("February", 27),
            Pair("March", 30),
            Pair("April", 29),
            Pair("May", 30),
            Pair("June", 29),
            Pair("July", 30),
            Pair("August", 30),
            Pair("September", 29),
            Pair("October", 30),
            Pair("November", 29),
            Pair("December", 30)
        )
        val ranges = listOf("One Off Reminder", "Every Day", "Every Week")
        val weekDays =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        fun getDaysLeft(date: String): String {
            val formatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale.US)
            val eventDate = LocalDate.parse(date, formatter)
            val now: LocalDate = LocalDate.now()
            val diff = Duration.between(now.atStartOfDay(), eventDate.atStartOfDay())
            val dateDifference = diff.toDays()

            return dateDifference.toString()
        }
    }
}