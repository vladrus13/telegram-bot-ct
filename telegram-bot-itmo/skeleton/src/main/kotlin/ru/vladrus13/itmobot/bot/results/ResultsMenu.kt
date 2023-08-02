package ru.vladrus13.itmobot.bot.results

import com.google.inject.Inject
import ru.vladrus13.itmobot.command.Menu

class ResultsMenu @Inject constructor(resultsCommand: ResultsCommand) : Menu(arrayOf(resultsCommand)) {
    override val menuHelp = "Меню результатов"
    override val name = listOf("Результаты")
}