package ru.vladrus13.itmobot.utils

import java.text.SimpleDateFormat
import java.util.*


class TimeUtils {
    companion object {

        fun getCurrentTime(): GregorianCalendar = GregorianCalendar()

        private fun getSimpleDateFormat(): SimpleDateFormat =
            SimpleDateFormat("EEEE, dd MMMM, HH:mm:ss")

        fun getCurrentTimeString(): String {
            val time = getCurrentTime()
            val timeFormat = getSimpleDateFormat()
            return timeFormat.format(time.time)
        }

        fun getTimeString(time: Date): String {
            val timeFormat = getSimpleDateFormat()
            return timeFormat.format(time)
        }

        fun getDay(skip: Int = 0): Int =
            (Calendar.getInstance()[Calendar.DAY_OF_WEEK] + skip + 5) % 7

    }
}