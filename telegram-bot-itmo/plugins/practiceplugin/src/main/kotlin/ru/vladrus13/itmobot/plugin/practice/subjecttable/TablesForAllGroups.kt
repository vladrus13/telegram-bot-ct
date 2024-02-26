package ru.vladrus13.itmobot.plugin.practice.subjecttable

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.createDriveService
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.createSheetsService
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet
import java.util.logging.Logger

class TablesForAllGroups(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "1 строка - айди главной таблицы ДМ с результатами; 2 строка - название страницы, на которой находятся люди с номером группы (\"Результаты\"); 3 строка - ссылка на таблицу с заданиями"

    override val name: String
        get() = "Создать таблицы для всех групп"
    override val systemName: String
        get() = "makeTablesForAllGroups"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text: String = update.message.text
        val listTexts = SingleTable.getTextLines(text)
        if (text.isBlank()) {
            unknownCommand(bot, user)
        }

        val sheetService = createSheetsService()
        val driveService = createDriveService()

        val mainSheetId: String = listTexts[0]
        val listName: String = listTexts[1]
        val linkTasks: String = listTexts[2]

        val urls: List<String> = GoogleSheet(sheetService, mainSheetId)
            .getValueRange("$listName!A:B")
            .getValues()
            .map { list -> list.map(Any::toString) }
            .filter { list -> list.all(String::isNotBlank) && list.size == 2 }
            .groupBy { it[1] }
            .map { it.key + ": " + SingleTable.createTable(
                sheetService,
                driveService,
                it.key,
                linkTasks,
                it.value.map(List<String>::first),
                user.chatId
            ) }

        user.send(
            bot = bot,
            text = urls.joinToString("\n"),
            replyKeyboard = getReplyKeyboard(user)
        )
    }
}
