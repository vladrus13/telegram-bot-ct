package ru.vladrus13.itmobot.plugin.practice

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.createGridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getPrettyRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.nToAZ
import java.util.function.Function

class GoogleSheetUtils {
    companion object {
        enum class WHO_ENTERED {
            USER_ENTERED,
        }

        private const val MAIN_LIST_NAME = "Results"
        private const val FCS_COLUMN = "ФИО"
        private const val TOTAL_SCORES_COLUMN = "Total"
        private const val ONE_PRACTICE_TASKS_COLUMN = "S"
        private const val SCORES_FOR_DISCRETE_MATH_TASK: Int = 5

        private const val TASK_FIRST_COLUMN_INDEX = 3

        private fun getValueRange(sheetsService: Sheets, id: String, range: String) = sheetsService
            .spreadsheets()
            .values()
            .get(id, range)
            .execute()

        fun getTasksList(sheetsService: Sheets, id: String): List<List<String>> {
            val tasksList = mutableListOf<List<String>>()
            for (i in 1..30) {
                val result: ValueRange
                try {
                    result = getValueRange(sheetsService, id, "Д$i!1:1")
                } catch (e: GoogleJsonResponseException) {
                    break
                }

                val firstRow = result.getValues().first().map(Any::toString)
                tasksList.add(firstRow.subList(3, firstRow.size))
            }
            return tasksList
        }

        fun generateSheet(sheetsService: Sheets, id: String, students: List<String>, tasks: List<String>) {
            // make new list
            val homeworkCount = sheetsService.spreadsheets().get(id).execute().sheets.size
            val title = "Д$homeworkCount"
            val properties = SheetProperties().setTitle(title)
            val requests = listOf(
                Request().setAddSheet(AddSheetRequest().setProperties(properties))
            )
            val req = BatchUpdateSpreadsheetRequest().setRequests(requests.toList())
            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()

            fillInStudents(sheetsService, id, students, title) { ind -> "=$MAIN_LIST_NAME!B$ind" }

            val listBody = mutableListOf(mutableListOf(ONE_PRACTICE_TASKS_COLUMN) + tasks)
            for (i in 2..students.size + 1) {
                listBody.add(mutableListOf("=COUNTA(D$i:$i)"))
            }

            val lastRowNumber = students.size + 2
            val lastRow = mutableListOf("=COUNTIF(D$lastRowNumber:$lastRowNumber; \">0\")")
            for (taskIndex in tasks.indices) {
                val ch = nToAZ(taskIndex + TASK_FIRST_COLUMN_INDEX)
                lastRow.add("=COUNTA(${ch}2:${ch}${lastRowNumber - 1})")
            }
            listBody.add(lastRow)

            val body = ValueRange().setValues(listBody.toList())
            sheetsService.spreadsheets().values()
                .update(id, getPrettyRange(title, 0, lastRowNumber, 2, 2 + listBody[0].size), body)
                .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
                .execute()

            addNewMainListColumn(sheetsService, id, students, title)
        }

        fun addNewMainListColumn(sheetsService: Sheets, id: String, students: List<String>, titleSheet: String) {
            val width: Int
            try {
                val result = getValueRange(sheetsService, id, "Results!1:1")
                width = result.getValues().get(0).size
            } catch (e: GoogleJsonResponseException) {
                return
            }

            val listBody = mutableListOf(mutableListOf(titleSheet))
            for (studentIndex in students.indices) {
                val range = "$titleSheet!D${studentIndex + 2}:${studentIndex + 2}"
                listBody.add(mutableListOf("=${getCountIf(range, 'T')} + ${getCountIf(range, 'Т')}"))
            }

            val body = ValueRange().setValues(listBody.toList())
            sheetsService.spreadsheets().values()
                .update(id, getPrettyRange(MAIN_LIST_NAME, 0, students.size + 1, width, width + 1), body)
                .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
                .execute()
        }

        private fun getCountIf(range: String, char: Char) = "COUNTIF($range;\"$char\")"

        fun generateMainSheet(sheetsService: Sheets, id: String, students: List<String>) {
            // rename list to $MAIN_LIST_NAME
            val properties = SheetProperties().setIndex(0).setTitle(MAIN_LIST_NAME)
            val update = UpdateSheetPropertiesRequest()
                .setProperties(properties)
                .setFields("title")
            val requests = listOf(Request().setUpdateSheetProperties(update))
            val req = BatchUpdateSpreadsheetRequest()
                .setRequests(requests)
            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()

            fillInStudents(
                sheetsService,
                id,
                students,
                MAIN_LIST_NAME
            ) { ind -> "=SUM(C$ind:$ind)*$SCORES_FOR_DISCRETE_MATH_TASK" }
        }

        /**
         * @param title is "Д[0-9]+" or <code>MAIN_LIST_NAME</code>
         */
        private fun fillInStudents(
            sheetsService: Sheets,
            id: String,
            students: List<String>,
            title: String,
            getTotalCount: Function<Int, String>
        ) {
            val listBody = mutableListOf(listOf(FCS_COLUMN, TOTAL_SCORES_COLUMN))
            listBody.addAll(students.mapIndexed { index, name -> listOf(name, getTotalCount.apply(index + 2)) })
            val body = ValueRange().setValues(listBody.toList())
            val range = getPrettyRange(title, 0, body.getValues().size, 0, 2)
            sheetsService.spreadsheets().values()
                .update(id, range, body)
                .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
                .execute()


            val requests = mutableListOf<Request>()
            // Border
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    0,
                    body.getValues().size,
                    0,
                    1
                ).colorizeBorders()
            )
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    0,
                    body.getValues().size,
                    1,
                    2
                ).colorizeBorders()
            )
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    0,
                    1,
                    0,
                    body.getValues().first().size
                ).colorizeBorders()
            )
            // Align
            requests.add(createGridRequestMaker(sheetsService, id, title, 0, body.getValues().size, 0, 2).formatCells())
            val req = BatchUpdateSpreadsheetRequest().setRequests(requests.toList())
            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()
        }
    }
}