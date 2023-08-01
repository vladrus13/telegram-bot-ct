package ru.vladrus13.itmobot.bot.results

import com.google.inject.Inject
import ru.vladrus13.itmobot.command.Menu

class ResultsMenu @Inject constructor(resultsCommand: ResultsCommand) : Menu(arrayOf(resultsCommand)) {
    override val menuHelp: String
        get() = "Меню результатов"
    override val name: String
        get() = "Результаты"
}