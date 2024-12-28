package ru.vladrus13.itmobot.plugin.practice.subjecttable

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.createDriveService
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.insertPermission

class SetWritePermissions(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = """Даст всем людям - право писать в таблицу, необходимо передать id таблицы"""

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text: String = update.message.text
        val listTexts = getTextLines(text)
        if (text.isBlank()) {
            unknownCommand(bot, user)
        }

        val id = listTexts[0]

        val driveService = createDriveService()
        insertPermission(driveService, id)

        user.send(
            bot = bot,
            text = "Получилось всем дать доступ к таблице",
            replyKeyboard = getReplyKeyboard(user),
        )
    }

    override val name: String = "Дать всем права изменять таблицу"
    override val systemName: String = "getWritePermissions"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    private fun getTextLines(text: String) = text
        .split("\n")
        .filter(String::isNotBlank)
}