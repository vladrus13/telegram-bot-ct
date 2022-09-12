package ru.vladrus13.itmobot.tables.schedule

import ru.vladrus13.itmobot.bean.Chatted
import ru.vladrus13.itmobot.bean.UsersSubjects
import ru.vladrus13.itmobot.utils.TimeUtils
import java.util.*

class ScheduleTable {
    class Skip(var startRow: Int, val startColumn: Int, var sizeRow: Int, val sizeColumn: Int, val text: String) {
        fun down() {
            startRow++
            sizeRow--
        }
    }

    class Table(
        private val time: Date
    ) {

        private val groups: HashMap<String, TableGroup> = HashMap()
        private val subjects: HashSet<String> = HashSet()

        constructor(groups: HashMap<String, TableGroup>, time: Date) : this(time) {
            this.groups.putAll(groups)
        }

        constructor(table: MutableList<MutableList<String>>, time: Date) : this(time) {
            var lastGroup: String? = null
            var lastPosition = -1
            var lastNotBlank = -1
            for (i in table[0].indices) {
                if (table[0][i].isNotBlank()) {
                    if (lastGroup == null) {
                        lastPosition = i
                        lastGroup = table[0][i]
                    } else {
                        if (lastGroup != table[0][i]) {
                            // there is new group
                            groups[lastGroup] = TableGroup(table, lastPosition, lastNotBlank)
                            lastGroup = table[0][i]
                            lastPosition = i
                        }
                    }
                    lastNotBlank = i
                }
            }
            if (!lastGroup.isNullOrBlank() && !groups.containsKey(lastGroup)) {
                groups[lastGroup] = TableGroup(table, lastPosition, lastNotBlank)
            }
            for (group in groups.values) {
                subjects.addAll(group.getSubjects())
            }
        }

        fun toStringBuilder(
            user: Chatted,
            usersSubjects: UsersSubjects? = null,
            day: Int? = null
        ): StringBuilder {
            if (user.group != null) {
                if (!groups.containsKey(user.group)) {
                    return StringBuilder("Ваша группа не найдена :(")
                }
                val tableGroup = TableAdapter.adapt(groups[user.group], ArrayList(this.groups.values), usersSubjects)
                val time = "Последнее время обновления: ${TimeUtils.getTimeString(time)}\n"
                val sb = StringBuilder(time)
                sb.append(
                    if (day == null) {
                        tableGroup.toStringBuilder(user)
                    } else {
                        tableGroup.toStringBuilder(user, day)
                    }
                )
                return sb
            } else {
                val groups: Collection<TableGroup> =
                    this.groups.entries.sortedBy { it.key }.map { o1 -> o1.value }
                val time = "Последнее время обновления: ${TimeUtils.getTimeString(time)}\n"
                val sb = StringBuilder(time)
                for (it in groups) {
                    if (day == null) {
                        sb.append(it.toStringBuilder(user))
                    } else {
                        sb.append(it.toStringBuilder(user, day))
                    }
                }
                return sb
            }
        }

        fun getGroups(): List<String> {
            return groups.keys.toList()
        }

        fun getSubjects(): List<String> {
            return subjects.sorted()
        }
    }


    class TableGroup() {
        val days: ArrayList<TableDay> = ArrayList()

        constructor(groups: ArrayList<TableDay>) : this() {
            this.days.addAll(groups)
        }

        constructor(table: MutableList<MutableList<String>>, startColumn: Int, finishColumn: Int) : this() {
            var lastDay: String? = null
            var lastPosition = -1
            var lastNotBlank = -1
            for (i in table.indices) {
                if (table[i][0].isNotBlank()) {
                    if (lastDay == null) {
                        lastDay = table[i][0]
                        lastPosition = i
                    } else {
                        if (lastDay != table[i][0]) {
                            days.add(TableDay(table, startColumn, finishColumn, lastPosition, lastNotBlank))
                            lastDay = table[i][0]
                            lastPosition = i
                        }
                    }
                    lastNotBlank = i
                }
            }
            days.add(TableDay(table, startColumn, finishColumn, lastPosition, lastNotBlank))
        }

        fun toStringBuilder(user: Chatted): StringBuilder {
            val sb = StringBuilder()
            for (i in days.indices) {
                sb.append(daysNames[i]).append(":\n").append(days[i].toStringBuilder(user))
                    .append("\n")
            }
            return sb
        }

        fun toStringBuilder(user: Chatted, day: Int): StringBuilder {
            return days[day].toStringBuilder(user)
        }

        private val daysNames = arrayOf(
            "Понедельник",
            "Вторник",
            "\uD83D\uDC38Wednesday",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"
        )

        fun getSubjects(): Set<String> {
            val all = HashSet<String>()
            for (it in days) {
                all.addAll(it.getSubjects())
            }
            return all
        }
    }


    class TableDay() {
        val pairs: ArrayList<TablePair?> = ArrayList()

        constructor(pairs: ArrayList<TablePair?>) : this() {
            this.pairs.addAll(pairs)
        }

