package ru.vladrus13.itmobot.bot.settings

import com.google.inject.Inject
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bean.UsersSubjects
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry

class SubjectSettingsMenu @Inject constructor(private val scheduleRegistry: ScheduleRegistry) :
    Menu(arrayOf()) {
    override val menuHelp = "Меню выбора предметов (для предметов по выбору)"
    override val name = "Настройки предметов"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val options = recollect(user.getSubjects(), scheduleRegistry.table.getSubjects())
        val rows = ArrayList<KeyboardRow>()
        for (part in options.chunked(2)) {
            val row = KeyboardRow()
            row.addAll(part.map { name })
            rows.add(row)
        }
        val backRow = KeyboardRow()
        backRow.add("Помощь")
        backRow.add("<< Назад")
        rows.add(backRow)
        return ReplyKeyboardMarkup(rows)
    }

    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return recollect(user.getSubjects(), scheduleRegistry.table.getSubjects())
    }

    private fun recollect(
        usersSubjects: UsersSubjects,
        subjects: Collection<String>
    ): List<String> {
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

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
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
                return true
            }
        }
        return false
    }
}