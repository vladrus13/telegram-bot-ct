package ru.vladrus13.itmobot.plugin.practice

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet
import ru.vladrus13.itmobot.plugin.practice.parsers.neerc.NeercParserInfo

class CoroutineJob {
    companion object {
        const val NEERC_JOB = 1L

        fun addTask(jobId: Long, sourceLink: String, tableLink: String, tableId: String, userId: Long) {
            transaction(DataBaseParser.connection) {
                SheetJobTable.insert {
                    it[SheetJobTable.jobId] = jobId
                    it[SheetJobTable.sourceLink] = sourceLink
                    it[SheetJobTable.tableLink] = tableLink
                    it[SheetJobTable.tableId] = tableId
                    it[SheetJobTable.userId] = userId
                }
            }
        }

        fun runTasks() {
            transaction(DataBaseParser.connection) {
                println("1")
                SheetJobTable
                    .selectAll()
                    .forEach { row ->
                        val jobId = row[SheetJobTable.jobId]
                        val sourceLink = row[SheetJobTable.sourceLink]
                        val tableId = row[SheetJobTable.tableId]
                        when (jobId) {
                            NEERC_JOB -> {
                                val actualTasks: List<String> = NeercParserInfo(sourceLink).getTasks()
                                val googleSheet = GoogleSheet(GoogleTableResponse.createSheetsService(), tableId)

                                val currentTasks = googleSheet.getTasksList().flatten()

                                if (currentTasks.isEmpty() && actualTasks.isNotEmpty() || actualTasks.last() != currentTasks.last()) {
                                    googleSheet.generateSheet(
                                        actualTasks.subList(currentTasks.size, actualTasks.size)
                                    )
                                }
                            }
                        }
                    }
                println("2")
            }
        }
    }
}
