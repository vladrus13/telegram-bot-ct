package ru.vladrus13.itmobot.plugin.practice.subjecttable

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
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
import ru.vladrus13.itmobot.plugin.practice.CoroutineJob
import ru.vladrus13.itmobot.plugin.practice.CoroutineJob.Companion.NEERC_JOB
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet
import java.util.logging.Logger

class NeercTable(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "Пункт создания таблицы"

    override val name: String
        get() = "Создать новую таблицу (в первой строке - название таблицы; во второй - ссылку на сайт; в следующих строках ФИО студентов)"
    override val systemName: String
        get() = "makeTable"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text: String = update.message.text
        val listTexts = getTextLines(text)
        if (text.isBlank()) {
            unknownCommand(bot, user)
        }

        // All what I need for creating table
        // Example input:
        // """
        // TestNameTable
        // link to tasks
        // Grunskii Alexey
        // Vladimir Kuznetsov
        // """

        val sheetsService = createSheetsService()
        val driveService = createDriveService()

        val name: String = listTexts[0]
        val link: String = listTexts[1]
        val students = listTexts.subList(2, listTexts.size)

        val url = createTable(sheetsService, driveService, name, link, students, user.chatId)
        logger.info("Done making table '$name' with $url")

        user.send(
            bot = bot,
            text = url,
            replyKeyboard = getReplyKeyboard(user)
        )
    }

    companion object {
        fun createTable(sheetsService: Sheets, driveService: Drive, name: String, link: String, students: List<String>, chatId: Long): String {
            val spreadsheet = Spreadsheet()
                .setProperties(
                    SpreadsheetProperties()
                        .setTitle(name)
                )

            val dictData = getTableInfo(sheetsService, spreadsheet)
            val id: String = dictData["spreadsheetId"] ?: ""
            val url: String = dictData["spreadsheetUrl"] ?: ""

            val googleSheet = GoogleSheet(sheetsService, id)
            googleSheet.generateMainSheet(students)

            // Allow all to edit page
            insertPermission(driveService, id)

            CoroutineJob.addTask(NEERC_JOB, link, url, id, chatId)

            return url
        }

        fun getTextLines(text: String) = text
            .split("\n")
            .filter(String::isNotBlank)
    }
}