package ru.vladrus13.itmobot.plugin.practice

import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.createDriveService
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.createSheetsService
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.getTableInfo
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.insertPermission
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
        // Example input:
        // """
        // TestNameTable
        // Grunskii Alexey
        // Vladimir Kuznetsov
        // """

        val name: String = listTexts[0]
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

        GoogleSheetUtils.generateMainList(sheetsService, id, peopleList)

        GoogleSheetUtils.generateList(sheetsService, id, peopleList, (1 .. 8).map(Int::toString))
        GoogleSheetUtils.generateList(sheetsService, id, peopleList, (9 .. 20).map(Int::toString))

        val service = createDriveService()
        insertPermission(service, id)

        user.send(
            bot = bot,
            text = url,
            replyKeyboard = getReplyKeyboard(user)
        )
    }
}