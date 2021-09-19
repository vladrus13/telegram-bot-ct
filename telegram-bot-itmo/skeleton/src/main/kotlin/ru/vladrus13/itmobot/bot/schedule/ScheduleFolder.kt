package ru.vladrus13.itmobot.bot.schedule

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.utils.Utils

class ScheduleFolder(
    override val parent: Menu
) : Menu(parent) {

    private val getCommand = GetScheduleCommand(this)

    override val childes: Array<Foldable>
        get() = arrayOf(getCommand)

    override fun menuHelp(): String {
        return "Меню расписания"
    }

    override val name: String
        get() = "Меню расписания"
    override val systemName: String
        get() = "schedule"

    override fun isAccept(update: Update): Boolean {
        return update.message.text == name
    }

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = Utils.splitBy(getCommand.names)
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (mapping(update, bot, user)) return
        unknownCommand(bot, user)
    }
}