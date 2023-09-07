package ru.vladrus13.itmobot.plugin.practice

import org.jetbrains.exposed.sql.selectAll
import ru.vladrus13.itmobot.parallel.parsers.neerc.NeercParserInfo

class CoroutineThreadOverseer {
    companion object {
        private const val NEERC_JOB = 1L

        fun addTask(link: String, user_id: Long) {
        }

        fun runTasks() {
            SheetJobTable
                .selectAll()
                .forEach { row ->
                    when (row[SheetJobTable.jobId]) {
                        NEERC_JOB -> {
                            val actualTasks: List<String> = NeercParserInfo(row[SheetJobTable.link]).getTasks()
                            val currentTasks = googleSheet.getTasksList().flatten()

                            if (currentTasks.isEmpty() && actualTasks.isNotEmpty() || actualTasks.last() != currentTasks.last()) {
                                googleSheet.generateSheet(
                                    actualTasks.subList(currentTasks.size, actualTasks.size)
                                )
                            }
                        }
                    }
                }
        }
    }
}