        constructor(
            table: MutableList<MutableList<String>>,
            startColumn: Int,
            finishColumn: Int,
            startRow: Int,
            finishRow: Int
        ) : this() {
            for (i in startRow..finishRow step 2) {
                pairs.add(TablePair(table, startColumn, i))
            }
            for (i in pairs.indices) {
                if (pairs[i]!!.isBlank()) {
                    pairs[i] = null
                }
            }
            while (pairs.size > 0 && pairs.last() == null) {
                pairs.removeLast()
            }
        }

        private fun isBlank(): Boolean {
            return pairs.isEmpty()
        }

        fun toStringBuilder(user: Chatted): StringBuilder {
            return if (!isBlank()) {
                val sb = StringBuilder()
                var isOk = false
                for (i in pairs.indices) {
                    if (pairs[i] != null) {
                        val time = if (user.settings.isShowEnd) timesStart[i] + "-" + timesEnd[i] else timesStart[i]
                        val n = pairs[i]!!.toStringBuilder(user, time)
                        if (n != null) {
                            sb.append(n).append("\n")
                            isOk = true
                        }
                    }
                }
                if (!isOk) {
                    StringBuilder("Пустой день!\n")
                } else {
                    sb
                }
            } else {
                StringBuilder("Пустой день!\n")
            }
        }

        private val timesStart = arrayOf(
            "8:20",
            "10:00",
            "11:40",
            "13:30",
            "15:20",
            "17:00",
            "18:40",
            "20:20"

        )

        private val timesEnd = arrayOf(
            "9:50",
            "11:30",
            "13:10",
            "15:00",
            "16:50",
            "18:30",
            "20:10",
            "21:50"

        )

        fun getSubjects(): Set<String> {
            val all = HashSet<String>()
            for (it in pairs) {
                if (it != null) {
                    all.addAll(it.getSubjects())
                }
            }
            return all
        }
    }


    class TablePair() {
        var even: TableLesson? = null
        var odd: TableLesson? = null

        constructor(even: TableLesson?, odd: TableLesson?) : this() {
            this.even = even
            this.odd = odd
        }

        constructor(table: MutableList<MutableList<String>>, startColumn: Int, startRow: Int) : this() {
            odd = TableLesson(table, startColumn, startRow)
            even = TableLesson(table, startColumn, startRow + 1)
            if (odd!!.isBlank()) {
                odd = null
            }
            if (even!!.isBlank()) {
                even = null
            }
        }

        fun isBlank(): Boolean {
            return even == null && odd == null
        }

        fun toStringBuilder(user: Chatted, time: String): StringBuilder? {
            return if (even == odd) {
                even!!.toStringBuilder(user, time, null)
            } else {
                val sb = StringBuilder()
                var isOk = false
                if (even != null) {
                    val n = even!!.toStringBuilder(user, time, false)
                    if (n != null) {
                        sb.append(n)
                        if (odd != null) {
                            sb.append("\n")
                        }
                        isOk = true
                    }
                }
                if (odd != null) {
                    val n = odd!!.toStringBuilder(user, time, true)
                    if (n != null) {
                        sb.append(n)
                        isOk = true
                    }
                }
                return if (!isOk) {
                    null
                } else {
                    sb
                }
            }
        }

        fun getSubjects(): Set<String> {
            return if (odd == null && even == null) {
                hashSetOf()
            } else {
                if (odd == null) {
                    even!!.getSubjects()
                } else {
                    if (even == null) {
                        odd!!.getSubjects()
                    } else {
                        even!!.getSubjects().plus(odd!!.getSubjects())
                    }
                }
            }
        }
    }


    class TableLesson() {

        var name: String = ""
        var place: String = ""
        var teacher: String = ""
        var type: String = ""

        constructor(table: MutableList<MutableList<String>>, startColumn: Int, startRow: Int) : this() {
            name = table[startRow][startColumn]
            type = table[startRow][startColumn + 1]
            place = table[startRow][startColumn + 2]
            teacher = table[startRow][startColumn + 3]
        }

        fun isBlank(): Boolean {
            return name.isBlank() && place.isBlank() && teacher.isBlank() && type.isBlank()
        }

        fun toStringBuilder(
            user: Chatted,
            time: String,
            isOdd: Boolean?
        ): StringBuilder? {
            return if (isBlank()) {
                null
            } else {
                val sb = StringBuilder()
                val week = if (isOdd == null) "" else if (isOdd == true) " (нечет)" else " (чет)"
                sb.append(time).append(week).append(":").append(if (user.settings.isCompact) ' ' else "\n")
                sb.append(name).append("-").append(type).append(" ").append(place)
                    .append(if (user.settings.isShowTeacher) " $teacher" else "")
                sb
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TableLesson

            if (name != other.name) return false
            if (place != other.place) return false
            if (teacher != other.teacher) return false
            if (type != other.type) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + place.hashCode()
            result = 31 * result + teacher.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }

        fun getSubjects(): HashSet<String> {
            return hashSetOf(name)
        }
    }
}