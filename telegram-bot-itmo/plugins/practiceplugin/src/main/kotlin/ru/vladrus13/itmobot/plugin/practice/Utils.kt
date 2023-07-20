package ru.vladrus13.itmobot.plugin.practice

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.createGridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getPrettyRange
import java.lang.IllegalArgumentException
import java.util.function.Function

class Utils {
    companion object {
        val const: Int = 5

        fun deleteBrackets(str: String) =
            if (str.length >= 2 && str.first() == '"' && str.last() == '"')
                str.substring(1, str.length - 1)
            else str

        fun generateList(sheetsService: Sheets, id: String, students: List<String>, tasks: List<String>) {
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

            fillInStudents(sheetsService, id, students, title) { ind -> "=Results!B$ind" }

             val listBody = mutableListOf(mutableListOf("S"))
            listBody[0].addAll(tasks)

            val body = ValueRange().setValues(listBody.toList())

            // Enter input
            sheetsService.spreadsheets().values()
                .update(id, getPrettyRange(title, 0, 1, 2, 2 + listBody[0].size), body)
                .setValueInputOption("USER_ENTERED")
                .execute()

        }

//        fun getCurrentBody(sheetsService: Sheets, id: String): MutableList<MutableList<String>> {
//
//        }

        fun generateMainList(sheetsService: Sheets, id: String, students: List<String>) {
            val properties = SheetProperties().setIndex(0).setTitle("Results")

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

            fillInStudents(sheetsService, id, students, "Results") { ind -> "=SUM(C$ind:$ind)*$const" }
        }

        /**
         * sheet is "[0-9]+Д" or "Results"
         */
        private fun fillInStudents(sheetsService: Sheets, id: String, students: List<String>, title: String, getTotalCount: Function<Int, String>) {
            val listBody = mutableListOf(listOf("ФИО", "Total"))
            listBody.addAll(students.mapIndexed { index, name -> listOf(name, getTotalCount.apply(index + 2)) })

            val body = ValueRange().setValues(listBody.toList())

            val range = "$title!A1:B" + body.getValues().size

            // Enter input
            sheetsService.spreadsheets().values()
                .update(id, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()

            val requests = mutableListOf<Request>()

            // Border
            requests.add(createGridRequestMaker(sheetsService, id, title, 0, body.getValues().size, 0, 1).updateBorders())
            requests.add(createGridRequestMaker(sheetsService, id, title, 0, body.getValues().size, 1, 2).updateBorders())
            requests.add(createGridRequestMaker(sheetsService, id, title, 0, 1, 0, body.getValues().first().size).updateBorders())
            // Align
            requests.add(createGridRequestMaker(sheetsService, id, title, 0, body.getValues().size, 0, 2).updateCells())

            val req = BatchUpdateSpreadsheetRequest().setRequests(requests.toList())

            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()
        }

        fun updateMainList(sheetsService: Sheets, id: String, students: List<String>) {
            // getCurrentBody
            // updating
        }
    }
}