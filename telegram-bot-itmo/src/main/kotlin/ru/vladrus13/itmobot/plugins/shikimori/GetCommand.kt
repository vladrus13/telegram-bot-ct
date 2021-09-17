package ru.vladrus13.itmobot.plugins.shikimori

import org.json.JSONArray
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu

class GetCommand(override val parent: Menu) : Command() {
    override fun help(): String = "ШикиГет"

    override val name: String
        get() = "Шикимори"
    override val systemName: String
        get() = "shikimoriGet"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    fun getReal(): String {
        val respond = ru.vladrus13.shikimori.Animes.get()
        val json = JSONArray(respond)
        return "<code>${json.toString(1)}</code>"
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = getReal(),
            other = {
                it.enableHtml(true)
            }
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        chat.send(
            bot = bot,
            text = getReal(),
            other = {
                it.enableHtml(true)
            }
        )
    }
}