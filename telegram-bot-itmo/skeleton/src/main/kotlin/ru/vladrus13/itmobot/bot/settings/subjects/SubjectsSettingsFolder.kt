package ru.vladrus13.itmobot.bot.settings.subjects

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bean.UsersSubjects
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry
import ru.vladrus13.itmobot.utils.Utils

class SubjectsSettingsFolder(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf()

    override fun menuHelp(): String = "Меню выбора предметов (для предметов по выбору)"

    override val name: String
        get() = "Настройки предметов"
    override val systemName: String
        get() = "subjectsSettings"

    private fun recollect(usersSubjects: UsersSubjects, subjects: Collection<String>): Collection<String> {
        val array = ArrayList<String>()
        subjects.forEach {
            array.add(
                if (usersSubjects.include.contains(it)) {
                    "Отключить включение \"$it\""
                } else {
                    "Включить \"$it\""
                }
            )
            array.add(
                if (usersSubjects.exclude.contains(it)) {
                    "Отключить отключение \"$it\""
                } else {
                    "Отключить \"$it\""
                }
            )
        }
        return array
    }

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        // TODO прикрутить DI
        val list = Utils.splitBy(recollect(user.getSubjects(), ScheduleRegistry().table.getSubjects()))
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text = update.message.text!!
        val splitted = text.split("\"")
        if (splitted.size == 3) {
            val subjects = user.getSubjects()
            var flag = true
            when (splitted[0]) {
                "Отключить включение " -> subjects.include.remove(splitted[1])
                "Включить " -> subjects.include.add(splitted[1])
                "Отключить отключение " -> subjects.exclude.remove(splitted[1])
                "Отключить " -> subjects.exclude.add(splitted[1])
                else -> flag = false
            }
            if (flag) {
                DataBase.put(subjects.chatId, subjects)
                user.send(
                    bot = bot,
                    text = "Успешно!",
                    replyKeyboard = getReplyKeyboard(user)
                )
                return
            }
        }
        unknownCommand(bot, user)
    }
}