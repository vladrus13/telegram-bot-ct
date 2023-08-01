package ru.vladrus13.itmobot.bot.results

import com.google.inject.Inject
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.tables.PointTablesRegistry

class ResultsCommand @Inject constructor(private val pointTablesRegistry: PointTablesRegistry) :
    Command() {
    override val name: String
        get() = "Результаты"
    override val help: String
        get() = "Получение результатов"

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
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
        val tables = pointTablesRegistry.pointsTableNamesByGroup[group]
        if (tables.isNullOrEmpty()) {
            user.send(
                bot = bot,
                text = "У вас нет таблиц для контроля!"
            )
            return
        }
        val realTables = tables
            .mapNotNull { pointTablesRegistry[it] }
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
            sb.append("${result.tableName} → ${result.result}\n")
        }
        user.send(
            bot = bot,
            text = sb.toString()
        )
    }
}