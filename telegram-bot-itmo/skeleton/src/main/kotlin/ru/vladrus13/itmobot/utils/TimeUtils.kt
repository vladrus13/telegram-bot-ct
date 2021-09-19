package ru.vladrus13.itmobot.utils

import java.text.SimpleDateFormat
import java.util.*


class TimeUtils {
    companion object {

        fun getCurrentTime(): GregorianCalendar = GregorianCalendar()

        private fun getSimpleDateFormat(): SimpleDateFormat = SimpleDateFormat("EEEE, dd MMMM, HH:mm:ss")

        fun getCurrentTimeString(): String {
            val time = getCurrentTime()
            val timeFormat = getSimpleDateFormat()
            return timeFormat.format(time.time)
        }

        fun getTimeString(time: Date): String {
            val timeFormat = getSimpleDateFormat()
            return timeFormat.format(time)
        }

        fun getDay(skip: Int = 0): Int = (Calendar.getInstance()[Calendar.DAY_OF_WEEK] + skip + 5) % 7

        val days = arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

        val englishDays = arrayOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
        fun getDayByName(real: String): Int {
            days.forEachIndexed { i, s -> if (real == s) return i }
            return -1
        }

        fun getEnglishDayByName(real: String): Int {
            englishDays.forEachIndexed { i, s -> if (real == s) return i }
            return -1
        }

        fun getNameByDay(i: Int): String? = if (i < days.size) days[i] else null

        fun getEnglishNameByDay(i: Int): String? = if (i < englishDays.size) englishDays[i] else null
    }
}