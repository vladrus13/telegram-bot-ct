package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.tables.MainTableHolder

class LinkCommand(override val parent: Menu) : Command() {

    override fun help(): String {
        return "Возвращает ссылки"
    }

    override val name: String
        get() = "Ссылки"
    override val systemName: String
        get() = "link"

    override fun isAccept(update: Update): Boolean {
        return update.message.text!! == name
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "Пожалуйста, выберите группу в меню настроек!"
            )
        } else {
            val group = user.group!!
            var message = MainTableHolder.links[group]
                ?: "Ошибка при поиске вашей таблички. Возможные проблемы: у бота проблемы с табличками, у вашей группы нет таблички"
            if (message.isEmpty()) {
                message = "Табличка для вашей группы была создана, но, к сожалению, не содержит ссылок"
            }
            user.send(
                bot = bot,
                text = message,
                other = {
                    it.enableHtml(true)
                    it.disableWebPagePreview()
                }
            )
        }
    }

}