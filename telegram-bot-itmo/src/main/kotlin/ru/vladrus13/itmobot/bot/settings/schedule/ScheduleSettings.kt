package ru.vladrus13.itmobot.bot.settings.schedule

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.utils.Utils

class ScheduleSettings(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf()

    override fun menuHelp(): String = "Настройки формата расписания"

    override val name: String
        get() = "Настройки расписания"
    override val systemName: String
        get() = "scheduleSettings"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = Utils.splitBy(user.settings.getStatus())
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text = update.message.text!!
        val isOn = text.endsWith("(включено)")
        val isOff = text.endsWith("(выключено)")
        if (isOn || isOff) {
            var flag = false
            if (text.startsWith("Показывать конец пары")) {
                user.settings.isShowEnd = !isOn
                flag = true
            }
            if (text.startsWith("Показывать преподавателя")) {
                user.settings.isShowTeacher = !isOn
                flag = true
            }
            if (text.startsWith("Показывать компактно")) {
                user.settings.isCompact = !isOn
                flag = true
            }
            if (flag) {
                user.send(
                    bot = bot,
                    text = "Успешно изменено!",
                    replyKeyboard = getReplyKeyboard(user)
                )
                return
            }
        }
        unknownCommand(bot, user)
    }

}