package ru.vladrus13.itmobot.plugin.practice

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet
import ru.vladrus13.itmobot.plugin.practice.parsers.neerc.NeercParserInfo
import ru.vladrus13.itmobot.plugin.practice.transfer.TransferData.Companion.transferFCSToLastName
import ru.vladrus13.itmobot.plugin.practice.transfer.TransferData.Companion.transferStudentTableToTeacher
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.utils.Messager
import java.util.Objects.deepEquals
import java.util.logging.Logger

class CoroutineJob {
    companion object {
        private val logger: Logger = InitialProperties.logger

        const val NEERC_JOB = 1L

        fun addTask(jobId: Long, sourceLink: String, tableLink: String, tableId: String, userId: Long) {
            transaction(DataBaseParser.connection) {
                SheetJobTable.insert {
                    it[SheetJobTable.jobId] = jobId
                    it[SheetJobTable.sourceLink] = sourceLink
                    it[SheetJobTable.tableLink] = tableLink
                    it[SheetJobTable.tableId] = tableId
                    it[chatId] = userId
                }
            }
        }

        fun runTasks(): Boolean {
            var allSheetTables: List<ResultRow> = emptyList()
            transaction(DataBaseParser.connection) {
                allSheetTables = SheetJobTable
                    .selectAll()
                    .toList()
                    .sortedBy { it[SheetJobTable.id].toInt() }
            }
            for (rw in allSheetTables) {
                if (!runTask(rw)) {
                    return false
                }
            }
            return true
        }

        private fun runTask(row: ResultRow): Boolean {
            for (i in 1..RETRY_COUNT) {
                val id = row[SheetJobTable.id]
                val jobId = row[SheetJobTable.jobId]
                val sourceLink = row[SheetJobTable.sourceLink]
                val tableLink = row[SheetJobTable.tableLink]
                val tableId = row[SheetJobTable.tableId]
                val chatId = row[SheetJobTable.chatId]
                try {
                    when (jobId) {
                        NEERC_JOB -> runNeercTask(id, sourceLink, tableId, tableLink)
                    }
                    return true
                } catch (e: Exception) {
                    val errorMessage =
                        "Unknown exception with table $id!\nLink: $tableLink!\nError:\n${e.stackTraceToString()}"
                    logger.severe(errorMessage)
                    if (i == RETRY_COUNT) {
                        Messager.sendMessage(bot = InitialProperties.bot, chatId = chatId, text = errorMessage)
                    }
                }
            }
            logger.info("Refreshing table job stopped after $RETRY_COUNT attempts")
            return false
        }

        @Throws(Exception::class)
        private fun runNeercTask(groupId: Long, sourceLink: String, tableId: String, tableLink: String) {
            val actualTasks: List<String> = NeercParserInfo(sourceLink).getTasks()
            val googleSheet = GoogleSheet(GoogleTableResponse.createSheetsService(), tableId)

            logger.info("Indicator of creating new teacher sheet: ${googleSheet.generateTeacherSheet()}")

            val fcsTasksWithMarks = googleSheet.getFCSTasksWithMarks()
            val currentTasks: List<String> = fcsTasksWithMarks.firstOrNull()?.drop(1) ?: listOf()
            val teacherSheetBody: List<List<String>> =
                if (fcsTasksWithMarks.isEmpty()) listOf()
                else fcsTasksWithMarks.transferStudentTableToTeacher().transferFCSToLastName()

            // Add newList
            if (currentTasks.isEmpty() && actualTasks.isNotEmpty() || actualTasks.last() != currentTasks.last()) {
                googleSheet.generateSheet(actualTasks.subList(currentTasks.size, actualTasks.size))
            }

            // Update teacher sheet List
            if (teacherSheetBody.isNotEmpty() && !deepEquals(googleSheet.getTeacherList(), teacherSheetBody)) {
                googleSheet.updateTeacherTableCells(GoogleSheet.transformListToRowDataOfString(teacherSheetBody))
            }
            logger.info("End with group $groupId, link $tableLink")
        }

        private const val RETRY_COUNT = 5
    }
}
