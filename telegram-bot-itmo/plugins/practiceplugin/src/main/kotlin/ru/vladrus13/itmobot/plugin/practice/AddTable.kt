package ru.vladrus13.itmobot.plugin.practice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.parallel.CoroutineThreadOverseer
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleApi.Companion.createDriveService
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleApi.Companion.createSheetsService
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleApi.Companion.getTableInfo
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleApi.Companion.insertPermission
import ru.vladrus13.itmobot.plugin.practice.parsers.neerc.NeercParserInfo
import java.util.logging.Logger

class AddTable(override val parent: Menu) : Menu(parent) {
    override val logger: Logger
        get() = super.logger
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "Пункт создания таблицы"

    override val name: String
        get() = "Создать новую таблицу (введите название таблицы, а в следующих строка ФИО студентов)"
    override val systemName: String
        get() = "makeTable"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text: String = update.message.text
        val listTexts = text
            .split("\n")
            .filter(String::isNotBlank)
        if (text.isBlank()) {
            unknownCommand(bot, user)
        }

        // All what I need for creating table
        val name: String = listTexts[0]
//        val link: String = listTexts[1]
        val peopleList = listTexts.subList(1, listTexts.size)

        val spreadsheet = Spreadsheet()
            .setProperties(
                SpreadsheetProperties()
                    .setTitle(name)
            )
        val sheetsService = createSheetsService()

        val dictData = getTableInfo(sheetsService, spreadsheet)
        val id: String = dictData["spreadsheetId"] ?: ""
        val url: String = dictData["spreadsheetUrl"] ?: ""

        Utils.generateMainList(sheetsService, id, peopleList)
//        // I need it for writing cells
//        val body = ValueRange()
//            .setValues(peopleList.map { item -> listOf(item) })
//        val range = "Sheet1!A1:A" + peopleList.size
//
//        val result: UpdateValuesResponse =
//            sheetsService.spreadsheets().values().update(id, range, body)
//                .setValueInputOption("USER_ENTERED")
//                .execute()

        val service = createDriveService()
        insertPermission(service, id)



//        runBlocking {
//            val job = launch {
//                NeercParserInfo(id, url).
//            }
//            CoroutineThreadOverseer.addTask(job)
//        }

        user.send(
            bot = bot,
            text = url,
            replyKeyboard = getReplyKeyboard(user)
        )
    }
}