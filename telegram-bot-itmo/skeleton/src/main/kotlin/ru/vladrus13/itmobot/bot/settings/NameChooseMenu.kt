package ru.vladrus13.itmobot.bot.settings

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu

class NameChooseMenu : Menu(arrayOf()) {
    override val menuHelp: String = "Пункт выбора имени"
    override val name: String = "Выбор имени"

    override fun onEnter(bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Напишите ваше имя. Оно будет использовано в качестве опознавателя для табличек"
        )
    }

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.name = update.message.text!!
        user.send(
            bot = bot,
            text = "Ваше имя успешно изменено на ${user.name}"
        )
    }
}