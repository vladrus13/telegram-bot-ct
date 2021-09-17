package ru.vladrus13.itmobot.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.*
import java.security.GeneralSecurityException

class GoogleTableResponse {
    companion object {
        private const val APPLICATION_NAME = "ParseScheduleBot"
        private val JSON_FACTORY: GsonFactory = GsonFactory.getDefaultInstance()
        private const val TOKENS_DIRECTORY_PATH = "tokens"

        private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
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
            val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            rangeCopy = "$sheetName!$rangeCopy"
            val service: Sheets = Sheets.Builder(
                httpTransport,
                JSON_FACTORY,
                getCredentials(httpTransport)
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
            val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val service: Sheets = Sheets.Builder(
                httpTransport,
                JSON_FACTORY,
                getCredentials(httpTransport)
            )
                .setApplicationName(APPLICATION_NAME)
                .build()
            val sheets: ArrayList<Sheet> = service.Spreadsheets().get(address).execute()["sheets"] as ArrayList<Sheet>
            for (it in sheets) {
                list.add((it["properties"] as SheetProperties)["title"] as String)
            }
            return list
        }
    }
}