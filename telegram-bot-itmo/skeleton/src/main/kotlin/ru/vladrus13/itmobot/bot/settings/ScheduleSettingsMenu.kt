package ru.vladrus13.itmobot.bot.settings

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu

class ScheduleSettingsMenu : Menu(arrayOf()) {
    override val menuHelp: String = "Настройки формата расписания"
    override val name: String = "Настройки расписания"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val rows = ArrayList<KeyboardRow>()
        for (part in user.settings.getStatus().chunked(2)) {
            val row = KeyboardRow()
            row.addAll(part.map { name })
            rows.add(row)
        }
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        rows.add(backRow)
        return ReplyKeyboardMarkup(rows)
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
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
                return true
            }
        }
        return false
    }
}