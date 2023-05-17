package ru.vladrus13.itmobot.plugin.practice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.timeToReloadJobs
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.getCredentials
import ru.vladrus13.itmobot.parallel.ThreadHolder

class AddTable(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "Пункт создания таблицы"

    override val name: String
        get() = "Создать новую таблицу (введите название)"
    override val systemName: String
        get() = "makeTable"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text: String = update.message.text
        val listTexts = text
            .split("\n")
            .filter(String::isNotBlank)
            .map { item -> listOf(item) };
        if (text.isBlank()) {
            unknownCommand(bot, user)
        }

        // All what I need for creating table
        val name: String = listTexts[0][0]
        val peopleList = listTexts.subList(1, listTexts.size)

        val spreadsheet = Spreadsheet()
            .setProperties(
                SpreadsheetProperties()
                    .setTitle(name)
            )
        val sheetsService = createSheetsService()

        val request = sheetsService.spreadsheets().create(spreadsheet)

        val response: String = request.execute().toString()

        val mapper = ObjectMapper()

        val node = mapper.readTree(response)
        val id = deleteBrackets(node.get("spreadsheetId").toString())
        val url = deleteBrackets(node.get("spreadsheetUrl").toString())

        // I need it for writing cells

        val body = ValueRange()
            .setValues(peopleList)
        val range = "Sheet1!A1:A" + peopleList.size
        val secondRange = "Sheet1!A" + (peopleList.size) + ":A" + (peopleList.size * 2 - 1)

        val result: UpdateValuesResponse =
            sheetsService.spreadsheets().values().update(id, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()
        val result2 = sheetsService.spreadsheets().values().update(id, secondRange, body)
            .setValueInputOption("USER_ENTERED")
            .execute()

        val service = createDriveService()

        insertPermission(service, id)



        ThreadHolder.executorService.submit {
            while (true) {

                Thread.sleep(timeToReloadJobs)
            }
        }

        user.send(
            bot = bot,
            text = url,
            replyKeyboard = getReplyKeyboard(user)
        )
    }

    companion object {
        private const val APPLICATION_NAME = "ScoresBot"
        private val JSON_FACTORY: GsonFactory = GsonFactory.getDefaultInstance()
        private val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        private fun createSheetsService() = Sheets.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getCredentials(HTTP_TRANSPORT)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()

        private fun createDriveService(): Drive =

            Drive.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                getCredentials(HTTP_TRANSPORT)
            )
                .setApplicationName(APPLICATION_NAME)
                .build();

        private fun deleteBrackets(str: String) =
            if (str.length >= 2 && str.first() == '"' && str.last() == '"')
                str.substring(1, str.length - 1)
            else str

        private fun insertPermission(service: Drive, fileId: String) =
            service
                .Permissions()
                .create(
                    fileId,
                    // I don't understand why i can't create permission, maybe can update?
                    Permission()
                        .setType("user")
                        .setEmailAddress("ct.36.y2021@gmail.com")
                        .setRole("writer")
                )
                .execute()
    }
}