package ru.vladrus13.itmobot.plugin.practice.googleapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.getCredentials
import ru.vladrus13.itmobot.plugin.practice.GoogleSheetUtils.Companion.deleteQuotationMarks

class GoogleApi {
    companion object {
        private const val APPLICATION_NAME = "ScoresBot"
        private val JSON_FACTORY: GsonFactory = GsonFactory.getDefaultInstance()
        private val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        private val mapper = ObjectMapper()

        fun insertPermission(
            service: Drive,
            fileId: String,
            list: List<(Permission) -> Permission>
        ): Permission =
            service
                .Permissions()
                .create(
                    fileId,
                    list.fold(Permission()) { permission, f -> f.invoke(permission) })
                .execute()

        fun insertPermission(service: Drive, fileId: String): Permission =
            insertPermission(
                service,
                fileId,
                listOf(
                    { permission: Permission -> permission.setType("anyone") },
                    { permission: Permission -> permission.setRole("writer") })
            )

        fun createSheetsService(): Sheets = Sheets.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getCredentials(HTTP_TRANSPORT)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()

        fun createDriveService(): Drive =
            Drive.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                getCredentials(HTTP_TRANSPORT)
            )
                .setApplicationName(APPLICATION_NAME)
                .build()

        fun getTableInfo(sheetService: Sheets, spreadsheet: Spreadsheet): Map<String, String> {
            val node = mapper.readTree(
                sheetService
                    .spreadsheets()
                    .create(spreadsheet)
                    .execute()
                    .toString()
            )

            val id = "spreadsheetId"
            val url = "spreadsheetUrl"
            return mapOf(
                Pair(id, deleteQuotationMarks(node.get(id).toString())),
                Pair(url, deleteQuotationMarks(node.get(url).toString()))
            )
        }
    }
}