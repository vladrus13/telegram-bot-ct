package ru.vladrus13.itmobot.bot

import com.google.inject.Inject
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.tables.PointTablesRegistry

class LinkCommand @Inject constructor(private val pointTablesRegistry: PointTablesRegistry) : Command() {
    override val name = listOf("Ссылки")
    override val help = "Возвращает ссылки"

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "Пожалуйста, выберите группу в меню настроек!"
            )
        } else {
            val group = user.group!!
            var message = pointTablesRegistry.linksByGroup[group]
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