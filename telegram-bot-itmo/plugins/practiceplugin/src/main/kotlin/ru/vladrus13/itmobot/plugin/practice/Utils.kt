package ru.vladrus13.itmobot.plugin.practice

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import java.util.Arrays.asList

class Utils {
    companion object {
        val const: Int = 5

        fun deleteBrackets(str: String) =
            if (str.length >= 2 && str.first() == '"' && str.last() == '"')
                str.substring(1, str.length - 1)
            else str

        fun generateList(sheetsService: Sheets, id: String, students: List<String>) {

        }

//        fun getCurrentBody(sheetsService: Sheets, id: String): MutableList<MutableList<String>> {
//
//        }

        fun generateMainList(sheetsService: Sheets, id: String, students: List<String>) {
            val listBody = mutableListOf(listOf("ФИО", "Total"))
            listBody.addAll(students.mapIndexed { index, name -> listOf(name, "=SUM(C${index + 2}:${index + 2})*$const") })

            val body = ValueRange().setValues(listBody.toList())

            val range = "Sheet1!A1:B" + body.getValues().size

            // Enter input
            sheetsService.spreadsheets().values()
                .update(id, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()

            // requests
            val requests = mutableListOf<Request>()
            requests.add(RequestMaker.updateBorders(0, 0, body.getValues().size, 0, 1))
            requests.add(RequestMaker.updateBorders(0, 0, body.getValues().size, 1, 2))
            requests.add(RequestMaker.updateBorders(0, 0, 1, 0, body.getValues().first().size))

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