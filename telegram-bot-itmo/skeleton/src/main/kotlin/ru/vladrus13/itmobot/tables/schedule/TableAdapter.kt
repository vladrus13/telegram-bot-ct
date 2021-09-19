package ru.vladrus13.itmobot.tables.schedule

import ru.vladrus13.itmobot.bean.UsersSubjects
import kotlin.math.max

class TableAdapter {
    companion object {
        private fun adapt(
            lesson: ScheduleTable.TableLesson?,
            lessons: ArrayList<ScheduleTable.TableLesson>,
            usersSubjects: UsersSubjects?
        ): ScheduleTable.TableLesson? {
            if (lesson != null && !lesson.isBlank() && (usersSubjects != null && !usersSubjects.exclude.contains(lesson.name))) {
                return lesson
            } else {
                for (it in lessons) {
                    if (usersSubjects != null && usersSubjects.include.contains(it.name)) {
                        return it
                    }
                }
            }
            return null
        }

        private fun adapt(
            pair: ScheduleTable.TablePair?,
            pairs: ArrayList<ScheduleTable.TablePair>,
            usersSubjects: UsersSubjects?
        ): ScheduleTable.TablePair? {
            val newPair: ScheduleTable.TablePair = pair ?: ScheduleTable.TablePair(null, null)
            newPair.odd = adapt(newPair.odd, ArrayList(pairs.mapNotNull { e -> e.odd }), usersSubjects)
            newPair.even = adapt(newPair.even, ArrayList(pairs.mapNotNull { e -> e.even }), usersSubjects)
            return if (newPair.isBlank()) {
                null
            } else {
                newPair
            }
        }

        private fun adapt(
            day: ScheduleTable.TableDay?,
            days: ArrayList<ScheduleTable.TableDay>,
            usersSubjects: UsersSubjects?
        ): ScheduleTable.TableDay {
            val maxSize =
                max(day?.pairs?.size ?: 0, days.stream().mapToInt { dayIt -> dayIt.pairs.size }.max().orElse(0))
            val newDay = ScheduleTable.TableDay(ArrayList(MutableList(maxSize) { null }))
            if (day != null) {
                for (i in day.pairs.indices) {
                    newDay.pairs[i] = day.pairs[i]
                }
            }
            for (i in 0 until maxSize) {
                newDay.pairs[i] = adapt(
                    newDay.pairs[i],
                    ArrayList(days.mapNotNull { e -> if (e.pairs.size > i) e.pairs[i] else null }),
                    usersSubjects
                )
            }
            return newDay
        }

        fun adapt(
            group: ScheduleTable.TableGroup?,
            groups: ArrayList<ScheduleTable.TableGroup>,
            usersSubjects: UsersSubjects?
        ): ScheduleTable.TableGroup {
            val days: ArrayList<ScheduleTable.TableDay> = ArrayList()
            for (i in 0 until 7) {
                days.add(
                    adapt(
                        if (group == null) null else if (group.days.size > i) group.days[i] else null,
                        ArrayList(groups.mapNotNull { g -> if (g.days.size > i) g.days[i] else null }),
                        usersSubjects
                    )
                )
            }
            return ScheduleTable.TableGroup(days)
        }
    }
}