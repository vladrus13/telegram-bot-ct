package ru.vladrus13.itmobot.google

import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import java.io.IOException

class ExecuteSchedulerService {
    companion object {
        fun getValueRange(range: String, service: Sheets, id: String) = service
            .spreadsheets()
            .values()
            .get(id, range)
            .execute()

        fun executeRequestsSequence(service: Sheets, id: String, vararg requests: Request) =
            service
                .spreadsheets()
                .batchUpdate(id, BatchUpdateSpreadsheetRequest().setRequests(requests.toList())).execute()

        fun getSpreadSheetById(service: Sheets, id: String): Spreadsheet = service.spreadsheets().get(id).execute()

        @Suppress("UNCHECKED_CAST")
        fun getSheets(service: Sheets, address: String): ArrayList<Sheet> =
            service.Spreadsheets().get(address).execute()["sheets"] as ArrayList<Sheet>

        @Throws(IOException::class, TokenResponseException::class)
        fun insertPermission(
            service: Drive,
            fileId: String,
        ): Permission = service
            .Permissions()
            .create(fileId, Permission().setType("anyone").setRole("writer"))
            .execute()

        fun getSpreadsheet(
            sheetService: Sheets,
            spreadsheet: Spreadsheet
        ): Spreadsheet? = sheetService.spreadsheets().create(spreadsheet).execute()
    }
}