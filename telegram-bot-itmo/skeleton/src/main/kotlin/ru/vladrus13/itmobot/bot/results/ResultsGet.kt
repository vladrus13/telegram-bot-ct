package ru.vladrus13.itmobot.bot.results

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.tables.MainTableHolder
import ru.vladrus13.itmobot.tables.TableGroupsHolder

class ResultsGet(override val parent: Menu) : Command() {
    override fun help(): String = "Получение результатов"

    override val name: String = "Результаты"
    override val systemName: String = "results"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "У вас не выбрана группа! Выберете ее в настройках группы"
            )
            return
        }
        if (user.name == null) {
            user.send(
                bot = bot,
                text = "У вас не выбрано имя! Выберете его в настройках имени"
            )
            return
        }
        val group = user.group!!
        val tables = MainTableHolder.groupsTables[group]
        if (tables == null || tables.isEmpty()) {
            user.send(
                bot = bot,
                text = "У вас нет таблиц для контроля!"
            )
            return
        }
        val realTables = tables
            .map { TableGroupsHolder[it] }
            .mapNotNull { it[user.name!!] }
        if (realTables.isEmpty()) {
            user.send(
                bot = bot,
                text = "У вас нет данных среди таблиц!"
            )
            return
        }
        val sb = StringBuilder("Ваши результаты: \n")
        for (result in realTables) {
            sb.append("${result.tableName} -> ${result.result}\n")
        }
        user.send(
            bot = bot,
            text = sb.toString()
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        chat.send(
            bot = bot,
            text = "Получение результатов доступно только в личных сообщениях!"
        )
    }
}