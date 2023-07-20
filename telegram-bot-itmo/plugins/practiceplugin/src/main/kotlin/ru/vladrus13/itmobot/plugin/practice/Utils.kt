package ru.vladrus13.itmobot.plugin.practice

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker
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
            val name = "Д$homeworkCount"

            val properties = SheetProperties().setTitle(name)

            val requests = listOf(
                Request().setAddSheet(AddSheetRequest().setProperties(properties))
            )

            val req = BatchUpdateSpreadsheetRequest().setRequests(requests.toList())

            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()

            fillInStudents(sheetsService, id, students, name) { ind -> "=Results!B$ind" }
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

        private fun getSheetIdFromTitle(sheetsService: Sheets, id: String, title: String): Int {
            for (sheet in sheetsService.spreadsheets().get(id).execute().sheets) {
                if (sheet.properties.title == title)
                    return sheet.properties.sheetId
            }
            throw IllegalArgumentException("No sheet with current title")
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

            val sheetIndex = getSheetIdFromTitle(sheetsService, id, title)
            // Border
            requests.add(GridRequestMaker(sheetIndex, 0, body.getValues().size, 0, 1).updateBorders())
            requests.add(GridRequestMaker(sheetIndex, 0, body.getValues().size, 1, 2).updateBorders())
            requests.add(GridRequestMaker(sheetIndex, 0, 1, 0, body.getValues().first().size).updateBorders())
            // Align
            requests.add(GridRequestMaker(sheetIndex, 0, body.getValues().size, 0, 2).updateCells())

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