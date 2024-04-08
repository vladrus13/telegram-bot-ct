package ru.vladrus13.itmobot.plugin.practice.subjecttable

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.google.ExecuteSchedulerService
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.createDriveService
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.getTableInfo
import ru.vladrus13.itmobot.plugin.practice.CoroutineJob
import ru.vladrus13.itmobot.plugin.practice.CoroutineJob.Companion.NEERC_JOB
import ru.vladrus13.itmobot.plugin.practice.SheetJobTable
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet
import java.util.logging.Logger

class AddUserAdmins(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "Добавить админов в таблицы"
    override val name: String
        get() = "Добавить админов всем таблицам"
    override val systemName: String
        get() = "addAdmin"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return

        var allSheetTables: List<String> = emptyList()
        transaction(DataBaseParser.connection) {
            allSheetTables = SheetJobTable
                .selectAll()
                .map { it[SheetJobTable.tableId] }
        }

        val driveService = createDriveService()

        allSheetTables.forEach { str ->
            driveService.permissions().create(
                str,
                Permission().setType("user").setRole("writer").setEmailAddress("itmo.ct.bot.service@gmail.com")
            )
        }
        user.send(
            bot = bot,
            text = "done",
            replyKeyboard = getReplyKeyboard(user)
        )
    }

    companion object {
        fun createTable(
            sheetsService: Sheets,
            driveService: Drive,
            name: String,
            link: String,
            students: List<String>,
            chatId: Long
        ): String {
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
            ExecuteSchedulerService.insertPermission(driveService, id)

            CoroutineJob.addTask(NEERC_JOB, link, url, id, chatId)

            return url
        }

        fun getTextLines(text: String) = text
            .split("\n")
            .filter(String::isNotBlank)
    }
}
