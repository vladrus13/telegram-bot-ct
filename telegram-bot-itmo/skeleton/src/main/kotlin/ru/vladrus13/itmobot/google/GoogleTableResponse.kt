package ru.vladrus13.itmobot.google

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.*
import java.security.GeneralSecurityException

class GoogleTableResponse {
    companion object {
        private const val APPLICATION_NAME = "ParseScheduleBot"
        private const val TOKENS_DIRECTORY_PATH = "tokens"
        private val JSON_FACTORY: GsonFactory = GsonFactory.getDefaultInstance()
        private val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        private val mapper = ObjectMapper()

        private val SCOPES = listOf(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE, SheetsScopes.DRIVE_FILE)
        private const val CREDENTIALS_FILE_PATH = "/credentials.json"

        @Throws(IOException::class)
        fun getCredentials(HTTP_TRANSPORT: NetHttpTransport?): Credential? {
            val `in`: InputStream = GoogleTableResponse::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
            val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
            val flow: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
            )
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
            val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8888).build()
            return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        }

        @Throws(IOException::class, GeneralSecurityException::class)
        fun reload(address: String, sheetName: String, range: String): ArrayList<ArrayList<String>> {
            var rangeCopy = range
            rangeCopy = "$sheetName!$rangeCopy"
            val service: Sheets = Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT)
            )
                .setApplicationName(APPLICATION_NAME)
                .build()
            val response: ValueRange = service.spreadsheets().values()
                .get(address, rangeCopy)
                .execute()
            val values = response.getValues()
            val answer: ArrayList<ArrayList<String>> = ArrayList()
            if (values != null) {
                for (it in values) {
                    val row: ArrayList<String> = ArrayList()
                    for (jt in it) {
                        if (jt != null) {
                            row.add(jt.toString())
                        } else {
                            row.add("")
                        }
                    }
                    answer.add(row)
                }
            }
            return answer
        }

        fun getNames(address: String): ArrayList<String> {
            val list: ArrayList<String> = ArrayList()
            val service: Sheets = Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT)
            )
                .setApplicationName(APPLICATION_NAME)
                .build()
            val sheets: ArrayList<Sheet> = service.Spreadsheets().get(address).execute()["sheets"] as ArrayList<Sheet>
            for (it in sheets) {
                list.add((it["properties"] as SheetProperties)["title"] as String)
            }
            return list
        }

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

        fun deleteQuotationMarks(str: String) =
            if (str.length >= 2 && str.first() == '"' && str.last() == '"')
                str.substring(1, str.length - 1)
            else str
    }
}